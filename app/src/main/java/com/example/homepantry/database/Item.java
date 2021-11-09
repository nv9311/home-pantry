package com.example.homepantry.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "items")
public class Item implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int itemId;

    @ColumnInfo(name = "item_name")
    public String itemName;

    public String barcode;

    public byte[] image;

    @ColumnInfo(name = "date_added")
    public Date dateAdded;

    @ColumnInfo(name = "expiration_date")
    public Date expirationDate;
}
