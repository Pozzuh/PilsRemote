package nl.svia.pilsremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import nl.svia.pilsremote.fragments.Backable;
import nl.svia.pilsremote.fragments.LoginFragment;
import nl.svia.pilsremote.fragments.NetworkFragment;
import nl.svia.pilsremote.fragments.ProductFragment;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;

public class MainActivity extends AppCompatActivity implements NetworkFragmentGetter, LoginFragment.OnLoggedInListener {
    public static final String TAG = "MainActivity";

    private FragmentManager mFragmentManager;

    private NetworkFragment mNetworkFragment;
    private Fragment mActiveFragment;

    private SharedPreferences mSharedPrefs;

    private int mUserId;
    private int mPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();

        mNetworkFragment = NetworkFragment.getInstance(mFragmentManager);
        mSharedPrefs = this.getPreferences(Context.MODE_PRIVATE);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            mUserId = mSharedPrefs.getInt(getString(R.string.pref_user_id), -1);
            mPin = mSharedPrefs.getInt(getString(R.string.pref_pin), -1);

            if (mUserId != -1 && mPin != -1) {
                mActiveFragment = ProductFragment.newInstance(mUserId, mPin);
            } else {
                mActiveFragment = LoginFragment.newInstance();
            }

            // Add the fragment to the 'fragment_container' FrameLayout
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, mActiveFragment).commit();
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
        editor.putInt(getString(R.string.pref_user_id), mUserId);
        editor.putInt(getString(R.string.pref_pin), mPin);
        editor.apply();

        mActiveFragment = ProductFragment.newInstance(mUserId, mPin);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mActiveFragment).commit();
//                .add(R.id.fragment_container, mActiveFragment).commit();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed activity");
        if (mActiveFragment instanceof Backable) {
            if (((Backable) mActiveFragment).onBackPressed()) {
                Log.d(TAG, "Back pressed ignored");
                return;
            } else {
                Log.d(TAG, "Back pressed super");
                super.onBackPressed();
            }
        }
    }
}
