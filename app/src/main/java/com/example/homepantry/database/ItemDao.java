package com.example.homepantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items")
    LiveData<List<Item>> getAll();

    @Delete
    void delete(Item item);

    @Query("DELETE FROM items WHERE itemId = :id")
    void deleteItem(int id);

    @Insert
    void insertAll(Item... items);

    @Insert
    void insert(Item item);
}
