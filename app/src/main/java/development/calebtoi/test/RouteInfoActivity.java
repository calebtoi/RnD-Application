package development.calebtoi.test;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
public class RouteInfoActivity extends AppCompatActivity {

    private EditText routeNameField;
    private EditText routeDescField;
    private RatingBar difficultyBar;
    private float difficulty;

    private String routeName;
    private String routeDesc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_info);

        final Button saveButton = findViewById(R.id.routeSave);
        final Button cancelButton = findViewById(R.id.routeCancel);

        routeNameField = findViewById(R.id.routeName);
        routeDescField = findViewById(R.id.routeDesc);
        difficultyBar = findViewById(R.id.routeDifficulty);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routeName = routeNameField.getText().toString();
                routeDesc = routeDescField.getText().toString();
                difficulty = difficultyBar.getRating();

                Bundle extras = new Bundle();
                Intent resultIntent = new Intent();

                // NAME
                extras.putString("name", routeName);
                // DESCRIPTION
                extras.putString("description", routeDesc);
                // RATING
                extras.putFloat("difficulty", difficulty);

                resultIntent.putExtras(extras);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
