package com.example.homepantry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homepantry.database.Item;
import com.example.homepantry.utilities.DateUtils;
import com.example.homepantry.utilities.ImageUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PantryListAdapter extends RecyclerView.Adapter<PantryListAdapter.ItemViewHolder> implements Filterable {

    private List<Item> pantryItems;
    private List<Item> pantryItemsFiltered;
    Context context;

    private final OnClickInterface onClickInterface;

    public PantryListAdapter(Context context, OnClickInterface onClickInterface){
        this.context = context;
        this.onClickInterface = onClickInterface;
    }


    public interface OnClickInterface{
        void onClickMethod(int id);
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
        Item item = pantryItemsFiltered.get(position);
        String name = item.itemName;
        Date date  = item.expirationDate;
        long days = DateUtils.daysTillExpiration(context, date);
        holder.itemName.setText(name);
        @SuppressLint("DefaultLocale") String expirationString = String.format("%s %d %s",
                context.getString(R.string.product_expiry),
                days,
                context.getString(R.string.days));
        holder.daysTillExpiration.setText(expirationString);
        if(days < 1) {
            holder.daysTillExpiration.setTextColor(Color.RED);
        }
        else{
            holder.daysTillExpiration.setTextColor(Color.BLACK);
        }
        holder.iconImage.setImageBitmap(ImageUtils.getBitmapFromByteArray(item.image));
        holder.itemView.setTag(item.itemId);
    }

    @Override
    public int getItemCount() {
        if(pantryItemsFiltered == null) return 0;
        else return pantryItemsFiltered.size();
    }

    public void swapItems(List<Item> items){
        pantryItems = items;
        pantryItemsFiltered = items;

    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Item> tmpList = new ArrayList<>();
                if(constraint.toString().isEmpty()){
                    tmpList = pantryItems;
                }
                else{
                    for (Item element:pantryItems) {
                        if (element.itemName.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            tmpList.add(element);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = tmpList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try {
                    pantryItemsFiltered = (List<Item>) results.values;
                    notifyDataSetChanged();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView itemName;
        private final TextView daysTillExpiration;
        private final ImageView iconImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            daysTillExpiration = itemView.findViewById(R.id.days_till_expiration);
            iconImage = itemView.findViewById(R.id.list_item_icon);
            itemView.setOnClickListener(this);
        }
        public TextView getItemName() {
            return itemName;
        }
        public TextView getDaysTillExpiration() {
            return daysTillExpiration;
        }

        @Override
        public void onClick(View v) {
            int id = (int) v.getTag();
            onClickInterface.onClickMethod(id);
        }
    }
}
