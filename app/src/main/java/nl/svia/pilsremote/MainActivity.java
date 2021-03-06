package nl.svia.pilsremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import nl.svia.pilsremote.fragments.HistoryFragment;
import nl.svia.pilsremote.fragments.SettingsFragment;
import nl.svia.pilsremote.misc.Backable;
import nl.svia.pilsremote.fragments.LoginFragment;
import nl.svia.pilsremote.fragments.NetworkFragment;
import nl.svia.pilsremote.fragments.ProductFragment;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;

public class MainActivity extends AppCompatActivity implements NetworkFragmentGetter,
        LoginFragment.OnLoggedInListener, SettingsFragment.SettingsFragmentListener {
    public static final String TAG = "MainActivity";
    private static final String BUNDLE_USER_ID = "BUNDLE_USER_ID";
    private static final String BUNDLE_PIN = "BUNDLE_PIN";

    private FragmentManager mFragmentManager;

    private NetworkFragment mNetworkFragment;
    private ProductFragment mProductFragment;

    private SharedPreferences mSharedPrefs;

    private int mUserId;
    private int mPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mFragmentManager = getSupportFragmentManager();

        mNetworkFragment = NetworkFragment.getInstance(mFragmentManager);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            mUserId = mSharedPrefs.getInt(getString(R.string.key_user_id), -1);
            mPin = mSharedPrefs.getInt(getString(R.string.key_pin), -1);

            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            if (mUserId != -1 && mPin != -1) {
                mProductFragment = ProductFragment.newInstance(mUserId, mPin);
                transaction.add(R.id.fragment_container, mProductFragment);
            } else {
                transaction.add(R.id.fragment_container, LoginFragment.newInstance());
            }

            transaction.commit();
        }
    }

    public NetworkFragment getNetworkFragment() {
        return this.mNetworkFragment;
    }

    @Override
    public void onLoggedIn(int userId, int pin) {
        mUserId = userId;
        mPin = pin;

        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(getString(R.string.key_user_id), mUserId);
        editor.putInt(getString(R.string.key_pin), mPin);
        editor.apply();

        mProductFragment = ProductFragment.newInstance(mUserId, mPin);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mProductFragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                mFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_history:
                mFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HistoryFragment.newInstance(mUserId))
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_products:
                mFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProductFragment.newInstance(mUserId, mPin))
                        .addToBackStack(null)
                        .commit();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mUserId = savedInstanceState.getInt(BUNDLE_USER_ID, -1);
        mPin = savedInstanceState.getInt(BUNDLE_PIN, -1);

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_USER_ID, mUserId);
        outState.putInt(BUNDLE_PIN, mPin);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBeerOnlyChanged(boolean flag) {
        if (flag && mProductFragment != null) {
            mProductFragment.clearList();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed activity");

        Fragment curr = mFragmentManager.findFragmentById(R.id.fragment_container);

        if (curr instanceof Backable) {
            if (((Backable) curr).onBackPressed()) {
                Log.d(TAG, "Back pressed ignored");
                return;
            } else {
                Log.d(TAG, "Back pressed super");
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
