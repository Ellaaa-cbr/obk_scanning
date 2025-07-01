package com.example.obk.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.obk.data.local.entity.AuditLog;

import java.util.List;

@Dao
public interface AuditLogDao {
    /**
     * Insert a new AuditLog record.
     * If the primary key conflicts, replace the existing record.
     *
     * @param log the AuditLog object to insert
     * @return the row ID of the inserted AuditLog
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AuditLog log);

    /**
     * Retrieve all AuditLog entries in descending timestamp order.
     *
     * @return a LiveData list of all AuditLog records, newest first
     */
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    LiveData<List<AuditLog>> getAll();

    /**
     * Fetch all AuditLog entries that have not been synced to the server.
     *
     * @return a List of AuditLog records where synced == false
     */
    @Query("SELECT * FROM audit_log WHERE synced = 0")
    List<AuditLog> getUnsynced();


    /**
     * Mark the specified AuditLog records as synced.
     *
     * @param ids list of AuditLog primary key IDs to update
     */
    @Query("UPDATE audit_log SET synced = 1 WHERE id IN (:ids)")
    void markSynced(List<Integer> ids);
}