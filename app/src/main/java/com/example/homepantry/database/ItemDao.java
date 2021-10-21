package com.example.homepantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items ORDER BY expiration_date")
    LiveData<List<Item>> getAll();

    @Query("SELECT * FROM items WHERE itemId = :id")
    Item getItem(int id);

    @Query("UPDATE items SET item_name = :itemName, barcode = :barcode, expiration_date = :expirationDate WHERE itemId = :id")
    void update(int id, String itemName, String barcode, Date expirationDate);

    @Delete
    void delete(Item item);

    @Query("DELETE FROM items WHERE itemId = :id")
    void deleteItem(int id);

    @Insert
    void insertAll(Item... items);

    @Insert
    void insert(Item item);
}
