package com.example.obk.network;

import android.content.Context;
import android.util.Log;

import com.example.obk.auth.TokenManager;
import com.example.obk.data.remote.ApiService;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkModule {
    private static ApiService INSTANCE;
    private static final String BASE_URL =
            "https://apps.ensembleconsultinggroup.com/"
                    + "QairosDataServerOBK/QairosOBK/api/restBase/";
    public static ApiService api(Context ctx) {
        if (INSTANCE != null) return INSTANCE;


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);


        Interceptor auth = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            String token = TokenManager.get(ctx);
            Log.d("Auth", "token = " + token);


            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return chain.proceed(builder.build());
        };


        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(auth)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return INSTANCE = retrofit.create(ApiService.class);
    }
}
