package com.example.obk.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.obk.data.local.AppDatabase;
import com.example.obk.data.local.dao.AuditLogDao;
import com.example.obk.data.local.entity.AuditLog;
import com.example.obk.data.local.entity.Charity;
import com.example.obk.data.remote.ApiService;
import com.example.obk.data.remote.SubmitCallback;
import com.example.obk.data.remote.model.CheckoutRequest;
import com.example.obk.data.remote.model.OrganizationDto;
import com.example.obk.data.remote.model.OrganizationResponse;
import com.example.obk.data.remote.model.ToteItem;
import com.example.obk.network.NetworkModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;



/**
 * 单一数据源：决定网络 / 本地数据的读取与同步。
 */
public class AppRepository {

    private static volatile AppRepository INSTANCE;


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

    private final AuditLogDao     auditLogDao;
    private final ApiService      api;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Context appContext;

    private AppRepository(Context context) {
        appContext = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        auditLogDao = db.auditLogDao();
        api = NetworkModule.api(context);
    }


    public LiveData<List<Charity>> getCharityList() {
        MutableLiveData<List<Charity>> live = new MutableLiveData<>();

        api.getOrganizations(4).enqueue(new Callback<OrganizationResponse>() {
            @Override public void onResponse(Call<OrganizationResponse> call,
                                             Response<OrganizationResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    List<Charity> list = new ArrayList<>();
                    for (OrganizationDto o : resp.body().data) {
                        //charity typeId = 4
                        if (o.typeId == 4) {
                            list.add(new Charity(o.id, o.name));
                        }
                    }
                    live.postValue(list);
                } else {
                    live.postValue(Collections.emptyList());
                }
            }
            @Override public void onFailure(Call<OrganizationResponse> call, Throwable t) {
                live.postValue(Collections.emptyList());
            }
        });

        return live;
    }


    public void submitCheckout(String charityId,
                               List<String> toteIds,
                               File photoFile) {
        submitCheckout(charityId, toteIds, photoFile, null);

    }

    public void submitCheckout(String charityId,
                               List<String> toteIds,
                               File photoFile,
                               SubmitCallback cb) {
        ioExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();

            /* 1) insert local AuditLog, preserve data in local database in case network error */
            List<Integer> newIds = new ArrayList<>();
            for (String toteId : toteIds) {
                AuditLog log = new AuditLog();
                log.inventoryId = toteId;
                log.changeDesc  = "Checked out to charity " + charityId;
                log.timestamp   = timestamp;
                log.mode        = "Checkout";
                log.synced      = false;
                int rowId = (int) auditLogDao.insert(log);
                newIds.add(rowId);
            }

            /* 2) CheckoutRequest */
            CheckoutRequest req = new CheckoutRequest();
            req.charityId   = charityId;
            req.timestamp   = timestamp;
            req.photoBase64 = encodePhoto(photoFile);
            req.totes       = new ArrayList<>();

            for (String id : toteIds) {
                ToteItem ti = new ToteItem();
                ti.inventoryId = id;
                ti.quantity    = 36;
                req.totes.add(ti);
            }

            api.checkout(req).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                    if (r.isSuccessful()) {
                        auditLogDao.markSynced(newIds);
                        if (cb != null) cb.onResult(true, null);
                    } else {
                        if (cb != null) cb.onResult(false,
                                new IOException("HTTP " + r.code()));
                        SyncWorker.enqueue(appContext);
                    }
                }
                @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                    if (cb != null) cb.onResult(false, t);
                    SyncWorker.enqueue(appContext);
                }
            });
        });
    }


    public void syncUnsyncedLogs() {
        ioExecutor.execute(() -> {
            List<AuditLog> unsynced = auditLogDao.getUnsynced();
            if (unsynced.isEmpty()) return;

            for (AuditLog log : unsynced) {
                CheckoutRequest req = new CheckoutRequest();
                req.charityId   = "4";   //Todo
                req.timestamp   = log.timestamp;
                req.photoBase64 = null;

                ToteItem ti  = new ToteItem();
                ti.inventoryId  = log.inventoryId;
                ti.quantity     = 36;

                req.totes       = Collections.singletonList(ti);

                try {
                    Response<ResponseBody> resp = api.checkout(req).execute();
                    if (resp.isSuccessful()) {
                        auditLogDao.markSynced(Collections.singletonList(log.id));
                    }
                } catch (IOException ignored) {  }
            }
        });
    }

    //util method
    /** turn photo into Base64 string */
    private String encodePhoto(File file) {
        if (file == null || !file.exists()) return null;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 50% quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }
}
