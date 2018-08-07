package development.calebtoi.test.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;

import development.calebtoi.test.R;

// Placeholder login fragment
public class LoginFragment extends Fragment {

    // Fields used in the loging in sequence
    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private Button signupButton;

    private View view;

    // Used to send data to the main Activity
    OnLoginDataPass dataPasser;

    public interface OnLoginDataPass {
        void OnLoginDataPass(String email, String password);
    }


    // TODO: learn what this does
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            dataPasser = (OnLoginDataPass) context;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_login, container, false);


        // Instantiates different fields
        // Uses view object to locate ID
        emailField = view.findViewById(R.id.loginEmail);
        passwordField = view.findViewById(R.id.loginPassword);
        loginButton = view.findViewById(R.id.buttonLogin);
        signupButton = view.findViewById(R.id.buttonSignup);

        // Sends data grabbed from the login fields to the Main Activity
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // TODO: field null error handler
                dataPasser.OnLoginDataPass(emailField.getText().toString(), passwordField.getText().toString());
            }
        });

        // Takes user to the sign up fragment
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });

        // Returns View object
        return view;
    }

}
