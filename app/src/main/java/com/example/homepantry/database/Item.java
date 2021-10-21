package com.example.homepantry.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity(tableName = "items")
public class Item implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int itemId;

    @ColumnInfo(name = "item_name")
    public String itemName;

    public String barcode;

    @ColumnInfo(name = "date_added")
    public LocalDateTime dateAdded;

    @ColumnInfo(name = "expiration_date")
    public Date expirationDate;
}
