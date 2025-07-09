package com.example.obk.network;

import android.content.Context;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Automatically adds "Authorization: Bearer <token>" header if a token exists.
 */
public class AuthInterceptor implements Interceptor {

    private final com.example.obk.auth.TokenStore store;

    public AuthInterceptor(Context ctx) {
        store = new com.example.obk.auth.TokenStore(ctx);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String token = store.getToken();
        if (token != null && !token.isEmpty()) {
            req = req.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }
        return chain.proceed(req);
    }
}