package com.fintellix.framework.SpEL.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class NumericFunction {

    public static Double abs(Double number) {
        return Math.abs(number);
    }

    public static BigDecimal round(BigDecimal number, Integer scale) {
        return number.setScale(scale, RoundingMode.HALF_EVEN);
    }
}