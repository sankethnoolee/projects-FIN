package com.fintellix.validationrestservice.core.evaulator.spEL.functions;

import com.fintellix.validationrestservice.util.ValidationProperties;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateFunction {
    public static Date toDate(Object periodId) throws ParseException {
        SimpleDateFormat s = new SimpleDateFormat(ValidationProperties.getValue("app.validation.dateFunction.defaultPeriodFormat"));
        return s.parse(String.valueOf(periodId));
    }

    public static Date SOM(Date period) {
        DateTime x = new DateTime(period.getTime());
        return x.minusDays(x.getDayOfMonth() - 1).toDate();
    }

    public static Date EOM(Date period) {
        DateTime x = new DateTime(period.getTime());
        x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMaximumValue()).getDayOfMonth());
        return x.toDate();
    }

    public static Date SOY(Date period) {
        int startMonth = Integer.parseInt(ValidationProperties.getValue("app.validation.dateFunction.startMonth"));
        DateTime x = new DateTime(period.getTime());
        x = x.minusMonths(x.getMonthOfYear() - startMonth);
        x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMinimumValue()).getDayOfMonth());
        return x.toDate();
    }

    public static Date EOY(Date period) {
        int endMonth = Integer.parseInt(ValidationProperties.getValue("app.validation.dateFunction.endMonth"));
        DateTime x = new DateTime(period.getTime());
        x = x.minusMonths(x.getMonthOfYear() - endMonth);
        x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMaximumValue()).getDayOfMonth());
        return x.toDate();
    }

    public static Date SOFY(Date period) {
        int finStartMonth = Integer.parseInt(ValidationProperties.getValue("app.validation.dateFunction.finStartMonth"));
        DateTime x = new DateTime(period.getTime());
        x = x.minusMonths(x.getMonthOfYear() - finStartMonth);
        x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMinimumValue()).getDayOfMonth());
        return x.toDate();
    }

    public static Date EOFY(Date period) {
        int finEndMonth = Integer.parseInt(ValidationProperties.getValue("app.validation.dateFunction.finEndMonth"));
        DateTime x = new DateTime(period.getTime());
        x = x.minusMonths(x.getMonthOfYear() - finEndMonth);
        x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMaximumValue()).getDayOfMonth());
        return x.toDate();
    }

    public static Integer datePart(Date period, String type) {
        DateTime x = new DateTime(period.getTime());
        if (type.equalsIgnoreCase("m")) {
            return x.getMonthOfYear();
        } else if (type.equalsIgnoreCase("y")) {
            return x.getYear();
        } else if (type.equalsIgnoreCase("d")) {
            return x.getDayOfMonth();
        }

        return null;
    }

    public static Integer dateDiff(Date period1, Date period2, String type) {
        if (type.equalsIgnoreCase("m")) {
            DateTime x = new DateTime(period1.getTime());
            DateTime y = new DateTime(period2.getTime());
            long diff = x.getMonthOfYear() - y.getMonthOfYear();
            long yearDiff = x.getYear() - y.getYear();
            yearDiff = yearDiff * 12;
            return Integer.parseInt(diff + yearDiff + "");
        } else if (type.equalsIgnoreCase("y")) {
            DateTime x = new DateTime(period1.getTime());
            DateTime y = new DateTime(period2.getTime());
            long diff = x.getYear() - y.getYear();
            return Integer.parseInt(diff + "");
        } else if (type.equalsIgnoreCase("d")) {
            long diff = period1.getTime() - period2.getTime();
            diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            return Integer.parseInt(diff + "");
        }

        return null;
    }

    public static Date toDate(Object periodId, String format) throws ParseException {
        SimpleDateFormat s = new SimpleDateFormat(format);
        return s.parse(String.valueOf(periodId));
    }
}
