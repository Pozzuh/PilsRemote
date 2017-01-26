package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Implementation of headless Fragment that runs an AsyncTask to fetch data from the network.
 */
public class NetworkFragment extends Fragment {
    public static final String URL_GET_USERS = "http://dev.automatis.nl/pos/api/?action=get_users&asArray";
    public static final String URL_GET_PRODUCTS = "http://dev.automatis.nl/pos/api/?action=get_products";
    public static final String URL_GET_BALANCE = "http://dev.automatis.nl/pos/api/?action=get_user_balance&pin=%d&user=%d";
    public static final String URL_GET_BUY_PRODUCT = "http://dev.automatis.nl/pos/api/?action=buy_products&bijpinnen=0&cart=%s&clientKey=kelder_bier_app&forUser=0&method=list&pincode=%d&user=%d";
    public static final String TAG = "NetworkFragment";

    private RequestQueue mRequestQueue;

    // Helper variables for getBalanceAndProducts
    private Double mBalance = null;
    private JSONObject mProducts = null;
    private boolean mBalanceAndProductsErrorCalled = false;
    private boolean mBalanceAndProductsSuccessCalled = false;

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getInstance(FragmentManager fragmentManager) {

        // Recover NetworkFragment in case we are re-creating the Activity due to a config change.
        // This is necessary because NetworkFragment might have a task that began running before
        // the config change occurred and has not finished yet.
        // The NetworkFragment is recoverable because it calls setRetainInstance(true).
        NetworkFragment networkFragment = (NetworkFragment) fragmentManager
                .findFragmentByTag(NetworkFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();

            fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        }
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this Fragment across configuration changes in the host Activity.
        setRetainInstance(true);

        mRequestQueue = Volley.newRequestQueue(this.getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity to avoid memory leak.
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        // TODO: is this stop call correct?
        mRequestQueue.stop();
        super.onDestroy();
    }

    public void getUsers(Response.Listener<JSONArray> responseListener,
                         Response.ErrorListener errorListener) {
        // Request a string response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_USERS,
                null, responseListener, errorListener);

        mRequestQueue.add(jsonArrayRequest);

    }

    public void getBalance(int pin, int user, final Response.Listener<Double> responseListener,
                           final Response.ErrorListener errorListener) {
        // Request a string response from the provided URL.
        String url = String.format(URL_GET_BALANCE, pin, user);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.isEmpty()) {
                            errorListener.onErrorResponse(new VolleyError("Balance is empty."));
                        } else {
                            responseListener.onResponse(Double.parseDouble(response));
                        }
                    }

                }, errorListener);

        mRequestQueue.add(stringRequest);

    }

    public void getProducts(Response.Listener<JSONObject> responseListener,
                            Response.ErrorListener errorListener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_GET_PRODUCTS,
                null, responseListener, errorListener);

        mRequestQueue.add(jsonObjectRequest);
    }

    public void buyProducts(int pin, int user, int productId, int amount,
                            final Response.Listener<Boolean> responseListener,
                            final Response.ErrorListener errorListener) {
//        String url = String.format(URL_GET_BALANCE, pin, user);

        // Cart syntax:
        // { "<ID>" : <amount> }

        HashMap<String, Integer> cart = new HashMap<>();
        cart.put(String.valueOf(productId), amount);
        JSONObject obj = new JSONObject(cart);

        String url = String.format(URL_GET_BUY_PRODUCT, obj.toString(), pin, user);

        Log.d(TAG, "cart:" + obj.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Buy response: " + response);
                        responseListener.onResponse(true);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Buy error: " + error.toString());
                errorListener.onErrorResponse(error);
            }
        });

        mRequestQueue.add(stringRequest);
    }

    public void getBalanceAndProducts(int pin, int user,
                                      final Response.Listener<BalanceProductPair> responseListener,
                                      final Response.ErrorListener errorListener) {

        getBalance(pin, user, new Response.Listener<Double>() {
            @Override
            public void onResponse(final Double balance) {
                getProducts(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject products) {
                        BalanceProductPair pair = new BalanceProductPair(balance, products);
                        responseListener.onResponse(pair);
                    }
                }, errorListener);
            }
        }, errorListener);
    }

    static class BalanceProductPair {
        public double balance;
        public JSONObject products;

        public BalanceProductPair(double b, JSONObject p) {
            balance = b;
            products = p;
        }
    }
}