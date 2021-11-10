package com.example.homepantry;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements PantryListAdapter.OnClickInterface {

    private RecyclerView itemsRecyclerView;
    PantryListAdapter pantryAdapter;
    AppDatabase db;
    ExecutorService executor;
    PantryViewModel model;
    private ActivityResultLauncher<Intent> forResult;
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

        model = new ViewModelProvider(this).get(PantryViewModel.class);
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
                forResult.launch(new Intent(MainActivity.this, ScanBarcodeActivity.class));
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

        model.sendNotification();
        forResult = registerForResult();

        db = AppDatabase.getDatabase(this);
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
                        final Future<Item> future = executor.submit(new ItemCallable(db, id));

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                db.itemDao().deleteItem(id);
                            }
                        });
                        showSnackBar(viewHolder, future);
                    }
                });
    }
    private static class ItemCallable implements Callable<Item> {
        AppDatabase db;
        int id;

        public ItemCallable(AppDatabase db, int id){
            this.db = db;
            this.id = id;
        }
        @Override
        public Item call() throws Exception {
            return  db.itemDao().getItem(id);
        }
    }
    private void showSnackBar(RecyclerView.ViewHolder viewHolder, Future<Item> future){
        Snackbar
                .make(viewHolder.itemView, getString(R.string.item_deleted),
                        Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    db.itemDao().insert(future.get());
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                })
                .show();
    }

    @Override
    public void onClickMethod(int id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Item item = db.itemDao().getItem(id);
                model.getCurrentItem().postValue(item);
            }
        });

    }
    private ActivityResultLauncher<Intent> registerForResult(){
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            // Handle the Intent
                            assert intent != null;
                            Bundle scanData = intent.getExtras();
                            Intent addItemActivity = new Intent(MainActivity.this, AddAndEditItemActivity.class);
                            addItemActivity.putExtras(scanData);
                            startActivity(addItemActivity);
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_action_main_activity, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pantryAdapter.getFilter().filter(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}