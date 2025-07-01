package com.example.obk.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.obk.data.local.dao.AuditLogDao;
import com.example.obk.data.local.dao.InventoryDao;
import com.example.obk.data.local.entity.AuditLog;
import com.example.obk.data.local.entity.Charity;
import com.example.obk.data.local.entity.Inventory;


@Database(entities = {Inventory.class, AuditLog.class, Charity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract InventoryDao inventoryDao();

    public abstract AuditLogDao auditLogDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "kitchen_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}