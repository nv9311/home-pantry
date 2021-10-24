package com.example.homepantry;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;
import com.example.homepantry.workers.NotificationWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemViewModel extends AndroidViewModel {
    private LiveData<List<Item>> items;
    private MutableLiveData<Item> item;
    private WorkManager mWorkManager;
    private LiveData<String> nameItem;
    private Application application;

    private final String UNIQUE_WORK_NAME = "NOTIFICATION_PERIODIC_WORK";
    private final String TAG_NOTIFICATION = "NOTIFICATION";

    public ItemViewModel(Application application){
        super(application);
        this.application = application;
        items = AppDatabase.getDatabase(application).itemDao().getAll();
        mWorkManager = WorkManager.getInstance(application);
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

    public void sendNotification(){
        PeriodicWorkRequest notificationsWork =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 1, TimeUnit.DAYS)
                        .addTag(TAG_NOTIFICATION)
                        .setInitialDelay(23, TimeUnit.HOURS)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();
        mWorkManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, notificationsWork);
    }

    public void getNameFromBarcode(String barcode){
       nameItem = AppDatabase.getDatabase(application).itemDao().getName(barcode);
    }
    public LiveData<String> getNameItem(){
        return nameItem;
    }
}