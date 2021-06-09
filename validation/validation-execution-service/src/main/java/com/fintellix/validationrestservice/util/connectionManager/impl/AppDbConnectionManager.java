package com.fintellix.validationrestservice.util.connectionManager.impl;

import com.fintellix.validationrestservice.util.connectionManager.ConnectionManager;
import com.fintellix.validationrestservice.util.connectionManager.CryptoDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

//@Component

public class AppDbConnectionManager implements ConnectionManager {

    private static final String CONFIG_FILE = "jdbc-appdb.properties";
    static Logger LOGGER = LoggerFactory.getLogger(AppDbConnectionManager.class);

    public static Properties prop = null;
    static InputStream input = null;

    protected static BasicDataSource ds;

    /**
     *
     */

    public AppDbConnectionManager() {

        try {
            String configFilePath = System.getenv().get(CONFIG_FILE);
            if (configFilePath == null) {
                // look in system.property.
                configFilePath = System.getProperty(CONFIG_FILE);
            }

            if (configFilePath == null) {

                LOGGER.info("Searching " + CONFIG_FILE + " in classpath..");

            }

            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                prop = new Properties();
                prop.load(is);
                ds = new CryptoDataSource();

                ds.setDriverClassName(prop.getProperty("jdbc.datasourceClassName"));
                ds.setUsername(prop.getProperty("jdbc.username"));
                ds.setPassword(prop.getProperty("jdbc.password"));
                ds.setUrl(prop.getProperty("jdbc.url"));
                ds.setInitialSize(Integer.parseInt(prop.getProperty("dbcp.pool.initialSize")));
                ds.setMaxIdle(Integer.parseInt(prop.getProperty("dbcp.pool.maxIdle")));
                ds.setMaxTotal(Integer.parseInt(prop.getProperty("dbcp.pool.maxTotal")));
                ds.setMinIdle(Integer.parseInt(prop.getProperty("dbcp.pool.minIdle")));
                ds.setMaxWaitMillis(Integer.parseInt(prop.getProperty("dbcp.pool.maxWaitMillis")));
            } else {
                LOGGER.info(CONFIG_FILE + " in classpath.. not found");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void cleanup() {
        // do nothing.
    }

    protected static void shutdownHook() {
        // setup JVM shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Running shutdown hook");
                try {
                    ds.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });

        LOGGER.info("Shut Down Hook Attached for App Db");

    }

    @Override
    public Connection getConnection() throws Throwable {
        return ds.getConnection();
        // FIXME - why auto commit to false
        // missing?
    }

    @Override
    public Connection getConnection(Integer solId) throws Throwable {
        throw new IllegalStateException("Not Supported");
    }

}
