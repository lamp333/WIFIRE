package com.example.yuzur.maps;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DrawerManager {
    public static final int HOME = 0;
    public static final int GALLERY = 1;
    public static final int SETTINGS = 2;
    public static final int ABOUT = 3;
    public static final int CAMERA = 4;

    private AppCompatActivity mActivity;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mNavMenuOptions;
    private ActionBarDrawerToggle mDrawerToggle;

    public DrawerManager(AppCompatActivity activity){
        mActivity = activity;
        mNavMenuOptions = activity.getResources().getStringArray(R.array.nav_menu);
    }

    public ActionBarDrawerToggle setUpDrawer(DrawerLayout drawerLayout, ListView drawerList, final int callingActivity){
        mDrawerLayout = drawerLayout;
        mDrawerList = drawerList;


        mActivity.getSupportActionBar().setLogo(R.drawable.qualitylogo);
        mActivity.getSupportActionBar().setIcon(R.drawable.qualitylogo);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setHomeButtonEnabled(true);
        mActivity.getSupportActionBar().setDisplayUseLogoEnabled(true);


        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, mNavMenuOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case HOME:
                        if(callingActivity != HOME) {
                            goHome();
                        } else {
                            mDrawerLayout.closeDrawers();
                        }
                        break;
                    case SETTINGS:
                        if(callingActivity != SETTINGS) {
                            openSettings();
                        } else {
                            mDrawerLayout.closeDrawers();
                        }
                        break;
                    case ABOUT:
                        if(callingActivity != ABOUT) {
                            openAbout();
                        } else {
                            mDrawerLayout.closeDrawers();
                        }
                        break;

                    case GALLERY:
                        if(callingActivity != GALLERY) {
                            openGallery();
                        }  else {
                            mDrawerLayout.closeDrawers();
                        }

                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                try {
                    mActivity.getSupportActionBar().setTitle("Navigation");
                    mActivity.getSupportActionBar().setLogo(R.drawable.qualitylogo);

                } catch (NullPointerException e) {
                    //do nothing
                }
                mActivity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                try {
                    mActivity.getSupportActionBar().setTitle(mActivity.getTitle().toString());
                    mActivity.getSupportActionBar().setLogo(R.drawable.qualitylogo);
                } catch (NullPointerException e) {
                    //do nothing
                }
                mActivity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        return mDrawerToggle;
    }

    private void goHome(){
        Intent i = new Intent(mActivity, MapsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mActivity.startActivity(i);
    }

    private void openAbout() {
        Intent i = new Intent(mActivity, AboutActivity.class);
        mActivity.startActivity(i);
    }

    private void openSettings() {
        Intent i = new Intent(mActivity, SettingsActivity.class);
        mActivity.startActivity(i);
    }

    private void openGallery() {
        Intent i = new Intent(mActivity, ImageGalleryActivity.class);
        mActivity.startActivity(i);
    }
}
