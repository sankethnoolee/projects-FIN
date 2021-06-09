package com.fintellix.dld.bo;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Color;
import com.aspose.cells.Style;
import com.aspose.cells.StyleFlag;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.fintellix.dld.dao.DldDao;
import com.fintellix.dld.domain.ClientContent;
import com.fintellix.dld.domain.DLDEntity;
import com.fintellix.dld.domain.DLDTask;
import com.fintellix.dld.domain.DldEntityCollection;
import com.fintellix.dld.models.DataSource;
import com.fintellix.dld.models.DlEntity;
import com.fintellix.dld.models.DlEntityOwner;
import com.fintellix.dld.models.DlFlowType;
import com.fintellix.dld.models.DlTaskFlowType;
import com.fintellix.dld.models.DlTaskFrequency;
import com.fintellix.dld.models.DlTaskMaster;
import com.fintellix.dld.models.DlTaskRepository;
import com.fintellix.dld.models.DlTaskSourceTarget;
import com.fintellix.dld.models.DldSolution;
import com.fintellix.dld.models.DldStatusDownload;
import com.fintellix.dld.models.EntityMasterUploaderDTO;
import com.fintellix.dld.models.EntityOwnerUploaderDTO;
import com.fintellix.dld.models.ErrorLogForUploader;
import com.fintellix.dld.models.FlowTypesUploaderDTO;
import com.fintellix.dld.models.StagingDetails;
import com.fintellix.dld.models.TaskEntityDetailUploaderDTO;
import com.fintellix.dld.models.TaskExecutionLog;
import com.fintellix.dld.models.TaskFlowTypeDetail;
import com.fintellix.dld.models.TaskFrequencyDetail;
import com.fintellix.dld.models.TaskFrequencyExclusionOffset;
import com.fintellix.dld.models.TaskFrequencyOffset;
import com.fintellix.dld.models.TaskMasterUploaderDTO;
import com.fintellix.dld.models.TaskRepositoriesUploaderDTO;
import com.fintellix.dld.repository.DLDEntityRepository;
import com.fintellix.dld.util.LineItemInfo;
import com.fintellix.dld.util.ReportInfo;
import com.fintellix.dld.util.ReportStatusPayload;
import com.fintellix.dld.util.SolutionReportStatus;
import com.fintellix.dld.util.UploaderHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Component
public class DldBoImpl implements DldBo {

	static Logger LOGGER = LoggerFactory.getLogger(DldBoImpl.class);

	//variables.
	private static final String N = "N";
	private static final String Y = "Y";
	private static final String EMPTY_QUOTES = "";
	@Value("${dld.periodIdFormat}")
	private String PERIOD_ID_DATE_FORMAT;
	private static final String COMPLETED  = "COMPLETED";
	private static final String FAILED = "FAILED";
	private String DUMMY_END_DATE = "31-12-9999";
	private String SOURCE_SYSTEM = "SOURCESYSTEM";
	private String DATA_REPOSITORY = "DATAREPOSITORY";
	private String SOURCE_TASK_TAB = "SOURCETASK";
	private String SOURCE_SYSTEM_TAB = "SOURCESYSTEM";
	private String SOURCE_ENTITY_TAB="SOURCEENTITY";
	private String DATA_REPO_TASK = "DATAREPOTASK";
	private String DATA_REPO_ENTITY = "DATAREPOENTITY";
	private static final String SEPARATOR = "@##@";

	@Value("${dld.clientSheetName}")
	private String clientSheetName;

	@Value("${dld.entityOwnerSheetName}")
	private String entityOwnerSheetName;

	@Value("${dld.entityMasterSheetName}")
	private String entityMasterSheetName;

	@Value("${dld.flowTypesSheetName}")
	private String flowTypeSheetName;

	@Value("${dld.taskrepositorySheetName}")
	private String taskRepositorySheetName;

	@Value("${dld.taskMasterSheetName}")
	private String taskMasterSheetName;	

	@Value("${dld.taskEntityDetailsSheetName}")
	private String taskEntityDetailsSheetName;	

	@Value("${dld.colorCode}")
	private String strColor;

	@Value("${dld.dateFormatForDld}")
	private String dateFormat;

	
	@Value("${dld.statusDeactive}")
	private String deactive;
	



	@Autowired
	private DldDao dldDao;

