package ncl.csc3122.spendguru;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class BudgetRViewAdapter extends RecyclerView.Adapter<BudgetRViewAdapter.ViewHolder> {

    private ArrayList<Category> categories;

    public BudgetRViewAdapter(ArrayList<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        //holder.image.setImageResource(R.drawable.ic_add_circle_outline_black_24dp); //example
        holder.textView.setText(category.getName());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        public ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //image = itemView.findViewById(R.id.categoryImage);
            textView = itemView.findViewById(R.id.categoryText1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String categoryName = textView.getText().toString();
            Category category = new Category(categoryName);
            // If budget does not exist, go to add budget fragment
            changeFragment(category, view);
        }

        // TODO remove view
        public void changeFragment(final Category category, final View view) {
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = firestore.collection("users")
                    .document(userUid)
                    .collection("Budget")
                    .document(category.getName());

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            goToBudgetInfo(category, view);
                        } else {
                            goToAddBudget(category, view);
                        }
                    } else {

                    }
                }
            });
        }
    }

    private void goToAddBudget(Category category, View view) {
        AddBudget addBudget = new AddBudget(category);
        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  addBudget)
                .addToBackStack(null)
                .commit();
    }

    private void goToBudgetInfo (Category category, View view) {
        BudgetInfo budgetInfo = new BudgetInfo(category);
        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainLayout,  budgetInfo)
                .addToBackStack(null)
                .commit();
    }
}
