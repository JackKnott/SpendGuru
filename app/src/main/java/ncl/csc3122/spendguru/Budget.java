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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;


public class Budget extends Fragment {

    // Firebase
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Recycler view
    private RecyclerView rview;
    private RecyclerView.Adapter rviewAdapter;

    // Primitive and Other Objects
    private ArrayList<Category> categories;

    public Budget() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Set XML Components
        rview = view.findViewById(R.id.budgetRecyclerView);

        // Set recycler view
        rview.addItemDecoration(new DividerItemDecoration(rview.getContext(), DividerItemDecoration.VERTICAL));
        rview.setLayoutManager(new LinearLayoutManager(getContext()));

        categories = new ArrayList<>();

        addCategories();

        // Add recycler view adapter
        rviewAdapter = new BudgetRViewAdapter(categories);
        rview.setAdapter(rviewAdapter);

        return view;
    }

    /**
     * Add categories to recycler view and update.
     */
    private void addCategories() {
        firestore.collection("categories")
            .document("names")
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Map<String, Object> map = snapshot.getData();
                    if (map != null) {
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            categories.add(new Category(entry.getValue().toString()));
                        }
                        rviewAdapter.notifyDataSetChanged();
                    }
                }
            }
            }
        });
    }
}
