package ncl.csc3122.spendguru;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;


public class CostCategories extends Fragment {
    // Firebase
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Views
    private RecyclerView rview;
    private RecyclerView.Adapter rviewAdapter;
    private FloatingActionButton viewCosts;

    // Primitive and Other Objects
    private ArrayList<Category> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cost_categories, container, false);

        // Set XML components
        rview = view.findViewById(R.id.recyclerView);
        viewCosts = view.findViewById(R.id.costsViewButton);

        // Set recycler view
        rview.addItemDecoration(new DividerItemDecoration(rview.getContext(), DividerItemDecoration.VERTICAL));
        rview.setLayoutManager(new LinearLayoutManager(getContext()));

        categories = new ArrayList<>();

        viewCosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToViewCosts();
            }
        });

        addCategories();

        // Add recycler view adapter
        rviewAdapter = new AddCostRViewAdapter(categories);
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

    /**
     * Go to the view costs fragment to view all costs from the user.
     */
    private void goToViewCosts() {
        ViewCosts viewCosts = new ViewCosts();
        AppCompatActivity activity = (AppCompatActivity) getView().getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  viewCosts)
                .addToBackStack(null)
                .commit();
    }
}