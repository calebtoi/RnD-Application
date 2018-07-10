package development.calebtoi.test.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;

import development.calebtoi.test.R;

// Placeholder login fragment
public class LoginFragment extends Fragment {

    private Button loginButton;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Creates View object to return for the function
        view = inflater.inflate(R.layout.fragment_login, container, false);

        // Instantiates loginButton from Login Fragment
        // Uses view object to locate button ID
        loginButton = view.findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                // Blank Fragment object
                Fragment current = null;

                // Logic for the Listener
                if(v == loginButton){
                    Toast.makeText(getContext(), "Logged in", Toast.LENGTH_SHORT).show();
                    // Sets blank object as a GPS Fragment
                    current = new GPSFragment();
                }

                // Changes displayed fragment to the new 'current' fragment object
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_test, current)
                        .addToBackStack(null)
                        .commit();
            }
        });





        // Returns View object
        return view;


    }
}
