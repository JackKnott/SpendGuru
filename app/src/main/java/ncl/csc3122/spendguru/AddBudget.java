package ncl.csc3122.spendguru;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class AddBudget extends Fragment {

    // Firebase
    private final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Views
    private TextView budgetCategory;
    private EditText enterAmount;
    private EditText enterStartDate;
    private EditText enterEndDate;
    private Button submit;

    // Primitive and Other Objects
    private Category category;
    private double budgetAmount;
    private Date startDate;
    private Date endDate;
    private boolean budgetExists = false;

    public AddBudget(Category category) {
        this.category = category;
    }

    // If current budget is wanting to be edited, go to add budget with the data of current budget
    public AddBudget(Category category, double budgetAmount, Date startDate, Date endDate) {
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        budgetExists = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_add_budget, null, false);

        // Set XML components
        budgetCategory = view.findViewById(R.id.budgetCategory);
        enterAmount = view.findViewById(R.id.enterBudgetAmount);
        enterStartDate = view.findViewById(R.id.enterStartDate);
        enterEndDate = view.findViewById(R.id.enterEndDate);
        submit = view.findViewById(R.id.submitBudget);

        budgetCategory.setText(category.getName() + " Budget");

        // If budget exists, fill in data
        if (budgetExists) {
            enterAmount.setText(Double.toString(budgetAmount));

            Format formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);

            enterStartDate.setText(formatter.format(startDate));
            enterEndDate.setText(formatter.format(endDate));
        }

        // Set pop up calendar pickers
        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setCalendarPicker(enterStartDate);
        calendarPicker.setCalendarPicker(enterEndDate);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBudget();
            }
        });
        return view;
    }

    /**
     * If the budget form is validated, the budget will be added and the user will be taken
     * to the budgets fragment.
     */
    private void submitBudget() {
        try {
            if (isValidated()) {
                addBudget();
                goToBudgets();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Go through each form component to see if entered suitably. No field can be empty and the
     * start date can not be after the end date. Feedback will be provided through toast to the
     * user.
     * @return boolean if the form is entered suitably by the user
     * @throws ParseException
     */
    private boolean isValidated() throws ParseException {
        if (enterAmount.getText().toString().length() == 0) {
            Toast.makeText(getContext(), "Budget amount has not been entered.", Toast.LENGTH_LONG).show();
            return false;
        }

        budgetAmount = Double.parseDouble(enterAmount.getText().toString());

        if (enterStartDate.getText().toString().length() == 0) {
            Toast.makeText(getContext(), "Start date has not been entered.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (enterEndDate.getText().toString().length() == 0) {
            Toast.makeText(getContext(), "End date has not been entered.", Toast.LENGTH_LONG).show();
            return false;
        }

        startDate = new SimpleDateFormat("dd/MM/yyyy").parse(enterStartDate.getText().toString());
        endDate = new SimpleDateFormat("dd/MM/yyyy").parse(enterEndDate.getText().toString());

        if (endDate.before(startDate)) {
            Toast.makeText(getContext(), "End date is before start date.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * Adds budget to Firestore.
     */
    private void addBudget() {
        Map<String, Object> budgetMap = new HashMap<>();
        budgetMap.put("amount", budgetAmount);
        budgetMap.put("startDate", startDate);
        budgetMap.put("endDate", endDate);

        firestore.collection("users")
                .document(userUid)
                .collection("Budget")
                .document(category.getName())
                .set(budgetMap);

        Toast.makeText(getContext(), "Budget updated", Toast.LENGTH_LONG).show();
    }

    /**
     * Goes to the budget fragment.
     */
    private void goToBudgets() {
        Budget budgetFragment = new Budget();
        AppCompatActivity activity = (AppCompatActivity) getView().getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  budgetFragment)
                .addToBackStack(null)
                .commit();
    }
}