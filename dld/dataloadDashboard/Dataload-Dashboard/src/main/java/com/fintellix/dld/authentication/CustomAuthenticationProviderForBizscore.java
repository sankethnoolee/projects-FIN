package com.fintellix.dld.authentication;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.fintellix.dld.authentication.authhelper.DldUserAuthenticationFromBizscore;
import com.fintellix.dld.util.SolutionURLMappingPropertiesLoader;

import net.sf.json.JSONObject;



@Component
public class CustomAuthenticationProviderForBizscore implements AuthenticationProvider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProviderForBizscore.class);

	@Value("${dld.AllowedUser}")
	private String userBucketPath;
	
	
	@Autowired HttpServletRequest request;
	@Autowired HttpServletResponse respone;
	@Autowired HttpSession session;
	@Autowired DldUserAuthenticationFromBizscore dldAuthentication;
	@Override
	public Authentication authenticate(Authentication auth) 
			throws AuthenticationException {
		String solutionName = request.getParameter("solutionName");
		LOGGER.info("Trying application for authentication -- "+solutionName);
		String username = auth.getName();
		String password = auth.getCredentials().toString();
		SolutionURLMappingPropertiesLoader x = SolutionURLMappingPropertiesLoader.getInstance();
		String solutionUrl = x.getSolutions().get(solutionName).getSolutionURL();
		
		JSONObject publishResponseJson = new JSONObject();
		try{
			publishResponseJson = (JSONObject)dldAuthentication.getAuthenticationDetails(username, password, solutionUrl);
			if (null!=publishResponseJson.get("sucess") && (Boolean) publishResponseJson.get("sucess")) {
						if (!userBucketPath.equals(publishResponseJson.get("userRole")))
							throw new BadCredentialsException("User is Not Authorised");
						//successful authentication
						session.setAttribute("username", publishResponseJson.get("userName"));
						session.setAttribute("userRole", publishResponseJson.get("userRole"));
						session.setAttribute("userId", publishResponseJson.get("userId"));
						session.setAttribute("clientCode", publishResponseJson.get("clientCode"));
						session.setAttribute("solutionName", solutionName);
						return new UsernamePasswordAuthenticationToken
								(username, password,Collections.emptyList());
				}else{
					//unsuccessful authentication
					throw new BadCredentialsException("External system authentication failed");
				}
		}catch (Exception e){
			LOGGER.info("Authentication failed-- "+solutionName);
			throw new BadCredentialsException("External system authentication failed");
		}
	}

	@Override
	public boolean supports(Class<?> auth) {
		return auth.equals(UsernamePasswordAuthenticationToken.class);
	}
}
