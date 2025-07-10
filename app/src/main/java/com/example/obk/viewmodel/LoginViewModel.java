package com.example.obk.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.text.TextUtils;
import android.util.Log;

import com.example.obk.auth.TenantInfo;
import com.example.obk.auth.TokenResponse;
import com.example.obk.auth.UserDetail;
import com.example.obk.auth.ValidateUserResponse;
import com.example.obk.data.remote.ApiService;
import com.example.obk.util.CryptoUtils;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** 完整封装登录流程的 ViewModel */
public class LoginViewModel extends ViewModel {

    /* ---------------- Retrofit & API ---------------- */

    private static final String BASE_URL =
            "https://apps.ensembleconsultinggroup.com/QairosDataServer/";

    private final ApiService api;

    /* ---------------- UI 状态 ---------------- */

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String>  errorMsg     = new MutableLiveData<>();

    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String>  getErrorMsg()     { return errorMsg; }

    /* ---------------- 构造 ---------------- */

    public LoginViewModel() {

        /* ① 打印 URL + 头 + Body */
        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor(msg -> Log.d("HTTP", msg))
                        .setLevel(HttpLoggingInterceptor.Level.BODY);

        /* ② 选做：再打印一次简洁的时间消耗 */
        Interceptor timing = chain -> {
            Request req = chain.request();
            long t1 = System.nanoTime();
            Response rsp = chain.proceed(req);
            long t2 = System.nanoTime();
            Log.d("HTTP", String.format("→ %s %s  ← %d (%.1f ms)",
                    req.method(), req.url(), rsp.code(), (t2 - t1) / 1e6));
            return rsp;
        };

        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(timing)   // 可删
                .addInterceptor(logging)  // ★ 只在这里加一次
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit rt = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(ok)                              // ★ Retrofit 复用同一 client
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = rt.create(ApiService.class);
    }

    /* ---------------- STEP-1: 校验邮箱 ---------------- */

    public void login(String email, String password) {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            errorMsg.postValue("邮箱或密码不能为空");
            return;
        }

        api.validateUser("Qairos", email).enqueue(new Callback<ValidateUserResponse>() {
            @Override public void onResponse(Call<ValidateUserResponse> call,
                                             retrofit2.Response<ValidateUserResponse> resp) {

                if (resp.isSuccessful()
                        && resp.body() != null
                        && resp.body().success
                        && resp.body().data != null
                        && !resp.body().data.isEmpty()) {

                    TenantInfo tInfo  = resp.body().data.get(0);
                    String tenantName = tInfo.name;      // "QairosWorsley"（服务器字段是 Name）

                    if (TextUtils.isEmpty(tenantName)) {
                        errorMsg.postValue("服务器未返回租户信息，无法继续登录");
                        return;
                    }

                    Log.d("Login", "tenant = " + tenantName);
                    requestChallenge(email, password, tenantName);

                } else {
                    errorMsg.postValue("邮箱不存在或未授权");
                }

                Log.d("Login-JSON", "validateUser = "
                        + new Gson().toJson(resp.body()));
            }

            @Override public void onFailure(Call<ValidateUserResponse> call, Throwable t) {
                errorMsg.postValue("STEP-1 网络异常: " + t.getMessage());
            }
        });
    }

    /* ---------------- STEP-2: 拿挑战串 ---------------- */

    private void requestChallenge(String email, String password, String tenant) {

        api.getLogin(tenant, email).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override public void onResponse(Call<okhttp3.ResponseBody> call,
                                             retrofit2.Response<okhttp3.ResponseBody> resp) {

                if (resp.code() == 401 && resp.errorBody() != null) {
                    try {
                        /* ★ errorBody().string() 只能读一次！ */
                        String rawChallenge = resp.errorBody().string()
                                .replace("\"", "").trim();
                        Log.d("Step2", "challenge = " + rawChallenge);

                        String userChallenge =
                                CryptoUtils.solveChallenge(rawChallenge, password);
                        Log.d("Step2", "userChallenge = " + userChallenge);

                        doLoginWithChallenge(tenant, email, userChallenge);

                    } catch (Exception e) {
                        errorMsg.postValue("加密/解密失败: " + e.getMessage());
                        Log.e("Crypto", "solveChallenge error", e);
                    }
                } else {
                    errorMsg.postValue("获取挑战串失败，HTTP " + resp.code());
                }
            }

            @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                errorMsg.postValue("STEP-2 网络异常: " + t.getMessage());
            }
        });
    }

    /* ---------------- STEP-3 + STEP-4 ---------------- */

    private void doLoginWithChallenge(String tenant,
                                      String email,
                                      String userChallenge) {

        api.login(tenant, email, userChallenge).enqueue(new Callback<TokenResponse>() {
            @Override public void onResponse(Call<TokenResponse> call,
                                             retrofit2.Response<TokenResponse> resp) {

                if (resp.isSuccessful()
                        && resp.body() != null
                        && !TextUtils.isEmpty(resp.body().token)) {

                    String token = resp.body().token;
                    Log.d("Step3", "token = " + token);

                    RequestBody body = RequestBody.create(
                            MediaType.parse("text/plain"),
                            token.getBytes(StandardCharsets.UTF_8));

                    api.getUserByToken(tenant, body)
                            .enqueue(new Callback<UserDetail>() {
                                @Override public void onResponse(Call<UserDetail> c,
                                                                 retrofit2.Response<UserDetail> r) {
                                    if (r.isSuccessful() && r.body() != null) {
                                        loginSuccess.postValue(true);
                                    } else {
                                        errorMsg.postValue("Token 无效或用户信息解析失败");
                                    }
                                }
                                @Override public void onFailure(Call<UserDetail> c, Throwable t) {
                                    errorMsg.postValue("STEP-4 网络异常: " + t.getMessage());
                                }
                            });

                } else {
                    errorMsg.postValue("邮箱或密码错误");
                }
            }

            @Override public void onFailure(Call<TokenResponse> call, Throwable t) {
                errorMsg.postValue("STEP-3 网络异常: " + t.getMessage());
            }
        });
    }
}
