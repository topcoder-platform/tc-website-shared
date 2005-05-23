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

    public static String padLeft(String s, int len) {
        return padLeft(s, len, ' ');
    }

    // Returns the input string padded on the left by characters to fit the given length.
    public static String padLeft(String s, int len, char c) {
        int pad_len = len - s.length();
        if (pad_len <= 0) return s;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < pad_len; i++)
            buf.append(c);
        buf.append(s);
        return buf.toString();
    }

    public static String padRight(String s, int len) {
        return padRight(s, len, ' ');
    }

    // Returns the input string padded on the right by characters to fit the given length.
    public static String padRight(String s, int len, char c) {
        int pad_len = len - s.length();
        if (pad_len <= 0) return s;
        StringBuffer buf = new StringBuffer();
        buf.append(s);
        for (int i = 0; i < pad_len; i++)
            buf.append(c);
        return buf.toString();
    }


    /**
     * Change all occurrances of <code>changeFrom</code> in <code>original</code>
     * to <code>changeTo</code>
     * @param original
     * @param changeFrom
     * @param changeTo
     * @return
     */
    public static String replace(String original, String changeFrom, String changeTo) {
        if (original == null) {
            throw new IllegalArgumentException("the original string was null");
        } else if (changeFrom == null) {
            throw new IllegalArgumentException("the changeFrom string was null");
        } else if (changeTo == null) {
            throw new IllegalArgumentException("the changeTo string was null");
        } else if (changeFrom.length() == 0) {
            throw new IllegalArgumentException("the changeFrom string was empty");
        }

        int dif = changeTo.length() - changeFrom.length();
        StringBuffer ret = new StringBuffer(original.length() + (dif > 0 ? dif * 5 : 0));
        for (int i = 0; i < original.length(); i++) {
            if (i <= original.length() - changeFrom.length()) {
                if (original.substring(i, i + changeFrom.length()).equals(changeFrom)) {
//                    System.out.println("if1: " + i);
                    ret.append(changeTo);
                    i += (changeFrom.length() - 1);
                } else {
//                    System.out.println("else1: " + i);
                    ret.append(original.charAt(i));
                }
            } else {
//                System.out.println("else2: " + i);
                ret.append(original.charAt(i));
            }
        }

        return ret.toString();
    }


}
