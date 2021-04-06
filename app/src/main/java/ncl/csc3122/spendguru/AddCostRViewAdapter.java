package ncl.csc3122.spendguru;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AddCostRViewAdapter extends RecyclerView.Adapter<AddCostRViewAdapter.ViewHolder> {

    private ArrayList<Category> categories;

    public AddCostRViewAdapter(ArrayList<Category> categories) {
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
            Category category = new Category(textView.getText().toString());
            AddCost addCost = new AddCost(category);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainLayout, addCost)
                    .addToBackStack(null)
                    .commit();
        }
    }
}