package development.calebtoi.test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

// TODO: Link to firebase database and authentication, design a way to save walks

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ArrayList<Location> currentRoute = new ArrayList<>();
    private ArrayList<Marker> pointsOfInterests = new ArrayList<>();
    private ArrayList<HikingRoute> hikingRoutes = new ArrayList<>();

    private LocationManager manager;
    private Location mLocation;
    private Criteria mCriteria;

    private boolean paused = false;


    private long UPDATE_INTERVAL = 100000;
    private long FASTEST_INTERVAL = 100000;

    private static final int EDIT_REQUEST = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private TextView distanceInMetersTextView;
    private float distanceInMetersFloat = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(checkLocationPermission()){
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } else {
            // TODO: add handler
        }

        // Initialise XML objects
        final Button markLocationButton = findViewById(R.id.Mark_Location);
        final Button startLocationUpdatesButton = findViewById(R.id.Start_Pause_Route);
        final Button saveRouteButton = findViewById(R.id.Save_Route_Button);
        distanceInMetersTextView = findViewById(R.id.Distance_Text);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    onLocationChanged(locationResult.getLastLocation());
                    updateCameraLocation(locationResult.getLastLocation());
                }
            };
        };

        markLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markLocation();
            }
        });

        startLocationUpdatesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!paused){
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), "Now tracking for your route", Toast.LENGTH_SHORT);
                    toast.show();
                    startLocationUpdates();
                    paused = true;
                }else{
                    Toast toast;
                    toast = Toast.makeText(getApplicationContext(), "Paused", Toast.LENGTH_SHORT);
                    toast.show();
                    stopLocationUpdates();
                    paused = false;
                }
            }
        });

        // TODO: save route
        saveRouteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                hikingRoutes.add(new HikingRoute("temp name",
                        currentRoute.toArray(new Location[currentRoute.size()]),
                        pointsOfInterests.toArray(new Marker[pointsOfInterests.size()])));
                mMap.clear();
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
        distanceInMetersTextView.setText(Float.toString(distanceInMetersFloat));
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //new Google API SDK v11 uses getFusedLocationProviderClient(this)
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }else{
            // TODO: add something to handle when a user hasn't approved permissions
        }
    }

    public void onLocationChanged(Location location) {
        //New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        //LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        currentRoute.add(location);

        msg = "Array size: "+ currentRoute.size();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if (currentRoute.size() >= 2){
            drawRoute();
        }
    }

    public void drawRoute(){
        //create polylines between gps locations
        Location previousLocation = currentRoute.get(currentRoute.size() - 2);
        Location currentLocation = currentRoute.get(currentRoute.size() - 1);

        LatLng temp1 = new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude());
        LatLng temp2 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        mMap.addPolyline(new PolylineOptions()
            .add(temp1, temp2)
            .width(5)
            .color(Color.RED));

        calculateDistance(previousLocation, currentLocation);
    }

    public void markLocation() {
        //Get last known recent location using new Google Play Sevices SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        // Has to wrapped in a permission checker
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            //GPS location can be null if GPS is switched off
                            if (location != null) {
                                updateCameraLocation(location);
                                LatLng poi = new LatLng(location.getLatitude(), location.getLongitude());
                                currentRoute.add(location);

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
        } else {
            // TODO: add something to handle when a user hasn't approved permissions
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (EDIT_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    MarkerOptions markerOptions = data.getParcelableExtra("marker");
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.showInfoWindow();
                    pointsOfInterests.add(marker);
                }
                break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Has to be wrapped in a permission checker
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Move Camera
            manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            mCriteria = new Criteria();
            String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
            mLocation = manager.getLastKnownLocation(bestProvider);

            if(mLocation != null){
                updateCameraLocation(mLocation);
            } else {
                // TODO: handle error
            }

        } else {
            // TODO: add something to handle when a user hasn't approved permissions
        }
    }

    // ACCESS_FINE_LOCATION apparently allows access to coarse location as well
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Opens up a dialog that notifys the user about the permissions the app needs
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO: improve
                new AlertDialog.Builder(this)
                        .setTitle("TITLE")
                        .setMessage("EXPLANATION")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    // Updates camera to follow location on the map
    // TODO: need to find where to implement this effectively
    public void updateCameraLocation(Location location){
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        float zoomLevel = 16.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomLevel));
    }


    // TODO: return user to a new dashboard screen
    // When back is pressed, with return the user to the placeholder logout screen
    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }
}
