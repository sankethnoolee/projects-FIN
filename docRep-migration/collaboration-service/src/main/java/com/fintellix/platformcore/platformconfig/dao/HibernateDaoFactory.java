package com.fintellix.platformcore.platformconfig.dao;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.platformcore.common.hibernate.VyasaHibernateDaoSupport;
//import com.fintellix.products.traq.npa.dao.NPAClassificationDao;
//import com.fintellix.products.adf.requestprocessor.dao.RequestProcessorAppDao;

// refer to the comments in the DaoFactory file for changes

public class HibernateDaoFactory extends DaoFactory {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private SessionFactory sessionFactory;
	/**
	 * Default Constructor
	 */
	public HibernateDaoFactory() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor
	 * 
	 * @param sessionFactory
	 */
	public HibernateDaoFactory(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return Returns the sessionFactory.
	 */
	public SessionFactory getSessionFactory() {
		logger.info("EXEFLOW - HibernateDaoFactory -> getSessionFactory()");
		return sessionFactory;
	}

	/**
	 * @param sessionFactory
	 *            The sessionFactory to set.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		logger.info("EXEFLOW - HibernateDaoFactory -> setSessionFactory()");
		this.sessionFactory = sessionFactory;
	}




}
