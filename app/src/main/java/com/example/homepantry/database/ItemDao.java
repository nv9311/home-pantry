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

    @Query("SELECT * FROM items WHERE DATE(ROUND(expiration_date / 1000), 'unixepoch', 'localtime') < date('now', 'localtime') LIMIT 2")
    List<Item> getExpiredItems();

    @Query("SELECT * FROM items WHERE DATE(ROUND(expiration_date / 1000), 'unixepoch', 'localtime') >= date('now', 'localtime') ORDER BY expiration_date LIMIT 2")
    List<Item> getSoonToBeExpiredItems();

    @Query("SELECT * FROM items WHERE barcode = :barcode ORDER BY date_added DESC LIMIT 1")
    LiveData<Item> getItem(String barcode);

    @Query("UPDATE items SET item_name = :itemName, barcode = :barcode, image = :image, expiration_date = :expirationDate WHERE itemId = :id")
    void update(int id, String itemName, String barcode, byte[] image, Date expirationDate);

    @Delete
    void delete(Item item);

    @Query("DELETE FROM items WHERE itemId = :id")
    void deleteItem(int id);

    @Insert
    void insertAll(Item... items);

    @Insert
    void insert(Item item);
}
