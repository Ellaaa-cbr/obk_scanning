package com.example.obk.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.obk.data.local.entity.Inventory;


@Dao
public interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Inventory inventory);

    @Update
    void update(Inventory inventory);

    @Query("SELECT * FROM Inventory WHERE id = :id LIMIT 1")
    LiveData<Inventory> getById(String id);
}
