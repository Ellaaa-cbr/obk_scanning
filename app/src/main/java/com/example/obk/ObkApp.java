package com.example.obk;

import android.app.Application;

import com.example.obk.auth.AuthRepository;
import com.example.obk.network.ApiService;
import com.example.obk.network.NetworkModule;

public class ObkApp extends Application {
    private AuthRepository authRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        ApiService api = NetworkModule.api(this); // <-- fixed name
        authRepository = new AuthRepository(this, api);
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }
}
