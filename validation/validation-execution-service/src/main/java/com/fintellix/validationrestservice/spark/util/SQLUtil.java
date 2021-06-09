package com.fintellix.validationrestservice.spark.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import com.fintellix.validationrestservice.util.connectionManager.ConnectionPropertiesLoader;


/**
 * @author Sanketh Noolee
 *
 */
public class SQLUtil {
	private final ConnectionPropertiesLoader c;
	private final String SQL_USERNAME;
	private final String SQL_PWD;
	private final String SQL_CONNECTION_URL;
	private final Properties connectionProperties = new Properties();
	//private Map<String, String> options = new HashMap<>();

	private SQLContext sqlContext;

	public SQLUtil(SQLContext _SQLContext) {
		this.sqlContext = _SQLContext;
		 c = ConnectionPropertiesLoader.getInstance();
		 SQL_USERNAME = "ADEPT_DEV_DATAHUB";
		 SQL_PWD = "Welcome1";
		 SQL_CONNECTION_URL = "jdbc:oracle:thin:@BLRDEVORCLDB008:1521:ORCL";
		
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

	public Dataset<Row> getDatasetFromSQL(String selectQuery) {
		System.out.println(selectQuery);
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
