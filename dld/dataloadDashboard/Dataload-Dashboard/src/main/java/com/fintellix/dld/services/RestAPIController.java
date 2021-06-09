package com.fintellix.dld.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aspose.cells.FileFormatType;
import com.aspose.cells.Workbook;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fintellix.dld.bo.DldBo;
import com.fintellix.dld.models.DldStatusDownload;
import com.fintellix.dld.models.TaskExecutionLog;
import com.fintellix.dld.models.TaskExecutionLogDetails;
import com.fintellix.dld.security.OTPAuthenticator;
import com.fintellix.dld.security.OTPGenerator;
import com.fintellix.dld.util.UploaderHelper;

@RestController
public class RestAPIController {
	private static final Logger logger = LoggerFactory.getLogger(RestAPIController.class);

	private static Properties applicationProperties;
	static{
		try {
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(is);

		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application / data-dashboard-queries  properties from class path",e);
		}
	}

	private static final String PERIOD_DATE_FORMAT=applicationProperties.getProperty("dld.periodIdFormat");


	private static final String dateFormat=applicationProperties.getProperty("dld.dateFormat");

	@Value("${dld.defaultFlowType}")
	private String DEFAULT_FLOW_TYPE;

	private DateTimeFormatter dt = DateTimeFormat.forPattern(PERIOD_DATE_FORMAT);
	private DateTimeFormatter dateFormatter=DateTimeFormat.forPattern(dateFormat);
	
	
	
	@Autowired
	private OTPGenerator otpGenerator;

	@Autowired
	private DldBo dldBo;

	@RequestMapping(value = "/API/getdOTP", method = {RequestMethod.GET})
	public ResponseEntity<String> getOTP(HttpServletRequest request) throws Exception {
		logger.info("EXEFLOW->RestAPIController->getOTP");
		ResponseEntity<String> reply = null;
		try

		{
			reply = new ResponseEntity<String>(otpGenerator.getOtp(), HttpStatus.OK);

		}

		catch (Throwable e)

		{
			logger.error(e.getMessage(), e);
			reply = new ResponseEntity<String>(new String(), HttpStatus.NOT_FOUND);
		}

		return reply;
	}

