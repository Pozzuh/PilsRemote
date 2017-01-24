package nl.svia.pilsremote.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.Preference;

import nl.svia.pilsremote.R;

/**
 * A simple {@link PreferenceFragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class SettingsFragment extends PreferenceFragmentCompat {
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
