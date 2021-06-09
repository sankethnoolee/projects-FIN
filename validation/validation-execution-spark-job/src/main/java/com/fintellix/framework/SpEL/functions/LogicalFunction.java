package com.fintellix.framework.SpEL.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.PatternSyntaxException;

public abstract class LogicalFunction {
	//private static final SimpleDateFormat s = new SimpleDateFormat(ValidationProperties.getValue("app.validation.dateFunction.defaultDateFormat"));
    public static boolean and(Boolean... values) {
        boolean returnValue = true;

        for (Boolean value : values) {
            returnValue = returnValue && value;

            if (!returnValue) {
                return returnValue;
            }
        }

        return returnValue;
    }

    public static boolean or(Boolean... values) {
        boolean returnValue = false;

        for (Boolean value : values) {
            returnValue = returnValue || value;

            if (returnValue) {
                return returnValue;
            }
        }

        return returnValue;
    }

    public static boolean regex(String expression, String pattern) {
        try {
            return expression.trim().matches(pattern.trim());
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean between(Object value, Object lowerCutOff, Object upperCutOff) {
        if (value instanceof Date) {
            return (((Date) value).getTime() >= ((Date) lowerCutOff).getTime() &&
                    ((Date) value).getTime() <= ((Date) upperCutOff).getTime());
        } else {
            return (Double.valueOf(value.toString()) >= Double.valueOf(lowerCutOff.toString())
                    && Double.valueOf(value.toString()) <= Double.valueOf(upperCutOff.toString()));
        }
    }

    public static boolean beginsWith(String exp, String... values) {
        for (String value : values) {
            if (exp.startsWith(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean endsWith(String exp, String... values) {
        for (String value : values) {
            if (exp.endsWith(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean contains(String exp, String... values) {
        for (String value : values) {
            if (exp.contains(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean in(Object exp, Object... values) throws ParseException {
        boolean returnValue = false;
        if(null==exp) {
        	exp = new String("null");
        }
        if (exp instanceof String) {
            for (Object value : values) {
                if ((exp + "").trim().equals((value + "").trim())) {
                    returnValue = true;
                    break;
                }
            }
        } else if (exp instanceof Date) {
            for (Object value : values) {
            	if(!(value instanceof Date)) {
            		// value = s.parse(String.valueOf(value));
            	}
            	if (((Date) exp).getTime() == ((Date) value).getTime()) {
                    returnValue = true;
                    break;
                }
            }
        } else {
            for (Object value : values) {
                if (Double.parseDouble(exp + "") == Double.parseDouble(value + "")) {
                    returnValue = true;
                    break;
                }
            }
        }

        return returnValue;
    }

    public static boolean notIn(Object exp, Object... values) throws ParseException {
        return !in(exp, values);
    }
}
