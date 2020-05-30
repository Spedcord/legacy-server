package xyz.spedcord.server.util;

import java.util.Random;

public class StringUtil {

    private StringUtil() {
    }

    public static String generateKey(int len) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < len; i++) {
            stringBuilder.append(chars.toCharArray()[random.nextInt(chars.length())]);
        }
        return stringBuilder.toString();
    }

}
