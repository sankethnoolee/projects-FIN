package com.fintellix.framework.utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class FileUtil {

    private SparkSession sparkSession;

    public FileUtil(SparkSession _sparkSession) {
        this.sparkSession = _sparkSession;
    }

    public Dataset<Row> getDatasetFromFileGeneric(String filePath) {

        Dataset<Row> fileDataSet = this.sparkSession.read().option("header", "true").csv(filePath);
                //.as(Encoders.bean(String.class));

        return fileDataSet;
    }
}
