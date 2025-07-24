package com.example.obk.auth;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

public final class TokenManager {
    private static final String KEY = "jwt";
    public static void save(Context c, String t) {
        c.getSharedPreferences("auth", MODE_PRIVATE).edit().putString(KEY, t).apply();
    }
    public static String get(Context c) {
        return c.getSharedPreferences("auth", MODE_PRIVATE).getString(KEY, null);
    }
}
