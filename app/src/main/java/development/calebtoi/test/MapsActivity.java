package development.calebtoi.test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import development.calebtoi.test.datamodels.HikingRoute;
import development.calebtoi.test.datamodels.LocationModel;
import development.calebtoi.test.datamodels.POIImage;
import development.calebtoi.test.datamodels.POIModel;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

// TODO: Link to FireBase database and authentication
// TODO: Design a way to save walks

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
    private Bitmap mapBitmap;

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
    StorageReference poiStorage;
    StorageReference mapStorage;

    private String hikingRouteKey = "hikingKey";
    private String userID = "user";

    private long UPDATE_INTERVAL = 10000;
    private long FASTEST_INTERVAL = 10000;

    private static final int EDIT_REQUEST = 54321;
    private static final int ROUTE_INFO_REQUEST = 12345;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_INTERNET = 999;

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
        poiStorage = FirebaseStorage.getInstance().getReference().child("poi_images");
        mapStorage = FirebaseStorage.getInstance().getReference().child("map_images");

        // Retrieves User ID from FireBase
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null) {
            userID = mUser.getUid();
        }

        if(checkLocationPermission()){
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if(mapFragment != null){
                mapFragment.getMapAsync(this);
            }
        }

        checkInternetPermission();

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
                    // If the user want to end their route for any reason
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Dialog Box that prompts users to choose what the want to do with the current route
                        AlertDialog.Builder saveDialog = new AlertDialog.Builder(MapsActivity.this);
                        saveDialog.setTitle("Route Options");
                        saveDialog.setMessage("What do you want to do with your current Route?");
                        saveDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            // If the User wants to save the route - opens activity where user can add information about the route
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent;
                                intent = new Intent(MapsActivity.this, RouteInfoActivity.class);
                                MapsActivity.this.startActivityForResult(intent, ROUTE_INFO_REQUEST);

                                startLocationUpdatesButton.setImageDrawable(drawableStart);
                                trackingStatusText.setText(null);
                                trackingStatusContainer.setBackground(null);
                            }
                        });

                        // If the User wants to clear their current route
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
                        // Cancels the dialog
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

                // If the user does not want to end their route
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

    @Override
    protected void onResume() {
        super.onResume();

        String id = getIntent().getStringExtra("routeID");

        if(id != null) {
            createRoute(id);
        }


    }

    // Method used to upload images to FireBase
    private void uploadPOIImage(POIImage poiI) {
        StorageReference imageRef = poiStorage.child(poiI.getPoiID());
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

    // Method used to create and upload an image of a Route
    public void createMapImage(final String routekey) {
        if(mMap != null) {
            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    mapBitmap = bitmap;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    mMap.clear();

                    if(data != null) {
                        StorageReference imageRef = mapStorage.child(routekey);
                        imageRef.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(getApplicationContext(), "File Uploading..Please wait! ",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            }
                        });

                    }
                }
            });
        }

    }


    private void stopLocationUpdates(){
        getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
    }

    public void calculateDistance(Location loc1, Location loc2){
        float distance = loc1.distanceTo(loc2);
        distanceInMetersFloat = distance + distanceInMetersFloat;
        String distanceString = String.format(Locale.ENGLISH, "%.2f",distanceInMetersFloat);
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
        }
        // TODO: add something to handle when a user hasn't approved permissions
    }

    public void onLocationChanged(Location location) {
        //New location has now been determined
        Log.d("LOCATION CHANGED",
                "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()));

        currentRoute.add(location);

        // Location model to save route
        LocationModel tempLocationModel = new LocationModel(location.getLatitude(), location.getLongitude());
        routeSave.add(tempLocationModel);

        Log.d("LOCATION CHANGED", "Array size: "+ currentRoute.size());

        if (currentRoute.size() >= 2){
            drawRoute();
        }
    }

    public void drawRoute(){
        //create PolyLines between gps locations
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
        //Get last known recent location using new Google Play Services SDK (v11+)
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

                    if(extras != null) {
                        // Creates and adds marker to the map
                        MarkerOptions markerOptions = extras.getParcelable("marker");
                        Marker marker = mMap.addMarker(markerOptions);

                        // Gets the image URL from the EditMarker Activity
                        String image_path = extras.getString("imageURI");
                        Uri imgUri = Uri.parse("file://"+image_path);
                        try {
                            Bitmap imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                            marker.setTag(imgBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        LocationModel tempLoc = new LocationModel(marker.getPosition().latitude, marker.getPosition().longitude);
                        POIModel tempPOI = new POIModel(marker.getTitle(), marker.getSnippet(), tempLoc);

                        POIImage tempImage = new POIImage(Uri.parse("file://"+image_path), tempPOI.getPoiID());

                        poiImageSave.add(tempImage);
                        poiSave.add(tempPOI);
                    }
                }
                break;
            }
            case (ROUTE_INFO_REQUEST) : {
                if(resultCode == Activity.RESULT_OK){
                    Bundle extras = data.getExtras();

                    String routeName;
                    String routeDesc;
                    float difficulty;

                    if(extras != null) {
                        routeName = extras.getString("name");
                        routeDesc = extras.getString("description");
                        difficulty = extras.getFloat("difficulty");

                        // Generates ID for current route
                        hikingRouteKey = hikingRef.push().getKey();

                        createMapImage(hikingRouteKey);

                        // Creates a router object
                        if(poiSave != null) {
                            // Creates object with POI
                            hikingRouteSave = new HikingRoute(hikingRouteKey, routeName, routeDesc, difficulty, userID, routeSave, poiSave);
                        } else {
                            // Creates object without POI
                            hikingRouteSave = new HikingRoute(hikingRouteKey, routeName, routeDesc, difficulty, userID, routeSave);
                        }
                        // Saves object to the FireBase database
                        hikingRef.child(hikingRouteKey).setValue(hikingRouteSave);

                        // Loop through all the POI images within list
                        if(poiImageSave != null) {
                            for(int i=0; i < poiImageSave.size(); i++) {
                                if(poiImageSave.get(i) != null){
                                    uploadPOIImage(poiImageSave.get(i));
                                }
                            }
                        }
                    }

                    stopLocationUpdates();
                    poiImageSave.clear();
                    poiSave.clear();
                    currentRoute.clear();
                    routeSave.clear();
                    paused = false;


                }
            }
        }
    }


    // WIP
    public void createRoute(String id) {

        String hikingRouteID = id;

        // Child will parse the HikingRoutes uID
        hikingRef.child(hikingRouteID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Hiking Route
                HikingRoute tempHR = dataSnapshot.getValue(HikingRoute.class);
                List<LocationModel> tempLocation = tempHR.getRoute();
                List<POIModel> tempPOI = new ArrayList<>();

                // Fix for problems with retrieving POI locations
                for(DataSnapshot snapshot : dataSnapshot.child("poi").getChildren()){

                    POIModel poi = snapshot.getValue(POIModel.class);
                    double lat = snapshot.child("location").child("latitude").getValue(double.class);
                    double lng = snapshot.child("location").child("longitude").getValue(double.class);
                    poi.setLocation(new LocationModel(lat, lng));

                    tempPOI.add(poi);
                }


                if(tempHR.getPoi() != null){

                    // Get POI List
                    // Loop through list to get marker objects
                    // Add to map
                    for(int i = 0; i < tempPOI.size(); i++) {

                        POIModel poi = tempPOI.get(i);

                        LatLng poiLatLng = poi.getLocation();

                        final MarkerOptions markerOptions = new MarkerOptions().position(poiLatLng);
                        markerOptions.title(tempPOI.get(i).getTitle());
                        markerOptions.snippet(tempPOI.get(i).getDescription());

                        final Marker marker = mMap.addMarker(markerOptions);
                        marker.hideInfoWindow();

                        StorageReference imageRef = FirebaseStorage.getInstance().getReference("poi_images/"+tempPOI.get(i).getPoiID());
                        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                marker.setTag(bmp);
                            }
                        });
                    }
                }

                // Draws Route out for the user
                for(int i = 0; i < tempLocation.size(); i++){

                    LatLng tempLatLng1;
                    LatLng tempLatLng2;
                    if(i == 0){
                        // First Location of the Route
                        tempLatLng1 = new LatLng(tempLocation.get(0).getLat(), tempLocation.get(0).getLng());
                    } else {
                        // Previous Location of the Route
                        tempLatLng1 = new LatLng(tempLocation.get(i-1).getLat(), tempLocation.get(i-1).getLng());
                    }
                    // Current Location of the Route
                    tempLatLng2 = new LatLng(tempLocation.get(i).getLat(), tempLocation.get(i).getLng());

                    mMap.addPolyline(new PolylineOptions()
                            .add(tempLatLng1, tempLatLng2)
                            .width(10)
                            .color(Color.YELLOW));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



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

            // Shows an hides InfoWindow when pressed
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if(!pressed){
                        pressed = true;
                    } else {
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
            }
            // TODO: handle null error
        }
        // TODO: add something to handle when a user hasn't approved permissions
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

    public boolean checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Opens up a dialog that notifys the user about the permissions the app needs
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                // TODO: improve
                new AlertDialog.Builder(this)
                        .setTitle("TITLE")
                        .setMessage("EXPLANATION")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.INTERNET},
                                        MY_PERMISSIONS_REQUEST_INTERNET);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
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
