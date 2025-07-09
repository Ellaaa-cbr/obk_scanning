package com.example.obk.network.dto;

import java.util.List;

public class CheckoutRequest {
    public String charityId;
    public List<String> toteIds;
    public long timestamp;
    public String photo; // Base64 or null

    public CheckoutRequest(String charityId, List<String> toteIds, long timestamp, String photo) {
        this.charityId = charityId;
        this.toteIds   = toteIds;
        this.timestamp = timestamp;
        this.photo     = photo;
    }
}
