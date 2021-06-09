package com.fintellix.framework.collaboration.dao;

import org.hibernate.SessionFactory;

public class HibernateDaoFactory extends DaoFactory{
	
	private DocumentManagerDao documentManagerDao;
	
	
	public HibernateDaoFactory(){
		super();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	public DocumentManagerDao getDocumentManagerDao() {
		return documentManagerDao;
	}

	public void setDocumentManagerDao(DocumentManagerDao documentManagerDao) {
		this.documentManagerDao = documentManagerDao;
	}
	private SessionFactory sessionFactory;

}
