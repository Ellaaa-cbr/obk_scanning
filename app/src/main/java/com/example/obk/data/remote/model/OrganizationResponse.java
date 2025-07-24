package com.example.obk.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrganizationResponse {
    @SerializedName("data")
    public List<OrganizationDto> data;
}
