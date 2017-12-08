package com.example.yuzur.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        //GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveStartedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private boolean mLocationPermissionGranted = false;
    private boolean setup = true;
    private boolean followUserLocation = true;
    public static final int REQUEST_LOCATION_CODE = 99;
    private static final String TAG = MapsActivity.class.getSimpleName();

    private DrawerManager mDrawerManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    /*Values*/

    double longitude = 0;
    double latitude = 0;
    double altitude = 0;
    double accuracy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mLocationPermissionGranted = checkLocationPermission();
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
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(this);
                        mMap.setOnCameraMoveStartedListener(this);
                    }
                    mLocationPermissionGranted = true;
                }
                // Permission is denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                }
        }
    }

    public boolean checkLocationPermission() {
        // If permission is not granted yet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //If user denied access, request permissions again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            //Else If user has not denied access, request permissions
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        return true;
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
            startPictureIntent();
        }
    }

    private void startPictureIntent() {
        Intent i = new Intent(MapsActivity.this, CameraActivity.class);
        i.putExtra("longitude", longitude);
        i.putExtra("latitude", latitude);
        i.putExtra("altitude", altitude);
        i.putExtra("accuracy", accuracy);

        startActivity(i);
    }

}
