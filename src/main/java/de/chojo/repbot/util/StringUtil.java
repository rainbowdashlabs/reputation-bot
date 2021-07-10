package de.chojo.repbot.util;

public class StringUtil {
    public static boolean contains(String cmd, String... values) {
        for (var value : values) {
            if (cmd.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
