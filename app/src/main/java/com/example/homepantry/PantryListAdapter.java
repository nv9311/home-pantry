package com.example.homepantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepantry.database.Item;

import java.util.Date;
import java.util.List;

public class PantryListAdapter extends RecyclerView.Adapter<PantryListAdapter.ItemViewHolder> {

    private List<Item> pantryItems;
    Context context;

    public PantryListAdapter(Context context){
        this.context = context;
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
        String text = pantryItems.get(position).itemName;
        String manufacturer = pantryItems.get(position).manufacturer;
        Date date  = pantryItems.get(position).expirationDate;
        holder.getTextView().setText(text + " " + manufacturer + " " + date);
    }

    @Override
    public int getItemCount() {
        if(pantryItems == null) return 0;
        else return pantryItems.size();
    }

    public void swapItems(List<Item> items){
        pantryItems = items;

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
