package com.example.obk.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkModule {
    private static final String BASE_URL = "https://httpbin.org/"; // TODO put real API URL
    private static volatile ApiService api;

    /** original entry point kept */
    public static ApiService api(Context ctx) {
        if (api == null) {
            synchronized (NetworkModule.class) {
                if (api == null) {
                    OkHttpClient ok = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(ctx))
                            .build();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(ok)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(ApiService.class);
                }
            }
        }
        return api;
    }

    /** alias so existing sample code compiles */
    public static ApiService createApiService(Context ctx) {
        return api(ctx);
    }
}