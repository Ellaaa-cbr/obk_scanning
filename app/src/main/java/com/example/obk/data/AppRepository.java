package com.example.obk.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.obk.data.local.AppDatabase;
import com.example.obk.data.local.dao.AuditLogDao;
import com.example.obk.data.local.dao.InventoryDao;
import com.example.obk.data.local.entity.AuditLog;
import com.example.obk.data.local.entity.Charity;
import com.example.obk.data.remote.ApiService;
import com.example.obk.data.remote.FakeApiService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The single source of truth for app data. Decides whether to fetch from
 * network (fake) or local DB. Provides LiveData to ViewModels.
 */
public class AppRepository {

    private static volatile AppRepository INSTANCE;

    private final InventoryDao inventoryDao;
    private final AuditLogDao auditLogDao;
    private final ApiService api;      // Here unused – rely on FakeApi
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private AppRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        inventoryDao = db.inventoryDao();
        auditLogDao = db.auditLogDao();
        api = null; // placeholder for real Retrofit instance
    }

    public static AppRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    /* ---------------------- Charities ---------------------- */

    public LiveData<List<Charity>> getCharityList() {
        MutableLiveData<List<Charity>> live = new MutableLiveData<>();
        ioExecutor.execute(() -> {
            List<Charity> remote = FakeApiService.getCharities();
            live.postValue(remote);
        });
        return live;
    }

    /* ---------------------- Checkout ----------------------- */

    public void submitCheckout(String charityId, List<String> toteIds, File photoFile) {
        ioExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            // 1. Insert AuditLog locally
            List<Integer> newIds = new ArrayList<>();
            for (String toteId : toteIds) {
                AuditLog log = new AuditLog();
                log.inventoryId = toteId;
                log.changeDesc = "Checked out to charity " + charityId;
                log.timestamp = now;
                log.mode = "Checkout";
                log.synced = false;
                int rowId = (int) auditLogDao.insert(log);
                newIds.add(rowId);
            }

            // 2. Try fake network submission immediately
            String photoBase64 = encodePhoto(photoFile);
            boolean success = FakeApiService.submitCheckout(charityId, toteIds, now, photoBase64);
            if (success) {
                auditLogDao.markSynced(newIds);
            }
            // If fail, SyncWorker will retry later.
        });
    }

    private String encodePhoto(File file) {
        if (file == null || !file.exists()) return null;
        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    /* ------------------ Called by SyncWorker --------------- */

    public void syncUnsyncedLogs() {
        ioExecutor.execute(() -> {
            List<AuditLog> unsynced = auditLogDao.getUnsynced();
            if (unsynced.isEmpty()) return;

            // Group by charity + timestamp for batch, or send individually
            for (AuditLog log : unsynced) {
                List<String> singleTote = new ArrayList<>();
                singleTote.add(log.inventoryId);
                boolean ok = FakeApiService.submitCheckout("unknown", singleTote, log.timestamp, null);
                if (ok) {
                    List<Integer> idList = new ArrayList<>();
                    idList.add(log.id);
                    auditLogDao.markSynced(idList);
                }
            }
        });
    }
}