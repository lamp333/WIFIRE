package com.example.yuzur.maps;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Chris Ozawa on 11/30/2017.
 */

public class UploadFileService extends JobService {
    private static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.wtf(TAG, (String) params.getExtras().get("input"));
        Toast.makeText(getApplicationContext(), (String)params.getExtras().get("input"), Toast.LENGTH_SHORT).show();
        jobFinished( params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //mJobHandler.removeMessages( 1 );
        return false;
    }
}
