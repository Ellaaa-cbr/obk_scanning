package com.example.obk.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class OrganizationDto {
    @SerializedName("OrganizationId")         public String id;
    @SerializedName("Name")                   public String name;
    @SerializedName("OrganizationTypeId")     public int    typeId;  // charity: type = 4

}
