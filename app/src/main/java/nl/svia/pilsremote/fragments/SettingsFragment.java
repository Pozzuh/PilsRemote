package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.Preference;
import android.util.Log;

import nl.svia.pilsremote.R;

/**
 * A simple {@link PreferenceFragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences mSharedPrefs;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.d("KUT", "asd" + key);
        if (key.equals(getString(R.string.key_beer_only))) {
            Preference p = findPreference(getString(R.string.key_beer_only));
            p.setSummary("yes" + prefs.getBoolean(key, false));
        }
//        boolean bool1 = prefs.getBoolean(key, false);
//        mBool1.setSummary(bool1 ? "Enabled" : "Disabled")
    }

    @Override
    public void onResume() {
        super.onResume();

        mSharedPrefs = getPreferenceManager().getSharedPreferences();

        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, null);

        Preference logoutButton = findPreference(getString(R.string.key_logout));
        logoutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putInt(getString(R.string.key_user_id), -1);
                editor.putInt(getString(R.string.key_pin), -1);
                editor.commit();

                getActivity().finish();

                return true;
            }
        });
    }
}
