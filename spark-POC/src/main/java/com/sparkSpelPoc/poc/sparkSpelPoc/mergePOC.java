/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.sparkSpelPoc.poc.sparkSpelPoc.utils.FileUtil;

/**
 * @author Sanketh Noolee
 *
 */
public class mergePOC {
	/*public static void main(String[] args) {
		SparkSession sparkSession = SparkSession.builder()
	            .appName("Adhoc Testing").master("local[*]")
	            .getOrCreate();
		FileUtil fileUtil = new FileUtil(sparkSession);
		String inputFilePath="D:\\spark-SpEL-POC\\speldata.csv";
		Dataset<Row> inputFileDataset = fileUtil.getDatasetFromFileGeneric(inputFilePath);
		Dataset<Row> df2 = inputFileDataset.withColumn("serialNumber", functions.monotonicallyIncreasingId());
        df2 = unionDatasets(df2, inputFileDataset);
        df2.show(35);
	}
	
	
	private static Dataset<Row> unionDatasets(Dataset<Row> one, Dataset<Row> another) {
        StructType firstSchema = one.schema();
        List<String> anotherFields = Arrays.asList(another.schema().fieldNames());
        another = balanceDataset(another, firstSchema, anotherFields);
        StructType secondSchema = another.schema();
        List<String> oneFields = Arrays.asList(one.schema().fieldNames());
        one = balanceDataset(one, secondSchema, oneFields);
        return another.unionByName(one);
    }

    private static Dataset<Row> balanceDataset(Dataset<Row> dataset, StructType schema, List<String> fields) {
        for (StructField e : schema.fields()) {
            if (!fields.contains(e.name())) {
                dataset = dataset
                        .withColumn(e.name(),
                        		functions.lit(null));
                dataset = dataset.withColumn(e.name(),
                        dataset.col(e.name()).cast(Optional.ofNullable(e.dataType()).orElse(DataTypes.StringType)));
            }
        }
        return dataset;
    }
    */
}
