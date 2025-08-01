package com.example.obk.util;

public class MD5 {


    private static int rotateLeft(int val, int shift) {
        return (val << shift) | (val >>> (32 - shift));
    }

    private static int addUnsigned(int x, int y) {
        long xl = x & 0xFFFFFFFFL;
        long yl = y & 0xFFFFFFFFL;
        return (int) ((xl + yl) & 0xFFFFFFFFL);
    }


    private static int F(int x, int y, int z) { return (x & y) | (~x & z); }
    private static int G(int x, int y, int z) { return (x & z) | (y & ~z); }
    private static int H(int x, int y, int z) { return x ^ y ^ z; }
    private static int I(int x, int y, int z) { return y ^ (x | ~z); }


    private static int FF(int a, int b, int c, int d, int x, int s, int ac) {
        a = addUnsigned(a, addUnsigned(addUnsigned(F(b, c, d), x), ac));
        return addUnsigned(rotateLeft(a, s), b);
    }
    private static int GG(int a, int b, int c, int d, int x, int s, int ac) {
        a = addUnsigned(a, addUnsigned(addUnsigned(G(b, c, d), x), ac));
        return addUnsigned(rotateLeft(a, s), b);
    }
    private static int HH(int a, int b, int c, int d, int x, int s, int ac) {
        a = addUnsigned(a, addUnsigned(addUnsigned(H(b, c, d), x), ac));
        return addUnsigned(rotateLeft(a, s), b);
    }
    private static int II(int a, int b, int c, int d, int x, int s, int ac) {
        a = addUnsigned(a, addUnsigned(addUnsigned(I(b, c, d), x), ac));
        return addUnsigned(rotateLeft(a, s), b);
    }


    private static int[] convertToWordArray(String msg) {
        int msgLen = msg.length();
        long bitLen = (long) msgLen << 3;

        int totalWords = (((msgLen + 8) / 64) + 1) * 16;
        int[] wordArray = new int[totalWords];

        for (int i = 0; i < msgLen; i++) {
            int wordIndex = i >>> 2;
            int bytePos   = (i & 3) << 3;
            wordArray[wordIndex] |= (msg.charAt(i) & 0xFF) << bytePos;
        }

        int wordIndex = msgLen >>> 2;
        int bytePos   = (msgLen & 3) << 3;
        wordArray[wordIndex] |= 0x80 << bytePos;

        wordArray[totalWords - 2] = (int) bitLen;
        wordArray[totalWords - 1] = (int) (bitLen >>> 32);

        return wordArray;
    }


    private static String wordToString(int val) {
        char[] bytes = new char[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (char) ((val >>> (i * 8)) & 0xFF);
        }
        return new String(bytes);
    }


