package com.fintellix.framework.validation.dao;

import org.springframework.stereotype.Component;

@Component
public abstract class DaoFactory {

    public abstract ValidationDao getValidationDao();

    public abstract ValidationAPIDao getValidationAPIDao();
}
