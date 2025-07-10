package com.example.obk.auth;

import com.google.gson.annotations.SerializedName;

public class UserDetail {
    @SerializedName("id")          public String id;
    @SerializedName("username")    public String username;
    @SerializedName("email")       public String email;
    @SerializedName("validUntil")  public String validUntil; // 如果是时间戳可改 long

    // 也可以加 getter/setter，这里偷懒直接用公有字段
}
