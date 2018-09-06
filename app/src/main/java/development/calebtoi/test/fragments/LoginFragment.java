package development.calebtoi.test.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import development.calebtoi.test.R;

// Placeholder login fragment
public class LoginFragment extends Fragment {

    // Fields used in the logging in sequence
    private EditText emailField;
    private TextInputLayout emailTIL;
    private EditText passwordField;
    private TextInputLayout passwordTIL;
    private Button loginButton;
    private Button signupButton;

    private String email;
    private String password;

    private View view;

    // Used to send data to the main Activity
    OnLoginDataPass dataPasser;

    public interface OnLoginDataPass {
        void OnLoginDataPass(String email, String password);
    }

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
        signupButton = view.findViewById(R.id.buttonSignupLink);

        emailTIL = view.findViewById(R.id.loginEmailTIL);
        passwordTIL = view.findViewById(R.id.loginPasswordTIL);


        // Sends data grabbed from the login fields to the Main Activity
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                password = passwordField.getText().toString();
                email = emailField.getText().toString();

                // Basic empty field checker
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    dataPasser.OnLoginDataPass(email, password);
                } else {
                    Toast.makeText(getContext(), "Fields are empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Takes user to the sign up fragment
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment frag = new SignupFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .replace(R.id.content_test, frag)
                        .commit();
            }
        });

        // Returns View object
        return view;
    }

}
