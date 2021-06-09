package com.fintellix.platformcore.search.dao;

import java.util.Map;

import org.hibernate.SessionFactory;

public class SolutionSpecificSessionFinder {
	
	private Map<String, SessionFactory> sessionFactoryMap;
	private Map<String, String> sessionFactoryTypeMap;
	
	public void setSessionFactoryMap(Map<String, SessionFactory> sessionFactoryMap) {
		this.sessionFactoryMap = sessionFactoryMap;
	}
	
	public Map<String, SessionFactory> getSessionFactoryMap() {
		return this.sessionFactoryMap;
	}

	public SessionFactory getSessionFactory(String solutionName){
		SessionFactory sessionFactory = sessionFactoryMap.get(solutionName);
		if(sessionFactory == null){
			sessionFactory = sessionFactoryMap.get("DEFAULT");
		}
		return sessionFactory;
	}

	public Map<String, String> getSessionFactoryTypeMap() {
		return sessionFactoryTypeMap;
	}

	public void setSessionFactoryTypeMap(Map<String, String> sessionFactoryTypeMap) {
		this.sessionFactoryTypeMap = sessionFactoryTypeMap;
		
	}
	

}
