package com.example.obk.data.remote;

import com.example.obk.auth.TokenResponse;
import com.example.obk.auth.UserDetail;
import com.example.obk.auth.ValidateUserResponse;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("{tenant}/api/user/validateUser")
    Call<ValidateUserResponse> validateUser(
            @Path("tenant") String tenant,
            @Query("userData") String email);

    @GET("{tenant}/api/login/")
    Call<ResponseBody> getLogin(
            @Path("tenant") String tenant,
            @Query("userId") String userId);

    @GET("{tenant}/api/login/")
    Call<TokenResponse> login(
            @Path("tenant") String tenant,
            @Query("userId") String userId,
            @Query(value = "userChallenge", encoded = true) String userChallenge);

    @POST("{tenant}/api/user/byToken")
    Call<UserDetail> getUserByToken(
            @Path("tenant") String tenant,
            @Body RequestBody tokenBody);
}
