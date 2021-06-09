package com.fintellix.dld.dbConnection;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface ConnectionManager {
	
	public Connection getConnection() throws Throwable;
	
	public Connection getConnection(String solutionName,String clientCode) throws Throwable;
	
	public void cleanup();
	
	public String getMartDbType(String solutionName,String clientCode) throws Throwable;
	
	public Connection getPrimaryAppDBConnection(String clientCode) throws Throwable;
	
	public String getReportStatusURLForSolution(String solutionName,String clientCode) throws Throwable;
	
	public Map<String,Integer> getAllSolutionNames(String clientCode)throws Throwable;


}
