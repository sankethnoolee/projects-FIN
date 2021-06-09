package com.fintellix.validationrestservice.spark.util;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * @author Sanketh Noolee
 *
 */
public class FileUtil {

    private SparkSession sparkSession;

    public FileUtil(SparkSession _sparkSession) {
        this.sparkSession = _sparkSession;
    }

    /*public Dataset<FileInputLine> getDatasetFromFile(String filePath) {

        Dataset<FileInputLine> fileDataSet = this.sparkSession.read().option("header", "true").csv(filePath)
                .as(Encoders.bean(FileInputLine.class));

        return fileDataSet;
    }
    */
    public Dataset<Row> getDatasetFromFileGeneric(String filePath) {

        Dataset<Row> fileDataSet = this.sparkSession.read().option("header", "true").csv(filePath);
                //.as(Encoders.bean(String.class));

        return fileDataSet;
    }
}
