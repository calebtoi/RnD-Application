package development.calebtoi.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class EditMarker extends Activity {
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_marker);

        final LatLng latlng = getIntent().getParcelableExtra("location");

        final EditText titleField = findViewById(R.id.titleMarker);
        Button saveButton = findViewById(R.id.save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                title = titleField.getText().toString();

                MarkerOptions marker = new MarkerOptions().position(latlng);
                if (title != null) {
                    marker.title(title);
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("marker", marker);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

    }
}
