/*********************************************************************************
 * TODO DESCRIPTION: 
 * Created on May 27, 2007
 * Author Angshuman Sarkar 
 * Copyright (C) 2006 i-Create Software India Pvt Ltd. All Rights Reserved.
 *
 * $Id: UserContextUtil.java 2720 2007-09-03 09:30:52Z angshu $
 * $LastChangedRevision: 2720 $
 * $LastChangedDate: 2007-09-03 15:00:52 +0530 (Mon, 03 Sep 2007) $
 * $LastChangedBy: angshu $
 *
 ********************************************************************************/
package com.fintellix.platformcore.web.utils;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fintellix.administrator.model.Users;
import com.fintellix.utils.security.VyasaCache;
import com.fintellix.utils.security.VyasaSecurityUser;

public class AppContextUtil {
	
	public static final Logger logger = LoggerFactory.getLogger("com.fintellix.platformcore.utils.UserContextUtil");
	private static Properties ApplicationProperties;
	static{
		try {
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("collaboration.properties");
			ApplicationProperties = new Properties();
			ApplicationProperties.load(is);
		}catch (Exception e) {
			throw new RuntimeException("Coudnt read collaboration  properties from class path",e);
		}
	}


	public static final String userAnalysesKey = ApplicationProperties.getProperty("app.useranalyses");
	public static final String userReportsKey = ApplicationProperties.getProperty("app.userreports");	
	public static final String solutionName = ApplicationProperties.getProperty("app.solutionName");
	public static final String userdashboardDefKey = ApplicationProperties.getProperty("app.userdashboardDef");
	public static final String userAnalysisPackDefKey = ApplicationProperties.getProperty("app.userAnalysisPackDef");
	public static final String CURRENT_PRODUCT = ApplicationProperties.getProperty("app.currentProduct");
	public static final String CURRENT_SOLUTION = ApplicationProperties.getProperty("app.currentSolution");
	public static final String CURRENT_ORG_UNIT = ApplicationProperties.getProperty("app.currentOrgUnit");
	public static final String LINKED_ORG_UNITS = ApplicationProperties.getProperty("app.linkedOrgUnits");
	public static final String IS_LEAD_ORG = ApplicationProperties.getProperty("app.isLeadOrg");
	public static final String CURRENT_PROJECTS = "currentProjects";
	public static final String ACCESSID = "ACCESSID";
	public static final String TERMSANDCONDITIONS = "TERMSANDCONDITIONS";
	
	public static final void setUserContextAttribute(
            String key, 
            Object value) {
		VyasaSecurityUser user = com.fintellix.utils.security.SecurityContextUtil.getVyasaUser();
		if (user != null) {
			user.getUsercache().setAttribute(key, value);
		}
    }
	
	public static final Boolean isLeadOrganisation(){
		return (Boolean) getUserContextAttribute(AppContextUtil.IS_LEAD_ORG);
		
	}
	public static final void setUserContextAttribute(
            String key, 
            Object value,VyasaSecurityUser user) {
		if (user != null) {
			user.getUsercache().setAttribute(key, value);
		}
    }
	
	public static final Object getUserContextAttribute(String key) {
		VyasaSecurityUser user = com.fintellix.utils.security.SecurityContextUtil.getVyasaUser();
		if (user != null) {
			return user.getUsercache().getAttribute(key);
		} else 
			return null;
    }
	
	public static Users getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof VyasaSecurityUser) {
			return ((VyasaSecurityUser) principal).getUsers();
		} else {
			return null;
		}
	}
	
	public static final void removeUserContextAttribute(String key) {
		VyasaSecurityUser user = com.fintellix.utils.security.SecurityContextUtil.getVyasaUser();
		if (user != null) {
			user.getUsercache().removeAttibute(key);
		}
    }
	
	public static final void debugUserContext() {
		VyasaSecurityUser user = com.fintellix.utils.security.SecurityContextUtil.getVyasaUser();
		if (user != null) {
			logger.info("User found in Context:" + user);
			VyasaCache userCache = user.getUsercache();
			List<String> keys = userCache.keySet();
		   for (String key: keys) {
			  logger.info("\n Key " + key + " = " + userCache.getAttribute(key));
		   }			
		}
	}
	
	
	
	
    public boolean isUserInRole(String[] roles) {
		return true;
	}
    
    public static String getUserSolutionName() {
    	Object keyValue = getUserContextAttribute(solutionName);		
		if (keyValue != null) {
		   return (String) keyValue;
		} else
			return null;
    }
   
}
