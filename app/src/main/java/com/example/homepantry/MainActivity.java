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
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity implements PantryListAdapter.OnClickInterface {

    private RecyclerView itemsRecyclerView;
    PantryListAdapter pantryAdapter;
    AppDatabase db;
    ExecutorService executor;
    ItemViewModel model;
    public static final String ITEM_KEY = "item_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemsRecyclerView = findViewById(R.id.recycler_view_items);

        pantryAdapter = new PantryListAdapter(this, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        itemsRecyclerView.setLayoutManager(linearLayoutManager);
        itemsRecyclerView.setHasFixedSize(true);
        itemsRecyclerView.setAdapter(pantryAdapter);

        model = new ViewModelProvider(this).get(ItemViewModel.class);
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
                Intent addItemActivity = new Intent(MainActivity.this, AddAndEditItemActivity.class);
                startActivity(addItemActivity);
            }
        });

        ItemTouchHelper itemTouchHelper = deleteItemOnSwipe();
        itemTouchHelper.attachToRecyclerView(itemsRecyclerView);

        executor = AppDatabase.getExecutorsService();

        final Observer<Item> itemObserver = new Observer<Item>() {
            @Override
            public void onChanged(@Nullable final Item updatingItem) {
                // Update the UI
                Intent intent = new Intent(MainActivity.this, AddAndEditItemActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(ITEM_KEY, updatingItem);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        model.getCurrentItem().observe(this, itemObserver);
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
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                db.itemDao().deleteItem(id);
                            }
                        });
                    }
                });
    }

    @Override
    public void onClickMethod(int id) {

        AppDatabase db = AppDatabase.getDatabase(this);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Item item = db.itemDao().getItem(id);
                model.getCurrentItem().postValue(item);
            }
        });

    }
}