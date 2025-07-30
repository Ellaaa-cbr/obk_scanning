package com.example.obk.util;

public class Base64 {

    private static final String KEY_STR =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";


    public static String encode(String input) {
        StringBuilder output = new StringBuilder();
        int i = 0;

        while (i < input.length()) {
            int chr1 = (byte) input.charAt(i++) & 0xFF;
            int enc1, enc2, enc3, enc4;
            int chr2 = 0, chr3 = 0;

            enc1 = chr1 >> 2;

            if (i < input.length()) {
                chr2 = (byte) input.charAt(i++) & 0xFF;
                enc2 = ((chr1 & 0x3) << 4) | (chr2 >> 4);

                if (i < input.length()) {
                    chr3 = (byte) input.charAt(i++) & 0xFF;
                    enc3 = ((chr2 & 0xF) << 2) | (chr3 >> 6);
                    enc4 = chr3 & 0x3F;
                } else {
                    enc3 = ((chr2 & 0xF) << 2);
                    enc4 = 64; // '='
                }
            } else {
                enc2 = ((chr1 & 0x3) << 4);
                enc3 = enc4 = 64; // '='
            }

            output.append(KEY_STR.charAt(enc1))
                    .append(KEY_STR.charAt(enc2))
                    .append(KEY_STR.charAt(enc3))
                    .append(KEY_STR.charAt(enc4));
        }


        return output.toString().replace('+', '-').replace('/', '_');
    }


    public static String decode(String input) {

        String normalized = input.replace('_', '/')
                .replace('-', '+')
                .replaceAll("[^A-Za-z0-9+/=]", "");

        StringBuilder output = new StringBuilder();
        int i = 0;

        while (i < normalized.length()) {
            int enc1 = KEY_STR.indexOf(normalized.charAt(i++));
            int enc2 = KEY_STR.indexOf(normalized.charAt(i++));
            int enc3 = KEY_STR.indexOf(normalized.charAt(i++));
            int enc4 = KEY_STR.indexOf(normalized.charAt(i++));

            int chr1 = (enc1 << 2) | (enc2 >> 4);
            int chr2 = ((enc2 & 0xF) << 4) | (enc3 >> 2);
            int chr3 = ((enc3 & 0x3) << 6) | enc4;

            output.append((char) chr1);

            if (enc3 != 64) {
                output.append((char) chr2);
            }
            if (enc4 != 64) {
                output.append((char) chr3);
            }
        }
        return output.toString();
    }
}
