/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc;

import java.util.Date;
import java.util.Properties;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.expressions.Window;

/**
 * @author Sanketh Noolee
 *
 */
public class AdhocTest {
	private static final String SQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String SQL_USERNAME = "Dev_la";
	private static final String SQL_PWD = "hVn84+Cv";
	private static final String SQL_CONNECTION_URL = "jdbc:sqlserver://10.95.243.159:1433;databaseName=LA_DEV_MART";
	final static Properties connectionProperties = new Properties();
	private static JavaSparkContext javaSparkContext;
	private static SQLContext sqlContext;
	private static SparkSession sparkSession;
	
	public static void main(String[] args) {
		connectionProperties.put("user", SQL_USERNAME);
		connectionProperties.put("password", SQL_PWD);
		connectionProperties.put("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
		

		SparkSession spark = SparkSession.builder()
	            .appName("Adhoc Testing").master("local[*]")
	            .getOrCreate();
		
		javaSparkContext = JavaSparkContext.fromSparkContext(spark.sparkContext());
		sqlContext = new SQLContext(javaSparkContext);
		
		String query =  "(SELECT  EX_5873477174204979409 as EX_16505831853529111673,EX_15785297445060410556 as EX_9881565075046590856,EX_370080051702586760 as EX_538497545781638284,SUM(EX_7871099884844905310) " + 
				"as EX_1187551083090087214  FROM  ( SELECT EX_7871099884844905310,EX_8704408192289855630,EX_5873477174204979409,EX_15785297445060410556,EX_370080051702586760 FROM ( SELECT " + 
				"PNC_SB_81.AD_active_accounts AS EX_7871099884844905310,runid.RUNDESC AS EX_8704408192289855630,PNC_SB_81.AD_product AS EX_5873477174204979409,PNC_SB_81.month AS EX_15785297445060410556,PNC_SB_81.vintageName AS EX_370080051702586760 from  PNC_SB_81 PNC_SB_81  left outer join DIM_PNC_SB_81_RUN                                                                                                                runid on PNC_SB_81.runid=runid.RUNID  ) IEX_8433014227906308048 WHERE ( EX_8704408192289855630  IN ('R-202133-81-122612')) ) T1  GROUP BY EX_5873477174204979409,EX_15785297445060410556,EX_370080051702586760" + 
				") as test";
		
		
		System.out.println("sanketh_____"+new Date().getTime());
		//INIT
		Dataset<Row> inputFileDataset = getDatasetFromSQL(query);
		inputFileDataset.createOrReplaceTempView("dp");
		
		/* dynamic order by column at code -- required later currently hard coding
		Column[] sort = new Column[2];
		sort[0] = new Column("spelExpr").desc_nulls_last();
		sort[1] = new Column("srno").asc_nulls_last();
		inputFileDataset.show();
		WindowSpec w = Window.orderBy(sort);
		w.orderBy(inputFileDataset.col("srno").desc_nulls_last());
		*/
		
		
		
		/*
		 // Current code using row_num query
		 
		  
		Dataset<Row> test = sqlContext.sql("select row_number() over ( order by "+"16505831853529111673"+") as serialNumber,* from dp" )
		.toDF();
		test.collect();
		System.out.println("sanketh_____"+new Date().getTime());
		test.show();
		*/
		
		/*
		 
		 // using zipWithIndex
		  
		  
		JavaPairRDD<Row, Long> rddzip  = inputFileDataset.orderBy("EX_16505831853529111673").javaRDD().zipWithIndex();
		JavaRDD rdd = rddzip.map(s->{
            Row r = s._1;
            Object[] arr = new Object[r.size()+1];
            for (int i = 0; i < arr.length-1; i++) {
                arr[i] = r.get(i);
            }
            arr[arr.length-1] = s._2;
            return RowFactory.create(arr);
        });

        StructType newSchema = inputFileDataset.schema().add(new StructField("rowid",
                DataTypes.LongType, false, Metadata.empty()));

        Dataset<Row> df2 = sqlContext.createDataFrame(rdd,newSchema);
        df2.collect();
        System.out.println("sanketh_____"+new Date().getTime());
		df2.show();
		
		*/
		
		/*
		 // using spark.sql.functions.row_number
		  
		  
		Dataset<Row> df2 = inputFileDataset.withColumn("serialNumber", functions.row_number()
				.over((Window.orderBy(inputFileDataset.col("EX_16505831853529111673").desc_nulls_last()))));
        df2.collect();
        System.out.println("sanketh_____"+new Date().getTime());
        df2.show();
        
        */
		
		//using monotonicallyIncreasingId
		
		Dataset<Row> df2 = inputFileDataset.withColumn("serialNumber", functions.monotonicallyIncreasingId());
        df2.collect();
        System.out.println("sanketh_____"+new Date().getTime());
        df2.show();
		
		
	}
	
	public static Dataset<Row> getDatasetFromSQL(String selectQuery) {
		Dataset<Row> jdbcDS = sqlContext.read().jdbc(SQL_CONNECTION_URL, selectQuery, connectionProperties);
		return jdbcDS;
	}

}
