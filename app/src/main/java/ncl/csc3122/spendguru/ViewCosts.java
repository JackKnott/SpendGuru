package ncl.csc3122.spendguru;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewCosts extends Fragment {

    // Firebase
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DocumentReference userDocuments = firestore.collection("users").document(userUid);

    // Recycler view
    private RecyclerView rview;
    private RecyclerView.Adapter rviewAdapter;

    // Primitive and Other Objects
    private ArrayList<Cost> costs;
    private ArrayList<String> rViewArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_costs, container, false);

        // Set XML components
        rview = view.findViewById(R.id.viewCostsRecycler);

        // Set recycler view
        rview.addItemDecoration(new DividerItemDecoration(rview.getContext(), DividerItemDecoration.VERTICAL));
        rview.setLayoutManager(new LinearLayoutManager(getContext()));

        costs = new ArrayList<>();
        rViewArray = new ArrayList<>();

        // Add recycler view adapter
        rviewAdapter = new ViewCostsRViewAdapter(costs);
        rview.setAdapter(rviewAdapter);

        getCosts();

        return view;
    }

    /**
     * Collect all spending documents for user
     */
    private void getCosts() {
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

                            // Make cost
                            int id = Integer.parseInt(document.getId());
                            Date costDate = document.getDate("date");
                            double amount = Double.parseDouble(document.get("amount").toString());
                            String category = document.get("category").toString();
                            String info = document.getString("info");
                            Cost cost = new Cost(id, category, amount, costDate, info);

                            // Add costs
                            costs.add(cost);

                            rViewArray.add(cost.toString());
                        }
                        rviewAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Snapshot is empty", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Task unsuccessful", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
