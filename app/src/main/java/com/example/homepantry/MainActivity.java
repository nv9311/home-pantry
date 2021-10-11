package com.example.homepantry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private RecyclerView items;
    PantryListAdapter pantryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = findViewById(R.id.recycler_view_items);

        pantryAdapter = new PantryListAdapter(new String[]{"neki", "nekidva", "neki3"});

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        items.setLayoutManager(linearLayoutManager);
        items.setHasFixedSize(true);
        items.setAdapter(pantryAdapter);
    }
}