package com.fintellix.dld.util;

import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintellix.dld.dbConnection.PersistentStoreManager;

public class SolutionReportStatus {
	private final static Properties applicationProperties;
	static{
		
		try {		
			
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(is);

		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application / data-dashboard-queries  properties from class path",e);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(SolutionReportStatus.class);
	private static final int timeout = Integer.parseInt(applicationProperties.getProperty("dld.reportStatusRequest.timeout"));
	private static final RequestConfig config = RequestConfig.custom().
	  setConnectTimeout(timeout).
	  setConnectionRequestTimeout(timeout).
	  setSocketTimeout(timeout).build();
	private static CloseableHttpClient httpclient = HttpClientBuilder.create()
			  .setDefaultRequestConfig(config).build();
	private static ObjectMapper mapper = new ObjectMapper();
	public static ReportStatusPayload getStatusForReportForSolutions(Map<String,String> mapOfReportIDAndLineItems,String solutionName,String periodId,String solutionId,String clientCode) throws Throwable{
		LOGGER.info("EXEFLOW -> SolutionReportStatus -> getStatusForReportForSolutions");

		ReportStatusPayload reportStatusPayload = new ReportStatusPayload();

		List<ReportInfo> reportInfoList = new ArrayList<ReportInfo>();
		ReportInfo info;
		String lineItemsCSV="";
		for(Entry<String, String> lineItems :mapOfReportIDAndLineItems.entrySet()){
			info = new ReportInfo();
			if(lineItems.getValue()!=null && !"".equalsIgnoreCase(lineItems.getValue())){
				lineItemsCSV="";
				for(String li:lineItems.getValue().split(",")){
					lineItemsCSV=lineItemsCSV+li.split("@##@")[0]+",";
				}
			}
			if(lineItemsCSV.length()>0){
				info.setLineItemIds(lineItemsCSV.substring(0, lineItemsCSV.length()-1));
			}
			
			info.setPeriodId(periodId);
			info.setRegReportId(lineItems.getKey());
			reportInfoList.add(info);
		}
		reportStatusPayload.setFetchAllLineItems("false");
		reportStatusPayload.setSolutionId(solutionId);
		reportStatusPayload.setReportInfo(reportInfoList);


		StringEntity postRequestEntity;


		try {

			postRequestEntity = new StringEntity(mapper.writeValueAsString(reportStatusPayload),ContentType.APPLICATION_JSON);
		} catch (UnsupportedCharsetException | JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException("Json Object Maper failed");
		}
		
		String endPointURL = PersistentStoreManager.getReportStatusURLForSolution(solutionName,clientCode);
		HttpPost postMethod = new HttpPost(endPointURL);

		postMethod.setEntity(postRequestEntity);
		CloseableHttpResponse response;
		response = httpclient.execute(postMethod);
		if (response.getStatusLine().getStatusCode()!=200)
			throw new RuntimeException("Bad Response, URL endpoint might be down");
		return mapper.readValue(EntityUtils.toString(response.getEntity()), ReportStatusPayload.class);
	}
}
