package com.example.yuzur.maps;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private DrawerManager mDrawerManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private JobScheduler mJobScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .add(R.id.settings_fragment, new SettingsFragment())
                .commit();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        mDrawerManager = new DrawerManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerToggle = mDrawerManager.setUpDrawer(mDrawerLayout, mDrawerList, DrawerManager.SETTINGS);
        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("wi_fi_only")) {
            boolean wifi = sharedPreferences.getBoolean("wi_fi_only", false);
            List<JobInfo> activeJobs = mJobScheduler.getAllPendingJobs();
            ComponentName serviceComponent = new ComponentName( getPackageName(), UploadFileService.class.getName() );
            int networkType = 0;
            if(wifi) {
                networkType = JobInfo.NETWORK_TYPE_UNMETERED;
            }

            else {
                networkType = JobInfo.NETWORK_TYPE_ANY;
            }

            for(int i = 0; i < activeJobs.size(); i++)
            {
                JobInfo current = activeJobs.get(i);
                JobInfo.Builder builder = new JobInfo.Builder( current.getId(), serviceComponent)
                        .setRequiredNetworkType(networkType)
                        .setExtras(current.getExtras())
                        .setPersisted(true);
                mJobScheduler.schedule(builder.build());
            }
        }

        else if(s.equals("photo_delete")) {
            boolean delete = sharedPreferences.getBoolean("photo_delete", false);
            boolean wifi = sharedPreferences.getBoolean("wi_fi_only", false);
            List<JobInfo> activeJobs = mJobScheduler.getAllPendingJobs();
            ComponentName serviceComponent = new ComponentName( getPackageName(), UploadFileService.class.getName() );
            int networkType = 0;
            if(wifi) {
                networkType = JobInfo.NETWORK_TYPE_UNMETERED;
            }

            else {
                networkType = JobInfo.NETWORK_TYPE_ANY;
            }
            if(delete)
            {
                for(int i = 0; i < activeJobs.size(); i++)
                {
                    JobInfo current = activeJobs.get(i);
                    PersistableBundle bundle = current.getExtras();
                    bundle.putString("delete", "true");
                    JobInfo.Builder builder = new JobInfo.Builder( current.getId(), serviceComponent)
                            .setRequiredNetworkType(networkType)
                            .setExtras(bundle)
                            .setPersisted(true);
                    mJobScheduler.schedule(builder.build());
                }
                SharedPreferences uploads =  getSharedPreferences("Uploads", MODE_PRIVATE);
                Map<String,?> keys = uploads.getAll();

                for(Map.Entry<String,?> entry : keys.entrySet()){
                    if(entry.getValue().toString() == "true"){
                        File image = new File(entry.getKey());
                        image.delete();
                        uploads.edit().remove(entry.getKey()).commit();
                    }
                }

            }

            else
            {
                for(int i = 0; i < activeJobs.size(); i++)
                {
                    JobInfo current = activeJobs.get(i);
                    PersistableBundle bundle = current.getExtras();
                    bundle.putString("delete", "false");
                    JobInfo.Builder builder = new JobInfo.Builder( current.getId(), serviceComponent)
                            .setRequiredNetworkType(networkType)
                            .setExtras(bundle)
                            .setPersisted(true);
                    mJobScheduler.schedule(builder.build());
                }

            }

        }
    }


}
