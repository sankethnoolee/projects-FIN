package com.fintellix.dld.dbConnection.impl;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import com.fintellix.dld.dbConnection.ConnectionManager;
import com.fintellix.dld.dbConnection.DecryptDataSource;
import com.fintellix.dld.dbConnection.impl.ConnectionPropertiesLoader.SolutionDB;
import com.zaxxer.hikari.HikariDataSource;
@Component
public class MultiSolutionConnectionManager implements ConnectionManager {

	static Logger LOGGER = LoggerFactory
			.getLogger(MultiSolutionConnectionManager.class);

	protected AbstractApplicationContext context;
	private static Map<String, HikariDataSource> connSet;
	private static Map<String, HikariDataSource> primaryConn;
	private static Map<String,String> martDbType;
	private static Map<String,String> reportSolutionStatusURL;
	private static Map<String,Map<String,Integer>> allSolutionName;
	public MultiSolutionConnectionManager() {
		connSet = new ConcurrentHashMap<String, HikariDataSource>();
		martDbType=new ConcurrentHashMap<String, String>();
		reportSolutionStatusURL=new ConcurrentHashMap<String, String>();
		allSolutionName = new ConcurrentHashMap<String, Map<String,Integer>>();
		primaryConn= new ConcurrentHashMap<String, HikariDataSource>();
		setupDBPool();
	}

	protected void setupDBPool() {

		Set<Entry<String, SolutionDB>> solutions = ConnectionPropertiesLoader.getInstance().getSolutions().entrySet();
		SolutionDB solutionDB;
		Map<String,Integer> allSolsForClient = new HashMap<String, Integer>();
		for (Entry<String, SolutionDB> entry : solutions) {
			solutionDB = entry.getValue();
			HikariDataSource ds = new DecryptDataSource();
			ds.setDriverClassName(solutionDB.getDriverClazz());
			ds.setUsername(solutionDB.getUserName());
			ds.setPassword(solutionDB.getPassword());
			ds.setJdbcUrl(solutionDB.getJdbcUrl());
			//ds.setsetInitialSize(solutionDB.getInitialSize());
			ds.setIdleTimeout(solutionDB.getMaxIdle());
			ds.setMaximumPoolSize(solutionDB.getMaxTotal());
			ds.setMinimumIdle(solutionDB.getMinIdle());
			ds.setMaxLifetime(solutionDB.getMaxWaitMillis());
			if("Y".equalsIgnoreCase(solutionDB.getIsPrimaryFrequency()))
				primaryConn.put(solutionDB.getClientCode(), ds);
			martDbType.put(entry.getKey(), solutionDB.getDbType());

			reportSolutionStatusURL.put(entry.getKey(),solutionDB.getReportStatusURL());
			connSet.put(entry.getKey(), ds);
			if(allSolutionName.containsKey((solutionDB.getClientCode()))){
				allSolsForClient=allSolutionName.get(solutionDB.getClientCode());
				allSolsForClient.put(solutionDB.getSolutionName(), solutionDB.getSolutionId());
				allSolutionName.put(solutionDB.getClientCode(), allSolsForClient);
			} else {
				allSolsForClient= new HashMap<String, Integer>();
				allSolsForClient.put(solutionDB.getSolutionName(), solutionDB.getSolutionId());
				allSolutionName.put(solutionDB.getClientCode(), allSolsForClient);
			}
		}

		shutdownHook();
	}

	protected void shutdownHook() {
		// setup JVM shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Running shutdown hook");
				Collection<HikariDataSource> pools = connSet.values();
				for ( HikariDataSource pool: pools){

					pool.close();

				}

			}
		});

		LOGGER.info("Shut Down Hook Attached Mart Connections");

	}


	@Override
	public Connection getConnection() throws Throwable {
		throw new IllegalStateException("Not Supported");
	}

	@Override
	public Connection getConnection(String solutionName,String clientCode) throws Throwable { 
		Connection conn = null;
		LOGGER.info("Solution_Id=" + solutionName);
		if ((solutionName != null) && (connSet.size() > 0)) {
			DataSource ds = connSet.get(clientCode+solutionName);
			if (ds == null) {
				throw new IllegalArgumentException(
						"No dsn configured for Client = "+clientCode+" and Solution_Name=" + solutionName);
			}

			conn = ds.getConnection();
			conn.setAutoCommit(false);
		}

		return conn;
	}

	@Override
	public void cleanup() {
		// do nothing.
	}

	@Override
	public String getMartDbType(String solutionName,String clientCode) throws Throwable {
		LOGGER.info("Solution_Id=" + solutionName);
		String dbType="";
		if ((solutionName != null) && (martDbType.size() > 0)) {
			dbType=martDbType.get(clientCode+solutionName);
		}
		return dbType;
	}

	@Override
	public Connection getPrimaryAppDBConnection(String clientCode) throws Throwable {
		LOGGER.info("Get Primary App DB connection");
		return primaryConn.get(clientCode).getConnection();
	}

	@Override
	public String getReportStatusURLForSolution(String solutionName,String clientCode) throws Throwable {
		return reportSolutionStatusURL.get(clientCode+solutionName);

	}

	@Override
	public Map<String,Integer> getAllSolutionNames(String clientCode) throws Throwable {
		return allSolutionName.get(clientCode);
	}
}

