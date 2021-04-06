package ncl.csc3122.spendguru;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    // Firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;

    // Views
    private Button signInButton;
    private Button createAccountButton;
    private EditText emailInput;
    private EditText passwordInput;
    private Button resetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            goToApp();
        }

        // Set XML components
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        createAccountButton = findViewById(R.id.createAccountButton);
        signInButton = findViewById(R.id.signInButton);
        resetPassword = findViewById(R.id.resetPassword);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    /**
     * Attempts to sign in with entered email and password. If succeeded, user goes to main
     * activity. Else, the user is given feedback authentication has failed.
     */
    private void signIn() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();
                            goToApp();
                        } else {
                            Toast.makeText(getApplicationContext(), "Sign In failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Attempts to make account with entered email and password. If succeeded, user goes to main
     * activity. Else, the user is given feedback authentication has failed.
     */
    private void createAccount() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();
                            goToApp();
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * User has either made a new account or signed in. User is taken to main activity in app.
     */
    private void goToApp() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Reset password email is sent to email entered. Toast appears, giving feedback to user.
     */
    private void resetPassword() {
        if (emailInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter email to send password reset.", Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.sendPasswordResetEmail(emailInput.getText().toString());
            Toast.makeText(this, "Reset password email has been sent.", Toast.LENGTH_LONG).show();
        }
    }

}