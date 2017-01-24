package nl.svia.pilsremote.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

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
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof SettingsFragmentListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoggedInListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.key_beer_only))) {
            boolean def = getActivity().getResources().getBoolean(R.bool.beer_only_default);
            boolean val = sharedPreferences.getBoolean(key, def);
            ((SettingsFragmentListener) getActivity()).onBeerOnlyChanged(val);
        }
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
                getActivity().startActivity(getActivity().getIntent());

                return true;
            }
        });
    }

    public interface SettingsFragmentListener {
        void onBeerOnlyChanged(boolean beerOnly);
    }
}
