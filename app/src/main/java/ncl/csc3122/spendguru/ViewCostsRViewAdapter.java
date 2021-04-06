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

class ViewCostsRViewAdapter extends RecyclerView.Adapter<ViewCostsRViewAdapter.ViewHolder> {
        private ArrayList<Cost> costs;

    public ViewCostsRViewAdapter(ArrayList<Cost> costs) {
            this.costs = costs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder (@NonNull ViewHolder holder,int position){
        Cost cost = costs.get(position);
        holder.cost = cost;
        holder.textView.setText(cost.toString());
    }

    @Override
    public int getItemCount () {
        return costs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        public ImageView image;
        public Cost cost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.categoryText1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AddCost addCost = new AddCost(cost);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainLayout, addCost)
                    .addToBackStack(null)
                    .commit();
        }
    }
}


