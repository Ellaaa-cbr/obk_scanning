package com.example.obk.auth;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Implements the RC6 + MD5 challenge‑response algorithm used by Qairos server.
 * Mirrors the C# logic shown in your original Blazor code.
 */
public class ChallengeSolver {

    public static String solve(String serverChallengeB64, String password) {
        byte[] challengeBytes = Base64.getDecoder().decode(serverChallengeB64);

        // 1) MD5(password)
        byte[] pwdHash;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            pwdHash = md5.digest(password.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("MD5 not supported", e);
        }

        // 2) RC6 decrypt (bytes[16..]) with pwdHash as key
        RC6 rc6 = new RC6(pwdHash);
        byte[] userSecret = rc6.decrypt(challengeBytes, 16, challengeBytes.length - 16);

        // 3) RC6 encrypt (bytes[0..15]) with userSecret
        rc6 = new RC6(userSecret);
        byte[] first16 = rc6.encrypt(challengeBytes, 0, 16);

        // 4) concat & Base64
        byte[] answer = new byte[challengeBytes.length];
        System.arraycopy(challengeBytes, 0, answer, 0, 16);
        System.arraycopy(first16, 0, answer, 16, 16);
        return Base64.getEncoder().encodeToString(answer);
    }

    /* -------------------- Minimal RC6 implementation -------------------- */
    private static class RC6 {
        private static final int WORD_SIZE = 32; // bits
        private static final int ROUNDS = 20;
        private static final int BLOCK_BYTES = 16;
        private final int[] S = new int[2 * ROUNDS + 4];

        RC6(byte[] key16) { // expect 16‑byte key
            // Key schedule – truncated for brevity but good enough for 16B key
            int Pw = 0xB7E15163, Qw = 0x9E3779B9;
            int c = key16.length / 4;
            int[] L = new int[c];
            ByteBuffer.wrap(key16).asIntBuffer().get(L);
            S[0] = Pw;
            for (int i = 1; i < S.length; i++) S[i] = S[i - 1] + Qw;
            int A = 0, B = 0, i = 0, j = 0, v = 3 * Math.max(S.length, c);
            for (int k = 0; k < v; k++) {
                A = S[i] = Integer.rotateLeft(S[i] + A + B, 3);
                B = L[j] = Integer.rotateLeft(L[j] + A + B, (A + B));
                i = (i + 1) % S.length;
                j = (j + 1) % c;
            }
        }

        byte[] encrypt(byte[] src, int off, int len) {
            byte[] out = new byte[len];
            System.arraycopy(src, off, out, 0, len);
            ByteBuffer buf = ByteBuffer.wrap(out);
            int A = buf.getInt(0), B = buf.getInt(4), C = buf.getInt(8), D = buf.getInt(12);
            B += S[0];
            D += S[1];
            for (int i = 1; i <= ROUNDS; i++) {
                int t = Integer.rotateLeft(B * (2 * B + 1), 5);
                int u = Integer.rotateLeft(D * (2 * D + 1), 5);
                A = Integer.rotateLeft(A ^ t, u) + S[2 * i];
                C = Integer.rotateLeft(C ^ u, t) + S[2 * i + 1];
                int tmp = A; A = B; B = C; C = D; D = tmp;
            }
            A += S[2 * ROUNDS + 2];
            C += S[2 * ROUNDS + 3];
            buf.putInt(0, A);
            buf.putInt(4, B);
            buf.putInt(8, C);
            buf.putInt(12, D);
            return out;
        }

        byte[] decrypt(byte[] src, int off, int len) {
            byte[] out = new byte[len];
            System.arraycopy(src, off, out, 0, len);
            ByteBuffer buf = ByteBuffer.wrap(out);
            int A = buf.getInt(0), B = buf.getInt(4), C = buf.getInt(8), D = buf.getInt(12);
            C -= S[2 * ROUNDS + 3];
            A -= S[2 * ROUNDS + 2];
            for (int i = ROUNDS; i >= 1; i--) {
                int tmp = D; D = C; C = B; B = A; A = tmp;
                int u = Integer.rotateLeft(D * (2 * D + 1), 5);
                int t = Integer.rotateLeft(B * (2 * B + 1), 5);
                C = Integer.rotateRight(C - S[2 * i + 1], t) ^ u;
                A = Integer.rotateRight(A - S[2 * i], u) ^ t;
            }
            D -= S[1];
            B -= S[0];
            buf.putInt(0, A);
            buf.putInt(4, B);
            buf.putInt(8, C);
            buf.putInt(12, D);
            return out;
        }
    }
}