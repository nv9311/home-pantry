package com.example.homepantry.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Item.class}, version = 1)
@TypeConverters(com.example.homepantry.database.TypeConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ItemDao itemDao();
    private static volatile AppDatabase INSTANCE;
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
}
