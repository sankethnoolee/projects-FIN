package com.fintellix.validationrestservice.util.connectionManager;

import java.sql.Connection;

public interface ConnectionManager {
	
	public Connection getConnection() throws Throwable;
	
	public Connection getConnection(Integer solutionId) throws Throwable;
	
	public void cleanup();


}
