package ncl.csc3122.spendguru;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BudgetInfo extends Fragment {

    // Firebase
    private String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Anychart
    private AnyChartView anyChartView;

    // Views
    private TextView budgetCategory;
    private TextView budgetAmount;
    private TextView budgetSpent;
    private TextView budgetRemaining;
    private TextView budgetStartDate;
    private TextView budgetEndDate;
    private Button editBudget;
    private Button deleteBudget;

    // Primitive and Other Objects
    private Category category;
    private Date startDate;
    private Date endDate;
    private double spent = 0;
    private double remaining = 0;
    private double amount = 0;

    public BudgetInfo(Category category) {
        this.category = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget_info, null, false);

        // Set XML components
        budgetCategory = view.findViewById(R.id.budgetCategory);
        budgetAmount = view.findViewById(R.id.budgetAmount);
        budgetSpent = view.findViewById(R.id.budgetSpent);
        budgetRemaining = view.findViewById(R.id.budgetRemaining);
        budgetStartDate = view.findViewById(R.id.startDate);
        budgetEndDate = view.findViewById(R.id.endDate);
        editBudget = view.findViewById(R.id.editBudget);
        deleteBudget = view.findViewById(R.id.deleteBudget);

        setValues();

        editBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditBudget();
            }
        });

        deleteBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUp popUp = new BudgetPopUp(
                        "Are You Sure You Want to Delete This Budget?",
                        "Delete this budget.",
                        "Cancel.");
                popUp.showPopupWindow(v);
            }
        });

        return view;
    }

    /**
     * Set category text. Get budget data and set text for dates and budget amount.
     */
    private void setValues() {
        budgetCategory.setText(category.getName() + " Budget");

        firestore.collection("users")
            .document(userUid)
            .collection("Budget")
            .document(category.getName())
            .get()
            .addOnCompleteListener(new  OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        amount = snapshot.getDouble("amount");
                        budgetAmount.setText(Double.toString(amount));

                        Format formatter = new SimpleDateFormat("EEE dd/MM/yyyy");

                        startDate = snapshot.getDate("startDate");
                        budgetStartDate.setText(formatter.format(startDate));

                        endDate = snapshot.getDate("endDate");
                        budgetEndDate.setText(formatter.format(endDate));

                        getBudgetSpent();
                    } else {
                        Toast.makeText(getContext(), "Unable to get data.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to get data.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Query the firestore to get amount of budget spent. Calculate the remaining amount.
     */
    private void getBudgetSpent() {
        Query query = firestore.collection("users")
                .document(userUid)
                .collection("Spending")
                .whereEqualTo("category", category.getName());

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (!snapshot.isEmpty()) {
                        List<DocumentSnapshot> documentSnapshots = snapshot.getDocuments();
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            DocumentSnapshot currentDocument = documentSnapshots.get(i);
                            Date date = currentDocument.getDate("date");

                            if (startDate.before(date) && endDate.after(date)) {
                                spent = spent + currentDocument.getDouble("amount");
                            }
                        }
                        budgetSpent.setText(Double.toString(spent));
                        remaining = amount - spent;
                        budgetRemaining.setText(Double.toString(remaining));
                        setChart();
                    }
                }
            }
        });
    }

    /**
     * Create chart and assign data.
     */
    private void setChart() {
        anyChartView = getView().findViewById(R.id.budgetChart);

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Budget", amount));
        data.add(new ValueDataEntry("Spent", spent));
        data.add(new ValueDataEntry("Remaining", remaining));

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("£{%Value}{groupsSeparator: }");

        cartesian.animation(true);
        cartesian.title(category.getName() + " Budget");

        double min = spent;
        if (remaining < min) min = remaining;
        if (amount < min) min = amount;

        cartesian.yScale().minimum(min);

        cartesian.yAxis(0).labels().format("£{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        anyChartView.setChart(cartesian);
    }

    /**
     * Go to edit budget, pass through the information so it is not needed to collect again.
     */
    private void goToEditBudget() {
        AddBudget addBudget = new AddBudget(category, amount, startDate, endDate);
        AppCompatActivity activity = (AppCompatActivity) getView().getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  addBudget)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Deletes budget and feedbacks to users if successful.
     */
    private void deleteBudget() {
        firestore.collection("users")
                .document(userUid)
                .collection("Budget")
                .document(category.getName())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),
                                category + " budget has been deleted.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),
                                "Unable to delete " + category + " budget.",
                                Toast.LENGTH_LONG).show();
                    }
                });

        goToBudgets();
    }

    /**
     * Goes to budgets fragment.
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

    /**
     * Popup for confirmation of deleting budget.
     */
    class BudgetPopUp extends PopUp {

        public BudgetPopUp(String headerText, String yesText, String noText) {
            super(headerText, yesText, noText);
        }

        @Override
        public void setPopupWindowListener() {
            getPopupWindow().dismiss();
        }

        @Override
        public void setNoButtonListener() {
            getPopupWindow().dismiss();
        }

        @Override
        public void setYesButtonListener() {
            getPopupWindow().dismiss();
            deleteBudget();
        }
    }
}
