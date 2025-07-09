package com.example.obk.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.obk.auth.TokenStore;
import com.example.obk.data.local.AppDatabase;
import com.example.obk.data.local.dao.AuditLogDao;
import com.example.obk.data.local.dao.InventoryDao;
import com.example.obk.data.local.entity.AuditLog;
import com.example.obk.data.local.entity.Charity;
import com.example.obk.network.ApiService;
import com.example.obk.network.NetworkModule;
import com.example.obk.network.dto.CheckoutRequest;
import com.example.obk.network.dto.CharityDto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * The single source of truth for app data – now powered by Retrofit instead of FakeApiService.
 */
public class AppRepository {

    private static volatile AppRepository INSTANCE;

    private final InventoryDao inventoryDao;
    private final AuditLogDao  auditLogDao;
    private final ApiService   api;
    private final TokenStore   tokenStore;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private AppRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        inventoryDao = db.inventoryDao();
        auditLogDao  = db.auditLogDao();
        api         = NetworkModule.api(context);
        tokenStore  = new TokenStore(context);
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
            try {
                Response<List<CharityDto>> resp = api.getCharities().execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    List<Charity> mapped = new ArrayList<>();
                    for (CharityDto dto : resp.body()) {
                        Charity c = new Charity(dto.id, dto.name);
                        c.id   = dto.id;
                        c.name = dto.name;
                        mapped.add(c);
                    }
                    live.postValue(mapped);
                } else {
                    // handle HTTP error – left as exercise
                }
            } catch (Exception e) {
                e.printStackTrace(); // network error
            }
        });
        return live;
    }

    /* ---------------------- Checkout ----------------------- */

    public void submitCheckout(String charityId, List<String> toteIds, File photoFile) {
        ioExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            // 1) Save locally for offline reliability
            List<Integer> newIds = new ArrayList<>();
            for (String toteId : toteIds) {
                AuditLog log = new AuditLog();
                log.inventoryId = toteId;
                log.changeDesc  = "Checked out to charity " + charityId;
                log.timestamp   = now;
                log.mode        = "Checkout";
                log.synced      = false;
                int rowId = (int) auditLogDao.insert(log);
                newIds.add(rowId);
            }

            // 2) Attempt server submission
            String photo = encodePhoto(photoFile);
            CheckoutRequest body = new CheckoutRequest(charityId, toteIds, now, photo);
            boolean success = false;
            try {
                success = api.submitCheckout(body).execute().isSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 3) Mark synced if successful
            if (success) auditLogDao.markSynced(newIds);
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

            for (AuditLog log : unsynced) {
                List<String> singleTote = new ArrayList<>();
                singleTote.add(log.inventoryId);
                CheckoutRequest req = new CheckoutRequest("unknown", singleTote, log.timestamp, null);
                boolean ok = false;
                try {
                    ok = api.submitCheckout(req).execute().isSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (ok) {
                    List<Integer> idList = new ArrayList<>();
                    idList.add(log.id);
                    auditLogDao.markSynced(idList);
                }
            }
        });
    }
}