package com.fintellix.framework.validation.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HibernateDaoFactory extends DaoFactory {
    protected static final Log logger = LogFactory.getLog(HibernateDaoFactory.class);

    @Autowired
    ValidationDao validationDao;

    @Autowired
    ValidationAPIDao validationAPIDao;

    private SessionFactory sessionFactory;

    /**
     * Default Constructor
     */
    public HibernateDaoFactory() {
        super();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setValidationDao(ValidationDao validationDao) {
        this.validationDao = validationDao;
    }

    public void setValidationAPIDao(ValidationAPIDao validationAPIDao) {
        this.validationAPIDao = validationAPIDao;
    }

    @Override
    public com.fintellix.framework.validation.dao.ValidationDao getValidationDao() {
        return validationDao;
    }

    @Override
    public com.fintellix.framework.validation.dao.ValidationAPIDao getValidationAPIDao() {
        return validationAPIDao;
    }

}
