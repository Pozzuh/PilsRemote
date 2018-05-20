package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import javax.net.ssl.TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import nl.svia.pilsremote.R;

/**
 * Implementation of headless Fragment that runs an AsyncTask to fetch data from the network.
 */
public class NetworkFragment extends Fragment {
    public static final String API_KEY = "kelder_bier_app";

    public static final String URL_GET_USERS = "https://pos.svia.nl/api/?action=get_users&asArray";
    public static final String URL_GET_PRODUCTS = "https://pos.svia.nl/api/?action=get_products";
    public static final String URL_GET_BALANCE = "https://pos.svia.nl/api/?action=get_user_balance&pin=%d&user=%d";
    public static final String URL_GET_BUY_PRODUCT = "https://pos.svia.nl/api/?action=buy_products&bijpinnen=0&cart=%s&clientKey=%s&forUser=0&method=list&pincode=%d&user=%d";
    public static final String TAG = "NetworkFragment";
    public static final String REFERER_HEADER = "https://pos.svia.nl/saldo/";

    private RequestQueue mRequestQueue;
    private Cache mCache;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

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
        mCache = mRequestQueue.getCache();
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
                null, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Referer", REFERER_HEADER);
                headers.putAll(super.getHeaders());
                return headers;
            }
        };

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
                }, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Referer", REFERER_HEADER);
                headers.putAll(super.getHeaders());
                return headers;
            }
        };

        // setShouldCache doesn't always work for some reason, so we clear the cache beforehand.
//        mCache.clear();
        stringRequest.setShouldCache(false);

        mRequestQueue.add(stringRequest);

    }

    public void getProducts(Response.Listener<JSONObject> responseListener,
                            Response.ErrorListener errorListener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_GET_PRODUCTS,
                null, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Referer", REFERER_HEADER);
                headers.putAll(super.getHeaders());
                return headers;
            }
        };

        mRequestQueue.add(jsonObjectRequest);
    }

    public void buyProducts(int pin, int user, int productId, int amount,
                            final Response.Listener<Boolean> responseListener,
                            final Response.ErrorListener errorListener) {
        // Cart syntax: (JS(ON) style)
        // { "<ID>" : <amount> }

        HashMap<String, Integer> cart = new HashMap<>();
        cart.put(String.valueOf(productId), amount);
        JSONObject obj = new JSONObject(cart);

        String url = String.format(URL_GET_BUY_PRODUCT, obj.toString(), API_KEY, pin, user);

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
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Referer", REFERER_HEADER);
                headers.putAll(super.getHeaders());
                return headers;
            }
        };

        stringRequest.setShouldCache(false);
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
        double balance;
        JSONObject products;

        BalanceProductPair(double b, JSONObject p) {
            balance = b;
            products = p;
        }
    }
}
