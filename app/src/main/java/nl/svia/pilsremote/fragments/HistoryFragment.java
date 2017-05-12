package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseArray;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.adapters.ProductAdapter;
import nl.svia.pilsremote.adapters.PurchaseAdapter;
import nl.svia.pilsremote.data.DatabaseHelper;
import nl.svia.pilsremote.misc.Backable;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;
import nl.svia.pilsremote.misc.ProductModel;
import nl.svia.pilsremote.misc.PurchaseModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements Backable {
    private static final String TAG = "HistoryFragment";
    private static final String ARGUMENT_USER_ID = "ARG_USER_ID";

    private NetworkFragment mNetworkFragment;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;

    private ArrayList<PurchaseModel> mPurchaseList;
    private PurchaseAdapter mPurchaseAdapter;

    private int mUserId;

    private SharedPreferences mSharedPrefs;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static HistoryFragment newInstance(int userId) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENT_USER_ID, userId);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void setLoading(boolean flag) {
        if (flag) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private SparseArray<String> parseProducts(final JSONObject products) {
        SparseArray<String> productList = new SparseArray<>();
        Iterator<String> iter = products.keys();

        while (iter.hasNext()) {
            String key = iter.next();
            try {
                JSONObject obj = products.getJSONObject(key);

                String name = obj.getString("name");
                int id = obj.getInt("id");

                productList.put(id, name);

            } catch (JSONException ignored) {
                // Just don't add it to the list
            }
        }

        // The app introduces a few 'fake' products. Add those.
        productList.put(ProductFragment.PRODUCT_DEPOSIT_ID, getString(R.string.product_deposit));
        productList.put(ProductFragment.PRODUCT_UNKNOWN_ID, getString(R.string.product_unknown));

        return productList;
    }

    private void createAdapter() {
        final DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        mPurchaseList = helper.getPurchases(mUserId);

        final PurchaseAdapter.PurchaseViewHolderListener listener =
                new PurchaseAdapter.PurchaseViewHolderListener() {
                    @Override
                    public void onItemClick(View view, int index) {
                        Log.d(TAG, "Clicked: " + mPurchaseList.get(index).getId());
//                helper.deletePurchase(mPurchaseList.get(index).getId());
                    }
                };

        mNetworkFragment.getProducts(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SparseArray<String> products = parseProducts(response);

                mPurchaseAdapter = new PurchaseAdapter(getContext(), products, listener);
                mPurchaseAdapter.add(mPurchaseList);

                mRecyclerView.setAdapter(mPurchaseAdapter);
                setLoading(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // We didn't get a proper response, so we just show the product ids instead
                mPurchaseAdapter = new PurchaseAdapter(getContext(), listener);
                mPurchaseAdapter.add(mPurchaseList);

                mRecyclerView.setAdapter(mPurchaseAdapter);
                setLoading(false);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "on create called");

        Bundle bundle = getArguments();

        if (bundle == null) {
            throw new RuntimeException("PurchaseFragment must be called with bundle");
        }

        mUserId = bundle.getInt(ARGUMENT_USER_ID, -1);

        if (mUserId == -1) {
            throw new RuntimeException("PurchaseFragment must be called with correct bundle.");
        }

        setHasOptionsMenu(true);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.productList);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (mPurchaseAdapter != null) {
            // We are restoring from backstack, set the adapter
            mRecyclerView.setAdapter(mPurchaseAdapter);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // We already checked that our context is of type NetworkFragmentGetter in onAttach,
        // so we can safely get the network fragment..
        mNetworkFragment = ((NetworkFragmentGetter) getContext()).getNetworkFragment();

        Log.d(TAG, "Onstart product fragment");
        createAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof NetworkFragmentGetter)) {
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
        inflater.inflate(R.menu.menu_history, menu);

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
                if (newText == null || mPurchaseAdapter == null) {
                    return false;
                }

                final List<PurchaseModel> filtered = filter(newText);

                mPurchaseAdapter.replaceAll(filtered);
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
            case R.id.action_products:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<PurchaseModel> filter(String query) {
        SparseArray<String> productMap = mPurchaseAdapter.getProductMap();

        if (productMap == null) {
            return mPurchaseList;
        }

        final String lowerCaseQuery = query.toLowerCase();

        final List<PurchaseModel> filteredModelList = new ArrayList<>();
        for (PurchaseModel product : mPurchaseList) {
            final int id = product.getProductId();
            final String name = productMap.get(id).toLowerCase();

            if (name.contains(lowerCaseQuery)) {
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
