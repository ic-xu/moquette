package io.moquette.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static int strToHashKey(String k) {
        int tmpKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(k.getBytes());
            tmpKey =Integer.parseInt(bytesToHexString(mDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            tmpKey = k.hashCode();
        }
        return tmpKey;
    }

    private static String bytesToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(0xFF & b[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();

    }
}
