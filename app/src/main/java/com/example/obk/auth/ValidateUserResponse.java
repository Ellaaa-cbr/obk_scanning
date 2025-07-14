package com.example.obk.auth;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ValidateUserResponse {

    @SerializedName("data")
    public List<TenantInfo> data;

    @SerializedName("success")
    public boolean success;
}