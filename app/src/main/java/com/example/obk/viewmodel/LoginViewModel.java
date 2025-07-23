
package com.example.obk.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.text.TextUtils;
import android.util.Log;

import com.example.obk.auth.TenantInfo;
import com.example.obk.auth.UserDetail;
import com.example.obk.auth.ValidateUserResponse;
import com.example.obk.data.remote.ApiService;
import com.example.obk.util.ChallengeSovler;
import com.google.gson.Gson;

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
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginViewModel extends ViewModel {

    /* ---------------- Retrofit & API ---------------- */

    private static final String BASE_URL =
            "https://apps.ensembleconsultinggroup.com/QairosDataServer/";

    private final ApiService api;

    /* ---------------- UI ---------------- */

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String>  errorMsg     = new MutableLiveData<>();

    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String>  getErrorMsg()     { return errorMsg; }

    public LoginViewModel() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(msg -> Log.d("HTTP", msg))
                .setLevel(HttpLoggingInterceptor.Level.BODY);

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
                .addInterceptor(timing)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit rt = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(ok)
                // ⚠ ScalarsConverterFactory MUST be before GsonConverterFactory
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = rt.create(ApiService.class);
    }

    /* ---------------- STEP-1: validate email ---------------- */

    public void login(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            errorMsg.postValue("email or password cannot be empty");
            return;
        }

        api.validateUser("Qairos", email).enqueue(new Callback<ValidateUserResponse>() {
            @Override public void onResponse(Call<ValidateUserResponse> call,
                                             retrofit2.Response<ValidateUserResponse> resp) {

                if (resp.isSuccessful() && resp.body() != null && resp.body().success
                        && resp.body().data != null && !resp.body().data.isEmpty()) {

                    TenantInfo tInfo  = resp.body().data.get(0);
                    String tenant = tInfo.name;

                    if (TextUtils.isEmpty(tenant)) {
                        errorMsg.postValue("Tenant name missing from server response");
                        return;
                    }

                    Log.d("Login", "tenant = " + tenant);
                    requestChallenge(email, password, tenant);
                } else {
                    errorMsg.postValue("Email does not exist or is not authorized");
                }

                Log.d("Login-JSON", "validateUser = " + new Gson().toJson(resp.body()));
            }

            @Override public void onFailure(Call<ValidateUserResponse> call, Throwable t) {
                errorMsg.postValue("STEP-1 Network error: " + t.getMessage());
            }
        });
    }

    /* ---------------- STEP-2: get challenge ---------------- */

    private void requestChallenge(String email, String password, String tenant) {
        api.getLogin(tenant, email).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override public void onResponse(Call<okhttp3.ResponseBody> call,
                                             retrofit2.Response<okhttp3.ResponseBody> resp) {

                if (resp.code() == 401 && resp.errorBody() != null) {
                    try {
                        String rawChallenge = resp.errorBody().string().replace("\"", "").trim();
                        Log.d("Step2", "challenge = " + rawChallenge);

                        String userChallenge = ChallengeSovler.solveUserChallenge(rawChallenge, password);
                        Log.d("Step2", "userChallenge = " + userChallenge);

                        doLoginWithChallenge(tenant, email, userChallenge);

                    } catch (Exception e) {
                        errorMsg.postValue("solve error: " + e.getMessage());
                        Log.e("Crypto", "solveChallenge error", e);
                    }
                } else {
                    errorMsg.postValue("get challenge fail, HTTP " + resp.code());
                }
            }

            @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                errorMsg.postValue("STEP-2 Network error: " + t.getMessage());
            }
        });
    }

    /* ---------------- STEP-3 + STEP-4 ---------------- */

    private void doLoginWithChallenge(String tenant, String email, String userChallenge) {
        api.login(tenant, email, userChallenge).enqueue(new Callback<String>() {
            @Override public void onResponse(Call<String> call, retrofit2.Response<String> resp) {
                if (resp.isSuccessful() && resp.body() != null && !TextUtils.isEmpty(resp.body())) {
                    // Server returns plain-text JWT
                    String token = resp.body().replace("\"", "").trim();
                    Log.d("Step3", "token = " + token);

                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"),   // <- JSON
                            "\"" + token + "\""                    // <- "eyJhbGciOiJI..."
                    );

                    api.getUserByToken(tenant, body).enqueue(new Callback<UserDetail>() {
                        @Override public void onResponse(Call<UserDetail> c, retrofit2.Response<UserDetail> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                loginSuccess.postValue(true);
                            } else {
                                errorMsg.postValue("Token invalid");
                            }
                        }
                        @Override public void onFailure(Call<UserDetail> c, Throwable t) {
                            errorMsg.postValue("STEP-4 Network error: " + t.getMessage());
                        }
                    });
                } else {
                    errorMsg.postValue("email or password wrong");
                }
            }

            @Override public void onFailure(Call<String> call, Throwable t) {
                errorMsg.postValue("STEP-3 Network error: " + t.getMessage());
            }
        });
    }
}