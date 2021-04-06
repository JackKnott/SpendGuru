package ncl.csc3122.spendguru;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class Settings extends Fragment {

    // Firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    // Views
    private Button resetPasswordButton;
    private Button logOutButton;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, null, false);

        // Set XML components
        resetPasswordButton = view.findViewById(R.id.resetPassword);
        logOutButton = view.findViewById(R.id.logout);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              goToResetPassword();
          }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        return view;
    }

    /**
     * Go to reset password.
     */
        private void goToResetPassword() {
        ResetPassword resetPasswordFragment = new ResetPassword();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainLayout, resetPasswordFragment);
        transaction.addToBackStack(null).commit();
    }

    /**
     * Log user out.
     */
    private void logOut() {
        firebaseAuth.signOut();
        Intent intent = new Intent(getActivity(), SignIn.class);
        startActivity(intent);
        getActivity().finish();
    }

}
