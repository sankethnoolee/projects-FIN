package com.sparkSpelPoc.poc.sparkSpelPoc.utils;

import static com.sparkSpelPoc.poc.sparkSpelPoc.config.CustomConstants.SPEL_EVAL_UDF_NAME;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.sparkSpelPoc.poc.sparkSpelPoc.SpEL.ExpressionEvaluatorContext;

/**
 * @author Sanketh Noolee
 *
 */
public class SpelUtil {

	public static final SpelExpressionParser parser = new SpelExpressionParser();
	
    private SQLContext sqlContext;

    public SpelUtil(SQLContext _sqlContext) {
        this.sqlContext = _sqlContext;
    }

    public void registerSpelEvalUdf() {

        this.sqlContext.udf().register(SPEL_EVAL_UDF_NAME, (UDF1<String, String>)
            (columnValue) -> {
            	try {
            		return parser.parseExpression(columnValue).getValue(ExpressionEvaluatorContext.context, String.class);
            	}catch(Throwable e) {
            		e.printStackTrace();
            		System.out.println("Expression evaluation failed for --> "+columnValue);
            		return "false";
            	}

            }, DataTypes.StringType);
    }
    
}
