package ncl.csc3122.spendguru;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class ResetPassword extends Fragment {

    // Firebase
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    // Views
    private EditText emailInput;
    private Button resetButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, null, false);

        // Set XML components
        emailInput = view.findViewById(R.id.passwordResetInput);
        resetButton = view.findViewById(R.id.resetPasswordButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        return view;
    }

    /**
     * Reset password email is sent to email entered.
     */
    private void resetPassword() {
        if (emailInput.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Enter email to send password reset.", Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.sendPasswordResetEmail(emailInput.getText().toString());
            goToSignIn();
        }
    }

    /**
     * Password reset has been sent and user is taken to sign in page.
     */
    private void goToSignIn() {
        firebaseAuth.signOut();
        Intent intent = new Intent(getActivity(), SignIn.class);
        startActivity(intent);
        getActivity().finish();
    }
}
