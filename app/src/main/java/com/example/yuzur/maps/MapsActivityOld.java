package com.example.yuzur.maps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.OrientationEventListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

/*
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        //GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveStartedListener,
        SensorEventListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Circle accuracyRange;
    private Circle currentPosition;
    private boolean mLocationPermissionGranted = false;
    private boolean setup = true;
    private boolean followUserLocation = true;
    public static final int REQUEST_LOCATION_CODE =  99;
    private static final String TAG = MapsActivity.class.getSimpleName();



    private SensorManager mSensorManager;
    float[] inR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    double azimuth = 0;
    double pitch = 0;
    double roll = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            mLocationPermissionGranted = checkLocationPermission();
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    protected synchronized void buildGoogleApiClient()
    {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission is granted
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(client == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(this);
                        mMap.setOnCameraMoveStartedListener(this);
                    }
                    mLocationPermissionGranted = true;
                }
                // Permission is denied
                else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                }
        }
    }

    public boolean checkLocationPermission(){
        // If permission is not granted yet
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //If user denied access, request permissions again
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            //Else If user has not denied access, request permissions
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnCameraMoveStartedListener(this);
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        }


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(20);
        locationRequest.setFastestInterval(20/2);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(client,locationRequest,this);
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

        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        TextView TV_Location = (TextView) findViewById((R.id.TV_Location));
        TV_Location.setText("Lat: " + (float)((int)(location.getLatitude()*100))/100 + "\n" +
                        "Lon: " + (float)((int)(location.getLongitude()*100))/100 + "\n" +
                        "Altitude: " + (float)((int)(location.getAltitude()*100))/100 + "meters");

        TextView TV_Accuracy = (TextView) findViewById((R.id.TV_Accuracy));
        TV_Accuracy.setText("Radius: " + location.getAccuracy() + " meters");


        if( setup){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            setup = !setup;
        }
        else if (followUserLocation && !setup){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        }

    }


    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Following Current Location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        followUserLocation = true;
        return false;
    }


    public void onCameraMoveStarted(int i) {
        if (i == REASON_GESTURE && followUserLocation){
            Toast.makeText(this, "Location tracking halted", Toast.LENGTH_SHORT).show();
            followUserLocation= false;
        }
    }


    public void onSensorChanged(SensorEvent sensorEvent) {
// If the sensor data is unreliable return
        boolean inaccurate = false;
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            inaccurate = true;
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
                TextView TV_Orientation = (TextView) findViewById((R.id.TV_Orientation));
                if(inaccurate){
                    TV_Orientation.setText("Sensors may currently be inaccurate.\n" +
                            "azimuth: " + (int)azimuth + "\n" +
                            "pitch: " + (int) pitch + "\n" +
                            "roll: " + (int)roll + "\n" );
                }
                else{
                    TV_Orientation.setText("azimuth: " + (int)azimuth + "\n" +
                            "pitch: " + (int) pitch + "\n" +
                            "roll: " + (int)roll + "\n" );
                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



}

*/