    public String digest(String message) {

        final int S11 =  7, S12 = 12, S13 = 17, S14 = 22;
        final int S21 =  5, S22 =  9, S23 = 14, S24 = 20;
        final int S31 =  4, S32 = 11, S33 = 16, S34 = 23;
        final int S41 =  6, S42 = 10, S43 = 15, S44 = 21;

        int[] x = convertToWordArray(message);

        int a = 0x67452301;
        int b = 0xEFCDAB89;
        int c = 0x98BADCFE;
        int d = 0x10325476;

        for (int k = 0; k < x.length; k += 16) {
            int AA = a, BB = b, CC = c, DD = d;

            /* --- Round 1 --- */
            a = FF(a, b, c, d, x[k +  0], S11, 0xD76AA478);
            d = FF(d, a, b, c, x[k +  1], S12, 0xE8C7B756);
            c = FF(c, d, a, b, x[k +  2], S13, 0x242070DB);
            b = FF(b, c, d, a, x[k +  3], S14, 0xC1BDCEEE);
            a = FF(a, b, c, d, x[k +  4], S11, 0xF57C0FAF);
            d = FF(d, a, b, c, x[k +  5], S12, 0x4787C62A);
            c = FF(c, d, a, b, x[k +  6], S13, 0xA8304613);
            b = FF(b, c, d, a, x[k +  7], S14, 0xFD469501);
            a = FF(a, b, c, d, x[k +  8], S11, 0x698098D8);
            d = FF(d, a, b, c, x[k +  9], S12, 0x8B44F7AF);
            c = FF(c, d, a, b, x[k + 10], S13, 0xFFFF5BB1);
            b = FF(b, c, d, a, x[k + 11], S14, 0x895CD7BE);
            a = FF(a, b, c, d, x[k + 12], S11, 0x6B901122);
            d = FF(d, a, b, c, x[k + 13], S12, 0xFD987193);
            c = FF(c, d, a, b, x[k + 14], S13, 0xA679438E);
            b = FF(b, c, d, a, x[k + 15], S14, 0x49B40821);

            /* --- Round 2 --- */
            a = GG(a, b, c, d, x[k +  1], S21, 0xF61E2562);
            d = GG(d, a, b, c, x[k +  6], S22, 0xC040B340);
            c = GG(c, d, a, b, x[k + 11], S23, 0x265E5A51);
            b = GG(b, c, d, a, x[k +  0], S24, 0xE9B6C7AA);
            a = GG(a, b, c, d, x[k +  5], S21, 0xD62F105D);
            d = GG(d, a, b, c, x[k + 10], S22, 0x02441453);
            c = GG(c, d, a, b, x[k + 15], S23, 0xD8A1E681);
            b = GG(b, c, d, a, x[k +  4], S24, 0xE7D3FBC8);
            a = GG(a, b, c, d, x[k +  9], S21, 0x21E1CDE6);
            d = GG(d, a, b, c, x[k + 14], S22, 0xC33707D6);
            c = GG(c, d, a, b, x[k +  3], S23, 0xF4D50D87);
            b = GG(b, c, d, a, x[k +  8], S24, 0x455A14ED);
            a = GG(a, b, c, d, x[k + 13], S21, 0xA9E3E905);
            d = GG(d, a, b, c, x[k +  2], S22, 0xFCEFA3F8);
            c = GG(c, d, a, b, x[k +  7], S23, 0x676F02D9);
            b = GG(b, c, d, a, x[k + 12], S24, 0x8D2A4C8A);

            /* --- Round 3 --- */
            a = HH(a, b, c, d, x[k +  5], S31, 0xFFFA3942);
            d = HH(d, a, b, c, x[k +  8], S32, 0x8771F681);
            c = HH(c, d, a, b, x[k + 11], S33, 0x6D9D6122);
            b = HH(b, c, d, a, x[k + 14], S34, 0xFDE5380C);
            a = HH(a, b, c, d, x[k +  1], S31, 0xA4BEEA44);
            d = HH(d, a, b, c, x[k +  4], S32, 0x4BDECFA9);
            c = HH(c, d, a, b, x[k +  7], S33, 0xF6BB4B60);
            b = HH(b, c, d, a, x[k + 10], S34, 0xBEBFBC70);
            a = HH(a, b, c, d, x[k + 13], S31, 0x289B7EC6);
            d = HH(d, a, b, c, x[k +  0], S32, 0xEAA127FA);
            c = HH(c, d, a, b, x[k +  3], S33, 0xD4EF3085);
            b = HH(b, c, d, a, x[k +  6], S34, 0x04881D05);
            a = HH(a, b, c, d, x[k +  9], S31, 0xD9D4D039);
            d = HH(d, a, b, c, x[k + 12], S32, 0xE6DB99E5);
            c = HH(c, d, a, b, x[k + 15], S33, 0x1FA27CF8);
            b = HH(b, c, d, a, x[k +  2], S34, 0xC4AC5665);

            /* --- Round 4 --- */
            a = II(a, b, c, d, x[k +  0], S41, 0xF4292244);
            d = II(d, a, b, c, x[k +  7], S42, 0x432AFF97);
            c = II(c, d, a, b, x[k + 14], S43, 0xAB9423A7);
            b = II(b, c, d, a, x[k +  5], S44, 0xFC93A039);
            a = II(a, b, c, d, x[k + 12], S41, 0x655B59C3);
            d = II(d, a, b, c, x[k +  3], S42, 0x8F0CCC92);
            c = II(c, d, a, b, x[k + 10], S43, 0xFFEFF47D);
            b = II(b, c, d, a, x[k +  1], S44, 0x85845DD1);
            a = II(a, b, c, d, x[k +  8], S41, 0x6FA87E4F);
            d = II(d, a, b, c, x[k + 15], S42, 0xFE2CE6E0);
            c = II(c, d, a, b, x[k +  6], S43, 0xA3014314);
            b = II(b, c, d, a, x[k + 13], S44, 0x4E0811A1);
            a = II(a, b, c, d, x[k +  4], S41, 0xF7537E82);
            d = II(d, a, b, c, x[k + 11], S42, 0xBD3AF235);
            c = II(c, d, a, b, x[k +  2], S43, 0x2AD7D2BB);
            b = II(b, c, d, a, x[k +  9], S44, 0xEB86D391);

            a = addUnsigned(a, AA);
            b = addUnsigned(b, BB);
            c = addUnsigned(c, CC);
            d = addUnsigned(d, DD);
        }

        String raw = wordToString(a) + wordToString(b) +
                wordToString(c) + wordToString(d);

        StringBuilder hex = new StringBuilder(32);
        for (int i = 0; i < raw.length(); i++) {
            int v = raw.charAt(i) & 0xFF;
            hex.append(String.format("%02X", v));
        }
        return hex.toString();
    }


}
