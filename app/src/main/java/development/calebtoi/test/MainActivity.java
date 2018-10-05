package development.calebtoi.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import development.calebtoi.test.fragments.MenuFragment;
import development.calebtoi.test.fragments.LoginFragment;
import development.calebtoi.test.fragments.SignupFragment;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginDataPass, SignupFragment.OnSignUpDataPass {

    String loginEmail;
    String loginPassword;

    String signupUsername;
    String signupEmail;
    String signupPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String TAG = "1234";


    @Override
    public void OnLoginDataPass(String email, String password){

        loginEmail = email;
        loginPassword = password;

        if(loginEmail != null || loginPassword != null){

            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                Toast.makeText(MainActivity.this, "Logged in!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // If sign in fails
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
        }



    }

    @Override
    public void OnSignUpDataPass(String username, String email, String password){
        signupUsername = username;
        signupEmail = email;
        signupPassword = password;


        if(signupUsername != null && signupEmail != null && signupPassword != null){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Fragment frag = new MenuFragment();
                    FragmentManager manager = getSupportFragmentManager();
                    manager.beginTransaction()
                            .replace(R.id.content_test, frag)
                            .commit();

                } else {
                    // User is not signed in
                    Fragment frag = new LoginFragment();
                    FragmentManager manager = getSupportFragmentManager();
                    manager.beginTransaction()
                            .replace(R.id.content_test, frag)
                            .commit();
                }

            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
