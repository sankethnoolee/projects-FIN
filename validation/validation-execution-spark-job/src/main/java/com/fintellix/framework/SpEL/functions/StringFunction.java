package com.fintellix.framework.SpEL.functions;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class StringFunction {

	private final static Logger LOGGER = LoggerFactory.getLogger(StringFunction.class);
    public static String substr(String input, Integer startIndex, Integer endIndex) {
        try {
            if (endIndex != null) {
                return input.substring(startIndex, endIndex);
            }

            return input.substring(startIndex);
        } catch (Throwable e) {
        	LOGGER.error(e.getMessage());
        }
        return input;
    }

    public static String lower(String input) {
    	if(null==input) {
    		input = "null";
        }
        return input.toLowerCase();
    }

    public static String upper(String input) {
    	if(null==input) {
    		input = "null";
        }
    	return input.toUpperCase();
    }

    public static Integer len(String input) {
        return input.length();
    }

    public static String concat(String... values) {
        String returnString = "";

        for (String string : values) {
            returnString = returnString.concat(string);
        }

        return returnString;
    }

    public static Boolean isNotEmpty(String value) {
        return !StringUtils.isEmpty(value);
    }

    public static Boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    public static Object convert(@NotNull Object value, @NotNull String type) {
        try{
            switch (type.toUpperCase()) {
                case "NUMBER" :
                    if(value != null && value.toString().trim().length() > 0) {
                        if(value instanceof String) {
                            if(((String) value).contains(".")) {
                                return Double.parseDouble(value.toString().trim());
                            } else {
                                return Integer.parseInt(value.toString().trim());
                            }
                        } else {
                            return value;
                        }
                    }
                    break;
                case "STRING" :
                    return value.toString();
                default: throw new Exception("Incorrect/Unsupported convert type");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }
}
