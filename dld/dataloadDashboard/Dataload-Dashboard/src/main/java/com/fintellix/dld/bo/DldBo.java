package com.fintellix.dld.bo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.aspose.cells.Workbook;
import com.fintellix.dld.models.DlTaskMaster;
import com.fintellix.dld.models.DlTaskSourceTarget;
import com.fintellix.dld.models.DldStatusDownload;
import com.fintellix.dld.models.EntityMasterUploaderDTO;
import com.fintellix.dld.models.EntityOwnerUploaderDTO;
import com.fintellix.dld.models.ErrorLogForUploader;
import com.fintellix.dld.models.FlowTypesUploaderDTO;
import com.fintellix.dld.models.TaskEntityDetailUploaderDTO;
import com.fintellix.dld.models.TaskExecutionLog;
import com.fintellix.dld.models.TaskFrequencyDetail;
import com.fintellix.dld.models.TaskMasterUploaderDTO;
import com.fintellix.dld.models.TaskRepositoriesUploaderDTO;

@Component
public interface DldBo {
	public JSONObject getStatsOnChange(String currrentBusinessDate,String clientCode)throws Throwable;
	public Map<String,Object> getStatisticsForGivenDate(String businessDate,
			String isMaxBusinessDate,String clientCode)throws Throwable;
	public String getMaxBusinessDate(String cleintCode) throws Throwable;
	public String getPreviousBusinessDate(String currrentBusinessDate,String clientCode)
			throws Throwable;
	public String getPreviousPendingDate(String currrentBusinessDate,String clientCode)
			throws Throwable;
	
