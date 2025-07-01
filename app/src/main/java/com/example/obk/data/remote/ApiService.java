package com.example.obk.data.remote;



import com.example.obk.data.local.entity.Charity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    class CheckoutRequest {
        public String charityId;
        public List<String> toteIds;
        public long timestamp;
        public String photo; // optional

        public CheckoutRequest(String charityId, List<String> toteIds, long timestamp, String photo) {
            this.charityId = charityId;
            this.toteIds = toteIds;
            this.timestamp = timestamp;
            this.photo = photo;
        }
    }

    @GET("charities")
    Call<List<Charity>> getCharities();

    @POST("checkout")
    Call<Void> submitCheckout(@Body CheckoutRequest request);
}