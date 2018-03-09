package com.example.yuzur.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        //GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveStartedListener,
        LocationListener,
        SensorEventListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private boolean setup = true;
    private boolean followUserLocation = true;
    public static final int REQUEST_PERMISSIONS_CODE = 99;
    public static final int MILLIS_BETWEEN_CHANGE = 500;

    private boolean allPermissionsGranted = false;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private DrawerManager mDrawerManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private JobScheduler mJobScheduler;
    private Uri file;

    /*Values*/

    double longitude = 0;
    double latitude = 0;
    double altitude = 0;
    double accuracy = 0;

    /*Sensor Values*/
    float[] inR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    double azimuth = 0;
    double pitch = 0;
    double roll = 0;
    boolean unreliable = false;

    long lastTimeStringSet = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerManager = new DrawerManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerToggle = mDrawerManager.setUpDrawer(mDrawerLayout, mDrawerList, DrawerManager.HOME);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );

        if(checkFirstRun()){
            // Build a Welcome dialog
            showInstructionsDialog();
        }

    }

    public boolean checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            // Place your dialog code here to display the dialog

            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
        return isFirstRun;
    }

    private void showInstructionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.welcome)
                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and show it
        Dialog welcome_dialog = builder.create();
        welcome_dialog.setCancelable(false);
        welcome_dialog.setCanceledOnTouchOutside(false);
        welcome_dialog.show();
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    boolean noPermissionsDenied = true;

                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            noPermissionsDenied = false;
                        }
                    }

                    allPermissionsGranted = noPermissionsDenied;

                    //check if location was granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(this);
                        mMap.setOnCameraMoveStartedListener(this);

                    }
                }
                break;
        }
    }

    public void requestPermissions() {
        String[] permissionsNeeded = checkPermissions();
        if(permissionsNeeded.length != 0) {
            ActivityCompat.requestPermissions(this, permissionsNeeded, REQUEST_PERMISSIONS_CODE);
        } else {
            allPermissionsGranted = true;
        }
    }

    public String[] checkPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        // If location permission is not granted yet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // If camera permission is not granted yet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // If storage permission is not granted yet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        return permissionsNeeded.toArray(new String[permissionsNeeded.size()]);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnCameraMoveStartedListener(this);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(20);
        locationRequest.setFastestInterval(20 / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latitude = location.getLatitude();
        longitude =  location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAltitude();

        /*TextView TV_Location = (TextView) findViewById((R.id.TV_Location));

        TV_Location.setText("Lat: " + (float) ((int) (location.getLatitude() * 100)) / 100 + "\n" +
                "Lon: " + (float) ((int) (location.getLongitude() * 100)) / 100 + "\n" +
                "Altitude: " + (float) ((int) (location.getAltitude() * 100)) / 100 + "meters");

        TextView TV_Accuracy = (TextView) findViewById((R.id.TV_Accuracy));
        TV_Accuracy.setText("Radius: " + location.getAccuracy() + " meters"); */

        TextView TV_Location = (TextView) findViewById((R.id.loc_info_txt));
        String longStr = String.format(Locale.US, "Longitude: %1$.3f", longitude);
        String latStr = String.format(Locale.US, "Latitude: %1$.3f", latitude);
        String altitudeStr = String.format(Locale.US, "Altitude: %1$.3f", altitude);
        TV_Location.setText(longStr + "\n" + latStr + "\n" + altitudeStr);

        if (setup) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            setup = !setup;
        } else if (followUserLocation && !setup) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }
        /*
        if(client != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }*/
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Following Current Location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        followUserLocation = true;
        return false;
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == REASON_GESTURE && followUserLocation) {
            Toast.makeText(this, "Location tracking halted", Toast.LENGTH_SHORT).show();
            followUserLocation = false;
        }
    }


    public void onClick(View v) {
        if(v.getId() == R.id.picture_button){
            if(allPermissionsGranted) {
                takePicture();
            } else {
                requestPermissions();
            }
        } else if(v.getId() == R.id.help_button) {
            Log.wtf("sdf", "sdfsadf");
            showInstructionsDialog();
        }
    }

    public void takePicture() {
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
        //Toast.makeText(this, "LOLOLOL", Toast.LENGTH_LONG).show();


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

    private void goToGallery() {
        Intent i = new Intent(MapsActivity.this, ImageGalleryActivity.class);
        startActivity(i);
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

                    //Toast.makeText(this, ""+(int) azimuth, Toast.LENGTH_LONG).show();
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
                    //Log.wtf(TAG, "wi_fi_only: " + wifi + " delete:" + delete);
                    int result = mJobScheduler.schedule(builder.build());
                    if(result <= 0)
                        Log.wtf(TAG, "failed to schedule");

                }
                catch (IOException e){
                    Log.wtf(TAG, "IOException");
                    Log.wtf(TAG, e.getMessage());
                }
                goToGallery();
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

    private Bitmap rotateImage(Uri file, Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
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
                azimuth = Math.toDegrees(orientVals[0]);
                pitch = Math.toDegrees(orientVals[1]);
                roll = Math.toDegrees(orientVals[2]);

                if(System.currentTimeMillis() - lastTimeStringSet > MILLIS_BETWEEN_CHANGE) {
                    TextView TV_Sensor = (TextView) findViewById((R.id.sensor_info_txt));
                    String azimuthStr = String.format(Locale.US, "Azimuth: %1$.1f", azimuth);
                    String pitchStr = String.format(Locale.US, "Pitch: %1$.1f", pitch);
                    String rollStr = String.format(Locale.US, "Roll: %1$.1f", roll);
                    TV_Sensor.setText(azimuthStr + "\n" + pitchStr + "\n" + rollStr);

                    lastTimeStringSet = System.currentTimeMillis();
                }

                if (unreliable) {
                    //TV_Warning.setText("Sensor: unreliable");
                } else {
                    //TV_Warning.setText("Sensor: reliable");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
