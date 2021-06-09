package com.fintellix.validationrestservice.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.Properties;

public class ValidationProperties {
    private static Properties validationProperties;

    static {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("validationProperties.properties");
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            validationProperties = new Properties();
            validationProperties.load(isr);
        } catch (Exception e) {
            throw new RuntimeException("Coudnt read validationProperties  properties from class path", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Default Constructor
     */
    private ValidationProperties() {
    }

    public static String getValue(String key) {
        try {
            return validationProperties.getProperty(key).trim();
        } catch (MissingResourceException e) {
            e.printStackTrace();
            return '@' + key + '@';
        }
    }

    public static boolean isKeyPresent(String key) {
        try {
            return validationProperties.containsKey(key);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
