package com.fintellix.framework.SpEL;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fintellix.framework.SpEL.functions.DateFunction;
import com.fintellix.framework.SpEL.functions.LogicalFunction;
import com.fintellix.framework.SpEL.functions.NumericFunction;
import com.fintellix.framework.SpEL.functions.StringFunction;

public class ExpressionEvaluatorContext {
    public static final StandardEvaluationContext context = new StandardEvaluationContext();

    static {
        try {
            //String functions
            context.registerFunction("substr".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("substr", String.class, Integer.class, Integer.class));
            context.registerFunction("upper".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("upper", String.class));
            context.registerFunction("lower".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("lower", String.class));
            context.registerFunction("len".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("len", String.class));
            context.registerFunction("concat".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("concat", String[].class));
            context.registerFunction("isNotEmpty".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("isNotEmpty", String.class));
            context.registerFunction("isEmpty".toUpperCase(),
                    StringFunction.class.getDeclaredMethod("isEmpty", String.class));
            context.registerFunction("CONVERT",
                    StringFunction.class.getDeclaredMethod("convert", Object.class, String.class));

            //Logical Functions
            context.registerFunction("and".toUpperCase(),
                    LogicalFunction.class.getDeclaredMethod("and", Boolean[].class));
            context.registerFunction("or".toUpperCase(),
                    LogicalFunction.class.getDeclaredMethod("or", Boolean[].class));
            context.registerFunction("REGEX",
                    LogicalFunction.class.getDeclaredMethod("regex", String.class, String.class));
            context.registerFunction("BETWEEN",
                    LogicalFunction.class.getDeclaredMethod("between", Object.class, Object.class, Object.class));
            context.registerFunction("BEGINSWITH",
                    LogicalFunction.class.getDeclaredMethod("beginsWith", String.class, String[].class));
            context.registerFunction("ENDSWITH",
                    LogicalFunction.class.getDeclaredMethod("endsWith", String.class, String[].class));
            context.registerFunction("CONTAINS",
                    LogicalFunction.class.getDeclaredMethod("contains", String.class, String[].class));
            context.registerFunction("IN",
                    LogicalFunction.class.getDeclaredMethod("in", Object.class, Object[].class));
            context.registerFunction("NOTIN",
                    LogicalFunction.class.getDeclaredMethod("notIn", Object.class, Object[].class));

            //Numeric Functions
            context.registerFunction("abs".toUpperCase(),
                    NumericFunction.class.getDeclaredMethod("abs", Double.class));
            context.registerFunction("ROUND",
                    NumericFunction.class.getDeclaredMethod("round", BigDecimal.class, Integer.class));

            //Date Functions
            context.registerFunction("TODATE",
                    DateFunction.class.getDeclaredMethod("toDate", Object.class));
            context.registerFunction("SOM",
                    DateFunction.class.getDeclaredMethod("SOM", Date.class));
            context.registerFunction("EOM",
                    DateFunction.class.getDeclaredMethod("EOM", Date.class));
            context.registerFunction("SOY",
                    DateFunction.class.getDeclaredMethod("SOY", Date.class));
            context.registerFunction("EOY",
                    DateFunction.class.getDeclaredMethod("EOY", Date.class));
            context.registerFunction("SOFY",
                    DateFunction.class.getDeclaredMethod("SOFY", Date.class));
            context.registerFunction("EOFY",
                    DateFunction.class.getDeclaredMethod("EOFY", Date.class));
            context.registerFunction("DATEPART",
                    DateFunction.class.getDeclaredMethod("datePart", Date.class, String.class));
            context.registerFunction("DATEDIFF",
                    DateFunction.class.getDeclaredMethod("dateDiff", Date.class, Date.class, String.class));
            context.registerFunction("DATE",
                    DateFunction.class.getDeclaredMethod("toDate", Object.class, String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
