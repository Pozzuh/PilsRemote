package nl.svia.pilsremote.fragments;

import android.content.Context;
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
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.adapters.UserAdapter;
import nl.svia.pilsremote.misc.Backable;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;
import nl.svia.pilsremote.misc.UserModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLoggedInListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements Backable {
    public static final String TAG = "LoginFragment";

    private OnLoggedInListener mListener;
    private NetworkFragment mNetworkFragment;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private SearchView mSearchView;

    private UserAdapter mUserAdapter;

    private List<UserModel> mUserList;

    // For logging in
    private int mUserId;
    private int mPin;
    private EditText mEditText = null;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
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

    private void updateUsers() {
        setLoading(true, null);

        mNetworkFragment.getUsers(new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mUserList = parseUsers(response);

                if (mUserAdapter == null) {
                    createAdapter();
                } else {
                    mUserAdapter.add(mUserList);
                }

                mRecyclerView.scrollToPosition(0);
                setLoading(false, null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error getting users: " + error.toString());
                setLoading(false, getContext().getString(R.string.get_users_error));
            }
        });
    }

    private List<UserModel> parseUsers(final JSONArray users) {
        List<UserModel> userList = new ArrayList<>();

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject obj = users.getJSONObject(i);
                int id = obj.getInt("id");
                String name = obj.getString("name");

                userList.add(new UserModel(id, name));
            } catch (JSONException ignored) {
                // Just don't add the invalid user to the list
            }
        }

        return userList;
    }

    private void createAdapter() {
        mUserAdapter = new UserAdapter(getContext(), new UserAdapter.UserViewHolderListener() {
            @Override
            public void onItemClick(View view, int i) {
                UserModel user = mUserAdapter.getItem(i);
                Log.d(TAG, "Pressed: " + user.getName() + ", " + user.getId());
                mUserAdapter.replaceOne(user);
            }
        }, new UserAdapter.FooterViewHolderListener() {
            @Override
            public void onSubmitClick(int userId, int pin, EditText editText) {
                setLoading(true, null);

                mUserId = userId;
                mPin = pin;
                mEditText = editText;

                mNetworkFragment.getBalance(pin, userId, new Response.Listener<Double>() {
                    @Override
                    public void onResponse(Double balance) {
                        Log.d(TAG, "Succesful login " + balance);

                        // Reset searchView, so the next fragment doesn't have to deal with it.
                        mSearchView.setQuery(null, false);
                        mSearchView.setIconified(true);
                        setLoading(false, null);
                        ((OnLoggedInListener) getActivity()).onLoggedIn(mUserId, mPin);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error login ");
                        setLoading(false, null);
                        mEditText.setError(getString(R.string.invalid_pincode));
                    }
                });
            }
        });

        mUserAdapter.add(mUserList);
        mRecyclerView.setAdapter(mUserAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        updateUsers();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnLoggedInListener) {
            mListener = (OnLoggedInListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoggedInListener");
        }

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
        mListener = null;
        mNetworkFragment = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_login, menu);

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
                if (newText == null || mUserAdapter == null) {
                    return false;
                }

                final List<UserModel> filtered = filter(newText);
                mUserAdapter.replaceAll(filtered);
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
                updateUsers();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<UserModel> filter(String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<UserModel> filteredModelList = new ArrayList<>();
        for (UserModel user : mUserList) {
            final String text = user.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(user);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onBackPressed() {
        Log.d(TAG, "Back pressed fragment");

        if (mUserAdapter.footerVisible()) {
            mUserAdapter.replaceAll(mUserList);
            mSearchView.setQuery(null, false);
            mSearchView.setIconified(true);

            if (mEditText != null) {
                mEditText.setError(null);
            }

            return true;
        }

        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoggedInListener {
        void onLoggedIn(int userId, int pin);
    }
}
