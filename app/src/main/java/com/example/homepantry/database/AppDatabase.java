package com.example.homepantry.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Item.class}, version = 3)
@TypeConverters(com.example.homepantry.database.TypeConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ItemDao itemDao();
    private static volatile AppDatabase INSTANCE;
    static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

    public static AppDatabase getDatabase(Context context){
         if(INSTANCE == null) {
             synchronized (AppDatabase.class) {
                 if (INSTANCE == null) {
                     INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                             AppDatabase.class, "item_database").build();
                 }
             }
         }
         return INSTANCE;
    }
    public static ExecutorService getExecutorsService(){
        return databaseWriteExecutor;
    }

}
