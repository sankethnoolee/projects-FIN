package com.fintellix.dld.dbConnection.impl;

import java.sql.Connection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fintellix.dld.dbConnection.ConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

@Component
@PropertySource("classpath:/dld_app.properties")
public class DLDdbConnectionManager implements ConnectionManager {

	static Logger LOGGER = LoggerFactory
			.getLogger(DLDdbConnectionManager.class);

	protected static AbstractApplicationContext context;
	protected static HikariDataSource dataSource;

	@Autowired
    private Environment env;
	/**
	 * 
	 */
	static {

		try {
			context = new ClassPathXmlApplicationContext("classpath:app-db.xml");
			dataSource = context.getBean(HikariDataSource.class);
			
			shutdownHook();
		}

		catch (Exception ex) {
			LOGGER.warn("app-db.xml not configured, ignoring. " +
					"Probably, don't wanna use JPA");
			
		}

	}
	
	protected static void shutdownHook() {
		// setup JVM shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Running shutdown hook");
				dataSource.close();
			}
		});
		
		LOGGER.info("Shut Down Hook Attached for App Db");

	}

	@Override
	public Connection getConnection() throws Throwable {
		return dataSource.getConnection();
		// FIXME - why auto commit to false
											// missing?
	}
	
	@Override
	public Connection getConnection(String solutionName,String clientCode) throws Throwable {
		throw new IllegalStateException("Not Supported");
	}
	
	@Override
	public void cleanup() {
		if (context != null)
			context.close();
	}
	
	@Override
	public String getMartDbType(String solutionName,String clientCode) throws Throwable {
		throw new IllegalStateException("Not Supported");
	}
	
	 @PostConstruct
     public void migrateFlyway() {
         Flyway flyway = new Flyway();
         flyway.setLocations("db/migration/"+env.getProperty("dld.dbType").trim());
         flyway.setDataSource(dataSource);
         flyway.setTable("SCHEMA_VERSION");
         flyway.migrate();
     }

	@Override
	public Connection getPrimaryAppDBConnection(String clientCode) throws Throwable {
		throw new IllegalStateException("Not Supported");
	}

	@Override
	public String getReportStatusURLForSolution(String solutionName,String clientCode) throws Throwable {
		throw new IllegalStateException("Not Supported");
	}

	@Override
	public Map<String,Integer> getAllSolutionNames(String clientCode) throws Throwable {
		throw new IllegalStateException("Not Supported");
	}
}
