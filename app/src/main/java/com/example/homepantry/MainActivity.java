package com.example.homepantry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity{

    private RecyclerView itemsRecyclerView;
    PantryListAdapter pantryAdapter;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemsRecyclerView = findViewById(R.id.recycler_view_items);

        pantryAdapter = new PantryListAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        itemsRecyclerView.setLayoutManager(linearLayoutManager);
        itemsRecyclerView.setHasFixedSize(true);
        itemsRecyclerView.setAdapter(pantryAdapter);

        ItemViewModel model = new ViewModelProvider(this).get(ItemViewModel.class);
        final Observer<List<Item>> itemsObserver = new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable final List<Item> items) {
                // Update the UI
                pantryAdapter.swapItems(items);
                pantryAdapter.notifyDataSetChanged();
            }
        };
        model.getItems().observe(this, itemsObserver);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent addItemActivity = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(addItemActivity);
            }
        });

        ItemTouchHelper itemTouchHelper = deleteItemOnSwipe();
        itemTouchHelper.attachToRecyclerView(itemsRecyclerView);
    }

    private ItemTouchHelper deleteItemOnSwipe(){

        return new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int id = (int) viewHolder.itemView.getTag();
                        AppDatabase db = AppDatabase.getDatabase(MainActivity.this);
                        AppDatabase.getExecutorsService().execute(new Runnable() {
                            @Override
                            public void run() {
                                db.itemDao().deleteItem(id);
                            }
                        });
                    }
                });
    }

}