	public JSONArray getLoadActualProgressDetails(String currrentBusinessDate,
			String isFlowFilterApplied, String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String clientCode);
	public void insertTaskStatics(List<TaskExecutionLog> taskExecutionLog)throws Throwable ;
	public List<TaskFrequencyDetail> getListOfTaskMaster(String cbd,String freqFilter,
			String flowFilters,String freqFilterApplied,String flowFilterApplied,String clientCode) throws Throwable ;
	public JSONObject getStagingDetailsForSourceSystems(
			String currrentBusinessDate, String isFlowFilterApplied,
			String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String clientCode);
	public JSONObject getStagingDetailsForDataRepository(
			String currrentBusinessDate, String isFlowFilterApplied,
			String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV,String clientCode);
	public void logAllTask(String params,String requestOrigin)throws Throwable;
	public void putUploadedFileToCache(String userTokenSession,Map<String,Object> uploadDTOMap) throws Throwable;
	public Map<String, Object> getUploadedFileFromCache(String userTokenSession) throws Throwable;
	public List<ErrorLogForUploader> validationForEntityOwner(List<EntityOwnerUploaderDTO> entityownerUploadList,String effectiveDate,String clientCode) throws Throwable;
	public List<ErrorLogForUploader> validationForEntityMaster(List<EntityMasterUploaderDTO> enitityMasterUploaderlist,List<EntityOwnerUploaderDTO> entityownerUploadList, String effectiveDate, String clientCode) throws Throwable;
	public List<ErrorLogForUploader> validationForFlowTypes(List<FlowTypesUploaderDTO> flowTypesList, String effectiveDate) throws Throwable;
	public List<ErrorLogForUploader> validationForTaskRepositories(List<TaskRepositoriesUploaderDTO> taskRepositoriesList,String effectiveDate) throws Throwable;
	public List<ErrorLogForUploader> validationForTaskMaster(String clientCode,List<TaskMasterUploaderDTO> taskMasterList, List<FlowTypesUploaderDTO> flowTypesList, List<EntityMasterUploaderDTO> enitityMasterUploaderlist, List<EntityOwnerUploaderDTO> entityownerUploadList, List<TaskEntityDetailUploaderDTO> taskEntityDetailList,List<TaskRepositoriesUploaderDTO> taskRepositoriesList,String effectiveDate)throws Throwable;
	public void generateValidationReport(Workbook workbook,	List<ErrorLogForUploader> masterErrorMsgList) throws Throwable;
	public boolean saveDataForUploader(String clientCode,String effectiveDate, String userTokenSession) throws Throwable;
	public Workbook getDataToDownload(String clientCode,String effectiveDate,Workbook workbook) throws Throwable;
	public JSONArray getDetailsForDataConsumers(String currrentBusinessDate,String isFrequencyFilterApplied,String frequencyFilterCSV,String isFlowFilterApplied,String flowFilterCSV,String clientCode)throws Throwable;
	public List<Object> validateMetaData(String clientCode,List<EntityMasterUploaderDTO> enitityMasterUploaderlist, List<EntityOwnerUploaderDTO> entityownerUploadList,List<FlowTypesUploaderDTO> flowTypesList, List<TaskMasterUploaderDTO> taskMasterList,
			List<TaskRepositoriesUploaderDTO> taskRepositoriesList, List<TaskEntityDetailUploaderDTO> taskEntityDetailList, String effectiveDate) throws Throwable;
	public JSONObject getSourceSystemsStagingSummmary(String currrentBusinessDate,
			String isFlowFilterApplied, String flowFilterCSV,
			String isFrequencyFilterApplied, String frequencyFilterCSV,
			String tabIndicator,String clientCode);
	public JSONObject getStagingSummaryForDataRepositoryGrid(
			String currrentBusinessDate, String isFlowFilterApplied,
			String flowFilterCSV, String isFrequencyFilterApplied,
			String frequencyFilterCSV, String tabIndicator,String clientCode);
	public JSONObject getUnplannedTaskDetails(String currrentBusinessDate,String clientCode);
	public JSONObject getSubGridTaskDetails(String currrentBusinessDate,String taskName, String repoName,String clientCode);
	public JSONObject getDetailsForDataConsumersSummary(String currrentBusinessDate,
			String isFrequencyFilterApplied, String frequencyFilterCSV,
			String isFlowFilterApplied, String flowFilterCSV,String clientCode,String solutionName) throws Throwable;
	public JSONObject getDetailsForDataConsumersSummaryLineItemGrid(
			String currrentBusinessDate, String isFrequencyFilterApplied,
			String frequencyFilterCSV, String isFlowFilterApplied,
			String flowFilterCSV,Integer reportId,Integer pageNo, Integer pageSize,String lineItemIdSearch,
			String lineItemDescSearch,String solutionName,Integer solutionId,String cleintCode)
			throws Throwable;
	public boolean isValidationOnTaskRequired(String clientCode,String taskName,String taskRepo,java.util.Date businessDate ) throws Throwable ;
	public List<Object>  getSheetForEntity(Workbook workbook);
	public List<String> getAllTaskType(String clientCode) throws Throwable;
	public List<String> getAllentityType(String clientCode) throws Throwable;
	public JSONObject getDataForLineage(String clientCode,String currrentBusinessDate);
	public DlTaskMaster getTaskDetails(String clientCode, String taskRepo, String taskName, Date effectiveDate)throws Throwable;
	public List<DlTaskSourceTarget> getTaskSourceTarget(String clientCode, String taskRepo, String taskName, Integer versionNo)throws Throwable;
	public JSONObject getLineageDataForBU(String clientCode,String currrentBusinessDate,String solutionName);
	public Workbook getLineageWorkBoook(List<DldStatusDownload> listOfDldStatusDownload,Workbook workbook) throws Throwable;
	public List<DldStatusDownload> getDownloadDetailsForSheetOne(String currrentBusinessDate,
			String clientCode, String solutionName) throws Throwable;
	public JSONObject getLineageForLineItems(String businessDate, String clientCode, Integer reportId, Integer pageNo,
			Integer pageSize, String solutionName) throws Throwable;

}
