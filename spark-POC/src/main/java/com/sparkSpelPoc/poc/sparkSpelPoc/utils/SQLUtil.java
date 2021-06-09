package com.sparkSpelPoc.poc.sparkSpelPoc.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import com.sparkSpelPoc.poc.sparkSpelPoc.domain.FileInputLine;

/**
 * @author Sanketh Noolee
 *
 */
public class SQLUtil {

	private static final String SQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String SQL_USERNAME = "Dev_la";
	private static final String SQL_PWD = "hVn84+Cv";
	private static final String SQL_CONNECTION_URL = "jdbc:sqlserver://10.95.243.159:1433;databaseName=LA_DEV_MART";
	final Properties connectionProperties = new Properties();
	//private Map<String, String> options = new HashMap<>();

	private SQLContext sqlContext;

	public SQLUtil(SQLContext _SQLContext) {
		this.sqlContext = _SQLContext;
		
		System.out.println("***********************************");
		System.out.println("***********************************");
		connectionProperties.put("user", SQL_USERNAME);
		connectionProperties.put("password", SQL_PWD);
		connectionProperties.put("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
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

	public Dataset<Row> getDatasetFromSQL(String selectQuery) {
		Dataset<Row> jdbcDS = sqlContext.read().jdbc(SQL_CONNECTION_URL, selectQuery, connectionProperties);

		return jdbcDS;

	}
	
	public Dataset<Row> getDatasetFromSQL(String selectQuery,String partionColumnName,
			Integer lowerBoundLimit,Integer upperBoundLimit,Integer noOfPartition) {
		
		//setting partitioning example
		
		Dataset<Row> jdbcDS = sqlContext.read().jdbc(SQL_CONNECTION_URL, selectQuery, partionColumnName,
				lowerBoundLimit, upperBoundLimit, noOfPartition, connectionProperties);;

		return jdbcDS;

	}
}
