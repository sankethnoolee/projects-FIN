package com.fintellix.validationrestservice.util.connectionManager.impl;

import com.fintellix.validationrestservice.util.connectionManager.ConnectionManager;
import com.fintellix.validationrestservice.util.connectionManager.ConnectionPropertiesLoader;
import com.fintellix.validationrestservice.util.connectionManager.ConnectionPropertiesLoader.SolutionDB;
import com.fintellix.validationrestservice.util.connectionManager.CryptoDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiSolutionConnectionManager implements ConnectionManager {

    static Logger LOGGER = LoggerFactory
            .getLogger(MultiSolutionConnectionManager.class);

    protected AbstractApplicationContext context;
    private static Map<Integer, BasicDataSource> connSet;
    private static Map<String, String> martDbType;

    public MultiSolutionConnectionManager() {
        connSet = new ConcurrentHashMap<Integer, BasicDataSource>();
        martDbType = new ConcurrentHashMap<String, String>();
        setupDBPool();
    }

    protected void setupDBPool() {

        Set<Entry<String, SolutionDB>> solutions = ConnectionPropertiesLoader.getInstance().getSolutions().entrySet();
        SolutionDB solutionDB;

        for (Entry<String, SolutionDB> entry : solutions) {
            solutionDB = entry.getValue();
            BasicDataSource ds = new CryptoDataSource();
            ds.setDriverClassName(solutionDB.getDriverClazz());
            ds.setUsername(solutionDB.getUserName());
            ds.setPassword(solutionDB.getPassword());
            ds.setUrl(solutionDB.getJdbcUrl());

            ds.setInitialSize(solutionDB.getInitialSize());
            ds.setMaxIdle(solutionDB.getMaxIdle());
            ds.setMaxTotal(solutionDB.getMaxTotal());
            ds.setMinIdle(solutionDB.getMinIdle());
            ds.setMaxWaitMillis(solutionDB.getMaxWaitMillis());

            if ("Y".equalsIgnoreCase(solutionDB.getIsPrimaryFrequency()))

                martDbType.put(entry.getKey(), solutionDB.getDbType());

            connSet.put(solutionDB.getSolutionId(), ds);
        }

        shutdownHook();
    }

    protected void shutdownHook() {
        // setup JVM shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Running shutdown hook");
                Collection<BasicDataSource> pools = connSet.values();
                for (BasicDataSource pool : pools) {
                    try {
                        pool.close();
                    } catch (SQLException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
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
    public Connection getConnection(Integer solutionId) throws Throwable {
        Connection conn = null;
        LOGGER.info("Solution_Id=" + solutionId);
        if ((solutionId != null) && (connSet.size() > 0)) {
            DataSource ds = connSet.get(solutionId);
            if (ds == null) {
                throw new IllegalArgumentException(
                        "No dsn configured for Solution_Id=" + solutionId);
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
}

