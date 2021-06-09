package com.fintellix.dld.authentication.authhelper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.fintellix.dld.dao.DldDao;
import com.fintellix.dld.models.UserDetail;

@Component
public class DldUserAuthenticationFromDbData implements UserDetailsService  {
	static Logger LOGGER = LoggerFactory.getLogger(DldUserAuthenticationFromDbData.class);

	@Autowired
	private DldDao dldDao;
	
	public UserDetail loadUserByUsername(String userName){
		LOGGER.info("DldUserAuthenticationFromDbData -- > loadUserByUsername ");
		UserDetail userDetail=null;
		try {
			userDetail = dldDao.loadUserByUsername(userName);
		} catch (Throwable e) {
			LOGGER.error("Error",e);
		}
		
		return userDetail;
	}
	
	


}
