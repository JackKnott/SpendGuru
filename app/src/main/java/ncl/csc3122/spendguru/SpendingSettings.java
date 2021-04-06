package ncl.csc3122.spendguru;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class SpendingSettings extends Fragment {
    // Firebase
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Views
    private LinearLayout linearLayout;
    private EditText enterStartDate;
    private EditText enterEndDate;
    private Button clearStartDate;
    private Button clearEndDate;
    private Button submit;
    private Button selectAll;
    private Button deselectAll;

    // Primitive and Other Objects
    private Date startDate;
    private Date endDate;
    private ArrayList<String> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spending_settings, container, false);

        // Set XML components
        linearLayout = view.findViewById(R.id.layoutInScrollview);
        enterStartDate = view.findViewById(R.id.enterStartDate);
        enterEndDate = view.findViewById(R.id.enterEndDate);
        clearStartDate = view.findViewById(R.id.clearStartDate);
        clearEndDate = view.findViewById(R.id.clearEndDate);
        submit = view.findViewById(R.id.submitSettings);
        selectAll = view.findViewById(R.id.selectAllButton);
        deselectAll = view.findViewById(R.id.deselectAllButton);

        addCheckboxes();

        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setCalendarPicker(enterStartDate);
        calendarPicker.setCalendarPicker(enterEndDate);

        clearStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterStartDate.setText("");
            }
        });

        clearEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterEndDate.setText("");
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidated()) {
                    getCategories();
                    goToSpending();
                }
            }
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAll();
            }
        });

        deselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deselectAll();
            }
        });

        return view;
    }

    /**
     * Go through checkboxes and check.
     */
    private void selectAll() {
        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            View nextChild = linearLayout.getChildAt(i);

            if (nextChild instanceof CheckBox) {
                CheckBox check = (CheckBox) nextChild;
                check.setChecked(true);
            }
        }
    }

    /**
     * Go through all checkboxes and uncheck.
     */
    private void deselectAll() {
        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            View nextChild = linearLayout.getChildAt(i);

            if (nextChild instanceof CheckBox) {
                CheckBox check = (CheckBox) nextChild;
                check.setChecked(false);
            }
        }
    }

    /**
     * Checks if the form is validated to suitable criteria.
     * @return if the form is validated
     */
    private boolean isValidated() {
        // If both date fields are entered, they must be chronological
        if (!enterEndDate.getText().toString().equals("")
                && !enterStartDate.getText().toString().equals("")) {
            try {
                startDate = new SimpleDateFormat("dd/MM/yyyy").parse(enterStartDate.getText().toString());
                endDate = new SimpleDateFormat("dd/MM/yyyy").parse(enterEndDate.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
                // TODO make toast
            }

            if (startDate.after(endDate)) {

                // TODO make toast
                return false;
            }
        }
        return true;
    }

    /**
     * Collect categories selected and put in array list to pass through to spending fragment.
     */
    private void getCategories() {
        categories = new ArrayList<>();

        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            View nextChild = linearLayout.getChildAt(i);

            if (nextChild instanceof CheckBox) {
                CheckBox check = (CheckBox) nextChild;
                if (check.isChecked()) {
                    categories.add(check.getText().toString());
                }
            }
        }

        // If no categories selected, make null for spending fragment to select all
        if (categories.isEmpty()) {
            categories = null;
        }
    }

    /**
     * Go to spending with info to fill the chart and costs in.
     */
    private void goToSpending() {
        Spending spending = new Spending(categories, startDate, endDate);
        AppCompatActivity activity = (AppCompatActivity) getView().getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  spending)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Add check boxes to scroll view in form.
     */
    private void addCheckboxes() {
        final DocumentReference document = firestore.collection("categories")
                .document("names");
        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Map<String, Object> map = snapshot.getData();
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                CheckBox checkBox = new CheckBox(getContext());
                                checkBox.setText(entry.getValue().toString());
                                linearLayout.addView(checkBox);
                            }
                        }
                    }
                }
            }
        });
    }
}
