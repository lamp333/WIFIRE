package com.example.yuzur.maps;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Chris Ozawa on 11/16/2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
