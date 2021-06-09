package com.fintellix.dld.dao.daoImpl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fintellix.dld.dao.DldDao;
import com.fintellix.dld.dbConnection.PersistentStoreManager;
import com.fintellix.dld.models.ClientUploaderDTO;
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
import com.fintellix.dld.models.StagingDetails;
import com.fintellix.dld.models.TaskExecutionLog;
import com.fintellix.dld.models.TaskFlowTypeDetail;
import com.fintellix.dld.models.TaskFrequencyDetail;
import com.fintellix.dld.models.UserDetail;
import com.google.common.base.Joiner;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;

@Component
public class DldDaoImpl implements DldDao{

	
	private static final Logger LOGGER = LoggerFactory.getLogger(DldDaoImpl.class);
	//getting queries from prop file.
	private static Properties dataloadDashboardQuery;
	private static Properties applicationProperties;
	static{
		try {
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("dataloadDashboard-queries.properties");
			dataloadDashboardQuery = new Properties();
			dataloadDashboardQuery.load(is);
			
			
			InputStream is1  = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(is1);

		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application / data-dashboard-queries  properties from class path",e);
		}
	}

	//queries from the property file.
	private static final String LOGINUSERQUERY = dataloadDashboardQuery.getProperty("dld.loginUserQuery");
	private static final String APPLICABLEFREQUENCIES = dataloadDashboardQuery.getProperty("dld.applicableFrequencies");
	private static final String COMPLETEDTASKQUERY= dataloadDashboardQuery.getProperty("dld.completedTaskQuery");
	private static final String ALL_TASK_DETAILS_BY_CBD= dataloadDashboardQuery.getProperty("dld.allTaskByCBD");
	private static final String MAX_BUSINESS_DATE= dataloadDashboardQuery.getProperty("dld.getMaxBusinessDate");
	private static final String PREV_BUSINESS_DATE= dataloadDashboardQuery.getProperty("dld.getPreviousBusinessDate");
	private static final String PREV_PENDING_DATE= dataloadDashboardQuery.getProperty("dld.getPreviousPendingDate");
	private static final String TASK_QUERY= dataloadDashboardQuery.getProperty("dld.taskQuery");
	private static final String MAX_SEQ_NO=dataloadDashboardQuery.getProperty("dld.getMaxSeqNo");
	private static final String INSERT_TASK_STATICS=dataloadDashboardQuery.getProperty("dld.insertTaskStatics");
	private static final String GET_DISTINCT_FLOW_TYPE=dataloadDashboardQuery.getProperty("dld.getDistinctFlowType");
	private static final String TASK_FROM_FLOW_TYPE=dataloadDashboardQuery.getProperty("dld.getTaskListFromFlowType");
	private static final String TASK_FILTER_QUERRY=dataloadDashboardQuery.getProperty("dld.getTaskFilterQuerry");
	private static final String TOTAL_SOURCE_SYSTEMS_FOR_DATE=dataloadDashboardQuery.getProperty("dld.getTotalSourceSystemsForDate");
	private static final String STAGING_DETAILS_QUERRY=dataloadDashboardQuery.getProperty("dld.getStagingDetailsQuerry");
	private static final String LOG_ALL_TASK=dataloadDashboardQuery.getProperty("dld.logAllTask");
	private static final String INSERT_ENTITY_OWNER=dataloadDashboardQuery.getProperty("dld.insertEntityOwner");
	private static final String INSERT_ENTITY_MASTER=dataloadDashboardQuery.getProperty("dld.insertEntityMaster");
	private static final String INSERT_FLOW_TYPES=dataloadDashboardQuery.getProperty("dld.insertFlowTypes");
	private static final String INSERT_TASK_REP=dataloadDashboardQuery.getProperty("dld.insertTaskRepository");

	private static final String INSERT_TASK_MASTER=dataloadDashboardQuery.getProperty("dld.insertTaskMaster");
	private static final String INSERT_TASK_FLOW_TYPE=dataloadDashboardQuery.getProperty("dld.insertTaskFlowType");
	private static final String INSERT_TASK_FREQUENCY=dataloadDashboardQuery.getProperty("dld.insertTaskFrequency");
	private static final String INSERT_TASK_SOURCE_TARGET=dataloadDashboardQuery.getProperty("dld.insertTaskSourceTarget");
	private static final String GET_TASK_MASTER=dataloadDashboardQuery.getProperty("dld.getTaskMaster");

	private static final String GET_REP_DATA=dataloadDashboardQuery.getProperty("dld.getRepData");
	private static final String GET_FLOW_DATA=dataloadDashboardQuery.getProperty("dld.getFlowData");
	private static final String GET_ENTITY_MASTER_DATA=dataloadDashboardQuery.getProperty("dld.getEntitymasterData");
	private static final String GET_ENTITY_OWNER_DATA=dataloadDashboardQuery.getProperty("dld.getEntityOwnerData");

	
	private static final String GET_TASK_DATA=dataloadDashboardQuery.getProperty("dld.getTaskData");
	private static final String GET_ST_DATA=dataloadDashboardQuery.getProperty("dld.getSTData");
	private static final String GET_FL_DATA=dataloadDashboardQuery.getProperty("dld.getFLData");
	private static final String GET_FQ_DATA=dataloadDashboardQuery.getProperty("dld.getFQData");
	private static final String ALLFREQUENCIES=dataloadDashboardQuery.getProperty("dld.getAllFrequency");
	
	



	private static final String GET_CLIENT_MASTER=dataloadDashboardQuery.getProperty("dld.getClientMaster");
	private static final String GET_TASK_MAX_VERSION=dataloadDashboardQuery.getProperty("dld.getTaskMaxVersion");
	private static final String GET_ALL_TASK=dataloadDashboardQuery.getProperty("dld.getTask");
	private static final String GET_ENTITY_OWNER=dataloadDashboardQuery.getProperty("dld.getEntityOwner");
	private static final String GET_ENTITY_MASTER=dataloadDashboardQuery.getProperty("dld.getEntityMaster");
	private static final String GET_FLOW_TYPE=dataloadDashboardQuery.getProperty("dld.getFlowType");
	private static final String GET_REPOSITORY=dataloadDashboardQuery.getProperty("dld.getRepository");

	private static final String GET_ALL_SOLUTIONS=dataloadDashboardQuery.getProperty("dld.getAllSolutions");
	private static final String GET_RULE_IDS=dataloadDashboardQuery.getProperty("dld.getRuleIds");
	private static final String GET_ENTITIES_FOR_RULE=dataloadDashboardQuery.getProperty("dld.getEntitiesForRule");
	private static final String GET_REPORT_AND_ENTITYMAP = dataloadDashboardQuery.getProperty("dld.getReportAndEntityMap");
	//variables.
	
	private static final String Y = "Y";
	
	private static final String EXTRA_CLAUSE_FOR_FREQ_FILTER =" AND UPPER(fm.FREQUENCY_NAME) IN ";
	private static final String EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER =" WHERE UPPER(d1.FLOW_TYPE) IN ";
	private static final String EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING =" AND UPPER(DLFT.FLOW_TYPE) IN ";
	private static final String EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING =" AND UPPER(DLF.FREQUENCY) IN ";
	private static final String OPEN_BRACKET = " ( " ;
	private static final String CLOSE_BRACKET = " ) " ;

	private static final String SOLUTION_NAME=dataloadDashboardQuery.getProperty("dld.getSolutionName");
	private static final String DATA_SOURCE_NAME=dataloadDashboardQuery.getProperty("dld.dsName");

	//update Statements
	private static final String DEACTIVATE_TASK_MASTER=dataloadDashboardQuery.getProperty("dld.deactivateTaskMaster");
	private static final String UPDATE_TASK_MASTER=dataloadDashboardQuery.getProperty("dld.updateTaskMaster");
	private static final String UPDATE_ENTITY_OWNER=dataloadDashboardQuery.getProperty("dld.updateEntityOwner");
	private static final String UPDATE_FLOW_TYPES=dataloadDashboardQuery.getProperty("dld.updateFlowType");
	private static final String UPDATE_TASK_REP=dataloadDashboardQuery.getProperty("dld.updateTaskRep");
	private static final String UPDATE_ENTITY_MASTER=dataloadDashboardQuery.getProperty("dld.updateEntityMaster");
	private static final String GET_TASK_TYPE=dataloadDashboardQuery.getProperty("dld.getTaskTypeData");
	private static final String GET_ENTITY_TYPE=dataloadDashboardQuery.getProperty("dld.getEntityTypeData");
	



	private static final String STAGING_SUMMARY_FOR_SOURCE_SYSTEM_GRID=dataloadDashboardQuery.getProperty("dld.getSourceStagingSummaryForGrid");
	private static final String UNPLANNED_TASK_DETAILS_FOR_CBD=dataloadDashboardQuery.getProperty("dld.getUnplannedTasksForCbd");
	private static final String SUB_GRID_DATA=dataloadDashboardQuery.getProperty("dld.getSubGridData");
	private static final String IS_VALIDATION_REQUIRED_ON_TASK=dataloadDashboardQuery.getProperty("dld.isValidationRequiredOnTask");

	private static final String SEPARATOR = "@##@";
	private static final String LINE_ITEM_DETAILS=dataloadDashboardQuery.getProperty("dld.getLineItemDetails");
	private static final String ALL_LINE_ITEM_DETAILS=dataloadDashboardQuery.getProperty("dld.getAllLineItemDetails");
	private static final String LINE_ITEM_COUNT=dataloadDashboardQuery.getProperty("dld.getLineItemTotal");
	private static final String REPORT_AND_FLAG=dataloadDashboardQuery.getProperty("dld.getReportAndFlag");
	private static final String DEPENDENT_TASK=dataloadDashboardQuery.getProperty("dld.getDependentTaskNameFromTargetEntity");
	private static final String TASK_VALIDATION=dataloadDashboardQuery.getProperty("dld.isValidationRequiredOnTask");
	private static final String dateFormat=applicationProperties.getProperty("dld.dateFormatForDld");
	private static final String TASK_STATUS_LINAGE=dataloadDashboardQuery.getProperty("dld.getTaskStatusForLinage");
	private static final String EXCLUDED_SOLUTION=dataloadDashboardQuery.getProperty("dld.getExcludedSolution");
	private static final String IS_TASK_EXECUTABLE=dataloadDashboardQuery.getProperty("dld.isTaskExecutable");
	private static final String ADHOC_FREQUENCY_NAME=applicationProperties.getProperty("dld.adhocFrequencyName");
	private static final String GET_PARENT_RULE_IDS=dataloadDashboardQuery.getProperty("dld.getParentRuleIds");
	private static final String TASK_QUERY_FOR_LINEAGE= dataloadDashboardQuery.getProperty("dld.taskQueryLineage");
	private static final String ENTITY_AND_TYPE_QUERY= dataloadDashboardQuery.getProperty("dld.getEntityAndTypeQuery");
	private static final String ALL_TASK_STATUS= dataloadDashboardQuery.getProperty("dld.getAllTaskStatusApplicableForCBD");
	private static final String LINE_ITEM_DETAILS_FOR_MSSQL=dataloadDashboardQuery.getProperty("dld.getLineItemDetailsForMssql");
	private static final String ALL_LINE_ITEM_DETAILS_FOR_MSSQL=dataloadDashboardQuery.getProperty("dld.getAllLineItemDetailsForMssql");
	private static final String ORACLE = "ORACLE";
	//generic methods to close connections and statements.
	private void closeConnection(Connection dbConnection){
		if(dbConnection!=null)
			try {
				dbConnection.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close the connection",e);
			}
	}

