package com.example.yuzur.maps;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Chris Ozawa on 11/16/2017.
 */

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
