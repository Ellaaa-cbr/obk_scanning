

package com.example.obk.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "audit_log")
public class AuditLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "inventory_id")
    public String inventoryId;    // tote ID(s), comma‑separated if multiple

    @ColumnInfo(name = "change_desc")
    public String changeDesc;     //  changes

    public long timestamp;        // epoch millis
    public String mode;           // Manual | Usage | Spoilage | Checkout | etc.

    public boolean synced;        // false = pending sync
}