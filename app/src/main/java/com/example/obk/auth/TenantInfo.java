package com.example.obk.auth;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TenantInfo {
    @SerializedName("Name")
    public String name;              // "QairosWorsley"

    @SerializedName("Alias")
    public String alias;             // "Worsley"

    @SerializedName("Address")
    public String address;

    @SerializedName("Logo")
    public String logo;

    @SerializedName("DisplayNames")
    public List<String> displayNames;
}
