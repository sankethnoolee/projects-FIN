package com.fintellix.platformcore.common.hibernate;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Base helper class for Daos
 *
 * @author greeshma.s
 */
public class VyasaHibernateDaoSupport {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * For platform session
     *
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * For platform session
     *
     * @return
     */
    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Uses platform session
     *
     * @param query
     * @return
     */
    public List find(String query) {
        logger.info("EXEFLOW - VyasaHibernateDaoSupport -> find() with query:" + query);
        Query queryObject = getSession().createQuery(query);
        return queryObject.list();
    }

    /**
     * Uses platform session
     *
     * @param queryString
     * @param values
     * @return
     */
    public List find(String queryString, Object... values) {
        logger.info("EXEFLOW - VyasaHibernateDaoSupport -> find() with query:" + queryString);
        Query queryObject = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.list();
    }


    public void saveAll(final Collection entities) {
        Session s = getSession();
        for (Object entity : entities) {
            s.save(entity);
        }
    }


    /**
     * Uses platform session
     *
     * @param entities
     */
    public void saveOrUpdateAll(final Collection entities) {
        Session s = getSession();
        int i = 0;
        logger.debug("Collection ---" + entities.size());

        for (Object entity : entities) {
            entity.getClass().getName();
            i++;
            s.saveOrUpdate(entity);
            if (i % 1500 == 0) {
                s.flush();
                s.clear();
                i = 0;
                logger.debug("Collection -- Entity type --" + entity.getClass().getName());
            }
        }
    }

    /**
     * Uses platform session
     *
     * @param entities
     */
    public void deleteAll(final Collection entities) {
        for (Object entity : entities) {
            getSession().delete(entity);
        }
    }

    /**
     * Uses platform session
     *
     * @param queryString
     * @return
     */
    public int bulkUpdate(String queryString) {
        return bulkUpdate(queryString, (Object[]) null);
    }

    /**
     * Uses platform session
     *
     * @param queryString
     * @param value
     * @return
     */
    public int bulkUpdate(String queryString, Object value) {
        return bulkUpdate(queryString, new Object[]{value});
    }

    /**
     * Uses platform session
     *
     * @param queryString
     * @param values
     * @return
     */
    public int bulkUpdate(final String queryString, final Object... values) {
        Query queryObject = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.executeUpdate();
    }

    /**
     * Uses platform session
     *
     * @param queryString
     * @param paramName
     * @param value
     * @return
     */
    public List findByNamedParam(String queryString, String paramName, Object value) {
        return findByNamedParam(queryString, new String[]{paramName}, new Object[]{value});
    }

    /**
     * Uses platform session
     *
     * @param queryString
     * @param paramNames
     * @param values
     * @return
     */
    public List findByNamedParam(final String queryString, final String[] paramNames, final Object[] values) {

        Query queryObject = getSession().createQuery(queryString);
        if (values != null && paramNames != null) {
            if (paramNames.length != values.length) {
                throw new IllegalArgumentException("Length of paramNames array must match length of values array");
            }
            for (int i = 0; i < values.length; i++) {
                applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
            }
        }
        return queryObject.list();
    }

    private void applyNamedParameterToQuery(Query queryObject, String paramName, Object value) {

        if (value instanceof Collection) {
            queryObject.setParameterList(paramName, (Collection) value);
        } else if (value instanceof Object[]) {
            queryObject.setParameterList(paramName, (Object[]) value);
        } else {
            queryObject.setParameter(paramName, value);
        }
    }

    public List find(Session session, String query) {
        logger.info("EXEFLOW - VyasaHibernateDaoSupport -> find() with query:" + query);
        Query queryObject = session.createQuery(query);
        return queryObject.list();
    }

    public List find(Session session, String queryString, Object... values) {
        logger.info("EXEFLOW - VyasaHibernateDaoSupport -> find() with query:" + queryString);
        Query queryObject = session.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.list();
    }

    public void saveOrUpdateAll(Session session, final Collection entities) {
        for (Object entity : entities) {
            session.saveOrUpdate(entity);
        }
    }

    public void deleteAll(Session session, final Collection entities) {
        for (Object entity : entities) {
            session.delete(entity);
        }
    }

    public int bulkUpdate(Session session, String queryString) {
        return bulkUpdate(session, queryString, (Object[]) null);
    }

    public int bulkUpdate(Session session, String queryString, Object value) {
        return bulkUpdate(session, queryString, new Object[]{value});
    }

    public int bulkUpdate(Session session, final String queryString, final Object... values) {
        Query queryObject = session.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.executeUpdate();
    }

    public List findByNamedParam(Session session, String queryString, String paramName, Object value) {
        return findByNamedParam(session, queryString, new String[]{paramName}, new Object[]{value});
    }

    public List findByNamedParam(Session session, final String queryString, final String[] paramNames, final Object[] values) {

        Query queryObject = session.createQuery(queryString);
        if (values != null && paramNames != null) {
            if (paramNames.length != values.length) {
                throw new IllegalArgumentException("Length of paramNames array must match length of values array");
            }
            for (int i = 0; i < values.length; i++) {
                applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
            }
        }
        return queryObject.list();
    }
}
