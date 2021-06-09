package com.fintellix.dld.dao;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.fintellix.dld.models.ClientUploaderDTO;
import com.fintellix.dld.models.DataSource;
import com.fintellix.dld.models.DldSolution;
import com.fintellix.dld.models.DlEntity;
import com.fintellix.dld.models.DlEntityOwner;
import com.fintellix.dld.models.DlFlowType;
import com.fintellix.dld.models.StagingDetails;
import com.fintellix.dld.models.TaskExecutionLog;
import com.fintellix.dld.models.DlTaskFlowType;
import com.fintellix.dld.models.TaskFlowTypeDetail;
import com.fintellix.dld.models.DlTaskFrequency;
import com.fintellix.dld.models.TaskFrequencyDetail;
import com.fintellix.dld.models.DlTaskMaster;
import com.fintellix.dld.models.DlTaskRepository;
import com.fintellix.dld.models.DlTaskSourceTarget;
import com.fintellix.dld.models.UserDetail;

@Component
public interface DldDao {
	public UserDetail loadUserByUsername(String username) throws Throwable;
	public  List<TaskFrequencyDetail> getTotalNumberOfTasks(Set<String> frequencyTypeName,String clientCode,DateTime dt)throws Throwable;
	public  Set<String> getFrequenciesApplicableForCbd(DateTime cbd, String isFreqFilterApplied, String freqFilterCSV,String clientCode)throws Throwable;
	public  Integer getCompletedTasks(DateTime dt,String clientCode,Set<String> freqType)throws Throwable;
	public  Integer getFailedTasks(DateTime dt,String clientCode,Set<String> freqType)throws Throwable;
	public  List<TaskExecutionLog> getTasksByCbdandStatus(DateTime dt,String status, String isFlowFilterApplied, String flowTypeCSV,String clientCode,Set<String> freqType)throws Throwable;
	public  String getPreviousBusinessDate(DateTime dt,String clientCode) throws Throwable;
	public  String getMaxBusinessDate(String clientCode) throws Throwable;
	public  String getPreviousPendingDate(DateTime dt,String clientCode)throws Throwable;
	public void insertTaskStatics(List<TaskExecutionLog> taskExeccutionLog)throws Throwable ;
	public void putUploadedFileToCache(String userTokenSession,Map<String,Object> uploadDTOMap) throws Throwable;
	public Map<String, Object> getUploadedFileFromCache(String userTokenSession) throws Throwable;
	public Set<String> getDistinctApplicableFlowTypesByTask(List<String> taskNames,String clientCode);
	public List<TaskFlowTypeDetail> getTaskListBasedOnFilter(String flowFilterCSV,String clientCode);
	public List<DldSolution> getAllSoutions(String clientCode) throws Throwable;
	public List<DlTaskMaster> getTaskList(String taskInCls);
	public List<DataSource> getAllDataSource(String clientCode) throws Throwable;
	public List<String> getLoadSourceSystemDetailsCompleted(String taskInCls);
	public List<StagingDetails> getStagingDetails(DateTime cbd, String isFreqFilterApplied, String freqFilterCSV,String isFlowFilterApplied, String flowTypeCSV,String isDataSource,String clientCode)throws Throwable;
	public void logAllTask(String params,String requestOrigin) throws Throwable;
	public DlTaskMaster getTaskMaster(String clientCode, String taskRepository, String taskName,Date date) throws Throwable;
	public void deactivateDataForTaskMaster(List<DlTaskMaster> deactivateTaskList) throws Throwable;
	public void updateDataForTaskMaster(List<DlTaskMaster> updateTaskList) throws Throwable;
	public List<DlTaskRepository> gettaskrepositoriesData(String clientCode,java.sql.Date effectiveDate) throws Throwable;
	public List<DlFlowType> getFlowTypesData(String clientCode,java.sql.Date effectiveDate) throws Throwable;
	public List<DlEntity> getEntityMasterData(String clientCode) throws Throwable;
	public List<DlEntityOwner> getEntityOwnerData(String clientCode) throws Throwable;
	public List<DlTaskMaster> gettaskData(String clientCode,java.sql.Date effDate) throws Throwable;
	public List<DlTaskFlowType> getFlowData(String clientCode,String RepositoryName,String taskName,Integer versionNo) throws Throwable;
	public List<DlTaskFrequency> getFeqData(String clientCode,String RepositoryName,String taskName,Integer versionNo) throws Throwable;
	public List<DlTaskSourceTarget> getSourceTargetData(String clientCode,String RepositoryName,String taskName,Integer versionNo) throws Throwable;
	public Map<String,Integer> getListOfSolutionNames(String clientCode) throws Throwable;
	public Map<Integer,Set<String>> getListOfRuleIds(DateTime cbd,String solutionName,Integer solutionId,String clientCode) throws Throwable;
	public List<StagingDetails> getSourceSystemSummaryForGrid(DateTime cbd,String isFreqFilterApplied, String freqFilterCSV,String isFlowFilterApplied, String flowTypeCSV,String summaryType,String clientCode) throws Throwable;
	public List<StagingDetails> getUnplannedTaskDetails(DateTime dt,String clientCode) throws Throwable;
	public List<TaskExecutionLog> getSubGridTaskDetails(DateTime businessDateRec,String taskName, String repoName,String clientCode) throws Throwable;
	Map<String, Set<String>> getReportAndEntityMap(DateTime cbd,
			String solutionName,Integer solutionId,String clientCode) throws Throwable;
	public Map<String, String> getReportAndLineItemFlag(Integer solId,String solutionName,String clientCode) throws Throwable;
	public Map<String, Set<String>> getLineItemEntityMap(
			DateTime businessDateRec, DldSolution sol, Integer reportId,Integer pageNo,
			Integer pageSize,String lineItemIdSearch,String lineItemDescSearch,String clientCode) throws Throwable;
	public Integer getMaxVersionNoForTask(String clientCode,
			String task_Repository, String task_Name) throws Throwable;
	public List<DlTaskMaster> getTaskForEdit(String clientCode,
			String task_Repository, String task_Name) throws Throwable;
	public DlTaskRepository getTaskRepository(String clientCode, String name) throws Throwable;
	public DlFlowType getDlFlowType(String clientCode, String flow_type) throws Throwable;
	public DlEntity getEntityMaster(String clientCode, String owner_Name,
			String entity_Name) throws Throwable;
	public DlEntityOwner getEntityOwner(String clientCode, String owner_Name) throws Throwable;
	public ClientUploaderDTO getClientMaster(String clientCode) throws Throwable;
	public Boolean saveUploaderData(List<DlEntityOwner> updateEntityOwnerList, List<DlEntityOwner> insertEntityOwnerList,
			List<DlEntity> updateEntityMasterList, List<DlEntity> insertEntityMasterList,
			List<DlFlowType> updateFlowTypeList, List<DlFlowType> insertFlowTypeList,
			List<DlTaskRepository> updateTaskRepList, List<DlTaskRepository> insertTaskRepMasterList,
			List<DlTaskMaster> tMasterList, List<DlTaskFlowType> taskFlowTypeList,
			List<DlTaskFrequency> taskFrequencyList, List<DlTaskSourceTarget> taskSourceTargetList,
			List<DlTaskMaster> updateTaskList, List<DlTaskMaster> deactivateTaskList) throws Throwable;
	public Integer getLineItemTotalCount(DateTime businessDateRec,DldSolution sol, Integer reportId
			,String lineItemIdSearch,String lineItemDescSearch,String clientCode) throws Throwable ;
	public List<String> getAllTaskType(String clientCode) throws Throwable;
	public List<String> getAllentityType(String clientCode) throws Throwable;
	public List<DlTaskSourceTarget> getDependentTask(String clientCode,String repositryName,Integer versionNo,String taskName) throws Throwable;
	public boolean isValidationRequiredOnTask(String clientCode, String taskName, String taskRepo, Date businessDate)
			throws Throwable;
	public String getTaskStatusForDataLinage(String clientCode, String taskName, String taskRepo, Date businessDate)
			throws Throwable;
	public List<DldSolution> getExcludedSolution(String solutionName)throws Throwable;
	public String isTaskExecutable(String taskName,String clientCode) throws Throwable;
	public Set<String> getAllFrequecy(String clientCode) throws Throwable;
	Map<String, Set<String>> getLineItemAllEntityMap(DateTime businessDateRec,
			Integer solutionId, Integer reportId, String solutionName,
			String clientCode) throws Throwable;
	Map<String,String> getAllActualNameFrequency(String clientCode) throws Throwable;
	List<TaskFrequencyDetail> getTotalNumberOfApplicableTasksForLineage(Set<String> freqType, String clientCode,
			DateTime dt) throws Throwable;
	public Map<String, String> getEntityAndEntityTaskTypeMap(DateTime cbd, String isFreqFilterApplied, String freqFilterCSV,
			String isFlowFilterApplied, String flowTypeCSV, String summaryType,String clientCode) throws Throwable;
	List<StagingDetails> getTaskStatusDetails(DateTime cbd,String freqFilterCSV,String clientCode) throws Throwable;
}
	

