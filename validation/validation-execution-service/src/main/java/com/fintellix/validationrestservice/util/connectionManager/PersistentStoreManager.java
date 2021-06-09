package com.fintellix.validationrestservice.util.connectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class PersistentStoreManager {

    static Logger LOGGER = LoggerFactory.getLogger(PersistentStoreManager.class);
    private static ConnectionManager appConnMan;
    private static ConnectionManager multiMartConnectionManager;

    static {
        try {

            appConnMan = (ConnectionManager) Class
                    .forName("com.fintellix.validationrestservice.util.connectionManager.impl.AppDbConnectionManager").newInstance();
            multiMartConnectionManager = (ConnectionManager) Class
                    .forName("com.fintellix.validationrestservice.util.connectionManager.impl.MultiSolutionConnectionManager")
                    .newInstance();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public static void cleanup() {
        if (appConnMan != null)
            appConnMan.cleanup();

    }

    public static Connection getConnection() throws Throwable {
        return appConnMan.getConnection();
        // return null;
    }

    public static Connection getSolutionDBConnection(Integer solId) throws Throwable {
        return multiMartConnectionManager.getConnection(solId);
    }

}
