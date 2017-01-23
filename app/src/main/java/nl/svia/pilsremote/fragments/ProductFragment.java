package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.adapters.ProductAdapter;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;
import nl.svia.pilsremote.misc.ProductObject;

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

    private List<ProductObject> mProductList;

    private int mUserId;
    private int mPin;
    private double mBalance;

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

    private void updateBalance() {
        mNetworkFragment.getBalance(mPin, mUserId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ProductAdapter.HeaderViewHolder header = (ProductAdapter.HeaderViewHolder)
                        mRecyclerView.findViewHolderForLayoutPosition(0);

                mBalance = Double.parseDouble(response);
                header.getBalanceText().setText(getString(R.string.product_header_balance, mBalance));
                header.setLoading(false);
                Log.d(TAG, "Updated balance " + header.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Couldn't update balance!");
            }
        });
    }

    private void createAdapter(final JSONObject products) {
        mProductList = new ArrayList<>();
        Iterator<String> iter = products.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                JSONObject obj = products.getJSONObject(key);

                int id = obj.getInt("id");
                String name = obj.getString("name");
                double price = obj.getDouble("price");

                mProductList.add(new ProductObject(id, name, price));

            } catch (JSONException ignored) {
                // Just don't add it to the lsit
            }
        }

        Log.d(TAG, "length/." + mProductList.size());

        mProductAdapter.add(mProductList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setLoading(true, null);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mProductAdapter = new ProductAdapter(getContext(), new ProductAdapter.ProductViewHolderListener() {
            @Override
            public void onItemClick(View view, int i) {
                ProductObject product = mProductAdapter.getItem(i);
                Log.d(TAG, "Pressed: " + product.toString());
            }
        });
        mRecyclerView.setAdapter(mProductAdapter);

        mNetworkFragment.getProducts(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                createAdapter(response);
                mRecyclerView.scrollToPosition(0);
                setLoading(false, null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setLoading(false, getContext().getString(R.string.get_products_error));
            }
        });

        updateBalance();
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
                final List<ProductObject> filtered = filter(newText);
                mProductAdapter.replaceAll(filtered);
                mRecyclerView.scrollToPosition(0);
                return true;
            }
        });

        final MenuItem resetButton = menu.findItem(R.id.action_reset);
        resetButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(getString(R.string.pref_user_id), -1);
                editor.putInt(getString(R.string.pref_pin), -1);
                editor.commit();

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private List<ProductObject> filter(String query) {
        if (mProductList == null) {
            return new ArrayList<>();
        }

        final String lowerCaseQuery = query.toLowerCase();

        final List<ProductObject> filteredModelList = new ArrayList<>();
        for (ProductObject product : mProductList) {
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
