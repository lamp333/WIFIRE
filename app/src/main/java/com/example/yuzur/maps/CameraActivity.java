
package com.example.yuzur.maps;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class CameraActivity extends AppCompatActivity implements SensorEventListener {
    private Button takePictureButton;
    private ImageView imageView;
    private Uri file;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private JobScheduler mJobScheduler;

    double longitude = 0;
    double latitude = 0;
    double altitude = 0;
    double accuracy = 0;

    private DrawerManager mDrawerManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    /*Values*/
    float[] inR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    double azimuth = 0;
    double pitch = 0;
    double roll = 0;
    boolean unreliable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        setContentView(R.layout.activity_camera);
        Intent i = getIntent();
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
        else {
            takePicture(takePictureButton);
        }

        mDrawerManager = new DrawerManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerToggle = mDrawerManager.setUpDrawer(mDrawerLayout, mDrawerList, DrawerManager.CAMERA);

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

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
                takePicture(takePictureButton);
            } else {
                Toast.makeText(this, "Please enable camera permissions to take a picture.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void takePicture(View view) {
        if(unreliable) {
            Toast.makeText(this, R.string.sensor_warning, Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = getOutputMediaFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            file = FileProvider.getUriForFile(this, "com.example.yuzur.fileprovider", f);
        }
        else{
            file = Uri.fromFile(f);
        }
        Toast.makeText(this, ""+(int) azimuth, Toast.LENGTH_LONG).show();
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

    public String getProperty(String key) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = this.getAssets().open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                rotateImage(file);
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
                    header.setAttribute("UserComment", "direction: " + (int)azimuth);
                    header.saveAttributes();

                    Toast.makeText(this, ""+(int) azimuth, Toast.LENGTH_LONG).show();
                    //Set unique ID for job ID
                    List<JobInfo> activeJobs = mJobScheduler.getAllPendingJobs();
                    int id = 1;
                    if(activeJobs.size() != 0)
                        id = activeJobs.get(activeJobs.size() - 1).getId() + 1;

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    boolean delete = sharedPref.getBoolean("photo_delete", false);
                    boolean wifi = sharedPref.getBoolean("wi_fi_only", false);
                    int networkType = 0;
                    if(wifi) {
                        networkType = JobInfo.NETWORK_TYPE_UNMETERED;
                    }

                    else {
                        networkType = JobInfo.NETWORK_TYPE_ANY;
                    }
                    //Pass in file path as a Persistable bundle to job service
                    PersistableBundle bundle = new PersistableBundle();
                    bundle.putString("input", path);
                    bundle.putString("user", getProperty("user"));
                    bundle.putString("publicKey", getProperty("publicKey"));
                    bundle.putString("privateKey", getProperty("privateKey"));
                    bundle.putString("delete", delete + "");

                    ComponentName serviceComponent = new ComponentName( getPackageName(), UploadFileService.class.getName() );
                    JobInfo.Builder builder = new JobInfo.Builder( id, serviceComponent)
                                    .setRequiredNetworkType(networkType)
                                    .setExtras(bundle)
                                    .setPersisted(true);
                    //Schedule the job
                    Log.wtf(TAG, "wi_fi_only: " + wifi + " delete:" + delete);
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

    private void rotateImage(Uri imageUri) {
        InputStream input = null;
        try {
            input = getContentResolver().openInputStream(imageUri);

            ExifInterface ei;
            if (Build.VERSION.SDK_INT > 23) {
                ei = new ExifInterface(input);
            } else {
                ei = new ExifInterface(imageUri.getPath());
            }

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Bitmap img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            Log.d(TAG, "orientation " + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateImage(file, img, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateImage(file, img, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateImage(file, img, 270);
                    break;
                default:
                    rotateImage(file, img, 0);
                    break;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error: cannot find image uri");
        } catch (IOException e) {
            Log.e(TAG, "Error: IOException");
        }
    }

    private void rotateImage(Uri file, Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        imageView.setImageBitmap(null);
        imageView.setImageBitmap(rotatedImg);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // If the sensor data is unreliable return
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            unreliable = true;
        }

        // Gets the value of the sensor that has been changed
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravity = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomag = sensorEvent.values.clone();
                break;
        }

        // If gravity and geomag have values then find rotation matrix
        if (gravity != null && geomag != null) {

            // checks that the rotation matrix is found
            boolean success = SensorManager.getRotationMatrix(inR, I,
                    gravity, geomag);
            if (success) {
                SensorManager.getOrientation(inR, orientVals);
                azimuth =  Math.toDegrees(orientVals[0]);
                pitch = Math.toDegrees(orientVals[1]);
                roll = Math.toDegrees(orientVals[2]);
                //TextView TV_Warning = (TextView) findViewById((R.id.TV_Warning));
                if(unreliable){
                    //TV_Warning.setText("Sensor: unreliable");
                }
                else{
                    //TV_Warning.setText("Sensor: reliable");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