	@RequestMapping(value = "/API/gatherstats", method = {RequestMethod.POST,RequestMethod.GET})
	public ResponseEntity<String> gatherstats(HttpServletRequest request) throws Throwable {
		logger.info("EXEFLOW->RestAPIController->gatherstats");
		ResponseEntity<String> reply = null;
		com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
		String params="";
		String requestOrigin="";
		TaskExecutionLogDetails taskDetail = new TaskExecutionLogDetails();
		boolean isValidationRequired=false;
		boolean nullCheckValidation=true;
		try

		{
			requestOrigin=request.getHeader("origin");
			params=request.getParameter("params");
			objMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			taskDetail=objMapper.readValue(request.getParameter("params"), TaskExecutionLogDetails.class);

			reply = new ResponseEntity<String>(otpGenerator.getOtp(), HttpStatus.OK);


			OTPAuthenticator.getInstance().assertToken(taskDetail.getOtp());

			TaskExecutionLog taskExLog = new TaskExecutionLog();

			if(taskDetail.getRunPeriodId()==null || "".equalsIgnoreCase(taskDetail.getRunPeriodId().trim())){
				logger.info("Run Period Id is null/empty!");
				nullCheckValidation=false;	
			}
			if (taskDetail.getTaskStartTime()==null ||"".equalsIgnoreCase(taskDetail.getTaskStartTime().trim())){
				logger.info("Task Start Time is null/empty!");
				nullCheckValidation=false;
			}
			if(taskDetail.getTaskEndTime()==null ||"".equalsIgnoreCase(taskDetail.getTaskEndTime().trim())){
				logger.info("Task Start End is null/empty!");
				nullCheckValidation=false;
			}
			if(taskDetail.getClientCode()==null || "".equalsIgnoreCase(taskDetail.getClientCode().trim())){
				logger.info("Client Code is null/empty!");
				nullCheckValidation=false;
			}
			if(taskDetail.getTaskName()==null ||"".equalsIgnoreCase(taskDetail.getTaskName().trim())){
				logger.info("Task Name is null/empty!");
				nullCheckValidation=false;
			}
			if(taskDetail.getTaskRepo()==null ||"".equalsIgnoreCase(taskDetail.getTaskRepo().trim())){
				logger.info("Task Repo is null/empty!");
				nullCheckValidation=false;
			}
			

			List<TaskExecutionLog> taskExecutionLogs= new ArrayList<TaskExecutionLog>();
			if(nullCheckValidation){
				if(taskDetail.getBusinessPeriodId()!=null && !"".equalsIgnoreCase(taskDetail.getBusinessPeriodId())){
					for(int i=0;i<(taskDetail.getBusinessPeriodId().split(",")).length;i++){
						taskExLog = new TaskExecutionLog();
						taskExLog.setRunPeriodDate(new Date(dt.parseDateTime(taskDetail.getRunPeriodId()).getMillis()));

						taskExLog.setStartDate(new Date(dateFormatter.parseDateTime(taskDetail.getTaskStartTime().trim()).getMillis()));
						taskExLog.setEndDate(new Date(dateFormatter.parseDateTime(taskDetail.getTaskEndTime().trim()).getMillis()));

						if(taskDetail.getFlowType()!=null & !"".equalsIgnoreCase(taskDetail.getFlowType()))
							taskExLog.setFlowType(taskDetail.getFlowType());
						else
							taskExLog.setFlowType(DEFAULT_FLOW_TYPE);
						taskExLog.setTaskName(taskDetail.getTaskName());

						if(taskDetail.getAppliedRows()!=null && !"".equalsIgnoreCase(taskDetail.getAppliedRows())){
							taskExLog.setTargetInsertedCount((int) Double.parseDouble(taskDetail.getAppliedRows()));
						}

						if(taskDetail.getAffectedRows()!=null && !"".equalsIgnoreCase(taskDetail.getAffectedRows())){
							taskExLog.setTargetUpdatedCount((int) Double.parseDouble(taskDetail.getAffectedRows()));
						}

						if(taskDetail.getRejectedRows()!=null && !"".equalsIgnoreCase(taskDetail.getRejectedRows())){
							taskExLog.setTargetRejectedRecord((int) Double.parseDouble(taskDetail.getRejectedRows()));
						}

						if(taskDetail.getFlowSeqNo()!=null && !"".equalsIgnoreCase(taskDetail.getFlowSeqNo())){
							taskExLog.setFlowSequenceNumber((int) Double.parseDouble(taskDetail.getFlowSeqNo()));
						}

						if(taskDetail.getSrcCnt()!=null && !"".equalsIgnoreCase(taskDetail.getSrcCnt())){
							taskExLog.setSourceCount((int) Double.parseDouble(taskDetail.getSrcCnt()));
						}

						if(taskDetail.getTgtCnt()!=null && !"".equalsIgnoreCase(taskDetail.getTgtCnt())){
							taskExLog.setTargetCount((int) Double.parseDouble(taskDetail.getTgtCnt()));
						}
						isValidationRequired=dldBo.isValidationOnTaskRequired(taskDetail.getClientCode(),taskDetail.getTaskRepo(),taskDetail.getTaskName(),taskExLog.getRunPeriodDate());

						if(taskDetail.getRunStatus()!=null && !"".equalsIgnoreCase(taskDetail.getRunStatus())){
							taskExLog.setRunDetails(taskDetail.getRunStatus());
							taskExLog.setTaskStatus("FAILED");
						} else {
							if(isValidationRequired){
								if(taskExLog.getSourceCount()!=taskExLog.getTargetCount()){
									taskExLog.setRunDetails("SOURCE TARGET COUNT DOES NOT MATCH. VALIDATION FALIED!");
									taskExLog.setTaskStatus("FAILED");
								}
							} else {
								taskExLog.setRunDetails("");
								taskExLog.setTaskStatus("COMPLETED");
							}
						}

						if(taskDetail.getClientCode()!=null && !"".equalsIgnoreCase(taskDetail.getClientCode())){
							taskExLog.setClientCode(taskDetail.getClientCode());
						}

						if(taskDetail.getTaskTechName()!=null && !"".equalsIgnoreCase(taskDetail.getTaskTechName())){
							taskExLog.setTechnicalTaskName(taskDetail.getTaskTechName());
						}

						if(taskDetail.getTaskTechSubName()!=null && !"".equalsIgnoreCase(taskDetail.getTaskTechSubName())){
							taskExLog.setTechnicalSubTaskName(taskDetail.getTaskTechSubName());
						}

						if(taskDetail.getTaskRepo()!=null && !"".equalsIgnoreCase(taskDetail.getTaskRepo())){
							taskExLog.setTaskRepository(taskDetail.getTaskRepo());
						}
						taskExLog.setBusinessDate(new Date(dt.parseDateTime(taskDetail.getBusinessPeriodId().split(",")[i]).getMillis()));					
						taskExecutionLogs.add(taskExLog);
						
						
					}
				} else {
					taskExLog = new TaskExecutionLog();
					taskExLog.setRunPeriodDate(new Date(dt.parseDateTime(taskDetail.getRunPeriodId()).getMillis()));;

					taskExLog.setStartDate(new Date(dateFormatter.parseDateTime(taskDetail.getTaskStartTime().trim()).getMillis()));
					taskExLog.setEndDate(new Date(dateFormatter.parseDateTime(taskDetail.getTaskEndTime().trim()).getMillis()));

					if(taskDetail.getFlowType()!=null & !"".equalsIgnoreCase(taskDetail.getFlowType()))
						taskExLog.setFlowType(taskDetail.getFlowType());
					else
						taskExLog.setFlowType(DEFAULT_FLOW_TYPE);
					taskExLog.setTaskName(taskDetail.getTaskName());

					if(taskDetail.getAppliedRows()!=null && !"".equalsIgnoreCase(taskDetail.getAppliedRows())){
						taskExLog.setTargetInsertedCount((int) Double.parseDouble(taskDetail.getAppliedRows()));
					}

					if(taskDetail.getAffectedRows()!=null && !"".equalsIgnoreCase(taskDetail.getAffectedRows())){
						taskExLog.setTargetUpdatedCount((int) Double.parseDouble(taskDetail.getAffectedRows()));
					}

					if(taskDetail.getRejectedRows()!=null && !"".equalsIgnoreCase(taskDetail.getRejectedRows())){
						taskExLog.setTargetRejectedRecord((int) Double.parseDouble(taskDetail.getRejectedRows()));
					}

					if(taskDetail.getFlowSeqNo()!=null && !"".equalsIgnoreCase(taskDetail.getFlowSeqNo())){
						taskExLog.setFlowSequenceNumber((int) Double.parseDouble(taskDetail.getFlowSeqNo()));
					}

					if(taskDetail.getSrcCnt()!=null && !"".equalsIgnoreCase(taskDetail.getSrcCnt())){
						taskExLog.setSourceCount((int) Double.parseDouble(taskDetail.getSrcCnt()));
					}

					if(taskDetail.getTgtCnt()!=null && !"".equalsIgnoreCase(taskDetail.getTgtCnt())){
						taskExLog.setTargetCount((int) Double.parseDouble(taskDetail.getTgtCnt()));
					}
					isValidationRequired=dldBo.isValidationOnTaskRequired(taskDetail.getClientCode(),taskDetail.getTaskRepo(),taskDetail.getTaskName(),taskExLog.getRunPeriodDate());

					if(taskDetail.getRunStatus()!=null && !"".equalsIgnoreCase(taskDetail.getRunStatus())){
						taskExLog.setRunDetails(taskDetail.getRunStatus());
						taskExLog.setTaskStatus("FAILED");
					} else {
						if(isValidationRequired){
							if(taskExLog.getSourceCount()!=taskExLog.getTargetCount()){
								taskExLog.setRunDetails("SOURCE TARGET COUNT DOES NOT MATCH. VALIDATION FALIED!");
								taskExLog.setTaskStatus("FAILED");
							}
						} else {
							taskExLog.setRunDetails("");
							taskExLog.setTaskStatus("COMPLETED");
						}
					}

					if(taskDetail.getClientCode()!=null && !"".equalsIgnoreCase(taskDetail.getClientCode())){
						taskExLog.setClientCode(taskDetail.getClientCode());
					}

					if(taskDetail.getTaskTechName()!=null && !"".equalsIgnoreCase(taskDetail.getTaskTechName())){
						taskExLog.setTechnicalTaskName(taskDetail.getTaskTechName());
					}

					if(taskDetail.getTaskTechSubName()!=null && !"".equalsIgnoreCase(taskDetail.getTaskTechSubName())){
						taskExLog.setTechnicalSubTaskName(taskDetail.getTaskTechSubName());
					}

					if(taskDetail.getTaskRepo()!=null && !"".equalsIgnoreCase(taskDetail.getTaskRepo())){
						taskExLog.setTaskRepository(taskDetail.getTaskRepo());
					}
					taskExLog.setBusinessDate(new Date(dt.parseDateTime(taskDetail.getRunPeriodId()).getMillis()));					
					taskExecutionLogs.add(taskExLog);
				}

				dldBo.insertTaskStatics(taskExecutionLogs);

				if(isValidationRequired){
					if(taskExLog.getSourceCount()!=taskExLog.getTargetCount()){
						reply = new ResponseEntity<String>("FALSE", HttpStatus.OK);	
					} else {

						reply = new ResponseEntity<String>("TRUE", HttpStatus.OK);
					}	
				} else {
					reply = new ResponseEntity<String>("TRUE", HttpStatus.OK);
				}
			} else {
				reply = new ResponseEntity<String>("FALSE", HttpStatus.OK);	
				logger.error("Mandatory field value/s are not present!");
			}
			

		}
		catch (IllegalStateException e)

		{
			logger.error(e.getMessage(), e);
			reply = new ResponseEntity<String>("ERROR", HttpStatus.NOT_FOUND);
		}
		catch (IllegalArgumentException e){
			logger.error(e.getMessage(), e);
			reply = new ResponseEntity<String>("ERROR", HttpStatus.NOT_FOUND);
		}
		finally{
			dldBo.logAllTask(params,requestOrigin);
		}
		return reply;
	}


