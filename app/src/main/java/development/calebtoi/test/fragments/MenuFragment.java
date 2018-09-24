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
;
import com.google.firebase.auth.FirebaseAuth;

import development.calebtoi.test.MapsActivity;
import development.calebtoi.test.R;

public class MenuFragment extends Fragment{

    private View view;

    private Button logoutButton;
    private Button mapsButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_menu, container, false);

        logoutButton = view.findViewById(R.id.buttonLogout);
        mapsButton = view.findViewById(R.id.buttonMaps);

        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent;
                intent = new Intent(view.getContext(), MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        return view;
    }

}
