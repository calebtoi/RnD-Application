package development.calebtoi.test;

// TODO: CHANGE TO A FRAGMENT

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EditMarker extends Activity {

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_marker);

        final LatLng latlng = getIntent().getParcelableExtra("location");

        final EditText title = findViewById(R.id.title);

        Button btnCamera = findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });

        Button saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                MarkerOptions marker = new MarkerOptions().position(latlng);
                if (title.getText() != null) {
                    marker.title(title.getText().toString());
                }
                if(bitmap != null){
                    marker.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("marker", marker);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // TODO: NullPointerException error handler
        bitmap = (Bitmap)data.getExtras().get("data");
    }
}
