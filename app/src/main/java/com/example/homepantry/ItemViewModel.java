package com.example.homepantry;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;

import java.util.List;

public class ItemViewModel extends AndroidViewModel {
    private LiveData<List<Item>> items;
    private MutableLiveData<Item> item;

    public ItemViewModel(Application application){
        super(application);
        items = AppDatabase.getDatabase(application).itemDao().getAll();
    }
    public LiveData<List<Item>> getItems() {
        return items;
    }


    public MutableLiveData<Item> getCurrentItem() {
        if (item == null) {
            item = new MutableLiveData<>();
        }
        return item;
    }

}
