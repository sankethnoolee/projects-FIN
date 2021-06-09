package com.fintellix.validationrestservice.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class ApplicationProperties {
    private static final String BUNDLE_NAME = "application";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());

    /**
     * Default Constructor
     */
    private ApplicationProperties() {
    }

    public static String getValue(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key).trim();
        } catch (MissingResourceException e) {
            e.printStackTrace();
            return '@' + key + '@';
        }
    }

    public static boolean isKeyPresent(String key) {
        try {
            return RESOURCE_BUNDLE.containsKey(key);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
