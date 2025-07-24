package com.example.obk.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class OrganizationDto {
    @SerializedName("OrganizationId")         public String id;
    @SerializedName("Name")                   public String name;
    @SerializedName("OrganizationTypeId")     public int    typeId;  // 新增字段
    // …其余字段可以按需加入
}