	@RequestMapping(value = "/API/getdetails", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<String> getdetails(@RequestParam(value = "otp", required = false) String otp) throws Throwable {
		logger.info("EXEFLOW->RestAPIController->getdetails");
		if (true) {
			OTPAuthenticator.getInstance().assertToken(otp);
		}

		return new ResponseEntity<String>("Heelo Details", HttpStatus.OK);
	}

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/API/getcurrrentBusinessDateStatisticsForLd", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getCurrrentBusinessDateStats(HttpServletRequest request) {
		logger.info("EXEFLOW->RestAPIController->getCurrrentBusinessDateStats");
		JSONObject json = new JSONObject();
		String otp=request.getParameter("otp");
		try
		{	
			OTPAuthenticator.getInstance().assertToken(otp);
			json.put("curBusinessDate",dldBo.getMaxBusinessDate(request.getParameter("clientCode")));
			return new ResponseEntity<JSONObject>(json, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error("getCurrrentBusinessDateStats failed",t);
			return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@RequestMapping(value = "/API/getdataconsumersumary", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getDataConsumerSummary(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,
			@RequestParam(value="otp") String otp,
			@RequestParam(value="clientCode") String clientCode,
			@RequestParam(value="solutionName") String solutionName,
			HttpServletRequest request
			) {
		logger.info("EXEFLOW->RestAPIController->getDataConsumerSummary");
		JSONObject progressDetails = new JSONObject();

		try
		{
			OTPAuthenticator.getInstance().assertToken(otp);	  
			progressDetails=dldBo.getDetailsForDataConsumersSummary(currrentBusinessDate,
					isFrequencyFilterApplied, frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV,clientCode,solutionName);
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error("getDataConsumerSummary failed",t);
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	



	@RequestMapping(value = "/API/getlineitemsumary", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getLineItemSummary(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,
			@RequestParam(value="reportId") Integer reportId,
			@RequestParam(value="pageNo") Integer pageNo,
			@RequestParam(value="pageSize") Integer pageSize,
			@RequestParam(value="lineItemIdSearch") String lineItemIdSearch,
			@RequestParam(value="lineItemDescSearch") String lineItemDescSearch,
			@RequestParam(value="solutionName") String solutionName,
			@RequestParam(value="solutionId") String solutionId,
			@RequestParam(value="otp") String otp,
			@RequestParam(value="clientCode") String clientCode,
			HttpServletRequest request
			) {
		logger.info("EXEFLOW->RestAPIController->getLineItemSummary");
		JSONObject progressDetails = new JSONObject();
		try
		{

		OTPAuthenticator.getInstance().assertToken(otp);

			progressDetails=dldBo.getDetailsForDataConsumersSummaryLineItemGrid(currrentBusinessDate, isFrequencyFilterApplied, frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, reportId, pageNo, pageSize, lineItemIdSearch, lineItemDescSearch,solutionName,Integer.parseInt(solutionId),clientCode);

			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error("getLineItemSummary failed",t);
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}

	@RequestMapping(value = "/API/datalineage", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getDataForLinage(@RequestParam(value="otp") String otp,@RequestParam(value="cbd") String cbd,
			@RequestParam(value="clientCode") String clientCode,HttpServletRequest request
			) {
		logger.info("EXEFLOW->RestAPIController->getDataForLinage");
		JSONObject dataLineage = new JSONObject();
		try
		{
			OTPAuthenticator.getInstance().assertToken(otp);

			dataLineage=dldBo.getDataForLineage(clientCode,cbd);
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage());
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}
	
	
	
	
	@RequestMapping(value = "/API/datalineagebusinessuser", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getLineageDataForBU(@RequestParam(value="otp") String otp,@RequestParam(value="cbd") String cbd,
			@RequestParam(value="clientCode") String clientCode,@RequestParam(value="solutionName") String solutionName,HttpServletRequest request
			) {
		logger.info("EXEFLOW->RestAPIController->getDataForLinage");
		JSONObject dataLineage = new JSONObject();
		try
		{
			OTPAuthenticator.getInstance().assertToken(otp);

			dataLineage=dldBo.getLineageDataForBU(clientCode,cbd,solutionName);
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage());
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}
	
	
	@RequestMapping(value = "/API/handleLinageDownload", method = RequestMethod.GET)
	public void handleLineageDownload(HttpServletRequest request, HttpServletResponse response,@RequestParam(value="otp") String otp,
			@RequestParam(value="clientCode") String clientCode) throws Throwable {
		logger.info("EXEFLOW - RestAPIController ->  handleLineageDownload()");
		String effectiveDate = request.getParameter("effectiveDate");
		response.reset();
		Workbook workbook =null;
		List<DldStatusDownload> listOfDldStatus=null;
		try{
			OTPAuthenticator.getInstance().assertToken(otp);
			
			listOfDldStatus=new ArrayList<DldStatusDownload>();
			workbook = UploaderHelper.getTemplateForLineage();
			
			listOfDldStatus=dldBo.getDownloadDetailsForSheetOne(effectiveDate, clientCode, "ALL");
			workbook=dldBo.getLineageWorkBoook(listOfDldStatus,workbook);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");   
		    response.setHeader("Content-Disposition", "download;filename=Lineage_Downloader.xlsx");
		    response.setHeader("Access-Control-Allow-Origin", "*");
	        response.setHeader("Access-Control-Allow-Methods", "POST, GET");
	        response.setHeader("Access-Control-Max-Age", "3600");
	        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.save(outputStream, FileFormatType.XLSX);
            outputStream.flush();
            
		}catch(Exception e){
			logger.error("Error occured while downloading", e);
		}
	}
	
	
	@RequestMapping(value = "/API/getDataForlinageForLineItem", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getDataForLinageForLineItems(@RequestParam(value="otp") String otp,@RequestParam(value="cbd") String cbd,
    		@RequestParam(value="reportId") String reportId,@RequestParam(value="solutionName") String solutionName,
			@RequestParam(value="clientCode") String clientCode,HttpServletRequest request
			) {
		logger.info("EXEFLOW->RestAPIController->getDataForLinage");
		JSONObject dataLineage = new JSONObject();
		try
		{
			OTPAuthenticator.getInstance().assertToken(otp);

			dataLineage=dldBo.getLineageForLineItems(cbd, clientCode,Integer.parseInt(reportId), 0, 0, solutionName);
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage());
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}
	
	
	

}
