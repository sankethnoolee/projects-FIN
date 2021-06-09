package com.sparkSpelPoc.poc.sparkSpelPoc.utils;

import static com.sparkSpelPoc.poc.sparkSpelPoc.config.CustomConstants.COLUMN_DOUBLE_UDF_NAME;
import static com.sparkSpelPoc.poc.sparkSpelPoc.config.CustomConstants.COLUMN_UPPERCASE_UDF_NAME;

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

/**
 * @author Sanketh Noolee
 *
 */
public class UDFUtil {

    private SQLContext sqlContext;

    public UDFUtil(SQLContext _sqlContext) {
        this.sqlContext = _sqlContext;
    }

    public void registerColumnDoubleUdf() {

        this.sqlContext.udf().register(COLUMN_DOUBLE_UDF_NAME, (UDF1<String, Integer>)
            (columnValue) -> {

                return Integer.parseInt(columnValue) * 2;

            }, DataTypes.IntegerType);
    }

    public void registerColumnUppercaseUdf() {

        this.sqlContext.udf().register(COLUMN_UPPERCASE_UDF_NAME, (UDF1<String, String>)
            (columnValue) -> {

                return columnValue.toUpperCase();

            }, DataTypes.StringType);
    }
}
