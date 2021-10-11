package com.example.homepantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PantryListAdapter extends RecyclerView.Adapter<PantryListAdapter.ItemViewHolder> {

    private String[] localDataSet;

    public PantryListAdapter(String[] dataSet){
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public PantryListAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryListAdapter.ItemViewHolder holder, int position) {
        String text = localDataSet[position];
        holder.getTextView().setText(text);
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView item;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item_id);
        }
        public TextView getTextView() {
            return item;
        }
    }
}
