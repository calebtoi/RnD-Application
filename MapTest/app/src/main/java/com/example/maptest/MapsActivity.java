package com.example.maptest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ArrayList<Location> coordinates = new ArrayList();
    private ArrayList<MarkerOptions> pointsOfInterests = new ArrayList();


    private long UPDATE_INTERVAL = 10000; //10 sec
    private long FASTEST_INTERVAL = 10000; //10 sec

    private static final int EDIT_REQUEST = 1;

    private TextView distanceInMetersTextView;
    private float distanceInMetersFloat = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                onLocationChanged(locationResult.getLastLocation());
            };
        };
        distanceInMetersTextView = findViewById(R.id.Distance_Text);

        final Button markLocationButton = findViewById(R.id.Mark_Location);
        markLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markLocation();
            }
        });

        final Button startLocationUpdatesButton = findViewById(R.id.Start_Route);
        startLocationUpdatesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), "Now tracking for your route", Toast.LENGTH_SHORT);
                toast.show();
                startLocationUpdates();
            }
        });

        final Button pauseLocationUpdatesButton = findViewById(R.id.Pause_Route);
        pauseLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast;
                toast = Toast.makeText(getApplicationContext(), "Paused", Toast.LENGTH_SHORT);
                toast.show();
                stopLocationUpdates();
            }
        });
    }

    private void stopLocationUpdates(){
        getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
        Toast toast;
        toast = Toast.makeText(getApplicationContext(), "inside stop location update", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void calculateDistance(Location loc1, Location loc2){
        float distance = loc1.distanceTo(loc2);
        distanceInMetersFloat = distance + distanceInMetersFloat;
//        Random r = new Random();
//        int i1 = r.nextInt(100 - 1) + 1;
        distanceInMetersTextView.setText(Float.toString(distanceInMetersFloat));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (EDIT_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    MarkerOptions markerOptions = data.getParcelableExtra("marker");
                    pointsOfInterests.add(markerOptions);
                    mMap.addMarker(markerOptions);
                }
                break;
            }
        }
    }

    //Trigger new location updates at interval
    protected void startLocationUpdates() {

        //create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        //Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //check whether location settings are satisfied
        //https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        //Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    /*
    new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //do work here
                onLocationChanged(locationResult.getLastLocation());
            }
        }
     */

    public void onLocationChanged(Location location) {
        //New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        //LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        coordinates.add(location);

        msg = "Array size: "+ coordinates.size();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if (coordinates.size() >= 2){
            drawRoute();
        }
    }

    public void drawRoute(){
        //create polylines between gps locations
        Location previousLocation = coordinates.get(coordinates.size() - 2);
        Location currentLocation = coordinates.get(coordinates.size() - 1);

        LatLng temp1 = new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude());
        LatLng temp2 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        mMap.addPolyline(new PolylineOptions()
            .add(temp1, temp2)
            .width(5)
            .color(Color.RED));

        calculateDistance(previousLocation, currentLocation);
        //calculateDistance();
    }

    public void markLocation() {
        //Get last known recent location using new Google Play Sevices SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        //Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        //GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                            LatLng poi = new LatLng(location.getLatitude(), location.getLongitude());
                            coordinates.add(location);

                            Intent edit = new Intent(MapsActivity.this, EditMarker.class);
                            edit.putExtra("location", poi);
                            MapsActivity.this.startActivityForResult(edit, EDIT_REQUEST);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapsActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
