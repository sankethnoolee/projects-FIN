package com.fintellix.validationrestservice.util.connectionManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.calcite.adapter.csv.CsvSchema;
import org.apache.calcite.adapter.csv.CsvTable.Flavor;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CalciteConnectionManager {

	static Logger LOGGER = LoggerFactory.getLogger(CalciteConnectionManager.class);
	static Properties info = new Properties();

	static {
		try {
			info.setProperty("lex", "JAVA");
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	public static void cleanup() {

	}

	public static Connection getCalciteConnection(String path, String schemaName) throws Throwable {
		Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
		CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
		SchemaPlus rootSchema = calciteConnection.getRootSchema();
		Schema schema = new CsvSchema(new File(path), Flavor.SCANNABLE);
		rootSchema.add(schemaName, schema);
		return calciteConnection;
	}
	
}
