package development.calebtoi.test.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class SignupFragment extends Fragment {

    private EditText signupUsername;
    private EditText signupEmail;
    private EditText signupPassword;
    private EditText signupPasswordConfirm;
    private Button signupButton;
    private Button loginLinkButton;

    private String username;
    private String email;
    private String password;
    private String confirmPassword;

    private View view;

    OnSignUpDataPass dataPasser;

    public interface OnSignUpDataPass {
        void OnSignUpDataPass(String username, String email, String password);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            dataPasser = (SignupFragment.OnSignUpDataPass) context;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Instantiates different fields
        // Uses view object to locate ID
        signupUsername = view.findViewById(R.id.signupUsername);
        signupEmail = view.findViewById(R.id.signupEmail);
        signupPassword = view.findViewById(R.id.signupPassword);
        signupPasswordConfirm = view.findViewById(R.id.signupConfirmPassword);
        signupButton = view.findViewById(R.id.buttonSignup);
        loginLinkButton = view.findViewById(R.id.buttonLoginLink);

        // TODO: Check if fields are empty

        // TODO: Check if password matches


        /*
         * Send information to Activity
         *  Create profile
         *  Log user in
         */
        signupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                // Gather fields
                username = signupUsername.getText().toString();
                email = signupEmail.getText().toString();
                password = signupPassword.getText().toString();
                confirmPassword = signupPasswordConfirm.getText().toString();

                dataPasser.OnSignUpDataPass(username, email, password);
            }
        });

        // Takes user back to login page
        loginLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment frag = new LoginFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .replace(R.id.content_test, frag)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // TODO: back press takes user back to login screen

        return view;
    }
}
