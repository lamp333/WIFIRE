
package com.example.yuzur.maps;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private Button takePictureButton;
    private ImageView imageView;
    private Uri file;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private JobScheduler mJobScheduler;

    double direction = 0;
    double longitude = 0;
    double latitude = 0;
    double altitude = 0;
    double accuracy = 0;

    private DrawerManager mDrawerManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        setContentView(R.layout.activity_camera);
        Intent i = getIntent();
        direction = i.getDoubleExtra("direction", 300.0);
        longitude = i.getDoubleExtra("longitude", 0);
        latitude = i.getDoubleExtra("latitude", 0);
        altitude = i.getDoubleExtra("altitude", 0);
        accuracy = i.getDoubleExtra("accuracy", 0);

        takePictureButton = (Button) findViewById(R.id.button_image);
        imageView = (ImageView) findViewById(R.id.imageview);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECEIVE_BOOT_COMPLETED ,Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        mDrawerManager = new DrawerManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerToggle = mDrawerManager.setUpDrawer(mDrawerLayout, mDrawerList, DrawerManager.CAMERA);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = getOutputMediaFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            file = FileProvider.getUriForFile(this, "com.example.yuzur.fileprovider", f);
        }
        else{
            file = Uri.fromFile(f);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WIFIRE");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("CameraDemo", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpeg");
    }

    private void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(file); //your file uri
        this.sendBroadcast(mediaScanIntent);
    }
    synchronized public static final String convert(double latitude) {
        StringBuilder sb = new StringBuilder(20);
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(file);
                try {
                    String path = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        path = Environment.getExternalStoragePublicDirectory(file.getPath()).getAbsolutePath();
                    }
                    else{
                        path = file.getPath();
                    }
                    //Set Exif Headers in image
                    ExifInterface header = new ExifInterface(path);
                    header.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(latitude));
                    header.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(longitude));
                    header.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, (int)altitude+"/1");
                    header.setAttribute("UserComment", "direction: " + (int)direction);
                    header.saveAttributes();

                    //Set unique ID for job ID
                    List<JobInfo> activeJobs = mJobScheduler.getAllPendingJobs();
                    int id = 1;
                    if(activeJobs.size() != 0)
                        id = activeJobs.get(activeJobs.size() - 1).getId() + 1;

                    //Pass in file path as a Persistable bundle to job service
                    PersistableBundle bundle = new PersistableBundle();
                    bundle.putString("input", path);
                    bundle.putString("context", getApplicationContext().toString());

                    ComponentName serviceComponent = new ComponentName( getPackageName(), UploadFileService.class.getName() );
                    JobInfo.Builder builder = new JobInfo.Builder( id, serviceComponent)
                                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                                    .setExtras(bundle)
                                    .setPersisted(true);
                    //Schedule the job
                    int result = mJobScheduler.schedule(builder.build());
                    if(result <= 0)
                        Log.wtf(TAG, "failed to schedule");

                }
                catch (IOException e){
                    Log.wtf(TAG, "IOException");
                    Log.wtf(TAG, e.getMessage());
                }
                addPhotoToGallery();
            }
        }


    }

}
