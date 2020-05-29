package xyz.spedcord.server.util;

public class MySqlUtil {

    private MySqlUtil() {
    }

    public static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\0", "\\0")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\"", "\\\"")
                .replace("\\x1a", "\\Z");
    }

}
