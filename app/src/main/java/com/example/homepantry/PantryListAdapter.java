package com.example.homepantry;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepantry.database.Item;
import com.example.homepantry.utilities.DateUtils;

import java.time.LocalDateTime;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull PantryListAdapter.ItemViewHolder holder, int position) {
        Item item = pantryItems.get(position);
        String name = item.itemName;
        Date date  = item.expirationDate;
        long days = DateUtils.daysTillExpiration(context, date);
        holder.itemName.setText(name);
        holder.daysTillExpiration.setText("Potek uporabe: " + days + " dni");
        if(days < 1) {
            holder.daysTillExpiration.setTextColor(Color.RED);
        }
        else{
            holder.daysTillExpiration.setTextColor(Color.BLACK);
        }
        holder.itemView.setTag(item.itemId);
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
        private final TextView itemName;
        private final TextView daysTillExpiration;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            daysTillExpiration = itemView.findViewById(R.id.days_till_expiration);
        }
        public TextView getItemName() {
            return itemName;
        }
        public TextView getDaysTillExpiration() {
            return daysTillExpiration;
        }
    }
}
