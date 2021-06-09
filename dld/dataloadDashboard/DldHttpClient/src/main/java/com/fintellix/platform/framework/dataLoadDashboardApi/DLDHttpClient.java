package com.fintellix.platform.framework.dataLoadDashboardApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintellix.platform.framework.dataLoadDashboardApi.dto.RequestPayLoad;
import com.fintellix.platform.framework.dataLoadDashboardApi.exception.ResponseException;
import com.google.common.base.Joiner;

/**
 * @author varun.paramasivam
 *
 */
public class DLDHttpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(DLDHttpClient.class);
	private static final Properties prop = new Properties();
	private static String ENDPOINTURL;	
	
	private CloseableHttpClient httpclient = HttpClients.createDefault();
	private ObjectMapper mapper = new ObjectMapper();
	private RequestPayLoad rpl;
	
	
	private String getOTP(){
		LOGGER.info("DLDHttpClient -- getOTP");
		HttpGet getMethod = new HttpGet(ENDPOINTURL+"/API/getdOTP");
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(getMethod);
		} catch (IOException  e) {
			LOGGER.error(e.getMessage(), e);
			System.err.println("Get OTP failed, the end point is not rechable");
			throw new ResponseException("Get OTP failed, the end point is not rechable");
		} 
		if (response.getStatusLine().getStatusCode()!=200)
			throw new ResponseException("Get OTP failed, bad request");
		else
			try {
				return EntityUtils.toString(response.getEntity(), "UTF-8") ;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				LOGGER.debug(response.getEntity().toString());
				System.err.println("Get OTP failed, response parse failed");
				throw new ResponseException("Get OTP failed, response parse failed");
			}
	}
	
	
	public String pushStatistics(){
		LOGGER.info("DLDHttpClient -- pushStatistics");
		if (rpl==null)
			throw new RuntimeException("Request payload is null");
		
	    try {
			rpl.setOtp(getOTP());
		    //postParameters.add(new BasicNameValuePair("params", mapper.writeValueAsString(rpl)));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.error(rpl.toString());
			throw new RuntimeException("Json Object Maper failed");
		}
		
	    HttpPost postMethod=null;
		
		try {
			try {
				postMethod = new HttpPost(ENDPOINTURL+"/API/gatherstats?"+"params="+URLEncoder.encode(mapper.writeValueAsString(rpl), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		} 
		postMethod.setHeader("Content-Type", "application/json");
		//postMethod.setEntity(new UrlEncodedFormEntity(postParameters));
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(postMethod);
		} catch (IOException  e) {
			LOGGER.error(e.getMessage(), e);
			throw new ResponseException("Push Statistics failed, the end point is not rechable");
		}
		if (response.getStatusLine().getStatusCode()!=200)
			throw new ResponseException("Push Statistics failed, bad request");
		else
			try {
				return EntityUtils.toString(response.getEntity(), "UTF-8") ;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				LOGGER.debug(response.getEntity().toString());
				throw new ResponseException("Push Statistics failed, response parse failed");
			}
	}
	
	/**
	 * @param args
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static void main(String[] args) {
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for(String keyValue: Joiner.on("").skipNulls().join(args).split(",")){
			if(!"".equals(keyValue.trim()))
					keyValueMap.put(keyValue.trim().split("#")[0], keyValue.trim().split("#").length<=1? "":keyValue.trim().split("#")[1]);
		}
		 
		if(keyValueMap.size()!=19){
			System.err.println("Invalid Number of Argumnets");
			System.exit(0);
		}

		if(!Arrays.asList("endPointURL","clientCode","taskRepo","taskName","flowType","flowSeqNo","runStatus","taskTechName","taskTechSubName","runDetails","srcCnt","tgtCnt","rejectedRows","affectedRows","appliedRows","taskStartTime","taskEndTime","runPeriodId","businessPeriodId").containsAll(keyValueMap.keySet())){
			System.err.println("Invalid key found. List of valid key" + Arrays.asList("endPointURL","clientCode","taskRepo","taskName","flowType","flowSeqNo","runStatus","taskTechName","taskTechSubName","runDetails","srcCnt","tgtCnt","rejectedRows","affectedRows","appliedRows","taskStartTime","taskEndTime","runPeriodId","businessPeriodId"));
			System.exit(0);
		}
		ENDPOINTURL = keyValueMap.get("endPointURL");
		DLDHttpClient dldClient = new DLDHttpClient();
		dldClient.rpl = new RequestPayLoad();
		dldClient.rpl.setTaskName(keyValueMap.get("taskName"));
		dldClient.rpl.setTaskStartTime(keyValueMap.get("taskStartTime"));
		dldClient.rpl.setTaskEndTime(keyValueMap.get("taskEndTime"));
		dldClient.rpl.setAppliedRows(keyValueMap.get("appliedRows"));
		dldClient.rpl.setAffectedRows(keyValueMap.get("affectedRows"));
		dldClient.rpl.setRejectedRows(keyValueMap.get("rejectedRows"));
		dldClient.rpl.setTaskRepo(keyValueMap.get("taskRepo"));
		dldClient.rpl.setFlowSeqNo(keyValueMap.get("flowSeqNo"));
		dldClient.rpl.setFlowType(keyValueMap.get("flowType"));
		dldClient.rpl.setBusinessPeriodId(keyValueMap.get("businessPeriodId"));
		dldClient.rpl.setRunPeriodId(keyValueMap.get("runPeriodId"));
		dldClient.rpl.setSrcCnt(keyValueMap.get("srcCnt"));
		dldClient.rpl.setTgtCnt(keyValueMap.get("tgtCnt"));
		dldClient.rpl.setRunStatus(keyValueMap.get("runStatus"));
		dldClient.rpl.setClientCode(keyValueMap.get("clientCode"));
		dldClient.rpl.setTaskTechName(keyValueMap.get("taskTechName"));
		dldClient.rpl.setTaskTechSubName(keyValueMap.get("taskTechSubName"));
		dldClient.rpl.setTaskRepo(keyValueMap.get("taskRepo"));

		try{
		
		System.err.println(dldClient.pushStatistics());
		} catch (Throwable t){
			System.err.println(t.getMessage());
			System.exit(1000);
		}
				
	}

	
	
}
