package development.calebtoi.test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import development.calebtoi.test.datamodels.HikingRoute;
import development.calebtoi.test.datamodels.LocationModel;
import development.calebtoi.test.datamodels.POIImage;
import development.calebtoi.test.datamodels.POIModel;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

// TODO: Link to firebase database and authentication, design a way to save walks

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ArrayList<Location> currentRoute = new ArrayList<>();

    // Saving Hiking Route
    private List<LocationModel> routeSave = new ArrayList<>();
    private List<POIModel> poiSave = new ArrayList<>();
    private List<POIImage> poiImageSave = new ArrayList<>();
    private HikingRoute hikingRouteSave;

    private LocationManager manager;
    private Location mLocation;
    private Criteria mCriteria;

    private boolean paused = false;
    private boolean pressed = false;

    // XML Variables
    private String stringPaused;
    private String stringTracking;
    private TextView trackingStatusText;
    private LinearLayout trackingStatusContainer;
    private Drawable drawableStart;
    private Drawable drawablePause;
    private Drawable drawableResume;

    // Firebase Database
    FirebaseDatabase database;
    DatabaseReference hikingRef;
    StorageReference mStorage;

    private String hikingRouteKey = "hikingKey";
    private String userID = "user";

    private long UPDATE_INTERVAL = 10000;
    private long FASTEST_INTERVAL = 10000;

    private static final int EDIT_REQUEST = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private TextView distanceInMetersTextView;
    private float distanceInMetersFloat = 0.0f;

    private final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Used for Button logic
        paused = false;

        // FireBase Database Variables
        database = FirebaseDatabase.getInstance();
        hikingRef = database.getReference().child("HikingRoutes");
        mStorage = FirebaseStorage.getInstance().getReference().child("POI_Images");

        // Retrieves User ID from FireBase
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(checkLocationPermission()){
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if(mapFragment != null){
                mapFragment.getMapAsync(this);
            }
        }

        // Initialise XML variables
        final ImageButton markLocationButton = findViewById(R.id.markButton);
        final ImageButton startLocationUpdatesButton = findViewById(R.id.startButton);
        final ImageButton saveRouteButton = findViewById(R.id.stopButton);
        distanceInMetersTextView = findViewById(R.id.Distance_Text);
        stringPaused = getResources().getString(R.string.paused);
        stringTracking = getResources().getString(R.string.tracking);
        trackingStatusText = findViewById(R.id.trackingStatusText);
        trackingStatusContainer = findViewById(R.id.trackingStatusCont);
        drawableStart = getResources().getDrawable(R.drawable.round_fiber_manual_record_24);
        drawablePause = getResources().getDrawable(R.drawable.round_pause_24);
        drawableResume = getResources().getDrawable(R.drawable.round_fiber_manual_record_24);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    onLocationChanged(locationResult.getLastLocation());
                    updateCameraLocation(locationResult.getLastLocation());
                }
            }
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

                if(paused){
                    // Pauses location updates
                    Toast.makeText(getApplicationContext(), "Route tracking is paused!", Toast.LENGTH_LONG).show();
                    stopLocationUpdates();
                    startLocationUpdatesButton.setImageDrawable(drawableResume);
                    trackingStatusText.setText(stringPaused);
                    trackingStatusContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    paused = false;
                } else {
                    // Starts and Resumes location updates
                    Toast.makeText(getApplicationContext(), "Now tracking your route!", Toast.LENGTH_LONG).show();
                    startLocationUpdates();
                    startLocationUpdatesButton.setImageDrawable(drawablePause);
                    trackingStatusText.setText(stringTracking);
                    trackingStatusContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    paused = true;
                }

            }
        });

        saveRouteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                AlertDialog.Builder endDialog = new AlertDialog.Builder(MapsActivity.this);
                endDialog.setTitle("End Route");
                endDialog.setMessage("Are you sure you want to end your route?");

                endDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    // On Yes click
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // Dialog Box that prompts users to choose what the want to do with the current route
                        AlertDialog.Builder saveDialog = new AlertDialog.Builder(MapsActivity.this);
                        saveDialog.setTitle("Route Options");
                        saveDialog.setMessage("What do you want to do with your current Route?");
                        saveDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            // On Save click
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // Saves Route
                                Toast.makeText(MapsActivity.this,"Saving Route...", Toast.LENGTH_LONG).show();
                                // TODO: Add menu for so that users can add more information about the Route
                                // Generates ID for current route
                                hikingRouteKey = hikingRef.push().getKey();
                                // Creates a router object
                                hikingRouteSave = new HikingRoute("test", userID, routeSave, poiSave);
                                // Saves object to the Firebase database
                                hikingRef.child(hikingRouteKey).setValue(hikingRouteSave);

                                // Loop through all the POI images within list
                                if(poiImageSave != null) {
                                    for(int i=0; i < poiImageSave.size(); i++) {
                                        if(poiImageSave.get(i) != null){
                                            uploadImage(poiImageSave.get(i));
                                        }

                                    }
                                }

                                // Resets for next Route
                                stopLocationUpdates();
                                mMap.clear();
                                poiImageSave.clear();
                                poiSave.clear();
                                currentRoute.clear();
                                routeSave.clear();
                                paused = false;
                                startLocationUpdatesButton.setImageDrawable(drawableStart);
                                trackingStatusText.setText(null);
                                trackingStatusContainer.setBackground(null);
                            }
                        });

                        // On No click
                        saveDialog.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // Resets for next Route
                                stopLocationUpdates();
                                mMap.clear();
                                poiImageSave.clear();
                                poiSave.clear();
                                currentRoute.clear();
                                routeSave.clear();
                                paused = false;
                                startLocationUpdatesButton.setImageDrawable(drawableStart);
                                trackingStatusText.setText(null);
                                trackingStatusContainer.setBackground(null);
                            }
                        });

                        saveDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int i) {
                                 // Returns User back to their previous route
                                 dialog.dismiss();
                             }
                        });

                        AlertDialog alert = saveDialog.create();
                        alert.show();
                        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_light));
                        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_light));
                        alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_light));
                    }
                });

                // On No click
                endDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancels Dialog
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = endDialog.create();
                alert.show();
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_light));
                alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_light));
                alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_light));
            }
        });

    }


    // Method used to upload images to FireBase
    private void uploadImage(POIImage poiI) {
        StorageReference imageRef = mStorage.child(poiI.getPoiID());
                imageRef.putFile(poiI.getImageUri())
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(getApplicationContext(), "File Uploading..Please wait! ",
                                            Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successful
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                // You can show your progress bar here \\
                            }
                        });


    }


    private void stopLocationUpdates(){
        getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
    }

    public void calculateDistance(Location loc1, Location loc2){
        float distance = loc1.distanceTo(loc2);
        distanceInMetersFloat = distance + distanceInMetersFloat;
        String distanceString = String.format("%.2f",distanceInMetersFloat);
        distanceInMetersTextView.setText(distanceString);
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
        Log.d("LOCATION CHANGED",
                "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()));

        currentRoute.add(location);

        /** Location model to save route **/
        LocationModel tempLocationModel = new LocationModel(location.getLatitude(), location.getLongitude());
        routeSave.add(tempLocationModel);

        Log.d("LOCATION CHANGED", "Array size: "+ currentRoute.size());

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
            .width(10)
            .color(Color.YELLOW));

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
                                LocationModel tempLoc = new LocationModel(location.getLatitude(), location.getLongitude());


                                routeSave.add(tempLoc);
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
                            Log.d(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        } else {
            // TODO: add something to handle when a user hasn't approved permissions
            Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (EDIT_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    // Creates and adds marker to the map
                    MarkerOptions markerOptions = extras.getParcelable("marker");
                    Marker marker = mMap.addMarker(markerOptions);

                    // Gets the image URL from the EditMarker Activity
                    String image_path = extras.getString("imageURI");

                    marker.setTag(image_path);

                    LocationModel tempLoc = new LocationModel(marker.getPosition().latitude, marker.getPosition().longitude);
                    POIModel tempPOI = new POIModel(marker.getTitle(), marker.getSnippet(), tempLoc);

                    POIImage tempImage = new POIImage(Uri.parse("file://"+image_path), tempPOI.getPoiID());

                    poiImageSave.add(tempImage);
                    poiSave.add(tempPOI);

                }
                break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0,40,10,0);

        // Has to be wrapped in a permission checker
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            try{
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.style_json));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            }

            // Move Camera
            manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            mCriteria = new Criteria();
            String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
            mLocation = manager.getLastKnownLocation(bestProvider);


            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
            mMap.setInfoWindowAdapter(customInfoWindow);

            // Shows an hides infowindow when pressed
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if(!pressed){
                        pressed = true;
                    }
                    if (pressed) {
                        marker.hideInfoWindow();
                        pressed = false;
                    }
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    final int dX = getResources().getDimensionPixelSize(R.dimen.map_dx);
                    final int dY = getResources().getDimensionPixelSize(R.dimen.map_dy);
                    final Projection projection = mMap.getProjection();
                    final Point markerPoint = projection.toScreenLocation(
                            marker.getPosition()
                    );
                    markerPoint.offset(dX, dY);
                    final LatLng newLatLng = projection.fromScreenLocation(markerPoint);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng));

                    marker.showInfoWindow();

                    return true;
                }
            });

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
