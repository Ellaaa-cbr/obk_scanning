package com.example.obk.auth;

import com.google.gson.annotations.SerializedName;

public class UserDetail {
    @SerializedName("id")          public String id;
    @SerializedName("username")    public String username;
    @SerializedName("email")       public String email;
    @SerializedName("validUntil")  public String validUntil;

}
