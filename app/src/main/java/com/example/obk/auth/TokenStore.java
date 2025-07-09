package com.example.obk.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class TokenStore {
    private static final String PREFS = "auth_prefs";
    private static final String KEY   = "jwt_token";
    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(String token) { sp.edit().putString(KEY, token).apply(); }
    public String getToken()      { return sp.getString(KEY, null); }
    public void clear()           { sp.edit().remove(KEY).apply(); }

    public boolean isExpired() {
        String jwt = getToken();
        if (jwt == null) return true;
        try {
            String[] parts = jwt.split("\\.");
            String payload = new String(android.util.Base64.decode(parts[1], Base64.URL_SAFE));
            long exp = new org.json.JSONObject(payload).optLong("exp", 0);
            return System.currentTimeMillis() / 1000L >= exp;
        } catch (Exception e) {
            return true;     // 无法解析时视为过期
        }
    }
}