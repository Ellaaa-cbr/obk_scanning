package com.example.obk.util;

import java.util.Random;

public class RC6 {


    private static final Random rand = new Random(System.currentTimeMillis());
    private final int[] sKey = new int[44];
    private static final int BLOCK_SIZE = 16;

    /* ---------- 内部工具函数（完全等价） ---------- */

    private static int MAX(int x, int y) { return x > y ? x : y; }
    private static int MIN(int x, int y) { return x < y ? x : y; }

    /** 字节序反转（与 C# 的 BSWAP 相同） */
    private static int BSWAP(int x) {
        return ((x >>> 24) & 0x000000FF) |
                ((x << 24)  & 0xFF000000) |
                ((x >>> 8)  & 0x0000FF00) |
                ((x << 8)   & 0x00FF0000);
    }

    /** 32-bit 左循环 */
    private static int ROL(int x, int y) {
        int n = y & 31;
        return (x << n) | (x >>> (32 - n));
    }

    /** 32-bit 右循环 */
    private static int ROR(int x, int y) {
        int n = y & 31;
        return (x >>> n) | (x << (32 - n));
    }

    /** 把 32-bit 整数写成 4 字节字符串（低字节在前） */
    private static String STORE32(int x) {
        return new String(new char[] {
                (char) (x       & 0xFF),
                (char) (x >>> 8 & 0xFF),
                (char) (x >>>16 & 0xFF),
                (char) (x >>>24 & 0xFF)
        });
    }

