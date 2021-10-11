package com.example.homepantry;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;

import java.util.List;

public class ItemViewModel extends AndroidViewModel {
    private LiveData<List<Item>> items;
    public ItemViewModel(Application application){
        super(application);
        items = AppDatabase.getDatabase(application).itemDao().getAll();
    }
    public LiveData<List<Item>> getItems() {
        return items;
    }

}
