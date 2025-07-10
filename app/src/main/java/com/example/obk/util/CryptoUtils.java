package com.example.obk.util;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 负责把服务器第一次 401 返回的 challenge 计算成 userChallenge。
 *
 * 协议：
 *   ① challenge(Base64URL) --decode--> rawBytes
 *   ② HMAC-SHA256(rawBytes, key = password UTF-8)
 *   ③ 再 Base64URL(NO_WRAP) 编码，得到 userChallenge
 */
public final class CryptoUtils {

    private CryptoUtils() { }

    /**
     * @param rawChallenge  服务器 401 body 里的字符串
     * @param password      用户输入的明文密码
     * @return   userChallenge，可直接放到  ?userChallenge= 里
     */
    public static String solveChallenge(String rawChallenge,
                                        String password) throws Exception {

        // 去掉引号 / 空白
        String c = rawChallenge.replace("\"", "").trim();

        // 1) Base64URL → bytes
        byte[] challengeBytes = Base64.decode(
                c, Base64.URL_SAFE | Base64.NO_WRAP);

        // 2) HMAC-SHA256(challengeBytes, passwordBytes)
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                password.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"));
        byte[] digest = mac.doFinal(challengeBytes);

        // 3) Base64URL(NO_WRAP)，保留 '=' padding
        return Base64.encodeToString(
                digest, Base64.URL_SAFE | Base64.NO_WRAP);
    }
}
