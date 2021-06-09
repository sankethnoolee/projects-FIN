package com.fintellix.framework.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.spark.SparkFiles;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

/**
 * @author Sanketh Noolee
 *
 */
public class SQLUtil {
	private final String SQL_USERNAME;
	private final String SQL_PWD;
	private final String SQL_CONNECTION_URL;
	private final Properties connectionProperties = new Properties();
	//private Map<String, String> options = new HashMap<>();

	private SQLContext sqlContext;

	public SQLUtil(SQLContext _SQLContext, String[] _args) {
		this.sqlContext = _SQLContext;
		SQL_USERNAME = sqlContext.getConf("spark.custom.jdbc.mart.username");
		SQL_PWD = sqlContext.getConf("spark.custom.jdbc.mart.password");
		SQL_CONNECTION_URL = sqlContext.getConf("spark.custom.jdbc.mart.connectionURL");
		connectionProperties.put("driver", sqlContext.getConf("spark.custom.jdbc.mart.driverName"));
		connectionProperties.put("user", SQL_USERNAME);
		connectionProperties.put("password", SQL_PWD);

		//use this if using sqlContext.load() -- this is deprecated
		//hence using read

		//Datasource options
		//options.put("driver", SQL_DRIVER);
		//options.put("url", SQL_CONNECTION_URL);
		//options.put("dbtable", "(select emp_no, concat_ws(' ', first_name, last_name) as full_name from employees) as employees_name");

		//optional 
		//if the load is huge best way to employ partitions during read.
		//options.put("partitionColumn", "emp_no");
		//options.put("numPartitions", "10");
	}

	public Dataset<Row> getDatasetFromSQL(String selectQuery){
		System.out.println(selectQuery);
		//Class.forName("oracle.jdbc.driver.OracleDriver");
		Dataset<Row> jdbcDS = sqlContext.read().jdbc(SQL_CONNECTION_URL, selectQuery, connectionProperties);
		return jdbcDS;

	}

	public Dataset<Row> getDatasetFromSQL(String selectQuery,String partionColumnName,
			Integer lowerBoundLimit,Integer upperBoundLimit,Integer noOfPartition) {

		//setting partitioning example

		Dataset<Row> jdbcDS = sqlContext.read().jdbc(SQL_CONNECTION_URL, selectQuery, partionColumnName,
				lowerBoundLimit, upperBoundLimit, noOfPartition, connectionProperties);

				return jdbcDS;

	}
}