    /** 逐字节相加（与原 C# Add32 逐字节带进位的写法保持一致） */
    private static int ADD32(int x, int y) {
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res += (((x >>> (i * 8)) & 0xFF) + ((y >>> (i * 8)) & 0xFF)) << (i * 8);
        }
        return res;
    }

    private static int SUB32(int x, int y) { return ADD32(x, ADD32(~y, 1)); }

    /** 多项式乘法的逐字节模拟（与 C# 完全一致） */
    private static int MUL32(int x, int y) {
        int res = 0;
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = 0; ix < (4 - iy); ix++) {
                res += (((x >>> (ix * 8)) & 0xFF) * ((y >>> (iy * 8)) & 0xFF)) << ((ix + iy) * 8);
            }
        }
        return res;
    }

    /** 从字符串第 w 个 32-bit 位置取 4 字节小端整数 */
    private static int LOAD32(String s, int w) {
        int base = w * 4;
        return  (s.charAt(base    ) & 0xFF)        |
                ((s.charAt(base + 1) & 0xFF) << 8 ) |
                ((s.charAt(base + 2) & 0xFF) << 16) |
                ((s.charAt(base + 3) & 0xFF) << 24);
    }

    /* ---------- 密钥扩展 ---------- */

    public void setup(String key) {
        int[] L = new int[(key.length() + 3) / 4];   // 原始密钥分组
        int[] S = new int[44];                       // 20 轮 ×2 +4

        int A = 0, j = 0;
        for (int i = 0; i < key.length(); ) {
            A = (A << 8) | (key.charAt(i++) & 0xFF);
            if ((i & 3) == 0) {                      // 每 4 字节填入一词
                L[j++] = BSWAP(A);
                A = 0;
            }
        }
        if ((key.length() & 3) != 0) {               // 处理非 4 的整倍数
            A <<= 8 * (4 - (key.length() & 3));
            L[j++] = BSWAP(A);
        }

        /* 初始化 S 表 */
        S[0] = ADD32(0xB7E15163, 0);
        for (int i = 1; i < 44; i++)
            S[i] = ADD32(S[i - 1], 0x9E3779B9);

        /* 混合密钥与 S */
        int s = 3 * MAX(44, j);
        int Areg = 0, Breg = 0, i = 0;
        for (int v = 0, k = j; v < s; v++) {
            Areg = S[i] = ROL(ADD32(ADD32(S[i], Areg), Breg), 3);
            Breg = L[k % j] = ROL(ADD32(ADD32(L[k % j], Areg), Breg), ADD32(Areg, Breg));
            i = (i + 1) % 44;
            k++;
        }
        System.arraycopy(S, 0, this.sKey, 0, 44);
    }

    /* ---------- 加 / 解密核心 ---------- */

    private String coreCrypt(String block, boolean decipher) {
        int a = LOAD32(block, 0),
                b = LOAD32(block, 1),
                c = LOAD32(block, 2),
                d = LOAD32(block, 3),
                t, u;

        if (decipher) {
            a = SUB32(a, sKey[42]);
            c = SUB32(c, sKey[43]);

            for (int r = 19; r >= 0; r--) {
                int temp = d;  d = c;  c = b;  b = a;  a = temp;

                t = ROL(MUL32(b, ADD32(ADD32(b, b), 1)), 5);
                u = ROL(MUL32(d, ADD32(ADD32(d, d), 1)), 5);

                c = ROR(SUB32(c, sKey[r * 2 + 3]), t) ^ u;
                a = ROR(SUB32(a, sKey[r * 2 + 2]), u) ^ t;
            }

            b = SUB32(b, sKey[0]);
            d = SUB32(d, sKey[1]);

        } else {
            b = ADD32(b, sKey[0]);
            d = ADD32(d, sKey[1]);

            for (int r = 0; r < 20; r++) {
                t = ROL(MUL32(b, ADD32(ADD32(b, b), 1)), 5);
                u = ROL(MUL32(d, ADD32(ADD32(d, d), 1)), 5);

                a = ADD32(ROL(a ^ t, u), sKey[r * 2 + 2]);
                c = ADD32(ROL(c ^ u, t), sKey[r * 2 + 3]);

                int temp = a;  a = b;  b = c;  c = d;  d = temp;
            }

            a = ADD32(a, sKey[42]);
            c = ADD32(c, sKey[43]);
        }

        return STORE32(a) + STORE32(b) + STORE32(c) + STORE32(d);
    }

    /* ---------- 公共 API ---------- */

    public String encrypt(String data) {
        StringBuilder sb = new StringBuilder(data);
        while (sb.length() % BLOCK_SIZE != 0) sb.append('\0');  // '\0' 填充
        StringBuilder out = new StringBuilder(sb.length());

        for (int i = 0; i < sb.length(); i += BLOCK_SIZE) {
            out.append(coreCrypt(sb.substring(i, i + BLOCK_SIZE), false));
        }
        return out.toString();
    }

    public String encrypt(String key, String data) {
        setup(key);
        return encrypt(data);
    }

    public String decrypt(String data) {
        StringBuilder out = new StringBuilder(data.length());
        for (int i = 0; i < data.length(); i += BLOCK_SIZE)
            out.append(coreCrypt(data.substring(i, i + BLOCK_SIZE), true));

        /* 去掉 '\0' 填充（与原逻辑一致） */
        while (out.length() > BLOCK_SIZE && out.charAt(out.length() - 1) == '\0')
            out.deleteCharAt(out.length() - 1);

        return out.toString();
    }

    public String decrypt(String key, String data) {
        setup(key);
        return decrypt(data);
    }

    /* ---------- 其它工具函数 ---------- */

    public String randomValue(int size) {
        StringBuilder sb = new StringBuilder(size);
        while (size-- > 0) sb.append((char) (rand.nextInt() & 0xFF));
        return sb.toString();
    }

    public String generatePassword(int size) {
        final String chars = "!QqAaZz@2WwSsXx3EeDdCc$4RrFfVv5TtGgBb6YyHhNn7UuJjMm*8IiKk9OoLl0Pp";
        StringBuilder sb = new StringBuilder(size);
        while (size-- > 0) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }

    public String generateNumbers(int size) {
        final String digits = "0123456789";
        StringBuilder sb = new StringBuilder(size);
        while (size-- > 0) sb.append(digits.charAt(rand.nextInt(digits.length())));
        return sb.toString();
    }
}
