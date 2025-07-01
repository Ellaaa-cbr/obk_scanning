package com.example.obk.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "inventory")
public class Inventory {
    @PrimaryKey
    @NonNull
    public String id;                 // GUID / barcode

    public String type;               // TOTE-in | TOTE-out | Package
    public String contents;           // description
    public String measurement;        // unit (g, kg, each, etc.)

    @ColumnInfo(name = "initial_quantity")
    public double initialQuantity;
    @ColumnInfo(name = "current_quantity")
    public double currentQuantity;

    @ColumnInfo(name = "creation_date")
    public Date creationDate;
    @ColumnInfo(name = "last_edited")
    public Date lastEdited;
    @ColumnInfo(name = "expiry_date")
    public Date expiryDate;

    @ColumnInfo(name = "current_location")
    public String currentLocation;    // FK to Locations table (omitted for brevity)
    public String association;        // charity/org ID or null
}
