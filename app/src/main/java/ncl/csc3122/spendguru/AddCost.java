package ncl.csc3122.spendguru;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddCost extends Fragment {

    // Firebase
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String userUid = firebaseAuth.getCurrentUser().getUid();

    // Views
    private TextView headerView;
    private EditText numberField;
    private EditText dateField;
    private EditText infoField;
    private Button enterButton;
    private Button resetDate;

    // Primitive and Other Objects
    private Category category;
    private Cost cost;
    private String id;

    public AddCost(Category category) {
        this.category = category;
    }

    public AddCost(Cost cost) {
        this.cost = cost;
        this.category = new Category(cost.getCategory());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_cost, null, false);

        // Set XML components
        headerView = view.findViewById(R.id.enterCostHeader);
        numberField = view.findViewById(R.id.enterAmountField);
        dateField = view.findViewById(R.id.enterDate);
        infoField = view.findViewById(R.id.enterInfo);
        resetDate = view.findViewById(R.id.clearDate);
        enterButton = view.findViewById(R.id.enterButton);

        String header = "Enter Cost For " + category.getName();
        headerView.setText(header);

        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setCalendarPicker(dateField);

        resetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValues();
            }
        });

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidated()) {
                    goToAddCost();
                }
            }
        });

        setValues();

        return view;
    }

    /**
     * If cost has been passed through, user will edit cost instead of add one. Set views text to
     * cost info. Otherwise, just set date field to today's date.
     */
    private void setValues() {
        Date today = Calendar.getInstance().getTime();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        dateField.setText(formatter.format(today));

        if(cost != null) {
            dateField.setText(formatter.format(cost.getDate()));
            numberField.setText(Double.toString(cost.getAmount()));
            infoField.setText(cost.getInfo());
        }
    }

    /**
     * Validate the form, number field must not be empty. Info field is not required. Date field
     * can not be blank as it is already set and can only be changed due to calendar picker.
     * @return if the form has met the validation requirements.
     */
    private boolean isValidated() {
        if (numberField.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Amount field is empty.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Add cost to firestore and take user to cost categories fragment.
     */
    private void goToAddCost() {
        addCostToFirestore();

        CostCategories costCategoriesFragment = new CostCategories();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainLayout, costCategoriesFragment);
        transaction.addToBackStack(null).commit();
    }

    /**
     * Add cost to firestore. If date field is empty, add todays date.
     */
    private void addCostToFirestore() {
        Date currentTime = Calendar.getInstance().getTime();
        if (!dateField.getText().toString().isEmpty()) {

            String dateString = dateField.getText().toString();
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            try {
                currentTime = format.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        double amount = Double.parseDouble(numberField.getText().toString());
        String info = infoField.getText().toString();

        // Data to put into Firestore spending
        final Map<String, Object> entryData = new HashMap<>();
        entryData.put("date", currentTime);
        entryData.put("amount", amount);
        entryData.put("category", category.getName());
        entryData.put("info", info);

        // Collect latest document to get latest id
        Query query = firestore.collection("users")
                .document(userUid)
                .collection("Spending")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (!snapshot.isEmpty()) {
                        if (cost != null) {
                            // Cost is being edited
                            int nextId = cost.getId();
                            id = Integer.toString(nextId);
                        } else {
                            // Cost is being added. Get ID of latest spending
                            String result = snapshot.getDocuments().get(0).getId();
                            int nextId = Integer.parseInt(result) + 1;
                            id = Integer.toString(nextId);
                        }
                    } else {
                        // No spending yet, make first id
                        id = "1";
                    }
                    entryData.put("id", id);
                    firestore.collection("users")
                            .document(userUid)
                            .collection("Spending")
                            .document(id)
                            .set(entryData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(),
                                                "£" + entryData.get("amount") + " added to " + entryData.get("category"),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Unable to update costs",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                } else {

                }
            }
        });
    }
}
