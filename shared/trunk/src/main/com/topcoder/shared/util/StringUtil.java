package com.topcoder.shared.util;

public final class StringUtil {

    private StringUtil() {
    }

    public static String doubleQuote(Object object) {
        return quote(object.toString(), '"');
    }

    private static String quote(String s, char c) {
        return c + s + c;
    }

}
