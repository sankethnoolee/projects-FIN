/**
 * 
 */
package com.fintellix.platformcore.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vishwanath.varanasi
 *
 */
public class DBSwitch {

	private static final String MS_SQL = "mssql";
	protected static final String SYBASEIQ_DB = "sybaseiq";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected Boolean appDbSybase;
	protected Boolean martDbSybase;
	protected String appDbType;
	protected String martDbType;
	protected Boolean isMSSQL;
	
	

	public String getMartDbType() {
		return martDbType;
	}



	/**
	 * @param appDbType the appDbType to set
	 */
	public void setAppDbType(String appDbType) {
		this.appDbType = appDbType;
		if ( this.appDbType.equalsIgnoreCase(SYBASEIQ_DB)){
			appDbSybase = Boolean.TRUE;
		}
		
		else {
			appDbSybase = Boolean.FALSE;
		}
		
		if (logger.isInfoEnabled()){
			logger.info(" Appdb is Sybase IQ ?  " + appDbSybase);
		}
	}

	

	/**
	 * @param martDbType the martDbType to set
	 */
	public void setMartDbType(String martDbType) {
		this.martDbType = martDbType;
		if ( this.martDbType.equalsIgnoreCase(SYBASEIQ_DB)){
			martDbSybase = Boolean.TRUE;
		}
		
		else {
			martDbSybase = Boolean.FALSE;
		}
		
		if (logger.isInfoEnabled()){
			logger.info(" Mart db is Sybase IQ ?   " + martDbSybase);
		}
		
		if ( this.martDbType.equalsIgnoreCase(MS_SQL)){
			isMSSQL = Boolean.TRUE;
		}
		
		else {
			isMSSQL = Boolean.FALSE;
		}
	}

	/**
	 * @return the appDbSybase
	 */
	public Boolean isAppDbSybase() {
		return appDbSybase;
	}


	/**
	 * @return the martDbSybase
	 */
	public Boolean isMartDbSybase() {
		return martDbSybase;
	}



	/**
	 * @return the isMSSQL
	 */
	public Boolean getIsMSSQL() {
		return isMSSQL;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DBSwitch [appDbSybase=");
		builder.append(appDbSybase);
		builder.append(", martDbSybase=");
		builder.append(martDbSybase);
		builder.append(", appDbType=");
		builder.append(appDbType);
		builder.append(", martDbType=");
		builder.append(martDbType);
		builder.append("]");
		return builder.toString();
	}



}
