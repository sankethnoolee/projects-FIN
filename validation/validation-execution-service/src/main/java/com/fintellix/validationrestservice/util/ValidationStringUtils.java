package com.fintellix.validationrestservice.util;

public class ValidationStringUtils {

    private static final int INDEX_NOT_FOUND = -1;

    /**
     * Replaces a String with another String inside a larger String,
     * for the first {max} values of the search String,
     * case sensitively/insensisitively based on {ignoreCase} value.
     *
     * A {null} reference passed to this method is a no-op.
     * 
     * StringUtils.replace(null, *, *, *, false)         = null
     * StringUtils.replace("", *, *, *, false)           = ""
     * StringUtils.replace("any", null, *, *, false)     = "any"
     * StringUtils.replace("any", *, null, *, false)     = "any"
     * StringUtils.replace("any", "", *, *, false)       = "any"
     * StringUtils.replace("any", *, *, 0, false)        = "any"
     * StringUtils.replace("abaa", "a", null, -1, false) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1, false)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0, false)   = "abaa"
     * StringUtils.replace("abaa", "A", "z", 1, false)   = "abaa"
     * StringUtils.replace("abaa", "A", "z", 1, true)    = "zbaa"
     * StringUtils.replace("abAa", "a", "z", 2, true)    = "zbza"
     * StringUtils.replace("abAa", "a", "z", -1, true)   = "zbzz"
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for (case insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param max  maximum number of values to replace, or { -1} if no maximum
     * @param ignoreCase if true replace is case insensitive, otherwise case sensitive
     * @return the text with any replacements processed, null if null String input
     */
    public static String replace(final String text, String searchString, final String replacement, int max,
                                  final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }

        String searchText = text;
        if (ignoreCase) {
            searchText = text.toLowerCase();
            searchString = searchString.toLowerCase();
        }

        int start = 0;
        int end = searchText.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }

        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        final StringBuilder buf = new StringBuilder(text.length() + increase);

        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = searchText.indexOf(searchString, start);
        }

        buf.append(text, start, text.length());
        return buf.toString();
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
