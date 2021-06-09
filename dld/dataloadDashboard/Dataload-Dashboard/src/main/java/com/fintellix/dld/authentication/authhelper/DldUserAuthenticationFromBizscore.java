package com.fintellix.dld.authentication.authhelper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import net.sf.json.JSONObject;
@Component
public class DldUserAuthenticationFromBizscore {
	private static final Logger LOGGER = LoggerFactory.getLogger(DldUserAuthenticationFromBizscore.class);

	public JSONObject getAuthenticationDetails(String userName,String password,String solutionUrl){
		LOGGER.info("Fetching Authentication Details From Bizscore.");
		JSONObject publishResponseJson = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		RestTemplate rt = new RestTemplate();
		rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		rt.getMessageConverters().add(new StringHttpMessageConverter());
		MultiValueMap<String,String> bodyMap = new LinkedMultiValueMap<String, String>();
		bodyMap.add("j_username", userName);
		bodyMap.add("j_password", password);
		bodyMap.add("loginbtn", "Login");
		bodyMap.add("requestOrigin", "dataLoadDashboard");
		HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<MultiValueMap<String,String>>(bodyMap, headers);
		ResponseEntity<String> publishResponse = rt.exchange(solutionUrl, HttpMethod.POST, entity, String.class);
		if (publishResponse.getStatusCode() == HttpStatus.OK) {
			publishResponseJson=JSONObject.fromObject(publishResponse.getBody());
		} 
		return publishResponseJson;
	}
	
}
