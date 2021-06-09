package com.fintellix.framework.collaboration.dao;

import org.springframework.stereotype.Component;

@Component
public abstract class DaoFactory {
	public abstract DocumentManagerDao getDocumentManagerDao();
}
