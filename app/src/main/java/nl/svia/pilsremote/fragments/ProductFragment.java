package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.adapters.ProductAdapter;
import nl.svia.pilsremote.misc.Backable;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;
import nl.svia.pilsremote.misc.ProductModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductFragment extends Fragment implements Backable {
    private static final String TAG = "ProductFragment";
    private static final String ARGUMENT_USER_ID = "ARG_USER_ID";
    private static final String ARGUMENT_PIN = "ARG_PIN";

    private NetworkFragment mNetworkFragment;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private SearchView mSearchView;

    private ProductAdapter mProductAdapter;

    private List<ProductModel> mProductList;

    private ProductAdapter.HeaderViewHolder mHeaderView;

    private int mUserId;
    private int mPin;
    private double mBalance;

    private SharedPreferences mSharedPrefs;

    public ProductFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static ProductFragment newInstance(int userId, int pin) {
        ProductFragment fragment = new ProductFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENT_USER_ID, userId);
        bundle.putInt(ARGUMENT_PIN, pin);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void setLoading(boolean flag, @Nullable CharSequence error) {
        if (flag) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);

            if (error == null) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
                mEmptyView.setText(error);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateBalanceText() {
        // TODO balance text won't be updated if search is active
        mHeaderView.getBalanceText().setText(getString(R.string.product_header_balance, mBalance));
        Log.d(TAG, "Set balance text");
    }

    private void updateBalance() {
        Log.d(TAG, "Updating balance");

        mNetworkFragment.getBalance(mPin, mUserId, new Response.Listener<Double>() {
            @Override
            public void onResponse(Double response) {
                Log.d(TAG, "Update balance response " + String.valueOf(response));
                mBalance = response;
                updateBalanceText();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Couldn't update balance!");
            }
        });

    }

    private void updateBalanceAndProducts() {
        setLoading(true, null);

        if (mNetworkFragment == null) {
            // This sometimes happens, but only during debugging (?)
            Log.e(TAG, "mNetworkFragment was null!");
            setLoading(false, getContext().getString(R.string.get_products_error));
        }

        mNetworkFragment.getBalanceAndProducts(mPin, mUserId,
                new Response.Listener<NetworkFragment.BalanceProductPair>() {
                    @Override
                    public void onResponse(NetworkFragment.BalanceProductPair response) {
                        mProductList = parseProducts(response.products);

                        if (mProductAdapter == null) {
                            createAdapter();
                            Log.d(TAG, "created adapter " + mProductList.size());
                        } else {
                            mProductAdapter.add(mProductList);
                        }

                        mRecyclerView.scrollToPosition(0);

                        mBalance = response.balance;
                        setLoading(false, null);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Product data could not be loaded: " + error.toString());
                        setLoading(false, getContext().getString(R.string.get_products_error));
                    }
                });
    }

    public void clearList() {
        if (mProductAdapter != null) {
            mProductAdapter.removeAll();
        }
    }

    private void createAdapter() {
        mProductAdapter = new ProductAdapter(getContext(), new ProductAdapter.ProductViewHolderListener() {
            @Override
            public void onItemClick(View view, int i) {
                ProductModel product = mProductAdapter.getItem(i);
                Log.d(TAG, "Pressed: " + product.toString());

                Bundle b = product.getBundle();
                PurchaseDialogFragment fragment = PurchaseDialogFragment.newInstance(mUserId, b, mPin);
                fragment.show(getActivity().getSupportFragmentManager(), "PurchaseDialogFragment");

                fragment.SetOnPurchaseListener(new PurchaseDialogFragment.OnPurchaseListener() {
                    @Override
                    public void onPurchase(ProductModel product, int amount) {
                        Toast toast = Toast.makeText(getContext(),
                                getString(R.string.purchase_success, amount, product.getName()),
                                Toast.LENGTH_SHORT);

                        toast.show();

                        ProductFragment.this.updateBalance();

                    }
                });

            }
        }, new ProductAdapter.HeaderViewHolderListener() {
            @Override
            public void onCreated(ProductAdapter.HeaderViewHolder headerViewHolder) {
                mHeaderView = headerViewHolder;
                updateBalanceText();
            }
        });
        mProductAdapter.add(mProductList);
        mRecyclerView.setAdapter(mProductAdapter);
    }

    private List<ProductModel> parseProducts(final JSONObject products) {
        List<ProductModel> productList = new ArrayList<>();
        Iterator<String> iter = products.keys();

        boolean beerOnly = mSharedPrefs.getBoolean(
                getActivity().getString(R.string.key_beer_only),
                getContext().getResources().getBoolean(R.bool.beer_only_default));

        while (iter.hasNext()) {
            String key = iter.next();
            try {
                JSONObject obj = products.getJSONObject(key);

                String name = obj.getString("name");

                if (beerOnly &&
                        !name.toLowerCase().contains(getString(R.string.beer_search_value))) {
                    continue;
                }

                int id = obj.getInt("id");
                double price = obj.getDouble("price");

                productList.add(new ProductModel(id, name, price));

            } catch (JSONException ignored) {
                // Just don't add it to the lsit
            }
        }

        return productList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "on create called");

        Bundle bundle = getArguments();

        if (bundle == null) {
            throw new RuntimeException("ProductFragment must be called with bundle");
        }

        mUserId = bundle.getInt(ARGUMENT_USER_ID, -1);
        mPin = bundle.getInt(ARGUMENT_PIN, -1);

        if (mUserId == -1 || mPin == -1) {
            throw new RuntimeException("ProductFragment must be called with correct bundle.");
        }

        setHasOptionsMenu(true);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mEmptyView = (TextView) rootView.findViewById(R.id.emptyText);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.userList);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (mProductAdapter != null) {
            // We are restoring from backstack, set the adapter
            mRecyclerView.setAdapter(mProductAdapter);
        }

        setLoading(true, null);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Onstart product fragment");
        updateBalanceAndProducts();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NetworkFragmentGetter) {
            mNetworkFragment = ((NetworkFragmentGetter) context).getNetworkFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement NetworkFragmentGetter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNetworkFragment = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_products, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        mSearchView.setOnQueryTextFocusChangeListener(new SearchView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && mSearchView.getQuery().length() == 0) {
                    mSearchView.setIconified(true);
                }

            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || mProductAdapter == null) {
                    return false;
                }

                final List<ProductModel> filtered = filter(newText);
                mProductAdapter.replaceAll(filtered);
                mRecyclerView.scrollToPosition(0);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                updateBalanceAndProducts();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<ProductModel> filter(String query) {
        if (mProductList == null) {
            return new ArrayList<>();
        }

        final String lowerCaseQuery = query.toLowerCase();

        final List<ProductModel> filteredModelList = new ArrayList<>();
        for (ProductModel product : mProductList) {
            final String text = product.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(product);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onBackPressed() {
        Log.d(TAG, "Back pressed fragment");

        return false;
    }
}
