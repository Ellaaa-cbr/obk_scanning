package com.example.obk.util;



public class aaa {
    public static String solveUserChallenge(String serverChallengeB64, String password) throws Exception {
        MD5 md5 = new MD5();
        RC6 rc6 = new RC6();

        String userChallengeAnswer = Base64.decode(serverChallengeB64);

        /* 2) 用 MD5( password ) 初始化 RC6 */
        rc6.setup(md5.digest(password));   // digest() 返回 32 字节十六进制

        /* 3) 解密挑战数据后半部分 (从第 16 字节开始) 取得 userSecret */
        String userSecret = rc6.decrypt(userChallengeAnswer.substring(16));

        /* 4) 用 userSecret 重新初始化 RC6 */
        rc6.setup(userSecret);

        /* 5) 把前 16 字节作为明文用 RC6 加密，再与前缀拼接并做 Base64(URL-safe) 编码 */
        String prefix     = userChallengeAnswer.substring(0, 16);  // 等价于 C# 的[..16]
        String encrypted  = rc6.encrypt(prefix);

        return Base64.encode(prefix + encrypted);

    }

}
