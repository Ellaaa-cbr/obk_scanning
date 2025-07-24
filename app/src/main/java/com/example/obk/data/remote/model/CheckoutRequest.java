package com.example.obk.data.remote.model;

import java.util.List;

public class CheckoutRequest {
    public String charityId;
    public List<ToteItem> totes;
    public String photoBase64;
    public long   timestamp;
}
