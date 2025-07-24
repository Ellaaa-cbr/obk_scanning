    package com.example.obk.data.remote;

    import com.example.obk.auth.TokenResponse;
    import com.example.obk.auth.UserDetail;
    import com.example.obk.auth.ValidateUserResponse;
    import com.example.obk.data.local.entity.Charity;
    import com.example.obk.data.remote.model.CheckoutRequest;
    import com.example.obk.data.remote.model.OrganizationDto;
    import com.example.obk.data.remote.model.OrganizationResponse;
    import java.util.List;

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
        Call<String> login(
                @Path("tenant") String tenant,
                @Query("userId") String userId,
                @Query(value = "userChallenge") String userChallenge);

        @POST("{tenant}/api/user/byToken")
        Call<UserDetail> getUserByToken(
                @Path("tenant") String tenant,
                @Body RequestBody tokenBody);

        @GET("Organization")
        Call<OrganizationResponse> getOrganizations(
                @Query("OrganizationTypeId") int typeId
        );

        @POST("User/Checkout")
        Call<ResponseBody> checkout(@Body CheckoutRequest body);


    }
