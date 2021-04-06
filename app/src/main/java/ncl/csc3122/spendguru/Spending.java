package ncl.csc3122.spendguru;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Spending extends Fragment {

    // Firebase
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DocumentReference userDocuments = firestore.collection("users").document(userUid);

    // Anychart
    private AnyChartView anyChartView;

    // Views
    private Date startDate;
    private Date endDate;
    private TextView chartInfo;
    private FloatingActionButton spendingSettings;
    private LinearLayout scrollView;

    // Primitive and Other Objects
    private ArrayList<String> categories;
    private Map<String, Double> chartData;
    private ArrayList<Cost> costs;

    public Spending () {}

    public Spending (ArrayList<String> categories, Date startDate, Date endDate) {
        this.categories = categories;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spending, container, false);

        // Set XML components
        chartInfo = view.findViewById(R.id.spendingChartInfo);
        spendingSettings = view.findViewById(R.id.spendingSettings);
        anyChartView = view.findViewById(R.id.pieChart);
        scrollView = view.findViewById(R.id.costsLayout);

        spendingSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSpendingSettings();
            }
        });

        chartData = new HashMap<>();
        costs = new ArrayList<>();

        setDates();
        getSpending();

        return view;
    }

    /**
     * If dates are null, set to the past thirty days.
     */
    private void setDates() {
        if (startDate == null) {
            // Get time and day 30 days ago
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Calendar.getInstance().getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            startDate = calendar.getTime();
        }

        if (endDate == null) {
            endDate = Calendar.getInstance().getTime();
        }
    }

    /**
     * Fill scroll view with costs from user with chosen criteria.
     */
    private void fillScrollView() {
        for (int i = 0; i < costs.size(); i++) {
            Cost cost = costs.get(i);

            String costView = cost.toString();

            TextView textView = new TextView(getContext());
            textView.setText(costView);
            textView.setPadding(10, 25, 10, 75);
            scrollView.addView(textView);

            // Divider
            View divider = new View(getContext());
            divider.setBackgroundColor(Color.parseColor("#000000"));
            scrollView.addView(divider,
                    new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 3));
        }
    }

    /**
     * Go to spending settings to change criteria.
     */
    private void goToSpendingSettings() {
        SpendingSettings spendingSettings = new SpendingSettings();
        AppCompatActivity activity = (AppCompatActivity) getView().getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  spendingSettings)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Collect spending documents for user for certain category and certain dates chosen. If no
     * dates chosen, the last thirty days will be chose. If no categories chosen, all categories
     * will be chosen.
     */
    private void getSpending() {
        Query query = userDocuments.collection("Spending");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (!snapshot.isEmpty()) {
                        List<DocumentSnapshot> documentSnapshots = snapshot.getDocuments();
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            DocumentSnapshot document = documentSnapshots.get(i);
                            Date costDate = document.getDate("date");

                            if (startDate.before(costDate) && endDate.after(costDate)){
                                // Make cost
                                int id = Integer.parseInt(document.getId());
                                double amount = document.getDouble("amount");
                                String category = document.getString("category");
                                String info = document.getString("info");
                                Cost cost = new Cost(id, category, amount, costDate, info);

                                // Add costs
                                if (categories == null || categories.size() == 0) {
                                    costs.add(cost);
                                    if (costsContains(category)) {
                                        addToExistingCost(cost);
                                    } else {
                                        chartData.put(cost.getCategory(), cost.getAmount());
                                    }
                                } else {
                                    if (categories.contains(cost.getCategory())) {
                                        costs.add(cost);
                                        if (costsContains(category)) {
                                            addToExistingCost(cost);
                                        } else {
                                            chartData.put(cost.getCategory(), cost.getAmount());
                                        }
                                    }
                                }
                            }
                        }
                        setUpPieChart();
                        fillScrollView();
                    } else {
                        Toast.makeText(getContext(), "Snapshot is empty", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Task unsuccessful", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Adding to existing cost in array list if the cost already exists in the array list.
     * @param cost added to existing cost.
     */
    private void addToExistingCost(Cost cost) {
        if (chartData.containsKey(cost.getCategory())) {
            double newAmount = chartData.get(cost.getCategory());
            newAmount = newAmount + cost.getAmount();
            chartData.put(cost.getCategory(), newAmount);
        }
    }

    /**
     * To check if a cost already exists when adding the sum of each categories costs.
     * @param category checked if it currently exists in the array list.
     * @return if the category exists in the array list.
     */
    private boolean costsContains(String category) {
        if (chartData.containsKey(category)) return true;
        return false;
    }

    /**
     * Set up pie chart information.
     */
    private void setUpPieChart() {

        if (chartData == null || chartData.size() == 0) {
            chartInfo.setText("No information available to display in chart.");
        }

        Pie pie = AnyChart.pie();

        List<DataEntry> dataEntries = new ArrayList<>();

        for (Map.Entry<String, Double> entry : chartData.entrySet()) {
            dataEntries.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        pie.data(dataEntries);

        Format formatter = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm");
        String startDateString = formatter.format(startDate);
        String endDateString = formatter.format(endDate);

        pie.title("Spending between " + startDateString + " & " + endDateString);
        pie.labels().position("outside");
        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Category")
                .padding(0d, 0d, 10d, 0d);
        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);
        pie.animation(true);

        anyChartView.setChart(pie);
    }
}
