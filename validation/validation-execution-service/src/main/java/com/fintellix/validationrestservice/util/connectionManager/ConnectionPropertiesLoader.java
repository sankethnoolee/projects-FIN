package com.fintellix.validationrestservice.util.connectionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.validationrestservice.core.AbstractPropertyLoader;

public class ConnectionPropertiesLoader extends AbstractPropertyLoader {

	private static final String CONFIG = "validation_solutions.json";
	private final Map<String, SolutionDB> solutions;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConnectionPropertiesLoader.class);

	private static final ConnectionPropertiesLoader instance = new ConnectionPropertiesLoader();

	private ConnectionPropertiesLoader() {

		solutions = new ConcurrentHashMap<String, SolutionDB>();
		try {

			this.configFile = CONFIG;
			load(init());
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fintellix.hadoop.applications.util.AbstractPropertyLoader#load(org
	 * .json.simple.JSONObject)
	 */
	@Override
	public void load(JSONObject jsonObject) throws Throwable {
		if (jsonObject == null)
			return; // we don't have marts.json file
		String driverClazz = (String) jsonObject.get("driverClassName");
		JSONArray solutions = (JSONArray) jsonObject.get("solutions");
		JSONObject solution;
		String solutionName;
		String clientCode;
		for (Object o : solutions) {
			solution = (JSONObject) o;
			SolutionDB x = new SolutionDB();
			x.setDbType((String) solution.get("dbType"));
			x.setDriverClazz(driverClazz);
			solutionName = ((String) solution.get("solution_name"));
			clientCode=(String)solution.get("clientCode");
			x.setJdbcUrl((String) solution.get("jdbc_url"));
			x.setUserName((String) solution.get("database_username"));
			x.setPassword((String) solution.get("database_password"));
			x.setIsPrimaryFrequency((String) solution.get("isPrimaryFrequency"));
			x.setSolutionName(solutionName);
			x.setSolutionId(((Long) solution.get("solution_id")).intValue());

			// set up pool properties, if configured.
			if (solution.get("initialSize") != null) {
				x.setInitialSize(((Long) solution.get("initialSize"))
						.intValue());
			}
			if (solution.get("maxTotal") != null) {
				x.setMaxTotal(((Long) solution.get("maxTotal")).intValue());
			}
			if (solution.get("maxIdle") != null) {
				x.setMaxIdle(((Long) solution.get("maxIdle")).intValue());
			}
			if (solution.get("minIdle") != null) {
				x.setMinIdle(((Long) solution.get("minIdle")).intValue());
			}
			if (solution.get("maxWaitMillis") != null) {
				x.setMaxWaitMillis(((Long) solution.get("maxWaitMillis"))
						.intValue());
			}
			if (solution.get("dbType") != null) {
				x.setDbType((String)solution.get("dbType"));
			}
			
			if(solution.get("reportStatusURL")!=null){
				x.setReportStatusURL((String)solution.get("reportStatusURL"));
			}
			x.setClientCode(clientCode);
			this.solutions.put(clientCode+solutionName, x);

		}

	}

	public class SolutionDB {

		private String driverClazz;
		private String jdbcUrl;
		private String userName;
		private String password;
		private String isPrimaryFrequency;
		private String dbType;
		private String reportStatusURL;
		private String solutionName;
		private Integer solutionId;
		private String clientCode;

		// The initial number of connections that are created when the pool is
		// started.
		private Integer initialSize = 10;

		// The maximum number of active connections that can be allocated
		// from this pool at the same time, or negative for no limit.
		private Integer maxTotal = -1;

		// The maximum number of connections that can remain idle in the pool,
		// without extra ones being released, or negative for no limit.
		private Integer maxIdle = -1;

		// The minimum number of connections that can remain idle in the pool,
		// without extra ones being created, or zero to create none.
		private Integer minIdle = 10;

		// The maximum number of milliseconds that the pool will wait (when
		// there are no available connections)
		// for a connection to be returned before throwing an exception, or -1
		// to wait indefinitely.
		private Integer maxWaitMillis = 60000; // 60 seconds.


		/**
		 * @return the driverClazz
		 */
		public String getDriverClazz() {
			return driverClazz;
		}

		/**
		 * @param driverClazz
		 *            the driverClazz to set
		 */
		public void setDriverClazz(String driverClazz) {
			this.driverClazz = driverClazz;
		}

		/**
		 * @return the jdbcUrl
		 */
		public String getJdbcUrl() {
			return jdbcUrl;
		}

		/**
		 * @param jdbcUrl
		 *            the jdbcUrl to set
		 */
		public void setJdbcUrl(String jdbcUrl) {
			this.jdbcUrl = jdbcUrl;
		}

		/**
		 * @return the userName
		 */
		public String getUserName() {
			return userName;
		}

		/**
		 * @param userName
		 *            the userName to set
		 */
		public void setUserName(String userName) {
			this.userName = userName;
		}

		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * @return the initialSize
		 */
		public Integer getInitialSize() {
			return initialSize;
		}

		/**
		 * @param initialSize
		 *            the initialSize to set
		 */
		public void setInitialSize(Integer initialSize) {
			this.initialSize = initialSize;
		}

		/**
		 * @return the maxTotal
		 */
		public Integer getMaxTotal() {
			return maxTotal;
		}

		/**
		 * @param maxTotal
		 *            the maxTotal to set
		 */
		public void setMaxTotal(Integer maxTotal) {
			this.maxTotal = maxTotal;
		}

		/**
		 * @return the maxIdle
		 */
		public Integer getMaxIdle() {
			return maxIdle;
		}

		/**
		 * @param maxIdle
		 *            the maxIdle to set
		 */
		public void setMaxIdle(Integer maxIdle) {
			this.maxIdle = maxIdle;
		}

		/**
		 * @return the minIdle
		 */
		public Integer getMinIdle() {
			return minIdle;
		}

		/**
		 * @param minIdle
		 *            the minIdle to set
		 */
		public void setMinIdle(Integer minIdle) {
			this.minIdle = minIdle;
		}

		/**
		 * @return the maxWaitMillis
		 */
		public Integer getMaxWaitMillis() {
			return maxWaitMillis;
		}

		/**
		 * @param maxWaitMillis
		 *            the maxWaitMillis to set
		 */
		public void setMaxWaitMillis(Integer maxWaitMillis) {
			this.maxWaitMillis = maxWaitMillis;
		}

		@Override
		public String toString() {
			return "SolutionDB [driverClazz=" + driverClazz + ", jdbcUrl=" + jdbcUrl + ", userName=" + userName
					+ ", password=" + password + ", isPrimaryFrequency=" + isPrimaryFrequency + ", dbType=" + dbType
					+ ", reportStatusURL=" + reportStatusURL + ", solutionName=" + solutionName + ", solutionId="
					+ solutionId + ", clientCode=" + clientCode + ", initialSize=" + initialSize + ", maxTotal="
					+ maxTotal + ", maxIdle=" + maxIdle + ", minIdle=" + minIdle + ", maxWaitMillis=" + maxWaitMillis
					+ "]";
		}

		public String getDbType() {
			return dbType;
		}

		public void setDbType(String dbType) {
			this.dbType = dbType;
		}

		public String getIsPrimaryFrequency() {
			return isPrimaryFrequency;
		}

		public void setIsPrimaryFrequency(String isPrimaryFrequency) {
			this.isPrimaryFrequency = isPrimaryFrequency;
		}

		public String getReportStatusURL() {
			return reportStatusURL;
		}

		public void setReportStatusURL(String reportStatusURL) {
			this.reportStatusURL = reportStatusURL;
		}

		public String getSolutionName() {
			return solutionName;
		}

		public void setSolutionName(String solutionName) {
			this.solutionName = solutionName;
		}

		public Integer getSolutionId() {
			return solutionId;
		}

		public void setSolutionId(Integer solutionId) {
			this.solutionId = solutionId;
		}

		public String getClientCode() {
			return clientCode;
		}

		public void setClientCode(String clientCode) {
			this.clientCode = clientCode;
		}

	}

	/**
	 * @return the instance
	 */
	public static ConnectionPropertiesLoader getInstance() {
		return instance;
	}

	/**
	 * @return the solutions
	 */
	public Map<String, SolutionDB> getSolutions() {
		return solutions;
	}

	public SolutionDB getSolution(String solutionName) {
		return solutions.get(solutionName);
	}

	public SolutionDB getCurrentSolution() {
		return solutions.get(Integer.parseInt((System.getenv("SOLUTION_ID"))));
	}

}