	@SuppressWarnings("rawtypes")
	@Autowired 
	private DLDEntityRepository dldEntityRepository;
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getStatsOnChange (String currrentBusinessDate,String clientCode) throws Throwable{
		LOGGER.info("EXEFLOW-DldBoImpl -- > getStatsOnChange");
		Map<String, Object> statisticsForChoosenBusinessDate=new HashMap<String,Object>();
		Map<String, Object> statisticsForPreviousBusinessDate=new HashMap<String,Object>();
		Map<String, Object> statisticsForPreviousPendingBusinessDate=new HashMap<String,Object>();
		JSONObject finalJson=new JSONObject();
		statisticsForChoosenBusinessDate = getStatisticsForGivenDate(currrentBusinessDate,Y,clientCode);
		String prevBusinessDate=getPreviousBusinessDate(currrentBusinessDate,clientCode);
		String prevPendingDate=getPreviousPendingDate(currrentBusinessDate,clientCode);


		JSONObject statisticsForChoosenBusinessDateJson=new JSONObject();
		JSONObject statisticsForPrevBusinessDateJson=new JSONObject();
		JSONObject statisticsForPrevPendingDateJson=new JSONObject();
		statisticsForChoosenBusinessDateJson.put("curBusinessDate", currrentBusinessDate);


		if(prevBusinessDate!=null)
		{	
			statisticsForPreviousBusinessDate=getStatisticsForGivenDate(prevBusinessDate,N,clientCode);
			statisticsForPreviousBusinessDate.put("prevBusinessDate", prevBusinessDate);
		}
		else
			statisticsForPreviousBusinessDate.put("prevBusinessDate", "No Previous Business Date Found");

		if(prevPendingDate!=null){
			statisticsForPreviousPendingBusinessDate=getStatisticsForGivenDate(prevPendingDate,N,clientCode);
			statisticsForPreviousPendingBusinessDate.put("prevPendingDate", prevPendingDate);
		}
		else
			statisticsForPrevPendingDateJson.put("prevPendingDate", "No Previous Pending Business Date Found");


		for(Map.Entry<String, Object>entry:statisticsForChoosenBusinessDate.entrySet() )
		{
			statisticsForChoosenBusinessDateJson.put(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<String, Object>entry:statisticsForPreviousBusinessDate.entrySet() )
		{
			statisticsForPrevBusinessDateJson.put(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<String, Object>entry:statisticsForPreviousPendingBusinessDate.entrySet() )
		{
			statisticsForPrevPendingDateJson.put(entry.getKey(), entry.getValue());
		}
		finalJson.put("chosenBusDateStats", statisticsForChoosenBusinessDateJson);
		finalJson.put("prevBusDateStats", statisticsForPrevBusinessDateJson);
		finalJson.put("prevPenDateStats", statisticsForPrevPendingDateJson);
		return finalJson;

	}




	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getStatisticsForGivenDate(String businessDate,String isMaxBusinessDate,String clientCode)throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getcurrrentBusinessDateStatistics");
		try{

			DateTime dt = DateTime.parse(businessDate, DateTimeFormat.forPattern(dateFormat));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(dt,N,EMPTY_QUOTES,clientCode);
			List<TaskFrequencyDetail> totalTasks=dldDao.getTotalNumberOfTasks(freqApplicableForcbd,clientCode,dt);
			Integer failedTasks=null;
			HashMap<String, Object> statistics=new HashMap<String,Object>();
			Integer completedTasks=null;
			Integer totalNumberOfTasks=totalTasks.size();
			Integer plannedCompletionTasks=0;
			Integer notYetDue =0;
			Integer pendingDueToday = 0;
			Integer overDueTasks = 0;
			Integer overDuePosssibleCount = 0;
			DateTime sysDate=new DateTime();
			List<String> taskNames = new ArrayList<String>();
			List<String> onlyTaskNames = new ArrayList<String>();
			List<TaskExecutionLog> taskListForCbd=new ArrayList<TaskExecutionLog>();
			completedTasks=dldDao.getCompletedTasks(dt,clientCode,freqApplicableForcbd);
			failedTasks=dldDao.getFailedTasks(dt,clientCode,freqApplicableForcbd);
			List<String> overDueTaskNameCheck = new ArrayList<String>();
			Set<String> onlyTaskRelatedFreq = new HashSet<String>();
			for(TaskFrequencyDetail tsk:totalTasks){
				if(freqApplicableForcbd.contains(tsk.getFrequencyType())){
					onlyTaskRelatedFreq.add(tsk.getFrequencyType());
				}
				
				taskNames.add(tsk.getTaskName()+tsk.getTaskRepository());
				onlyTaskNames.add(tsk.getTaskName());
				if(dt.plusDays(tsk.getGraceDays()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
					//after
					notYetDue++;
				}
				else if(dt.plusDays(tsk.getGraceDays()).toLocalDate().compareTo(sysDate.toLocalDate())<0){	
					//before
					overDueTaskNameCheck.add(tsk.getTaskName()+tsk.getTaskRepository());
				}
			}

			//check for chosen cbd and send freq and flow type data.
			if(Y.equalsIgnoreCase(isMaxBusinessDate)){
				JSONArray applicableFreq = new JSONArray();
				JSONArray applicableFlow = new JSONArray();
				for(String frequency : onlyTaskRelatedFreq){
					applicableFreq.add(frequency);
				}
				Set<String> applicableFlowTypes=new HashSet<>();
				if(onlyTaskNames!=null && onlyTaskNames.size()>0){
					applicableFlowTypes = dldDao.getDistinctApplicableFlowTypesByTask(onlyTaskNames,clientCode);
				}
				
				for(String flowType : applicableFlowTypes){
					applicableFlow.add(flowType);
				}
				statistics.put("applicableFreqTypes", applicableFreq);
				statistics.put("applicableFlowTypes", applicableFlow);
			}

			if(overDueTaskNameCheck.size()>0){
				taskListForCbd=dldDao.getTasksByCbdandStatus(dt,"%%",N,EMPTY_QUOTES,clientCode,freqApplicableForcbd);
				for(TaskExecutionLog task : taskListForCbd){
					if(overDueTaskNameCheck.contains(task.getTaskName()+task.getTaskRepository())){
						overDuePosssibleCount++;	
					}
				}
			}
			overDueTasks = overDueTaskNameCheck.size() - overDuePosssibleCount;
			pendingDueToday = totalNumberOfTasks-notYetDue-completedTasks-failedTasks-overDueTasks;
			plannedCompletionTasks = completedTasks+failedTasks+overDueTasks+pendingDueToday;
			statistics.put("totalTasks", totalNumberOfTasks.toString());
			statistics.put("plannedCompletionTasks", plannedCompletionTasks.toString());
			statistics.put("notDueYet", notYetDue.toString());
			statistics.put("completedTasks", completedTasks.toString());
			statistics.put("failedTasks",failedTasks.toString());
			statistics.put("overDueTasks", overDueTasks+"");
			statistics.put("pendingDueTasks", pendingDueToday+"");
			return statistics;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(), e);
			throw new Throwable();
		}
	}


	@Override
	public String getMaxBusinessDate(String clientCode) throws Throwable {

		LOGGER.info("EXEFLOW-DldBoImpl -- > getMaxBusinessDate");
		return dldDao.getMaxBusinessDate(clientCode);
	}


	@Override
	public String getPreviousBusinessDate(String currrentBusinessDate,String clientCode)
			throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getPreviousBusinessDate");
		DateTime dt = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(dateFormat));
		return dldDao.getPreviousBusinessDate(dt,clientCode);

	}

	@Override
	public String getPreviousPendingDate(String currrentBusinessDate,String clientCode)
			throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getPreviousPendingDate");
		DateTime dt = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(dateFormat));
		return dldDao.getPreviousPendingDate(dt,clientCode);

	}


	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getLoadActualProgressDetails(String businessDate,
			String isFlowFilterApplied, String flowFilterCSV,
			String isFrequencyFilterApplied, String frequencyFilterCSV,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getcurrrentBusinessDateStatistics");
		Map<String,Integer> plannedDatesAndTaskCount = new HashMap<String, Integer>();
		Map<String,Integer> completedDatesAndTaskCount = new HashMap<String, Integer>();
		JSONArray progressDetails = new JSONArray();
		Set<String> freqApplied = new HashSet<String>();
		try{
			//getLineageForLineItems(businessDate, clientCode, 1001, 0, 0, "RBS");
			//getting all tasks matching frequency for the business date
			DateTime businessDateRec = DateTime.parse(businessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			List<TaskFrequencyDetail> totalTasks = new ArrayList<TaskFrequencyDetail>();
			List<TaskFrequencyDetail> totalTasksAfterFreqFilter = new ArrayList<TaskFrequencyDetail>();
			//getting total tasks.
			totalTasksAfterFreqFilter=getListOfTaskMaster(businessDate,EMPTY_QUOTES, EMPTY_QUOTES,
					N, N,clientCode);
			Integer totalTaskCount = totalTasksAfterFreqFilter.size();
			totalTasks=getListOfTaskMaster(businessDate,frequencyFilterCSV, flowFilterCSV,
					isFrequencyFilterApplied, isFlowFilterApplied,clientCode);
			Set<String> distinctDates = new HashSet<String>();
			List<String> sortedDates = new LinkedList<String>();
			JSONObject dateObject = new JSONObject();
			//preparing planned map .
			String plannedDateString = "";
			Integer offset = 1;
			for(TaskFrequencyDetail matchedTask : totalTasks){
				//check for default grace days.
				freqApplied.add(matchedTask.getFrequencyType());
				if(matchedTask.getGraceDays()==null){
					offset = 1;
				}else{
					offset = matchedTask.getGraceDays();
				}
				plannedDateString = businessDateRec.plusDays(offset).toLocalDate().toString();
				distinctDates.add(plannedDateString);
				if(plannedDatesAndTaskCount.get(plannedDateString)!=null){
					//increment count if it already have the date else make a new entry in planned.
					plannedDatesAndTaskCount.put(plannedDateString,
							plannedDatesAndTaskCount.get(plannedDateString)+1);
				}else{
					plannedDatesAndTaskCount.put(plannedDateString,1);
				}
			}
			
			//preparing completedTaskList
			String executedDate = EMPTY_QUOTES;
			List<TaskExecutionLog> completedTaskList=dldDao.getTasksByCbdandStatus(businessDateRec,COMPLETED,isFlowFilterApplied,flowFilterCSV,clientCode,freqApplied);
			for(TaskExecutionLog loggedTasks : completedTaskList){
				executedDate = new DateTime(loggedTasks.getRunPeriodDate()).toLocalDate().toString();
				distinctDates.add(executedDate);
				if(completedDatesAndTaskCount.get(executedDate)!=null){
					//increment count if it already have the date else make a new entry in planned.
					completedDatesAndTaskCount.put(executedDate,
							completedDatesAndTaskCount.get(executedDate)+1);
				}else{
					completedDatesAndTaskCount.put(executedDate,1);
				}
			}
			//preparing json array
			if(distinctDates.size()>0){
				sortedDates.addAll(distinctDates);
				Collections.sort(sortedDates);
				for (String distinctDate : sortedDates) {
					dateObject = new JSONObject();
					dateObject.put("progressDate", distinctDate);
					dateObject.put("totalTaskCount", totalTaskCount);

					//planned count
					if(plannedDatesAndTaskCount.get(distinctDate)==null){
						dateObject.put("plannedCount", "");
					}else{
						dateObject.put("plannedCount", plannedDatesAndTaskCount.get(distinctDate));
					}

					//completed count
					if(completedDatesAndTaskCount.get(distinctDate)==null){
						dateObject.put("completedCount", "");
					}else{
						dateObject.put("completedCount", completedDatesAndTaskCount.get(distinctDate));
					}
					progressDetails.add(dateObject);

				}
			}else{
				dateObject = new JSONObject();
				dateObject.put("status", " -NA-");
			}

		}catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}

		return progressDetails;
	}


	@Override
	public void insertTaskStatics(List<TaskExecutionLog> taskExecutionLog) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > insertTaskStatics");

		dldDao.insertTaskStatics(taskExecutionLog);

	}


	@Override
	public void putUploadedFileToCache(String userTokenSession, Map<String, Object> uploadDTOMap) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > putUploadedFileToCache");

		dldDao.putUploadedFileToCache(userTokenSession,uploadDTOMap);

	}

	@Override
	public Map<String, Object> getUploadedFileFromCache(String userTokenSession) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getUploadedFileFromCache");

		return dldDao.getUploadedFileFromCache(userTokenSession);

	}



	@Override
	public List<ErrorLogForUploader> validationForEntityOwner(List<EntityOwnerUploaderDTO> entityownerUploadList,
			String effectiveDate,String clientCode) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > validationForEntityOwner");

		ErrorLogForUploader errorLogObj ;
		List<ErrorLogForUploader> errorLogList = new ArrayList<ErrorLogForUploader>();
		List<String> solutionNameList=new ArrayList<String>();
		List<String> dsList=new ArrayList<String>();
		List<DldSolution> solutionList=new ArrayList<DldSolution>();
		List<DataSource> dataSourceList=new ArrayList<DataSource>();
		List<Integer> displayOderList=new ArrayList<Integer>();

		try{
			solutionList=dldDao.getAllSoutions(clientCode);
			dataSourceList=dldDao.getAllDataSource(clientCode);

			if(null!=solutionList && solutionList.size()>0){
				for(DldSolution dldSolution:solutionList)
				{
					solutionNameList.add(dldSolution.getSolutionName());

				}	
			}

			if(null!=dataSourceList && dataSourceList.size()>0){
				for(DataSource dataSource:dataSourceList)
				{
					dsList.add(dataSource.getDataSourceName());

				}	
			}



			if(entityownerUploadList!=null && entityownerUploadList.size()>0) {
				Boolean isNullCheckFailed;
				List<Integer> displayOrderNList=new ArrayList<Integer>();
				for(EntityOwnerUploaderDTO entityownerDTO :entityownerUploadList) {
					LOGGER.info("EXEFLOW - Validating Metadata for EntityOwner:"+entityownerDTO.getOwner_Name());
					isNullCheckFailed=false;
					if(entityownerDTO.getOwner_Name()==null){
						isNullCheckFailed=true;
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("EntityOwner");
						errorLogObj.setEntityName("");
						errorLogObj.setErrorMsg("Entity Owner Name is Blank");
						errorLogList.add(errorLogObj);

					}

					if(entityownerDTO.getExternal_Source()==null){
						isNullCheckFailed=true;
						if(entityownerDTO.getOwner_Name()!=null){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("External Source is blank.");
							errorLogList.add(errorLogObj);
						}
					}

					if(entityownerDTO.getDisplay_Sorting_Order()==null){
						isNullCheckFailed=true;
						if(entityownerDTO.getOwner_Name()!=null){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("Display Sorting Order is blank.");
							errorLogList.add(errorLogObj);
						}
					}

					if(entityownerDTO.getExternal_Source().equals("Y") && entityownerDTO.getData_source_Name()==null){
						isNullCheckFailed=true;
						if(entityownerDTO.getOwner_Name()!=null){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("Data Source Name is blank");
							errorLogList.add(errorLogObj);
						}
					}

					if(!isNullCheckFailed){

						if(entityownerDTO.getOwner_Name().length()>100){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("Length of Entity Owner cannot exceed more than 100 characters");
							errorLogList.add(errorLogObj);

						}
						if(entityownerDTO.getDescription().length()>256){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("Length of Description cannot exceed more than 256 characters");
							errorLogList.add(errorLogObj);

						}
						if(entityownerDTO.getExternal_Source().length()>1){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("Length of External Source Indicator cannot exceed more than 1 character ");
							errorLogList.add(errorLogObj);					

						}
						if(entityownerDTO.getContact_Details()!=null){
							if(entityownerDTO.getContact_Details().length()>2000){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("EntityOwner");
								errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
								errorLogObj.setErrorMsg("Length of Contact Details cannot exceed more than 2000 characters");
								errorLogList.add(errorLogObj);
							}
						}



						//duplicate values for OwnerName
						List<EntityOwnerUploaderDTO> clientListForDuplicateEntityOwner = entityownerUploadList.stream()               
								.filter(line -> entityownerDTO.getOwner_Name().equals(line.getOwner_Name()))     
								.collect(Collectors.toList());

						//duplicate values for DataSourceName
						if(entityownerDTO.getData_source_Name()!=null){
							List<EntityOwnerUploaderDTO> clientListForDuplicateDataSourceName = entityownerUploadList.stream()               
									.filter(line -> entityownerDTO.getData_source_Name().equals(line.getData_source_Name()))    
									.collect(Collectors.toList());


							if(clientListForDuplicateDataSourceName.size()>1){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("EntityOwner");
								errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
								errorLogObj.setErrorMsg("Duplicate entries found for Data Source Name.");
								errorLogList.add(errorLogObj);
							}


						}
						//duplicate values for SolutionName
						if(entityownerDTO.getSolution_Name()!=null){
							List<EntityOwnerUploaderDTO> clientListForDuplicateSolutionName = entityownerUploadList.stream()               
									.filter(line -> entityownerDTO.getSolution_Name().equals(line.getSolution_Name()))    
									.collect(Collectors.toList());

							if(clientListForDuplicateSolutionName.size()>1){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("EntityOwner");
								errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
								errorLogObj.setErrorMsg("Duplicate entries found for Solution Name.");
								errorLogList.add(errorLogObj);
							}
						}

						if(clientListForDuplicateEntityOwner.size()>1){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
							errorLogObj.setErrorMsg("Duplicate entries found for Owner Name.");
							errorLogList.add(errorLogObj);
						}


						if(entityownerDTO.getSolution_Name()!=null)
						{
							if(!solutionNameList.contains(entityownerDTO.getSolution_Name())){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("EntityOwner");
								errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
								errorLogObj.setErrorMsg("Solution Name Not found in DB.");
								errorLogList.add(errorLogObj);
							}
						}

						if(entityownerDTO.getData_source_Name()!=null){
							if(!dsList.contains(entityownerDTO.getData_source_Name())){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("EntityOwner");
								errorLogObj.setEntityName(entityownerDTO.getOwner_Name());
								errorLogObj.setErrorMsg("Data SourceName Not found in DB.");
								errorLogList.add(errorLogObj);

							}


						}

						if(entityownerDTO.getExternal_Source().equalsIgnoreCase("N")){
							displayOrderNList.add(Integer.parseInt(entityownerDTO.getDisplay_Sorting_Order()));
						}

					}

				}


				if(displayOrderNList!=null && displayOrderNList.size()>0)
				{	
					Collections.sort(displayOrderNList);
					Integer j=displayOrderNList.get(0);
					Integer Nlistsum=0;
					Integer countersum=0;
					for(Integer dispOrder:displayOrderNList){
						if(displayOderList.contains(dispOrder)){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("EntityOwner");
							errorLogObj.setEntityName("");
							errorLogObj.setErrorMsg("Display Sorting Order of External Data Source N  must be unique among rows");
							errorLogList.add(errorLogObj);

						}
						displayOderList.add(dispOrder);
						Nlistsum=dispOrder+Nlistsum;
						countersum=countersum+j;
						j=j+1;
					}
					if(!countersum.equals(Nlistsum)){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("EntityOwner");
						errorLogObj.setEntityName("");
						errorLogObj.setErrorMsg("Display Sorting Order of External Data Source N is not consistent");
						errorLogList.add(errorLogObj);	
					}

				}
			}

		}

		catch (Exception e) {

			LOGGER.info("Error Occured while validating Metadata for EntityOwner:");
		}

		List<ErrorLogForUploader> errorLogListForDistinctValues;
		List<ErrorLogForUploader> errorLogFinal = new ArrayList<ErrorLogForUploader>();
		for(ErrorLogForUploader errorLogForUploader: errorLogList)
		{

			errorLogListForDistinctValues  = errorLogList.stream()               
					.filter(line -> errorLogForUploader.getEntityName().equals(line.getEntityName()) && errorLogForUploader.getErrorMsg().equals(line.getErrorMsg()) )    
					.collect(Collectors.toList());
			if(!errorLogFinal.contains(errorLogListForDistinctValues.get(0)))
				errorLogFinal.add(errorLogListForDistinctValues.get(0));
		}
		return errorLogFinal;
	}




	@Override
	public List<ErrorLogForUploader> validationForEntityMaster(List<EntityMasterUploaderDTO> enitityMasterUploaderlist,List<EntityOwnerUploaderDTO> entityownerUploadList,String effectiveDate, String clientCode) throws Throwable{
		LOGGER.info("EXEFLOW-DldBoImpl -- > validationForEntityMaster");

		ErrorLogForUploader errorLogObj ;
		List<ErrorLogForUploader> errorLogList = new ArrayList<ErrorLogForUploader>();
		List<String> ownerList=new ArrayList<String>();
		List<String> entityTypeList=new ArrayList<String>();
		for(EntityOwnerUploaderDTO entityownerDTO:entityownerUploadList){
			ownerList.add(entityownerDTO.getOwner_Name());

		}
		List<String> entitytype=getAllentityType(clientCode);
		if(entitytype!=null && entitytype.size()>0){
			for(String ent:entitytype){
				entityTypeList.add(ent.toLowerCase());
			}
		}
		
		if(enitityMasterUploaderlist!=null && enitityMasterUploaderlist.size()>0) {
			Boolean isNullCheckFailed;
			for(EntityMasterUploaderDTO entityMasterDTO :enitityMasterUploaderlist) {
				LOGGER.info("EXEFLOW - Validating Metadata for EntityMaster:"+entityMasterDTO.getEntity_Name());
				isNullCheckFailed=false;
				if(entityMasterDTO.getEntity_Name()==null){
					isNullCheckFailed=true;
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Entity Master");
					errorLogObj.setEntityName("");
					errorLogObj.setErrorMsg("Entity Owner Name is Blank");
					errorLogList.add(errorLogObj);

				}

				if(entityMasterDTO.getOwner_Name()==null){
					isNullCheckFailed=true;
					if(entityMasterDTO.getEntity_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Owner Name is blank.");
						errorLogList.add(errorLogObj);
					}
				}

				if(entityMasterDTO.getEntity_Type()==null){
					isNullCheckFailed=true;
					if(entityMasterDTO.getEntity_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Entity Type is blank.");
						errorLogList.add(errorLogObj);
					}
				}

				if(entityMasterDTO.getEntity_Detail()==null){
					isNullCheckFailed=true;
					if(entityMasterDTO.getEntity_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Entity Detail (Technical Name Reference) is blank.");
						errorLogList.add(errorLogObj);
					}
				}
				if(entityMasterDTO.getDescription()==null){
					isNullCheckFailed=true;
					if(entityMasterDTO.getEntity_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Entity Description is blank.");
						errorLogList.add(errorLogObj);
					}}

				if(!isNullCheckFailed){

					if(entityMasterDTO.getOwner_Name().length()>100){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Length of Owner Name cannot exceed more than 100 characters");
						errorLogList.add(errorLogObj);

					}
					if(entityMasterDTO.getEntity_Name().length()>1000){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Length of Entity Name cannot exceed more than 1000 characters");
						errorLogList.add(errorLogObj);						

					}
					if(entityMasterDTO.getEntity_Type().length()>100){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Length of Entity Type cannot exceed more than 100 characters");
						errorLogList.add(errorLogObj);	

					}
					if(entityMasterDTO.getEntity_Detail().length()>256){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Length of Entity Detail cannot exceed more than 256 characters");
						errorLogList.add(errorLogObj);	

					}
					if(entityMasterDTO.getDescription().length()>256){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Length of Description cannot exceed more than 256 characters");
						errorLogList.add(errorLogObj);	
					}


					//duplicate values for ClientName
					List<EntityMasterUploaderDTO> listForDuplicateEntityMaster = enitityMasterUploaderlist.stream().filter(line -> entityMasterDTO.equals(line))     
							.collect(Collectors.toList());
					if(listForDuplicateEntityMaster.size()>1){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Duplicate entries found for combination(Entity Name("+entityMasterDTO.getEntity_Name()+")"+", Owner Name("+entityMasterDTO.getOwner_Name()+"), Entity Type("+entityMasterDTO.getEntity_Type()+"))");
						errorLogList.add(errorLogObj);
					}


					if(entityMasterDTO.getEntity_Name().contains(",")){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg(", Found in Entity Name(Only one entity is allowed in one row)");
						errorLogList.add(errorLogObj);

					}



					if(entityMasterDTO.getEntity_Type().equalsIgnoreCase("table")){
						if(entityMasterDTO.getEntity_Detail().contains(" ")){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Entity Master");
							errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
							errorLogObj.setErrorMsg("Space Found in Entity Detail (Technical Name Reference) for Entitytype Table");
							errorLogList.add(errorLogObj);
						}
					}

					//Owner name Validation
					if(!ownerList.contains(entityMasterDTO.getOwner_Name())){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Entity Master");
						errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
						errorLogObj.setErrorMsg("Entity Owner Name "+entityMasterDTO.getOwner_Name()+" Not Found in Entity Owner Sheet");
						errorLogList.add(errorLogObj);

					}
					
					if(entityTypeList!=null && entityTypeList.size()>0){
						if(!entityTypeList.contains(entityMasterDTO.getEntity_Type().toLowerCase())){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Entity Master");
							errorLogObj.setEntityName(entityMasterDTO.getEntity_Name());
							errorLogObj.setErrorMsg("Entity Type("+entityMasterDTO.getEntity_Type()+") Not Found in Entity Type table");
							errorLogList.add(errorLogObj);
	
							
						}
					}
					

				}

			}


		}
		List<ErrorLogForUploader> errorLogListForDistinctValues;
		List<ErrorLogForUploader> errorLogFinal = new ArrayList<ErrorLogForUploader>();
		for(ErrorLogForUploader errorLogForUploader: errorLogList)
		{

			errorLogListForDistinctValues  = errorLogList.stream()               
					.filter(line -> errorLogForUploader.getEntityName().equals(line.getEntityName()) && errorLogForUploader.getErrorMsg().equals(line.getErrorMsg()) )    
					.collect(Collectors.toList());
			if(!errorLogFinal.contains(errorLogListForDistinctValues.get(0)))
				errorLogFinal.add(errorLogListForDistinctValues.get(0));
		}
		return errorLogFinal;

	}




	@Override
	public List<ErrorLogForUploader> validationForFlowTypes(List<FlowTypesUploaderDTO> flowTypesList, String effectiveDate) throws Throwable{
		LOGGER.info("EXEFLOW-DldBoImpl -- > validationForFlowTypes");

		Boolean isNullCheckFailed;
		ErrorLogForUploader errorLogObj ;
		List<ErrorLogForUploader> errorLogList = new ArrayList<ErrorLogForUploader>();
		for(FlowTypesUploaderDTO flowTypesDTO :flowTypesList) {
			LOGGER.info("EXEFLOW - Validating Metadata for FlowTypes:"+flowTypesDTO.getFlow_type());
			isNullCheckFailed=false;
			if(flowTypesDTO.getFlow_type()==null){
				isNullCheckFailed=true;
				errorLogObj = new ErrorLogForUploader();
				errorLogObj.setEntityType("Flow Types");
				errorLogObj.setEntityName("");
				errorLogObj.setErrorMsg("Flow Types is Blank");
				errorLogList.add(errorLogObj);

			}
			if(flowTypesDTO.getDescription()==null){
				isNullCheckFailed=true;
				if(flowTypesDTO.getFlow_type()!=null){
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Flow Types");
					errorLogObj.setEntityName(flowTypesDTO.getFlow_type());
					errorLogObj.setErrorMsg("Flow Description is Blank");
					errorLogList.add(errorLogObj);
				}
			}
			if(!isNullCheckFailed){

				if(flowTypesDTO.getFlow_type().length()>100){

					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Flow Types");
					errorLogObj.setEntityName(flowTypesDTO.getFlow_type());
					errorLogObj.setErrorMsg("Length of Flow Type cannot exceed more than 100 characters");
					errorLogList.add(errorLogObj);
				}

				if(flowTypesDTO.getDescription().length()>256){

					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Flow Types");
					errorLogObj.setEntityName(flowTypesDTO.getFlow_type());
					errorLogObj.setErrorMsg("Length of Description cannot exceed more than 256 characters");
					errorLogList.add(errorLogObj);
				}



				//duplicate values for ClientName
				List<FlowTypesUploaderDTO> listForDuplicateTaskRepositories = flowTypesList.stream().filter(line -> flowTypesDTO.getFlow_type().equals(line.getFlow_type()))     
						.collect(Collectors.toList());
				if(listForDuplicateTaskRepositories.size()>1){
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Flow Types");
					errorLogObj.setEntityName(flowTypesDTO.getFlow_type());
					errorLogObj.setErrorMsg("Duplicate entries found for Flow Type");
					errorLogList.add(errorLogObj);
				}

			}
		}
		List<ErrorLogForUploader> errorLogListForDistinctValues;
		List<ErrorLogForUploader> errorLogFinal = new ArrayList<ErrorLogForUploader>();
		for(ErrorLogForUploader errorLogForUploader: errorLogList)
		{

			errorLogListForDistinctValues  = errorLogList.stream()               
					.filter(line -> errorLogForUploader.getEntityName().equals(line.getEntityName()) && errorLogForUploader.getErrorMsg().equals(line.getErrorMsg()) )    
					.collect(Collectors.toList());
			if(!errorLogFinal.contains(errorLogListForDistinctValues.get(0)))
				errorLogFinal.add(errorLogListForDistinctValues.get(0));
		}
		return errorLogFinal;
	}




	@Override
	public List<ErrorLogForUploader> validationForTaskRepositories(List<TaskRepositoriesUploaderDTO> taskRepositoriesList,String effectiveDate) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > validationForTaskRepositories");

		Boolean isNullCheckFailed;
		ErrorLogForUploader errorLogObj;
		List<ErrorLogForUploader> errorLogList = new ArrayList<ErrorLogForUploader>();
		for(TaskRepositoriesUploaderDTO taskRepositoriesDTO :taskRepositoriesList) {
			LOGGER.info("EXEFLOW - Validating Metadata for TaskRepositories:"+taskRepositoriesDTO.getName());
			isNullCheckFailed=false;
			if(taskRepositoriesDTO.getName()==null){
				isNullCheckFailed=true;
				errorLogObj = new ErrorLogForUploader();
				errorLogObj.setEntityType("Task Repositories");
				errorLogObj.setEntityName("");
				errorLogObj.setErrorMsg("Task Repository Name is Blank");
				errorLogList.add(errorLogObj);

			}
			if(taskRepositoriesDTO.getDescription()==null){
				isNullCheckFailed=true;
				if(taskRepositoriesDTO.getName()!=null){
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Task Repositories");
					errorLogObj.setEntityName(taskRepositoriesDTO.getName());
					errorLogObj.setErrorMsg("Task Description is Blank");
					errorLogList.add(errorLogObj);
				}

			}

			if(!isNullCheckFailed){

				if(taskRepositoriesDTO.getName().length()>100){
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Task Repositories");
					errorLogObj.setEntityName(taskRepositoriesDTO.getName());
					errorLogObj.setErrorMsg("Length of Repository Name cannot exceed more than 100 characters");
					errorLogList.add(errorLogObj);
				}



				if(taskRepositoriesDTO.getDescription().length()>256){

					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Task Repositories");
					errorLogObj.setEntityName(taskRepositoriesDTO.getName());
					errorLogObj.setErrorMsg("Length of Description cannot exceed more than 256 characters");
					errorLogList.add(errorLogObj);
				}




				//duplicate values for ClientName
				List<TaskRepositoriesUploaderDTO> listForDuplicateTaskRepositories = taskRepositoriesList.stream().filter(line -> taskRepositoriesDTO.getName().equals(line.getName()))     
						.collect(Collectors.toList());
				if(listForDuplicateTaskRepositories.size()>1){
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Client");
					errorLogObj.setEntityName(taskRepositoriesDTO.getName());
					errorLogObj.setErrorMsg("Duplicate entries found for Task Repository Name");
					errorLogList.add(errorLogObj);
				}

			}
		}
		List<ErrorLogForUploader> errorLogListForDistinctValues;
		List<ErrorLogForUploader> errorLogFinal = new ArrayList<ErrorLogForUploader>();
		for(ErrorLogForUploader errorLogForUploader: errorLogList)
		{

			errorLogListForDistinctValues  = errorLogList.stream()               
					.filter(line -> errorLogForUploader.getEntityName().equals(line.getEntityName()) && errorLogForUploader.getErrorMsg().equals(line.getErrorMsg()) )    
					.collect(Collectors.toList());
			if(!errorLogFinal.contains(errorLogListForDistinctValues.get(0)))
				errorLogFinal.add(errorLogListForDistinctValues.get(0));
		}
		return errorLogFinal;
	}




	@Override
	public List<ErrorLogForUploader> validationForTaskMaster(String clientCode,List<TaskMasterUploaderDTO> taskMasterList, List<FlowTypesUploaderDTO> flowTypesList, List<EntityMasterUploaderDTO> enitityMasterUploaderlist, List<EntityOwnerUploaderDTO> entityownerUploadList,List<TaskEntityDetailUploaderDTO> taskEntityDetailList,List<TaskRepositoriesUploaderDTO> taskRepositoriesList,String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > validationForTaskMaster");
		SimpleDateFormat formater= new SimpleDateFormat(dateFormat);
		Boolean isNullCheckFailed;
		ErrorLogForUploader errorLogObj ;
		List<ErrorLogForUploader> errorLogList = new ArrayList<ErrorLogForUploader>();
		Set<String> frequencySet=new HashSet<String>();
		List<String> ownerList=new ArrayList<String>();
		List<String> entityMasterList=new ArrayList<String>();
		List<String> flowTypeList=new ArrayList<String>();
		DlTaskMaster tMasterFromDb=null;
		Map<String,String> entityOwnerMap=null;
		List<Map<String,String>> entityOwnerMapList=new ArrayList<Map<String,String>>();
		List<String> freqOffsetList=new ArrayList<String>();
		List<String> freqList=new ArrayList<String>();
		List<TaskEntityDetailUploaderDTO> filteredTaskEntitydetailList=new ArrayList<TaskEntityDetailUploaderDTO>();
		List<TaskEntityDetailUploaderDTO> filteredSourceEntitydetailList=new ArrayList<TaskEntityDetailUploaderDTO>();
		List<TaskEntityDetailUploaderDTO> filteredTargetEntitydetailList=new ArrayList<TaskEntityDetailUploaderDTO>();
		Set<String> repoNameSet=new HashSet<String>();
		List<String> tskTypeList=new ArrayList<String>();
		try{
			
			List<String> taskTypeList=getAllTaskType(clientCode);
			if(taskTypeList!=null && taskTypeList.size()>0){
				for(String tsk:taskTypeList){
					tskTypeList.add(tsk.toLowerCase());
					
				}
			}
			
			
			for(EntityOwnerUploaderDTO entityownerDTO:entityownerUploadList){
				ownerList.add(entityownerDTO.getOwner_Name().toLowerCase());

			}
			
			for(TaskRepositoriesUploaderDTO taskRepositoriesUploaderDTO:taskRepositoriesList){
				repoNameSet.add(taskRepositoriesUploaderDTO.getName());
			}
			

			for(EntityMasterUploaderDTO entitymasterDTO:	enitityMasterUploaderlist){
				entityMasterList.add(entitymasterDTO.getEntity_Name().toLowerCase());
				entityOwnerMap=new HashMap<String,String>();
				entityOwnerMap.put(entitymasterDTO.getEntity_Name().toLowerCase(), entitymasterDTO.getOwner_Name().toLowerCase());
				entityOwnerMapList.add(entityOwnerMap);
			}

			for(FlowTypesUploaderDTO flowTypesDTO:	flowTypesList){
				flowTypeList.add(flowTypesDTO.getFlow_type());

			}
			frequencySet=dldDao.getAllFrequecy(clientCode);

			List<String> taskType=dldDao.getAllTaskType(clientCode);
			for(TaskMasterUploaderDTO taskMasterDTO :taskMasterList) {
				LOGGER.info("EXEFLOW - Validating Metadata for TaskMaster:"+taskMasterDTO.getTask_Name());
				isNullCheckFailed=false;
				if(taskMasterDTO.getTask_Name()==null){
					isNullCheckFailed=true;
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Task Master");
					errorLogObj.setEntityName("");
					errorLogObj.setErrorMsg("Task Name is Blank");
					errorLogList.add(errorLogObj);

				}
				if(taskMasterDTO.getTask_Type()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Type is Blank");
						errorLogList.add(errorLogObj);
					}
				}
				if(taskMasterDTO.getTask_Repository()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Repository is Blank");
						errorLogList.add(errorLogObj);
					}

				}
				if(taskMasterDTO.getDescription()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Description is Blank");
						errorLogList.add(errorLogObj);
					}

				}
				if(taskMasterDTO.getTask_Technical_Name()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Technical Name is Blank");
						errorLogList.add(errorLogObj);
					}

				}

				if(taskMasterDTO.getTask_Flows()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Flow is Blank");
						errorLogList.add(errorLogObj);
					}
				}
				if(taskMasterDTO.getTask_Frequency_Offset()==null || taskMasterDTO.getTask_Frequency_Offset().size()==0){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Frequency is Blank");
						errorLogList.add(errorLogObj);
					}
				}

				if(taskMasterDTO.getStatus()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Status is Blank");
						errorLogList.add(errorLogObj);
					}
				}

				if(taskMasterDTO.getIsValidationRequired()==null){
					isNullCheckFailed=true;
					if(taskMasterDTO.getTask_Name()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Is Validation Required is Blank");
						errorLogList.add(errorLogObj);
					}
				}


				if(!isNullCheckFailed){


					if(taskMasterDTO.getTask_Repository().length()>100){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Length of Task Repository cannot exceed more than 100 characters");
						errorLogList.add(errorLogObj);

					}
					if(taskMasterDTO.getTask_Type().length()>100){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Length of Task Type cannot exceed more than 100 characters");
						errorLogList.add(errorLogObj);

					}
					if(taskMasterDTO.getDescription().length()>256){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Length of Task Description cannot exceed more than 256 characters");
						errorLogList.add(errorLogObj);

					}
					if(taskMasterDTO.getTask_Technical_Name().length()>1000){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Length of Task Technical Name cannot exceed more than 1000 characters");
						errorLogList.add(errorLogObj);

					}
					if(taskMasterDTO.getSub_Task_Technical_Name()!=null){
						if(taskMasterDTO.getSub_Task_Technical_Name().length()>1000){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Length of Sub Task Technical Name cannot exceed more than 1000 characters");
							errorLogList.add(errorLogObj);

						}
					}
					if(taskMasterDTO.getIsValidationRequired().length()>1){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Length of Is Validation Required cannot exceed more than 1 characters");
						errorLogList.add(errorLogObj);

					}
					
					if(tskTypeList!=null && tskTypeList.size()>0){
						if(!tskTypeList.contains(taskMasterDTO.getTask_Type().toLowerCase())){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Task Type("+taskMasterDTO.getTask_Type()+") not found in Task Type Table");
							errorLogList.add(errorLogObj);
							
						}
					}

					for(String flowName:taskMasterDTO.getTask_Flows()){
						if(flowName.length()>100)
						{
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Length of each Task Flow Name cannot exceed more than 100 characters");
							errorLogList.add(errorLogObj);
						}
					}



					//duplicate values for Task
					List<TaskMasterUploaderDTO> listForDuplicateEntityMaster = taskMasterList.stream().filter(line -> taskMasterDTO.equals(line))     
							.collect(Collectors.toList());
					if(listForDuplicateEntityMaster.size()>1){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Duplicate entries found for combination(Task Name("+taskMasterDTO.getTask_Name()+"), Task Repositories("+taskMasterDTO.getTask_Repository()+") and Task Type("+taskMasterDTO.getTask_Type()+"))");
						errorLogList.add(errorLogObj);
					}

					//Task Flow Validation
					if(!flowTypeList.containsAll(taskMasterDTO.getTask_Flows())){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Flow Type("+taskMasterDTO.getTask_Flows()+") Not Found in Flow Types Sheet");
						errorLogList.add(errorLogObj);

					}



					freqList=new ArrayList<String>();
					List<TaskFrequencyOffset> freqDTOList=taskMasterDTO.getTask_Frequency_Offset();
					for(TaskFrequencyOffset taskFrequencyOffset: freqDTOList){
						freqList.add(taskFrequencyOffset.getFrequency().toLowerCase());
						if(taskFrequencyOffset.getFrequency().length()>100){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Length of each Task frequency Name in Task Frequency column cannot exceed more than 100 cahracters");
							errorLogList.add(errorLogObj);

						}
					}


					freqOffsetList=new ArrayList<String>();
					if(taskMasterDTO.getTask_Frequency_Exclusions_Offset()!=null){
						List<TaskFrequencyExclusionOffset> freqExDTOList=taskMasterDTO.getTask_Frequency_Exclusions_Offset();
						for(TaskFrequencyExclusionOffset taskFrequencyExclusionOffset: freqExDTOList){
							freqOffsetList.add(taskFrequencyExclusionOffset.getFrequency().toLowerCase());
							if(taskFrequencyExclusionOffset.getFrequency().length()>100){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("Task Master");
								errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
								errorLogObj.setErrorMsg("Length of each Task frequency Name in Task Frequency Exclusions column cannot exceed more than 100 cahracters");
								errorLogList.add(errorLogObj);
							}
						}
					}

					
					if(!repoNameSet.contains(taskMasterDTO.getTask_Repository())){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Task Repository("+taskMasterDTO.getTask_Repository()+") Not Found in Task Repository Sheet");
						errorLogList.add(errorLogObj);

					}
					
					
					
					if(!frequencySet.containsAll(freqList)){

						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Frequency Not Found for Task frequency(Offset)");
						errorLogList.add(errorLogObj);

					}


					if(taskMasterDTO.getTask_Frequency_Exclusions_Offset()!=null){
						if(!frequencySet.containsAll(freqOffsetList)){

							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Task Frequency Not Found for Task Frequency Exclusion(Offset)");
							errorLogList.add(errorLogObj);

						}
					}

					if(!(taskMasterDTO.getIsValidationRequired().equalsIgnoreCase("Y")) && !(taskMasterDTO.getIsValidationRequired().equalsIgnoreCase("N"))){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Is Validation Required Value can be only (Y/N)");
						errorLogList.add(errorLogObj);
					}


					if(taskMasterDTO.getStatus().equalsIgnoreCase(deactive)){
						tMasterFromDb=dldDao.getTaskMaster(clientCode, taskMasterDTO.getTask_Repository(), taskMasterDTO.getTask_Name(),formater.parse(effectiveDate));
						if(tMasterFromDb==null){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("There is no task to deactivate");
							errorLogList.add(errorLogObj);
						}
					}

					//Validating task type

					if(!taskType.stream().anyMatch(col->col.equalsIgnoreCase(taskMasterDTO.getTask_Type()))){
						if(tMasterFromDb==null){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg(" Task Type : "+taskMasterDTO.getTask_Type()+" is not in metadata table DL_TASK_TYPE");
							errorLogList.add(errorLogObj);
						}
					}
					
					
					//Validation for Source/Target entity 
					
					filteredTaskEntitydetailList  = taskEntityDetailList.stream()               
							.filter(line ->line.getTaskName().equals(taskMasterDTO.getTask_Name())
									&& line.getTaskRepository().equals(taskMasterDTO.getTask_Repository()))    
							.collect(Collectors.toList());
					
					
					if(filteredTaskEntitydetailList!=null && filteredTaskEntitydetailList.size()>0){
						
						filteredSourceEntitydetailList  = taskEntityDetailList.stream()               
								.filter(line ->line.getTaskName().equals(taskMasterDTO.getTask_Name())
										&& line.getTaskRepository().equals(taskMasterDTO.getTask_Repository())
										&& line.getLinkType().equals("S")).collect(Collectors.toList());
						
						if(filteredSourceEntitydetailList==null || filteredSourceEntitydetailList.size()==0){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Source Entity not found for Task in Task Entity Details Sheet");
							errorLogList.add(errorLogObj);
							
							
						}
						
						
						
						
						filteredTargetEntitydetailList  = taskEntityDetailList.stream()               
								.filter(line ->line.getTaskName().equals(taskMasterDTO.getTask_Name())
										&& line.getTaskRepository().equals(taskMasterDTO.getTask_Repository())
										&& line.getLinkType().equals("T")).collect(Collectors.toList());
						
						if(filteredTargetEntitydetailList==null || filteredTargetEntitydetailList.size()==0){
							errorLogObj = new ErrorLogForUploader();
							errorLogObj.setEntityType("Task Master");
							errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
							errorLogObj.setErrorMsg("Target Entity not found for Task in Task Entity Details Sheet");
							errorLogList.add(errorLogObj);
						}
						
						
						
					}
					else{
						
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Master");
						errorLogObj.setEntityName(taskMasterDTO.getTask_Name());
						errorLogObj.setErrorMsg("Source/Target details not found for Task in Task Entity Details Sheet");
						errorLogList.add(errorLogObj);
						
					}
					

				}
			}
			
			

		}
		catch (Exception e) {

			LOGGER.error("Error occured while validation of Task Master", e);
		}
		List<ErrorLogForUploader> errorLogListForDistinctValues;
		List<ErrorLogForUploader> errorLogFinal = new ArrayList<ErrorLogForUploader>();
		for(ErrorLogForUploader errorLogForUploader: errorLogList)
		{

			errorLogListForDistinctValues  = errorLogList.stream()               
					.filter(line -> errorLogForUploader.getEntityName().equals(line.getEntityName()) && errorLogForUploader.getErrorMsg().equals(line.getErrorMsg()) )    
					.collect(Collectors.toList());
			if(!errorLogFinal.contains(errorLogListForDistinctValues.get(0)))
				errorLogFinal.add(errorLogListForDistinctValues.get(0));
		}
		return errorLogFinal;
	}

	public void generateValidationReport(Workbook workbook,	List<ErrorLogForUploader> masterErrorMsgList) throws Exception {
		LOGGER.info("EXEFLOW-DldBoImpl -- > generateValidationReport");

		int sheetIndex = 0;
		Worksheet worksheet = null;
		Style style=null;
		Cell header=null;
		StyleFlag flag=null;
		Style styleForWrap=null;
		try {
			//styling for wrapping text in each worksheet
			styleForWrap= new Style();
			styleForWrap.setTextWrapped(true);
			flag = new StyleFlag();
			flag.setWrapText(true);
			workbook.getWorksheets().get(entityOwnerSheetName).getCells().applyStyle(styleForWrap, flag);
			workbook.getWorksheets().get(entityMasterSheetName).getCells().applyStyle(styleForWrap, flag);
			workbook.getWorksheets().get(flowTypeSheetName).getCells().applyStyle(styleForWrap, flag);
			workbook.getWorksheets().get(taskRepositorySheetName).getCells().applyStyle(styleForWrap, flag);
			workbook.getWorksheets().get(taskMasterSheetName).getCells().applyStyle(styleForWrap, flag);


			WorksheetCollection worksheets = workbook.getWorksheets();
			if (workbook.getWorksheets().get("Validation Report") == null) {
				sheetIndex = worksheets.add();
				worksheet = worksheets.get(sheetIndex);
				worksheet.setName("Validation Report");
				worksheet.getCells().setStandardWidth(38.5f);
				worksheet.getCells().setRowHeight(0, 20.5f);
				worksheet.getCells().applyStyle(styleForWrap, flag);
			}
			//setting style for validation report sheet
			Integer intColor = Integer.parseInt(strColor, 16);
			Color clr = Color.fromArgb(intColor);
			style = new Style();
			style.setPattern(BackgroundType.SOLID);
			style.setForegroundColor(clr);
			style.getFont().setSize(10);
			style.getFont().setBold(true);
			Cells cells = workbook.getWorksheets().get("Validation Report").getCells();
			cells.setColumnWidth(2, 72.5);
			header = cells.get("A1");
			header.putValue("Entity Name");
			header.setStyle(style);

			header = cells.get("B1");
			header.putValue("Entity Type");
			header.setStyle(style);

			header = cells.get("C1");
			header.putValue("Error Message");
			style.getFont().setColor(Color.getRed());
			header.setStyle(style);

			header = cells.get("D1");
			header.putValue("Warning Message");
			header.setStyle(style);

			ErrorLogForUploader errorLogForUploader;
			for (int i = 0; i < masterErrorMsgList.size(); i++) {
				errorLogForUploader = masterErrorMsgList.get(i);
				for (int j = 0; j < 4; j++) {
					if (j == 0) {
						Cell cell1 = cells.get(i + 1, j);
						cell1.setValue(errorLogForUploader.getEntityName());
					}
					if (j == 1) {
						Cell cell2 = cells.get(i + 1, j);
						cell2.setValue(errorLogForUploader.getEntityType());
					}
					if (j == 2) {
						Cell cell3 = cells.get(i + 1, j);
						if(errorLogForUploader.getErrorMsg()!=null && errorLogForUploader.getErrorMsg()!=""){
							
							cell3.setValue(errorLogForUploader.getErrorMsg());
							
						}
					}
					if (j == 3) {
						Cell cell4 = cells.get(i + 1, j);
						if(errorLogForUploader.getWarningMsg()!=null && errorLogForUploader.getWarningMsg()!="")
						{
							cell4.setValue(errorLogForUploader.getWarningMsg());
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error occured while generating Validation Report", e);
		}
	}









	@Override
	public List<TaskFrequencyDetail> getListOfTaskMaster(String cbd, String frequencyFilterCSV, String flowFilterCSV,
			String isFrequencyFilterApplied, String isFlowFilterApplied,String clientCode) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getListOfTaskMaster");
		try{

			DateTime businessDateRec = DateTime.parse(cbd, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			List<TaskFrequencyDetail> totalTasks = new ArrayList<TaskFrequencyDetail>();
			List<TaskFrequencyDetail> totalTasksAfterFreqFilter = new ArrayList<TaskFrequencyDetail>();
			//getting total tasks.
			freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
			totalTasksAfterFreqFilter=dldDao.getTotalNumberOfTasks(freqApplicableForcbd,clientCode,businessDateRec);
			//task filter based on flow type.
			if(Y.equalsIgnoreCase(isFlowFilterApplied)){
				totalTasks = new ArrayList<TaskFrequencyDetail>();
				List<TaskFlowTypeDetail> flowTypeInFilter = dldDao.getTaskListBasedOnFilter(flowFilterCSV,clientCode);
				//retaining common tasks 
				for(TaskFrequencyDetail tf :totalTasksAfterFreqFilter){
					for(TaskFlowTypeDetail tfl :flowTypeInFilter){
						if(tfl.getTaskName().equals(tf.getTaskName()) && (tf.getVersionNo().equals(tfl.getVersionNo()))){
							totalTasks.add(tf);
						}
					}
				}
			}else{
				totalTasks = totalTasksAfterFreqFilter;
			}
			return totalTasks;		
		}
		catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}




	@Override
	public void logAllTask(String params,String requestOrigin) throws Throwable{
		LOGGER.info("DldBoImpl  -- >logAllTask");
		dldDao.logAllTask(params,requestOrigin);
	}




	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getStagingDetailsForSourceSystems(String currrentBusinessDate, 
			String isFlowFilterApplied,	String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getStagingDetailsForSourceSystems");
		JSONObject finalResponse = new JSONObject();
		try{
			DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
			Map<String,Integer> statsMap = null;
			Map<String,HashMap<String,Integer>> sourceSystemDetails = new HashMap<String, HashMap<String,Integer>>();
			Map<String,HashMap<String,Integer>> sourceEntityDetails = new HashMap<String, HashMap<String,Integer>>();
			Map<String,Integer> sourceTaskDetails = new HashMap<String,Integer>();
			DateTime sysDate=new DateTime();
			Set<String> processedTasks = new HashSet<String>();
			//getting applicable frequencies and generating csv for in clause if filter is not applied.
			if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
				freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
				frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
			}

			//initializing source task details
			sourceTaskDetails.put("completed",0 );
			sourceTaskDetails.put("notDueYet",0 );
			sourceTaskDetails.put("failed",0 );
			sourceTaskDetails.put("overDue",0 );


			//get staging details.
			totalStagingObject = dldDao.getStagingDetails(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, Y,clientCode);


			//processing staging results
			for(StagingDetails tsk :totalStagingObject){

				//for source system details.
				//check for src system entry
				if(sourceSystemDetails.get(tsk.getOwnerName())==null){
					statsMap = new HashMap<String, Integer>();
					statsMap.put("completed",0 );
					statsMap.put("notDueYet",0 );
					statsMap.put("failed",0 );
					statsMap.put("overDue",0 );
					sourceSystemDetails.put(tsk.getOwnerName(), (HashMap<String, Integer>) statsMap);
				}
				if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
					//not yet started 
					sourceSystemDetails.get(tsk.getOwnerName()).put("notDueYet",
							(sourceSystemDetails.get(tsk.getOwnerName()).get("notDueYet")+1));
				}
				else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
					//check for over due failed success.
					if(null ==tsk.getTaskStatus()){
						//overDueTask
						sourceSystemDetails.get(tsk.getOwnerName()).put("overDue",
								(sourceSystemDetails.get(tsk.getOwnerName()).get("overDue")+1));
					}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//failed tasks.
						sourceSystemDetails.get(tsk.getOwnerName()).put("failed",
								(sourceSystemDetails.get(tsk.getOwnerName()).get("failed")+1));
					}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//completed tasks.
						sourceSystemDetails.get(tsk.getOwnerName()).put("completed",
								(sourceSystemDetails.get(tsk.getOwnerName()).get("completed")+1));
					}
				}


				//for source entity details.
				//check for src system entry
				String entityOwner = tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
				if(sourceEntityDetails.get(entityOwner)==null){
					statsMap = new HashMap<String, Integer>();
					statsMap.put("completed",0 );
					statsMap.put("notDueYet",0 );
					statsMap.put("failed",0 );
					statsMap.put("overDue",0 );
					sourceEntityDetails.put(entityOwner, (HashMap<String, Integer>) statsMap);
				}
				if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
					//not yet started 
					sourceEntityDetails.get(entityOwner).put("notDueYet",
							(sourceEntityDetails.get(entityOwner).get("notDueYet")+1));
				}
				else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
					//check for over due failed success.
					if(null ==tsk.getTaskStatus()){
						//overDueTask
						sourceEntityDetails.get(entityOwner).put("overDue",
								(sourceEntityDetails.get(entityOwner).get("overDue")+1));
					}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//failed tasks.
						sourceEntityDetails.get(entityOwner).put("failed",
								(sourceEntityDetails.get(entityOwner).get("failed")+1));
					}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//completed tasks.
						sourceEntityDetails.get(entityOwner).put("completed",
								(sourceEntityDetails.get(entityOwner).get("completed")+1));
					}
				}

				//initializing source task details
				if(!processedTasks.contains(tsk.getTaskName())){
					if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
						//not yet started 
						sourceTaskDetails.put("notDueYet",sourceTaskDetails.get("notDueYet")+1);
					}
					else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
						//check for over due failed success.
						if(null ==tsk.getTaskStatus()){
							//overDueTask
							sourceTaskDetails.put("overDue",sourceTaskDetails.get("overDue")+1);
						}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//failed tasks.
							sourceTaskDetails.put("failed",sourceTaskDetails.get("failed")+1);
						}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks.
							sourceTaskDetails.put("completed",sourceTaskDetails.get("completed")+1);
						}
					}
				}
				processedTasks.add(tsk.getTaskName());
			}
			//preparing json object for response.

			JSONObject sourceSystemDetailsJson = new JSONObject();
			JSONObject sourceEntityDetailsJson = new JSONObject();
			JSONObject sourceTaskDetailsJson = new JSONObject();
			Integer completedCount = 0;
			Integer notStartedCount = 0;
			Integer partiallyCount = 0;

			//json for sourceSystemDetails
			for (Map.Entry<String, HashMap<String,Integer>> entry : sourceSystemDetails.entrySet()) {
				HashMap<String,Integer> value =  entry.getValue();
				if(value.get("completed")>0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")==0){
					completedCount++;
				}else if(value.get("completed")==0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")>0){
					notStartedCount++;
				}else if(value.get("completed")>0&&value.get("failed")==0&&(value.get("overDue")>0||value.get("notDueYet")>0)){
					partiallyCount++;
				} else {
					notStartedCount++;
				}
			}
			sourceSystemDetailsJson.put("total", sourceSystemDetails.keySet().size());
			sourceSystemDetailsJson.put("completed", completedCount);
			sourceSystemDetailsJson.put("partiallyCompleted", partiallyCount);
			sourceSystemDetailsJson.put("notStarted", notStartedCount);

			finalResponse.put("sourceSystemDetails", sourceSystemDetailsJson);



			//json for sourceEntityDetails
			completedCount = 0;
			notStartedCount = 0;
			partiallyCount = 0;
			for (Map.Entry<String, HashMap<String,Integer>> entry : sourceEntityDetails.entrySet()) {
				HashMap<String,Integer> value =  entry.getValue();
				if(value.get("completed")>0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")==0){
					completedCount++;
				}else if(value.get("completed")==0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")>0){
					notStartedCount++;
				}else if(value.get("completed")>0&&value.get("failed")==0&&(value.get("overDue")>0||value.get("notDueYet")>0)){
					partiallyCount++;
				} else {
					notStartedCount++;
				}
			}
			sourceEntityDetailsJson.put("total", sourceEntityDetails.keySet().size());
			sourceEntityDetailsJson.put("completed", completedCount);
			sourceEntityDetailsJson.put("partiallyCompleted", partiallyCount);
			sourceEntityDetailsJson.put("notStarted", notStartedCount);

			finalResponse.put("sourceEntityDetails", sourceEntityDetailsJson);

			//json for tasks.
			sourceTaskDetailsJson.put("total",processedTasks.size() );
			sourceTaskDetailsJson.put("notStarted",sourceTaskDetails.get("notDueYet") );
			sourceTaskDetailsJson.put("completed",sourceTaskDetails.get("completed") );
			sourceTaskDetailsJson.put("overDue",sourceTaskDetails.get("overDue") );
			sourceTaskDetailsJson.put("failed",sourceTaskDetails.get("failed") );

			finalResponse.put("sourceTaskDetails", sourceTaskDetailsJson);

		}
		catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}
		return finalResponse;
	}




	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getStagingDetailsForDataRepository(
			String currrentBusinessDate, String isFlowFilterApplied,
			String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getStagingDetailsForSourceSystems");
		JSONObject finalResponse = new JSONObject();
		try{
			DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
			Map<String,Integer> statsMap = null;
			Map<String,HashMap<String,Integer>> dataRepoDetails = new HashMap<String, HashMap<String,Integer>>();
			Map<String,HashMap<String,Integer>> repoEntityDetails = new HashMap<String, HashMap<String,Integer>>();
			Map<String,Integer> repoTaskDetails = new HashMap<String,Integer>();
			DateTime sysDate=new DateTime();
			Set<String> distinctDataRepo = new HashSet<String>();
			Map<String,String> entityRepoNameMap = new HashMap<String,String>();
			Set<String> processedTasks = new HashSet<String>();
			Map<String,Set<String>> repoTaskMap = new HashMap<String,Set<String>>();
			//getting applicable frequencies and generating csv for in clause if filter is not applied.
			if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
				freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
				frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
			}

			//initializing source task details
			repoTaskDetails.put("completed",0 );
			repoTaskDetails.put("notDueYet",0 );
			repoTaskDetails.put("failed",0 );
			repoTaskDetails.put("overDue",0 );
			repoTaskDetails.put("pendingDueToday",0 );


			//get staging details.
			totalStagingObject = dldDao.getStagingDetails(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);


			//processing staging results
			for(StagingDetails tsk :totalStagingObject){
				//for data repo split fetching distinct repo.
				distinctDataRepo.add(tsk.getTaskRepository());


				//for source entity details.
				//check for src system entry
				String entityOwner = tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
				if(repoEntityDetails.get(entityOwner)==null){
					statsMap = new HashMap<String, Integer>();
					statsMap.put("completed",0 );
					statsMap.put("notDueYet",0 );
					statsMap.put("failed",0 );
					statsMap.put("overDue",0 );
					repoEntityDetails.put(entityOwner, (HashMap<String, Integer>) statsMap);
				}
				if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
					//not yet started 
					repoEntityDetails.get(entityOwner).put("notDueYet",
							(repoEntityDetails.get(entityOwner).get("notDueYet")+1));
				}
				else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
					//check for over due failed success.
					if(null ==tsk.getTaskStatus()){
						//overDueTask
						repoEntityDetails.get(entityOwner).put("overDue",
								(repoEntityDetails.get(entityOwner).get("overDue")+1));
					}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//failed tasks.
						repoEntityDetails.get(entityOwner).put("failed",
								(repoEntityDetails.get(entityOwner).get("failed")+1));
					}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//completed tasks.
						repoEntityDetails.get(entityOwner).put("completed",
								(repoEntityDetails.get(entityOwner).get("completed")+1));
					}
				}

				//for data repository details.
				//check for data Repository entry
				//processing only tasks here in loop.
				if(dataRepoDetails.get(tsk.getTaskRepository())==null){
					statsMap = new HashMap<String, Integer>();
					statsMap.put("entityCompleted",0 );
					statsMap.put("entityTotal",0 );
					statsMap.put("taskCompleted",0 );
					statsMap.put("taskTotal",0 );
					dataRepoDetails.put(tsk.getTaskRepository(), (HashMap<String, Integer>) statsMap);
				}
				if(null==repoTaskMap.get(tsk.getTaskRepository())){
					Set<String> taskNameSet = new HashSet<String>();
					repoTaskMap.put(tsk.getTaskRepository(),taskNameSet);
				}
				if(!repoTaskMap.get(tsk.getTaskRepository()).contains(tsk.getTaskName())){
					if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
						//completedTask 
						dataRepoDetails.get(tsk.getTaskRepository()).put("taskCompleted",
								(dataRepoDetails.get(tsk.getTaskRepository()).get("taskCompleted")+1));
					}
					dataRepoDetails.get(tsk.getTaskRepository()).put("taskTotal",
							(dataRepoDetails.get(tsk.getTaskRepository()).get("taskTotal")+1));

				}


				//initializing source task details
				if(!processedTasks.contains(tsk.getTaskName())){
					if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
						//not yet started 
						repoTaskDetails.put("notDueYet",repoTaskDetails.get("notDueYet")+1);
					}
					else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<0 ){	
						//check for over due failed success.
						if(null ==tsk.getTaskStatus()){
							//overDueTask
							repoTaskDetails.put("overDue",repoTaskDetails.get("overDue")+1);
						}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//failed tasks.
							repoTaskDetails.put("failed",repoTaskDetails.get("failed")+1);
						}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks.
							repoTaskDetails.put("completed",repoTaskDetails.get("completed")+1);
						}
					}else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())==0 ){
						if(null ==tsk.getTaskStatus()){
							//pending due today.
							repoTaskDetails.put("pendingDueToday",repoTaskDetails.get("pendingDueToday")+1);
						}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks on the current day.
							repoTaskDetails.put("completed",repoTaskDetails.get("completed")+1);
						}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks on the current day.
							repoTaskDetails.put("failed",repoTaskDetails.get("failed")+1);
						}
					}
				}
				entityRepoNameMap.put(tsk.getEntityName()+SEPARATOR+tsk.getOwnerName(), tsk.getTaskRepository());
				processedTasks.add(tsk.getTaskName());
				repoTaskMap.get(tsk.getTaskRepository()).add(tsk.getTaskName());
			}

			//processing entites for distinct repo.

			for(String repoName:distinctDataRepo){
				for (Map.Entry<String, HashMap<String,Integer>> entry : repoEntityDetails.entrySet()) {
					HashMap<String,Integer> value =  entry.getValue();
					if(repoName.equalsIgnoreCase(entityRepoNameMap.get(entry.getKey()))){
						if(value.get("completed")>0 && value.get("failed")==0 &&value.get("overDue")==0&&value.get("notDueYet")==0){
							dataRepoDetails.get(repoName).put("entityCompleted",
									dataRepoDetails.get(repoName).get("entityCompleted")+1);
						}
						dataRepoDetails.get(repoName).put("entityTotal",
								dataRepoDetails.get(repoName).get("entityTotal")+1);
					}
				}
			}

			//preparing json object for response.

			JSONArray dataRepoDetailsJson = new JSONArray();
			JSONObject repoEntityDetailsJson = new JSONObject();
			JSONObject repoTaskDetailsJson = new JSONObject();
			JSONObject indivisualRepoTaskDetails = new JSONObject();

			Integer completedCount = 0;
			Integer notStartedCount = 0;
			Integer partiallyCount = 0;

			//json for repoEntityDetails

			for (Map.Entry<String, HashMap<String,Integer>> entry : repoEntityDetails.entrySet()) {
				HashMap<String,Integer> value =  entry.getValue();
				if(value.get("completed")>0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")==0){
					completedCount++;
				}else if(value.get("completed")==0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")>0){
					notStartedCount++;
				}else if(value.get("completed")>0&&value.get("failed")==0&&(value.get("overDue")>0||value.get("notDueYet")>0)){
					partiallyCount++;
				} else {
					notStartedCount++;
				}
			}
			repoEntityDetailsJson.put("total", repoEntityDetails.keySet().size());
			repoEntityDetailsJson.put("completed", completedCount);
			repoEntityDetailsJson.put("partiallyCompleted", partiallyCount);
			repoEntityDetailsJson.put("notStarted", notStartedCount);

			finalResponse.put("dataRepoEntityDetails", repoEntityDetailsJson);

			//json for tasks.
			repoTaskDetailsJson.put("total",processedTasks.size() );
			repoTaskDetailsJson.put("notStarted",repoTaskDetails.get("notDueYet") );
			repoTaskDetailsJson.put("completed",repoTaskDetails.get("completed") );
			repoTaskDetailsJson.put("overDue",repoTaskDetails.get("overDue") );
			repoTaskDetailsJson.put("failed",repoTaskDetails.get("failed") );
			repoTaskDetailsJson.put("pendingDueToday",repoTaskDetails.get("pendingDueToday") );
			repoTaskDetailsJson.put("plannedCompletion",repoTaskDetails.get("pendingDueToday")+
					repoTaskDetails.get("failed")+repoTaskDetails.get("overDue")+
					repoTaskDetails.get("completed"));

			finalResponse.put("dataRepoTaskDetails", repoTaskDetailsJson);



			//json for individual repo Details
			for (Map.Entry<String, HashMap<String,Integer>> entry : dataRepoDetails.entrySet()) {
				String key = entry.getKey();
				HashMap<String,Integer> value =  entry.getValue();
				indivisualRepoTaskDetails = new JSONObject();
				indivisualRepoTaskDetails.put("repositoryName", key);
				indivisualRepoTaskDetails.put("entityCompleted", value.get("entityCompleted"));
				indivisualRepoTaskDetails.put("entityTotal", value.get("entityTotal"));
				indivisualRepoTaskDetails.put("taskCompleted", value.get("taskCompleted"));
				indivisualRepoTaskDetails.put("taskTotal", value.get("taskTotal"));
				dataRepoDetailsJson.add(indivisualRepoTaskDetails);
			}
			finalResponse.put("repoListDetails", dataRepoDetailsJson);

		}
		catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}
		return finalResponse;
	}



	@SuppressWarnings("unchecked")
	@Override
	public boolean saveDataForUploader(String clientCode,String effectiveDate, String userTokenSession) throws Throwable {

		LOGGER.info("EXEFLOW-DldBoImpl -- > saveDataForUploader");
		boolean response=false;
		Map<String,Object> uploaderMap = new HashMap<String, Object>();
		List<EntityOwnerUploaderDTO> entityOwnersList=null;
		List<EntityMasterUploaderDTO> entityMasterList=null;
		List<FlowTypesUploaderDTO> flowTypesList=null;
		List<TaskRepositoriesUploaderDTO> taskRepositoriesList=null;
		List<TaskMasterUploaderDTO> taskMasterList=null;
		List<Object> allEntityOwners=null;
		List<Object> allFlowTypes=null;
		List<Object> allTaskRepositories=null;
		List<Object> allEntityMaster=null;
		List<Object> finalTaskList=null;
		List<DlTaskMaster> tMasterList=new ArrayList<DlTaskMaster>();
		List<DlTaskFlowType> taskFlowTypeList=new ArrayList<DlTaskFlowType>();
		List<DlTaskFrequency> taskFrequencyList=new ArrayList<DlTaskFrequency>();
		List<DlTaskSourceTarget> taskSourceTargetList=new ArrayList<DlTaskSourceTarget>();

		List<DlEntityOwner> updateEntityOwnerList=new ArrayList<DlEntityOwner>();
		List<DlEntityOwner> insertEntityOwnerList=new ArrayList<DlEntityOwner>();


		List<DlEntity> updateEntityMasterList=new ArrayList<DlEntity>();
		List<DlEntity> insertEntityMasterList=new ArrayList<DlEntity>();

		List<DlFlowType> updateFlowTypeList=new ArrayList<DlFlowType>();
		List<DlFlowType> insertFlowTypeList=new ArrayList<DlFlowType>();


		List<DlTaskRepository> updateTaskRepList=new ArrayList<DlTaskRepository>();
		List<DlTaskRepository> insertTaskRepMasterList=new ArrayList<DlTaskRepository>();
		List<DlTaskMaster> deactivateTaskList=new ArrayList<DlTaskMaster>();
		List<DlTaskMaster> updateTaskList=new ArrayList<DlTaskMaster>();
		List<TaskEntityDetailUploaderDTO> taskEntityDetailList=new ArrayList<TaskEntityDetailUploaderDTO>();

		try{
			uploaderMap=dldDao.getUploadedFileFromCache(userTokenSession);
			entityOwnersList = (List<EntityOwnerUploaderDTO>) uploaderMap.get("entityOwnersList");
			entityMasterList = (List<EntityMasterUploaderDTO>) uploaderMap.get("entityMasterList");
			flowTypesList = (List<FlowTypesUploaderDTO>) uploaderMap.get("flowTypesList");
			taskRepositoriesList= (List<TaskRepositoriesUploaderDTO>) uploaderMap.get("taskRepositoriesList");
			taskMasterList= (List<TaskMasterUploaderDTO>) uploaderMap.get("taskMasterList");
			taskEntityDetailList=(List<TaskEntityDetailUploaderDTO>) uploaderMap.get("taskEntityDetailList");

			if(entityOwnersList!=null && entityOwnersList.size()>0){
				allEntityOwners = saveDataForEntityOwners(entityOwnersList, clientCode,effectiveDate);
				if(allEntityOwners!=null && allEntityOwners.size()>0){
					updateEntityOwnerList=(List<DlEntityOwner>) allEntityOwners.get(0);
					insertEntityOwnerList=(List<DlEntityOwner>) allEntityOwners.get(1);		
				}
			}

			if(entityMasterList!=null && entityMasterList.size()>0){
				allEntityMaster=saveDataForEntityMaster(entityMasterList, clientCode, effectiveDate);
				if(allEntityMaster!=null && allEntityMaster.size()>0){
					updateEntityMasterList=(List<DlEntity>) allEntityMaster.get(0);
					insertEntityMasterList=(List<DlEntity>) allEntityMaster.get(1);		
				}

			}


			if(flowTypesList!=null && flowTypesList.size()>0){
				allFlowTypes = saveDataForFlowTypes(flowTypesList, clientCode,effectiveDate);
				if(allFlowTypes!=null && allFlowTypes.size()>0){
					updateFlowTypeList=(List<DlFlowType>) allFlowTypes.get(0);
					insertFlowTypeList=(List<DlFlowType>) allFlowTypes.get(1);		
				}
			}



			if(taskRepositoriesList!=null && taskRepositoriesList.size()>0){
				allTaskRepositories=saveDataForTaskRepositories(taskRepositoriesList,clientCode,effectiveDate);
				if(allTaskRepositories!=null && allTaskRepositories.size()>0){
					updateTaskRepList=(List<DlTaskRepository>) allTaskRepositories.get(0);
					insertTaskRepMasterList=(List<DlTaskRepository>) allTaskRepositories.get(1);		
				}
			}


			if(taskMasterList!=null && taskMasterList.size()>0){
				finalTaskList=saveDataForTaskMaster(taskMasterList,taskEntityDetailList,clientCode,effectiveDate);
				if(finalTaskList!=null && finalTaskList.size()>0){
					tMasterList=(List<DlTaskMaster>) finalTaskList.get(0);
					taskFlowTypeList=(List<DlTaskFlowType>) finalTaskList.get(1);
					taskFrequencyList=(List<DlTaskFrequency>) finalTaskList.get(2);
					taskSourceTargetList=(List<DlTaskSourceTarget>) finalTaskList.get(3);
					updateTaskList=(List<DlTaskMaster>) finalTaskList.get(4);
					deactivateTaskList=(List<DlTaskMaster>) finalTaskList.get(5);



				}
			}

			List<DLDTask> taskList = createDataForGraphDB(entityMasterList, entityOwnersList, taskMasterList, clientCode,taskEntityDetailList);

			//TODO delete all from repositry
			dldEntityRepository.deleteExistingData(clientCode);

			dldEntityRepository.save(taskList);

			response=dldDao.saveUploaderData(updateEntityOwnerList,insertEntityOwnerList,updateEntityMasterList,insertEntityMasterList,
					updateFlowTypeList,insertFlowTypeList,updateTaskRepList,insertTaskRepMasterList,tMasterList,taskFlowTypeList,taskFrequencyList,taskSourceTargetList,
					updateTaskList,deactivateTaskList);


		}
		catch (Exception e){
			LOGGER.error("Error occured while saving Data for Uploader", e);
		}
		return response;
	}




	private List<Object> saveDataForTaskMaster(List<TaskMasterUploaderDTO> taskMasterList,List<TaskEntityDetailUploaderDTO> taskEntityDetailList, String clientCode, String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > saveDataForTaskMaster");

		SimpleDateFormat formater= new SimpleDateFormat(dateFormat);
		DlTaskMaster tMaster=null;
		DlTaskFlowType taskFlowType=null;
		DlTaskFrequency taskFrequency=null;
		DlTaskSourceTarget taskSourceTarget=null;
		List<String> flowList=new ArrayList<String>();
		List<DlTaskMaster> insertTaskMasterList=new ArrayList<DlTaskMaster>();
		List<DlTaskFlowType> taskFlowTypeList=new ArrayList<DlTaskFlowType>();
		List<DlTaskFrequency> taskFrequencyList=new ArrayList<DlTaskFrequency>();
		List<DlTaskSourceTarget> taskSourceTargetList=new ArrayList<DlTaskSourceTarget>();
		List<DlTaskMaster> deactivateTaskList=new ArrayList<DlTaskMaster>();
		List<DlTaskMaster> updateTaskList=new ArrayList<DlTaskMaster>();
		List<Object> finalTaskList=new ArrayList<Object>();
		Integer versionNo=null;
		DlTaskMaster tMasterFromDb=null;
		List<DlTaskMaster> taskForEditList=null;
		List<String> sourceEntityList=null;
		//TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO=null;
		List<TaskEntityDetailUploaderDTO> taskEntityListFromEntityDetailList=new ArrayList<TaskEntityDetailUploaderDTO>();
		Map<String,String> actualFrequencyNameMap= dldDao.getAllActualNameFrequency(clientCode);

		for(TaskMasterUploaderDTO taskMasterDTO:taskMasterList){
				versionNo=dldDao.getMaxVersionNoForTask(clientCode, taskMasterDTO.getTask_Repository(), taskMasterDTO.getTask_Name());
				taskForEditList=dldDao.getTaskForEdit(clientCode, taskMasterDTO.getTask_Repository(), taskMasterDTO.getTask_Name());
				tMasterFromDb=dldDao.getTaskMaster(clientCode, taskMasterDTO.getTask_Repository(), taskMasterDTO.getTask_Name(),formater.parse(effectiveDate));
				if(taskForEditList!=null && taskForEditList.size()>0){
						int taskCount=0;
						int taskCreateCount=0;
						for(DlTaskMaster task:taskForEditList){
							if(task.getStartDate().compareTo(formater.parse(effectiveDate))<0){
								if(taskCount==0){
									taskCount=1;
									task.setEnddate(new DateTime(formater.parse(effectiveDate)).minusDays(1).toDate());
									updateTaskList.add(task);
									//Create new Entry
									tMaster=new DlTaskMaster();
									tMaster.setClientCode(clientCode);
									tMaster.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
									tMaster.setTaskName(taskMasterDTO.getTask_Name().trim());
									tMaster.setVersionNo(versionNo+1);
									tMaster.setStartDate(formater.parse(effectiveDate));
									tMaster.setEnddate(formater.parse(DUMMY_END_DATE));
									tMaster.setIsActive("Y");
									tMaster.setTaskType(taskMasterDTO.getTask_Type().trim());
									tMaster.setTaskDescription(taskMasterDTO.getDescription().trim());
									tMaster.setTechnicalTaskName(taskMasterDTO.getTask_Technical_Name().trim());
									tMaster.setTechnicalSubTaskName(taskMasterDTO.getSub_Task_Technical_Name().trim());
									tMaster.setIsValidationRequired(taskMasterDTO.getIsValidationRequired().trim());
									insertTaskMasterList.add(tMaster);


									//For FlowType
									flowList=taskMasterDTO.getTask_Flows();
									if(flowList!=null &&flowList.size()>0){
										for(String flowType: flowList){
											taskFlowType=new DlTaskFlowType();
											taskFlowType.setClientCode(clientCode);
											taskFlowType.setFlowType(flowType.trim());
											taskFlowType.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFlowType.setVersionNo(versionNo+1);
											taskFlowType.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFlowTypeList.add(taskFlowType);
										}


									}

									//For Task Frequency
									List<TaskFrequencyOffset> taskFrequencyOffsetList=taskMasterDTO.getTask_Frequency_Offset();
									if(taskFrequencyOffsetList!=null && taskFrequencyOffsetList.size()>0){
										for(TaskFrequencyOffset taskFrequencyOffset:taskFrequencyOffsetList){	
											taskFrequency=new DlTaskFrequency();
											taskFrequency.setClientCode(clientCode);
											taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFrequency.setVersionNo(versionNo+1);
											taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyOffset.getFrequency().toLowerCase().trim()));
											taskFrequency.setOffset(taskFrequencyOffset.getOffset());
											taskFrequency.setIsExclusionInd("N");
											taskFrequencyList.add(taskFrequency);
										}

									}

									//For Task Exclusion Frequency
									List<TaskFrequencyExclusionOffset> taskFrequencyExOffsetList=taskMasterDTO.getTask_Frequency_Exclusions_Offset();
									if(taskFrequencyExOffsetList!=null && taskFrequencyExOffsetList.size()>0){
										for(TaskFrequencyExclusionOffset taskFrequencyExclusionOffset:taskFrequencyExOffsetList){
											taskFrequency=new DlTaskFrequency();
											taskFrequency.setClientCode(clientCode);
											taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFrequency.setVersionNo(versionNo+1);
											taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyExclusionOffset.getFrequency().toLowerCase().trim()));
											taskFrequency.setOffset(taskFrequencyExclusionOffset.getOffset());
											taskFrequency.setIsExclusionInd("Y");
											taskFrequencyList.add(taskFrequency);
										}

									}


									taskEntityListFromEntityDetailList  = taskEntityDetailList.stream()              
											.filter(line -> line.getTaskName().equalsIgnoreCase(taskMasterDTO.getTask_Name()) && line.getTaskRepository().equalsIgnoreCase(taskMasterDTO.getTask_Repository()) )    
											.collect(Collectors.toList());


									if(taskEntityListFromEntityDetailList!=null && taskEntityListFromEntityDetailList.size()>0){
										for(TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO: taskEntityListFromEntityDetailList){

											sourceEntityList=Arrays.asList(taskEntityDetailUploaderDTO.getEntityName().split("\\s*,\\s*"));
											if(sourceEntityList!=null && sourceEntityList.size()>0){
												for(String sourceName:sourceEntityList){
													taskSourceTarget=new DlTaskSourceTarget();
													taskSourceTarget.setClientCode(clientCode);
													taskSourceTarget.setTaskname(taskEntityDetailUploaderDTO.getTaskName().trim());
													taskSourceTarget.setTaskRepository(taskEntityDetailUploaderDTO.getTaskRepository().trim());
													taskSourceTarget.setEntityName(sourceName);
													taskSourceTarget.setOwnerName(taskEntityDetailUploaderDTO.getEntityOwnerName().trim());
													taskSourceTarget.setVersionNo(versionNo+1);
													taskSourceTarget.setLinkType(taskEntityDetailUploaderDTO.getLinkType().trim());
													taskSourceTargetList.add(taskSourceTarget);
												}
											}

										}
									}
								}
							}else if(task.getStartDate().compareTo(formater.parse(effectiveDate))>0){
								//Deactivate
								task.setIsActive("N");
								deactivateTaskList.add(task);
								if(tMasterFromDb==null && taskCreateCount==0){
									taskCreateCount=1;
									tMaster=new DlTaskMaster();
									tMaster.setClientCode(clientCode.trim());
									tMaster.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
									tMaster.setTaskName(taskMasterDTO.getTask_Name().trim());
									tMaster.setVersionNo(versionNo+1);
									tMaster.setStartDate(formater.parse(effectiveDate));
									tMaster.setEnddate(formater.parse(DUMMY_END_DATE));
									tMaster.setIsActive("Y");
									tMaster.setTaskType(taskMasterDTO.getTask_Type().trim());
									tMaster.setTaskDescription(taskMasterDTO.getDescription().trim());
									tMaster.setTechnicalTaskName(taskMasterDTO.getTask_Technical_Name().trim());
									tMaster.setTechnicalSubTaskName(taskMasterDTO.getSub_Task_Technical_Name().trim());
									tMaster.setIsValidationRequired(taskMasterDTO.getIsValidationRequired().trim());
									insertTaskMasterList.add(tMaster);


									//For FlowType
									flowList=taskMasterDTO.getTask_Flows();
									if(flowList!=null &&flowList.size()>0){
										for(String flowType: flowList){
											taskFlowType=new DlTaskFlowType();
											taskFlowType.setClientCode(clientCode);
											taskFlowType.setFlowType(flowType.trim());
											taskFlowType.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFlowType.setVersionNo(versionNo+1);
											taskFlowType.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFlowTypeList.add(taskFlowType);
										}


									}

									//For Task Frequency
									List<TaskFrequencyOffset> taskFrequencyOffsetList=taskMasterDTO.getTask_Frequency_Offset();
									if(taskFrequencyOffsetList!=null && taskFrequencyOffsetList.size()>0){
										for(TaskFrequencyOffset taskFrequencyOffset:taskFrequencyOffsetList){	
											taskFrequency=new DlTaskFrequency();
											taskFrequency.setClientCode(clientCode);
											taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFrequency.setVersionNo(versionNo+1);
											taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyOffset.getFrequency().toLowerCase().trim()));
											taskFrequency.setOffset(taskFrequencyOffset.getOffset());
											taskFrequency.setIsExclusionInd("N");
											taskFrequencyList.add(taskFrequency);
										}

									}

									//For Task Exclusion Frequency
									List<TaskFrequencyExclusionOffset> taskFrequencyExOffsetList=taskMasterDTO.getTask_Frequency_Exclusions_Offset();
									if(taskFrequencyExOffsetList!=null && taskFrequencyExOffsetList.size()>0){
										for(TaskFrequencyExclusionOffset taskFrequencyExclusionOffset:taskFrequencyExOffsetList){
											taskFrequency=new DlTaskFrequency();
											taskFrequency.setClientCode(clientCode);
											taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFrequency.setVersionNo(versionNo+1);
											taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyExclusionOffset.getFrequency().toLowerCase().trim()));
											taskFrequency.setOffset(taskFrequencyExclusionOffset.getOffset());
											taskFrequency.setIsExclusionInd("Y");
											taskFrequencyList.add(taskFrequency);
										}

									}


									taskEntityListFromEntityDetailList  = taskEntityDetailList.stream()              
											.filter(line -> line.getTaskName().equalsIgnoreCase(taskMasterDTO.getTask_Name().trim()) && line.getTaskRepository().equalsIgnoreCase(taskMasterDTO.getTask_Repository().trim()) )    
											.collect(Collectors.toList());


									if(taskEntityListFromEntityDetailList!=null && taskEntityListFromEntityDetailList.size()>0){
										for(TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO: taskEntityListFromEntityDetailList){

											sourceEntityList=Arrays.asList(taskEntityDetailUploaderDTO.getEntityName().split("\\s*,\\s*"));
											if(sourceEntityList!=null && sourceEntityList.size()>0){
												for(String sourceName:sourceEntityList){
													taskSourceTarget=new DlTaskSourceTarget();
													taskSourceTarget.setClientCode(clientCode);
													taskSourceTarget.setTaskname(taskEntityDetailUploaderDTO.getTaskName().trim());
													taskSourceTarget.setTaskRepository(taskEntityDetailUploaderDTO.getTaskRepository().trim());
													taskSourceTarget.setEntityName(sourceName.trim());
													taskSourceTarget.setOwnerName(taskEntityDetailUploaderDTO.getEntityOwnerName().trim());
													taskSourceTarget.setVersionNo(versionNo+1);
													taskSourceTarget.setLinkType(taskEntityDetailUploaderDTO.getLinkType().trim());
													taskSourceTargetList.add(taskSourceTarget);
												}
											}

										}
									}


									
								}
							}
							else if(task.getStartDate().compareTo(formater.parse(effectiveDate))==0){
								taskCount=1;
								if(taskMasterDTO.getStatus().equalsIgnoreCase(deactive)){
									task.setIsActive("N");
									deactivateTaskList.add(task);
								}else
								{
									task.setIsActive("N");
									deactivateTaskList.add(task);

									//Create new Entry
									tMaster=new DlTaskMaster();
									tMaster.setClientCode(clientCode);
									tMaster.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
									tMaster.setTaskName(taskMasterDTO.getTask_Name().trim());
									tMaster.setVersionNo(versionNo+1);
									tMaster.setStartDate(formater.parse(effectiveDate));
									tMaster.setEnddate(formater.parse(DUMMY_END_DATE));
									tMaster.setIsActive("Y");
									tMaster.setTaskType(taskMasterDTO.getTask_Type().trim());
									tMaster.setTaskDescription(taskMasterDTO.getDescription().trim());
									tMaster.setTechnicalTaskName(taskMasterDTO.getTask_Technical_Name().trim());
									tMaster.setTechnicalSubTaskName(taskMasterDTO.getSub_Task_Technical_Name().trim());
									tMaster.setIsValidationRequired(taskMasterDTO.getIsValidationRequired().trim());
									insertTaskMasterList.add(tMaster);


									//For FlowType
									flowList=taskMasterDTO.getTask_Flows();
									if(flowList!=null &&flowList.size()>0){
										for(String flowType: flowList){
											taskFlowType=new DlTaskFlowType();
											taskFlowType.setClientCode(clientCode);
											taskFlowType.setFlowType(flowType.trim());
											taskFlowType.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFlowType.setVersionNo(versionNo+1);
											taskFlowType.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFlowTypeList.add(taskFlowType);
										}


									}

									//For Task Frequency
									List<TaskFrequencyOffset> taskFrequencyOffsetList=taskMasterDTO.getTask_Frequency_Offset();
									if(taskFrequencyOffsetList!=null && taskFrequencyOffsetList.size()>0){
										for(TaskFrequencyOffset taskFrequencyOffset:taskFrequencyOffsetList){	
											taskFrequency=new DlTaskFrequency();
											taskFrequency.setClientCode(clientCode);
											taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFrequency.setVersionNo(versionNo+1);
											taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyOffset.getFrequency().toLowerCase().trim()));
											taskFrequency.setOffset(taskFrequencyOffset.getOffset());
											taskFrequency.setIsExclusionInd("N");
											taskFrequencyList.add(taskFrequency);
										}

									}

									//For Task Exclusion Frequency
									List<TaskFrequencyExclusionOffset> taskFrequencyExOffsetList=taskMasterDTO.getTask_Frequency_Exclusions_Offset();
									if(taskFrequencyExOffsetList!=null && taskFrequencyExOffsetList.size()>0){
										for(TaskFrequencyExclusionOffset taskFrequencyExclusionOffset:taskFrequencyExOffsetList){
											taskFrequency=new DlTaskFrequency();
											taskFrequency.setClientCode(clientCode);
											taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
											taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
											taskFrequency.setVersionNo(versionNo+1);
											taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyExclusionOffset.getFrequency().toLowerCase().trim()));
											taskFrequency.setOffset(taskFrequencyExclusionOffset.getOffset());
											taskFrequency.setIsExclusionInd("Y");
											taskFrequencyList.add(taskFrequency);
										}

									}


									taskEntityListFromEntityDetailList  = taskEntityDetailList.stream()              
											.filter(line -> line.getTaskName().equalsIgnoreCase(taskMasterDTO.getTask_Name().trim()) && line.getTaskRepository().equalsIgnoreCase(taskMasterDTO.getTask_Repository().trim()) )    
											.collect(Collectors.toList());


									if(taskEntityListFromEntityDetailList!=null && taskEntityListFromEntityDetailList.size()>0){
										for(TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO: taskEntityListFromEntityDetailList){

											sourceEntityList=Arrays.asList(taskEntityDetailUploaderDTO.getEntityName().split("\\s*,\\s*"));
											if(sourceEntityList!=null && sourceEntityList.size()>0){
												for(String sourceName:sourceEntityList){
													taskSourceTarget=new DlTaskSourceTarget();
													taskSourceTarget.setClientCode(clientCode);
													taskSourceTarget.setTaskname(taskEntityDetailUploaderDTO.getTaskName().trim());
													taskSourceTarget.setTaskRepository(taskEntityDetailUploaderDTO.getTaskRepository().trim());
													taskSourceTarget.setEntityName(sourceName.trim());
													taskSourceTarget.setOwnerName(taskEntityDetailUploaderDTO.getEntityOwnerName().trim());
													taskSourceTarget.setVersionNo(versionNo+1);
													taskSourceTarget.setLinkType(taskEntityDetailUploaderDTO.getLinkType().trim());
													taskSourceTargetList.add(taskSourceTarget);
												}
											}

										}
									}

								}
							}
						}
					

				}
				else{
				//create new entry
				tMaster=new DlTaskMaster();
				tMaster.setClientCode(clientCode);
				tMaster.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
				tMaster.setTaskName(taskMasterDTO.getTask_Name().trim());
				tMaster.setVersionNo(versionNo+1);
				tMaster.setStartDate(formater.parse(effectiveDate));
				tMaster.setEnddate(formater.parse(DUMMY_END_DATE));
				tMaster.setIsActive("Y");
				tMaster.setTaskType(taskMasterDTO.getTask_Type().trim());
				tMaster.setTaskDescription(taskMasterDTO.getDescription().trim());
				tMaster.setTechnicalTaskName(taskMasterDTO.getTask_Technical_Name().trim());
				tMaster.setTechnicalSubTaskName(taskMasterDTO.getSub_Task_Technical_Name().trim());
				tMaster.setIsValidationRequired(taskMasterDTO.getIsValidationRequired().trim());
				insertTaskMasterList.add(tMaster);


				//For FlowType
				flowList=taskMasterDTO.getTask_Flows();
				if(flowList!=null &&flowList.size()>0){
					for(String flowType: flowList){
						taskFlowType=new DlTaskFlowType();
						taskFlowType.setClientCode(clientCode);
						taskFlowType.setFlowType(flowType.trim());
						taskFlowType.setTaskName(taskMasterDTO.getTask_Name().trim());
						taskFlowType.setVersionNo(versionNo+1);
						taskFlowType.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
						taskFlowTypeList.add(taskFlowType);
					}


				}

				//For Task Frequency
				List<TaskFrequencyOffset> taskFrequencyOffsetList=taskMasterDTO.getTask_Frequency_Offset();
				if(taskFrequencyOffsetList!=null && taskFrequencyOffsetList.size()>0){
					for(TaskFrequencyOffset taskFrequencyOffset:taskFrequencyOffsetList){	
						taskFrequency=new DlTaskFrequency();
						taskFrequency.setClientCode(clientCode);
						taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
						taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
						taskFrequency.setVersionNo(versionNo+1);
						taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyOffset.getFrequency().toLowerCase().trim()));
						taskFrequency.setOffset(taskFrequencyOffset.getOffset());
						taskFrequency.setIsExclusionInd("N");
						taskFrequencyList.add(taskFrequency);
					}

				}

				//For Task Exclusion Frequency
				List<TaskFrequencyExclusionOffset> taskFrequencyExOffsetList=taskMasterDTO.getTask_Frequency_Exclusions_Offset();
				if(taskFrequencyExOffsetList!=null && taskFrequencyExOffsetList.size()>0){
					for(TaskFrequencyExclusionOffset taskFrequencyExclusionOffset:taskFrequencyExOffsetList){
						taskFrequency=new DlTaskFrequency();
						taskFrequency.setClientCode(clientCode);
						taskFrequency.setTaskRepository(taskMasterDTO.getTask_Repository().trim());
						taskFrequency.setTaskName(taskMasterDTO.getTask_Name().trim());
						taskFrequency.setVersionNo(versionNo+1);
						taskFrequency.setFrequency(actualFrequencyNameMap.get(taskFrequencyExclusionOffset.getFrequency().toLowerCase().trim()));
						taskFrequency.setOffset(taskFrequencyExclusionOffset.getOffset());
						taskFrequency.setIsExclusionInd("Y");
						taskFrequencyList.add(taskFrequency);
					}

				}


				taskEntityListFromEntityDetailList  = taskEntityDetailList.stream()              
						.filter(line -> line.getTaskName().equalsIgnoreCase(taskMasterDTO.getTask_Name().trim()) && line.getTaskRepository().equalsIgnoreCase(taskMasterDTO.getTask_Repository().trim()) )    
						.collect(Collectors.toList());


				if(taskEntityListFromEntityDetailList!=null && taskEntityListFromEntityDetailList.size()>0){
					for(TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO: taskEntityListFromEntityDetailList){

						sourceEntityList=Arrays.asList(taskEntityDetailUploaderDTO.getEntityName().split("\\s*,\\s*"));
						if(sourceEntityList!=null && sourceEntityList.size()>0){
							for(String sourceName:sourceEntityList){
								taskSourceTarget=new DlTaskSourceTarget();
								taskSourceTarget.setClientCode(clientCode);
								taskSourceTarget.setTaskname(taskEntityDetailUploaderDTO.getTaskName().trim());
								taskSourceTarget.setTaskRepository(taskEntityDetailUploaderDTO.getTaskRepository().trim());
								taskSourceTarget.setEntityName(sourceName.trim());
								taskSourceTarget.setOwnerName(taskEntityDetailUploaderDTO.getEntityOwnerName().trim());
								taskSourceTarget.setVersionNo(versionNo+1);
								taskSourceTarget.setLinkType(taskEntityDetailUploaderDTO.getLinkType().trim());
								taskSourceTargetList.add(taskSourceTarget);
							}
						}

					}
				}
		}
	
				
		}

		finalTaskList.add(insertTaskMasterList);
		finalTaskList.add(taskFlowTypeList);
		finalTaskList.add(taskFrequencyList);
		finalTaskList.add(taskSourceTargetList);
		finalTaskList.add(updateTaskList);
		finalTaskList.add(deactivateTaskList);
		
		return finalTaskList;
	}




	private List<Object> saveDataForTaskRepositories(List<TaskRepositoriesUploaderDTO> taskRepositoriesList,String clientCode, String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > saveDataForTaskRepositories");

		DlTaskRepository taskRepositories=null;
		List<Object> taskRepList=new ArrayList<Object>();
		List<DlTaskRepository> taskRepInsertList=new ArrayList<DlTaskRepository>();
		List<DlTaskRepository> taskRepUpdateList=new ArrayList<DlTaskRepository>();

		for(TaskRepositoriesUploaderDTO taskRepositoriesDTO:taskRepositoriesList){

			taskRepositories=dldDao.getTaskRepository(clientCode,taskRepositoriesDTO.getName().trim());
			if(taskRepositories!=null){
				taskRepositories.setDescription(taskRepositoriesDTO.getDescription().trim());
				taskRepUpdateList.add(taskRepositories);
			}
			else{
				taskRepositories=new DlTaskRepository();
				taskRepositories.setClientCode(clientCode);
				taskRepositories.setRepositoryName(taskRepositoriesDTO.getName().trim());
				taskRepositories.setDescription(taskRepositoriesDTO.getDescription().trim());
				taskRepInsertList.add(taskRepositories);
			}
		}
		taskRepList.add(taskRepUpdateList);
		taskRepList.add(taskRepInsertList);

		return taskRepList;
	}




	private List<Object> saveDataForFlowTypes(List<FlowTypesUploaderDTO> flowTypesList, String clientCode,String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > saveDataForFlowTypes");

		DlFlowType flowType=null;
		List<Object> flowList=new ArrayList<Object>();
		List<DlFlowType> flowInsertList=new ArrayList<DlFlowType>();
		List<DlFlowType> flowUpdateList=new ArrayList<DlFlowType>();
		if(flowTypesList!=null && flowTypesList.size()>0){ 
			for(FlowTypesUploaderDTO flowTypesDTO:flowTypesList){
				flowType=dldDao.getDlFlowType(clientCode,flowTypesDTO.getFlow_type().trim());
				if(flowType!=null){
					flowType.setDescription(flowTypesDTO.getDescription().trim());
					flowUpdateList.add(flowType);

				}
				else{
					flowType=new DlFlowType();
					flowType.setClientCode(clientCode);
					flowType.setDescription(flowTypesDTO.getDescription().trim());
					flowType.setFlowType(flowTypesDTO.getFlow_type().trim());
					flowInsertList.add(flowType);
				}
			}
		}
		flowList.add(flowUpdateList);
		flowList.add(flowInsertList);
		return flowList;
	}




	private List<Object> saveDataForEntityMaster(List<EntityMasterUploaderDTO> entityMasterList, String clientCode, String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > saveDataForEntityMaster");

		DlEntity entityMaster=null;
		List<Object> entityList=new ArrayList<Object>();
		List<DlEntity> entityUpdateList=new ArrayList<DlEntity>();
		List<DlEntity> entityInsertList=new ArrayList<DlEntity>();
		if(null!=entityMasterList && entityMasterList.size()>0){
			for(EntityMasterUploaderDTO entityMasterDTO:entityMasterList)
			{
				entityMaster=dldDao.getEntityMaster(clientCode,entityMasterDTO.getOwner_Name().trim(),entityMasterDTO.getEntity_Name().trim());
				if(entityMaster!=null){
					entityMaster.setDescription(entityMasterDTO.getDescription().trim());
					entityMaster.setEntityDetail(entityMasterDTO.getEntity_Detail().trim());
					entityMaster.setEntityType(entityMasterDTO.getEntity_Type().trim());
					entityUpdateList.add(entityMaster);
				}
				else{
					entityMaster=new DlEntity();
					entityMaster.setClientCode(clientCode);
					entityMaster.setDescription(entityMasterDTO.getDescription().trim());
					entityMaster.setEntityDetail(entityMasterDTO.getEntity_Detail().trim());
					entityMaster.setEntityName(entityMasterDTO.getEntity_Name().trim());
					entityMaster.setEntityType(entityMasterDTO.getEntity_Type().trim());
					entityMaster.setOwnerName(entityMasterDTO.getOwner_Name().trim());
					entityInsertList.add(entityMaster);
				}
			}

		}
		entityList.add(entityUpdateList);
		entityList.add(entityInsertList);
		return entityList;
	}




	private List<Object> saveDataForEntityOwners(List<EntityOwnerUploaderDTO> entityOwnersUploadList, String  clientCode, String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > saveDataForEntityOwners");

		List<DlEntityOwner> entityOwnerInsertList=new ArrayList<DlEntityOwner>();
		List<DlEntityOwner> entityOwnerUpdateList=new ArrayList<DlEntityOwner>();
		List<Object> entityOwnerList=new ArrayList<Object>();
		DlEntityOwner entityOwner=null;

		List<DataSource> dataSourceList=dldDao.getAllDataSource(clientCode);
		List<DldSolution> solutionList=dldDao.getAllSoutions(clientCode);
		Map<String, Integer> dsMap=new HashMap<String,Integer>();
		Map<String, Integer> solMap=new HashMap<String,Integer>();

		if(null!=dataSourceList && dataSourceList.size()>0){
			for(DataSource dataSource:dataSourceList)
				dsMap.put(dataSource.getDataSourceName(),dataSource.getDataSourceID());
		}

		if(null!=solutionList && solutionList.size()>0){
			for(DldSolution dldSolution:solutionList)
				solMap.put(dldSolution.getSolutionName(),dldSolution.getSolutionID());
		}


		if(entityOwnersUploadList!=null && entityOwnersUploadList.size()>0){ 

			for(EntityOwnerUploaderDTO entityownerDTO:entityOwnersUploadList)
			{

				entityOwner=dldDao.getEntityOwner(clientCode,entityownerDTO.getOwner_Name().trim());
				if(entityOwner!=null){
					entityOwner.setSolution_Id(solMap.get(entityownerDTO.getSolution_Name()));
					entityOwner.setData_Source_Id(dsMap.get(entityownerDTO.getData_source_Name()));
					entityOwner.setExternal_Source(entityownerDTO.getExternal_Source());
					entityOwner.setDescription(entityownerDTO.getDescription());
					entityOwner.setDisplay_Sorting_Order(entityownerDTO.getDisplay_Sorting_Order());
					entityOwner.setContact_Details(entityownerDTO.getContact_Details());
					entityOwnerUpdateList.add(entityOwner);
				}
				else{
					entityOwner=new DlEntityOwner();
					entityOwner.setClientCode(clientCode);
					entityOwner.setOwner_Name(entityownerDTO.getOwner_Name());
					entityOwner.setSolution_Id(solMap.get(entityownerDTO.getSolution_Name()));
					entityOwner.setData_Source_Id(dsMap.get(entityownerDTO.getData_source_Name()));
					entityOwner.setExternal_Source(entityownerDTO.getExternal_Source());
					entityOwner.setDescription(entityownerDTO.getDescription());
					entityOwner.setDisplay_Sorting_Order(entityownerDTO.getDisplay_Sorting_Order());
					entityOwner.setContact_Details(entityownerDTO.getContact_Details());
					entityOwnerInsertList.add(entityOwner);
				}
			}

		}
		entityOwnerList.add(entityOwnerUpdateList);
		entityOwnerList.add(entityOwnerInsertList);


		return entityOwnerList;
	}


	@Override
	public Workbook getDataToDownload(String clientCode,String effectiveDate, Workbook workbook) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getDataToDownload");

		SimpleDateFormat formater= new SimpleDateFormat(dateFormat);
		java.util.Date dt= formater.parse(effectiveDate);
		java.sql.Date effDate=new java.sql.Date(dt.getTime());
		List<TaskMasterUploaderDTO> taskMasterList=new ArrayList<TaskMasterUploaderDTO>();
		List<TaskEntityDetailUploaderDTO> taskEntityDetailList=new ArrayList<TaskEntityDetailUploaderDTO>();
		List<Object> objList=new ArrayList<Object>();

		//List<ClientUploaderDTO> clientList=getClientData(effDate);
		List<DlEntityOwner> entityOwnerList=getEntityOwnerData(clientCode,effDate);
		List<DlEntity> entityMasterList=getEntitymasterData(clientCode,effDate);
		List<DlFlowType> flowTypesList=getFlowTypesData(clientCode,effDate);//check
		List<DlTaskRepository> taskRepositoryList=gettaskrepositoriesData(clientCode,effDate);
		objList=gettaskMasterData(clientCode,effDate);
		
		if(objList!=null && objList.size()>0){
		taskMasterList=(List<TaskMasterUploaderDTO>) objList.get(0);
		taskEntityDetailList=(List<TaskEntityDetailUploaderDTO>) objList.get(1);
		}


		Map<Integer,String> solmap= new HashMap<Integer,String>();
		Map<Integer,String> dsMap= new HashMap<Integer,String>();

		List<DldSolution> solList=dldDao.getAllSoutions(clientCode);
		if(solList!=null && solList.size()>0){
			for(DldSolution sol:solList){
				solmap.put(sol.getSolutionID(), sol.getSolutionName());

			}

		}


		List<DataSource> dsList=dldDao.getAllDataSource(clientCode);
		if(dsList!=null && dsList.size()>0){
			for(DataSource dataSource:dsList){
				dsMap.put(dataSource.getDataSourceID(), dataSource.getDataSourceName());

			}

		}

		//EntityOwner
		if(entityOwnerList!=null){
			DlEntityOwner entityOwner ;
			Cells cells1=workbook.getWorksheets().get(entityOwnerSheetName).getCells();
			for (int i = 0; i < entityOwnerList.size(); i++) {
				String solName="";
				String dsName="";
				entityOwner = entityOwnerList.get(i);
				for (int j = 0; j < 7; j++) {
					if (j == 0) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityOwner.getOwner_Name());
					}
					if (j == 1) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityOwner.getDescription());
					}
					if (j == 2) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityOwner.getExternal_Source());
					}

					dsName=dsMap.get(entityOwner.getData_Source_Id());
					if (j == 3) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(dsName);
					}


					solName=solmap.get(entityOwner.getSolution_Id());
					if (j == 4) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(solName);
					}
					if (j == 5) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityOwner.getContact_Details());
					}
					if (j == 6) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityOwner.getDisplay_Sorting_Order());
					}

				}
			}
		}

		//Entity master
		if(entityMasterList!=null){
			DlEntity entityMaster ;
			Cells cells1=workbook.getWorksheets().get(entityMasterSheetName).getCells();
			for (int i = 0; i < entityMasterList.size(); i++) {
				entityMaster = entityMasterList.get(i);
				for (int j = 0; j < 5; j++) {
					if (j == 0) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityMaster.getEntityName());
					}
					if (j == 1) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityMaster.getOwnerName());
					}
					if (j == 2) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityMaster.getEntityType());
					}
					if (j == 3) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityMaster.getEntityDetail());
					}
					if (j == 4) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(entityMaster.getDescription());
					}

				}
			}
		}

		//Flow types
		if(flowTypesList!=null){
			DlFlowType flowType ;
			Cells cells1=workbook.getWorksheets().get(flowTypeSheetName).getCells();
			for (int i = 0; i < flowTypesList.size(); i++) {
				flowType = flowTypesList.get(i);
				for (int j = 0; j < 2; j++) {
					if (j == 0) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(flowType.getFlowType());
					}
					if (j == 1) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(flowType.getDescription());
					}


				}
			}
		}

		//Task Repositories
		if(taskRepositoryList!=null){
			DlTaskRepository taskRepository ;
			Cells cells1=workbook.getWorksheets().get(taskRepositorySheetName).getCells();
			for (int i = 0; i < taskRepositoryList.size(); i++) {
				taskRepository = taskRepositoryList.get(i);
				for (int j = 0; j < 2; j++) {
					if (j == 0) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskRepository.getRepositoryName());
					}
					if (j == 1) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskRepository.getDescription());
					}


				}
			}
		}


		//task master
		if(taskMasterList!=null){
			TaskMasterUploaderDTO taskMaster ;
			List<String> flowlist=null;
			String finalFlow="";
			String freq="";
			String freqEx="";
			Cells cells1=workbook.getWorksheets().get(taskMasterSheetName).getCells();
			for (int i = 0; i < taskMasterList.size(); i++) {
				taskMaster = taskMasterList.get(i);
				for (int j = 0; j < 11; j++) {
					if (j == 0) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getTask_Repository());
					}
					if (j == 1) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getTask_Name());
					}
					if (j == 2) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getTask_Type());
					}
					if (j == 3) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getDescription());
					}
					if (j == 4) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getTask_Technical_Name());
					}
					if (j == 5) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getSub_Task_Technical_Name());
					}
					if (j == 6) {

						//to do
						flowlist=taskMaster.getTask_Flows();
						finalFlow="";
						if(flowlist!=null && flowlist.size()>0){
							for(String str:flowlist)
							{
								if(finalFlow.equals(""))
									finalFlow=finalFlow+str;
								else
									finalFlow=finalFlow+","+str;
							}
						}

						Cell cell = cells1.get(i + 1, j);
						cell.setValue(finalFlow);
					}
					if (j == 7) {

						// to do
						freq="";
						List<TaskFrequencyOffset> taskFrequencyOffsetList=taskMaster.getTask_Frequency_Offset();
						if(taskFrequencyOffsetList!=null && taskFrequencyOffsetList.size()>0){
							for(TaskFrequencyOffset taskFrequencyOffset:taskFrequencyOffsetList){
								if(freq.equals(""))
									freq=freq+"("+taskFrequencyOffset.getFrequency()+","+taskFrequencyOffset.getOffset()+")";
								else
									freq=freq+","+"("+taskFrequencyOffset.getFrequency()+","+taskFrequencyOffset.getOffset()+")";
							}

						}

						Cell cell = cells1.get(i + 1, j);
						cell.setValue(freq);
					}

					if (j == 8) {

						// to do
						freqEx="";
						List<TaskFrequencyExclusionOffset> taskFrequencyExclusionOffsetList=taskMaster.getTask_Frequency_Exclusions_Offset();
						if(taskFrequencyExclusionOffsetList!=null && taskFrequencyExclusionOffsetList.size()>0){
							for(TaskFrequencyExclusionOffset taskFrequencyExclusionOffset:taskFrequencyExclusionOffsetList){
								if(freqEx.equals(""))
									freqEx=freqEx+"("+taskFrequencyExclusionOffset.getFrequency()+","+taskFrequencyExclusionOffset.getOffset()+")";
								else
									freqEx=freqEx+","+"("+taskFrequencyExclusionOffset.getFrequency()+","+taskFrequencyExclusionOffset.getOffset()+")";
							}

						}

						Cell cell = cells1.get(i + 1, j);
						cell.setValue(freqEx);
					}

					if (j == 9) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getStatus());
					}
					if (j == 10) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskMaster.getIsValidationRequired());
					}


				}
			}
		}

		//entity Detail
		if(taskEntityDetailList!=null){
			TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO ;
			Cells cells1=workbook.getWorksheets().get(taskEntityDetailsSheetName).getCells();
			for (int i = 0; i < taskEntityDetailList.size(); i++) {
				taskEntityDetailUploaderDTO = taskEntityDetailList.get(i);
				for (int j = 0; j < 5; j++) {
					if (j == 0) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskEntityDetailUploaderDTO.getTaskRepository());
					}
					if (j == 1) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskEntityDetailUploaderDTO.getTaskName());
					}
					if (j == 2) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskEntityDetailUploaderDTO.getEntityOwnerName());
					}
					if (j == 3) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskEntityDetailUploaderDTO.getEntityName());
					}
					if (j == 4) {
						Cell cell = cells1.get(i + 1, j);
						cell.setValue(taskEntityDetailUploaderDTO.getLinkType());
					}


				}
			}
		}
		return workbook;
	}




	private List<Object> gettaskMasterData(String clientCode,Date effDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > gettaskMasterData");

		List<TaskMasterUploaderDTO> taskMasterDTOList=new ArrayList<TaskMasterUploaderDTO>();
		TaskMasterUploaderDTO taskMasterDTO=null;
		List<DlTaskFlowType> flowList=null;
		List<DlTaskFrequency>freqList=null;
		List<DlTaskSourceTarget> sTList=null;
		List<TaskFrequencyOffset> taskFreqOffsetList=null; 
		List<String> flow=null;
		TaskFrequencyExclusionOffset taskFrequencyExclusionOffset=null;
		TaskFrequencyOffset taskFrequencyOffset=null;
		List<TaskFrequencyExclusionOffset> taskFrequencyExclusionOffsetList=null; 
		Set<String> ownerSet=null;
		TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO=null;
		List<TaskEntityDetailUploaderDTO> taskEntityDetailUploaderDTOList=new ArrayList<TaskEntityDetailUploaderDTO>();
		List<Object> taskdataList=new ArrayList<Object>();


		List<DlTaskMaster> taskMasterList= dldDao.gettaskData(clientCode,effDate);
		if(taskMasterList!=null && taskMasterList.size()>0){
			for(DlTaskMaster taskMaster:taskMasterList){
				flow=new ArrayList<String>();
				taskFreqOffsetList=new ArrayList<TaskFrequencyOffset>();
				taskFrequencyExclusionOffsetList=new ArrayList<TaskFrequencyExclusionOffset>();
				String source_owner_Name="";
				String source_entity_Name="";
				String target_owner_Name="";
				String target_entity_Name="";

				//For Flow Types
				flowList=dldDao.getFlowData(taskMaster.getClientCode(),taskMaster.getTaskRepository(),taskMaster.getTaskName(),taskMaster.getVersionNo());
				if(flowList!=null && flowList.size()>0){
					for(DlTaskFlowType taskFlowType:flowList)
						flow.add(taskFlowType.getFlowType());
				}

				//For Frequency
				freqList=dldDao.getFeqData(taskMaster.getClientCode(),taskMaster.getTaskRepository(),taskMaster.getTaskName(),taskMaster.getVersionNo());
				if(freqList!=null && freqList.size()>0){
					for(DlTaskFrequency taskFrequency:freqList){

						if(taskFrequency.getIsExclusionInd().equalsIgnoreCase("Y")){
							taskFrequencyExclusionOffset=new TaskFrequencyExclusionOffset();
							taskFrequencyExclusionOffset.setFrequency(taskFrequency.getFrequency());
							taskFrequencyExclusionOffset.setOffset(taskFrequency.getOffset());
							taskFrequencyExclusionOffsetList.add(taskFrequencyExclusionOffset);
						}
						else{
							taskFrequencyOffset=new TaskFrequencyOffset();
							taskFrequencyOffset.setFrequency(taskFrequency.getFrequency());
							taskFrequencyOffset.setOffset(taskFrequency.getOffset());
							taskFreqOffsetList.add(taskFrequencyOffset);
						}
					}
				}

				//For Source Target
				sTList=dldDao.getSourceTargetData(taskMaster.getClientCode(),taskMaster.getTaskRepository(),taskMaster.getTaskName(),taskMaster.getVersionNo());
				source_entity_Name="";
				target_entity_Name="";
				ownerSet=new HashSet<String>();
				if(sTList!=null && sTList.size()>0){
					for(DlTaskSourceTarget taskSourceTarget:sTList){
						ownerSet.add(taskSourceTarget.getOwnerName());
					}

					for(String ownerName:ownerSet)	{	
						source_entity_Name="";
						target_entity_Name="";
						//For Source
						List<DlTaskSourceTarget> sourceList = sTList.stream()               
								.filter(line -> line.getOwnerName().equals(ownerName)  && line.getLinkType().equals("S"))
								.collect(Collectors.toList());
						if(sourceList!=null && sourceList.size()>0){		
							taskEntityDetailUploaderDTO=new TaskEntityDetailUploaderDTO();
							taskEntityDetailUploaderDTO.setTaskRepository(taskMaster.getTaskRepository());
							taskEntityDetailUploaderDTO.setTaskName(taskMaster.getTaskName());
							taskEntityDetailUploaderDTO.setEntityOwnerName(ownerName);

							for(DlTaskSourceTarget taskSource:sourceList){
								if(source_entity_Name.equals(""))
									source_entity_Name=source_entity_Name+taskSource.getEntityName();
								else
									source_entity_Name=source_entity_Name+","+taskSource.getEntityName();
							}
							taskEntityDetailUploaderDTO.setEntityName(source_entity_Name);;
							taskEntityDetailUploaderDTO.setLinkType("S");
							taskEntityDetailUploaderDTOList.add(taskEntityDetailUploaderDTO);
						}
						//For Target
						List<DlTaskSourceTarget> targetList = sTList.stream()               
								.filter(line -> line.getOwnerName().equals(ownerName)  && line.getLinkType().equals("T"))
								.collect(Collectors.toList());

						if(targetList!=null && targetList.size()>0){
							taskEntityDetailUploaderDTO=new TaskEntityDetailUploaderDTO();
							taskEntityDetailUploaderDTO.setTaskRepository(taskMaster.getTaskRepository());
							taskEntityDetailUploaderDTO.setTaskName(taskMaster.getTaskName());
							taskEntityDetailUploaderDTO.setEntityOwnerName(ownerName);
							for(DlTaskSourceTarget taskTarget:targetList){
								if(target_entity_Name.equals(""))
									target_entity_Name=target_entity_Name+taskTarget.getEntityName();
								else
									target_entity_Name=target_entity_Name+","+taskTarget.getEntityName();
							}
							taskEntityDetailUploaderDTO.setEntityName(target_entity_Name);;
							taskEntityDetailUploaderDTO.setLinkType("T");
							taskEntityDetailUploaderDTOList.add(taskEntityDetailUploaderDTO);
						}
					}
				}

				taskMasterDTO=new TaskMasterUploaderDTO();
				taskMasterDTO.setTask_Repository(taskMaster.getTaskRepository());
				taskMasterDTO.setTask_Name(taskMaster.getTaskName());
				taskMasterDTO.setTask_Type(taskMaster.getTaskType());
				taskMasterDTO.setDescription(taskMaster.getTaskDescription());
				taskMasterDTO.setTask_Technical_Name(taskMaster.getTechnicalTaskName());
				taskMasterDTO.setSub_Task_Technical_Name(taskMaster.getTechnicalSubTaskName());
				taskMasterDTO.setTask_Flows(flow);
				taskMasterDTO.setTask_Frequency_Offset(taskFreqOffsetList);
				taskMasterDTO.setTask_Frequency_Exclusions_Offset(taskFrequencyExclusionOffsetList);
				taskMasterDTO.setStatus("Active");
				taskMasterDTO.setIsValidationRequired(taskMaster.getIsValidationRequired());
				taskMasterDTOList.add(taskMasterDTO);
				taskdataList.add(taskMasterDTOList);
				taskdataList.add(taskEntityDetailUploaderDTOList);


			}

		}

		return taskdataList;
	}




	private List<DlTaskRepository> gettaskrepositoriesData(String clientCode,Date effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > gettaskrepositoriesData");
		List<DlTaskRepository> taskRepositoryList= dldDao.gettaskrepositoriesData(clientCode,effectiveDate);
		return taskRepositoryList;
	}




	private List<DlFlowType> getFlowTypesData(String clientCode,Date effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getFlowTypesData");
		List<DlFlowType> flowTypesList=dldDao.getFlowTypesData(clientCode,effectiveDate);
		return flowTypesList;
	}




	private List<DlEntity> getEntitymasterData(String clientCode,Date effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getEntitymasterData");
		List<DlEntity> entityMasterList=dldDao.getEntityMasterData(clientCode);
		return entityMasterList;
	}




	private List<DlEntityOwner> getEntityOwnerData(String clientCode,Date effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getEntityOwnerData");
		List<DlEntityOwner> entityOwnerList=dldDao.getEntityOwnerData(clientCode);
		return entityOwnerList;
	}



	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getDetailsForDataConsumers(String currrentBusinessDate,
			String isFrequencyFilterApplied,String frequencyFilterCSV,String isFlowFilterApplied,String flowFilterCSV,String clientCode) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getDetailsForDataConsumers");
		JSONArray finalResponse = new JSONArray();
		Map<String,Integer> solutions=new HashMap<>();
		solutions=dldDao.getListOfSolutionNames(clientCode);
		DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		Map<Integer,Set<String>> reportIdEntityNameMap = new HashMap<Integer,Set<String>>() ;
		Set<String> freqApplicableForcbd=new HashSet<String>();
		List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
		Map<String,Integer> statsMap = null;
		Map<Integer,HashMap<String,Integer>> reportEntityStats = new HashMap<Integer, HashMap<String,Integer>>();
		Map<String,HashMap<String,Integer>> sourceEntityDetails = new HashMap<String, HashMap<String,Integer>>();
		DateTime sysDate=new DateTime();

		//getting applicable frequencies and generating csv for in clause if filter is not applied.
		if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
			freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
			frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
		}

		//get staging details.
		totalStagingObject = dldDao.getStagingDetails(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);


		//processing staging results
		for(StagingDetails tsk :totalStagingObject){
			//for source entity details.
			//check for src system entry
			String entityNameOwner = tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
			if(sourceEntityDetails.get(entityNameOwner)==null){
				statsMap = new HashMap<String, Integer>();
				statsMap.put("completed",0 );
				statsMap.put("notDueYet",0 );
				statsMap.put("failed",0 );
				statsMap.put("overDue",0 );
				sourceEntityDetails.put(entityNameOwner, (HashMap<String, Integer>) statsMap);
			}
			if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
				//not yet started 
				sourceEntityDetails.get(entityNameOwner).put("notDueYet",
						(sourceEntityDetails.get(entityNameOwner).get("notDueYet")+1));
			}
			else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
				//check for over due failed success.
				if(null ==tsk.getTaskStatus()){
					//overDueTask
					sourceEntityDetails.get(entityNameOwner).put("overDue",
							(sourceEntityDetails.get(entityNameOwner).get("overDue")+1));
				}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//failed tasks.
					sourceEntityDetails.get(entityNameOwner).put("failed",
							(sourceEntityDetails.get(entityNameOwner).get("failed")+1));
				}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//completed tasks.
					sourceEntityDetails.get(entityNameOwner).put("completed",
							(sourceEntityDetails.get(entityNameOwner).get("completed")+1));
				}
			}
		}
		Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);
		//processing stats of entuty to get report stats.
		for(Map.Entry<String, Integer> sol:solutions.entrySet()){
			reportIdEntityNameMap = new HashMap<Integer,Set<String>>();
			reportEntityStats = new HashMap<Integer, HashMap<String,Integer>>();
			reportIdEntityNameMap=dldDao.getListOfRuleIds(businessDateRec,sol.getKey(),sol.getValue(),clientCode);
			for (Map.Entry<Integer,Set<String>> entry : reportIdEntityNameMap.entrySet()) {
				//looping through map of report and entity name to fetch stats.
				Integer reportId =  entry.getKey();
				Set<String> entityOwnerSet = new HashSet<String>();
				Set<String> entityOwnerSetImmediate = new HashSet<String>();
				for(String eName:entry.getValue()){
					if(Y.equalsIgnoreCase(entityAndTypeMap.get(eName+SEPARATOR+sol.getKey()))){
						entityOwnerSetImmediate.add(eName+SEPARATOR+sol.getKey());
					}else{
						entityOwnerSet.add(eName+SEPARATOR+sol.getKey());
					}
					
				}
				Set<String> underLyingExecEntities = getUnderLyingEntites(entityOwnerSet,clientCode,new HashSet<String>(),entityAndTypeMap);
				underLyingExecEntities.addAll(entityOwnerSetImmediate);
				Set<String> value =  underLyingExecEntities;
				if(null==reportEntityStats.get(reportId)){
					statsMap = new HashMap<String, Integer>();
					statsMap.put("completed",0 );
					statsMap.put("notDueYet",0 );
					statsMap.put("partiallyCompleted",0 );
					statsMap.put("overDue",0 );
					reportEntityStats.put(reportId, (HashMap<String, Integer>) statsMap);
				}
				for(String entityName : value){
					String entityNameOwner = entityName;
					if(null!=sourceEntityDetails.get(entityNameOwner)){
						//checking the entity status for a report.
						if(sourceEntityDetails.get(entityNameOwner).get("completed")>0 && sourceEntityDetails.get(entityNameOwner).get("notDueYet")==0
								&& sourceEntityDetails.get(entityNameOwner).get("failed")==0 && sourceEntityDetails.get(entityNameOwner).get("overDue")==0){
							reportEntityStats.get(reportId).put("completed",
									reportEntityStats.get(reportId).get("completed")+1);
						}else if(sourceEntityDetails.get(entityNameOwner).get("overDue")>0 || sourceEntityDetails.get(entityNameOwner).get("failed")>0){
							reportEntityStats.get(reportId).put("overDue",
									reportEntityStats.get(reportId).get("overDue")+1);
						}else if(sourceEntityDetails.get(entityNameOwner).get("completed")>0 && (sourceEntityDetails.get(entityNameOwner).get("notDueYet")>0
								 )){
							reportEntityStats.get(reportId).put("partiallyCompleted",
									reportEntityStats.get(reportId).get("partiallyCompleted")+1);
						}else if(sourceEntityDetails.get(entityNameOwner).get("completed")==0 && sourceEntityDetails.get(entityNameOwner).get("notDueYet")>0
								&& sourceEntityDetails.get(reportId).get("entityName")==0 && sourceEntityDetails.get(entityNameOwner).get("overDue")==0){
							reportEntityStats.get(reportId).put("notDueYet",
									reportEntityStats.get(reportId).get("notDueYet")+1);
						}
					}
				}
			}
			JSONObject solutionReportDetails = new JSONObject();
			solutionReportDetails.put("solutionName", sol.getKey());

			Integer completedCount = 0;
			Integer notDueYetCount = 0;
			Integer overDueCount = 0;
			Integer total = 0;
			Integer partiallyCompletedCount = 0;
			for (Map.Entry<Integer,HashMap<String,Integer>> entry : reportEntityStats.entrySet()) {
				//looping through map of report and entity name to fetch stats.
				HashMap<String,Integer> value =  entry.getValue();
				if(!(value.get("completed")==0 && value.get("partiallyCompleted")==0 && value.get("notDueYet")==0 && value.get("overDue")==0)){
					total++;
					if(value.get("completed")>0 && value.get("partiallyCompleted")==0 && value.get("notDueYet")==0 && value.get("overDue")==0){
						completedCount++;
					}else if(value.get("overDue")>0){
						overDueCount++;
					}else if(value.get("partiallyCompleted")>0){
						partiallyCompletedCount++;
					}else{
						notDueYetCount++;
					}
				}

			}
			solutionReportDetails.put("totalReports", total);
			solutionReportDetails.put("completed", completedCount);
			solutionReportDetails.put("partiallyCompleted", partiallyCompletedCount);
			solutionReportDetails.put("notDueYet",notDueYetCount);
			solutionReportDetails.put("overDue", overDueCount);

			finalResponse.add(solutionReportDetails);
		}
		//reportEntityStats has stats for each report number of entites completed failed or overdue.
		//preparing json using the stats.




		return finalResponse;
	}




	@SuppressWarnings("unchecked")
	@Override
	public List<Object> validateMetaData(String clientCode,List<EntityMasterUploaderDTO> enitityMasterUploaderlist, List<EntityOwnerUploaderDTO> entityownerUploadList,
			List<FlowTypesUploaderDTO> flowTypesList, List<TaskMasterUploaderDTO> taskMasterList,
			List<TaskRepositoriesUploaderDTO> taskRepositoriesList,List<TaskEntityDetailUploaderDTO> taskEntityDetailList,String effectiveDate) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > validateMetaData");

		List<ErrorLogForUploader> errorLogList=null;
		List<ErrorLogForUploader> masterErrorMsgList = new ArrayList<ErrorLogForUploader>();
		JSONArray allDTOValStatus=new JSONArray();
		JSONObject valStatus = null;
		List<Object> objectList=new ArrayList<Object>(); 

		// Entity Owner Validation
		if(entityownerUploadList!=null && entityownerUploadList.size()>0){
			errorLogList=validationForEntityOwner(entityownerUploadList, effectiveDate,clientCode);
			if(errorLogList!=null && errorLogList.size()>0){
				masterErrorMsgList.addAll(errorLogList);
				valStatus=new JSONObject();
				valStatus.put("entityName","EntityOwner");
				valStatus.put("noOfSuccess", UploaderHelper.getActualEntityOwnerDTOCount(entityownerUploadList)-UploaderHelper.getActualErrorDTOCount(errorLogList));
				valStatus.put("noOfError",UploaderHelper.getActualErrorDTOCount(errorLogList));
				allDTOValStatus.add(valStatus);
			}else{
				valStatus=new JSONObject();
				valStatus.put("entityName","EntityOwner");
				valStatus.put("noOfSuccess", UploaderHelper.getActualEntityOwnerDTOCount(entityownerUploadList));
				valStatus.put("noOfError",0);
				allDTOValStatus.add(valStatus);

			}
		}



		// Entity Master Validation
		if(enitityMasterUploaderlist!=null && enitityMasterUploaderlist.size()>0){
			errorLogList=validationForEntityMaster(enitityMasterUploaderlist, entityownerUploadList,effectiveDate,clientCode);
			if(errorLogList!=null && errorLogList.size()>0){
				masterErrorMsgList.addAll(errorLogList);
				valStatus=new JSONObject();
				valStatus.put("entityName","EntityMaster");
				valStatus.put("noOfSuccess", UploaderHelper.getActualEntityMasterDTOCount(enitityMasterUploaderlist)-UploaderHelper.getActualErrorDTOCount(errorLogList));
				valStatus.put("noOfError",UploaderHelper.getActualErrorDTOCount(errorLogList));
				allDTOValStatus.add(valStatus);
			}else{
				valStatus=new JSONObject();
				valStatus.put("entityName","EntityMaster");
				valStatus.put("noOfSuccess", UploaderHelper.getActualEntityMasterDTOCount(enitityMasterUploaderlist));
				valStatus.put("noOfError",0);
				allDTOValStatus.add(valStatus);

			}
		}


		// Flow Types Validation
		if(flowTypesList!=null && flowTypesList.size()>0){
			errorLogList=validationForFlowTypes(flowTypesList, effectiveDate);
			if(errorLogList!=null && errorLogList.size()>0){
				masterErrorMsgList.addAll(errorLogList);
				valStatus=new JSONObject();
				valStatus.put("entityName","Flow Types");
				valStatus.put("noOfSuccess", UploaderHelper.getActualFlowTypesDTOCount(flowTypesList)-UploaderHelper.getActualErrorDTOCount(errorLogList));
				valStatus.put("noOfError",UploaderHelper.getActualErrorDTOCount(errorLogList));
				allDTOValStatus.add(valStatus);
			}
			else{
				valStatus=new JSONObject();
				valStatus.put("entityName","Flow Types");
				valStatus.put("noOfSuccess", UploaderHelper.getActualFlowTypesDTOCount(flowTypesList));
				valStatus.put("noOfError",0);
				allDTOValStatus.add(valStatus);

			}
		}



		// Task Repositories Validation
		if(taskRepositoriesList!=null && taskRepositoriesList.size()>0){
			errorLogList=validationForTaskRepositories(taskRepositoriesList, effectiveDate);
			if(errorLogList!=null && errorLogList.size()>0){
				masterErrorMsgList.addAll(errorLogList);
				valStatus=new JSONObject();
				valStatus.put("entityName","Task Repositories");
				valStatus.put("noOfSuccess", UploaderHelper.getActualTaskRepositoriesDTOCount(taskRepositoriesList)-UploaderHelper.getActualErrorDTOCount(errorLogList));
				valStatus.put("noOfError",UploaderHelper.getActualErrorDTOCount(errorLogList));
				allDTOValStatus.add(valStatus);
			}else{
				valStatus=new JSONObject();
				valStatus.put("entityName","Task Repositories");
				valStatus.put("noOfSuccess", UploaderHelper.getActualTaskRepositoriesDTOCount(taskRepositoriesList));
				valStatus.put("noOfError",0);
				allDTOValStatus.add(valStatus);

			}
		}


		// Task Master Validation
		if(taskMasterList!=null && taskMasterList.size()>0){
			errorLogList=validationForTaskMaster(clientCode,taskMasterList, flowTypesList,enitityMasterUploaderlist,entityownerUploadList,taskEntityDetailList,taskRepositoriesList,effectiveDate);
			if(errorLogList!=null && errorLogList.size()>0){
				masterErrorMsgList.addAll(errorLogList);
				valStatus=new JSONObject();
				valStatus.put("entityName","Task Master");
				valStatus.put("noOfSuccess", UploaderHelper.getActualTaskMasterDTOCount(taskMasterList)-UploaderHelper.getActualErrorDTOCount(errorLogList));
				valStatus.put("noOfError",UploaderHelper.getActualErrorDTOCount(errorLogList));
				allDTOValStatus.add(valStatus);
			}else{
				valStatus=new JSONObject();
				valStatus.put("entityName","Task Master");
				valStatus.put("noOfSuccess",UploaderHelper.getActualTaskMasterDTOCount(taskMasterList));
				valStatus.put("noOfError",0);
				allDTOValStatus.add(valStatus);

			}
		}


		// Task Entity Details Validation
		if(taskEntityDetailList!=null && taskEntityDetailList.size()>0){
			errorLogList=validationForTaskEntityDetail(clientCode,taskEntityDetailList, enitityMasterUploaderlist,entityownerUploadList,effectiveDate,taskRepositoriesList,taskMasterList);
			if(errorLogList!=null && errorLogList.size()>0){
				masterErrorMsgList.addAll(errorLogList);
				valStatus=new JSONObject();
				valStatus.put("entityName","Task Entity Detail");
				valStatus.put("noOfSuccess", UploaderHelper.getActualTaskEntityDetailDTOCount(taskEntityDetailList)-UploaderHelper.getActualErrorDTOCount(errorLogList));
				valStatus.put("noOfError",UploaderHelper.getActualErrorDTOCount(errorLogList));
				allDTOValStatus.add(valStatus);
			}else{
				valStatus=new JSONObject();
				valStatus.put("entityName","Task Entity Detail");
				valStatus.put("noOfSuccess",UploaderHelper.getActualTaskEntityDetailDTOCount(taskEntityDetailList));
				valStatus.put("noOfError",0);
				allDTOValStatus.add(valStatus);

			}
		}





		objectList.add(masterErrorMsgList);
		objectList.add(allDTOValStatus);

		return objectList;

	}

	
	private List<ErrorLogForUploader> validationForTaskEntityDetail(String clientCode,
			List<TaskEntityDetailUploaderDTO> taskEntityDetailList,
			List<EntityMasterUploaderDTO> enitityMasterUploaderlist, List<EntityOwnerUploaderDTO> entityownerUploadList,
			String effectiveDate,List<TaskRepositoriesUploaderDTO> taskRepositoriesList,List<TaskMasterUploaderDTO> taskMasterList) {


		LOGGER.info("EXEFLOW-DldBoImpl -- > validationForTaskEntityDetail");
		Boolean isNullCheckFailed;
		ErrorLogForUploader errorLogObj ;
		List<ErrorLogForUploader> errorLogList = new ArrayList<ErrorLogForUploader>();
		List<String> ownerList=new ArrayList<String>();
		List<String> entityMasterList=new ArrayList<String>();
		List<String> entityList=new ArrayList<String>();
		Map<String,String> entityOwnerMap=null;
		Boolean sourceEntityOwnerFound=false;
		Boolean sourceEntityOwnerCombfound=false;
		List<Map<String,String>> entityOwnerMapList=new ArrayList<Map<String,String>>();
		Set<String> repoNameSet=new HashSet<String>();
		Set<String> taskNameSet=new HashSet<String>();
		Set<DlTaskSourceTarget> OverAllTaskSourceSet=new HashSet<DlTaskSourceTarget>();
		DlTaskSourceTarget taskSourceTarget=null;
		List<DlTaskSourceTarget> uniqueTS=new ArrayList<DlTaskSourceTarget>();

		try{
			for(EntityOwnerUploaderDTO entityownerDTO:entityownerUploadList){
				ownerList.add(entityownerDTO.getOwner_Name().toLowerCase());

			}

			for(TaskRepositoriesUploaderDTO taskRepositoriesUploaderDTO:taskRepositoriesList){
				repoNameSet.add(taskRepositoriesUploaderDTO.getName());
			}

			for(TaskMasterUploaderDTO taskMasterUploaderDTO:taskMasterList){
				taskNameSet.add(taskMasterUploaderDTO.getTask_Name()+taskMasterUploaderDTO.getTask_Repository());

			}


			for(EntityMasterUploaderDTO entitymasterDTO:	enitityMasterUploaderlist){
				entityMasterList.add(entitymasterDTO.getEntity_Name().toLowerCase());
				entityOwnerMap=new HashMap<String,String>();
				entityOwnerMap.put(entitymasterDTO.getEntity_Name().toLowerCase(), entitymasterDTO.getOwner_Name().toLowerCase());
				entityOwnerMapList.add(entityOwnerMap);
			}


			for(TaskEntityDetailUploaderDTO taskEntityDetailUploaderDTO :taskEntityDetailList) {
				LOGGER.info("EXEFLOW - Validating Metadata for TaskEntityDetail:"+taskEntityDetailUploaderDTO.getTaskName());
				isNullCheckFailed=false;
				if(taskEntityDetailUploaderDTO.getTaskName()==null){
					isNullCheckFailed=true;
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Task Entity Detail");
					errorLogObj.setEntityName("");
					errorLogObj.setErrorMsg("Task Name is Blank");
					errorLogList.add(errorLogObj);

				}
				if(taskEntityDetailUploaderDTO.getTaskRepository()==null){
					isNullCheckFailed=true;
					if(taskEntityDetailUploaderDTO.getTaskName()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Task Repository is Blank");
						errorLogList.add(errorLogObj);
					}
				}


				if(taskEntityDetailUploaderDTO.getEntityOwnerName()==null){
					isNullCheckFailed=true;
					if(taskEntityDetailUploaderDTO.getTaskName()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Task Entity Owner Name is Blank");
						errorLogList.add(errorLogObj);
					}
				}

				if(taskEntityDetailUploaderDTO.getEntityName()==null){
					isNullCheckFailed=true;
					if(taskEntityDetailUploaderDTO.getTaskName()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Task Entity Name is Blank");
						errorLogList.add(errorLogObj);
					}

				}

				if(taskEntityDetailUploaderDTO.getLinkType()==null){
					isNullCheckFailed=true;
					if(taskEntityDetailUploaderDTO.getTaskName()!=null){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Task Link Type is Blank");
						errorLogList.add(errorLogObj);
					}
				}

				if(!isNullCheckFailed){
					if(taskEntityDetailUploaderDTO.getTaskRepository().length()>100){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Length of Task Repository cannot exceed more than 100 characters");
						errorLogList.add(errorLogObj);

					}


					entityList = Arrays.asList(taskEntityDetailUploaderDTO.getEntityName().toLowerCase().split("\\s*,\\s*"));
					if(entityList!=null && entityList.size()>0){
						for(String entityName:entityList){
							if(entityName.length()>1000){
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("Task Entity Detail");
								errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
								errorLogObj.setErrorMsg("Length of Entity Name cannot exceed more than 1000 characters");
								errorLogList.add(errorLogObj);

							}
						}
					}


					if(taskEntityDetailUploaderDTO.getEntityOwnerName().length()>100){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Length of Owner Name cannot exceed more than 100 characters");
						errorLogList.add(errorLogObj);

					}
  

					if(taskEntityDetailUploaderDTO.getLinkType().length()>1){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Length of Link Type cannot exceed more than 1 characters");
						errorLogList.add(errorLogObj);

					}

					if(!(taskEntityDetailUploaderDTO.getLinkType().equalsIgnoreCase("S")) && !(taskEntityDetailUploaderDTO.getLinkType().equalsIgnoreCase("T"))){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Values of Link Type can be only(S/T)");
						errorLogList.add(errorLogObj);

					}



					//Source Entity name Validation
					//sourceEntityList=Arrays.asList(taskMasterDTO.getSource_entity_Name().split("\\s*,\\s*"));
					if(!entityMasterList.containsAll(entityList)){
						sourceEntityOwnerFound=true;
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Entity Name Not Found in Entity Master Sheet");
						errorLogList.add(errorLogObj);

					}

					if(!repoNameSet.contains(taskEntityDetailUploaderDTO.getTaskRepository())){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Task Repository("+taskEntityDetailUploaderDTO.getTaskRepository()+") Not Found in Task Repository Sheet");
						errorLogList.add(errorLogObj);

					}



					if(!taskNameSet.contains(taskEntityDetailUploaderDTO.getTaskName()+taskEntityDetailUploaderDTO.getTaskRepository())){
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Combination of Task Name("+taskEntityDetailUploaderDTO.getTaskName()+") and Task Repository("+taskEntityDetailUploaderDTO.getTaskRepository()+") Not Found in Task Master Sheet");
						errorLogList.add(errorLogObj);

					}



					//Source Owner name Validation
					if(!ownerList.contains(taskEntityDetailUploaderDTO.getEntityOwnerName().toLowerCase())){
						sourceEntityOwnerFound=true;
						errorLogObj = new ErrorLogForUploader();
						errorLogObj.setEntityType("Task Entity Detail");
						errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
						errorLogObj.setErrorMsg("Entity Owner Name("+taskEntityDetailUploaderDTO.getEntityOwnerName()+") Not Found in Entity Owner Sheet");
						errorLogList.add(errorLogObj);

					}

					//Source Owner name and Entity name combination Validation
					sourceEntityOwnerCombfound=false;
					if(!sourceEntityOwnerFound){
						for(String ent:entityList){

							for(Map<String,String> eOwnerMap:entityOwnerMapList)
							{
								if(eOwnerMap.get(ent)!=null){

									if(eOwnerMap.get(ent).equalsIgnoreCase(taskEntityDetailUploaderDTO.getEntityOwnerName())){
										sourceEntityOwnerCombfound=true;
										break;

									}
								}
							}

							if(sourceEntityOwnerCombfound==false)	
							{
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("Task Entity Detail");
								errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
								errorLogObj.setErrorMsg("Entity Name("+ent+") and Owner Name("+taskEntityDetailUploaderDTO.getEntityOwnerName()+") combination is not present in entity master sheet");
								errorLogList.add(errorLogObj);

							}
							sourceEntityOwnerCombfound=false;
						}
					}
					
					
					
					
					if(entityList!=null && entityList.size()>0){
						for(String sourceName:entityList){
							uniqueTS  = OverAllTaskSourceSet.stream()               
									.filter(line ->line.getTaskname().equals(taskEntityDetailUploaderDTO.getTaskName())
											&& line.getTaskRepository().equals(taskEntityDetailUploaderDTO.getTaskRepository()) 
											&& line.getEntityName().equals(sourceName) 
											&& line.getOwnerName().equals(taskEntityDetailUploaderDTO.getEntityOwnerName()) 
											&& line.getLinkType().equals(taskEntityDetailUploaderDTO.getLinkType()))    
									.collect(Collectors.toList());
							
							
							
							
							
							if(uniqueTS.size()>0){
								
								errorLogObj = new ErrorLogForUploader();
								errorLogObj.setEntityType("Task Entity Detail");
								errorLogObj.setEntityName(taskEntityDetailUploaderDTO.getTaskName());
								errorLogObj.setErrorMsg("Duplicate Entry found for the combination(Task Repository("+taskEntityDetailUploaderDTO.getTaskRepository()+"),Task Name("+taskEntityDetailUploaderDTO.getTaskName()+"),Entity Owner Name("+taskEntityDetailUploaderDTO.getEntityOwnerName()+"),Entity Name("+sourceName+") and Link Type)");
								errorLogList.add(errorLogObj);
							}
							else
								
							taskSourceTarget=new DlTaskSourceTarget();
							taskSourceTarget.setTaskname(taskEntityDetailUploaderDTO.getTaskName());
							taskSourceTarget.setTaskRepository(taskEntityDetailUploaderDTO.getTaskRepository());
							taskSourceTarget.setEntityName(sourceName);
							taskSourceTarget.setOwnerName(taskEntityDetailUploaderDTO.getEntityOwnerName());
							taskSourceTarget.setLinkType(taskEntityDetailUploaderDTO.getLinkType());	
							OverAllTaskSourceSet.add(taskSourceTarget);
						}
					}

				}
			}
			/*
			 * Validation for source target cyclic dependency
			 */
			List<ErrorLogForUploader> cyclicError = UploaderHelper.checkCyclicDependency(taskEntityDetailList, taskMasterList);
			errorLogList.addAll(cyclicError);

		}
		catch (Exception e) {

			LOGGER.error("Error occured while validation of Task Enitity Detail ", e);
		}
		List<ErrorLogForUploader> errorLogListForDistinctValues;
		List<ErrorLogForUploader> errorLogFinal = new ArrayList<ErrorLogForUploader>();
		for(ErrorLogForUploader errorLogForUploader: errorLogList)
		{

			errorLogListForDistinctValues  = errorLogList.stream()               
					.filter(line -> errorLogForUploader.getEntityName().equals(line.getEntityName()) && errorLogForUploader.getErrorMsg().equals(line.getErrorMsg()) )    
					.collect(Collectors.toList());
			if(!errorLogFinal.contains(errorLogListForDistinctValues.get(0)))
				errorLogFinal.add(errorLogListForDistinctValues.get(0));
		}
		return errorLogFinal;
	}




	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getSourceSystemsStagingSummmary(String currrentBusinessDate, 
			String isFlowFilterApplied,	String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String tabIndicator,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getStagingDetailsForSourceSystems");
		JSONObject finalResponse = new JSONObject();
		try{

			DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
			Map<String,Integer> statsMap = null;
			Map<String,HashMap<String,Integer>> sourceSystemDetails = new HashMap<String, HashMap<String,Integer>>();
			Map<String,HashMap<String,Integer>> sourceEntityDetails = new HashMap<String, HashMap<String,Integer>>();
			Map<String,HashMap<String,String>> sourceExtraDetails = new HashMap<String, HashMap<String,String>>();
			Map<String,HashMap<String,String>> entityExtraDetails = new HashMap<String, HashMap<String,String>>();
			Map<String,Set<String>> srcEntityList = new HashMap<String, Set<String>>();
			Map<String,Set<String>> srcTaskList = new HashMap<String, Set<String>>();
			Map<String,Set<String>> srcETaskList = new HashMap<String, Set<String>>();
			Map<String,Set<String>> entityTaskList = new HashMap<String, Set<String>>();
			DateTime sysDate=new DateTime();
			JSONArray dataArray = new JSONArray();
			//getting applicable frequencies and generating csv for in clause if filter is not applied.
			if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
				freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
				frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
			}
			//get staging details.
			totalStagingObject = dldDao.getSourceSystemSummaryForGrid(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, SOURCE_SYSTEM,clientCode);
			
			Map<String,String> taskOwnerStatus = new HashMap<String, String>();
			//processing data for src system and src entites.
			if(!SOURCE_TASK_TAB.equalsIgnoreCase(tabIndicator)){
				//processing staging results
				for(StagingDetails tsk :totalStagingObject){
					
					//general status check for src system.
					if(null==srcEntityList.get(tsk.getOwnerName())){
						Set<String> entityNames = new HashSet<String>();
						entityNames.add(tsk.getEntityName()+tsk.getOwnerName());
						srcEntityList.put(tsk.getOwnerName(), entityNames);
					}else {
						srcEntityList.get(tsk.getOwnerName()).add(tsk.getEntityName()+tsk.getOwnerName());
					}
					
					
					if(null==srcTaskList.get(tsk.getOwnerName())){
						Set<String> totalUniqueTaskSS = new HashSet<String>();
						totalUniqueTaskSS.add(tsk.getTaskName()+tsk.getOwnerName());
						srcTaskList.put(tsk.getOwnerName(), totalUniqueTaskSS);
					}else{
						srcTaskList.get(tsk.getOwnerName()).add(tsk.getTaskName()+tsk.getOwnerName());
					}
					
					if(null==entityTaskList.get(tsk.getEntityName()+ tsk.getOwnerName())){
						Set<String> uniqueTaskForEntity = new HashSet<String>();
						uniqueTaskForEntity.add(tsk.getTaskName()+tsk.getOwnerName());
						entityTaskList.put(tsk.getEntityName()+ tsk.getOwnerName(), uniqueTaskForEntity);
					}else{
						entityTaskList.get(tsk.getEntityName()+ tsk.getOwnerName()).add(tsk.getTaskName()+tsk.getOwnerName());
					}
					
					
					if(null==taskOwnerStatus.get(tsk.getTaskName()+tsk.getOwnerName())){
						taskOwnerStatus.put(tsk.getTaskName()+tsk.getOwnerName(), tsk.getTaskStatus()==null?"":tsk.getTaskStatus());
					}
					
					
					
					
					//check for src system entry
					if(sourceSystemDetails.get(tsk.getOwnerName())==null){
						//creating map of src and entity relation.

						
						statsMap = new HashMap<String, Integer>();
						statsMap.put("completed",0 );
						statsMap.put("notDueYet",0 );
						statsMap.put("failed",0 );
						statsMap.put("overDue",0 );
						sourceSystemDetails.put(tsk.getOwnerName(), (HashMap<String, Integer>) statsMap);
						//creating map of extra details.
						Map<String, String> extraDet = new HashMap<String, String>();

						//required common.
						extraDet.put("sourceDesc", tsk.getOwnerDesc());
						extraDet.put("sourceOwner",tsk.getOwnerName());
						extraDet.put("entityDesc", tsk.getEntityDesc());
						extraDet.put("entityType", tsk.getEntityType());
						extraDet.put("ContactDetails", tsk.getOwnerContactDetails());
						sourceExtraDetails.put(tsk.getOwnerName(),(HashMap<String, String>) extraDet);

					}
					if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
						//not yet started 
						sourceSystemDetails.get(tsk.getOwnerName()).put("notDueYet",
								(sourceSystemDetails.get(tsk.getOwnerName()).get("notDueYet")+1));
					}
					else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
						//check for over due failed success.
						if(null ==tsk.getTaskStatus()){
							//overDueTask
							sourceSystemDetails.get(tsk.getOwnerName()).put("overDue",
									(sourceSystemDetails.get(tsk.getOwnerName()).get("overDue")+1));
						}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//failed tasks.
							sourceSystemDetails.get(tsk.getOwnerName()).put("failed",
									(sourceSystemDetails.get(tsk.getOwnerName()).get("failed")+1));
						}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks.
							sourceSystemDetails.get(tsk.getOwnerName()).put("completed",
									(sourceSystemDetails.get(tsk.getOwnerName()).get("completed")+1));
						}
					}


					//for source entity details.
					//check for src system entry
					String entityOwner = tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
					if(sourceEntityDetails.get(entityOwner)==null){
						Set<String> totalUniqueTaskSE = new HashSet<String>();
						totalUniqueTaskSE.add(tsk.getTaskName()+tsk.getOwnerName()+tsk.getEntityName());
						srcETaskList.put(tsk.getEntityName()+tsk.getOwnerName(), totalUniqueTaskSE);
						statsMap = new HashMap<String, Integer>();
						statsMap.put("completed",0 );
						statsMap.put("notDueYet",0 );
						statsMap.put("failed",0 );
						statsMap.put("overDue",0 );
						sourceEntityDetails.put(entityOwner, (HashMap<String, Integer>) statsMap);
						//creating map of extra details.
						Map<String, String> extraDet = new HashMap<String, String>();

						//required common.
						extraDet.put("sourceDesc", tsk.getOwnerDesc());
						extraDet.put("sourceOwner",tsk.getOwnerName());
						extraDet.put("entityDesc", tsk.getEntityDesc());
						extraDet.put("entityType", tsk.getEntityType());
						extraDet.put("ContactDetails", tsk.getOwnerContactDetails());
						entityExtraDetails.put(entityOwner,(HashMap<String, String>) extraDet);
					}
					if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
						//not yet started 
						sourceEntityDetails.get(entityOwner).put("notDueYet",
								(sourceEntityDetails.get(entityOwner).get("notDueYet")+1));
					}
					else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
						//check for over due failed success.
						if(null ==tsk.getTaskStatus()){
							//overDueTask
							sourceEntityDetails.get(entityOwner).put("overDue",
									(sourceEntityDetails.get(entityOwner).get("overDue")+1));
						}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//failed tasks.
							sourceEntityDetails.get(entityOwner).put("failed",
									(sourceEntityDetails.get(entityOwner).get("failed")+1));
						}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks.
							sourceEntityDetails.get(entityOwner).put("completed",
									(sourceEntityDetails.get(entityOwner).get("completed")+1));
						}
					}

				}
				//json prepare for src system
				if(SOURCE_SYSTEM_TAB.equalsIgnoreCase(tabIndicator)){
					//having details about source system
					JSONObject sourceSystemDetailsJson = new JSONObject();

					//json for sourceSystemDetails
					//process entities map for corresponding source.
					
					for (Map.Entry<String, HashMap<String,Integer>> entry : sourceSystemDetails.entrySet()) {
						Integer completedCount = 0;
						Integer totalEntityCount = 0;
						Integer completedEntityCount = 0;
						Integer completedCountTask = 0;
						String srcName = entry.getKey();
						HashMap<String,Integer> value =  entry.getValue();
						sourceSystemDetailsJson = new JSONObject();
						sourceSystemDetailsJson.put("sourceName", srcName);
						sourceSystemDetailsJson.put("sourceDesc",sourceExtraDetails.get(srcName).get("sourceDesc") );
						sourceSystemDetailsJson.put("ContactDetails",sourceExtraDetails.get(srcName).get("ContactDetails") );
						
						for(String taskName:srcTaskList.get(srcName)){
							if(COMPLETED.equalsIgnoreCase(taskOwnerStatus.get(taskName))){
								completedCountTask++;
							}
						}
						sourceSystemDetailsJson.put("completedTaskCount", completedCountTask);
						sourceSystemDetailsJson.put("totalTaskCount", srcTaskList.get(srcName).size());
						
						if(value.get("completed")>0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")==0){
							sourceSystemDetailsJson.put("status", COMPLETED);
							totalEntityCount++;
						}else if(value.get("completed")==0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")>0){
							sourceSystemDetailsJson.put("status", "PENDING");
							totalEntityCount++;
						}else if(value.get("completed")>0&&value.get("failed")==0&&(value.get("overDue")>0||value.get("notDueYet")>0)){
							sourceSystemDetailsJson.put("status", "PARTIALLY COMPLETED");
							totalEntityCount++;
						} else {
							sourceSystemDetailsJson.put("status", "PENDING");
							totalEntityCount++;
						}
						for(String entityAndSrcName:srcEntityList.get(srcName)){
							for(String taskName:entityTaskList.get(entityAndSrcName)){
								if(COMPLETED.equalsIgnoreCase(taskOwnerStatus.get(taskName))){
									completedCount++;
								}
							}
							if(completedCount.equals(entityTaskList.get(entityAndSrcName).size())){
								completedEntityCount++;
							}
							completedCount=0;
							
						}
						sourceSystemDetailsJson.put("entityCompletedCount",completedEntityCount );
						sourceSystemDetailsJson.put("entityTotalCount",srcEntityList.get(srcName).size());
						
						/*for(String entityNamesFromSrc :srcEntityList.get(srcName)){
							String entityAndSrc = entityNamesFromSrc+SEPARATOR+srcName;
							completedCount = completedCount + sourceEntityDetails.get(entityAndSrc).get("completed");
							totalEntityCount = totalEntityCount+sourceEntityDetails.get(entityAndSrc).get("completed")+
									sourceEntityDetails.get(entityAndSrc).get("overDue")+sourceEntityDetails.get(entityAndSrc).get("failed")
									+sourceEntityDetails.get(entityAndSrc).get("notDueYet");
						}*/
						
						dataArray.add(sourceSystemDetailsJson);
					}

					finalResponse.put("data", dataArray);

				}else if(SOURCE_ENTITY_TAB.equalsIgnoreCase(tabIndicator)){
					//json prepare for src enttiy
					//having details about source system
					JSONObject sourceEntityDetailsJson = new JSONObject();

					//json for sourceSystemDetails
					for (Map.Entry<String, HashMap<String,Integer>> entry : sourceEntityDetails.entrySet()) {
						Integer completedCountTask = 0;
						String entityName = entry.getKey();
						HashMap<String,Integer> value =  entry.getValue();
						sourceEntityDetailsJson = new JSONObject();
						sourceEntityDetailsJson.put("entityName", entityName.split(SEPARATOR)[0]);
						sourceEntityDetailsJson.put("entityDesc",entityExtraDetails.get(entityName).get("entityDesc") );
						sourceEntityDetailsJson.put("entityType",entityExtraDetails.get(entityName).get("entityType") );
						sourceEntityDetailsJson.put("ContactDetails",entityExtraDetails.get(entityName).get("ContactDetails") );
						for(String taskName:entityTaskList.get(entityName.split(SEPARATOR)[0]+entityExtraDetails.get(entityName).get("sourceOwner"))){
							if(COMPLETED.equalsIgnoreCase(taskOwnerStatus.get(taskName))){
								completedCountTask++;
							}
						}
						sourceEntityDetailsJson.put("completedTaskCount", completedCountTask);
						sourceEntityDetailsJson.put("totalTaskCount", entityTaskList.get(entityName.split(SEPARATOR)[0]+entityExtraDetails.get(entityName).get("sourceOwner")).size());
						sourceEntityDetailsJson.put("sourceOwner",entityExtraDetails.get(entityName).get("sourceOwner") );
						if(value.get("completed")>0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")==0){
							sourceEntityDetailsJson.put("status", COMPLETED);
						}else if(value.get("completed")==0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")>0){
							sourceEntityDetailsJson.put("status", "PENDING");
						}else if(value.get("completed")>0&&value.get("failed")==0&&(value.get("overDue")>0||value.get("notDueYet")>0)){
							sourceEntityDetailsJson.put("status", "PARTIALLY COMPLETED");
						} else {
							sourceEntityDetailsJson.put("status", "PENDING");
						}

						dataArray.add(sourceEntityDetailsJson);
					}

					finalResponse.put("data", dataArray);

				}

			}else{
				//for task details data processing and json.
				Map<String,Map<String,String>> taskDetails = new HashMap<String,Map<String,String>>();
				for(StagingDetails tsk : totalStagingObject){
					Map<String,String> statsTask = new HashMap<String, String>();
					if(null==taskDetails.get(tsk.getTaskName())){
						statsTask.put("taskName", tsk.getTaskName());
						statsTask.put("taskDesc", tsk.getTaskDesc());
						statsTask.put("sourceEntity", tsk.getSourceEntityName());
						statsTask.put("flowType", tsk.getFlowType());
						statsTask.put("sourceOwner", tsk.getOwnerName());
						statsTask.put("flowTypeCount","1");
						statsTask.put("dueDate", businessDateRec.plusDays(tsk.getOffset()).toLocalDate().toString());
						if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							statsTask.put("completionDate", tsk.getRunDate().toString());
						}else{
							statsTask.put("completionDate", "");
						}
						//initializing source task details
						if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
							//not yet started 
							statsTask.put("status","NOT STARTED");
						}
						else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
							//check for over due failed success.
							if(null ==tsk.getTaskStatus()){
								//overDueTask
								statsTask.put("status","OVERDUE");
							}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
								//failed tasks.
								statsTask.put("status","FAILED");
							}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
								//completed tasks.
								statsTask.put("status",COMPLETED);
							}
						}
						statsTask.put("runCount", tsk.getRunCount()+"");
						taskDetails.put(tsk.getTaskName(), statsTask);
					}else{
						if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())&&"".equalsIgnoreCase(taskDetails.get(tsk.getTaskName()).get("completionDate"))){
							statsTask.put("completionDate", tsk.getRunDate().toString());
						}else{
							statsTask.put("completionDate", "");
						}
						taskDetails.get(tsk.getTaskName()).put("sourceEntity",
								taskDetails.get(tsk.getTaskName()).get("sourceEntity")+","+tsk.getSourceEntityName());
						taskDetails.get(tsk.getTaskName()).put("flowTypeCount",(Integer.parseInt(
								taskDetails.get(tsk.getTaskName()).get("flowTypeCount"))+1)+"");
					}
				}

				for (Map.Entry<String, Map<String,String>> entry : taskDetails.entrySet()) {
					JSONObject dataObj = new JSONObject();
					Map<String,String> value =  entry.getValue();
					dataObj.put("status", value.get("status"));
					dataObj.put("runCount", value.get("runCount"));
					dataObj.put("sourceEntity",Joiner.on(",").join(new HashSet<String>(Arrays.asList(value.get("sourceEntity").split(",")))));// value.get("sourceEntity"));
					dataObj.put("taskName", value.get("taskName"));
					dataObj.put("taskDesc", value.get("taskDesc"));
					dataObj.put("flowType", value.get("flowType"));
					dataObj.put("sourceOwner", value.get("sourceOwner"));
					dataObj.put("dueDate", value.get("dueDate"));
					dataObj.put("completionDate", value.get("completionDate"));
					dataArray.add(dataObj);

				}
				finalResponse.put("data", dataArray);
			}

		}catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}
		return finalResponse;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getStagingSummaryForDataRepositoryGrid(
			String currrentBusinessDate, String isFlowFilterApplied,
			String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String tabIndicator,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getStagingSummaryForDataRepositoryGrid");
		JSONObject finalResponse = new JSONObject();
		try{

			DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
			Map<String,Integer> statsMap = null;
			Map<String,HashMap<String,String>> entityExtraDetails = new HashMap<String, HashMap<String,String>>();
			Map<String,HashMap<String,Integer>> repoEntityDetails = new HashMap<String, HashMap<String,Integer>>();
			DateTime sysDate=new DateTime();
			Map<String,String> taskOwnerStatus = new HashMap<String, String>();
			HashMap<String,Set<String>> srcETaskList = new HashMap<String, Set<String>>();
			JSONArray dataArray = new JSONArray();
			//getting applicable frequencies and generating csv for in clause if filter is not applied.
			if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
				freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
				frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
			}
			//get staging details.
			totalStagingObject = dldDao.getSourceSystemSummaryForGrid(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, DATA_REPOSITORY,clientCode);
			if(DATA_REPO_TASK.equalsIgnoreCase(tabIndicator)){
				//for task details data processing and json.
				Map<String,Map<String,String>> taskDetails = new HashMap<String,Map<String,String>>();
				for(StagingDetails tsk : totalStagingObject){
					Map<String,String> statsTask = new HashMap<String, String>();
					if(null==taskDetails.get(tsk.getTaskName())){
						statsTask.put("taskName", tsk.getTaskName());
						statsTask.put("taskTechnicalName", tsk.getTaskTechnicalName());
						statsTask.put("taskDesc", tsk.getTaskDesc());
						statsTask.put("sourceEntity", tsk.getSourceEntityName());
						statsTask.put("targetEntity", tsk.getTargetEntityName());
						statsTask.put("flowType", tsk.getFlowType());
						statsTask.put("flowTypeCount","1");
						statsTask.put("frequencyType", tsk.getFrequency());
						statsTask.put("sourceOwner", tsk.getOwnerName());
						statsTask.put("dataRepo", tsk.getTaskRepository());
						statsTask.put("taskType",tsk.getTaskType());
						statsTask.put("entitySource", tsk.getEntityOwner());
						statsTask.put("dueDate", businessDateRec.plusDays(tsk.getOffset()).toLocalDate().toString());
						if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							statsTask.put("completionDate", tsk.getRunDate().toString());
							statsTask.put("loadStartDate", null==tsk.getStartDateTime()?"":tsk.getStartDateTime().toString());
							statsTask.put("loadEndDate", null==tsk.getEndDateTime()?"":tsk.getEndDateTime().toString());
							statsTask.put("runDetails", null==tsk.getRunDetails()?"":tsk.getRunDetails());

						}else if (FAILED.equalsIgnoreCase(tsk.getTaskStatus())){
							statsTask.put("completionDate", tsk.getRunDate().toString());
							statsTask.put("loadStartDate", null==tsk.getStartDateTime()?"":tsk.getStartDateTime().toString());
							statsTask.put("loadEndDate", null==tsk.getEndDateTime()?"":tsk.getEndDateTime().toString());
							statsTask.put("runDetails", null==tsk.getRunDetails()?"":tsk.getRunDetails());

						}else{
							statsTask.put("completionDate", "");
							statsTask.put("loadStartDate", "");
							statsTask.put("loadEndDate", "");
							statsTask.put("runDetails", "");

						}
						//initializing data repo task details
						if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
							//not yet started 
							statsTask.put("status","NOT DUE YET");
						}
						else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<0 ){	
							//check for over due failed success.
							if(null ==tsk.getTaskStatus()){
								//overDueTask
								statsTask.put("status","OVERDUE");
							}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
								//failed tasks.
								statsTask.put("status","FAILED");
							}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
								//completed tasks.
								statsTask.put("status","COMPLETED");
							}
						}else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())==0 ){
							if(null ==tsk.getTaskStatus()){
								//pending due today.
								statsTask.put("status","PENDING DUE TODAY");
							}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
								//completed tasks on the current day.
								statsTask.put("status","COMPLETED");
							}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
								//completed tasks on the current day.
								statsTask.put("status","FAILED");
							}
						}

						statsTask.put("runCount", tsk.getRunCount()+"");
						taskDetails.put(tsk.getTaskName(), statsTask);
					}else{
						if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())&&"".equalsIgnoreCase(taskDetails.get(tsk.getTaskName()).get("completionDate"))){
							statsTask.put("completionDate", tsk.getRunDate().toString());
							statsTask.put("loadStartDate", tsk.getStartDateTime().toString());
							statsTask.put("loadEndDate", tsk.getEndDateTime().toString());
							statsTask.put("runDetails", null==tsk.getRunDetails()?"":tsk.getRunDetails());
						}else{
							statsTask.put("completionDate", "");
							statsTask.put("loadStartDate", "");
							statsTask.put("loadEndDate", "");
						}
						taskDetails.get(tsk.getTaskName()).put("sourceEntity",
								taskDetails.get(tsk.getTaskName()).get("sourceEntity")+","+tsk.getSourceEntityName());
						taskDetails.get(tsk.getTaskName()).put("targetEntity",
								taskDetails.get(tsk.getTaskName()).get("targetEntity")+","+tsk.getTargetEntityName());
						taskDetails.get(tsk.getTaskName()).put("frequencyType",taskDetails.get(tsk.getTaskName()).get("frequencyType")+","+tsk.getFrequency());
						taskDetails.get(tsk.getTaskName()).put("flowTypeCount",(Integer.parseInt(
								taskDetails.get(tsk.getTaskName()).get("flowTypeCount"))+1)+"");
					}
				}

				for (Map.Entry<String, Map<String,String>> entry : taskDetails.entrySet()) {
					JSONObject dataObj = new JSONObject();
					Map<String,String> value =  entry.getValue();
					dataObj.put("runDetails", null==value.get("runDetails")?"":value.get("runDetails"));
					dataObj.put("frequencyType", Joiner.on(",").join(new HashSet<String>(Arrays.asList(value.get("frequencyType").split(",")))));
					dataObj.put("loadStartDate", value.get("loadStartDate"));
					dataObj.put("loadEndDate", value.get("loadEndDate"));
					dataObj.put("status", value.get("status"));
					dataObj.put("runCount", value.get("runCount"));
					dataObj.put("sourceEntity",Joiner.on(",").join(new HashSet<String>(Arrays.asList(value.get("sourceEntity").split(",")))) );
					dataObj.put("targetEntity", Joiner.on(",").join(new HashSet<String>(Arrays.asList(value.get("targetEntity").split(",")))));
					dataObj.put("taskName", value.get("taskName"));
					dataObj.put("taskTechnicalName", value.get("taskTechnicalName"));
					dataObj.put("taskDesc", value.get("taskDesc"));
					dataObj.put("taskType", value.get("taskType"));
					dataObj.put("source", value.get("entitySource"));
					dataObj.put("flowType", value.get("flowType"));
					dataObj.put("sourceOwner", value.get("sourceOwner"));
					dataObj.put("dataRepo", value.get("dataRepo"));
					dataObj.put("dueDate", value.get("dueDate"));
					dataObj.put("completionDate", value.get("completionDate"));
					dataArray.add(dataObj);

				}
				finalResponse.put("data", dataArray);

			}else if(DATA_REPO_ENTITY.equalsIgnoreCase(tabIndicator)){
				//for source entity details.
				//check for src system entry
				
				for(StagingDetails tsk : totalStagingObject){
					String entityOwner = tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
					if(null==taskOwnerStatus.get(tsk.getTaskName()+tsk.getOwnerName())){
						taskOwnerStatus.put(tsk.getTaskName()+tsk.getOwnerName(), tsk.getTaskStatus()==null?"":tsk.getTaskStatus());
					}
					if(null==srcETaskList.get(tsk.getEntityName()+tsk.getOwnerName())){
						Set<String> totalUniqueTaskSE = new HashSet<String>();
						totalUniqueTaskSE.add(tsk.getTaskName()+tsk.getOwnerName());
						srcETaskList.put(tsk.getEntityName()+tsk.getOwnerName(), totalUniqueTaskSE);
					}else{
						srcETaskList.get(tsk.getEntityName()+tsk.getOwnerName()).add(tsk.getTaskName()+tsk.getOwnerName());
					}
					
					if(repoEntityDetails.get(entityOwner)==null){
						statsMap = new HashMap<String, Integer>();
						statsMap.put("completed",0 );
						statsMap.put("notDueYet",0 );
						statsMap.put("failed",0 );
						statsMap.put("overDue",0 );
						repoEntityDetails.put(entityOwner, (HashMap<String, Integer>) statsMap);
						//creating map of extra details.
						Map<String, String> extraDet = new HashMap<String, String>();

						//required common.
						extraDet.put("sourceDesc", tsk.getOwnerDesc());
						extraDet.put("entityDesc", tsk.getEntityDesc());
						extraDet.put("entityType", tsk.getEntityType());
						extraDet.put("entityDataRepo", tsk.getOwnerName());
						extraDet.put("entityTechnicalName", tsk.getEntityTechnicalName());
						entityExtraDetails.put(entityOwner,(HashMap<String, String>) extraDet);
					}
					if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
						//not yet started 
						repoEntityDetails.get(entityOwner).put("notDueYet",
								(repoEntityDetails.get(entityOwner).get("notDueYet")+1));
					}
					else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
						//check for over due failed success.
						if(null ==tsk.getTaskStatus()){
							//overDueTask
							repoEntityDetails.get(entityOwner).put("overDue",
									(repoEntityDetails.get(entityOwner).get("overDue")+1));
						}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//failed tasks.
							repoEntityDetails.get(entityOwner).put("failed",
									(repoEntityDetails.get(entityOwner).get("failed")+1));
						}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
							//completed tasks.
							repoEntityDetails.get(entityOwner).put("completed",
									(repoEntityDetails.get(entityOwner).get("completed")+1));
						}
					}

				}
				//json prepare for repo enttiy

				//having details about data repo
				JSONObject repoEntityDetailsJson = new JSONObject();
				//json for sourceSystemDetails
				for (Map.Entry<String, HashMap<String,Integer>> entry : repoEntityDetails.entrySet()) {
					Integer completedCountTask = 0;
					String entityName = entry.getKey();
					HashMap<String,Integer> value =  entry.getValue();
					repoEntityDetailsJson = new JSONObject();
					repoEntityDetailsJson.put("entityName", entityName.split(SEPARATOR)[0]);
					repoEntityDetailsJson.put("entityDesc",entityExtraDetails.get(entityName).get("entityDesc") );
					repoEntityDetailsJson.put("entityType",entityExtraDetails.get(entityName).get("entityType") );
					repoEntityDetailsJson.put("entityTechnicalName",entityExtraDetails.get(entityName).get("entityTechnicalName") );
					repoEntityDetailsJson.put("entityDataRepo",entityExtraDetails.get(entityName).get("entityDataRepo") );
					for(String taskName:srcETaskList.get(entityName.split(SEPARATOR)[0]+entityExtraDetails.get(entityName).get("entityDataRepo"))){
						if(COMPLETED.equalsIgnoreCase(taskOwnerStatus.get(taskName))){
							completedCountTask++;
						}
					}
					repoEntityDetailsJson.put("completedTaskCount", completedCountTask);
					repoEntityDetailsJson.put("totalTaskCount", srcETaskList.get(entityName.split(SEPARATOR)[0]+entityExtraDetails.get(entityName).get("entityDataRepo")).size());
					if(value.get("completed")>0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")==0){
						repoEntityDetailsJson.put("status", COMPLETED);
					}else if(value.get("completed")==0&&value.get("overDue")==0&&value.get("failed")==0&&value.get("notDueYet")>0){
						repoEntityDetailsJson.put("status", "PENDING");
					}else if(value.get("completed")>0&&(value.get("failed")>0||value.get("overDue")>0||value.get("notDueYet")>0)){
						repoEntityDetailsJson.put("status", "PARTIALLY COMPLETED");
					} else {
						repoEntityDetailsJson.put("status", "PENDING");
					}
					dataArray.add(repoEntityDetailsJson);
				}
				finalResponse.put("data", dataArray);
			}


		}catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}
		return finalResponse;
	}




	@Override
	public List<Object> getSheetForEntity(Workbook workbook) {

		LOGGER.info("EXEFLOW-DldBoImpl -- > getSheetForEntity");
		List<EntityOwnerUploaderDTO> entityOwnersList=null;
		List<EntityMasterUploaderDTO> entityMasterList=null;
		List<FlowTypesUploaderDTO> flowTypesList=null;
		List<TaskRepositoriesUploaderDTO> taskRepositoriesList=null;
		List<TaskMasterUploaderDTO> taskMasterList=null;
		List<TaskEntityDetailUploaderDTO> taskEntityDetailsList=null;


		List<Object> objectList=new ArrayList<Object>();

		Worksheet worksheetForPreviousValidation = workbook.getWorksheets().get("Validation Report");
		if(worksheetForPreviousValidation!=null){
			workbook.getWorksheets().removeAt("Validation Report");
		}


		Worksheet worksheetForEntityOwner = workbook.getWorksheets().get(entityOwnerSheetName);
		if(worksheetForEntityOwner!=null){
			entityOwnersList=UploaderHelper.setDTOForEntityOwner(worksheetForEntityOwner);
			objectList.add(entityOwnersList);
		}

		Worksheet worksheetForEntityMaster =workbook.getWorksheets().get(entityMasterSheetName);
		if(worksheetForEntityMaster!=null){
			entityMasterList=UploaderHelper.setDTOForEntityMaster(worksheetForEntityMaster);
			objectList.add(entityMasterList);
		}

		Worksheet worksheetForFlowTypes = workbook.getWorksheets().get(flowTypeSheetName);
		if(worksheetForFlowTypes!=null){
			flowTypesList=UploaderHelper.setDTOForFlowTypes(worksheetForFlowTypes);
			objectList.add(flowTypesList);
		}

		Worksheet worksheetForTaskRepositories = workbook.getWorksheets().get(taskRepositorySheetName);
		if(worksheetForTaskRepositories!=null){
			taskRepositoriesList=UploaderHelper.setDTOForTaskRepositories(worksheetForTaskRepositories);
			objectList.add(taskRepositoriesList);
		}

		Worksheet worksheetForTaskMaster = workbook.getWorksheets().get(taskMasterSheetName);
		if(worksheetForTaskMaster!=null){
			taskMasterList=UploaderHelper.setDTOForTaskMaster(worksheetForTaskMaster);
			objectList.add(taskMasterList);
		}

		Worksheet worksheetForTaskEntityDetails = workbook.getWorksheets().get(taskEntityDetailsSheetName);
		if(worksheetForTaskEntityDetails!=null){
			taskEntityDetailsList=UploaderHelper.setDTOForTaskEntityDetails(worksheetForTaskEntityDetails);
			objectList.add(taskEntityDetailsList);
		}

		return objectList;
	}




	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getUnplannedTaskDetails(String currrentBusinessDate,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getUnplannedTaskDetails");
		DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
		JSONArray dataArray = new JSONArray();
		JSONObject finalObj = new JSONObject();
		try {
			totalStagingObject = dldDao.getUnplannedTaskDetails(businessDateRec,clientCode);
			for(StagingDetails tsk :totalStagingObject){

				JSONObject dataObj = new JSONObject();
				dataObj.put("taskName", tsk.getTaskName());
				dataObj.put("taskDesc", tsk.getTaskDesc());
				dataObj.put("flowType", tsk.getFlowType());
				if(tsk.getStartDateTime()!=null)
					dataObj.put("taskStartDate", tsk.getStartDateTime().toString());
				else
					dataObj.put("taskStartDate", "");
				if(tsk.getEndDateTime()!=null)
					dataObj.put("taskEndDate", tsk.getEndDateTime().toString());
				else
					dataObj.put("taskEndDate", "");
				dataObj.put("taskTechnicalName", tsk.getTaskTechnicalName());
				dataObj.put("runDetails", tsk.getRunDetails()==null?"":tsk.getRunDetails());
				dataObj.put("runCount", tsk.getRunCount()+"");
				dataObj.put("status", tsk.getTaskStatus());
				dataObj.put("repository", tsk.getTaskRepository());
				dataArray.add(dataObj);
			}
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
		finalObj.put("data", dataArray);
		return finalObj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getDetailsForDataConsumersSummary(String currrentBusinessDate,
			String isFrequencyFilterApplied,String frequencyFilterCSV,String isFlowFilterApplied,String flowFilterCSV,String clientCode,String solutionName) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getDetailsForDataConsumersSummary");
		JSONObject dataObj = new JSONObject();
		JSONArray finalResponse = new JSONArray();
		Map<String,Integer> solutions=new HashMap<>();
		Map<String,Integer> allSolutions=new HashMap<>();
		allSolutions=dldDao.getListOfSolutionNames(clientCode);
		if(!solutionName.equalsIgnoreCase("All")){
			for(Map.Entry<String, Integer> dldSolution:allSolutions.entrySet()){
				if(dldSolution.getKey().equalsIgnoreCase(solutionName)){
					solutions.put(dldSolution.getKey()	, dldSolution.getValue());
				}
			}
		}
		else
			solutions=allSolutions;
		DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		Map<String,Set<String>> reportIdEntityNameMap = new HashMap<String,Set<String>>() ;
		Set<String> freqApplicableForcbd=new HashSet<String>();
		List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
		Map<String,Integer> statsMap = null;
		Map<String,HashMap<String,Integer>> reportEntityStats = new HashMap<String, HashMap<String,Integer>>();
		Map<String,HashMap<String,Integer>> sourceEntityDetails = new HashMap<String, HashMap<String,Integer>>();
		DateTime sysDate=new DateTime();
		Map<String,Integer> taskDueMap = new HashMap<String, Integer>();
		Map<String,Integer> taskCompletionMap = new HashMap<String, Integer>();
		Map<String,Set<Integer>> reportDueMap = new HashMap<String, Set<Integer>>();
		Map<String,Set<Integer>> reportCompletionMap = new HashMap<String, Set<Integer>>();
		DateTimeFormatter formatter = DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT);
		//getting applicable frequencies and generating csv for in clause if filter is not applied.
		if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
			freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
			frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
		}

		//get staging details.
		totalStagingObject = dldDao.getStagingDetails(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);


		//processing staging results
		for(StagingDetails tsk :totalStagingObject){


			//for source entity details.
			//check for src system entry
			String 	entitySolutionId= tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
			if(sourceEntityDetails.get(entitySolutionId)==null){
				statsMap = new HashMap<String, Integer>();
				statsMap.put("completed",0 );
				statsMap.put("notDueYet",0 );
				statsMap.put("failed",0 );
				statsMap.put("overDue",0 );
				sourceEntityDetails.put(entitySolutionId, (HashMap<String, Integer>) statsMap);
			}

			//map to fiind max date of those against each entity.
			if(null== taskDueMap.get(entitySolutionId)){
				taskDueMap.put(entitySolutionId,Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset())))));
			}else{
				if(taskDueMap.get(entitySolutionId)<Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset()))))){
					taskDueMap.put(entitySolutionId,Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset())))));
				}
			}
			if(null!=tsk.getRunDate()){
				if(null==taskCompletionMap.get(entitySolutionId)){
					taskCompletionMap.put(entitySolutionId,Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate()))));
				}else{
					if(taskCompletionMap.get(entitySolutionId)<Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate())))){
						taskCompletionMap.put(entitySolutionId,Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate()))));
					}
				}
			}


			if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
				//not yet started 
				sourceEntityDetails.get(entitySolutionId).put("notDueYet",
						(sourceEntityDetails.get(entitySolutionId).get("notDueYet")+1));
			}
			else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
				//check for over due failed success.
				if(null ==tsk.getTaskStatus()){
					//overDueTask
					sourceEntityDetails.get(entitySolutionId).put("overDue",
							(sourceEntityDetails.get(entitySolutionId).get("overDue")+1));
				}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//failed tasks.
					sourceEntityDetails.get(entitySolutionId).put("failed",
							(sourceEntityDetails.get(entitySolutionId).get("failed")+1));
				}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//completed tasks.
					sourceEntityDetails.get(entitySolutionId).put("completed",
							(sourceEntityDetails.get(entitySolutionId).get("completed")+1));
				}
			}

		}
		Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);
		
		
		//processing stats of entity to get report stats.
		Map<String,String> reportLineItemMap = new HashMap<>();
		for(Map.Entry<String, Integer> sol:solutions.entrySet()){
			reportIdEntityNameMap = new HashMap<String,Set<String>>();
			reportEntityStats = new HashMap<String, HashMap<String,Integer>>();
			//TODO use flag for line item while preparing json.
			Map<String,String>reportNameLineItemFlag = dldDao.getReportAndLineItemFlag(sol.getValue(),sol.getKey(),clientCode);
			reportIdEntityNameMap=dldDao.getReportAndEntityMap(businessDateRec,sol.getKey(),sol.getValue(),clientCode);
			for (Map.Entry<String,Set<String>> entry : reportIdEntityNameMap.entrySet()) {
				//looping through map of report and entity name to fetch stats.
				Set<Integer> dueDates = new HashSet<Integer>();
				Set<Integer> completedDates = new HashSet<Integer>();
				Set<String> entityOwnerSet = new HashSet<String>();
				Set<String> entityOwnerSetImmediate = new HashSet<String>();
				for(String eName:entry.getValue()){
					if(Y.equalsIgnoreCase(entityAndTypeMap.get(eName+SEPARATOR+sol.getKey()))){
						entityOwnerSetImmediate.add(eName+SEPARATOR+sol.getKey());
					}else{
						entityOwnerSet.add(eName+SEPARATOR+sol.getKey());
					}
					
				}
				Set<String> underLyingExecEntities = getUnderLyingEntites(entityOwnerSet,clientCode,new HashSet<String>(),entityAndTypeMap);
				underLyingExecEntities.addAll(entityOwnerSetImmediate);
				Set<String> value =  underLyingExecEntities;
				String reportIdName =  entry.getKey();
				if(null==reportEntityStats.get(reportIdName)){
					statsMap = new HashMap<String, Integer>();
					statsMap.put("completed",0 );
					statsMap.put("notDueYet",0 );
					statsMap.put("partiallyCompleted",0 );
					statsMap.put("overDue",0 );
					reportEntityStats.put(reportIdName, (HashMap<String, Integer>) statsMap);
				}
				for(String entityName : value){
					String entitySolution =entityName;
					//having all possible dates for each report.
					if(null!=taskDueMap.get(entitySolution)){
						dueDates.add(taskDueMap.get(entitySolution));
					}
					if(null!=taskCompletionMap.get(entitySolution)){
						completedDates.add(taskCompletionMap.get(entitySolution));
					}
					if(null!=sourceEntityDetails.get(entitySolution)){
						//checking the entity status for a report.
						if(sourceEntityDetails.get(entitySolution).get("completed")>0 && sourceEntityDetails.get(entitySolution).get("notDueYet")==0
								&& sourceEntityDetails.get(entitySolution).get("failed")==0 && sourceEntityDetails.get(entitySolution).get("overDue")==0){
							reportEntityStats.get(reportIdName).put("completed",
									reportEntityStats.get(reportIdName).get("completed")+1);
						}else if(sourceEntityDetails.get(entitySolution).get("overDue")>0 || sourceEntityDetails.get(entitySolution).get("failed")>0){
							reportEntityStats.get(reportIdName).put("overDue",
									reportEntityStats.get(reportIdName).get("overDue")+1);
						}else if(sourceEntityDetails.get(entitySolution).get("completed")>0 && (sourceEntityDetails.get(entitySolution).get("notDueYet")>0
								 )){
							reportEntityStats.get(reportIdName).put("partiallyCompleted",
									reportEntityStats.get(reportIdName).get("partiallyCompleted")+1);
						}else if(sourceEntityDetails.get(entitySolution).get("completed")==0 && sourceEntityDetails.get(entitySolution).get("notDueYet")>0
								&& sourceEntityDetails.get(reportIdName).get("entityName")==0 && sourceEntityDetails.get(entitySolution).get("overDue")==0){
							reportEntityStats.get(reportIdName).put("notDueYet",
									reportEntityStats.get(reportIdName).get("notDueYet")+1);
						}
					}
				}
				reportDueMap.put(reportIdName, dueDates);
				reportCompletionMap.put(reportIdName,completedDates);
			}

			for(Map.Entry<String,HashMap<String,Integer>> entry : reportEntityStats.entrySet()){
				reportLineItemMap.put(entry.getKey().split(SEPARATOR)[0], "");
			}
			ReportStatusPayload reportsInfo ;
			try{
				reportsInfo= SolutionReportStatus.getStatusForReportForSolutions(reportLineItemMap, sol.getKey(), currrentBusinessDate,solutions.get(sol.getKey()).toString(),clientCode);
			} catch (Throwable e){
				LOGGER.error(e.getMessage());
				LOGGER.info("REPORT STATUS NOT AVAILABLE");
				reportsInfo= null;
			}

			Map<String,String> reportstatus=new HashMap<>();
			Map<String,String>	reportTimeStamp= new HashMap<>();
			if(reportsInfo!=null){
				for(ReportInfo info :reportsInfo.getReportInfo()){
					if(info.getStamp()!=null && !"null".equalsIgnoreCase(info.getStamp())){
						reportTimeStamp.put(info.getRegReportId(), info.getStamp());
						if(info.getStatus()!=null && !"null".equalsIgnoreCase(info.getStamp()))
							reportstatus.put(info.getRegReportId(), info.getStatus());
						else 
							reportstatus.put(info.getRegReportId(), "NA");
					}

					else {
						reportTimeStamp.put(info.getRegReportId(), "NA");
						reportstatus.put(info.getRegReportId(), "NA");
					}

				}
			} else {
				for(Map.Entry<String,HashMap<String,Integer>> entry : reportEntityStats.entrySet()){
					reportTimeStamp.put(entry.getKey().split(SEPARATOR)[0], "NA");
					reportstatus.put(entry.getKey().split(SEPARATOR)[0], "NA");
				}	
			}


			for (Map.Entry<String,HashMap<String,Integer>> entry : reportEntityStats.entrySet()) {
				HashMap<String,Integer> value =  entry.getValue();
				if(!(value.get("completed")==0 && value.get("partiallyCompleted")==0 && value.get("notDueYet")==0 && value.get("overDue")==0)){
					Integer reportId = Integer.parseInt(entry.getKey().split(SEPARATOR)[0]);
					String reportName = entry.getKey().split(SEPARATOR)[1];
					JSONObject solutionReportDetails = new JSONObject();
					solutionReportDetails.put("solutionName", sol.getKey());
					solutionReportDetails.put("solutionId", sol.getValue());
					solutionReportDetails.put("reportName",reportName);
					//TODO hard coded add column in dim reg report and set this variable
					solutionReportDetails.put("lineItemFlag",reportNameLineItemFlag.get(reportName+SEPARATOR+sol.getValue()));
					solutionReportDetails.put("reportId", reportId);
					solutionReportDetails.put("dataAvailDueDate", Collections.max(reportDueMap.get(entry.getKey())));
					if(!reportCompletionMap.get(entry.getKey()).isEmpty()){
						solutionReportDetails.put("dataAvailCompletedDate", Collections.max(reportCompletionMap.get(entry.getKey())));	
					}else{
						solutionReportDetails.put("dataAvailCompletedDate", "");
					}
					
					if(reportTimeStamp.get(reportId.toString())!=null &&!"NA".equalsIgnoreCase(reportTimeStamp.get(reportId.toString()))){
						solutionReportDetails.put("dataProcessingDueDate", formatter.print(Long.parseLong(reportTimeStamp.get(reportId.toString()))));
						solutionReportDetails.put("lastDataProcessingDate", formatter.print(Long.parseLong(reportTimeStamp.get(reportId.toString()))));
					} else {
						solutionReportDetails.put("dataProcessingDueDate", reportTimeStamp.get(reportId.toString()));
						solutionReportDetails.put("lastDataProcessingDate",reportTimeStamp.get(reportId.toString()));
					}
					
					//looping through map of report and entity name to fetch stats.

					solutionReportDetails.put("dataProcessedstatus", reportstatus.get(reportId.toString()));
					//looping through map of report and entity name to fetch stats.

					if(value.get("completed")>0 && value.get("partiallyCompleted")==0 && value.get("notDueYet")==0 && value.get("overDue")==0){
						solutionReportDetails.put("status", COMPLETED);
					}else if(value.get("overDue")>0){
						solutionReportDetails.put("status", "OVERDUE");
					}else if(value.get("partiallyCompleted")>0){
						solutionReportDetails.put("status", "PARTIALLY COMPLETED");
					}else if(value.get("notDueYet")>0){
						solutionReportDetails.put("status", "NOT DUE YET");
					}
					//TODO dataconsumer type
					solutionReportDetails.put("dataConsumer", "Report");
					finalResponse.add(solutionReportDetails);

				}
			}
		}
		dataObj.put("data", finalResponse);
		return dataObj;
	}


	private Set<String> getUnderLyingEntites(Set<String> entities,String clientCode,Set<String> entityAndUnderLyingExec,
			Map<String,String> entityAndTypeMap) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getUnderLyingEntites");
		Iterable<Map<String,Object>> taskDetails;
		Iterator<Map<String,Object>> taskItr;
		Map<String,Object> tempTaskDetails = new HashMap<>();
		Set<String> tempPending = entities;
		for(String entityName:entities){
			taskDetails=dldEntityRepository.getSourceForAEntity(clientCode, entityName.split(SEPARATOR)[0], entityName.split(SEPARATOR)[1]);
			taskItr=taskDetails.iterator();
			while(taskItr.hasNext()){
				tempTaskDetails=taskItr.next();
				
				String sourceOwnerName = tempTaskDetails.get("SOURCE")+SEPARATOR+tempTaskDetails.get("SOURCEOWNER");
				if(Y.equalsIgnoreCase(entityAndTypeMap.get(sourceOwnerName))){
						entityAndUnderLyingExec.add(sourceOwnerName);
				}else{
					entities.add(sourceOwnerName);
				}
			}
		}
		entities.removeAll(tempPending);
		
		if(entities.size()>0){
			return getUnderLyingEntites(entities, clientCode, entityAndUnderLyingExec, entityAndTypeMap);
		} else {
			return entityAndUnderLyingExec;	
		}
	
	}




	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getDetailsForDataConsumersSummaryLineItemGrid(String currrentBusinessDate,
			String isFrequencyFilterApplied,String frequencyFilterCSV,String isFlowFilterApplied,
			String flowFilterCSV,Integer reportId,Integer pageNo,Integer pageSize,
			String lineItemIdSearch,String lineItemDescSearch,String solutionName,Integer solutionId,String clientCode) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getDetailsForDataConsumersSummaryLineItemGrid");
		JSONObject dataObj = new JSONObject();
		JSONArray finalResponse = new JSONArray();
		DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		Map<String,Set<String>> lineItemIdEntityNameMap = new HashMap<String,Set<String>>() ;
		Set<String> freqApplicableForcbd=new HashSet<String>();
		List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
		Map<String,Integer> statsMap = null;
		Map<String,HashMap<String,Integer>> lineItemEntityStats = new HashMap<String, HashMap<String,Integer>>();
		Map<String,HashMap<String,Integer>> sourceEntityDetails = new HashMap<String, HashMap<String,Integer>>();
		DateTime sysDate=new DateTime();
		Map<String,Integer> taskDueMap = new HashMap<String, Integer>();
		Map<String,Integer> taskCompletionMap = new HashMap<String, Integer>();
		Map<String,Set<Integer>> reportDueMap = new HashMap<String, Set<Integer>>();
		Map<String,Set<Integer>> reportCompletionMap = new HashMap<String, Set<Integer>>();
		DateTimeFormatter formatter = DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT);
		Map<String,Integer> solutions = dldDao.getListOfSolutionNames(clientCode);
		//getting applicable frequencies and generating csv for in clause if filter is not applied.
		if(!Y.equalsIgnoreCase(isFrequencyFilterApplied)){
			freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
			frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";
		}

		//get staging details.
		totalStagingObject = dldDao.getStagingDetails(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);


		//processing staging results
		for(StagingDetails tsk :totalStagingObject){


			//for source entity details.
			//check for src system entry
			String entitySolutionId= tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();

			if(sourceEntityDetails.get(entitySolutionId)==null){
				statsMap = new HashMap<String, Integer>();
				statsMap.put("completed",0 );
				statsMap.put("notDueYet",0 );
				statsMap.put("failed",0 );
				statsMap.put("overDue",0 );
				sourceEntityDetails.put(entitySolutionId, (HashMap<String, Integer>) statsMap);
			}

			//map to fiind max date of those against each entity.
			if(null== taskDueMap.get(entitySolutionId)){
				taskDueMap.put(entitySolutionId,Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset())))));
			}else{
				if(taskDueMap.get(entitySolutionId)<Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset()))))){
					taskDueMap.put(entitySolutionId,Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset())))));
				}
			}
			if(null!=tsk.getRunDate()){
				if(null==taskCompletionMap.get(entitySolutionId)){
					taskCompletionMap.put(entitySolutionId,Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate()))));
				}else{
					if(taskCompletionMap.get(entitySolutionId)<Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate())))){
						taskCompletionMap.put(entitySolutionId,Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate()))));
					}
				}
			}


			if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
				//not yet started 
				sourceEntityDetails.get(entitySolutionId).put("notDueYet",
						(sourceEntityDetails.get(entitySolutionId).get("notDueYet")+1));
			}
			else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
				//check for over due failed success.
				if(null ==tsk.getTaskStatus()){
					//overDueTask
					sourceEntityDetails.get(entitySolutionId).put("overDue",
							(sourceEntityDetails.get(entitySolutionId).get("overDue")+1));
				}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//failed tasks.
					sourceEntityDetails.get(entitySolutionId).put("failed",
							(sourceEntityDetails.get(entitySolutionId).get("failed")+1));
				}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//completed tasks.
					sourceEntityDetails.get(entitySolutionId).put("completed",
							(sourceEntityDetails.get(entitySolutionId).get("completed")+1));
				}
			}

		}
		//processing stats of entuty to get report stats.
		DldSolution sol = new DldSolution();
		sol.setSolutionName(solutionName);
		sol.setSolutionID(solutionId);
		lineItemIdEntityNameMap = new HashMap<String,Set<String>>();
		Integer totalCount=dldDao.getLineItemTotalCount(businessDateRec,sol,reportId,lineItemIdSearch,lineItemDescSearch,clientCode);
		lineItemIdEntityNameMap=dldDao.getLineItemEntityMap(businessDateRec,sol,reportId,pageNo,pageSize,lineItemIdSearch,lineItemDescSearch,clientCode);

		String lineItemIDsCSV="";
		for(Map.Entry<String,Set<String>> entry : lineItemIdEntityNameMap.entrySet()){
			lineItemIDsCSV=lineItemIDsCSV+entry.getKey()+",";
		}
		Map<String,String> reportLineItemMap = new HashMap<>();
		reportLineItemMap.put(reportId.toString(),lineItemIDsCSV);
		ReportStatusPayload reportsInfo ;
		try{
			reportsInfo= SolutionReportStatus.getStatusForReportForSolutions(reportLineItemMap, solutionName, currrentBusinessDate,solutions.get(solutionName).toString(),clientCode);
		} catch (Throwable e){
			LOGGER.info("REPORT STATUS NOT AVAILABLE");
			reportsInfo= null;
		}

		Map<String,String> lineItemStatus=new HashMap<>();
		Map<String,String> lineItemTimeStamp= new HashMap<>();
		if(reportsInfo!=null){
			for(LineItemInfo info :reportsInfo.getReportInfo().get(0).getLineitems()){
				if(info.getStamp()!=null && !"null".equalsIgnoreCase(info.getStamp())){
					lineItemTimeStamp.put(info.getLineItemId(), info.getStamp());
					if(info.getStatus()!=null && !"null".equalsIgnoreCase(info.getStatus()))
						lineItemStatus.put(info.getLineItemId(),info.getStatus());
					else
						lineItemStatus.put(info.getLineItemId(), "NA");
				}
				else {
					lineItemTimeStamp.put(info.getLineItemId().split(SEPARATOR)[0], "NA");
					lineItemStatus.put(info.getLineItemId(), "NA");

				}
			}
		} else {
			for(Map.Entry<String,Set<String>> entry : lineItemIdEntityNameMap.entrySet()){
				lineItemTimeStamp.put(entry.getKey().split(SEPARATOR)[0], "NA");
				lineItemStatus.put(entry.getKey().split(SEPARATOR)[0], "NA");
			}
		}
		Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, "Y", frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, N,clientCode);
		lineItemEntityStats = new HashMap<String, HashMap<String,Integer>>();
		for (Map.Entry<String,Set<String>> entry : lineItemIdEntityNameMap.entrySet()) {
			//looping through map of report and entity name to fetch stats.
			Set<Integer> dueDates = new HashSet<Integer>();
			Set<Integer> completedDates = new HashSet<Integer>();
			Set<String> entityOwnerSet = new HashSet<String>();
			Set<String> entityOwnerSetImmediate = new HashSet<String>();
			for(String eName:entry.getValue()){
				if(Y.equalsIgnoreCase(entityAndTypeMap.get(eName+SEPARATOR+sol.getSolutionName()))){
					entityOwnerSetImmediate.add(eName+SEPARATOR+sol.getSolutionName());
				}else{
					entityOwnerSet.add(eName+SEPARATOR+sol.getSolutionName());
				}
				
			}
			Set<String> underLyingExecEntities = getUnderLyingEntites(entityOwnerSet,clientCode,new HashSet<String>(),entityAndTypeMap);
			underLyingExecEntities.addAll(entityOwnerSetImmediate);
			Set<String> value = underLyingExecEntities;
			String lineItemIdName =  entry.getKey();
			
			if(null==lineItemEntityStats.get(lineItemIdName)){
				statsMap = new HashMap<String, Integer>();
				statsMap.put("completed",0 );
				statsMap.put("notDueYet",0 );
				statsMap.put("partiallyCompleted",0 );
				statsMap.put("overDue",0 );
				lineItemEntityStats.put(lineItemIdName, (HashMap<String, Integer>) statsMap);
			}
			for(String entityName : value){
				String entityNameSolution = entityName;
				//having all possible dates for each report.
				if(null!=taskDueMap.get(entityNameSolution)){
					dueDates.add(taskDueMap.get(entityNameSolution));
				}
				if(null!=taskCompletionMap.get(entityNameSolution)){
					completedDates.add(taskCompletionMap.get(entityNameSolution));
				}
				if(null!=sourceEntityDetails.get(entityNameSolution)){
					//checking the entity status for a report.
					if(sourceEntityDetails.get(entityNameSolution).get("completed")>0 && sourceEntityDetails.get(entityNameSolution).get("notDueYet")==0
							&& sourceEntityDetails.get(entityNameSolution).get("failed")==0 && sourceEntityDetails.get(entityNameSolution).get("overDue")==0){
						lineItemEntityStats.get(lineItemIdName).put("completed",
								lineItemEntityStats.get(lineItemIdName).get("completed")+1);
					}else if(sourceEntityDetails.get(entityNameSolution).get("overDue")>0 || sourceEntityDetails.get(entityNameSolution).get("failed")>0 ){
						lineItemEntityStats.get(lineItemIdName).put("overDue",
								lineItemEntityStats.get(lineItemIdName).get("overDue")+1);
					}else if(sourceEntityDetails.get(entityNameSolution).get("completed")>0 && (sourceEntityDetails.get(entityNameSolution).get("notDueYet")>0
							)){
						lineItemEntityStats.get(lineItemIdName).put("partiallyCompleted",
								lineItemEntityStats.get(lineItemIdName).get("partiallyCompleted")+1);
					}else if(sourceEntityDetails.get(entityNameSolution).get("completed")==0 && sourceEntityDetails.get(entityNameSolution).get("notDueYet")>0
							&& sourceEntityDetails.get(lineItemIdName).get("entityName")==0 && sourceEntityDetails.get(entityNameSolution).get("overDue")==0){
						lineItemEntityStats.get(lineItemIdName).put("notDueYet",
								lineItemEntityStats.get(lineItemIdName).get("notDueYet")+1);
					}
				}
			}
			reportDueMap.put(lineItemIdName, dueDates);
			reportCompletionMap.put(lineItemIdName,completedDates);
		}



		for (Map.Entry<String,HashMap<String,Integer>> entry : lineItemEntityStats.entrySet()) {
			HashMap<String,Integer> value =  entry.getValue();
			if(!(value.get("completed")==0 && value.get("partiallyCompleted")==0 && value.get("notDueYet")==0 && value.get("overDue")==0)){
				Integer lineItemId = Integer.parseInt(entry.getKey().split(SEPARATOR)[0]);
				String lineItemName = entry.getKey().split(SEPARATOR)[1];
				JSONObject solutionReportDetails = new JSONObject();
				solutionReportDetails.put("solutionName", sol.getSolutionName());
				solutionReportDetails.put("lineItemName",lineItemName);
				solutionReportDetails.put("lineItemId", lineItemId);
				solutionReportDetails.put("dataAvailDueDate", Collections.max(reportDueMap.get(entry.getKey())));
				if(!reportCompletionMap.get(entry.getKey()).isEmpty()){
					solutionReportDetails.put("dataAvailCompletedDate", Collections.max(reportCompletionMap.get(entry.getKey())));	
				}else{
					solutionReportDetails.put("dataAvailCompletedDate", "");
				}
				
				if(lineItemTimeStamp.get(lineItemId.toString())!=null && !"NA".equalsIgnoreCase(lineItemTimeStamp.get(lineItemId.toString()))){
					solutionReportDetails.put("dataProcessingDueDate", formatter.print(Long.parseLong(lineItemTimeStamp.get(lineItemId.toString()))));
					solutionReportDetails.put("lastDataProcessingDate", formatter.print(Long.parseLong(lineItemTimeStamp.get(lineItemId.toString()))));
				} else {
					solutionReportDetails.put("dataProcessingDueDate", lineItemTimeStamp.get(lineItemId.toString()));
					solutionReportDetails.put("lastDataProcessingDate",lineItemTimeStamp.get(lineItemId.toString()));
				}
				
				//looping through map of report and entity name to fetch stats.
				//looping through map of report and entity name to fetch stats.
				if(value.get("completed")>0 && value.get("partiallyCompleted")==0 && value.get("notDueYet")==0 && value.get("overDue")==0){
					solutionReportDetails.put("status", COMPLETED);
				}else if(value.get("overDue")>0){
					solutionReportDetails.put("status", "OVERDUE");
				}else if(value.get("partiallyCompleted")>0){
					solutionReportDetails.put("status", "PARTIALLY COMPLETED");
				}else{
					solutionReportDetails.put("status", "NOT DUE YET");
				}
				solutionReportDetails.put("lineItemProcessedStatus", lineItemStatus.get(lineItemId.toString()));
				finalResponse.add(solutionReportDetails);

			}
		}
		dataObj.put("totalCount", totalCount);
		dataObj.put("data", finalResponse);
		return dataObj;
	}




	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getSubGridTaskDetails(String currrentBusinessDate,
			String taskName, String repoName,String clientCode) {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getSubGridTaskDetails");
		DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		List<TaskExecutionLog> totalStagingObject = new LinkedList<TaskExecutionLog>();
		JSONArray dataArray = new JSONArray();
		JSONObject finalObj = new JSONObject();
		try {
			totalStagingObject = dldDao.getSubGridTaskDetails(businessDateRec,taskName,repoName,clientCode);
			Integer seqNo = 1;
			for(TaskExecutionLog tsk :totalStagingObject){
				JSONObject dataObj = new JSONObject();
				dataObj.put("sequenceNumber", seqNo);
				if(tsk.getStartDate()!=null)
					dataObj.put("taskStartDateTime", tsk.getStartDate().toString());
				else
					dataObj.put("taskStartDateTime", "");
				if(tsk.getEndDate()!=null)
					dataObj.put("taskEndDateTime", tsk.getEndDate().toString());
				else
					dataObj.put("taskEndDateTime", "");
				dataObj.put("status", tsk.getTaskStatus());
				dataObj.put("runDetails", null==tsk.getRunDetails()?"":tsk.getRunDetails());
				seqNo++;
				dataArray.add(dataObj);
			}
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
		finalObj.put("data", dataArray);
		return finalObj;
	}




	@Override
	public boolean isValidationOnTaskRequired(String clientCode,String taskName,String taskRepo,java.util.Date businessDate ) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > isValidationOnTaskRequired");
		return dldDao.isValidationRequiredOnTask(clientCode, taskName, taskRepo, businessDate);
	}




	@Override
	public List<String> getAllTaskType(String clientCode) throws Throwable {
		List<String> taskTypeList=null;
		taskTypeList=dldDao.getAllTaskType(clientCode);
		return taskTypeList;
	}




	@Override
	public List<String> getAllentityType(String clientCode) throws Throwable{
		List<String> entityTypeList=null;
		entityTypeList=dldDao.getAllentityType(clientCode);
		return entityTypeList;
	}


	private List<DLDTask> createDataForGraphDB(List<EntityMasterUploaderDTO> entityMasterList,List<EntityOwnerUploaderDTO> entityOwners,
			List<TaskMasterUploaderDTO> taskMasterList,String clientCode,List<TaskEntityDetailUploaderDTO> taskSourceTargetList){
		LOGGER.info("EXEFLOW-DldBoImpl -- > createDataForGraphDB");

		List<DLDTask> allTaskList = new ArrayList<DLDTask>();
		Set<DLDEntity> entityList;
		DLDTask dldTask;
		DldEntityCollection toEntity;
		DldEntityCollection fromEntity;
		DLDEntity dldEntity;
		ClientContent client= new ClientContent();
		client.setClientCode(clientCode);


		Map<String,DLDEntity> entityMap = new HashMap<>();


		for(EntityMasterUploaderDTO tempEntityMaster:entityMasterList){
			dldEntity=new DLDEntity();
			dldEntity.setEntityDescription(tempEntityMaster.getDescription());
			dldEntity.setEntityDetail(tempEntityMaster.getEntity_Detail());
			dldEntity.setEntityName(tempEntityMaster.getEntity_Name());
			dldEntity.setEntityType(tempEntityMaster.getEntity_Type());
			dldEntity.setEntityOwnerName(tempEntityMaster.getOwner_Name());
			dldEntity.setClient(client);
			entityMap.put(tempEntityMaster.getEntity_Name().toLowerCase()+"#"+tempEntityMaster.getOwner_Name().toLowerCase(), dldEntity);
		}

		//iterate all task
		List<TaskEntityDetailUploaderDTO> tempTaskSrcTgtDet = new ArrayList<>();
		for(TaskMasterUploaderDTO taskMaster:taskMasterList){
			if("Active".equalsIgnoreCase(taskMaster.getStatus())){
				toEntity=new DldEntityCollection();
				fromEntity=new DldEntityCollection();
				String taskName = taskMaster.getTask_Name();
				//from Entity
				if(taskSourceTargetList.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName) && col.getLinkType().equalsIgnoreCase("S")).collect(Collectors.toList()).size()>0){
					tempTaskSrcTgtDet=taskSourceTargetList.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName) && col.getLinkType().equalsIgnoreCase("S")).collect(Collectors.toList());
					entityList= new HashSet<DLDEntity>();

					//iterate over all source entities to create list of entity 
					for(TaskEntityDetailUploaderDTO src:tempTaskSrcTgtDet){

						for(String srcEntityName:src.getEntityName().split(",")){
							//get entity details based on entity name and owner
							entityList.add(entityMap.get(srcEntityName.toLowerCase()+"#"+src.getEntityOwnerName().toLowerCase()));
						}


					}

					//add from entity for task
					fromEntity.setEntities(entityList);

				}



				//toEntity
				if(taskSourceTargetList.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName) && col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList()).size()>0){
					//multiple Entity in target for a task
					tempTaskSrcTgtDet=taskSourceTargetList.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName) && col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
					entityList= new HashSet<DLDEntity>();

					//iterate over all target entities to create list of entity 
					for(TaskEntityDetailUploaderDTO tgt:tempTaskSrcTgtDet){

						//get entity details based on entity name and owner
						for(String tgtEntityName:tgt.getEntityName().split(",")){
							entityList.add(entityMap.get(tgtEntityName.toLowerCase()+"#"+tgt.getEntityOwnerName().toLowerCase()));
						}


					}

					//set target entity for task
					toEntity.setEntities(entityList);
				}

				//set up task details
				dldTask= new DLDTask(toEntity, fromEntity,taskMaster.getTask_Name(), taskMaster.getTask_Repository(), clientCode,taskMaster.getTask_Type());

				allTaskList.add(dldTask);
			}
			

		}

		return allTaskList;
	}



	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Collection<DLDEntity>  graph(int limit) {
		Collection<DLDEntity> result = dldEntityRepository.graph(limit);
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getDataForLineage(String clientCode,String currrentBusinessDate){

		JSONObject lineageDetails = new JSONObject();
		try {
			Iterable<Map<String,Object>> taskDetails = dldEntityRepository.getSourceTargetDependencyTask(clientCode);

			Iterator<Map<String,Object>> taskItr = taskDetails.iterator();

			List<TaskFrequencyDetail> allTaskApplicable = getApplicableTaskForLineage(currrentBusinessDate, "", "", N, N, clientCode);
			
			Set<String> applicableFreq= new HashSet<>();
			for(TaskFrequencyDetail fd:allTaskApplicable){
				applicableFreq.add(fd.getFrequencyType());
			}
			String frequency="";
			for(String f:applicableFreq){
				frequency=frequency+"'"+f.toUpperCase()+"',";
			}
			if(frequency.length()>0){
				frequency=frequency.substring(0, frequency.length()-1);
			}
			

			

			JSONObject task;
			JSONArray taskArray = new JSONArray();
			JSONArray entityArray = new JSONArray();
			JSONObject entityObj;

			Map<String,Object> tempTaskDetails = new HashMap<>();

			Map<String,Integer> allSolutions=new HashMap<>();
			List<StagingDetails> totalStagingObject = new ArrayList<>();
			List<StagingDetails> tempTotalStagingObjects= new ArrayList<>();
			allSolutions=dldDao.getListOfSolutionNames(clientCode);

			DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			totalStagingObject = dldDao.getTaskStatusDetails(businessDateRec,frequency,clientCode);

			String status="";

			while(taskItr.hasNext()){
				tempTaskDetails=taskItr.next();
				String taskName=(String)tempTaskDetails.get("taskName");
				String taskRepo=(String)tempTaskDetails.get("taskRepo");
				String srcEntityName=(String) tempTaskDetails.get("SOURCE");
				String srcOwnerName=(String) tempTaskDetails.get("SOURCEOWNER");
				task=new JSONObject();
				task.put("taskName", tempTaskDetails.get("taskName"));
				task.put("SOURCE", tempTaskDetails.get("SOURCE"));
				task.put("SOURCEOWNER", tempTaskDetails.get("SOURCEOWNER"));
				task.put("TARGET", tempTaskDetails.get("TARGET"));
				task.put("TARGETOWNER", tempTaskDetails.get("TARGETOWNER"));
				if(allTaskApplicable.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName)&& col.getTaskRepository().equalsIgnoreCase(taskRepo)).collect(Collectors.toList()).size()>0){
					if(N.equalsIgnoreCase(dldDao.isTaskExecutable((String)tempTaskDetails.get("taskType"),clientCode))){
						tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
								col->col.getEntityName().equalsIgnoreCase(srcEntityName)
								&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
								&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
						if(tempTotalStagingObjects.size()>0){
							for(StagingDetails sd:tempTotalStagingObjects){
								if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
									task.put("STATUS", "FAILED");
									break;
								} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
									task.put("STATUS", "COMPLETED");
								}
							}
						} else {
							task.put("STATUS", "PENDING");
						}
						
						
					} else {
						status=dldDao.getTaskStatusForDataLinage(clientCode,(String) tempTaskDetails.get("taskName"),
								( String)tempTaskDetails.get("taskRepo"), new Date(businessDateRec.getMillis()));

						task.put("STATUS", status);

					}
				} else {
					task.put("STATUS", "Not Applicable");
				}

				taskArray.add(task);
			}



			List<DlEntity> entityList = dldDao.getEntityMasterData(clientCode);

			List<DlEntityOwner> entityOwner =dldDao.getEntityOwnerData(clientCode);

			Map<String,String> entityOwnerNameIsExtSrcMap = new HashMap<>();

			Map<String,String> entityOwnerIsStageArea = new HashMap<>();
			JSONObject ownerDisplayOrder;
			JSONArray ownerDisplayOrderArray = new JSONArray();

			for(DlEntityOwner eo:entityOwner){
				ownerDisplayOrder=new JSONObject();
				entityOwnerNameIsExtSrcMap.put(eo.getOwner_Name(), eo.getExternal_Source());

				if("N".equalsIgnoreCase(eo.getExternal_Source())&& eo.getSolution_Id()==null){
					entityOwnerIsStageArea.put(eo.getOwner_Name(), Y);
				}

				if("N".equalsIgnoreCase(eo.getExternal_Source())){
					ownerDisplayOrder.put("ownerName", eo.getOwner_Name());
					ownerDisplayOrder.put("displayOrder", eo.getDisplay_Sorting_Order());
					ownerDisplayOrderArray.add(ownerDisplayOrder);
				}
			}


			for(DlEntity entity:entityList){
				entityObj= new JSONObject();

				entityObj.put("entityName", entity.getEntityName());
				entityObj.put("entityDetail", entity.getEntityDetail());
				entityObj.put("entityDesc", entity.getDescription());
				entityObj.put("entityOwner", entity.getOwnerName());
				if(Y.equalsIgnoreCase(entityOwnerNameIsExtSrcMap.get(entity.getOwnerName()))){
					entityObj.put("isSourceSystem", Y);
					entityObj.put("isDataRepo", N);
					entityObj.put("isStageArea", N);
				}else{
					if(Y.equalsIgnoreCase(entityOwnerIsStageArea.get(entity.getOwnerName()))){
						entityObj.put("isStageArea", Y);	
					}else {
						entityObj.put("isStageArea", N);
					}
					entityObj.put("isSourceSystem", N);
					entityObj.put("isDataRepo", Y);
				}

				entityObj.put("isDataConsumer", N);

				entityArray.add(entityObj);
			}

			Map<String,Set<String>> reportEntityMap = new HashMap<>();
			List<DlEntityOwner> dcSrcEntityOwner = new ArrayList<>();
			Map<String,String> reportLineItemFlagMap= new HashMap<>();
			//processing stats of entity to get report stats.
			Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, Y , frequency, N, EMPTY_QUOTES, N,clientCode);
			Set<String> nonExecutableEntities =new HashSet<>(); 
			Set<String> basefact =new HashSet<>(); 
			
			for(Map.Entry<String, Integer> sol:allSolutions.entrySet()){

				reportEntityMap=dldDao.getReportAndEntityMap(businessDateRec, sol.getKey(),sol.getValue(),clientCode);
				dcSrcEntityOwner=entityOwner.stream().filter(col->col.getOwner_Name().equalsIgnoreCase(sol.getKey())).collect(Collectors.toList());
				reportLineItemFlagMap=dldDao.getReportAndLineItemFlag(sol.getValue(), sol.getKey(), clientCode);

				for(Map.Entry<String, Set<String>> report:reportEntityMap.entrySet()){

					entityObj= new JSONObject();

					entityObj.put("entityName", report.getKey().split("@##@")[1]+" ["+report.getKey().split("@##@")[0]+"]");
					entityObj.put("entityDetail", report.getKey().split("@##@")[1]);
					entityObj.put("entityDesc", report.getKey().split("@##@")[1]);
					entityObj.put("entityOwner", sol.getKey());
					entityObj.put("isSourceSystem", N);
					entityObj.put("isDataRepo", N);
					entityObj.put("isDataConsumer", Y);
					entityObj.put("isStageArea", N);
					entityObj.put("reportId",report.getKey().split("@##@")[0]);
					if(reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue())!=null
							&& reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue()).equalsIgnoreCase("Y")){
						entityObj.put("isLineItemDataRequired", Y);
					}else{
						entityObj.put("isLineItemDataRequired", N);
					}
					entityArray.add(entityObj);

					DldSolution solution = new DldSolution();
					solution.setSolutionID(sol.getValue());
					solution.setSolutionName(sol.getKey());

					for(String reportTask:report.getValue()){
						nonExecutableEntities= new HashSet<>();
						Integer totalCount=dldDao.getLineItemTotalCount(businessDateRec,solution,Integer.parseInt(report.getKey().split("@##@")[0]),"","",clientCode);
						if(reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue())!=null && reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue()).equalsIgnoreCase("Y")){

							task=new JSONObject();
							task.put("taskName","Load Data For " +report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
							task.put("SOURCE", reportTask);
							task.put("SOURCEOWNER", sol.getKey());
							task.put("TARGET", report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
							task.put("TARGETOWNER", sol.getKey());
							if(entityAndTypeMap.containsKey(reportTask+SEPARATOR+sol.getKey())){
								if("N".equalsIgnoreCase(entityAndTypeMap.get(reportTask+SEPARATOR+sol.getKey()))){
									nonExecutableEntities.add(reportTask+SEPARATOR+sol.getKey());
									basefact=getUnderLyingEntites(nonExecutableEntities, clientCode, new HashSet<>(), entityAndTypeMap);
									for(String bf:basefact){
										String srcEntityName=bf.split(SEPARATOR)[0];
										String srcOwnerName=bf.split(SEPARATOR)[1];
										tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
												col->col.getEntityName().equalsIgnoreCase(srcEntityName)
												&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
												&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
										if(tempTotalStagingObjects.size()>0){
											for(StagingDetails sd:tempTotalStagingObjects){
												if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
													task.put("STATUS", "FAILED");
													break;
												} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
													task.put("STATUS", "COMPLETED");
												}
											}
										} else {
											task.put("STATUS", "PENDING");
										}
									}
								}
							}
							task.put("isLineItemDataRequired", "Y");
							task.put("totalLineItems", totalCount);
							//task.put("reportId", report.getKey().split("@##@")[0]);
							taskArray.add(task);
						} else {
							task=new JSONObject();
							task.put("taskName","Load Data For " +report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
							task.put("SOURCE", reportTask);
							task.put("SOURCEOWNER", sol.getKey());
							task.put("TARGET", report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
							task.put("TARGETOWNER", sol.getKey());
							if(entityAndTypeMap.containsKey(reportTask+SEPARATOR+sol.getKey())){
								if("N".equalsIgnoreCase(entityAndTypeMap.get(reportTask+SEPARATOR+sol.getKey()))){
									nonExecutableEntities.add(reportTask+SEPARATOR+sol.getKey());
									basefact=getUnderLyingEntites(nonExecutableEntities, clientCode, new HashSet<>(), entityAndTypeMap);
									for(String bf:basefact){
										String srcEntityName=bf.split(SEPARATOR)[0];
										String srcOwnerName=bf.split(SEPARATOR)[1];
										tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
												col->col.getEntityName().equalsIgnoreCase(srcEntityName)
												&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
												&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
										if(tempTotalStagingObjects.size()>0){
											for(StagingDetails sd:tempTotalStagingObjects){
												if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
													task.put("STATUS", "FAILED");
													break;
												} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
													task.put("STATUS", "COMPLETED");
												}
											}
										} else {
											task.put("STATUS", "PENDING");
										}
									}
								}
							}
							task.put("isLineItemDataRequired", "N");
							task.put("totalLineItems", totalCount);
							task.put("reportId", report.getKey().split("@##@")[0]);
							taskArray.add(task);
						}

					}
				}
			}

			lineageDetails.put("entities", entityArray);
			lineageDetails.put("task", taskArray);
			lineageDetails.put("displayOrders", ownerDisplayOrderArray);


		} catch (Throwable e) {
			System.err.println(e.getMessage());
			LOGGER.error(e.getMessage());
		}


		return lineageDetails;
	}

	@Override
	public DlTaskMaster getTaskDetails(String clientCode,String taskRepo,String taskName,java.util.Date effectiveDate ) throws Throwable{

		return dldDao.getTaskMaster(clientCode, taskRepo, taskName, effectiveDate);
	}

	@Override
	public List<DlTaskSourceTarget> getTaskSourceTarget(String clientCode, String taskRepo, String taskName,
			Integer versionNo) throws Throwable {
		return dldDao.getSourceTargetData(clientCode, taskRepo, taskName, versionNo);
	}





	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getLineageDataForBU(String clientCode, String currrentBusinessDate, String solutionName) {

		JSONObject lineageDetails = new JSONObject();
		try {
			List<TaskFrequencyDetail> allTaskApplicable = getApplicableTaskForLineage(currrentBusinessDate, "", "", N, N, clientCode);
			Set<String> applicableFreq= new HashSet<>();
			for(TaskFrequencyDetail fd:allTaskApplicable){
				applicableFreq.add(fd.getFrequencyType());
			}
			String frequency="";
			for(String f:applicableFreq){
				frequency=frequency+"'"+f.toUpperCase()+"',";
			}
			if(frequency.length()>0){
				frequency=frequency.substring(0, frequency.length()-1);
			}
			Iterable<Map<String,Object>> taskDetails = dldEntityRepository.getSourceTargetDependencyTask(clientCode);
			List<StagingDetails> totalStagingObject = new ArrayList<>();
			List<StagingDetails> tempTotalStagingObjects= new ArrayList<>();

			Iterator<Map<String,Object>> taskItr = taskDetails.iterator();

			JSONObject task;
			JSONArray taskArray = new JSONArray();
			JSONArray entityArray = new JSONArray();
			JSONObject entityObj;

			Map<String,Object> tempTaskDetails = new HashMap<>();
			Set<String> excludedSolutionSet=new HashSet<String>();

			Map<String,Integer> allSolutions=new HashMap<>();
			allSolutions=dldDao.getListOfSolutionNames(clientCode);
			for(Map.Entry<String, Integer> sol:allSolutions.entrySet()){
				if(!sol.getKey().equalsIgnoreCase(solutionName)){
					excludedSolutionSet.add(sol.getKey().toUpperCase());	
				}
			}

			DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));

			String status="";

			List<DlEntity> entityList = dldDao.getEntityMasterData(clientCode);

			List<DlEntityOwner> entityOwner =dldDao.getEntityOwnerData(clientCode);

			Map<String,String> entityOwnerNameIsExtSrcMap = new HashMap<>();

			Map<String,String> entityOwnerIsStageArea = new HashMap<>();
			JSONObject ownerDisplayOrder;
			JSONArray ownerDisplayOrderArray = new JSONArray();

			for(DlEntityOwner eo:entityOwner){

				ownerDisplayOrder=new JSONObject();
				entityOwnerNameIsExtSrcMap.put(eo.getOwner_Name(), eo.getExternal_Source());

				if("N".equalsIgnoreCase(eo.getExternal_Source())&& eo.getSolution_Id()==null){
					entityOwnerIsStageArea.put(eo.getOwner_Name(), Y);
				}

				if("N".equalsIgnoreCase(eo.getExternal_Source())){
					if(!excludedSolutionSet.contains(eo.getOwner_Name().toUpperCase())){
						ownerDisplayOrder.put("ownerName", eo.getOwner_Name());
						ownerDisplayOrder.put("displayOrder", eo.getDisplay_Sorting_Order());
						ownerDisplayOrderArray.add(ownerDisplayOrder);
					}
				}
			}
			totalStagingObject = dldDao.getTaskStatusDetails(businessDateRec,frequency,clientCode);

			while(taskItr.hasNext()){
				tempTaskDetails=taskItr.next();
				task=new JSONObject();
				String taskName=(String)tempTaskDetails.get("taskName");
				String taskRepo=(String)tempTaskDetails.get("taskRepo");
				if(!excludedSolutionSet.contains(tempTaskDetails.get("SOURCEOWNER").toString().toUpperCase()) && !excludedSolutionSet.contains(tempTaskDetails.get("TARGETOWNER").toString().toUpperCase())){
					String srcEntityName=(String) tempTaskDetails.get("SOURCE");
					String srcOwnerName=(String) tempTaskDetails.get("SOURCEOWNER");
					task.put("taskName", tempTaskDetails.get("taskName"));
					task.put("SOURCE", tempTaskDetails.get("SOURCE"));
					task.put("SOURCEOWNER", tempTaskDetails.get("SOURCEOWNER"));
					task.put("TARGET", tempTaskDetails.get("TARGET"));
					task.put("TARGETOWNER", tempTaskDetails.get("TARGETOWNER"));
					if(allTaskApplicable.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName) && col.getTaskRepository().equalsIgnoreCase(taskRepo)).collect(Collectors.toList()).size()>0){
						if(N.equalsIgnoreCase(dldDao.isTaskExecutable((String)tempTaskDetails.get("taskName"),clientCode))){
							tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
									col->col.getEntityName().equalsIgnoreCase(srcEntityName)
									&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
									&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
							if(tempTotalStagingObjects.size()>0){
								for(StagingDetails sd:tempTotalStagingObjects){
									if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
										task.put("STATUS", "FAILED");
										break;
									} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
										task.put("STATUS", "COMPLETED");
									}
								}
							} else {
								task.put("STATUS", "PENDING");
							}
							
						} else {
							status=dldDao.getTaskStatusForDataLinage(clientCode,(String) tempTaskDetails.get("taskType"),
									( String)tempTaskDetails.get("taskRepo"), new Date(businessDateRec.getMillis()));
							task.put("STATUS", status);
						}
					} else {
						
							task.put("STATUS", "Not Applicable");
					}
					taskArray.add(task);
				}

			}




			for(DlEntity entity:entityList){
				entityObj= new JSONObject();
				if(!excludedSolutionSet.contains(entity.getOwnerName().toUpperCase())){
					entityObj.put("entityName", entity.getEntityName());
					entityObj.put("entityDetail", entity.getEntityDetail());
					entityObj.put("entityDesc", entity.getDescription());
					entityObj.put("entityOwner", entity.getOwnerName());
					if(Y.equalsIgnoreCase(entityOwnerNameIsExtSrcMap.get(entity.getOwnerName()))){
						entityObj.put("isSourceSystem", Y);
						entityObj.put("isDataRepo", N);
						entityObj.put("isStageArea", N);
					}else{
						if(Y.equalsIgnoreCase(entityOwnerIsStageArea.get(entity.getOwnerName()))){
							entityObj.put("isStageArea", Y);	
						}else {
							entityObj.put("isStageArea", N);
						}
						entityObj.put("isSourceSystem", N);
						entityObj.put("isDataRepo", Y);
					}

					entityObj.put("isDataConsumer", N);

					entityArray.add(entityObj);
				}
			}

			Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, Y , frequency, N, EMPTY_QUOTES, N,clientCode);
			Set<String> nonExecutableEntities =new HashSet<>(); 
			Set<String> basefact =new HashSet<>(); 
			Map<String,Set<String>> reportEntityMap = new HashMap<>();
			List<DlEntityOwner> dcSrcEntityOwner = new ArrayList<>();
			Map<String,String> reportLineItemFlagMap= new HashMap<>();
			for(Map.Entry<String, Integer> sol:allSolutions.entrySet()){
				if(sol.getKey().equalsIgnoreCase(solutionName)){
					reportEntityMap=dldDao.getReportAndEntityMap(businessDateRec, sol.getKey(),sol.getValue(),clientCode);
					dcSrcEntityOwner=entityOwner.stream().filter(col->col.getOwner_Name().equalsIgnoreCase(sol.getKey())).collect(Collectors.toList());
					reportLineItemFlagMap=dldDao.getReportAndLineItemFlag(sol.getValue(), sol.getKey(), clientCode);

					for(Map.Entry<String, Set<String>> report:reportEntityMap.entrySet()){

						entityObj= new JSONObject();

						entityObj.put("entityName", report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
						entityObj.put("entityDetail", report.getKey().split("@##@")[1]);
						entityObj.put("entityDesc", report.getKey().split("@##@")[1]);
						entityObj.put("entityOwner", sol.getKey());
						entityObj.put("isSourceSystem", N);
						entityObj.put("isDataRepo", N);
						entityObj.put("isDataConsumer", Y);
						entityObj.put("isStageArea", N);
						entityObj.put("reportId",report.getKey().split("@##@")[0]);
						if(reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue())!=null
								&& reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue()).equalsIgnoreCase("Y")){
							entityObj.put("isLineItemDataRequired", Y);
						}else{
							entityObj.put("isLineItemDataRequired", N);
						}
						entityArray.add(entityObj);

						DldSolution solution = new DldSolution();
						solution.setSolutionID(sol.getValue());
						solution.setSolutionName(sol.getKey());

						for(String reportTask:report.getValue()){
							Integer totalCount=dldDao.getLineItemTotalCount(businessDateRec,solution,Integer.parseInt(report.getKey().split("@##@")[0]),"","",clientCode);
							if(reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue())!=null && reportLineItemFlagMap.get( report.getKey().split("@##@")[1]+"@##@"+sol.getValue()).equalsIgnoreCase("Y")){

								task=new JSONObject();
								task.put("taskName","Load Data For " +report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
								task.put("SOURCE", reportTask);
								task.put("SOURCEOWNER", sol.getKey());
								task.put("TARGET", report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
								task.put("TARGETOWNER", sol.getKey());
								if(entityAndTypeMap.containsKey(reportTask+SEPARATOR+sol.getKey())){
									if("N".equalsIgnoreCase(entityAndTypeMap.get(reportTask+SEPARATOR+sol.getKey()))){
										nonExecutableEntities.add(reportTask+SEPARATOR+sol.getKey());
										basefact=getUnderLyingEntites(nonExecutableEntities, clientCode, new HashSet<>(), entityAndTypeMap);
										for(String bf:basefact){
											String srcEntityName=bf.split(SEPARATOR)[0];
											String srcOwnerName=bf.split(SEPARATOR)[1];
											tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
													col->col.getEntityName().equalsIgnoreCase(srcEntityName)
													&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
													&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
											if(tempTotalStagingObjects.size()>0){
												for(StagingDetails sd:tempTotalStagingObjects){
													if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
														task.put("STATUS", "FAILED");
														break;
													} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
														task.put("STATUS", "COMPLETED");
													}
												}
											} else {
												task.put("STATUS", "PENDING");
											}
										}
									}
								}
								task.put("isLineItemDataRequired", "Y");
								task.put("totalLineItems", totalCount);
								//task.put("reportId", report.getKey().split("@##@")[0]);
								taskArray.add(task);
							} else {
								task=new JSONObject();
								task.put("taskName","Load Data For " +report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
								task.put("SOURCE", reportTask);
								task.put("SOURCEOWNER", sol.getKey());
								task.put("TARGET", report.getKey().split("@##@")[1]+" ["+ report.getKey().split("@##@")[0]+"]");
								task.put("TARGETOWNER", sol.getKey());
								if(entityAndTypeMap.containsKey(reportTask+SEPARATOR+sol.getKey())){
									if("N".equalsIgnoreCase(entityAndTypeMap.get(reportTask+SEPARATOR+sol.getKey()))){
										nonExecutableEntities.add(reportTask+SEPARATOR+sol.getKey());
										basefact=getUnderLyingEntites(nonExecutableEntities, clientCode, new HashSet<>(), entityAndTypeMap);
										for(String bf:basefact){
											String srcEntityName=bf.split(SEPARATOR)[0];
											String srcOwnerName=bf.split(SEPARATOR)[1];
											tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
													col->col.getEntityName().equalsIgnoreCase(srcEntityName)
													&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
													&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
											if(tempTotalStagingObjects.size()>0){
												for(StagingDetails sd:tempTotalStagingObjects){
													if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
														task.put("STATUS", "FAILED");
														break;
													} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
														task.put("STATUS", "COMPLETED");
													}
												}
											} else {
												task.put("STATUS", "PENDING");
											}
										}
									}
								}
								task.put("isLineItemDataRequired", "N");
								task.put("totalLineItems", totalCount);
								task.put("reportId", report.getKey().split("@##@")[0]);
								taskArray.add(task);
							}

						}
					}
				}
			}

			lineageDetails.put("entities", entityArray);
			lineageDetails.put("task", taskArray);
			lineageDetails.put("displayOrders", ownerDisplayOrderArray);


		} catch (Throwable e) {
			LOGGER.error(e.getMessage());
		}


		return lineageDetails;
	}

	@Override
	public Workbook getLineageWorkBoook(List<DldStatusDownload> dldStatusDownloadList,Workbook workbook) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getLineageWorkBoook");
		Cells cells=workbook.getWorksheets().get("Data Consumer Status").getCells();
		DldStatusDownload dldStatusDownload=null;
		DateTime dt=null;
		for(int i=0;i<dldStatusDownloadList.size();i++)
		{
			dldStatusDownload=dldStatusDownloadList.get(i);
			cells.get(i+2,0).putValue(dldStatusDownload.getSolutionName());
			cells.get(i+2,1).putValue(dldStatusDownload.getConsumerType());
			cells.get(i+2,2).putValue(dldStatusDownload.getConsumerName());
			cells.get(i+2,3).putValue(dldStatusDownload.getSubItemName());
			cells.get(i+2,4).putValue(dldStatusDownload.getDataAvailabilityStatus());
			dt = DateTime.parse(dldStatusDownload.getDataAvailabilityDueDate(), DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			DateTimeFormatter dtf = DateTimeFormat.forPattern(dateFormat);
			cells.get(i+2,5).putValue(dtf.print(dt));
			if(dldStatusDownload.getDataAvailabilityCompletionDate()!=null&&dldStatusDownload.getDataAvailabilityCompletionDate()!="")
			{	
				dt = DateTime.parse(dldStatusDownload.getDataAvailabilityCompletionDate(), DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
				cells.get(i+2,6).putValue(dtf.print(dt));
			}
			else
				cells.get(i+2,6).putValue(dldStatusDownload.getDataAvailabilityCompletionDate());
			if(dldStatusDownload.getDataProcessingEndDueDate()!=null&&dldStatusDownload.getDataProcessingEndDueDate()!=""&&dldStatusDownload.getDataProcessingEndDueDate()!="NA")
			{
				dt = DateTime.parse(dldStatusDownload.getDataProcessingEndDueDate(), DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
				cells.get(i+2,7).putValue(dtf.print(dt));
			}
			else
				cells.get(i+2,7).putValue(dldStatusDownload.getDataProcessingEndDueDate());
			if(dldStatusDownload.getDataProcessingLastProcessedDate()!=null&&dldStatusDownload.getDataProcessingLastProcessedDate()!=""&&dldStatusDownload.getDataProcessingLastProcessedDate()!="NA")
			{
				dt = DateTime.parse(dldStatusDownload.getDataProcessingLastProcessedDate(), DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
				cells.get(i+2,8).putValue(dtf.print(dt));
			}
			else
				cells.get(i+2,8).putValue(dldStatusDownload.getDataProcessingLastProcessedDate());

			cells.get(i+2,9).putValue(dldStatusDownload.getConsumerRepository());
			cells.get(i+2,10).putValue(dldStatusDownload.getConsumerEntityName());
			cells.get(i+2,11).putValue(dldStatusDownload.getConsumerStatus());
			cells.get(i+2,13).putValue(dldStatusDownload.getSourceEntity());
			cells.get(i+2,12).putValue(dldStatusDownload.getSourceSystem());
			cells.get(i+2,14).putValue(dldStatusDownload.getSourceStatus());
		}

		return workbook;

	}
	@Override
	public List<DldStatusDownload> getDownloadDetailsForSheetOne(String currrentBusinessDate,
			String clientCode,String solutionName) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getDetailsForDataConsumersSummary");
		List<DldStatusDownload> objList = new ArrayList<DldStatusDownload>();
		Map<String,String> entityAndStatusMap = new HashMap<String, String>();
		Map<String,Integer> solutions=new HashMap<>();
		Map<String,Integer> allSolutions=new HashMap<>();
		Map<String,Set<String>> lineItemIdEntityNameMap = new HashMap<String,Set<String>>() ;
		Map<String,HashMap<String,Integer>> lineItemEntityStats = new HashMap<String, HashMap<String,Integer>>();
		allSolutions=dldDao.getListOfSolutionNames(clientCode);
		if(!solutionName.equalsIgnoreCase("All")){
			for(Map.Entry<String, Integer> dldSolution:allSolutions.entrySet()){
				if(dldSolution.getKey().equalsIgnoreCase(solutionName)){
					solutions.put(dldSolution.getKey()	, dldSolution.getValue());
				}
			}
		}
		else{
			solutions=allSolutions;
		}
		DateTime businessDateRec = DateTime.parse(currrentBusinessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		Map<String,Set<String>> reportIdEntityNameMap = new HashMap<String,Set<String>>() ;
		Set<String> freqApplicableForcbd=new HashSet<String>();
		List<StagingDetails> totalStagingObject = new ArrayList<StagingDetails>();
		Map<String,Integer> statsMap = null;
		Map<String,HashMap<String,Integer>> reportEntityStats = new HashMap<String, HashMap<String,Integer>>();
		Map<String,HashMap<String,Integer>> sourceEntityDetails = new HashMap<String, HashMap<String,Integer>>();
		DateTime sysDate=new DateTime();
		Map<String,Integer> taskDueMap = new HashMap<String, Integer>();
		Map<String,Integer> taskCompletionMap = new HashMap<String, Integer>();
		Map<String,Integer> reportDueMap = new HashMap<String,Integer>();
		Map<String,Integer> reportCompletionMap = new HashMap<String, Integer>();
		DateTimeFormatter formatter = DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT);
		//getting applicable frequencies and generating csv for in clause if filter is not applied.
		freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,N,"",clientCode);
		String frequencyFilterCSV = "'"+Joiner.on("','").join(freqApplicableForcbd)+"'";

		//get staging details.

		//for repo status.
		totalStagingObject = dldDao.getStagingDetails(businessDateRec, Y, frequencyFilterCSV, N,EMPTY_QUOTES , N,clientCode);

		//for source objects 
		totalStagingObject.addAll(dldDao.getStagingDetails(businessDateRec, Y, frequencyFilterCSV, N,EMPTY_QUOTES , Y,clientCode));

		//get source target array from graph db.
		@SuppressWarnings("unchecked")
		Iterable<Map<String,Object>> taskDetails = dldEntityRepository.getSourceTargetDependencyTask(clientCode);

		Iterator<Map<String,Object>> taskItr = taskDetails.iterator();
		Map<String,Object> tempTaskDetails = new HashMap<>();
		List<DlEntityOwner> entityOwner =dldDao.getEntityOwnerData(clientCode);
		List<String> sourceEntityOwnerList = new ArrayList<String>();
		Set<String> allSourceEntityWithOwner = new HashSet<String>();
		for(DlEntityOwner eo:entityOwner){
			if(Y.equalsIgnoreCase(eo.getExternal_Source())){
				sourceEntityOwnerList.add(eo.getOwner_Name());
			}
		}
		Multimap<String,String> sourceTargetWithOwners = ArrayListMultimap.create();

		//Multimap<String, String> invertedMultimap = Multimaps.invertFrom(a, ArrayListMultimap.<String, String>create());
		//Set<String> x = new HashSet<String>(invertedMultimap.get("1"));

		while(taskItr.hasNext()){
			tempTaskDetails=taskItr.next();
			sourceTargetWithOwners.put(tempTaskDetails.get("SOURCE")+SEPARATOR+tempTaskDetails.get("SOURCEOWNER")
					, tempTaskDetails.get("TARGET")+SEPARATOR+tempTaskDetails.get("TARGETOWNER"));
			if(sourceEntityOwnerList.contains(tempTaskDetails.get("SOURCEOWNER"))){
				allSourceEntityWithOwner.add(tempTaskDetails.get("SOURCE")+SEPARATOR+tempTaskDetails.get("SOURCEOWNER"));
			}

		}
		//TODO to increase performance have map of consumers entity name and itts source and statsus to reuse.
		/*
		 * TODO performance.
		 * 
		 * on (16-oct)
		 * */

		//processing staging results
		for(StagingDetails tsk :totalStagingObject){
			//for source entity details.
			//check for src system entry
			String entitySolutionId= tsk.getEntityName()+SEPARATOR+tsk.getOwnerName();
			if(sourceEntityDetails.get(entitySolutionId)==null){
				statsMap = new HashMap<String, Integer>();
				statsMap.put("completed",0 );
				statsMap.put("notDueYet",0 );
				statsMap.put("failed",0 );
				statsMap.put("overDue",0 );
				sourceEntityDetails.put(entitySolutionId, (HashMap<String, Integer>) statsMap);
			}

			//map to fiind max date of those against each entity.
			if(null== taskDueMap.get(entitySolutionId)){
				taskDueMap.put(entitySolutionId,Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset())))));
			}else{
				if(taskDueMap.get(entitySolutionId)<Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset()))))){
					taskDueMap.put(entitySolutionId,Integer.parseInt((formatter.print(businessDateRec.plusDays(tsk.getOffset())))));
				}
			}
			if(null!=tsk.getRunDate()){
				if(null==taskCompletionMap.get(entitySolutionId)){
					taskCompletionMap.put(entitySolutionId,Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate()))));
				}else{
					if(taskCompletionMap.get(entitySolutionId)<Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate())))){
						taskCompletionMap.put(entitySolutionId,Integer.parseInt(formatter.print(new DateTime(tsk.getRunDate()))));
					}
				}
			}


			if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())>0){
				//not yet started 
				sourceEntityDetails.get(entitySolutionId).put("notDueYet",
						(sourceEntityDetails.get(entitySolutionId).get("notDueYet")+1));
			}
			else if(businessDateRec.plusDays(tsk.getOffset()).toLocalDate().compareTo(sysDate.toLocalDate())<=0 ){	
				//check for over due failed success.
				if(null ==tsk.getTaskStatus()){
					//overDueTask
					sourceEntityDetails.get(entitySolutionId).put("overDue",
							(sourceEntityDetails.get(entitySolutionId).get("overDue")+1));
				}else if(!COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//failed tasks.
					sourceEntityDetails.get(entitySolutionId).put("failed",
							(sourceEntityDetails.get(entitySolutionId).get("failed")+1));
				}else if(COMPLETED.equalsIgnoreCase(tsk.getTaskStatus())){
					//completed tasks.
					sourceEntityDetails.get(entitySolutionId).put("completed",
							(sourceEntityDetails.get(entitySolutionId).get("completed")+1));
				}
			}

		}

		//process stats for each entity and have map.
		for(Map.Entry<String, HashMap<String,Integer>> entry:sourceEntityDetails.entrySet()){
			HashMap<String,Integer> value = entry.getValue();
			String entityNameSol = entry.getKey();
			if(value.get("completed")>0 && value.get("failed")==0 && value.get("notDueYet")==0 && value.get("overDue")==0){
				entityAndStatusMap.put(entityNameSol, COMPLETED);
			}else if(value.get("overDue")>0 ){
				entityAndStatusMap.put(entityNameSol, "OVERDUE");
			}else if(value.get("notDueYet")>0 && value.get("completed")==0 &&  value.get("failed")==0){
				entityAndStatusMap.put(entityNameSol, "NOT DUE YET");
			}else if(value.get("failed")>0) {
				entityAndStatusMap.put(entityNameSol, "FAILED");
			} else{
				entityAndStatusMap.put(entityNameSol, "PARTIALLY COMPLETED");
			}
		}




		//processing stats of entity to get report stats.
		Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, Y , frequencyFilterCSV, N, EMPTY_QUOTES, N,clientCode);
		for(Map.Entry<String, Integer> sol:solutions.entrySet()){
			reportIdEntityNameMap = new HashMap<String,Set<String>>();
			reportEntityStats = new HashMap<String, HashMap<String,Integer>>();
			//TODO use flag for line item while preparing json.
			Map<String,String>reportNameLineItemFlag = dldDao.getReportAndLineItemFlag(sol.getValue(),sol.getKey(),clientCode);
			reportIdEntityNameMap=dldDao.getReportAndEntityMap(businessDateRec,sol.getKey(),sol.getValue(),clientCode);
			for (Map.Entry<String,Set<String>> entry : reportIdEntityNameMap.entrySet()) {
				Map<String,String> reportLineItemMap = new HashMap<>();
				String reportIdName =  entry.getKey();
				String reportName = reportIdName.split(SEPARATOR)[1];
				String reportId = reportIdName.split(SEPARATOR)[0];
				//condition to check if line item details needed to be processsed.
				if(N.equalsIgnoreCase(reportNameLineItemFlag.get(entry.getKey().split(SEPARATOR)[1]+SEPARATOR+sol.getValue()))){
					//looping through map of report and entity name to fetch stats.
					Set<Integer> dueDates = new HashSet<Integer>();
					Set<Integer> completedDates = new HashSet<Integer>();
					Set<String> entityOwnerSet = new HashSet<String>();
					Set<String> entityOwnerSetImmediate = new HashSet<String>();
					for(String eName:entry.getValue()){
						if(Y.equalsIgnoreCase(entityAndTypeMap.get(eName+SEPARATOR+sol.getKey()))){
							entityOwnerSetImmediate.add(eName+SEPARATOR+sol.getKey());
						}else{
							entityOwnerSet.add(eName+SEPARATOR+sol.getKey());
						}
						
					}
					Set<String> underLyingExecEntities = getUnderLyingEntites(entityOwnerSet,clientCode,new HashSet<String>(),entityAndTypeMap);
					underLyingExecEntities.addAll(entityOwnerSetImmediate);
					Set<String> value = underLyingExecEntities;// entry.getValue()

					if(null==reportEntityStats.get(reportIdName)){
						statsMap = new HashMap<String, Integer>();
						statsMap.put("completed",0 );
						statsMap.put("notDueYet",0 );
						statsMap.put("partiallyCompleted",0 );
						statsMap.put("overDue",0 );
						statsMap.put("failed",0 );
						reportEntityStats.put(reportIdName, (HashMap<String, Integer>) statsMap);
					}
					for(String entityName : value){
						String entitySolution =entityName;
						//find all source entites for the consumer entity

						//having all possible dates for each report.
						if(null!=taskDueMap.get(entitySolution)){
							dueDates.add(taskDueMap.get(entitySolution));
						}
						if(null!=taskCompletionMap.get(entitySolution)){
							completedDates.add(taskCompletionMap.get(entitySolution));
						}
						if(null!=sourceEntityDetails.get(entitySolution)){
							//checking the entity status for a report.
							if(sourceEntityDetails.get(entitySolution).get("completed")>0 && sourceEntityDetails.get(entitySolution).get("notDueYet")==0
									&& sourceEntityDetails.get(entitySolution).get("failed")==0 && sourceEntityDetails.get(entitySolution).get("overDue")==0){
								reportEntityStats.get(reportIdName).put("completed",
										reportEntityStats.get(reportIdName).get("completed")+1);
							}else if(sourceEntityDetails.get(entitySolution).get("overDue")>0){
								reportEntityStats.get(reportIdName).put("overDue",
										reportEntityStats.get(reportIdName).get("overDue")+1);
							}else if(sourceEntityDetails.get(entitySolution).get("completed")>0 && (sourceEntityDetails.get(entitySolution).get("notDueYet")>0
									 )){
								reportEntityStats.get(reportIdName).put("partiallyCompleted",
										reportEntityStats.get(reportIdName).get("partiallyCompleted")+1);
							}else if(sourceEntityDetails.get(entitySolution).get("completed")==0 && sourceEntityDetails.get(entitySolution).get("notDueYet")>0
									&& sourceEntityDetails.get(reportIdName).get("entityName")==0 && sourceEntityDetails.get(entitySolution).get("overDue")==0){
								reportEntityStats.get(reportIdName).put("notDueYet",
										reportEntityStats.get(reportIdName).get("notDueYet")+1);
							}else if( sourceEntityDetails.get(entitySolution).get("failed")>0) {
								reportEntityStats.get(reportIdName).put("failed",
										reportEntityStats.get(reportIdName).get("failed")+1);
							
							}
						}
					}
					HashMap<String,Integer> statsValue = reportEntityStats.get(reportIdName);
					for(Map.Entry<String,HashMap<String,Integer>> entry1 : reportEntityStats.entrySet()){
						reportLineItemMap.put(entry1.getKey().split(SEPARATOR)[0], "");
					}
					ReportStatusPayload reportsInfo ;
					try{
						reportsInfo= SolutionReportStatus.getStatusForReportForSolutions(reportLineItemMap, sol.getKey(), currrentBusinessDate,solutions.get(sol.getKey()).toString(),clientCode);
					} catch (Throwable e){
						LOGGER.error(e.getMessage());
						LOGGER.info("REPORT STATUS NOT AVAILABLE");
						reportsInfo= null;
					}

					Map<String,String> reportstatus=new HashMap<>();
					Map<String,String>	reportTimeStamp= new HashMap<>();
					if(reportsInfo!=null){
						for(ReportInfo info :reportsInfo.getReportInfo()){
							if(info.getStamp()!=null && !"null".equalsIgnoreCase(info.getStamp())){
								reportTimeStamp.put(info.getRegReportId(), new DateTime(info.getStamp()).toString());
								if(info.getStatus()!=null && !"null".equalsIgnoreCase(info.getStamp()))
									reportstatus.put(info.getRegReportId(), info.getStatus());
								else 
									reportstatus.put(info.getRegReportId(), "NA");
							}

							else {
								reportTimeStamp.put(info.getRegReportId(), "NA");
								reportstatus.put(info.getRegReportId(), "NA");
							}

						}
					} else {
						for(Map.Entry<String,HashMap<String,Integer>> entry1 : reportEntityStats.entrySet()){
							reportTimeStamp.put(entry1.getKey().split(SEPARATOR)[0], "NA");
							reportstatus.put(entry1.getKey().split(SEPARATOR)[0], "NA");
						}
					}
					for(String entityName : value){

						if(!(statsValue.get("completed")==0 && statsValue.get("partiallyCompleted")==0 && 
								statsValue.get("notDueYet")==0 && statsValue.get("overDue")==0)){
							List<String> sourceListForConsumers = new ArrayList<String>();
							String entitySolution = entityName;
							sourceListForConsumers=getSourceFromConsumerEntity(entitySolution, sourceTargetWithOwners,
									allSourceEntityWithOwner, new ArrayList<String>(), new ArrayList<String>());
							for(String sourceAndOwnerNameAfterProcessing :sourceListForConsumers){
								DldStatusDownload reportWithoutLineItemObj = new DldStatusDownload();
								reportWithoutLineItemObj.setSolutionName(sol.getKey());
								reportWithoutLineItemObj.setConsumerType("Report");
								reportWithoutLineItemObj.setConsumerName(reportName+" ["+reportId+"]");
								reportWithoutLineItemObj.setSubItemName("");
								reportWithoutLineItemObj.setDataAvailabilityStatus("");
								if(statsValue.get("completed")>0 && statsValue.get("partiallyCompleted")==0 && 
										statsValue.get("notDueYet")==0 && statsValue.get("overDue")==0){
									reportWithoutLineItemObj.setDataAvailabilityStatus(COMPLETED);
								}else if(statsValue.get("overDue")>0){
									reportWithoutLineItemObj.setDataAvailabilityStatus("OVERDUE");
								}else if(statsValue.get("partiallyCompleted")>0){
									reportWithoutLineItemObj.setDataAvailabilityStatus("PARTIALLY COMPLETED");
								}else if(statsValue.get("notDueYet")>0){
									reportWithoutLineItemObj.setDataAvailabilityStatus("NOT DUE YET");
								}else if(statsValue.get("failed")>0){
									reportWithoutLineItemObj.setDataAvailabilityStatus("FAILED");
								}
								if(dueDates.size()>0){
									reportWithoutLineItemObj.setDataAvailabilityDueDate(Collections.max(dueDates).toString());
								}else{
									reportWithoutLineItemObj.setDataAvailabilityDueDate("");
								}

								if(completedDates.size()>0){
									reportWithoutLineItemObj.setDataAvailabilityCompletionDate(Collections.max(completedDates).toString());
								}else{
									reportWithoutLineItemObj.setDataAvailabilityCompletionDate("");
								}
								reportWithoutLineItemObj.setDataProcessingEndDueDate(reportTimeStamp.get(reportId.toString()));
								reportWithoutLineItemObj.setDataProcessingLastProcessedDate(reportTimeStamp.get(reportId.toString()));		
								reportWithoutLineItemObj.setConsumerEntityName(entityName.split(SEPARATOR)[0]);
								reportWithoutLineItemObj.setConsumerRepository(sol.getKey());
								reportWithoutLineItemObj.setConsumerStatus(entityAndStatusMap.get(entityName));
								reportWithoutLineItemObj.setSourceEntity(sourceAndOwnerNameAfterProcessing.split(SEPARATOR)[0]);
								reportWithoutLineItemObj.setSourceSystem(sourceAndOwnerNameAfterProcessing.split(SEPARATOR)[1]);
								reportWithoutLineItemObj.setSourceStatus(entityAndStatusMap.get(sourceAndOwnerNameAfterProcessing));
								objList.add(reportWithoutLineItemObj);	
							}
						}
					}

				}else {
					//for Reports with line Items.
					//ReportIdSolutionIdSolutionName.put(reportIdName.split(SEPARATOR)[0]+sol.getValue(), sol.getKey());

					lineItemIdEntityNameMap = new HashMap<String,Set<String>>();
					lineItemIdEntityNameMap=dldDao.getLineItemAllEntityMap(businessDateRec, sol.getValue(), Integer.parseInt(reportId), sol.getKey(), clientCode);

					String lineItemIDsCSV="";
					for(Map.Entry<String,Set<String>> entry1 : lineItemIdEntityNameMap.entrySet()){
						lineItemIDsCSV=lineItemIDsCSV+entry1.getKey()+",";
					}

					reportLineItemMap.put(reportId.toString(),lineItemIDsCSV);
					ReportStatusPayload reportsInfo ;
					try{
						reportsInfo= SolutionReportStatus.getStatusForReportForSolutions(reportLineItemMap, solutionName, currrentBusinessDate,solutions.get(solutionName).toString(),clientCode);
					} catch (Throwable e){
						LOGGER.info("REPORT STATUS NOT AVAILABLE");
						reportsInfo= null;
					}

					Map<String,String> lineItemStatus=new HashMap<>();
					Map<String,String> lineItemTimeStamp= new HashMap<>();
					if(reportsInfo!=null){
						for(LineItemInfo info :reportsInfo.getReportInfo().get(0).getLineitems()){
							if(info.getStamp()!=null && !"null".equalsIgnoreCase(info.getStamp())){
								lineItemTimeStamp.put(info.getLineItemId(), new DateTime(info.getStamp()).toString());
								if(info.getStatus()!=null && !"null".equalsIgnoreCase(info.getStatus()))
									lineItemStatus.put(info.getLineItemId(),info.getStatus());
								else
									lineItemStatus.put(info.getLineItemId(), "NA");
							}
							else {
								lineItemTimeStamp.put(info.getLineItemId().split(SEPARATOR)[0], "NA");
								lineItemStatus.put(info.getLineItemId(), "NA");

							}
						}
					} else {
						for(Map.Entry<String,Set<String>> entry1 : lineItemIdEntityNameMap.entrySet()){
							lineItemTimeStamp.put(entry1.getKey().split(SEPARATOR)[0], "NA");
							lineItemStatus.put(entry1.getKey().split(SEPARATOR)[0], "NA");
						}
					}
					for (Map.Entry<String,Set<String>> entry1 : lineItemIdEntityNameMap.entrySet()) {
						//looping through map of report and entity name to fetch stats.
						Set<Integer> dueDates = new HashSet<Integer>();
						Set<Integer> completedDates = new HashSet<Integer>();
						Set<String> entityOwnerSet = new HashSet<String>();
						Set<String> entityOwnerSetImmediate = new HashSet<String>();
						for(String eName:entry1.getValue()){
							if(Y.equalsIgnoreCase(entityAndTypeMap.get(eName+SEPARATOR+sol.getKey()))){
								entityOwnerSetImmediate.add(eName+SEPARATOR+sol.getKey());
							}else{
								entityOwnerSet.add(eName+SEPARATOR+sol.getKey());
							}
							
						}
						Set<String> underLyingExecEntities = getUnderLyingEntites(entityOwnerSet,clientCode,new HashSet<String>(),entityAndTypeMap);
						underLyingExecEntities.addAll(entityOwnerSetImmediate);
						Set<String> value = underLyingExecEntities;// entry1.getValue();
						lineItemEntityStats = new HashMap<String, HashMap<String,Integer>>();
						String lineItemIdName =  entry1.getKey();
						if(null==lineItemEntityStats.get(lineItemIdName)){
							statsMap = new HashMap<String, Integer>();
							statsMap.put("completed",0 );
							statsMap.put("notDueYet",0 );
							statsMap.put("partiallyCompleted",0 );
							statsMap.put("overDue",0 );
							statsMap.put("failed",0 );
							lineItemEntityStats.put(lineItemIdName, (HashMap<String, Integer>) statsMap);
						}
						for(String entityName : value){
							String entityNameSolution = entityName;
							//having all possible dates for each report.
							if(null!=taskDueMap.get(entityNameSolution)){
								dueDates.add(taskDueMap.get(entityNameSolution));
							}
							if(null!=taskCompletionMap.get(entityNameSolution)){
								completedDates.add(taskCompletionMap.get(entityNameSolution));
							}
							if(null!=sourceEntityDetails.get(entityNameSolution)){
								//checking the entity status for a report.
								if(sourceEntityDetails.get(entityNameSolution).get("completed")>0 && sourceEntityDetails.get(entityNameSolution).get("notDueYet")==0
										&& sourceEntityDetails.get(entityNameSolution).get("failed")==0 && sourceEntityDetails.get(entityNameSolution).get("overDue")==0){
									lineItemEntityStats.get(lineItemIdName).put("completed",
											lineItemEntityStats.get(lineItemIdName).get("completed")+1);
								}else if(sourceEntityDetails.get(entityNameSolution).get("overDue")>0 ){
									lineItemEntityStats.get(lineItemIdName).put("overDue",
											lineItemEntityStats.get(lineItemIdName).get("overDue")+1);
								}else if(sourceEntityDetails.get(entityNameSolution).get("completed")>0 && (sourceEntityDetails.get(entityNameSolution).get("notDueYet")>0
										 )){
									lineItemEntityStats.get(lineItemIdName).put("partiallyCompleted",
											lineItemEntityStats.get(lineItemIdName).get("partiallyCompleted")+1);
								}else if(sourceEntityDetails.get(entityNameSolution).get("completed")==0 && sourceEntityDetails.get(entityNameSolution).get("notDueYet")>0
										&& sourceEntityDetails.get(lineItemIdName).get("entityName")==0 && sourceEntityDetails.get(entityNameSolution).get("overDue")==0){
									lineItemEntityStats.get(lineItemIdName).put("notDueYet",
											lineItemEntityStats.get(lineItemIdName).get("notDueYet")+1);
								}else if( sourceEntityDetails.get(entityNameSolution).get("failed")>0) {
									lineItemEntityStats.get(lineItemIdName).put("failed",
											lineItemEntityStats.get(lineItemIdName).get("failed")+1);
								
								}
							}
						}
						if(dueDates.size()>0){
							reportDueMap.put(lineItemIdName, Collections.max(dueDates));
						}else{
							reportDueMap.put(lineItemIdName, 0);
						}
						if(completedDates.size()>0){
							reportCompletionMap.put(lineItemIdName,Collections.max(completedDates));	
						}else{
							reportCompletionMap.put(lineItemIdName,0);
						}
						for(String entityName : value){

							for (Map.Entry<String,HashMap<String,Integer>> entryForLI : lineItemEntityStats.entrySet()) {
								HashMap<String,Integer> valueStats =  entryForLI.getValue();
								if(!(valueStats.get("completed")==0 && valueStats.get("partiallyCompleted")==0 && valueStats.get("notDueYet")==0 && valueStats.get("overDue")==0)){
									Integer lineItemId = Integer.parseInt(entryForLI.getKey().split(SEPARATOR)[0]);
									List<String> sourceListForConsumers = new ArrayList<String>();
									String entitySolution = entityName;
									sourceListForConsumers=getSourceFromConsumerEntity(entitySolution, sourceTargetWithOwners,
											allSourceEntityWithOwner, new ArrayList<String>(), new ArrayList<String>());

									for(String sourceAndOwnerNameAfterProcessing :sourceListForConsumers){
										String lineItemName = entryForLI.getKey().split(SEPARATOR)[1];
										DldStatusDownload reportWithLi = new DldStatusDownload();
										reportWithLi.setSolutionName(sol.getKey());
										reportWithLi.setConsumerType("Report");
										reportWithLi.setConsumerName(reportName+" ["+reportId+"]");
										reportWithLi.setSubItemName(lineItemName+" ["+lineItemId+"]");
										if(valueStats.get("completed")>0 && valueStats.get("partiallyCompleted")==0 && valueStats.get("notDueYet")==0 && valueStats.get("overDue")==0){
											reportWithLi.setDataAvailabilityStatus(COMPLETED);
										}else if(valueStats.get("overDue")>0){
											reportWithLi.setDataAvailabilityStatus("OVERDUE");
										}else if(valueStats.get("partiallyCompleted")>0){
											reportWithLi.setDataAvailabilityStatus("PARTIALLY COMPLETED");
										}else if(valueStats.get("failed")>0){
											reportWithLi.setDataAvailabilityStatus("FAILED");
										}else{
											reportWithLi.setDataAvailabilityStatus("NOT DUE YET");
										}
										reportWithLi.setDataAvailabilityDueDate(reportDueMap.get(entryForLI.getKey())==0?"":reportDueMap.get(entryForLI.getKey()).toString());
										reportWithLi.setDataAvailabilityCompletionDate(reportCompletionMap.get(entryForLI.getKey())==0?"":reportCompletionMap.get(entryForLI.getKey()).toString());
										reportWithLi.setDataProcessingEndDueDate(lineItemTimeStamp.get(lineItemId.toString()));
										reportWithLi.setDataProcessingLastProcessedDate(lineItemTimeStamp.get(lineItemId.toString()));
										reportWithLi.setConsumerEntityName(entityName.split(SEPARATOR)[0]);
										reportWithLi.setConsumerRepository(sol.getKey());
										reportWithLi.setConsumerStatus(entityAndStatusMap.get(entityName));
										reportWithLi.setSourceEntity(sourceAndOwnerNameAfterProcessing.split(SEPARATOR)[0]);
										reportWithLi.setSourceSystem(sourceAndOwnerNameAfterProcessing.split(SEPARATOR)[1]);
										reportWithLi.setSourceStatus(entityAndStatusMap.get(sourceAndOwnerNameAfterProcessing));
										objList.add(reportWithLi);
									}
								}
							}
						}
					}
				}
			}
		}
		return objList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getLineageForLineItems(String businessDate,String clientCode,Integer reportId,Integer pageNo,Integer pageSize,String solutionName) throws Throwable{
		DateTime businessDateRec = DateTime.parse(businessDate, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
		JSONObject lineageDetails = new JSONObject();
		Map<String,Integer> allSolutions=new HashMap<>();
		allSolutions=dldDao.getListOfSolutionNames(clientCode);
		DldSolution sol= new DldSolution();
		JSONObject task;
		JSONArray taskArray = new JSONArray();
		JSONArray entityArray = new JSONArray();
		JSONObject entityObj;
		List<StagingDetails> totalStagingObject = new ArrayList<>();
		List<StagingDetails> tempTotalStagingObjects= new ArrayList<>();
		for(Map.Entry<String, Integer> sols:allSolutions.entrySet()){
			if(sols.getKey().equalsIgnoreCase(solutionName)){
				sol.setSolutionID(sols.getValue());
				sol.setSolutionName(sols.getKey());
			}
		}
		List<TaskFrequencyDetail> allTaskApplicable = getApplicableTaskForLineage(businessDate, "", "", N, N, clientCode);
		Set<String> applicableFreq= new HashSet<>();
		for(TaskFrequencyDetail fd:allTaskApplicable){
			applicableFreq.add(fd.getFrequencyType());
		}
		String frequency="";
		for(String f:applicableFreq){
			frequency=frequency+"'"+f.toUpperCase()+"',";
		}
		if(frequency.length()>0){
			frequency=frequency.substring(0, frequency.length()-1);
		}

		Map<String,Set<String>> lineItemIdEntityNameMap=dldDao.getLineItemAllEntityMap(businessDateRec, sol.getSolutionID(), reportId, sol.getSolutionName(), clientCode);
		Set<String> allFactForLineItems=new HashSet<>();
		totalStagingObject = dldDao.getTaskStatusDetails(businessDateRec,frequency,clientCode);
		Map<String,String> entityAndTypeMap = dldDao.getEntityAndEntityTaskTypeMap(businessDateRec, Y , frequency, N, EMPTY_QUOTES, N,clientCode);
		Set<String> nonExecutableEntities =new HashSet<>(); 
		Set<String> basefact =new HashSet<>(); 
		for(Map.Entry<String, Set<String>> lineItem:lineItemIdEntityNameMap.entrySet()){
			entityObj= new JSONObject();

			entityObj.put("entityName", lineItem.getKey().split("@##@")[1]+" ["+lineItem.getKey().split("@##@")[0]+"]");
			entityObj.put("entityDetail", lineItem.getKey().split("@##@")[1]);
			entityObj.put("entityDesc", lineItem.getKey().split("@##@")[1]);
			entityObj.put("entityOwner", sol.getSolutionName());
			entityObj.put("isSourceSystem", N);
			entityObj.put("isDataRepo", N);
			entityObj.put("isDataConsumer", Y);
			entityObj.put("isStageArea", N);
			entityObj.put("isLineItemDataRequired", N);
			entityArray.add(entityObj);

			for(String lineItemEntities:lineItem.getValue()){
				allFactForLineItems.add(lineItemEntities+"#"+solutionName);
				task=new JSONObject();
				task.put("taskName","Load Data For " +lineItem.getKey().split("@##@")[1]);
				task.put("SOURCE", lineItemEntities);
				task.put("SOURCEOWNER", solutionName);
				task.put("TARGET", lineItem.getKey().split("@##@")[1]+" ["+lineItem.getKey().split("@##@")[0]+"]");
				task.put("TARGETOWNER", solutionName);
				if(entityAndTypeMap.containsKey(lineItemEntities+SEPARATOR+solutionName)){
					if("N".equalsIgnoreCase(entityAndTypeMap.get(lineItemEntities+SEPARATOR+solutionName))){
						nonExecutableEntities.add(lineItemEntities+SEPARATOR+solutionName);
						basefact=getUnderLyingEntites(nonExecutableEntities, clientCode, new HashSet<>(), entityAndTypeMap);
						for(String bf:basefact){
							String srcEntityName=bf.split(SEPARATOR)[0];
							String srcOwnerName=bf.split(SEPARATOR)[1];
							tempTotalStagingObjects=totalStagingObject.stream().filter(col->col.getEntityName()!=null && col.getOwnerName()!=null).filter(
									col->col.getEntityName().equalsIgnoreCase(srcEntityName)
									&&col.getOwnerName().equalsIgnoreCase(srcOwnerName)
									&&col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
							if(tempTotalStagingObjects.size()>0){
								for(StagingDetails sd:tempTotalStagingObjects){
									if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("failed")){
										task.put("STATUS", "FAILED");
										break;
									} else if(sd.getTaskStatus()!=null && sd.getTaskStatus().equalsIgnoreCase("completed")){
										task.put("STATUS", "COMPLETED");
									}
								}
							} else {
								task.put("STATUS", "PENDING");
							}
						}
					}
				}
				//task.put("lineItemId", lineItem.getKey().split("@##@")[0]);
				taskArray.add(task);
			} 

		}
		Map<String,JSONObject> allTask= new HashMap<>();
		allTask = getAllSourceForLineItemEntities(allFactForLineItems, clientCode, allTask, allTaskApplicable, businessDateRec);
		Set<String> allRelevantEntities=new HashSet<>();
		
		for(Map.Entry<String, JSONObject> allOtherTask:allTask.entrySet()){
			taskArray.add(allOtherTask.getValue());
			allRelevantEntities.add(allOtherTask.getValue().get("SOURCE")+"#"+allOtherTask.getValue().get("SOURCEOWNER"));
			allRelevantEntities.add(allOtherTask.getValue().get("TARGET")+"#"+allOtherTask.getValue().get("TARGETOWNER"));
		}
		List<DlEntity> entityList = dldDao.getEntityMasterData(clientCode);

		List<DlEntityOwner> entityOwner =dldDao.getEntityOwnerData(clientCode);

		Map<String,String> entityOwnerNameIsExtSrcMap = new HashMap<>();

		Map<String,String> entityOwnerIsStageArea = new HashMap<>();
		JSONObject ownerDisplayOrder;
		JSONArray ownerDisplayOrderArray = new JSONArray();

		for(DlEntityOwner eo:entityOwner){
			ownerDisplayOrder=new JSONObject();
			entityOwnerNameIsExtSrcMap.put(eo.getOwner_Name(), eo.getExternal_Source());

			if("N".equalsIgnoreCase(eo.getExternal_Source())&& eo.getSolution_Id()==null){
				entityOwnerIsStageArea.put(eo.getOwner_Name(), Y);
			}

			if("N".equalsIgnoreCase(eo.getExternal_Source())){
				ownerDisplayOrder.put("ownerName", eo.getOwner_Name());
				ownerDisplayOrder.put("displayOrder", eo.getDisplay_Sorting_Order());
				ownerDisplayOrderArray.add(ownerDisplayOrder);
			}
		}
		DlEntity entity;
		for(String entityDetail:allRelevantEntities){
			String entityName=entityDetail.split("#")[0];
			String entityOwnerName=entityDetail.split("#")[1];
			entityObj= new JSONObject();
			if(entityList.stream().filter(col->col.getClientCode().equalsIgnoreCase(clientCode)
					&& col.getEntityName().equalsIgnoreCase(entityName) && col.getOwnerName().equalsIgnoreCase(entityOwnerName)).collect(Collectors.toList()).size()>0){
				entity=entityList.stream().filter(col->col.getClientCode().equalsIgnoreCase(clientCode)
						&& col.getEntityName().equalsIgnoreCase(entityName) && col.getOwnerName().equalsIgnoreCase(entityOwnerName)).collect(Collectors.toList()).get(0);
				entityObj.put("entityName", entity.getEntityName());
				entityObj.put("entityDetail", entity.getEntityDetail());
				entityObj.put("entityDesc", entity.getDescription());
				entityObj.put("entityOwner", entity.getOwnerName());
				if(Y.equalsIgnoreCase(entityOwnerNameIsExtSrcMap.get(entity.getOwnerName()))){
					entityObj.put("isSourceSystem", Y);
					entityObj.put("isDataRepo", N);
					entityObj.put("isStageArea", N);
				}else{
					if(Y.equalsIgnoreCase(entityOwnerIsStageArea.get(entity.getOwnerName()))){
						entityObj.put("isStageArea", Y);	
					}else {
						entityObj.put("isStageArea", N);
					}
					entityObj.put("isSourceSystem", N);
					entityObj.put("isDataRepo", Y);
				}

				entityObj.put("isDataConsumer", N);

				entityArray.add(entityObj);
			}
			
		}
		
		lineageDetails.put("entities", entityArray);
		lineageDetails.put("task", taskArray);
		lineageDetails.put("displayOrders", ownerDisplayOrderArray);
		return lineageDetails;
	}
	
	private List<String> getSourceFromConsumerEntity(String consumerEntity,
			Multimap<String,String> sourceTgtMap,Set<String> sourceList,List<String> pendingList,
			List<String> completedList){

		Multimap<String,String> sourceTargetWithOwners = sourceTgtMap;
		List<String> completedFinalArray = new ArrayList<String>();
		Multimap<String, String> invertedMultimap = Multimaps.invertFrom(sourceTargetWithOwners, ArrayListMultimap.<String, String>create());
		Set<String> previousParentArr = new HashSet<String>(invertedMultimap.get(consumerEntity));
		pendingList.remove(consumerEntity);
		for(String prevParent:previousParentArr){
			if(sourceList.contains(prevParent)){
				completedList.add(prevParent);
			}else{
				pendingList.add(prevParent);
			}
		}
		pendingList.removeAll(completedList);
		if(pendingList.size()>0){
			return getSourceFromConsumerEntity(pendingList.get(0), sourceTgtMap, sourceList, pendingList, completedList);
		}else{
			return completedList;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private Map<String,JSONObject> getAllSourceForLineItemEntities(Set<String> entities,String clientCode,
			Map<String,JSONObject> allTask,List<TaskFrequencyDetail> allTaskApplicable,DateTime businessDateRec) throws Throwable{
		
		Iterable<Map<String,Object>> taskDetails;
		Iterator<Map<String,Object>> taskItr;
		Set<String> allEntities= new HashSet<>();
		JSONObject task;
		String status;
		Map<String,Object> tempTaskDetails = new HashMap<>();
		for(String entityName:entities){
			taskDetails=dldEntityRepository.getSourceForAEntity(clientCode, entityName.split("#")[0], entityName.split("#")[1]);
			taskItr=taskDetails.iterator();
			while(taskItr.hasNext()){
				tempTaskDetails=taskItr.next();
				String taskName=(String)tempTaskDetails.get("taskName");
				String taskRepo=(String)tempTaskDetails.get("taskRepo");
				task=new JSONObject();
				task.put("taskName", tempTaskDetails.get("taskName"));
				task.put("SOURCE", tempTaskDetails.get("SOURCE"));
				task.put("SOURCEOWNER", tempTaskDetails.get("SOURCEOWNER"));
				task.put("TARGET", tempTaskDetails.get("TARGET"));
				task.put("TARGETOWNER", tempTaskDetails.get("TARGETOWNER"));
				if(allTaskApplicable.stream().filter(col->col.getTaskName().equalsIgnoreCase(taskName)&&col.getTaskRepository().equalsIgnoreCase(taskRepo)).collect(Collectors.toList()).size()>0){
					if(N.equalsIgnoreCase(dldDao.isTaskExecutable((String)tempTaskDetails.get("taskName"),clientCode))){
						task.put("STATUS", "COMPLETED");
					} else {
						status=dldDao.getTaskStatusForDataLinage(clientCode,(String) tempTaskDetails.get("taskType"),
								( String)tempTaskDetails.get("taskRepo"), new Date(businessDateRec.getMillis()));
	
						task.put("STATUS", status);
					}
				} else {
						task.put("STATUS", "Not Applicable");
				}
				if(!allTask.containsKey((String)tempTaskDetails.get("taskName")+(String)tempTaskDetails.get("SOURCE")+(String)tempTaskDetails.get("SOURCEOWNER")
						+ (String)tempTaskDetails.get("TARGET")+(String)tempTaskDetails.get("TARGETOWNER"))){
					allTask.put((String)tempTaskDetails.get("taskName")+(String)tempTaskDetails.get("SOURCE")+(String)tempTaskDetails.get("SOURCEOWNER")
							+ (String)tempTaskDetails.get("TARGET")+(String)tempTaskDetails.get("TARGETOWNER"), task);
				}
				allEntities.add((String)tempTaskDetails.get("SOURCE")+"#"+(String)tempTaskDetails.get("SOURCEOWNER"));
			}
		}
		
		entities.removeAll(entities);
		entities.addAll(allEntities);
		if(entities.size()>0){
			return getAllSourceForLineItemEntities(entities, clientCode, allTask, allTaskApplicable, businessDateRec);
		} else {
			return allTask;	
		}
		
	}
	
	public List<TaskFrequencyDetail> getApplicableTaskForLineage(String cbd, String frequencyFilterCSV, String flowFilterCSV,
			String isFrequencyFilterApplied, String isFlowFilterApplied,String clientCode) throws Throwable {
		LOGGER.info("EXEFLOW-DldBoImpl -- > getListOfTaskMaster");
		try{

			DateTime businessDateRec = DateTime.parse(cbd, DateTimeFormat.forPattern(PERIOD_ID_DATE_FORMAT));
			Set<String> freqApplicableForcbd=new HashSet<String>();
			List<TaskFrequencyDetail> totalTasks = new ArrayList<TaskFrequencyDetail>();
			List<TaskFrequencyDetail> totalTasksAfterFreqFilter = new ArrayList<TaskFrequencyDetail>();
			//getting total tasks.
			freqApplicableForcbd=dldDao.getFrequenciesApplicableForCbd(businessDateRec,isFrequencyFilterApplied,frequencyFilterCSV,clientCode);
			totalTasksAfterFreqFilter=dldDao.getTotalNumberOfApplicableTasksForLineage(freqApplicableForcbd,clientCode,businessDateRec);
			//task filter based on flow type.
			if(Y.equalsIgnoreCase(isFlowFilterApplied)){
				totalTasks = new ArrayList<TaskFrequencyDetail>();
				List<TaskFlowTypeDetail> flowTypeInFilter = dldDao.getTaskListBasedOnFilter(flowFilterCSV,clientCode);
				//retaining common tasks 
				for(TaskFrequencyDetail tf :totalTasksAfterFreqFilter){
					for(TaskFlowTypeDetail tfl :flowTypeInFilter){
						if(tfl.getTaskName().equals(tf.getTaskName()) && (tf.getVersionNo().equals(tfl.getVersionNo()))){
							totalTasks.add(tf);
						}
					}
				}
			}else{
				totalTasks = totalTasksAfterFreqFilter;
			}
			return totalTasks;		
		}
		catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	
}



