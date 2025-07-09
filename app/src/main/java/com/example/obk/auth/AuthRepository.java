package com.example.obk.auth;

import android.content.Context;
import androidx.annotation.WorkerThread;

import com.example.obk.network.ApiService;
import com.example.obk.network.dto.AuthenticateRequest;
import com.example.obk.network.dto.ChallengeRequest;
import com.example.obk.network.dto.ChallengeResponse;
import com.example.obk.network.dto.TokenResponse;
import com.example.obk.network.dto.UserDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class AuthRepository {
    private final ApiService api;
    private final TokenStore tokenStore;
    private final Executor io = Executors.newSingleThreadExecutor();

    public AuthRepository(Context ctx, ApiService api) {
        this.api = api;
        this.tokenStore = new TokenStore(ctx);
    }

    public CompletableFuture<UserDto> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                /* 1) 请求 challenge */
                Response<ChallengeResponse> ch = api.getChallenge(
                        new ChallengeRequest(username)).execute();
                if (!ch.isSuccessful() || ch.body() == null)
                    throw new IllegalStateException("Challenge failed – " + ch.code());

                /* 2) 求解 challenge */
                String answer = ChallengeSolver.solve(ch.body().challenge, password);

                /* 3) 换取 JWT */
                Response<TokenResponse> tk = api.authenticate(
                        new AuthenticateRequest(username, answer)).execute();
                if (!tk.isSuccessful() || tk.body() == null)
                    throw new IllegalStateException("Auth failed – " + tk.code());

                String jwt = tk.body().token;
                tokenStore.save(jwt);

                /* 4) 验证 & 取当前用户信息（可选） */
                Response<UserDto> me = api.getUser("Bearer " + jwt).execute();
                if (!me.isSuccessful() || me.body() == null)
                    throw new IllegalStateException("User fetch failed – " + me.code());

                return me.body();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, io);
    }

    @WorkerThread
    public boolean hasValidToken() {
        return tokenStore.getToken() != null && !tokenStore.isExpired();
    }
}
