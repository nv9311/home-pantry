package com.example.homepantry;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView items;
    PantryListAdapter pantryAdapter;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = findViewById(R.id.recycler_view_items);

        ExecutorService executor = AppDatabase.getExecutorsService();
        db = AppDatabase.getDatabase(MainActivity.this);

        executor.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Item item = new Item();
                item.dateAdded = LocalDateTime.now();
                item.itemName = "Test item 5";
                item.manufacturer = "Zelene doline";
                item.expirationDate = new Date();
                db.itemDao().insert(item);
            }
        });


        pantryAdapter = new PantryListAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        items.setLayoutManager(linearLayoutManager);
        items.setHasFixedSize(true);
        items.setAdapter(pantryAdapter);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Item> items = db.itemDao().getAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pantryAdapter.swapItems(items);
                        pantryAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}