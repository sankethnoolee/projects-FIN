package com.fintellix.dld.dbConnection;

import java.sql.Connection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Component
public class PersistentStoreManager {

	private static ConnectionManager dldAppConnMan;
	private static ConnectionManager dldAppconnManMultiApp;

	static Logger LOGGER = LoggerFactory.getLogger(PersistentStoreManager.class);
	
	static

	{
		try

		{

			dldAppConnMan = (ConnectionManager) Class.forName("com.fintellix.dld.dbConnection.impl.DLDdbConnectionManager").newInstance();
			dldAppconnManMultiApp = (ConnectionManager) Class.forName("com.fintellix.dld.dbConnection.impl.MultiSolutionConnectionManager").newInstance();


		}

		catch (Exception ex)

		{
			LOGGER.error(ex.getMessage(), ex);
		}
	}
	
	
	public static void cleanup() {
		if (dldAppConnMan != null)
			dldAppConnMan.cleanup();
		

	}

	public static Connection getConnection() throws Throwable {
		return dldAppConnMan.getConnection();
	}
	
	public static Connection getPrimaryAppDBConnection(String clientCode) throws Throwable {
		return dldAppconnManMultiApp.getPrimaryAppDBConnection(clientCode);
	}
	public static Connection getSolutionAppDBConnection(String solutionName,String clientCode) throws Throwable {
		return dldAppconnManMultiApp.getConnection(solutionName,clientCode);
	}
	public static String getReportStatusURLForSolution(String solutionName,String clientCode) throws Throwable{
		return dldAppconnManMultiApp.getReportStatusURLForSolution(solutionName,clientCode);
	}
	public static Map<String,Integer> getAllSolutionName(String clientCode)throws Throwable{
		//return dldAppconnManMultiApp.getReportStatusURLForSolution(solutionName);
		return dldAppconnManMultiApp.getAllSolutionNames(clientCode);
	}
	
}
