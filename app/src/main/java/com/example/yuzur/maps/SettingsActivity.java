package com.example.yuzur.maps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingsActivity extends AppCompatActivity {
    private final int HOME = 0;
    private final int TAKE_PICTURE = 1;
    private final int SETTINGS = 2;
    private final int ABOUT = 3;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mNavMenuOptions;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .add(R.id.settings_fragment, new SettingsFragment())
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mNavMenuOptions = getResources().getStringArray(R.array.nav_menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navList);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNavMenuOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("asdf", i + "");
                switch (i) {
                    case HOME:
                        goHome();
                        break;
                    case TAKE_PICTURE:
                        startPictureIntent();
                        break;
                    case SETTINGS:
                        mDrawerLayout.closeDrawers();
                        break;
                    case ABOUT:
                        openAbout();
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getTitle().toString());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startPictureIntent() {
        Intent i = new Intent(this, CameraActivity.class);
        /*i.putExtra("longitude", longitude);
        i.putExtra("latitude", latitude);
        i.putExtra("altitude", altitude);
        i.putExtra("accuracy", accuracy);
        i.putExtra("direction", azimuth);*/

        startActivity(i);
    }

    private void openAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    private void goHome(){
        Log.d("asdf", "going home");
        Intent i = new Intent(this, MapsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }
}
