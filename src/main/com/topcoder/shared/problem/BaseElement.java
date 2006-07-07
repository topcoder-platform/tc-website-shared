package com.topcoder.shared.problem;

import java.util.Arrays;
import java.util.List;

abstract class BaseElement implements Element {
    protected static final String[] USER_ONLY_TAGS = {"ul", "ol", "li", "tt", "i", "b", "h1", "h2", "h3", "h4",
                                                 "h5", "a", "img", "br", "sub", "sup", "p", "pre", "hr", "list", "type"};
    protected static final List USER_ONLY_TAGS_LIST = Arrays.asList(USER_ONLY_TAGS);
    
    private ElementRenderer renderer;

    public BaseElement() {
    }

    public void setRenderer(ElementRenderer renderer) {
        this.renderer = renderer;
    }

    public ElementRenderer getRenderer() {
        return renderer;
    }

    /**
     * Utility function for encoding HTML entities in text.  All occurrences of the &lt;, &gt;,
     * and &amp; characters are converted to &amp;lt;, &amp;gt;, and &amp;amp;, respectively.
     */
    static public String encodeHTML(String text) {
        StringBuffer buf = new StringBuffer(text.length());

        for (int i = 0; i < text.length(); i++)
            switch (text.charAt(i)) {
                case '&':
                    buf.append("&amp;");
                    break;
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                default:
                    buf.append(text.charAt(i));
            }
        return buf.toString();
    }
    
    
    /**
     * Replaces all escaped entities &amp;lt;, &amp;gt;, &amp;quot; 
     * and &amp;amp; with its unescaped representation (&lt; &gt; &quot; &,a[;)
     *  
     * Note: This is a bad implementation, it is cunrrently use in just a few places
     * A better implementation should be done for intensive use
     * 
     * @param text text to unescape
     * 
     * @return the resulting text
     */
    public static String decodeHTML(String text) {
        return HTMLCharacterHandler.decode(text);
    }
}
