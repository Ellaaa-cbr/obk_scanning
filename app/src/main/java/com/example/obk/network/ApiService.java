package com.example.obk.network;

import com.example.obk.network.dto.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    /* ---------------- Authentication ---------------- */
    @POST("auth/challenge")
    Call<ChallengeResponse> getChallenge(@Body ChallengeRequest request);

    @POST("auth/authenticate")
    Call<TokenResponse> authenticate(@Body AuthenticateRequest request);

    @GET("user")
    Call<UserDto> getUser(@Header("Authorization") String bearerToken);

    /* --------------- Domain Endpoints --------------- */

    @GET("charities") // alias for organizations used in OBK scanner UI
    Call<List<CharityDto>> getCharities();

    @POST("checkout")
    Call<Void> submitCheckout(@Body CheckoutRequest request);

    @POST("authentication/login")
    Call<TokenResponse> login(@Body AuthenticateRequest body);
}