	private void closeStatement(PreparedStatement stmt){
		if (stmt != null)
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close the statement",e);
			}
	}


	public UserDetail loadUserByUsername(String username) throws Throwable {
		LOGGER.info("DLD DAO -- >loadUserByUsername");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		UserDetail userDetails =null;

		String userDetailQuery = LOGINUSERQUERY;

		try {
			dbConnection = PersistentStoreManager.getConnection();

			stmt = dbConnection.prepareStatement(userDetailQuery);
			stmt.setString(1,username.toUpperCase().trim());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				userDetails = new UserDetail(username,rs.getString("PASSWORD"), new String[]{applicationProperties.getProperty("dld.AllowedUser")});
				userDetails.setClientCode(rs.getString("CLIENT_CODE"));
			}

		} catch (SQLException e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return userDetails;
	}

	@Override
	public List<TaskFrequencyDetail> getTotalNumberOfTasks(Set<String> freqType,String clientCode,DateTime dt) throws Throwable {

		LOGGER.info("DLD DAO -- >getTotalNumberOfTasks");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		List<TaskFrequencyDetail> listTaskFrequencyDetail=new ArrayList<TaskFrequencyDetail>();
		try {
			String taskQuery= TASK_QUERY;
			taskQuery = taskQuery+("'"+Joiner.on("','").join(freqType)+"'").toUpperCase();
			taskQuery=taskQuery+")";
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(taskQuery);
			stmt.setString(1,clientCode);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(2,cbd);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				TaskFrequencyDetail taskFrequencyDetail=new TaskFrequencyDetail();
				taskFrequencyDetail.setFrequencyType(rs.getString("frequency"));
				taskFrequencyDetail.setTaskName(rs.getString("task_name"));
				taskFrequencyDetail.setGraceDays(rs.getInt("offset"));
				taskFrequencyDetail.setVersionNo(rs.getInt("version_no"));
				taskFrequencyDetail.setTaskRepository(rs.getString("task_repository"));
				listTaskFrequencyDetail.add(taskFrequencyDetail);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return listTaskFrequencyDetail;
	}

	@Override
	public Set<String> getFrequenciesApplicableForCbd(DateTime dt, String isFreqFilterApplied, String freqFilterCSV,String clientCode)throws Throwable {
		LOGGER.info("DLD DAO -- >getFrequenciesApplicableForCbd");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Set<String> listFrequency=new HashSet<String>();
		String applicableFreqQuerry = APPLICABLEFREQUENCIES;
		try {
			
			if(Y.equalsIgnoreCase(isFreqFilterApplied)){
				//if filter is set appending extra clause.
				applicableFreqQuerry = applicableFreqQuerry+EXTRA_CLAUSE_FOR_FREQ_FILTER+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
			}
			dbConnection = PersistentStoreManager.getPrimaryAppDBConnection(clientCode);
			stmt = dbConnection.prepareStatement(applicableFreqQuerry);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(1,cbd);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				listFrequency.add(rs.getString("frequency_name"));
			}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return listFrequency;
	}

	@Override
	public Integer getCompletedTasks(DateTime dt,String clientCode,Set<String> freqType) throws Throwable {
		LOGGER.info("DLD DAO -- >getCompletedTasks");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Integer count=0;
		String applicableFreqQuerry = COMPLETEDTASKQUERY;
		applicableFreqQuerry = applicableFreqQuerry+("'"+Joiner.on("','").join(freqType)+"'").toUpperCase();
		applicableFreqQuerry=applicableFreqQuerry+")) SUB1  ON UPPER(SUB1.TASK_NAME) = UPPER(D1.TASK_NAME)  AND UPPER(d1.TASK_REPOSITORY) = UPPER(SUB1.TASK_REPOSITORY)"
				+ "  AND d1.CLIENT_CODE = SUB1.CLIENT_CODE  where UPPER(d1.task_status) = ?";
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(applicableFreqQuerry);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setString(5, "COMPLETED");
			stmt.setString(1, clientCode);
			stmt.setDate(2,cbd);
			stmt.setDate(4,cbd);
			stmt.setString(3, clientCode);

			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				count=rs.getInt(1);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return count;
	}

	@Override
	public Integer getFailedTasks(DateTime dt,String clientCode,Set<String> freqType) throws Throwable {
		LOGGER.info("DLD DAO -- >getFailedTasks");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Integer count=0;
		String applicableFreqQuerry = COMPLETEDTASKQUERY;
		applicableFreqQuerry = applicableFreqQuerry+("'"+Joiner.on("','").join(freqType)+"'").toUpperCase();
		applicableFreqQuerry=applicableFreqQuerry+")) SUB1  ON UPPER(SUB1.TASK_NAME) = UPPER(D1.TASK_NAME)  AND UPPER(d1.TASK_REPOSITORY) = UPPER(SUB1.TASK_REPOSITORY)"
				+ "  AND d1.CLIENT_CODE = SUB1.CLIENT_CODE  where UPPER(d1.task_status) = ?";
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(applicableFreqQuerry);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setString(5, "FAILED");
			stmt.setString(1, clientCode);
			stmt.setDate(2,cbd);
			stmt.setDate(4,cbd);
			stmt.setString(3, clientCode);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				count=rs.getInt(1);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return count;

	}

	@Override
	public List<TaskExecutionLog> getTasksByCbdandStatus(DateTime dt,String status,String isFlowFilterApplied, String flowTypeCSV,String clientCode,Set<String> freqType) throws Throwable {
		LOGGER.info("DLD DAO -- >getTasksByCbdandStatus");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		List<TaskExecutionLog> tskexecLogList=new ArrayList<TaskExecutionLog>();
		String query = ALL_TASK_DETAILS_BY_CBD;
		query = query+("'"+Joiner.on("','").join(freqType)+"'").toUpperCase();
		query=query+")) SUB1  ON UPPER(SUB1.TASK_NAME) = UPPER(D1.TASK_NAME)  AND UPPER(d1.TASK_REPOSITORY) = UPPER(SUB1.TASK_REPOSITORY)"
				+ "  AND d1.CLIENT_CODE = SUB1.CLIENT_CODE";
		
		try {
			if(Y.equalsIgnoreCase(isFlowFilterApplied)){
				query = query + EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER +OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET;
			}
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(query);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(1, cbd);
			stmt.setString(2, status.toUpperCase());
			stmt.setString(3, clientCode);
			stmt.setString(4, clientCode);
			stmt.setDate(5,cbd);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				TaskExecutionLog tskexec=new TaskExecutionLog();
				tskexec.setBusinessDate(rs.getDate("business_period_date"));
				tskexec.setClientCode(rs.getString("client_code"));
				tskexec.setEndDate(rs.getDate("end_date_time"));
				tskexec.setTaskRepository(rs.getString("task_repository"));
				tskexec.setTaskName(rs.getString("task_name"));
				tskexec.setFlowType(rs.getString("flow_type"));
				tskexec.setFlowSequenceNumber(rs.getInt("flow_sequence_no"));
				tskexec.setTechnicalTaskName(rs.getString("technical_task_name"));
				tskexec.setTechnicalSubTaskName(rs.getString("technical_sub_task_name"));
				tskexec.setTaskStatus(rs.getString("task_status"));
				tskexec.setRunPeriodDate(rs.getDate("run_period_date"));
				tskexec.setRunDetails(rs.getString("run_details"));
				tskexec.setStartDate(rs.getDate("start_date_time"));
				tskexec.setEndDate(rs.getDate("end_date_time"));
				tskexec.setSourceCount(rs.getInt("source_count"));
				tskexec.setTargetCount(rs.getInt("target_count"));
				tskexec.setTargetInsertedCount(rs.getInt("target_inserted_count"));
				tskexec.setTargetUpdatedCount(rs.getInt("target_updated_count"));
				tskexec.setTargetRejectedRecord(rs.getInt("target_rejected_record"));
				tskexecLogList.add(tskexec);
			}
			return tskexecLogList;

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);

			closeConnection(dbConnection);
		}

	}

	@Override
	public String getPreviousBusinessDate(DateTime dt,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getPreviousBusinessDate");

		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String prevBusinessDate="";
		String prevBusinessDateQuery = PREV_BUSINESS_DATE;

		try {
			dbConnection = PersistentStoreManager.getConnection();

			stmt = dbConnection.prepareStatement(prevBusinessDateQuery);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(1, cbd);
			stmt.setString(2, clientCode);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				DateTimeFormatter fmt = DateTimeFormat.forPattern(dateFormat);
				if(rs.getDate(1)==null)
					return null;
				else
					prevBusinessDate=fmt.print(new DateTime(rs.getDate(1)));

			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return prevBusinessDate;

	}

	@Override
	public void insertTaskStatics(List<TaskExecutionLog> taskExecutionLog) throws Throwable {
		LOGGER.info("DLD DAO -- >insertTaskStatics");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();

			stmt = dbConnection.prepareStatement(INSERT_TASK_STATICS);
			for(TaskExecutionLog tx:taskExecutionLog){

				stmt.setString(1, tx.getClientCode());
				stmt.setString(2, tx.getTaskRepository());
				stmt.setString(3, tx.getTaskName());
				stmt.setString(4, tx.getFlowType());
				stmt.setInt(5, tx.getFlowSequenceNumber());
				stmt.setString(6, tx.getTechnicalTaskName());
				stmt.setString(7, tx.getTechnicalSubTaskName());
				stmt.setString(8, tx.getTaskStatus());
				stmt.setDate(9, new Date(tx.getRunPeriodDate().getTime()));
				stmt.setDate(10, new Date(tx.getBusinessDate().getTime()));
				stmt.setString(11, tx.getRunDetails());
				stmt.setTimestamp(12, new Timestamp(tx.getStartDate().getTime()));
				stmt.setTimestamp(13, new Timestamp(tx.getEndDate().getTime()));
				stmt.setInt(14, tx.getSourceCount());
				stmt.setInt(15, tx.getTargetCount());
				stmt.setInt(16, tx.getTargetInsertedCount());
				stmt.setInt(17, tx.getTargetUpdatedCount());
				stmt.setInt(18, tx.getTargetRejectedRecord());
				stmt.execute();
				dbConnection.commit();
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

	}

	private Integer getMaxSeqNo() throws Throwable{
		LOGGER.info("DLD DAO -- >getMaxSeqNo");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Integer maxSeqNo=null;
		try {;
		dbConnection = PersistentStoreManager.getConnection();
		stmt = dbConnection.prepareStatement(MAX_SEQ_NO);
		// execute select SQL stetement
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			maxSeqNo=rs.getInt(1);
		}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);

			closeConnection(dbConnection);
		}
		if(maxSeqNo==null){
			maxSeqNo=1;
		} else {
			maxSeqNo=maxSeqNo+1;
		}
		return maxSeqNo;
	}




	@Override
	public String getMaxBusinessDate(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getMaxBusinessDate");

		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String maxBusinessDate="";
		String maxBusinessDateQuery = MAX_BUSINESS_DATE;

		try {
			dbConnection = PersistentStoreManager.getConnection();

			stmt = dbConnection.prepareStatement(maxBusinessDateQuery);
			stmt.setString(1, clientCode);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				DateTimeFormatter fmt = DateTimeFormat.forPattern(dateFormat);
				maxBusinessDate=fmt.print(new DateTime(rs.getDate(1)));

			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return maxBusinessDate;

	}

	@Override
	public String getPreviousPendingDate(DateTime dt,String clientCode) throws Throwable{
		LOGGER.info("DLD DAO -- >getPreviousPendingDate");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String prevPendingDate="";
		String prevPendingQuery = PREV_PENDING_DATE;
		try {
			dbConnection = PersistentStoreManager.getConnection();

			stmt = dbConnection.prepareStatement(prevPendingQuery);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(1, cbd);
			stmt.setString(2, clientCode);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				DateTimeFormatter fmt = DateTimeFormat.forPattern(dateFormat);
				if(rs.getDate(1)==null)
					return null;
				else
					prevPendingDate=fmt.print(new DateTime(rs.getDate(1)));

			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return prevPendingDate;
	}


	@Override
	public void putUploadedFileToCache(String userTokenSession, Map<String, Object> uploadDTOMap) throws Throwable {
		getUploadedFileFromCache(userTokenSession);
		LOGGER.info("DLD DAO -- >putUploadedFileToCache");
		if (CacheManager.getInstance().getCache("uploadedFileListCache")!=null){
			Element newElem = new Element(userTokenSession, uploadDTOMap);
			CacheManager.getInstance().getCache("uploadedFileListCache").put(newElem);
		}

	}


	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getUploadedFileFromCache(String userTokenSession) throws Throwable {
		LOGGER.info("DLD DAO -- >getUploadedFileFromCache");
		Map<String, Object> uploadDTOMap = new HashMap<String,Object>();
		if (CacheManager.getInstance().getCache("uploadedFileListCache")!=null){
			Element e = CacheManager.getInstance().getCache("uploadedFileListCache").get(userTokenSession);
			if(e!=null)
				uploadDTOMap = ((Map<String, Object>)e.getObjectValue());
		}else{
			CacheConfiguration cc = new CacheConfiguration("uploadedFileListCache", 10);
			cc.setDiskPersistent(true);
			cc.setMaxElementsInMemory(1);
			cc.setMaxElementsOnDisk(10000);
			cc.setOverflowToDisk(true);
			// TODO change
			cc.setEternal(true);
			Ehcache cache = new Cache(cc);
			CacheManager.getInstance().addCache(cache);
			while (CacheManager.getInstance().getCache("uploadedFileListCache").getStatus() != Status.STATUS_ALIVE){
				LOGGER.info("DLD DAO -- >STATUS_ALIVE");
			}
		}
		return uploadDTOMap;
	}
	@Override
	public Set<String> getDistinctApplicableFlowTypesByTask(List<String> taskNames,String clientCode) {
		LOGGER.info("DLD DAO -- >getDistinctApplicableFlowTypesByTask");
		Set<String> flowTypes = new HashSet<String>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String distinctFlowTypeQuery = GET_DISTINCT_FLOW_TYPE;
		//distinctFlowTypeQuery = distinctFlowTypeQuery + OPEN_BRACKET+"'"+Joiner.on("','").join(taskNames)+"'"+CLOSE_BRACKET;
		int j=1;
		String inCls="";
		String q=GET_DISTINCT_FLOW_TYPE +OPEN_BRACKET;
		for(String val:taskNames)
		{
            if(j==99){
                j=1;
                inCls=inCls+"'"+val.toUpperCase()+"'"+",";
                q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                j++;
                inCls="OR UPPER(TASK_NAME) IN "+OPEN_BRACKET;
            }
            else{
                j++;
                inCls=inCls+"'"+val.toUpperCase()+"'"+",";
            }
		}
		
		if(inCls.length()>19){
            q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
		}
		else 
			if(!inCls.substring(0,inCls.length()-1).equalsIgnoreCase("OR UPPER(TASK_NAME) IN  ("))
				q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
        q=q+CLOSE_BRACKET;
		
        distinctFlowTypeQuery = q.toUpperCase();
        LOGGER.info(q);
		
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(distinctFlowTypeQuery);
			stmt.setString(1, clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				flowTypes.add(rs.getString(1));
			}


		}catch( Throwable e){
			LOGGER.error("Error occured while Saving", e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return flowTypes;
	}

	@Override
	public List<TaskFlowTypeDetail> getTaskListBasedOnFilter(String flowFilterCSV,String clientCode) {
		LOGGER.info("DLD DAO -- >getTaskListBasedOnFilter");
		List<TaskFlowTypeDetail> flowTypes = new ArrayList<TaskFlowTypeDetail>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String taskByFlowTypeQuery = TASK_FROM_FLOW_TYPE;
		taskByFlowTypeQuery = taskByFlowTypeQuery+OPEN_BRACKET+flowFilterCSV.toUpperCase()+CLOSE_BRACKET;

		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(taskByFlowTypeQuery);
			stmt.setString(1, clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				TaskFlowTypeDetail task = new TaskFlowTypeDetail();
				task.setClientCode(rs.getString("CLIENT_CODE"));
				task.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				task.setTaskName(rs.getString("TASK_NAME"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				task.setVersionNo(rs.getInt("VERSION_NO"));
				flowTypes.add(task);
			}


		}catch( Throwable e){
			LOGGER.error("Error",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return flowTypes;
	}

	@Override
	public List<DldSolution> getAllSoutions(String clientCode) throws Throwable {
		List<DldSolution> solutionList = new ArrayList<DldSolution>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String soultionQuery = SOLUTION_NAME;
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(soultionQuery);
			stmt.setString(1, clientCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				DldSolution sol=new DldSolution();
				sol.setSolutionName(rs.getString("SOLUTIONNAME"));
				sol.setSolutionID(rs.getInt("SOLUTIONID"));
				sol.setIsActive(rs.getBoolean("ISACTIVE"));
				sol.setSolutionDescription(rs.getString("SOLUTIONDESCRIPTION"));
				sol.setProductID(rs.getInt("PRODUCTID"));
				sol.setBelongsTo(rs.getString("BELONGSTO"));
				sol.setBiType(rs.getString("BITYPE"));
				sol.setIsSecurityFilterActive(rs.getBoolean("ISSFACTIVE"));
				solutionList.add(sol);
			}
		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return solutionList;	

	}

	@Override
	public List<DataSource> getAllDataSource(String clientCode) throws Throwable {
		List<DataSource> dataSourceNameList = new ArrayList<DataSource>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String dsQuery = DATA_SOURCE_NAME;
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(dsQuery);
			stmt.setString(1, clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				DataSource ds=new DataSource();
				ds.setDataSourceName(rs.getString("DATA_SOURCE_NAME"));
				ds.setDataSourceID(rs.getInt("DATA_SOURCE_ID"));
				ds.setAuditGlDataSourceId(rs.getInt("AUDIT_GL_DATA_SOURCE_ID"));
				ds.setFinancialYearTypeId(rs.getInt("DATA_SOURCE_ID"));
				ds.setIsScglInd(rs.getString("IS_SCGL_IND"));
				ds.setScglCurrencyUcid(rs.getString("SCGL_CURRENCY_UCID"));
				dataSourceNameList.add(ds);
			}


		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return dataSourceNameList;


	}

	@Override
	public List<DlTaskMaster> getTaskList(String taskInCls) {

		LOGGER.info("DLD DAO -- >getTaskList");
		List<DlTaskMaster> tasks = new ArrayList<DlTaskMaster>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String taskQuery = TASK_FILTER_QUERRY;
		String taskByQuerryWithIn = taskQuery+OPEN_BRACKET+taskInCls+CLOSE_BRACKET;

		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(taskByQuerryWithIn);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				DlTaskMaster task = new DlTaskMaster();
				task.setClientCode(rs.getString("CLIENT_CODE"));
				task.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				task.setTaskName(rs.getString("TASK_NAME"));
				task.setTaskType(rs.getString("TASK_TYPE"));
				task.setTaskDescription(rs.getString("TASK_DESCRIPTION"));
				task.setTechnicalTaskName(rs.getString("TECHNICAL_TASK_NAME"));
				task.setTechnicalSubTaskName(rs.getString("TECHNICAL_SUB_TASK_NAME"));
				tasks.add(task);
			}


		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return tasks;
	}

	@Override
	public List<String> getLoadSourceSystemDetailsCompleted(String taskInCls) {
		LOGGER.info("DLD DAO -- >getLoadSourceSystemDetailsCompleted");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String totalSourceSystem = TOTAL_SOURCE_SYSTEMS_FOR_DATE;
		String taskByQuerryWithIn = totalSourceSystem+OPEN_BRACKET+taskInCls+CLOSE_BRACKET;
		List<String> taskNames=new ArrayList<String>();
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(taskByQuerryWithIn);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskNames.add(rs.getString("task_name"));
			}


		}catch( Throwable e){
			LOGGER.error("Error",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return taskNames;

	}



	@Override
	public List<StagingDetails> getStagingDetails(DateTime cbd, String isFreqFilterApplied, String freqFilterCSV,
			String isFlowFilterApplied, String flowTypeCSV, String isDataSource,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getStagingDetails");
		List<StagingDetails> stagingDetails = new ArrayList<StagingDetails>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String stagingDetailsQuery = STAGING_DETAILS_QUERRY;
		if(isFreqFilterApplied.equalsIgnoreCase("Y")&&isFlowFilterApplied.equalsIgnoreCase("Y"))
			stagingDetailsQuery = stagingDetailsQuery+EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING+OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET+EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
		else if(isFreqFilterApplied.equalsIgnoreCase("Y"))
			stagingDetailsQuery = stagingDetailsQuery+EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
		else if(isFlowFilterApplied.equalsIgnoreCase("Y"))
			stagingDetailsQuery = stagingDetailsQuery+EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING+OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET;
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(stagingDetailsQuery);
			java.sql.Date dt=new java.sql.Date(cbd.toDate().getTime());
			stmt.setDate(1, dt);
			if(isDataSource.equalsIgnoreCase("Y"))
			{
				stmt.setString(2, "S");
				stmt.setString(3, "Y");
			}
			else
			{
				stmt.setString(2, "T");
				stmt.setString(3, "N");
			}
			stmt.setString(4, clientCode);
			stmt.setDate(5, dt);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				StagingDetails task = new StagingDetails();
				task.setTaskName(rs.getString("TASK_NAME"));
				task.setClientCode(rs.getString("CLIENT_CODE"));
				task.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				task.setFrequency(rs.getString("FREQUENCY"));
				task.setOffset(rs.getInt("OFFSET"));
				task.setSolutionId(rs.getInt("SOLUTION_ID"));
				task.setIsExclusionIndicator(rs.getString("Is_Exclusion_Indicator"));
				task.setOwnerName(rs.getString("OWNER_NAME"));
				task.setEntityName(rs.getString("ENTITY_NAME"));
				task.setTaskStatus(rs.getString("TASK_STATUS"));
				task.setLinkType(rs.getString("LINK_TYPE"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				stagingDetails.add(task);
			}
		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return stagingDetails;
	}

	@Override
	public void logAllTask(String params,String requestOrigin) throws Throwable {
		LOGGER.info("DLD DAO -- >logAllTask");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();

			stmt = dbConnection.prepareStatement(LOG_ALL_TASK);
			
			stmt.setString(1, params);
			stmt.setString(2, requestOrigin);
			stmt.execute();
			dbConnection.commit();

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

	}


	@Override
	public DlTaskMaster getTaskMaster(String clientCode, String taskRepository, String taskName,java.util.Date effectiveDate) throws Exception {
		LOGGER.info("DLD DAO -- >getTaskMaster");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskMaster tMaster=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_TASK_MASTER);
			stmt.setString(1, clientCode);
			stmt.setString(2, taskRepository.toUpperCase());	
			stmt.setString(3, taskName.toUpperCase());
			stmt.setString(4, "Y");
			stmt.setDate(5, new java.sql.Date(effectiveDate.getTime()));

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				tMaster=new DlTaskMaster();
				tMaster.setClientCode(rs.getString("CLIENT_CODE"));
				tMaster.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				tMaster.setTaskName(rs.getString("TASK_NAME"));
				tMaster.setVersionNo(rs.getInt("VERSION_NO"));
				tMaster.setStartDate(rs.getDate("EFFECTIVE_START_DATE"));
				tMaster.setEnddate(rs.getDate("EFFECTIVE_END_DATE"));
				tMaster.setIsActive(rs.getString("IS_ACTIVE"));
				tMaster.setTaskType(rs.getString("TASK_TYPE"));
				tMaster.setTaskDescription(rs.getString("TASK_DESCRIPTION"));
				tMaster.setTechnicalTaskName(rs.getString("TECHNICAL_TASK_NAME"));
				tMaster.setTechnicalSubTaskName(rs.getString("TECHNICAL_SUB_TASK_NAME"));

			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return tMaster;
	}

	@Override
	public void deactivateDataForTaskMaster(List<DlTaskMaster> deactivateTaskList) throws Throwable {
		LOGGER.info("DLD DAO -- >deactivateDataForTaskMaster");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(DEACTIVATE_TASK_MASTER);
			for(DlTaskMaster task:deactivateTaskList){
				stmt.setString(1, task.getIsActive());
				stmt.addBatch();
			}
			stmt.executeBatch();
			dbConnection.commit();
		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}		


	}

	@Override
	public void updateDataForTaskMaster(List<DlTaskMaster> updateTaskList) throws Throwable {
		LOGGER.info("DLD DAO -- >deactivateDataForTaskMaster");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(UPDATE_TASK_MASTER);
			for(DlTaskMaster task:updateTaskList){
				stmt.setDate(1, (Date) task.getStartDate());
				stmt.setDate(2, (Date) task.getEnddate());
				stmt.addBatch();
			}
			stmt.executeBatch();
			dbConnection.commit();
		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}		

	}

	@Override
	public List<DlTaskRepository> gettaskrepositoriesData(String clientCode,Date effectiveDate) throws Throwable {
		LOGGER.info("DLD DAO -- >gettaskrepositoriesData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskRepository taskRepository=null;
		List<DlTaskRepository> taskRepositoryList=new ArrayList<DlTaskRepository>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_REP_DATA);
			stmt.setString(1,clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskRepository=new DlTaskRepository();
				taskRepository.setClientCode(rs.getString("CLIENT_CODE"));
				taskRepository.setRepositoryName(rs.getString("REPOSITORY_NAME"));
				taskRepository.setDescription(rs.getString("REPOSITORY_DESCRIPTION"));
				taskRepositoryList.add(taskRepository);
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskRepositoryList;
	}

	@Override
	public List<DlFlowType> getFlowTypesData(String clientCode,Date effectiveDate) throws Throwable {
		LOGGER.info("DLD DAO -- >getFlowTypesData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlFlowType flowType=null;
		List<DlFlowType> flowTypeList=new ArrayList<DlFlowType>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_FLOW_DATA);
			stmt.setString(1,clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				flowType=new DlFlowType();
				flowType.setClientCode(rs.getString("CLIENT_CODE"));
				flowType.setFlowType(rs.getString("FLOW_TYPE"));
				flowType.setDescription(rs.getString("FLOW_DESCRIPTION"));
				flowTypeList.add(flowType);
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return flowTypeList;
	}

	@Override
	public List<DlEntity> getEntityMasterData(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getEntityMasterData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlEntity entityMaster=null;
		List<DlEntity> entityMasterList=new ArrayList<DlEntity>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ENTITY_MASTER_DATA);
			stmt.setString(1,clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				entityMaster=new DlEntity();
				entityMaster.setClientCode(rs.getString("CLIENT_CODE"));
				entityMaster.setOwnerName(rs.getString("OWNER_NAME"));
				entityMaster.setEntityDetail(rs.getString("ENTITY_DETAIL"));
				entityMaster.setDescription(rs.getString("ENTITY_DESCRIPTION"));
				entityMaster.setEntityName(rs.getString("ENTITY_NAME"));
				entityMaster.setEntityType(rs.getString("ENTITY_TYPE"));
				entityMasterList.add(entityMaster);
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return entityMasterList;
	}

	@Override
	public List<DlEntityOwner> getEntityOwnerData(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getEntityOwnerData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlEntityOwner entityOwner=null;
		List<DlEntityOwner> entityOwnerList=new ArrayList<DlEntityOwner>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ENTITY_OWNER_DATA);
			stmt.setString(1,clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				entityOwner=new DlEntityOwner();
				entityOwner.setClientCode(rs.getString("CLIENT_CODE"));
				entityOwner.setOwner_Name(rs.getString("OWNER_NAME"));
				entityOwner.setExternal_Source(rs.getString("EXTERNAL_SOURCE_INDICATOR"));
				entityOwner.setDescription(rs.getString("OWNER_DESCRIPTION"));



				entityOwner.setData_Source_Id(rs.getInt("DATA_SOURCE_ID"));

				Integer dsVal=rs.getInt("DATA_SOURCE_ID");
				if(rs.wasNull())
				{
					entityOwner.setData_Source_Id(null);
				}
				else
					entityOwner.setData_Source_Id(dsVal);

				Integer solVal=rs.getInt("SOLUTION_ID");
				if(rs.wasNull())
				{
					entityOwner.setSolution_Id(null);
				}
				else
					entityOwner.setSolution_Id(solVal);

				entityOwner.setDisplay_Sorting_Order(rs.getString("DISPLAY_ORDER"));
				entityOwner.setContact_Details(rs.getString("CONTACT_DETAILS"));
				entityOwnerList.add(entityOwner);
			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return entityOwnerList;
	}

	@Override
	public List<DlTaskMaster> gettaskData(String clientCode,Date effDate) throws Throwable {
		LOGGER.info("DLD DAO -- >gettaskData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskMaster taskMaster=null;
		List<DlTaskMaster> taskMasterList=new ArrayList<DlTaskMaster>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_TASK_DATA);

			stmt.setString(1,clientCode);
			stmt.setString(2, "Y");
			stmt.setDate(3, effDate);
			
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskMaster=new DlTaskMaster();
				taskMaster.setClientCode(rs.getString("CLIENT_CODE"));
				taskMaster.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				taskMaster.setTaskName(rs.getString("TASK_NAME"));
				taskMaster.setVersionNo(rs.getInt("VERSION_NO"));
				taskMaster.setStartDate(rs.getDate("EFFECTIVE_START_DATE"));
				taskMaster.setEnddate(rs.getDate("EFFECTIVE_END_DATE"));
				taskMaster.setIsActive(rs.getString("IS_ACTIVE"));
				taskMaster.setTaskType(rs.getString("TASK_TYPE"));
				taskMaster.setTaskDescription(rs.getString("TASK_DESCRIPTION"));
				taskMaster.setTechnicalTaskName(rs.getString("TECHNICAL_TASK_NAME"));
				taskMaster.setTechnicalSubTaskName(rs.getString("TECHNICAL_SUB_TASK_NAME"));
				taskMaster.setIsValidationRequired(rs.getString("IS_VALIDATION_REQUIRED"));
				taskMasterList.add(taskMaster);
			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskMasterList;
	}

	@Override
	public List<DlTaskFlowType> getFlowData(String clientCode,String RepositoryName,String taskName,Integer versionNo) throws Throwable {
		LOGGER.info("DLD DAO -- >getFlowData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskFlowType taskFlowType=null;
		List<DlTaskFlowType> taskFlowTypeList=new ArrayList<DlTaskFlowType>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_FL_DATA);
			stmt.setString(1,clientCode);
			stmt.setString(2,RepositoryName.toUpperCase());
			stmt.setString(3,taskName.toUpperCase());
			stmt.setInt(4,versionNo);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskFlowType=new DlTaskFlowType();
				taskFlowType.setClientCode(rs.getString("CLIENT_CODE"));
				taskFlowType.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				taskFlowType.setTaskName(rs.getString("TASK_NAME"));
				taskFlowType.setVersionNo(rs.getInt("VERSION_NO"));
				taskFlowType.setFlowType(rs.getString("FLOW_TYPE"));
				taskFlowTypeList.add(taskFlowType);
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskFlowTypeList;
	}




	@Override
	public List<DlTaskFrequency> getFeqData(String clientCode,String RepositoryName,String taskName,Integer versionNo) throws Throwable {
		LOGGER.info("DLD DAO -- >getFeqData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskFrequency taskFrequency=null;
		List<DlTaskFrequency> taskFrequencyList=new ArrayList<DlTaskFrequency>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_FQ_DATA);
			stmt.setString(1,clientCode);
			stmt.setString(2,RepositoryName.toUpperCase());
			stmt.setString(3,taskName.toUpperCase());
			stmt.setInt(4,versionNo);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskFrequency=new DlTaskFrequency();
				taskFrequency.setClientCode(rs.getString("CLIENT_CODE"));
				taskFrequency.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				taskFrequency.setTaskName(rs.getString("TASK_NAME"));
				taskFrequency.setVersionNo(rs.getInt("VERSION_NO"));
				taskFrequency.setFrequency(rs.getString("FREQUENCY"));
				taskFrequency.setOffset(rs.getInt("OFFSET"));
				taskFrequency.setIsExclusionInd(rs.getString("IS_EXCLUSION_INDICATOR"));
				taskFrequencyList.add(taskFrequency);
			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskFrequencyList;
	}

	@Override
	public List<DlTaskSourceTarget> getSourceTargetData(String clientCode,String RepositoryName,String taskName,Integer versionNo) throws Throwable {
		LOGGER.info("DLD DAO -- >getSourceTargetData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskSourceTarget taskSourceTarget=null;
		List<DlTaskSourceTarget> taskSourceTargetList=new ArrayList<DlTaskSourceTarget>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ST_DATA);
			stmt.setString(1,clientCode);
			stmt.setString(2,RepositoryName.toUpperCase());
			stmt.setString(3,taskName.toUpperCase());
			stmt.setInt(4,versionNo);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskSourceTarget=new DlTaskSourceTarget();
				taskSourceTarget.setClientCode(rs.getString("CLIENT_CODE"));
				taskSourceTarget.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				taskSourceTarget.setTaskname(rs.getString("TASK_NAME"));
				taskSourceTarget.setVersionNo(rs.getInt("VERSION_NO"));
				taskSourceTarget.setOwnerName(rs.getString("OWNER_NAME"));
				taskSourceTarget.setEntityName(rs.getString("ENTITY_NAME"));
				taskSourceTarget.setLinkType(rs.getString("LINK_TYPE"));
				taskSourceTargetList.add(taskSourceTarget);
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskSourceTargetList;
	}






	@Override
	public Map<String,Integer> getListOfSolutionNames(String clientCode) throws Throwable {

		LOGGER.info("DLD DAO -- >getListOfSolutionNames");
		Map<String,Integer> solutions=new HashMap<>();
		//query execution.
		try {
			solutions= PersistentStoreManager.getAllSolutionName(clientCode);


		} catch (Throwable e) {
			LOGGER.error("Not able to read from file",e);
		} 
		return solutions;
	}

	@Override
	public Map<Integer,Set<String>> getListOfRuleIds(DateTime cbd,String solutionName,Integer solutionId,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getListOfRuleIds");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		Set<Integer> ruleIds=new HashSet<Integer>();
		Map<Integer,Set<Integer>> mapOfReportAndRuleId = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<String>> mapOfReportIDAndEntitityName = new HashMap<Integer,Set<String>>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solutionName,clientCode);
			

			stmt = dbConnection.prepareStatement(GET_RULE_IDS);
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
			String periodDate=fmt.print(cbd);
			stmt.setInt(1, solutionId);
			stmt.setInt(2, Integer.parseInt(periodDate));
			java.sql.Date dt=new java.sql.Date(cbd.toDate().getTime());
			stmt.setDate(3, dt);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				//processing rule id and report id
				ruleIds = new HashSet<Integer>();
				String ruleIdCsv=rs.getString("RULE_LIST");
				if(null!=ruleIdCsv){
					for(String ruleId:ruleIdCsv.split(","))
					{
						ruleIds.add(Integer.parseInt(ruleId));
					}
					if(null==mapOfReportAndRuleId.get(rs.getInt("REG_REPORT_ID"))){
						mapOfReportAndRuleId.put(rs.getInt("REG_REPORT_ID"), ruleIds);
					}else{
						mapOfReportAndRuleId.get(rs.getInt("REG_REPORT_ID")).addAll(ruleIds);
					}
				}
				

			}
			
			for (Map.Entry<Integer, Set<Integer>> entry : mapOfReportAndRuleId.entrySet()) {
				mapOfReportAndRuleId.put(entry.getKey(), getParentRuleIdsForDerivedRule(cbd, solutionName, clientCode, solutionId, entry.getValue()));
			}

			for (Map.Entry<Integer, Set<Integer>> entry : mapOfReportAndRuleId.entrySet()) {
				//looping through map of report and rule id to get underlying fact name.
				Set<String> entityNameSet = new HashSet<String>();
				Set<Integer> value =  entry.getValue();
				Integer reportId = entry.getKey();
				int j=1;
				String inCls="";
				String q=GET_ENTITIES_FOR_RULE;
				for(Integer val:value)
				{
                    if(j==99){
                        j=1;
                        inCls=inCls+val+",";
                        q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                        j++;
                        inCls="OR RULE_ID IN "+OPEN_BRACKET;
                    }
                    else{
                        j++;
                        inCls=inCls+val+",";
                    }
				}
				
				if(inCls.length()>17){
                    q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
				}
				else 
					if(!inCls.substring(0,inCls.length()-1).equalsIgnoreCase("OR RULE_ID IN  ("))
						q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                q=q+CLOSE_BRACKET;
				
                String queryForEntities = q;
				stmt1  = dbConnection.prepareStatement(queryForEntities);
				stmt1.setDate(1, dt);
				stmt1.setInt(2, solutionId);

				ResultSet rs1 = stmt1.executeQuery();
				while (rs1.next()) {
					entityNameSet.add(rs1.getString("FACT_NAME").trim());
				}
				if(null==mapOfReportIDAndEntitityName.get(reportId)){
					mapOfReportIDAndEntitityName.put(reportId, entityNameSet);
				}else{
					mapOfReportIDAndEntitityName.get(reportId).addAll(entityNameSet);
				}
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeStatement(stmt1);
			closeConnection(dbConnection);
		}
		return mapOfReportIDAndEntitityName;

	}

	@Override
	public List<StagingDetails> getSourceSystemSummaryForGrid(DateTime cbd, String isFreqFilterApplied, String freqFilterCSV,
			String isFlowFilterApplied, String flowTypeCSV, String summaryType,String clientCode) throws Throwable {
		// summaryType--> type of tab
		LOGGER.info("DLD DAO -- >getSourceSystemSummaryForGrid");
		List<StagingDetails> stagingDetails = new ArrayList<StagingDetails>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String stagingDetailsQuery = STAGING_SUMMARY_FOR_SOURCE_SYSTEM_GRID ;
		if(isFreqFilterApplied.equalsIgnoreCase("Y")&&isFlowFilterApplied.equalsIgnoreCase("Y"))
			stagingDetailsQuery = stagingDetailsQuery+EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING+OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET+EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
		else if(isFreqFilterApplied.equalsIgnoreCase("Y"))
			stagingDetailsQuery = stagingDetailsQuery+EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
		else if(isFlowFilterApplied.equalsIgnoreCase("Y"))
			stagingDetailsQuery = stagingDetailsQuery+EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING+OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET;

		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(stagingDetailsQuery);
			java.sql.Date dt=new java.sql.Date(cbd.toDate().getTime());
			stmt.setDate(1, dt);
			stmt.setDate(2, dt);
			stmt.setString(5, clientCode);
			stmt.setDate(6, dt);
			// CHECK FOR CONDITION AND SET FOR DATA REPO HERE.
			if(summaryType.equalsIgnoreCase("SOURCESYSTEM"))
			{
				stmt.setString(3, "S");
				stmt.setString(4, "Y");
			}
			else
			{
				stmt.setString(3, "T");
				stmt.setString(4, "N");
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				StagingDetails task = new StagingDetails();
				task.setTaskName(rs.getString("TASK_NAME"));
				task.setClientCode(rs.getString("CLIENT_CODE"));
				task.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				task.setFrequency(rs.getString("FREQUENCY"));
				task.setOffset(rs.getInt("OFFSET"));
				task.setIsExclusionIndicator(rs.getString("Is_Exclusion_Indicator"));
				task.setOwnerName(rs.getString("OWNER_NAME"));
				task.setEntityName(rs.getString("ENTITY_NAME"));
				task.setTaskStatus(rs.getString("TASK_STATUS"));
				task.setLinkType(rs.getString("LINK_TYPE"));
				task.setSourceEntityName(rs.getString("SOURCE_ENTITY"));
				task.setTargetEntityName(rs.getString("TARGET_ENTITY"));
				task.setOwnerDesc(rs.getString("OWNER_DESCRIPTION"));
				task.setOwnerContactDetails(rs.getString("CONTACT_DETAILS"));
				task.setTaskDesc(rs.getString("TASK_DESCRIPTION"));
				task.setTaskType(rs.getString("TASK_TYPE"));
				task.setRunCount(rs.getInt("RUN_COUNT"));
				task.setRunDate(rs.getDate("RUN_PERIOD_DATE"));
				task.setBusinessDate(rs.getDate("BUSINESS_PERIOD_DATE"));
				task.setEntityDesc(rs.getString("ENTITY_DESCRIPTION"));
				task.setEntityType(rs.getString("ENTITY_TYPE"));
				task.setStartDateTime(rs.getTimestamp("START_DATE_TIME"));
				task.setEndDateTime(rs.getTimestamp("END_DATE_TIME"));
				task.setTaskTechnicalName(rs.getString("TECHNICAL_TASK_NAME"));
				task.setRunDetails(rs.getString("RUN_DETAILS"));
				task.setEntityOwner(rs.getString("SOURCE"));
				task.setEntityTechnicalName(rs.getString("ENTITY_DETAIL"));
				task.setSolutionId(rs.getInt("SOLUTION_ID"));
				stagingDetails.add(task);
			}


		}catch( Throwable e){
			LOGGER.error("Error",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return stagingDetails;

	}


	@Override
	public Boolean saveUploaderData(List<DlEntityOwner> updateEntityOwnerList, List<DlEntityOwner> insertEntityOwnerList,
			List<DlEntity> updateEntityMasterList, List<DlEntity> insertEntityMasterList,
			List<DlFlowType> updateFlowTypeList, List<DlFlowType> insertFlowTypeList,
			List<DlTaskRepository> updateTaskRepList, List<DlTaskRepository> insertTaskRepMasterList,
			List<DlTaskMaster> tMasterList, List<DlTaskFlowType> taskFlowTypeList,
			List<DlTaskFrequency> taskFrequencyList, List<DlTaskSourceTarget> taskSourceTargetList,
			List<DlTaskMaster> updateTaskList, List<DlTaskMaster> deactivateTaskList)  throws Throwable {

		LOGGER.info("DLD DAO -- >saveUploaderData");
		Boolean status=false;
		Connection dbConnection = null;
		//Insert
		//PreparedStatement clientStmt = null;
		PreparedStatement entityOwnerStmt = null;
		PreparedStatement entitymasterStmt = null;
		PreparedStatement flowtypeStmt = null;
		PreparedStatement taskRepStmt = null;
		PreparedStatement taskMasterStmt = null;
		PreparedStatement taskFlowtypeStmt = null;
		PreparedStatement taskFrequStmt = null;
		PreparedStatement taskSTStmt = null;
		//Update
		PreparedStatement entityOwnerUpdateStmt = null;
		PreparedStatement entityMasterUpdateStmt = null;
		PreparedStatement flowTypeUpdateStmt = null;
		PreparedStatement taskRepUpdateStmt = null;
		PreparedStatement taskMasterUpdateStmt = null;
		PreparedStatement taskMastereactivateStmt = null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			dbConnection.setAutoCommit(false);

			//Update Entity Owner
			//update DL_ENTITY_OWNER set OWNER_DESCRIPTION=?, EXTERNAL_SOURCE_INDICATOR=?, DATA_SOURCE_ID=?,SOLUTION_ID=?,DISPLAY_ORDER=?,CONTACT_DETAILS=? where CLIENT_CODE=? and OWNER_NAME=? 
			entityOwnerUpdateStmt = dbConnection.prepareStatement(UPDATE_ENTITY_OWNER);
			for(DlEntityOwner entityOwner:updateEntityOwnerList){
				entityOwnerUpdateStmt.setString(1, entityOwner.getDescription());
				entityOwnerUpdateStmt.setString(2, entityOwner.getExternal_Source());
				if(entityOwner.getData_Source_Id()!=null)
					entityOwnerUpdateStmt.setInt(3, entityOwner.getData_Source_Id());
				else
					entityOwnerUpdateStmt.setNull(3, java.sql.Types.INTEGER);
				if(entityOwner.getSolution_Id()!=null)
					entityOwnerUpdateStmt.setInt(4, entityOwner.getSolution_Id());
				else
					entityOwnerUpdateStmt.setNull(4, java.sql.Types.INTEGER);
				entityOwnerUpdateStmt.setString(5, entityOwner.getDisplay_Sorting_Order());
				entityOwnerUpdateStmt.setString(6, entityOwner.getContact_Details());
				//where condition
				entityOwnerUpdateStmt.setString(7,entityOwner.getClientCode());
				entityOwnerUpdateStmt.setString(8,entityOwner.getOwner_Name());
				entityOwnerUpdateStmt.addBatch();
			}
			LOGGER.info("DLD DAO -- >entityOwnerUpdateStmt");

			entityOwnerUpdateStmt.executeBatch();



			//Insert EntityOwner
			entityOwnerStmt = dbConnection.prepareStatement(INSERT_ENTITY_OWNER);
			for(DlEntityOwner entityOwner:insertEntityOwnerList){
				entityOwnerStmt.setString(1, entityOwner.getClientCode());
				entityOwnerStmt.setString(2, entityOwner.getOwner_Name());
				entityOwnerStmt.setString(3, entityOwner.getDescription());
				entityOwnerStmt.setString(4, entityOwner.getExternal_Source());
				if(entityOwner.getData_Source_Id()!=null)
					entityOwnerStmt.setInt(5, entityOwner.getData_Source_Id());
				else
					entityOwnerStmt.setNull(5, java.sql.Types.INTEGER);
				if(entityOwner.getSolution_Id()!=null)
					entityOwnerStmt.setInt(6, entityOwner.getSolution_Id());
				else
					entityOwnerStmt.setNull(6, java.sql.Types.INTEGER);
				entityOwnerStmt.setString(7, entityOwner.getDisplay_Sorting_Order());
				entityOwnerStmt.setString(8, entityOwner.getContact_Details());
				entityOwnerStmt.addBatch();
			}
			LOGGER.info("DLD DAO -- >entityOwnerStmt");

			entityOwnerStmt.executeBatch();

			//Update FlowType
			flowTypeUpdateStmt = dbConnection.prepareStatement(UPDATE_FLOW_TYPES);
			//update DL_FLOW_TYPE set FLOW_DESCRIPTION=? where CLIENT_CODE=? and FLOW_TYPE=?
			for(DlFlowType flowType:updateFlowTypeList){
				flowTypeUpdateStmt.setString(1, flowType.getDescription());
				flowTypeUpdateStmt.setString(2, flowType.getClientCode());
				flowTypeUpdateStmt.setString(3, flowType.getFlowType());
				flowTypeUpdateStmt.addBatch();
			}
			flowTypeUpdateStmt.executeBatch();

			//Insert FlowType
			flowtypeStmt = dbConnection.prepareStatement(INSERT_FLOW_TYPES);
			for(DlFlowType flowType:insertFlowTypeList){
				flowtypeStmt.setString(1, flowType.getClientCode());
				flowtypeStmt.setString(2, flowType.getFlowType());
				flowtypeStmt.setString(3, flowType.getDescription());
				flowtypeStmt.addBatch();
			}
			flowtypeStmt.executeBatch();
			LOGGER.info("DLD DAO -- >flowtypeStmt");


			//Update Task Repository
			taskRepUpdateStmt = dbConnection.prepareStatement(UPDATE_TASK_REP);
			//update DL_TASK_REPOSITORY set REPOSITORY_DESCRIPTION=? where CLIENT_CODE=? and REPOSITORY_NAME=?
			for(DlTaskRepository taskRepository:updateTaskRepList){
				taskRepUpdateStmt.setString(1, taskRepository.getDescription());
				taskRepUpdateStmt.setString(2, taskRepository.getClientCode());
				taskRepUpdateStmt.setString(3, taskRepository.getRepositoryName());
				taskRepUpdateStmt.addBatch();
			}
			taskRepUpdateStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskRepUpdateStmt");


			//Insert Task Repository
			taskRepStmt = dbConnection.prepareStatement(INSERT_TASK_REP);
			for(DlTaskRepository taskRepository:insertTaskRepMasterList){
				taskRepStmt.setString(1, taskRepository.getClientCode());
				taskRepStmt.setString(2, taskRepository.getRepositoryName());
				taskRepStmt.setString(3, taskRepository.getDescription());
				taskRepStmt.addBatch();
			}
			taskRepStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskRepStmt");


			//Update EntityMaster
			entityMasterUpdateStmt = dbConnection.prepareStatement(UPDATE_ENTITY_MASTER);
			//update DL_ENTITY set ENTITY_TYPE=?, ENTITY_DETAIL=?, ENTITY_DESCRIPTION=? where CLIENT_CODE=? and OWNER_NAME=? and ENTITY_NAME=?
			for(DlEntity entityMaster:updateEntityMasterList){
				entityMasterUpdateStmt.setString(1, entityMaster.getEntityType());
				entityMasterUpdateStmt.setString(2, entityMaster.getEntityDetail());
				entityMasterUpdateStmt.setString(3, entityMaster.getDescription());
				entityMasterUpdateStmt.setString(4, entityMaster.getClientCode());
				entityMasterUpdateStmt.setString(5, entityMaster.getOwnerName());
				entityMasterUpdateStmt.setString(6, entityMaster.getEntityName());
				entityMasterUpdateStmt.addBatch();
			}
			taskRepUpdateStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskRepUpdateStmt");

			//Insert Entity Master
			entitymasterStmt = dbConnection.prepareStatement(INSERT_ENTITY_MASTER);
			for(DlEntity entityMaster:insertEntityMasterList){
				entitymasterStmt.setString(1, entityMaster.getClientCode());
				entitymasterStmt.setString(2, entityMaster.getEntityName());
				entitymasterStmt.setString(3, entityMaster.getDescription());
				entitymasterStmt.setString(4, entityMaster.getEntityType());
				entitymasterStmt.setString(5, entityMaster.getEntityDetail());
				entitymasterStmt.setString(6, entityMaster.getOwnerName());
				entitymasterStmt.addBatch();
			}
			entitymasterStmt.executeBatch();
			LOGGER.info("DLD DAO -- >entitymasterStmt");


			//Deactivate Task Master
			taskMastereactivateStmt = dbConnection.prepareStatement(DEACTIVATE_TASK_MASTER);
			//update DL_TASK_MASTER set IS_ACTIVE=? where CLIENT_CODE=? and TASK_REPOSITORY=? and TASK_NAME=? and VERSION_NO=? and EFFECTIVE_START_DATE=?
			for(DlTaskMaster tMaster:deactivateTaskList){
				taskMastereactivateStmt.setString(1,  tMaster.getIsActive());
				//where clause
				taskMastereactivateStmt.setString(2, tMaster.getClientCode());
				taskMastereactivateStmt.setString(3, tMaster.getTaskRepository());
				taskMastereactivateStmt.setString(4, tMaster.getTaskName());
				taskMastereactivateStmt.setInt(5, tMaster.getVersionNo());
				taskMastereactivateStmt.setDate(6, new java.sql.Date(tMaster.getStartDate().getTime()));
				taskMastereactivateStmt.addBatch();
			}
			taskMastereactivateStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskMastereactivateStmt");


			//Update Task Master
			taskMasterUpdateStmt = dbConnection.prepareStatement(UPDATE_TASK_MASTER);
			//update DL_TASK_MASTER set EFFECTIVE_END_DATE=?, TASK_TYPE=?, TASK_DESCRIPTION=?,TECHNICAL_TASK_NAME,TECHNICAL_SUB_TASK_NAME=? where CLIENT_CODE=? and TASK_REPOSITORY=? and TASK_NAME=? and VERSION_NO=? and EFFECTIVE_START_DATE=?
			for(DlTaskMaster tMaster:updateTaskList){
				taskMasterUpdateStmt.setDate(1,  new java.sql.Date(tMaster.getEnddate().getTime()));
				taskMasterUpdateStmt.setString(2, tMaster.getTaskType());
				taskMasterUpdateStmt.setString(3, tMaster.getTaskDescription());
				taskMasterUpdateStmt.setString(4, tMaster.getTechnicalTaskName());
				taskMasterUpdateStmt.setString(5, tMaster.getTechnicalSubTaskName());
				taskMasterUpdateStmt.setString(6, tMaster.getIsValidationRequired());
				//where clause
				taskMasterUpdateStmt.setString(7, tMaster.getClientCode());
				taskMasterUpdateStmt.setString(8, tMaster.getTaskRepository());
				taskMasterUpdateStmt.setString(9, tMaster.getTaskName());
				taskMasterUpdateStmt.setInt(10, tMaster.getVersionNo());
				taskMasterUpdateStmt.setDate(11, new java.sql.Date(tMaster.getStartDate().getTime()));
				taskMasterUpdateStmt.addBatch();
			}
			taskMasterUpdateStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskMasterUpdateStmt");


			//Insert TaskMaster
			taskMasterStmt = dbConnection.prepareStatement(INSERT_TASK_MASTER);
			for(DlTaskMaster tMaster:tMasterList){
				taskMasterStmt.setString(1, tMaster.getClientCode());
				taskMasterStmt.setString(2, tMaster.getTaskDescription());
				taskMasterStmt.setString(3, tMaster.getTaskName());
				taskMasterStmt.setString(4, tMaster.getTaskRepository());
				taskMasterStmt.setString(5, tMaster.getTaskType());
				taskMasterStmt.setString(6, tMaster.getTechnicalTaskName());
				taskMasterStmt.setString(7, tMaster.getTechnicalSubTaskName());
				taskMasterStmt.setString(8, tMaster.getIsActive());
				taskMasterStmt.setDate(9, new java.sql.Date(tMaster.getStartDate().getTime()));
				taskMasterStmt.setDate(10, new java.sql.Date(tMaster.getEnddate().getTime()));
				taskMasterStmt.setInt(11, tMaster.getVersionNo());
				taskMasterStmt.setString(12, tMaster.getIsValidationRequired());
				taskMasterStmt.addBatch();
			}
			taskMasterStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskMasterStmt");


			//Insert TaskFlowType
			//stmt = dbConnection.prepareStatement(INSERT_TASK_FLOW_TYPE);
			taskFlowtypeStmt = dbConnection.prepareStatement(INSERT_TASK_FLOW_TYPE);
			for(DlTaskFlowType taskFlowType:taskFlowTypeList){
				taskFlowtypeStmt.setString(1, taskFlowType.getClientCode());
				taskFlowtypeStmt.setInt(2, taskFlowType.getVersionNo());
				taskFlowtypeStmt.setString(3, taskFlowType.getTaskName());
				taskFlowtypeStmt.setString(4, taskFlowType.getTaskRepository());
				taskFlowtypeStmt.setString(5, taskFlowType.getFlowType());
				taskFlowtypeStmt.addBatch();
			}
			taskFlowtypeStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskFlowtypeStmt");

			//INSERT TaskFrequency
			taskFrequStmt = dbConnection.prepareStatement(INSERT_TASK_FREQUENCY);
			for(DlTaskFrequency taskFreq:taskFrequencyList){
				taskFrequStmt.setString(1, taskFreq.getClientCode());
				taskFrequStmt.setInt(2, taskFreq.getVersionNo());
				taskFrequStmt.setString(3, taskFreq.getTaskName());
				taskFrequStmt.setString(4, taskFreq.getTaskRepository());
				taskFrequStmt.setString(5, taskFreq.getFrequency());
				taskFrequStmt.setInt(6, taskFreq.getOffset());
				taskFrequStmt.setString(7, taskFreq.getIsExclusionInd());
				taskFrequStmt.addBatch();
			}
			taskFrequStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskFrequStmt");



			//INSERT TaskSourceTarget
			taskSTStmt = dbConnection.prepareStatement(INSERT_TASK_SOURCE_TARGET);
			for(DlTaskSourceTarget taskSourceTarget:taskSourceTargetList){
				taskSTStmt.setString(1, taskSourceTarget.getClientCode());
				taskSTStmt.setInt(2, taskSourceTarget.getVersionNo());
				taskSTStmt.setString(3, taskSourceTarget.getTaskname());
				taskSTStmt.setString(4, taskSourceTarget.getTaskRepository());
				taskSTStmt.setString(5, taskSourceTarget.getOwnerName());
				taskSTStmt.setString(6, taskSourceTarget.getEntityName());
				taskSTStmt.setString(7, taskSourceTarget.getLinkType());
				LOGGER.info("DLD DAO -- >"+taskSourceTarget.getTaskname()+ " -- " + taskSourceTarget.getEntityName() + " -- "+taskSourceTarget.getLinkType());

				taskSTStmt.execute();
				//taskSTStmt.addBatch();
			}
			//taskSTStmt.executeBatch();
			LOGGER.info("DLD DAO -- >taskSTStmt");

			dbConnection.commit();
			LOGGER.info("DLD DAO -- >commit");

			status=true;
		} catch (Throwable e) {
			status=false;
			dbConnection.rollback();
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(entityOwnerStmt);
			closeStatement(flowtypeStmt);
			closeStatement(entitymasterStmt);
			closeStatement(taskRepStmt);
			closeStatement(taskMasterStmt);
			closeStatement(taskFlowtypeStmt);
			closeStatement(taskFrequStmt);
			closeStatement(taskSTStmt);
			closeStatement(entityOwnerUpdateStmt);
			closeStatement(entityMasterUpdateStmt);
			closeStatement(flowTypeUpdateStmt);
			closeStatement(taskRepUpdateStmt);
			closeStatement(taskMasterUpdateStmt);
			closeStatement(taskMastereactivateStmt);
			closeConnection(dbConnection);
		}

		return status;
	}

	@Override
	public List<StagingDetails> getUnplannedTaskDetails(DateTime dt,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getUnplannedTaskDetails");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		List<StagingDetails> tskexecLogList=new ArrayList<StagingDetails>();
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(UNPLANNED_TASK_DETAILS_FOR_CBD);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(1, cbd);
			stmt.setDate(2, cbd);
			stmt.setString(3, ADHOC_FREQUENCY_NAME.toUpperCase());
			stmt.setDate(4, cbd);
			stmt.setString(5, clientCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				StagingDetails task = new StagingDetails();
				task.setTaskName(rs.getString("TASK_NAME"));
				task.setClientCode(rs.getString("CLIENT_CODE"));
				task.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				task.setTaskStatus(rs.getString("TASK_STATUS"));
				task.setRunCount(rs.getInt("RUN_COUNT"));
				task.setStartDateTime(rs.getDate("START_DATE_TIME"));
				task.setEndDateTime(rs.getDate("END_DATE_TIME"));
				task.setTaskTechnicalName(rs.getString("TECHNICAL_TASK_NAME"));
				task.setRunDetails(rs.getString("RUN_DETAILS"));

				tskexecLogList.add(task);
			}
			return tskexecLogList;

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
	}

	@Override
	public List<TaskExecutionLog> getSubGridTaskDetails(DateTime businessDateRec, String taskName, String repoName,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getSubGridTaskDetails");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		List<TaskExecutionLog> tskexecLogList=new LinkedList<TaskExecutionLog>();
		try {

			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(SUB_GRID_DATA);
			java.sql.Date cbd=new java.sql.Date(businessDateRec.toDate().getTime());
			stmt.setDate(1, cbd);
			stmt.setString(2, taskName.toUpperCase());
			stmt.setString(3, clientCode);
			stmt.setString(4, repoName.toUpperCase());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				TaskExecutionLog tskexec=new TaskExecutionLog();
				tskexec.setBusinessDate(rs.getDate("business_period_date"));
				tskexec.setClientCode(rs.getString("client_code"));
				tskexec.setEndDate(rs.getTimestamp("end_date_time"));
				tskexec.setTaskRepository(rs.getString("task_repository"));
				tskexec.setTaskName(rs.getString("task_name"));
				tskexec.setFlowType(rs.getString("flow_type"));
				tskexec.setFlowSequenceNumber(rs.getInt("flow_sequence_no"));
				tskexec.setTechnicalTaskName(rs.getString("technical_task_name"));
				tskexec.setTechnicalSubTaskName(rs.getString("technical_sub_task_name"));
				tskexec.setTaskStatus(rs.getString("task_status"));
				tskexec.setRunPeriodDate(rs.getDate("run_period_date"));
				tskexec.setRunDetails(rs.getString("run_details"));
				tskexec.setStartDate(rs.getTimestamp("start_date_time"));
				tskexec.setEndDate(rs.getTimestamp("end_date_time"));
				tskexec.setSourceCount(rs.getInt("source_count"));
				tskexec.setTargetCount(rs.getInt("target_count"));
				tskexec.setTargetInsertedCount(rs.getInt("target_inserted_count"));
				tskexec.setTargetUpdatedCount(rs.getInt("target_updated_count"));
				tskexec.setTargetRejectedRecord(rs.getInt("target_rejected_record"));
				tskexecLogList.add(tskexec);
			}
			return tskexecLogList;

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);

			closeConnection(dbConnection);
		}
	}

	@Override
	public Map<String,Set<String>> getReportAndEntityMap(DateTime cbd,String solutionName,Integer solutionId,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getReportAndEntityMap");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		Set<Integer> ruleIds=new HashSet<Integer>();
		Map<String,Set<Integer>> mapOfReportAndRuleId = new HashMap<String,Set<Integer>>();
		Map<String,Set<String>> mapOfReportIDAndEntitityName = new HashMap<String,Set<String>>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solutionName,clientCode);

			stmt = dbConnection.prepareStatement(GET_REPORT_AND_ENTITYMAP);
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
			String periodDate=fmt.print(cbd);
			stmt.setInt(1, solutionId);
			stmt.setInt(2, Integer.parseInt(periodDate));
			java.sql.Date dt=new java.sql.Date(cbd.toDate().getTime());
			stmt.setDate(3, dt);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				//processing rule id and report id
				ruleIds = new HashSet<Integer>();
				String ruleIdCsv=rs.getString("RULE_LIST");
				if(null!=ruleIdCsv){
					for(String ruleId:ruleIdCsv.split(","))
					{
						ruleIds.add(Integer.parseInt(ruleId));
					}
					if(null==mapOfReportAndRuleId.get(rs.getInt("REG_REPORT_ID")+ SEPARATOR + rs.getString("REPORT_NAME"))){
						mapOfReportAndRuleId.put(rs.getInt("REG_REPORT_ID")+ SEPARATOR + rs.getString("REPORT_NAME"), ruleIds);
					}else{
						mapOfReportAndRuleId.get(rs.getInt("REG_REPORT_ID")+ SEPARATOR + rs.getString("REPORT_NAME")).addAll(ruleIds);
					}
				}
				
				

			}
			for (Map.Entry<String, Set<Integer>> entry : mapOfReportAndRuleId.entrySet()) {
				mapOfReportAndRuleId.put(entry.getKey(), getParentRuleIdsForDerivedRule(cbd, solutionName, clientCode, solutionId, entry.getValue()));
			}

			for (Map.Entry<String, Set<Integer>> entry : mapOfReportAndRuleId.entrySet()) {
				//looping through map of report and rule id to get underlying fact name.
				Set<String> entityNameSet = new HashSet<String>();
				Set<Integer> value =  entry.getValue();
				String reportIdName = entry.getKey();
				int j=1;
				String inCls="";
				String q=GET_ENTITIES_FOR_RULE;
				for(Integer val:value)
				{
                    if(j==99){
                        j=1;
                        inCls=inCls+val+",";
                        q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                        j++;
                        inCls="OR RULE_ID IN "+OPEN_BRACKET;
                    }
                    else{
                        j++;
                        inCls=inCls+val+",";
                    }
				}
				
				if(inCls.length()>17){
                    q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
				}
				else 
					if(!inCls.substring(0,inCls.length()-1).equalsIgnoreCase("OR RULE_ID IN  ("))
						q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                q=q+CLOSE_BRACKET;
                String queryForEntities = q;
				stmt1  = dbConnection.prepareStatement(queryForEntities);
				stmt1.setDate(1, dt);
				stmt1.setInt(2, solutionId);

				ResultSet rs1 = stmt1.executeQuery();
				while (rs1.next()) {
					entityNameSet.add(rs1.getString("FACT_NAME").trim());
				}
				if(null==mapOfReportIDAndEntitityName.get(reportIdName)){
					mapOfReportIDAndEntitityName.put(reportIdName, entityNameSet);
				}else{
					mapOfReportIDAndEntitityName.get(reportIdName).addAll(entityNameSet);
				}
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeStatement(stmt1);
			closeConnection(dbConnection);
		}
		return mapOfReportIDAndEntitityName;

	}

	@Override
	public Map<String, String> getReportAndLineItemFlag(Integer solutionId,String solutionName,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getReportAndLineItemFlag");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Map<String,String> reportNameFlagMap = new HashMap<String, String>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solutionName,clientCode);
			stmt = dbConnection.prepareStatement(REPORT_AND_FLAG);
			stmt.setInt(1, solutionId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				reportNameFlagMap.put(rs.getString("REPORT_NAME")+SEPARATOR+rs.getInt("SOLUTION_ID"), rs.getString("SHOW_LINE_ITEM_DETAILS"));
			}

			return reportNameFlagMap;

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);

			closeConnection(dbConnection);
		}
	}

	@Override
	public Map<String, Set<String>> getLineItemEntityMap(
			DateTime businessDateRec, DldSolution solution,Integer reportId, Integer pageNo,
			Integer pageSize,String lineItemIdSearch,String lineItemDescSearch,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getLineItemEntityMap");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		Set<Integer> ruleIds=new HashSet<Integer>();
		Map<String,Set<Integer>> mapOfLineItemAndRuleId = new HashMap<String,Set<Integer>>();
		Map<String,Set<String>> mapOfReportIDAndEntitityName = new HashMap<String,Set<String>>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solution.getSolutionName(),clientCode);
			String query = "";
			if(dbConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase(ORACLE)){
				query = LINE_ITEM_DETAILS;
			}else{
				query = LINE_ITEM_DETAILS_FOR_MSSQL;
			}
			stmt = dbConnection.prepareStatement(query);
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
			String periodDate=fmt.print(businessDateRec);
			java.sql.Date dt=new java.sql.Date(businessDateRec.toDate().getTime());
			stmt.setInt(1, solution.getSolutionID());
			stmt.setInt(2, Integer.parseInt(periodDate));
			stmt.setInt(3, reportId);
			Integer recEnd = (pageNo*pageSize);
			Integer recStart = recEnd-pageSize+1;
			stmt.setString(4,"%"+lineItemIdSearch+"%");
			stmt.setString(5,"%"+lineItemDescSearch+"%");
			stmt.setInt(6,recStart );
			stmt.setInt(7,recEnd );
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				//processing rule id and report id
				ruleIds = new HashSet<Integer>();
				String ruleIdCsv=rs.getString("RULE_LIST");
				if(null!=ruleIdCsv){
					for(String ruleId:ruleIdCsv.split(","))
					{
						ruleIds.add(Integer.parseInt(ruleId));
					}
					if(null==mapOfLineItemAndRuleId.get(rs.getInt("LINE_ITEM_ID")+ SEPARATOR + rs.getString("LINE_ITEM_DESC"))){
						mapOfLineItemAndRuleId.put(rs.getInt("LINE_ITEM_ID")+ SEPARATOR + rs.getString("LINE_ITEM_DESC"), ruleIds);
					}else{
						mapOfLineItemAndRuleId.get(rs.getInt("LINE_ITEM_ID")+ SEPARATOR + rs.getString("LINE_ITEM_DESC")).addAll(ruleIds);
					}
				}
			}
			
			for (Map.Entry<String, Set<Integer>> entry : mapOfLineItemAndRuleId.entrySet()) {
				mapOfLineItemAndRuleId.put(entry.getKey(), getParentRuleIdsForDerivedRule(businessDateRec, solution.getSolutionName(), clientCode, solution.getSolutionID(), entry.getValue()));
			}

			for (Map.Entry<String, Set<Integer>> entry : mapOfLineItemAndRuleId.entrySet()) {
				//looping through map of report and rule id to get underlying fact name.
				Set<String> entityNameSet = new HashSet<String>();
				Set<Integer> value =  entry.getValue();
				String lineItemIdName = entry.getKey();
				int j=1;
				String inCls="";
				String q=GET_ENTITIES_FOR_RULE;
				for(Integer val:value)
				{
                    if(j==99){
                        j=1;
                        inCls=inCls+val+",";
                        q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                        j++;
                        inCls="OR RULE_ID IN "+OPEN_BRACKET;
                    }
                    else{
                        j++;
                        inCls=inCls+val+",";
                    }
				}
				
				if(inCls.length()>17){
                    q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
				}
				else 
					if(!inCls.substring(0,inCls.length()-1).equalsIgnoreCase("OR RULE_ID IN  ("))
						q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                q=q+CLOSE_BRACKET;
				
                String queryForEntities = q;
				stmt1  = dbConnection.prepareStatement(queryForEntities);
				stmt1.setDate(1, dt);
				stmt1.setInt(2, solution.getSolutionID());

				ResultSet rs1 = stmt1.executeQuery();
				while (rs1.next()) {
					entityNameSet.add(rs1.getString("FACT_NAME").trim());
				}
				if(null==mapOfReportIDAndEntitityName.get(lineItemIdName)){
					mapOfReportIDAndEntitityName.put(lineItemIdName, entityNameSet);
				}else{
					mapOfReportIDAndEntitityName.get(lineItemIdName).addAll(entityNameSet);
				}
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeStatement(stmt1);
			closeConnection(dbConnection);
		}
		return mapOfReportIDAndEntitityName;

	}
	public boolean isValidationOnTaskRequired(String taskName){

		LOGGER.info("DLD DAO -- >isValidationOnTaskRequired");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		//query execution.
		boolean isValidation=false;
		try {

			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(IS_VALIDATION_REQUIRED_ON_TASK);
			java.sql.Date cbd=new java.sql.Date(new java.util.Date().getTime());
			stmt.setDate(2, cbd);
			stmt.setString(1, taskName.toUpperCase());
			ResultSet rs = stmt.executeQuery();
			String isValidationRequired="";
			while (rs.next()) {
				isValidationRequired=rs.getString(1);
			}
			if(isValidationRequired!=null &&"Y".equalsIgnoreCase( isValidationRequired)){
				isValidation=true;
			} else {
				isValidation=false;
			}


		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);

			closeConnection(dbConnection);
		}
		return isValidation;
	}
	@Override
	public ClientUploaderDTO getClientMaster(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- > getClientMaster");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		ClientUploaderDTO clientUploaderDTO=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_CLIENT_MASTER);
			stmt.setString(1,clientCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				clientUploaderDTO=new ClientUploaderDTO();
				clientUploaderDTO.setClientCode(rs.getString("CLIENT_CODE"));
				clientUploaderDTO.setClientName(rs.getString("CLIENT_DESCRIPTION"));
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return clientUploaderDTO;
	}

	public Integer getMaxVersionNoForTask(String clientCode, String task_Repository, String task_Name)  throws Throwable{

		LOGGER.info("DLD DAO -- > getMaxVersionNoForTask");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Integer versionNo=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_TASK_MAX_VERSION);
			stmt.setString(1,clientCode);
			stmt.setString(2,task_Repository.toUpperCase());
			stmt.setString(3,task_Name.toUpperCase());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				versionNo=rs.getInt("VERSION_NO");
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return versionNo;
	}


	public List<DlTaskMaster> getTaskForEdit(String clientCode, String task_Repository, String task_Name) throws Throwable{

		LOGGER.info("DLD DAO -- >getTaskForEdit");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskMaster tMaster=null;
		List<DlTaskMaster> dlTaskMasterList=new ArrayList<DlTaskMaster>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ALL_TASK);
			stmt.setString(1, clientCode);
			stmt.setString(2, task_Repository.toUpperCase());	
			stmt.setString(3, task_Name.toUpperCase());
			stmt.setString(4, "Y");

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				tMaster=new DlTaskMaster();
				tMaster.setClientCode(rs.getString("CLIENT_CODE"));
				tMaster.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				tMaster.setTaskName(rs.getString("TASK_NAME"));
				tMaster.setVersionNo(rs.getInt("VERSION_NO"));
				tMaster.setStartDate(rs.getDate("EFFECTIVE_START_DATE"));
				tMaster.setEnddate(rs.getDate("EFFECTIVE_END_DATE"));
				tMaster.setIsActive(rs.getString("IS_ACTIVE"));
				tMaster.setTaskType(rs.getString("TASK_TYPE"));
				tMaster.setTaskDescription(rs.getString("TASK_DESCRIPTION"));
				tMaster.setTechnicalTaskName(rs.getString("TECHNICAL_TASK_NAME"));
				tMaster.setTechnicalSubTaskName(rs.getString("TECHNICAL_SUB_TASK_NAME"));
				tMaster.setIsValidationRequired(rs.getString("IS_VALIDATION_REQUIRED"));
				dlTaskMasterList.add(tMaster);
			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return dlTaskMasterList;


	}

	@Override
	public DlEntityOwner getEntityOwner(String clientCode, String owner_Name) throws Throwable {

		LOGGER.info("DLD DAO --> getEntityOwner");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlEntityOwner entityOwner=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ENTITY_OWNER);
			stmt.setString(1, clientCode);
			stmt.setString(2, owner_Name.toUpperCase());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				entityOwner=new DlEntityOwner();
				entityOwner.setClientCode(rs.getString("CLIENT_CODE"));
				entityOwner.setOwner_Name(rs.getString("OWNER_NAME"));
				entityOwner.setExternal_Source(rs.getString("EXTERNAL_SOURCE_INDICATOR"));
				entityOwner.setDescription(rs.getString("OWNER_DESCRIPTION"));
				entityOwner.setData_Source_Id(rs.getInt("DATA_SOURCE_ID"));
				Integer dsVal=rs.getInt("DATA_SOURCE_ID");
				if(rs.wasNull())
				{
					entityOwner.setData_Source_Id(null);
				}
				else
					entityOwner.setData_Source_Id(dsVal);

				Integer solVal=rs.getInt("SOLUTION_ID");
				if(rs.wasNull())
				{
					entityOwner.setSolution_Id(null);
				}
				else
					entityOwner.setSolution_Id(solVal);

				entityOwner.setDisplay_Sorting_Order(rs.getString("DISPLAY_ORDER"));
				entityOwner.setContact_Details(rs.getString("CONTACT_DETAILS"));

			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return entityOwner;

	}

	@Override
	public DlEntity getEntityMaster(String clientCode, String owner_Name, String entity_Name) throws Throwable {
		LOGGER.info("DLD DAO --> getEntityMaster");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlEntity entityMaster=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ENTITY_MASTER);
			stmt.setString(1, clientCode);
			stmt.setString(2, owner_Name.toUpperCase());
			stmt.setString(3, entity_Name.toUpperCase());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				entityMaster=new DlEntity();
				entityMaster.setClientCode(rs.getString("CLIENT_CODE"));
				entityMaster.setOwnerName(rs.getString("OWNER_NAME"));
				entityMaster.setEntityDetail(rs.getString("ENTITY_DETAIL"));
				entityMaster.setDescription(rs.getString("ENTITY_DESCRIPTION"));
				entityMaster.setEntityName(rs.getString("ENTITY_NAME"));
				entityMaster.setEntityType(rs.getString("ENTITY_TYPE"));
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return entityMaster;
	}

	@Override
	public DlFlowType getDlFlowType(String clientCode, String flow_type) throws Throwable {
		LOGGER.info("DLD DAO --> getDlFlowType");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlFlowType flowType=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_FLOW_TYPE);
			stmt.setString(1, clientCode);
			stmt.setString(2, flow_type.toUpperCase());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				flowType=new DlFlowType();
				flowType.setClientCode(rs.getString("CLIENT_CODE"));
				flowType.setFlowType(rs.getString("FLOW_TYPE"));
				flowType.setDescription(rs.getString("FLOW_DESCRIPTION"));
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return flowType;
	}

	@Override
	public DlTaskRepository getTaskRepository(String clientCode, String name) throws Throwable {
		LOGGER.info("DLD DAO --> getTaskRepository");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskRepository taskRepository=null;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_REPOSITORY);
			stmt.setString(1, clientCode);
			stmt.setString(2, name.toUpperCase());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskRepository=new DlTaskRepository();
				taskRepository.setClientCode(rs.getString("CLIENT_CODE"));
				taskRepository.setRepositoryName(rs.getString("REPOSITORY_NAME"));
				taskRepository.setDescription(rs.getString("REPOSITORY_DESCRIPTION"));
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskRepository;
	}

	@Override
	public Integer getLineItemTotalCount(DateTime businessDateRec,
			DldSolution solution, Integer reportId,String lineItemIdSearch,String lineItemDescSearch ,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getLineItemTotalCount");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Integer total = 0;
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solution.getSolutionName(),clientCode);
			stmt = dbConnection.prepareStatement(LINE_ITEM_COUNT);
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
			String periodDate=fmt.print(businessDateRec);
			stmt.setInt(1, solution.getSolutionID());
			stmt.setInt(2, Integer.parseInt(periodDate));
			stmt.setInt(3, reportId);
			stmt.setString(4,"%"+lineItemIdSearch+"%");
			stmt.setString(5,"%"+lineItemDescSearch+"%");
			
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				total = rs.getInt(1);				
			}
		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return total;

	}

	@Override
	public List<String> getAllTaskType(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getAllTaskType");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		List<String> taskTypeList=new ArrayList<String>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_TASK_TYPE);
			stmt.setString(1, clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskTypeList.add(rs.getString("TASK_TYPE"));
			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskTypeList;
	}

	@Override
	public List<String> getAllentityType(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getAllentityType");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		List<String> entityTypeList=new ArrayList<String>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ENTITY_TYPE);
			stmt.setString(1, clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				entityTypeList.add(rs.getString("ENTITY_TYPE"));
			}

		} catch (Throwable e) {
			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return entityTypeList;
	}
	
	@Override
	public List<DlTaskSourceTarget> getDependentTask(String clientCode,String repositryName,Integer versionNo,String taskName) throws Throwable {
		LOGGER.info("DLD DAO -- >getSourceTargetData");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		DlTaskSourceTarget taskSourceTarget=null;
		List<DlTaskSourceTarget> taskSourceTargetList=new ArrayList<DlTaskSourceTarget>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(GET_ST_DATA);
			stmt.setString(1,taskName.toUpperCase());
			stmt.setString(2,clientCode);
			stmt.setString(3,repositryName.toUpperCase());
			stmt.setInt(4,versionNo);
			stmt.setString(5,clientCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				taskSourceTarget=new DlTaskSourceTarget();
				taskSourceTarget.setClientCode(rs.getString("CLIENT_CODE"));
				taskSourceTarget.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				taskSourceTarget.setTaskname(rs.getString("TASK_NAME"));
				taskSourceTarget.setVersionNo(rs.getInt("VERSION_NO"));
				taskSourceTarget.setOwnerName(rs.getString("OWNER_NAME"));
				taskSourceTarget.setEntityName(rs.getString("ENTITY_NAME"));
				taskSourceTarget.setLinkType(rs.getString("LINK_TYPE"));
				taskSourceTargetList.add(taskSourceTarget);
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return taskSourceTargetList;
	}
	
	@Override
	public boolean isValidationRequiredOnTask(String clientCode,String taskName,String taskRepo,java.util.Date businessDate ) throws Throwable {
		LOGGER.info("DLD DAO -- >isValidationRequiredOnTask");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(TASK_VALIDATION);
			stmt.setString(1,taskName);
			stmt.setString(3,clientCode);
			stmt.setString(4,taskRepo);
			stmt.setDate(2, new java.sql.Date(businessDate.getTime()));

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if(Y.equalsIgnoreCase(rs.getString("IS_VALIDATION_REQUIRED"))){
					return true;
				}else {
					return false;
				}
			}
		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return false;
	}
	
	@Override
	public String getTaskStatusForDataLinage(String clientCode,String taskName,String taskRepo,java.util.Date businessDate ) throws Throwable {
		LOGGER.info("DLD DAO -- >getTaskStatusForDataLinage");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String status="";
		try {
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(TASK_STATUS_LINAGE);
			stmt.setString(1,taskName.toUpperCase());
			stmt.setString(2,clientCode);
			stmt.setString(3,taskRepo.toUpperCase());
			stmt.setDate(4, new java.sql.Date(businessDate.getTime()));

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if("COMPLETED".equalsIgnoreCase(rs.getString("TASK_STATUS").trim())){
					status= "COMPLETED";
				} else if("FAILED".equalsIgnoreCase(rs.getString("TASK_STATUS").trim())) {
					status= "FAILED";
				}
			}
			
			if(status!=null && "".equalsIgnoreCase(status)){
				status="PENDING";	
			}
		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return status;
	}

	@Override
	public List<DldSolution> getExcludedSolution(String solutionName) throws Throwable {
		List<DldSolution> solutionList = new ArrayList<DldSolution>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String soultionQuery = EXCLUDED_SOLUTION;
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(soultionQuery);
			stmt.setString(1, solutionName.toUpperCase());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				DldSolution sol=new DldSolution();
				sol.setSolutionName(rs.getString("SOLUTIONNAME"));
				sol.setSolutionID(rs.getInt("SOLUTIONID"));
				sol.setIsActive(rs.getBoolean("ISACTIVE"));
				sol.setSolutionDescription(rs.getString("SOLUTIONDESCRIPTION"));
				sol.setProductID(rs.getInt("PRODUCTID"));
				sol.setBelongsTo(rs.getString("BELONGSTO"));
				sol.setBiType(rs.getString("BITYPE"));
				sol.setIsSecurityFilterActive(rs.getBoolean("ISSFACTIVE"));
				solutionList.add(sol);
			}
		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return solutionList;	

	}
	@Override
	public String isTaskExecutable(String taskName,String clientCode) throws Throwable {
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String soultionQuery = IS_TASK_EXECUTABLE;
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(soultionQuery);
			stmt.setString(1, taskName.toUpperCase());
			stmt.setString(2, clientCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if(Y.equalsIgnoreCase(rs.getString("IS_EXECUTABLE_TASK"))){
					return Y;
				} else {
					return "N";
				}
			}
		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return null;
	}

	
	
	
	@Override
	public Set<String> getAllFrequecy(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getAllFrequecy");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Set<String> listFrequency=new HashSet<String>();
		String freqQuery = ALLFREQUENCIES;
		try {
			dbConnection = PersistentStoreManager.getPrimaryAppDBConnection(clientCode);
			stmt = dbConnection.prepareStatement(freqQuery);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				listFrequency.add(rs.getString("frequency_name").toLowerCase());
			}
			listFrequency.add(ADHOC_FREQUENCY_NAME.toLowerCase());
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return listFrequency;
	}
	
	

	@Override
	public Map<String, Set<String>> getLineItemAllEntityMap(
			DateTime businessDateRec, Integer solutionId,Integer reportId, String solutionName,
			String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getLineItemAllEntityMap");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		Set<Integer> ruleIds=new HashSet<Integer>();
		Map<String,Set<Integer>> mapOfLineItemAndRuleId = new HashMap<String,Set<Integer>>();
		Map<String,Set<String>> mapOfReportIDAndEntitityName = new HashMap<String,Set<String>>();
		//query execution.
		try {
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solutionName,clientCode);
			String query = "";
			if(dbConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase(ORACLE)){
				query = ALL_LINE_ITEM_DETAILS;
			}else{
				query = ALL_LINE_ITEM_DETAILS_FOR_MSSQL;
			}
			stmt = dbConnection.prepareStatement(query);
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
			String periodDate=fmt.print(businessDateRec);
			java.sql.Date dt=new java.sql.Date(businessDateRec.toDate().getTime());
			stmt.setInt(1, solutionId);
			stmt.setInt(2, Integer.parseInt(periodDate));
			stmt.setInt(3, reportId);
			stmt.setString(4,"%%");
			stmt.setString(5,"%%");
			
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				//processing rule id and report id
				ruleIds = new HashSet<Integer>();
				String ruleIdCsv=rs.getString("RULE_LIST");
				if(null!=ruleIdCsv){
					for(String ruleId:ruleIdCsv.split(","))
					{
						ruleIds.add(Integer.parseInt(ruleId));
					}
					if(null==mapOfLineItemAndRuleId.get(rs.getInt("LINE_ITEM_ID")+ SEPARATOR + rs.getString("LINE_ITEM_DESC"))){
						mapOfLineItemAndRuleId.put(rs.getInt("LINE_ITEM_ID")+ SEPARATOR + rs.getString("LINE_ITEM_DESC"), ruleIds);
					}else{
						mapOfLineItemAndRuleId.get(rs.getInt("LINE_ITEM_ID")+ SEPARATOR + rs.getString("LINE_ITEM_DESC")).addAll(ruleIds);
					}
				}
			}
			for (Map.Entry<String, Set<Integer>> entry : mapOfLineItemAndRuleId.entrySet()) {
				mapOfLineItemAndRuleId.put(entry.getKey(), getParentRuleIdsForDerivedRule(businessDateRec, solutionName, clientCode, solutionId, entry.getValue()));
			}

			for (Map.Entry<String, Set<Integer>> entry : mapOfLineItemAndRuleId.entrySet()) {
				//looping through map of report and rule id to get underlying fact name.
				Set<String> entityNameSet = new HashSet<String>();
				Set<Integer> value =  entry.getValue();
				String lineItemIdName = entry.getKey();
				int j=1;
				String inCls="";
				String q=GET_ENTITIES_FOR_RULE;
				for(Integer val:value)
				{
                    if(j==99){
                        j=1;
                        inCls=inCls+val+",";
                        q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                        j++;
                        inCls="OR RULE_ID IN "+OPEN_BRACKET;
                    }
                    else{
                        j++;
                        inCls=inCls+val+",";
                    }
				}
				
				if(inCls.length()>17){
                    q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
				}
				else 
					if(!inCls.substring(0,inCls.length()-1).equalsIgnoreCase("OR RULE_ID IN  ("))
						q=q+inCls.substring(0,inCls.length()-1)+CLOSE_BRACKET;
                q=q+CLOSE_BRACKET;
				
                String queryForEntities = q;
				stmt1  = dbConnection.prepareStatement(queryForEntities);
				stmt1.setDate(1, dt);
				stmt1.setInt(2, solutionId);

				ResultSet rs1 = stmt1.executeQuery();
				while (rs1.next()) {
					entityNameSet.add(rs1.getString("FACT_NAME").trim());
				}
				if(null==mapOfReportIDAndEntitityName.get(lineItemIdName)){
					mapOfReportIDAndEntitityName.put(lineItemIdName, entityNameSet);
				}else{
					mapOfReportIDAndEntitityName.get(lineItemIdName).addAll(entityNameSet);
				}
			}

		} catch (Throwable e) {

			LOGGER.error("Unable to read from database",e);
			throw new Throwable();

		} finally {
			closeStatement(stmt);
			closeStatement(stmt1);
			closeConnection(dbConnection);
		}
		return mapOfReportIDAndEntitityName;

	}
	
	
	private Set<Integer> getParentRuleIdsForDerivedRule(DateTime currentBusinessDate,String solutionName,String clientCode,Integer solutionId,Set<Integer> ruleIds) throws Throwable{

		LOGGER.info("DLD DAO -- >getParentRuleIdsForDerivedRule");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Set<Integer> tempRuleIds = new HashSet<>();
		try {
			
			String sj="";
			String mainInClause="";
			if(ruleIds.size()>1000){
				int i=0;
				for(Integer j:ruleIds){
					if(i<=1000){
						sj=sj+j+",";
						if(i==1000){
							mainInClause=sj.substring(0, sj.length()-1)+") OR RULE_ID IN(";
							sj="";
							i=0;
						}
						
					} 
					i++;
				}
				if(mainInClause.length()>0)
					mainInClause=mainInClause.substring(0, mainInClause.length()-15)+")";
				
			} else {
				for(Integer j:ruleIds){
					mainInClause=mainInClause+j+",";
				}
				mainInClause=mainInClause.substring(0, mainInClause.length()-1)+"))";
			}
			
			dbConnection = PersistentStoreManager.getSolutionAppDBConnection(solutionName, clientCode);
			stmt = dbConnection.prepareStatement(GET_PARENT_RULE_IDS+mainInClause);
			stmt.setInt(1, solutionId);
			stmt.setDate(2, new Date(currentBusinessDate.getMillis()));
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if(null!=rs.getString("RULE_LIST")){
					for(String tempRuleId:rs.getString("RULE_LIST").split(",")){
						if(tempRuleId!=null && !"".equalsIgnoreCase(tempRuleId)){
							tempRuleIds.add(Integer.parseInt(tempRuleId));
						}
					}
				}
			}
			
			if(!ruleIds.containsAll(tempRuleIds)){
				ruleIds.addAll(tempRuleIds);
				return getParentRuleIdsForDerivedRule(currentBusinessDate, solutionName, clientCode, solutionId, ruleIds);
			} else {
				return ruleIds;
			}
			
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
	}
	
	@Override
	public Map<String,String> getAllActualNameFrequency(String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getAllFrequecy");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Map<String,String> listFrequency=new HashMap<String, String>();
		String freqQuery = ALLFREQUENCIES;
		try {
			dbConnection = PersistentStoreManager.getPrimaryAppDBConnection(clientCode);
			stmt = dbConnection.prepareStatement(freqQuery);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				listFrequency.put(rs.getString("frequency_name").toLowerCase(),rs.getString("frequency_name"));
			}
			listFrequency.put(ADHOC_FREQUENCY_NAME.toLowerCase(),ADHOC_FREQUENCY_NAME);
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return listFrequency;
	}
	
	@Override
	public List<TaskFrequencyDetail> getTotalNumberOfApplicableTasksForLineage(Set<String> freqType,String clientCode,DateTime dt) throws Throwable {

		LOGGER.info("DLD DAO -- >getTotalNumberOfApplicableTasksForLineage");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		List<TaskFrequencyDetail> listTaskFrequencyDetail=new ArrayList<TaskFrequencyDetail>();
		try {
			String taskQuery= TASK_QUERY_FOR_LINEAGE;
			taskQuery = taskQuery+("'"+Joiner.on("','").join(freqType)+"'").toUpperCase();
			taskQuery=taskQuery+")";
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(taskQuery);
			stmt.setString(1,clientCode);
			java.sql.Date cbd=new java.sql.Date(dt.toDate().getTime());
			stmt.setDate(2,cbd);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				TaskFrequencyDetail taskFrequencyDetail=new TaskFrequencyDetail();
				taskFrequencyDetail.setFrequencyType(rs.getString("frequency"));
				taskFrequencyDetail.setTaskName(rs.getString("task_name"));
				taskFrequencyDetail.setGraceDays(rs.getInt("offset"));
				taskFrequencyDetail.setVersionNo(rs.getInt("version_no"));
				taskFrequencyDetail.setTaskRepository(rs.getString("task_repository"));
				listTaskFrequencyDetail.add(taskFrequencyDetail);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return listTaskFrequencyDetail;
	}

	@Override
	public Map<String, String> getEntityAndEntityTaskTypeMap(DateTime cbd, String isFreqFilterApplied, String freqFilterCSV,
			String isFlowFilterApplied, String flowTypeCSV, String summaryType,String clientCode) throws Throwable {

		LOGGER.info("DLD DAO -- >getEntityAndEntityTaskTypeMap");
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		Map<String, String> entityType=new HashMap<String, String>();
		try {
			String taskQuery= ENTITY_AND_TYPE_QUERY;
			if(isFreqFilterApplied.equalsIgnoreCase("Y")&&isFlowFilterApplied.equalsIgnoreCase("Y"))
				taskQuery = taskQuery+EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING+OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET+EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
			else if(isFreqFilterApplied.equalsIgnoreCase("Y"))
				taskQuery = taskQuery+EXTRA_CLAUSE_FOR_FREQ_TYPE_FILTER_STAGING+OPEN_BRACKET+freqFilterCSV.toUpperCase()+CLOSE_BRACKET;
			else if(isFlowFilterApplied.equalsIgnoreCase("Y"))
				taskQuery = taskQuery+EXTRA_CLAUSE_FOR_FLOW_TYPE_FILTER_STAGING+OPEN_BRACKET+flowTypeCSV.toUpperCase()+CLOSE_BRACKET;

			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(taskQuery);
			java.sql.Date dt=new java.sql.Date(cbd.toDate().getTime());
			stmt.setDate(1, dt);
			stmt.setDate(3, dt);
			stmt.setString(2,clientCode);
			// execute select SQL stetement
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				entityType.put(rs.getString("ENTITY_NAME")+SEPARATOR+rs.getString("OWNER_NAME"), rs.getString("IS_EXECUTABLE_TASK"));
			}
		} catch (Exception e) {
			LOGGER.error("Unable to read from database",e);
			throw new Throwable();
		} finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}
		return entityType;
	}
	
	@Override
	public List<StagingDetails> getTaskStatusDetails(DateTime cbd,String freqFilterCSV,String clientCode) throws Throwable {
		LOGGER.info("DLD DAO -- >getTaskStatusDetails");
		List<StagingDetails> stagingDetails = new ArrayList<StagingDetails>();
		Connection dbConnection = null;
		PreparedStatement stmt = null;
		String stagingDetailsQuery = ALL_TASK_STATUS;
		stagingDetailsQuery=stagingDetailsQuery+freqFilterCSV+")";
		try{
			dbConnection = PersistentStoreManager.getConnection();
			stmt = dbConnection.prepareStatement(stagingDetailsQuery);
			java.sql.Date dt=new java.sql.Date(cbd.toDate().getTime());
			stmt.setDate(1, dt);
			stmt.setString(2,clientCode);
			stmt.setString(3, clientCode);
			stmt.setDate(4, dt);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				StagingDetails task = new StagingDetails();
				task.setTaskName(rs.getString("TASK_NAME"));
				task.setClientCode(rs.getString("CLIENT_CODE"));
				task.setTaskRepository(rs.getString("TASK_REPOSITORY"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				task.setFrequency(rs.getString("FREQUENCY"));
				task.setOffset(rs.getInt("OFFSET"));
				task.setSolutionId(rs.getInt("SOLUTION_ID"));
				task.setIsExclusionIndicator(rs.getString("Is_Exclusion_Indicator"));
				task.setOwnerName(rs.getString("OWNER_NAME"));
				task.setEntityName(rs.getString("ENTITY_NAME"));
				task.setTaskStatus(rs.getString("TASK_STATUS"));
				task.setLinkType(rs.getString("LINK_TYPE"));
				task.setFlowType(rs.getString("FLOW_TYPE"));
				stagingDetails.add(task);
			}
		}catch( Throwable e){
			LOGGER.error("Unable to read from database",e);
		}finally {
			closeStatement(stmt);
			closeConnection(dbConnection);
		}

		return stagingDetails;
	}
}
