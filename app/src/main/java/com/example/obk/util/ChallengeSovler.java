package com.example.obk.util;



public class ChallengeSovler {
    public static String solveUserChallenge(String serverChallengeB64, String password) throws Exception {
        MD5 md5 = new MD5();
        RC6 rc6 = new RC6();

        String userChallengeAnswer = Base64.decode(serverChallengeB64);

        rc6.setup(md5.digest(password));

        String userSecret = rc6.decrypt(userChallengeAnswer.substring(16));

        rc6.setup(userSecret);

        String prefix     = userChallengeAnswer.substring(0, 16);  // 等价于 C# 的[..16]
        String encrypted  = rc6.encrypt(prefix);

        return Base64.encode(prefix + encrypted);

    }

}
