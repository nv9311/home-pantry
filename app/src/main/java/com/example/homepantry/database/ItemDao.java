package com.example.homepantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM item")
    LiveData<List<Item>> getAll();

    @Delete
    void delete(Item item);

    @Insert
    void insertAll(Item... items);

    @Insert
    void insert(Item item);
}
