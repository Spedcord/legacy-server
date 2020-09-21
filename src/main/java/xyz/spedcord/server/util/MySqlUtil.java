package xyz.spedcord.server.util;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
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
