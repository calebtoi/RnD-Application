package development.calebtoi.test.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import development.calebtoi.test.AllJourneysActivity;
import development.calebtoi.test.MapsActivity;
import development.calebtoi.test.MyJourneysActivity;
import development.calebtoi.test.R;

public class MenuFragment extends Fragment{

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_menu, container, false);

        Button logoutButton = view.findViewById(R.id.buttonLogout);
        Button mapsButton = view.findViewById(R.id.buttonNewRoute);
        Button myJourneysButton = view.findViewById(R.id.button_my_journeys);
        Button allJourneysButton = view.findViewById(R.id.button_all_journeys);

        // Logs user out
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
            }
        });

        // Opens map tracking functionality
        mapsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent;
                intent = new Intent(view.getContext(), MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Opens view of all the users saved Routes
        myJourneysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(view.getContext(), MyJourneysActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        allJourneysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(view.getContext(), AllJourneysActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        return view;
    }

}
