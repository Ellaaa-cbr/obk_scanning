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

    /* ----------- 单例 ----------- */
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

    /* ----------- 成员 ----------- */
    private final AuditLogDao     auditLogDao;
    private final ApiService      api;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private AppRepository(Context context) {
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
                        // 如果后端真的还会塞进其它 typeId，也可以在客户端再过滤：
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
        ioExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();

            /* 1) 本地插入 AuditLog，先保证离线可追溯 */
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

            /* 2) 组装请求体 */
            CheckoutRequest req = new CheckoutRequest();
            req.charityId   = charityId;
            req.timestamp   = timestamp;
            req.photoBase64 = encodePhoto(photoFile);
            req.totes       = new ArrayList<>();

            for (String id : toteIds) {
                ToteItem ti = new ToteItem();
                ti.inventoryId = id;
                ti.quantity    = 36;          // TODO: 真实数量
                req.totes.add(ti);
            }

            /* 3) 调用后台接口 */
            api.checkout(req).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                    if (r.isSuccessful()) {
                        auditLogDao.markSynced(newIds);
                    }
                }
                @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                    // 离线：保持未同步状态，交给 SyncWorker
                }
            });
        });
    }

    /* ---------- 后台补偿同步 ---------- */
    public void syncUnsyncedLogs() {
        ioExecutor.execute(() -> {
            List<AuditLog> unsynced = auditLogDao.getUnsynced();
            if (unsynced.isEmpty()) return;

            for (AuditLog log : unsynced) {
                CheckoutRequest req = new CheckoutRequest();
                req.charityId   = "unknown";          // 视业务需求替换
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
                } catch (IOException ignored) { /* 继续下次循环 */ }
            }
        });
    }

    /* ===================================================================== */
    /* ==================              工具方法           ================== */
    /* ===================================================================== */

    /** 把照片压缩 → Base64，返回 null 表示无照片或读取失败 */
    private String encodePhoto(File file) {
        if (file == null || !file.exists()) return null;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 50% 质量足够上传，又不会过大
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }
}
