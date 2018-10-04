package development.calebtoi.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EditMarker extends Activity {
    private String title;
    private String snippet;
    private String imageURI;

    private String userID;
    private StorageReference mStorage;

    private ImageView cameraImageView;

    private Bitmap bitmap;
    private File photoFile;
    private final static String TAG = "Test";
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 200;
    private static final int RESULT_GALLERY = 500;
    private Uri photoURI;
    private String mCurrentPhotoPath;
    private String imageFileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_marker);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance().getReference();

        if (Build.VERSION.SDK_INT < 23) {
            //Do not need to check the permission
        } else {
            requestPermissions();
        }

        final LatLng latlng = getIntent().getParcelableExtra("location");

        final EditText titleField = findViewById(R.id.titleMarker);
        final EditText snippetField = findViewById(R.id.snippetMarker);
        cameraImageView = findViewById(R.id.imagePOI);
        Button saveButton = findViewById(R.id.save);
        Button cameraButton = findViewById(R.id.buttonCamera);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Bundle extras = new Bundle();

                title = titleField.getText().toString();
                snippet = snippetField.getText().toString();

                MarkerOptions marker = new MarkerOptions().position(latlng);
                if (title != null) {
                    marker.title(title);
                    if(snippet != null) {
                        marker.snippet(snippet);
                    }
                }

                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title" , "description");

                Intent resultIntent = new Intent();

                extras.putParcelable("marker", marker);
                if(photoFile != null) {
                    imageURI = photoFile.toString();
                    extras.putString("imageURI", imageURI);
                }

                resultIntent.putExtras(extras);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

    }

    /* Log tag and shortcut */
    public static void ltag(String message) {
        Log.i(TAG, message);
    }

    /*
        Check permissions for camera and external storage
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            ltag("Permission is granted");
        } else {//request the permissions
            ltag("Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    /*
       use intent to capture the image
   */
    //camera permit
    public void capture() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        /* Ensure that there's a camera activity to handle the intent */

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            /* Create the File where the photo should go */
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ltag("error: "+ex.toString());
            }
            /* Continue only if the File was successfully created */
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "development.calebtoi.test.provider",
                        photoFile);

                ltag("photoURI: "+photoURI.getPath());

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /*
    Create image file by date and time.
    */
    private File createImageFile() throws IOException {
        /* Create an image file name */

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";

        //Album
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //create full size image
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ContentResolver contentResolver = this.getContentResolver();
            try {
                //this is full size image
                Bitmap fullBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoURI);


                cameraImageView.setImageBitmap(fullBitmap);
                cameraImageView.setVisibility(View.VISIBLE);
                saveImage(fullBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
   Save full size image
   */
    private void saveImage(Bitmap bitmap) {

        try {
            String albumName = "Adventure Sharer";
            File albumDir = getAlbumStorageDir(albumName);

            OutputStream imageOut = null;
            File file = new File(albumDir, imageFileName+".jpg");

            imageOut = new FileOutputStream(file);

            //Bitmap -> JPEG with 85% compression rate
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, imageOut);
            imageOut.flush();
            imageOut.close();

            //update gallery so you can view the image in gallery
            updateGallery(albumName, albumDir, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Set the personal album in DCIM
     */
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);
        if (!file.mkdirs()) {
            ltag("Directory not created");
        }
        return file;
    }

    /*
    Add the image and image album into gallery
    */
    private void updateGallery(String albumName, File albumDir, File file) {
        //metadata of new image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, imageFileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, albumName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        ltag("album Directory: "+albumDir.getAbsolutePath());

        File f = new File(albumDir.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        //notify gallery for new image
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        getApplicationContext().sendBroadcast(mediaScanIntent);
    }

}
