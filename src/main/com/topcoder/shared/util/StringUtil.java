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

    public static String padLeft(String s, int len)
    {
    	return padLeft(s, len, ' ');
    }
    
    // Returns the input string padded on the left by characters to fit the given length.
    public static String padLeft(String s, int len, char c)
    {
    	int pad_len = len - s.length();
    	if (pad_len <= 0) return s;
    	StringBuffer buf = new StringBuffer();
    	for (int i=0; i<pad_len; i++)
    		buf.append(c);
    	buf.append(s);
    	return buf.toString();
    }
    
    public static String padRight(String s, int len)
    {
    	return padRight(s, len, ' ');
    }
    
    // Returns the input string padded on the right by characters to fit the given length.
    public static String padRight(String s, int len, char c)
    {
    	int pad_len = len - s.length();
    	if (pad_len <= 0) return s;
    	StringBuffer buf = new StringBuffer();
    	buf.append(s);
    	for (int i=0; i<pad_len; i++)
    		buf.append(c);
    	return buf.toString();
    }
}
