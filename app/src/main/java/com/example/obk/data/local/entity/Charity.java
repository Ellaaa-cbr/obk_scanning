package com.example.obk.data.local.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "charity")
public class Charity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;

    public Charity(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
