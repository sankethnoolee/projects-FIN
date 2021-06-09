package com.fintellix.framework.validation.dao;

import com.fintellix.framework.validation.dto.ValidationCleanupRecord;
import com.fintellix.framework.validation.dto.ValidationComments;
import com.fintellix.framework.validation.dto.ValidationGroupCsvLinkage;
import com.fintellix.framework.validation.dto.ValidationLineItemLink;
import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationReturnResult;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.framework.validation.dto.ValidationWaiverDetails;
import com.fintellix.platformcore.common.hibernate.VyasaHibernateDaoSupport;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;
import com.northconcepts.datapipeline.core.DataReader;
import com.northconcepts.datapipeline.jdbc.JdbcReader;
import net.sf.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidationDaoHibernateImpl extends VyasaHibernateDaoSupport implements ValidationDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String ACTIVE_STATUS = "ACTIVE";

    @PostConstruct
    private void init() {
        changeStatusOfReturnValidation();
    }

    @Override
    public List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Date periodIdDate,
                                                               Integer orgId, String returnCode, Integer regReportId, String groupNameCSV, String regReportSectionId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchAllQualifiedValidations()");
        String groupIdClause = "";
        String sectionIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }
        if (null != regReportSectionId && !"".equalsIgnoreCase(regReportSectionId)) {
            sectionIdClause = " and VRL.REG_REPORT_SECTION_ID in (:regReportSectionId)";
        }
		/*
		Query q = getSession().createQuery("from ValidationMaster "
				+ " where isActive= :isActive and  solutionId= :solutionId and upper(status) ='"+ ACTIVE_STATUS+"' "
				+ " and entityType = :entityType "
				+ " and startDate <= :periodIdDate and (endDate >= :periodIdDate or endDate is null)"
				+ " and validationId in (select validationId from ValidationReportLink "
				+ " where solutionId = :solutionId "
				+ " and regReportId = :regReportId"
				+   sectionIdClause
				+ " "+groupIdClause+")");*/


        Query q = getSession().createNativeQuery("SELECT VM.* FROM VALIDATION_MASTER VM" +
                " INNER JOIN (SELECT VALIDATION_ID,SEQUENCE_NO FROM VALIDATION_RETURN_LINKAGE VRL" +
                "	WHERE VRL.SOLUTION_ID = :solutionId" +
                "	AND VRL.REG_REPORT_ID = :regReportId" +
                sectionIdClause +
                groupIdClause +
                "	) SB" +
                "	ON VM.VALIDATION_ID = SB.VALIDATION_ID" +
                "	AND VM.SEQUENCE_NO = SB.SEQUENCE_NO" +
                "	AND VM.IS_ACTIVE_RECORD = :isActive"
                + " AND ENTITY_TYPE = :entityType"
                + " AND SOLUTION_ID = :solutionId"
                + " AND START_DATE <=:periodIdDate"
                + " AND (END_DATE>=:periodIdDate OR END_DATE IS NULL)"
                + " AND VM.STATUS='" + ACTIVE_STATUS + "'", ValidationMaster.class);
        q.setParameter("solutionId", solutionId);
        q.setParameter("isActive", "Y");
        q.setParameter("entityType", ValidationConstants.TYPE_RETURN);
        q.setParameter("periodIdDate", new java.sql.Date(periodIdDate.getTime()));
        q.setParameter("regReportId", regReportId);
        if (null != regReportSectionId && !"".equalsIgnoreCase(regReportSectionId)) {
            q.setParameterList("regReportSectionId", Arrays.asList(regReportSectionId.split(",")).stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList()));
        }

        List<ValidationMaster> vm = q.getResultList();
        return vm;
    }

    @Override
    public List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Date periodDate, String entityCode,
                                                               String type, String groupNameCSV) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchAllQualifiedValidations()");
        String groupIdClause = "";

        if (groupNameCSV != null) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = "AND VALIDATION_GROUP_ID IN (SELECT GROUP_ID FROM VALIDATION_GROUP WHERE IS_ACTIVE = :isActive " +
                    " AND GROUP_NAME IN ('" + Arrays.stream(groupNameCSV.split(",")).map(String::trim)
                    .collect(Collectors.joining("','")) + "'))";
        }

        Query q = getSession().createNativeQuery("SELECT VM.* FROM VALIDATION_MASTER VM " +
                " INNER JOIN (Select VALIDATION_ID, SEQUENCE_NO from VALIDATION_ENTITY_LINKAGE " +
                " WHERE SOLUTION_ID = :solutionId " + groupIdClause + ") VEL " +
                " ON VM.VALIDATION_ID = VEL.VALIDATION_ID " +
                " AND VM.SEQUENCE_NO = VEL.SEQUENCE_NO " +
                " AND VM.ENTITY_TYPE = :entityType " +
                " AND VM.ENTITY_CODE = :entityCode " +
                " AND VM.IS_ACTIVE_RECORD = :isActive " +
                " AND SOLUTION_ID = :solutionId" +
                " AND START_DATE <= :periodDate " +
                " AND (END_DATE >= :periodDate OR END_DATE IS NULL) " +
                " AND VM.STATUS = :status ", ValidationMaster.class);

        q.setParameter("entityType", type);
        q.setParameter("entityCode", entityCode);
        q.setParameter("isActive", "Y");
        q.setParameter("solutionId", solutionId);
        q.setParameter("status", ACTIVE_STATUS);
        q.setParameter("periodDate", new java.sql.Date(periodDate.getTime()));

        List<ValidationMaster> vm = q.getResultList();
        return vm;
    }

    @Override
    public void registerValidationRequest(ValidationRequest vr) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> registerValidationRequest()");
        getSession().save(vr);
    }

    @Override
    public ValidationRequest getStatusOfTheExecutionByRunId(Integer runId) {
        Query q = getSession().createQuery("from ValidationRequest where runId= :runId");
        q.setParameter("runId", runId);
        ValidationRequest vr = (ValidationRequest) q.uniqueResult();
        return vr;
    }

    @Override
    public void registerValidationReturnResult(ValidationReturnResult vrr) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> registerValidationReturnResult()");
        getSession().save(vrr);

    }

    @Override
    public List<ValidationRunDetails> getValidationRunDetailsForReport(Integer solutionId,
                                                                       Date periodIdDate, Integer orgId, Integer regReportId, String groupNameCSV, Integer versionNo, Integer periodId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForReport()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID,VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select VRD.* from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL " +
                        "	ON VRD.VALIDATION_ID = VRL.VALIDATION_ID " +
                        "	AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO " +
                        "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID ";

        Query q = getSession().createSQLQuery(query).addEntity(ValidationRunDetails.class);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("regReportId", regReportId);
        q.setParameter("orgId", orgId);
        q.setParameter("periodId", periodId);
        List<ValidationRunDetails> vrd = q.list();
        return vrd;
    }

    @Override
    public List<Object[]> getValidationRunDetailsForReportBySection(Integer solutionId, Date periodIdDate,
                                                                    Integer orgId, Integer regReportId, String groupNameCSV, Integer versionNo, Integer regReportVersion, Integer periodId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForReportBySection()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID,VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select VRD.VALIDATION_ID,VRD.STATUS"
                        + " ,VM.VALIDATION_TYPE,VRL.REG_REPORT_SECTION_ID "
                        + " ,DRRS.REG_REPORT_SECTION_DESC,DRRS.FORM_NAME"
                        + " from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_MASTER VM"
                        + " on VM.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " inner join VALIDATION_RETURN_LINKAGE VRL"
                        + " on VRL.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VRL.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.SOLUTION_ID = VRL.SOLUTION_ID"
                        + "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID "
                        + " inner join DIM_REG_REPORT_SECTION DRRS"
                        + " on DRRS.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " and DRRS.REG_REPORT_ID = VRL.REG_REPORT_ID"
                        + " and DRRS.REG_REPORT_SECTION_ID = VRL.REG_REPORT_SECTION_ID"
                        + " where DRRS.REG_REPORT_VERSION = :regReportVersion";
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("regReportVersion", regReportVersion);
        List<Object[]> res = q.list();
        return res;
    }

    @Override
    public List<Object[]> returnValidationWarningsCommentsStatus(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                 Integer regReportId, String groupNameCSV, Integer versionNo, Integer periodId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForReportBySection()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID,VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select VRD.VALIDATION_ID,COMMENT from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_RETURN_LINKAGE VRL"
                        + " on VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " and VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID "
                        + " left outer join VALIDATION_COMMENTS VC"
                        + " on VC.REG_REPORT_ID = VRL.REG_REPORT_ID"
                        + " and VC.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VC.ORG_ID = :orgId"
                        + " and VC.PERIOD_ID = :periodId"
                        + " and VC.VERSION_NO = :versionNumber"
                        + " where VRD.STATUS = :validationStatus"
                        + " and VRD.VALIDATION_TYPE =:validationType"
                        + " and VRL.IS_COMMENT_AT_VALIDATION = :isCommentAtValidation";
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("isCommentAtValidation", "Y");
        q.setParameter("versionNumber", versionNo);
        q.setParameter("regReportId", regReportId);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("validationType", ValidationConstants.VALIDATION_TYPE_OPTIONAL);
        q.setParameter("validationStatus", ValidationConstants.VALIDATION_STATUS_FAILED);
        List<Object[]> res = q.list();


        return res;
    }

    @Override
    public List<Integer> fetchAllValidationIdOfTypeOptional(Integer solutionId,
                                                            Date periodIdDate, Integer orgId, Integer regReportId, String groupNameCSV, List<Integer> validationIdList) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchAllValidationIdOfTypeOptional()");
        String groupIdClause = "";
        List<String> validationIdListstr = new ArrayList<String>(validationIdList.size());
        for (Integer myInt : validationIdList) {
            validationIdListstr.add(String.valueOf(myInt));
        }
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and validationGroupId in (select groupId from ValidationGroup where isActive= 'Y' and groupName in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }
        Query q = getSession().createQuery("select validationId from ValidationMaster "
                + " where isActive= :isActive and  solutionId= :solutionId and upper(status) ='" + ACTIVE_STATUS + "' "
                + " and entityType = :entityType and validationType = :validationType"
                + " and ( " + getInClauseForStringDatatype(validationIdListstr, "validationId") + " )"
                //+ " and validationId in (:validationIdList)"
                + " and startDate <= :periodIdDate and (endDate >= :periodIdDate or endDate is null"
                + " and validationId in (select validationId from ValidationReportLink "
                + " where solutionId = :solutionId "
                + " and regReportId = :regReportId"
                + " " + groupIdClause + "))");
        q.setParameter("solutionId", solutionId);
        q.setParameter("isActive", "Y");
        q.setParameter("entityType", ValidationConstants.TYPE_RETURN);
        q.setParameter("periodIdDate", new java.sql.Date(periodIdDate.getTime()));
        q.setParameter("regReportId", regReportId);
        //q.setParameterList("validationIdList", validationIdList);
        q.setParameter("validationType", ValidationConstants.VALIDATION_TYPE_OPTIONAL);
        List<Integer> vIds = q.list();
        return vIds;
    }

    @Override
    public List<ValidationComments> fetchCommentsIfExist(Integer periodId, Integer orgId, Integer regReportId,
                                                         List<Integer> validValidationIds, List<String> hashValueList,
                                                         Integer versionNo) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchCommentsIfExist()");
        if (hashValueList == null || hashValueList.isEmpty()) {
            return new ArrayList<>();
        }

        List<ValidationComments> vcList = new ArrayList<>();
        List<String> valueBatch = new ArrayList<>();
        String inClauseBatchSize = ApplicationProperties.getValue("app.validations.inClauseBatchSize");
        int batchSize = 10000;

        if (!StringUtils.isEmpty(inClauseBatchSize)) {
            try {
                batchSize = Integer.parseInt(inClauseBatchSize);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (String occurrenceValue : hashValueList) {
            valueBatch.add(occurrenceValue);

            if (valueBatch.size() == batchSize) {
                vcList.addAll(fetchComments(periodId, orgId, regReportId, validValidationIds, valueBatch, versionNo));
                valueBatch.clear();
            }
        }

        if (valueBatch.size() > 0) {
            vcList.addAll(fetchComments(periodId, orgId, regReportId, validValidationIds, valueBatch, versionNo));
        }

        return vcList;
    }

    @Override
    public void saveValidationComments(List<ValidationComments> commentsList) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> saveValidationComments()");
        Session session = getSession();
        session.clear();
        for (ValidationComments vc : commentsList) {
            session.saveOrUpdate(vc);
        }
    }

    @Override
    public List<String> getRunIdAndValidationIdApplicable(Integer solutionId, Date periodIdDate, Integer orgId,
                                                          Integer regReportId, String groupNameCSV, Integer versionNo, Integer periodId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getRunIdAndValidationIdApplicable()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID ,VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " SELECT MRD.RUN_ID,VRD.VALIDATION_ID"
                        + " FROM MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_RETURN_LINKAGE VRL"
                        + " on VRL.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VRL.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID "
                        + " where VRD.STATUS = :validationStatus"
                        + " and VRD.VALIDATION_TYPE =:validationType"
                        + " and VRL.IS_COMMENT_AT_VALIDATION = :isCommentAtValidation";
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("isCommentAtValidation", "N");
        q.setParameter("versionNumber", versionNo);
        q.setParameter("regReportId", regReportId);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("validationType", ValidationConstants.VALIDATION_TYPE_OPTIONAL);
        q.setParameter("validationStatus", ValidationConstants.VALIDATION_STATUS_FAILED);
        List<Object[]> res = q.list();
        List<String> rvidList = new ArrayList<>();
        for (Object[] obj : res) {
            rvidList.add(obj[0].toString() + "###" + obj[1].toString());
        }


        return rvidList;
    }

    @Override
    public Boolean checkForCommentsAtOccurrenceLevel(Integer periodId, Integer orgId, Integer regReportId,
                                                     List<String> hashValueListTemp, Integer versionNo) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> checkForCommentsAtOccurrenceLevel()");
        Set<String> hashValueList = new HashSet<>(hashValueListTemp);
        if (hashValueList == null || hashValueList.isEmpty()) {
            return false;
        }

        List<ValidationComments> vcList = new ArrayList<ValidationComments>();
        List<String> valueBatch = new ArrayList<String>();
        String inClauseBatchSize = ApplicationProperties.getValue("app.validations.inClauseBatchSize");
        int batchSize = 10000;

        if (!StringUtils.isEmpty(inClauseBatchSize)) {
            try {
                batchSize = Integer.parseInt(inClauseBatchSize);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (String occurenceValue : hashValueList) {
            valueBatch.add(occurenceValue);

            if (valueBatch.size() == batchSize) {
                vcList.addAll(getValiationCommentsForBatch(valueBatch, periodId, regReportId, versionNo, orgId));
                valueBatch.clear();
            }
        }

        if (valueBatch.size() > 0) {
            vcList.addAll(getValiationCommentsForBatch(valueBatch, periodId, regReportId, versionNo, orgId));
        }

        logger.info("warnings count :" + hashValueList.size() + ", comments count:" + vcList.size());
        return vcList.size() != (hashValueList.size());
    }

    private List<ValidationComments> getValiationCommentsForBatch(List<String> valueBatch, Integer periodId, Integer regReportId, Integer versionNo, Integer orgId) {
        List<ValidationComments> vcList = new ArrayList<ValidationComments>();
        Query q = getSession().createQuery("from ValidationComments "
                + " where periodId = :periodId"
                + " and regReportId = :regReportId"
                + " and versionNumber = :versionNumber"
                + " and ( " + getInClauseForStringDatatype(valueBatch, "occurrence") + " )"
                //+ " and occurrence in (:hashValueList)"
                + " and orgId = :orgId");
        q.setParameter("periodId", periodId);
        q.setParameter("regReportId", regReportId);
        //q.setParameterList("hashValueList", hashValueList);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("orgId", orgId);

        vcList = q.list();

        if (vcList != null) {
            return vcList;
        } else {
            return new ArrayList<ValidationComments>();
        }

    }

    @Override
    public Integer getMaxRunIdForValidation(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId,
                                            String groupNameCSV, Integer versionNo, Integer periodId, Integer validationId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getMaxRunIdForValidation()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + "        AND VRL.VALIDATION_ID = :validationId"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select RUN_ID from MAX_RUN_DETAILS";
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("validationId", validationId);
        Integer res = (Integer) q.uniqueResult();
        return res;
    }


    @Override
    public Map<String, String> getCommentsAtOccurrenceLevel(Integer periodId, Integer orgId, Integer regReportId,
                                                            Integer versionNo, Integer validationId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getCommentsAtOccurrenceLevel()");
        Query q = getSession().createQuery("from ValidationComments "
                + " where periodId = :periodId"
                + " and regReportId = :regReportId"
                + " and versionNumber = :versionNumber"
                + " and occurrence !=:hashValue"
                + " and validationId = :validationId"
                + " and orgId = :orgId");
        q.setParameter("periodId", periodId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("hashValue", ValidationConstants.NO_OCCURRENCE_HASHKEY);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("validationId", validationId);
        q.setParameter("orgId", orgId);
        List<ValidationComments> vcList = q.list();
        Map<String, String> hashCommentMap = new HashMap<String, String>();
        for (ValidationComments vc : vcList) {
            hashCommentMap.put(vc.getValidationId() + ValidationConstants.HASH_DELIMITER + vc.getOccurrence(), vc.getComment().trim());
        }
        return hashCommentMap;
    }

    @Override
    public List<Object[]> getValidationDetailsAtFormLevel(Integer solutionId, Date periodIdDate, Integer orgId,
                                                          Integer regReportId, String groupNameCSV, Integer versionNo, Integer periodId, Integer regReportVersion) {

        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForReportBySection()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID ,VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select VRD.VALIDATION_ID,VM.VALIDATION_CODE"
                        + " ,VM.VALIDATION_NAME"
                        + " ,VRL.IS_COMMENT_AT_VALIDATION"
                        + " ,VRD.STATUS"
                        + " ,VM.VALIDATION_TYPE,VRL.REG_REPORT_SECTION_ID "
                        + " ,DRRS.REG_REPORT_SECTION_DESC,DRRS.FORM_NAME"
                        + " ,VRD.TOTAL_OCCURRENCE"
                        + " ,VRD.TOTAL_FAILED"
                        + " ,VRD.DIMENSIONS_CSV"
                        + " ,DRRS.SECTION_NAME"
                        + " ,DRRS.SECTION_TYPE"
                        + " from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_MASTER VM"
                        + " on VM.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " inner join VALIDATION_RETURN_LINKAGE VRL"
                        + " on VRL.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VRL.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.SOLUTION_ID = VRL.SOLUTION_ID"
                        + "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID "
                        + " inner join DIM_REG_REPORT_SECTION DRRS"
                        + " on DRRS.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " and DRRS.REG_REPORT_ID = VRL.REG_REPORT_ID"
                        + " and DRRS.REG_REPORT_SECTION_ID = VRL.REG_REPORT_SECTION_ID"

                        + " where DRRS.REG_REPORT_VERSION = :regReportVersion";
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("regReportVersion", regReportVersion);
        List<Object[]> res = q.list();
        return res;

    }

    @Override
    public Map<String, String> fetchCommentsIfExistAtValidation(Integer periodId, Integer orgId, Integer regReportId,
                                                                List<Integer> validValidationIds,
                                                                List<String> hashValueList, Integer versionNo) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchCommentsIfExistAtValidation()");
        Map<String, String> mp = new HashMap<>();

        if (hashValueList == null || hashValueList.isEmpty()) {
            return mp;
        }

        List<ValidationComments> vcList = new ArrayList<>();
        List<String> valueBatch = new ArrayList<>();
        String inClauseBatchSize = ApplicationProperties.getValue("app.validations.inClauseBatchSize");
        int batchSize = 10000;

        if (!StringUtils.isEmpty(inClauseBatchSize)) {
            try {
                batchSize = Integer.parseInt(inClauseBatchSize);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (String occurrenceValue : hashValueList) {
            valueBatch.add(occurrenceValue);

            if (valueBatch.size() == batchSize) {
                vcList.addAll(fetchComments(periodId, orgId, regReportId, validValidationIds, valueBatch, versionNo));
                valueBatch.clear();
            }
        }

        if (valueBatch.size() > 0) {
            vcList.addAll(fetchComments(periodId, orgId, regReportId, validValidationIds, valueBatch, versionNo));
        }

        if (!vcList.isEmpty()) {
            for (ValidationComments vc : vcList) {
                mp.put(vc.getValidationId() + ValidationConstants.HASH_DELIMITER + vc.getOccurrence(), vc.getComment().trim());
            }
        }

        return mp;
    }

    @Override
    public Object[] getValidationDetails(Integer solutionId,
                                         Date periodIdDate, Integer orgId, Integer regReportId, String groupNameCSV, Integer versionNo, Integer periodId, Integer validationId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationDetails()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + " 	   AND VRL.VALIDATION_ID = :validationId"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select MRD.RUN_ID,VM.VALIDATION_ID,VM.VALIDATION_NAME,VM.VALIDATION_DESC,"
                        + " VALIDATION_EXPRESSION from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_MASTER VM"
                        + " on  VM.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VM.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.VALIDATION_ID = :validationId";

        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("regReportId", regReportId);
        q.setParameter("orgId", orgId);
        q.setParameter("periodId", periodId);
        q.setParameter("validationId", validationId);
        Object[] vrd = (Object[]) q.uniqueResult();
        return vrd;
    }


    @Override
    public ValidationRunDetails getValidationRunDetailsByRunId(Integer runId, Integer validationId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsByRunId()");
        String query =
                " from ValidationRunDetails where runId = :runId and validationId = :validationId";

        Query q = getSession().createQuery(query);

        q.setParameter("runId", runId);
        q.setParameter("validationId", validationId);
        ValidationRunDetails vrd = (ValidationRunDetails) q.uniqueResult();
        return vrd;
    }

    @Override
    public ValidationReturnResult fetchStatusForTheRun(Integer periodId, Integer orgId, Integer regReportId, Integer versionNo, Integer solutionId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchStatusForTheRun()");
        ValidationReturnResult vrr = new ValidationReturnResult();
        Query q = getSession().createQuery("from ValidationReturnResult "
                + " where periodId = :periodId"
                + " and regReportId = :regReportId"
                + " and versionNumber = :versionNumber"
                + " and solutionId = :solutionId"
                + " and orgId = :orgId and runId=(select max(runId) from ValidationReturnResult where periodId = :periodId"
                + " and regReportId = :regReportId"
                + " and versionNumber = :versionNumber"
                + " and orgId = :orgId and solutionId = :solutionId"
                + ")");
        q.setParameter("periodId", periodId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("orgId", orgId);
        q.setParameter("solutionId", solutionId);
        vrr = (ValidationReturnResult) q.uniqueResult();

        return vrr;
    }

    @Override
    public Map<String, String> getLiDetailsBySubExpression(String sqlQuery, Integer solutionId) {
        Map<String, String> res = new HashMap<String, String>();
        Connection connection = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        Statement ps = null;
        try {
            connection = getMartConnection(solutionId);
            ps = connection.createStatement();
            rs = ps.executeQuery(sqlQuery);

            rsmd = rs.getMetaData();

            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                    res.put(rsmd.getColumnName(i).toUpperCase(), rs.getObject(i) == null ? "" : rs.getObject(i).toString());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

    private Connection getMartConnection(Integer solutionId) {
        try {
            return PersistentStoreManager.getSolutionDBConnection(solutionId);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public ValidationRequest getValidationRequestByRunId(Integer runId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchStatusForTheRun()");
        ValidationRequest vrr = new ValidationRequest();
        Query q = getSession().createQuery("from ValidationRequest "
                + " where runId=:runId");
        q.setParameter("runId", runId);

        vrr = (ValidationRequest) q.uniqueResult();

        return vrr;
    }

    @Override
    public void markCurrentExecutionOfValidationasFailed(ValidationReturnResult vrr, ValidationRequest vr) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchStatusForTheRun()");
        getSession().saveOrUpdate(vrr);
        getSession().saveOrUpdate(vr);
    }

    @Override
    public List<Object[]> getValidationDetailsForAllFormsForDownload(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                     Integer regReportId, String groupNameCSV, Integer versionNo,
                                                                     Integer periodId, Integer regReportVersion,
                                                                     String validationResultType, String formNameCSV,
                                                                     Set<Integer> validationIds) {

        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationDetailsForAllFormsForDownload()");
        String groupIdClause = "";
        if (null != groupNameCSV && !groupNameCSV.trim().equals("")) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID ,VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS like 'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select VRD.VALIDATION_ID"
                        + ",VM.VALIDATION_CODE"
                        + " ,VM.VALIDATION_NAME"
                        + " ,VRL.IS_COMMENT_AT_VALIDATION"
                        + " ,VRD.STATUS"
                        + " ,VM.VALIDATION_TYPE,VRL.REG_REPORT_SECTION_ID "
                        + " ,DRRS.REG_REPORT_SECTION_DESC,DRRS.FORM_NAME"
                        + " ,VRD.TOTAL_OCCURRENCE"
                        + " ,VRD.TOTAL_FAILED"
                        + " ,VRD.DIMENSIONS_CSV"
                        + " ,VM.VALIDATION_DESC"
                        + " ,VRD.REPLACED_EXPRESSION"
                        + " ,VM.VALIDATION_EXPRESSION"
                        + " from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_MASTER VM"
                        + " on VM.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " inner join VALIDATION_RETURN_LINKAGE VRL"
                        + " on VRL.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VRL.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID "
                        + " and VM.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " inner join DIM_REG_REPORT_SECTION DRRS"
                        + " on DRRS.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " and DRRS.REG_REPORT_ID = VRL.REG_REPORT_ID"
                        + " and DRRS.REG_REPORT_SECTION_ID = VRL.REG_REPORT_SECTION_ID"
                        + " where DRRS.REG_REPORT_VERSION = :regReportVersion"
                        + " and VRD.STATUS = :runStatus";

        if (validationResultType.equalsIgnoreCase(ValidationConstants.ERROR_VALIDATION_RESULT) ||
                validationResultType.equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)) {
            query = query + " and VM.VALIDATION_TYPE = :validationType";
        }

        List<String> formNames = null;
        if (formNameCSV != null && !formNameCSV.trim().equals("")) {
            formNames = Arrays.stream(formNameCSV.split("\\s*,\\s*")).map(String::trim).collect(Collectors.toList());
            query = query + " and DRRS.FORM_NAME IN (:formNames)";
        }

        int totalInClauseSize = 0;
        if (validationIds != null && !validationIds.isEmpty()) {
            int inClauseSize = Integer.parseInt(ApplicationProperties.getValue("app.validations.inClauseSize"));
            int size = validationIds.size();

            totalInClauseSize += size / inClauseSize;
            size = size % inClauseSize;

            if (size != 0) {
                ++totalInClauseSize;
            }

            query = query + " and (";
            for (int i = 1; i <= totalInClauseSize; i++) {
                query = query + " VRD.VALIDATION_ID IN (:validationIds" + i + ") ";

                if (i == totalInClauseSize) {
                    query = query + ")";
                } else {
                    query = query + " OR ";
                }
            }
        }

        logger.info("Data Query : " + query);
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("regReportVersion", regReportVersion);
        q.setParameter("runStatus", ValidationConstants.VALIDATION_STATUS_FAILED);

        if (validationResultType.equalsIgnoreCase(ValidationConstants.ERROR_VALIDATION_RESULT) ||
                validationResultType.equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)) {
            String paramValue = "";
            if (validationResultType.equalsIgnoreCase(ValidationConstants.ERROR_VALIDATION_RESULT)) {
                paramValue = ValidationConstants.VALIDATION_TYPE_MANDATORY;
            } else if (validationResultType.equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)) {
                paramValue = ValidationConstants.VALIDATION_TYPE_OPTIONAL;
            }

            q.setParameter("validationType", paramValue);
        }

        if (formNameCSV != null && !formNameCSV.trim().equals("")) {
            q.setParameterList("formNames", formNames);
        }

        if (validationIds != null && !validationIds.isEmpty()) {
            List<Integer> valList = new ArrayList<>(validationIds);
            int inClauseSize = Integer.parseInt(ApplicationProperties.getValue("app.validations.inClauseSize"));
            int size = validationIds.size();

            for (int i = 1; i <= totalInClauseSize; i++) {
                if ((size - inClauseSize) >= 0) {
                    q.setParameterList("validationIds" + i, valList.subList(0, inClauseSize));
                } else {
                    q.setParameterList("validationIds" + i, valList.subList(0, size));
                }

                size = size - inClauseSize;
            }
        }

        List<Object[]> res = q.list();
        return res;
    }

    @Override
    public Date getLatestCommentModificationDate(Set<Integer> validationId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getLatestCommentModificationDate()");
        String queryStr = "select max(lastModificationDate) from ValidationComments where validationId in (:validationId)";
        Query query = getSession().createQuery(queryStr);
        query.setParameterList("validationId", validationId);

        return (Date) query.uniqueResult();
    }


    @Override
    public List<ValidationLineItemLink> getValidationLineItemLinkDetails(Integer solutionId, Date periodIdDate, Integer orgId, String returnCode, Integer regReportId, String groupNameCSV) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForReport()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }
        String query =
                " WITH QUALIFIED_VM AS ("
                        + " SELECT VALIDATION_ID,SEQUENCE_NO,SOLUTION_ID "
                        + " FROM VALIDATION_MASTER VM"
                        + " WHERE IS_ACTIVE_RECORD = :isActive"
                        + " AND SOLUTION_ID = :solutionId"
                        + " AND UPPER(STATUS)='" + ACTIVE_STATUS + "'"
                        + " AND ENTITY_TYPE= :entityType"
                        + " AND START_DATE <=:periodIdDate AND  (END_DATE>=:periodIdDate OR END_DATE IS NULL)"
                        + " AND VALIDATION_ID IN ("
                        + " SELECT VALIDATION_ID FROM VALIDATION_RETURN_LINKAGE VRL"
                        + " WHERE SOLUTION_ID = :solutionId"
                        + " AND REG_REPORT_ID = :regReportId"
                        + " " + groupIdClause
                        + "  )"
                        + " )"
                        + " select VLL.* from VALIDATION_LINE_ITEM_LINK VLL"
                        + " inner join QUALIFIED_VM QVM"
                        + " on QVM.VALIDATION_ID = VLL.VALIDATION_ID"
                        + " AND QVM.SOLUTION_ID = VLL.SOLUTION_ID"
                        + " AND QVM.SEQUENCE_NO = VLL.SEQUENCE_NO";

        Query q = getSession().createSQLQuery(query).addEntity(ValidationLineItemLink.class);

        q.setParameter("solutionId", solutionId);
        q.setParameter("isActive", "Y");
        q.setParameter("entityType", ValidationConstants.TYPE_RETURN);
        q.setParameter("periodIdDate", new java.sql.Date(periodIdDate.getTime()));
        q.setParameter("regReportId", regReportId);
        List<ValidationLineItemLink> vll = q.list();
        return vll;
    }

    @Override
    public List<ValidationMaster> getValidationById(Integer validationId, Integer solutionId) {
        Query q = getSession().createQuery("from ValidationMaster "
                + " where isActive= :isActive and  solutionId= :solutionId and upper(status) ='" + ACTIVE_STATUS + "' "
                + " and entityType = :entityType "
                + " and validationId = :validationId");
        q.setParameter("solutionId", solutionId);
        q.setParameter("isActive", "Y");
        q.setParameter("entityType", ValidationConstants.TYPE_RETURN);
        q.setParameter("validationId", validationId);
        List<ValidationMaster> vm = q.list();
        return vm;
    }

    @Override
    public ValidationMaster getValidationBySequenceAndId(Integer validationId, Integer sequenceNo, Integer solutionId) {
        Query q = getSession().createQuery("from ValidationMaster "
                + " where isActive= :isActive and  solutionId= :solutionId and upper(status) ='" + ACTIVE_STATUS + "' "
                + " and entityType = :entityType "
                + " and validationId = :validationId");
        q.setParameter("solutionId", solutionId);
        q.setParameter("isActive", "Y");
        q.setParameter("entityType", ValidationConstants.TYPE_RETURN);
        q.setParameter("validationId", validationId);
        List<ValidationMaster> vm = q.list();
        return vm.get(0);
    }

    @Override
    public List<ValidationMaster> getAllValidations(Integer solutionId) {
        String query = "from ValidationMaster";
        if (solutionId != null) {
            query = query + " where solutionId= :solutionId";
        }

        Query q = getSession().createQuery(query);
        if (solutionId != null) {
            q.setParameter("solutionId", solutionId);
        }
        List<ValidationMaster> vm = q.list();
        return vm;
    }

    @Override
    public List<Object[]> getValidationDetailsAtFormLevelByLineItem(Integer solutionId, Date periodIdDate,
                                                                    Integer orgId, Integer regReportId, String groupNameCSV, Integer versionNo, Integer periodId,
                                                                    Integer regReportVersion, String lineItemBusinessName, Integer sectionId) {

        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForReportBySection()");
        String groupIdClause = "";
        if (null != groupNameCSV) {
            groupNameCSV = ValidationStringUtils.replace(groupNameCSV, "\"", "", -1, true);
            groupIdClause = " and VRL.VALIDATION_GROUP_ID in ( select GROUP_ID from VALIDATION_GROUP where IS_ACTIVE= 'Y' and  GROUP_NAME in ('"
                    + Arrays.asList(groupNameCSV.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream()
                    .collect(Collectors.joining("','")) + "'))";
        }

        String query =
                " WITH MAX_RUN_DETAILS AS ("
                        + " SELECT DISTINCT(MAX(VRD.RUN_ID)) RUN_ID, VRL.VALIDATION_GROUP_ID as GROUP_ID"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + "        ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "        AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + "        ON VRD.RUN_ID = VRR.RUN_ID"
                        + " WHERE VRL.REG_REPORT_ID = :regReportId"
                        + "        AND VRL.SOLUTION_ID = :solutionId"
                        + "        AND VRR.PERIOD_ID = :periodId"
                        + "        AND VRR.REG_REPORT_ID = :regReportId"
                        + "        AND VRR.ORG_ID = :orgId"
                        + "        AND VRR.VERSION_NO = :versionNumber"
                        + "        AND VRR.STATUS  like  'COMPLETED%'"
                        + groupIdClause
                        + " GROUP BY VRL.VALIDATION_GROUP_ID"
                        + " )"
                        + " select VRD.VALIDATION_ID,VM.VALIDATION_CODE"
                        + " ,VM.VALIDATION_NAME"
                        + " ,VRL.IS_COMMENT_AT_VALIDATION"
                        + " ,VRD.STATUS"
                        + " ,VM.VALIDATION_TYPE,VRL.REG_REPORT_SECTION_ID "
                        + " ,DRRS.REG_REPORT_SECTION_DESC,DRRS.FORM_NAME"
                        + " ,VRD.TOTAL_OCCURRENCE"
                        + " ,VRD.TOTAL_FAILED"
                        + " ,VRD.DIMENSIONS_CSV"
                        + " from MAX_RUN_DETAILS MRD"
                        + " inner join VALIDATION_RUN_DETAILS VRD"
                        + " on VRD.RUN_ID = MRD.RUN_ID"
                        + " inner join VALIDATION_MASTER VM"
                        + " on VM.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " inner join VALIDATION_RETURN_LINKAGE VRL"
                        + " on VRL.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " and VRL.SEQUENCE_NO = VRD.SEQUENCE_NUMBER"
                        + " and VM.SOLUTION_ID = VRL.SOLUTION_ID"
                        + "	AND MRD.GROUP_ID = VRL.VALIDATION_GROUP_ID "
                        + " inner join DIM_REG_REPORT_SECTION DRRS"
                        + " on DRRS.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " and DRRS.REG_REPORT_ID = VRL.REG_REPORT_ID"
                        + " and DRRS.REG_REPORT_SECTION_ID = VRL.REG_REPORT_SECTION_ID"
                        + " INNER JOIN VALIDATION_LINE_ITEM_LINK VLL"
                        + " ON VLL.VALIDATION_ID = VRD.VALIDATION_ID"
                        + " AND VLL.SEQUENCE_NO = VRD.SEQUENCE_NUMBER "
                        + " where DRRS.REG_REPORT_VERSION = :regReportVersion"
                        + " AND UPPER(VLL.LINE_ITEM_NAME)=:lineItemBusinessName"
                        + " AND VLL.SECTION_ID = :sectionId";
        Query q = getSession().createSQLQuery(query);

        q.setParameter("solutionId", solutionId);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("periodId", periodId);
        q.setParameter("orgId", orgId);
        q.setParameter("regReportId", regReportId);
        q.setParameter("regReportVersion", regReportVersion);
        q.setParameter("lineItemBusinessName", lineItemBusinessName.toUpperCase());
        q.setParameter("sectionId", sectionId);
        List<Object[]> res = q.list();
        return res;

    }

    @Override
    public Object[] getReportAndVersionName(Integer periodId, Integer solutionId, Integer orgId, Integer versionNo,
                                            Integer regReportId) {
        logger.info("EXEFLOW - ValidationDaoHibernateImpl -> getReportAndVersionName()");
        List<Object[]> res = null;
        try {
            String queryStr = " SELECT RR.REPORT_NAME,RV.VERSION_NUMBER,RV.VERSION_NAME,RV.STATUS FROM REG_REPORT_VERSION RV"
                    + " INNER JOIN DIM_REG_REPORT RR"
                    + " ON RR.REG_REPORT_ID = RV.REG_REPORT_ID"
                    + " AND RR.SOLUTION_ID = RV.SOLUTION_ID"
                    + " AND RR.REG_REPORT_VERSION = RV.REG_REPORT_VERSION"
                    + " WHERE RV.SOLUTION_ID = :solutionId"
                    + " AND RV.REG_REPORT_ID = :regReportId"
                    + " AND RV.VERSION_NUMBER = :versionNumber"
                    + " AND RV.ENTITY_ID = :orgEntityId"
                    + " AND RV.YEAR_OF_ACCOUNT = :periodId"
                    + " AND RV.IS_ACTIVE = :isActive";
            Query q = getSession().createSQLQuery(queryStr);
            q.setParameter("periodId", periodId);
            q.setParameter("regReportId", regReportId);
            q.setParameter("isActive", "Y");
            q.setParameter("solutionId", solutionId);
            q.setParameter("versionNumber", versionNo);
            q.setParameter("orgEntityId", orgId);
            res = q.list();
        } catch (Exception e) {
            logger.error("ERROR - ValidationDaoHibernateImpl -> getReportAndVersionName()");
            e.printStackTrace();
        }
        return (null == res || 0 == res.size()) ? null : res.get(0);
    }

    @Override
    public Integer getReportAndVersionName(Integer solutionId, Integer regReportId, Integer regReportVersion,
                                           String sectionType, String lineItemBusinessName) {
        logger.info("EXEFLOW - ValidationDaoHibernateImpl -> getReportAndVersionName()");
        Integer res = null;
        try {
            String queryStr = " SELECT REG_REPORT_SECTION_ID from DIM_REG_REPORT_SECTION"
                    + " where REG_REPORT_ID = :regReportId"
                    + " AND SOLUTION_ID = :solutionId"
                    + " AND REG_REPORT_VERSION =:regReportVersion"
                    + " AND SECTION_TYPE = :sectionType"
                    + " AND ENTITY_NAME = :lineItemBusinessName";
            Query q = getSession().createSQLQuery(queryStr);
            q.setParameter("regReportId", regReportId);
            q.setParameter("solutionId", solutionId);
            q.setParameter("regReportVersion", regReportVersion);
            q.setParameter("sectionType", sectionType);
            q.setParameter("lineItemBusinessName", lineItemBusinessName);
            res = (Integer) q.uniqueResult();
        } catch (Exception e) {
            logger.error("ERROR - ValidationDaoHibernateImpl -> getReportAndVersionName()");
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getGroupByColumnForGrid(Integer solutionId, Integer orgId, Integer regReportId, Integer versionNo,
                                          Integer periodId, Integer regReportVersion, Integer liId, Integer mapId) {
        Map<String, String> res = new HashMap<String, String>();
        Connection connection = null;
        ResultSet rs = null;
        Statement ps = null;
        String groupByDim = null;
        String sqlQuery = "SELECT GROUP_BY_DIMENSION from FCT_Group_Line_Item_Balance where PERIOD_ID = " + periodId + " "
                + " AND REG_REPORT_ID = " + regReportId + " AND LINE_ITEM_ID = " + liId + " AND LINE_ITEM_MAP_ID = " + mapId + ""
                + " AND SOLUTION_ID = " + solutionId + " AND VERSION_NUMBER = " + versionNo + " AND ORG_ENTITY_ID = " + orgId + ""
                + " AND REG_REPORT_VERSION = " + regReportVersion;
        try {
            connection = getMartConnection(solutionId);
            ps = connection.createStatement();
            rs = ps.executeQuery(sqlQuery);
            while (rs.next()) {
                groupByDim = rs.getString("GROUP_BY_DIMENSION");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return groupByDim;
    }

    @Override
    public Integer getMapIdFromGroupByColumn(Integer solutionId, Integer orgId, Integer regReportId, Integer versionNo,
                                             Integer periodId, Integer liId, String groupByColumn) {
        Map<String, String> res = new HashMap<String, String>();
        Connection connection = null;
        ResultSet rs = null;
        Statement ps = null;
        Integer mapId = null;
        String sqlQuery = "SELECT LINE_ITEM_MAP_ID from FCT_Group_Line_Item_Balance where PERIOD_ID = " + periodId + " "
                + " AND REG_REPORT_ID = " + regReportId + " AND LINE_ITEM_ID = " + liId + " AND GROUP_BY_DIMENSION = '" + groupByColumn + "'"
                + " AND SOLUTION_ID = " + solutionId + " AND VERSION_NUMBER = " + versionNo + " AND ORG_ENTITY_ID = " + orgId + "";
        try {
            connection = getMartConnection(solutionId);
            ps = connection.createStatement();
            rs = ps.executeQuery(sqlQuery);
            while (rs.next()) {
                mapId = rs.getInt("LINE_ITEM_MAP_ID");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return mapId;
    }

    @Override
    public void updateValidationReturnResult(ValidationReturnResult vrr) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> updateValidationReturnResult()");
        getSession().saveOrUpdate(vrr);
    }


    private String getInClauseForStringDatatype(List<String> values, String columnName) {
        StringBuilder inClause = new StringBuilder("");
        List<String> inClauseArr = new ArrayList<>();
        int counter = 0;
        if (values.size() > 0) {
            inClause.append(columnName).append(" in ").append(" ( ");
            for (String val : values) {
                inClause.append("'").append(val).append("',");
                if (counter < 999) {
                    counter++;
                } else {
                    inClause.setLength(inClause.length() - 1);
                    counter = 0;
                    inClause = inClause.append(" ) ");
                    inClauseArr.add(inClause.toString());
                    inClause = new StringBuilder("").append(columnName).append(" in ").append(" ( ");
                }

            }
            if (counter != 0) {
                inClause.setLength(inClause.length() - 1);
                inClause = inClause.append(" ) ");
                inClauseArr.add(inClause.toString());
            }
            return inClauseArr.stream().collect(Collectors.joining(" OR "));
        }

        return "";

    }

    @Override
    public DataReader getDataReader(String query, Connection conn) throws Throwable {
        DataReader reader = new JdbcReader(conn, query);
        return reader;
    }

    private List<ValidationComments> fetchComments(Integer periodId, Integer orgId, Integer regReportId,
                                                   List<Integer> validValidationIds, List<String> hashValueList,
                                                   Integer versionNo) {
        Query q = getSession().createQuery("from ValidationComments "
                + " where periodId = :periodId"
                + " and regReportId = :regReportId"
                + " and versionNumber = :versionNumber"
                + " and validationId in (:validationIdList)"
                + " and ( " + getInClauseForStringDatatype(hashValueList, "occurrence") + " )"
                + " and orgId = :orgId");
        q.setParameter("periodId", periodId);
        q.setParameter("regReportId", regReportId);
        q.setParameterList("validationIdList", validValidationIds);
        q.setParameter("versionNumber", versionNo);
        q.setParameter("orgId", orgId);
        return q.list();
    }

    private void changeStatusOfReturnValidation() {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> changeStatusOfReturnValidation()");
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = PersistentStoreManager.getConnection();

            // Updating VALIDATION_REQUEST table
            ps = conn.prepareStatement("update VALIDATION_REQUEST SET REQUEST_STATUS = ?, " +
                    "REQUEST_END_DATE_TIME = ? where REQUEST_STATUS = ? ");
            ps.setString(1, ValidationConstants.VALIDATION_STATUS_FAILED);
            ps.setTimestamp(2, new java.sql.Timestamp((new Date()).getTime()));
            ps.setString(3, ValidationConstants.VALIDATION_STATUS_PROCESSING);
            ps.executeUpdate();

            // Updating VALIDATION_RETURN_RESULT table
            ps = conn.prepareStatement("update VALIDATION_RETURN_RESULT SET STATUS = ?, " +
                    "END_DATE_TIME = ? where STATUS = ?");
            ps.setString(1, ValidationConstants.VALIDATION_STATUS_FAILED);
            ps.setTimestamp(2, new java.sql.Timestamp((new Date()).getTime()));
            ps.setString(3, ValidationConstants.VALIDATION_STATUS_PROCESSING);
            ps.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public JSONObject getValidationCode(String returnBkey, Date periodIdDate, Integer sectionId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationCode()");
        JSONObject resultObj = new JSONObject();
        String query = ("select VM FROM ValidationMaster VM"
                + " INNER JOIN ValidationReportLink VRL"
                + " ON VM.validationId = VRL.validationId"
                + " AND VM.sequenceNo = VRL.sequenceNo"
                + " AND VM.startDate <=:periodIdDate AND  (VM.endDate>=:periodIdDate OR VM.endDate IS NULL)"
                + " AND VM.entityCode = :returnBkey"
                + " AND VM.status='ACTIVE'"
                + " AND VM.isActive='Y'");
        if (sectionId != null) {
            query = query.concat(" AND VRL.regReportSectionId= :sectionId");
        }
        Query q = getSession().createQuery(query);
        q.setParameter("returnBkey", returnBkey);
        q.setParameter("periodIdDate", periodIdDate);
        if (sectionId != null) {
            q.setParameter("sectionId", sectionId);
        }
        List i = q.getResultList();
        if (i.size() > 0) {
            resultObj.put("result", i);
        } else {
            return null;
        }
        return resultObj;
    }

    @Override
    public JSONObject getErrorValidationCode(String returnBkey, Date periodIdDate, Integer orgId, Integer versionNo,
                                             Integer sectionId, Integer regReportId, Integer periodId, Integer solutionId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getErrorValidationCode()");
        JSONObject resultObj = new JSONObject();

        String query = ("WITH max_run_details AS " +
                "( " +
                "                SELECT DISTINCT(Max(vrd.run_id)) run_id, " +
                "                                vrl.validation_group_id AS GROUP_ID " +
                "                FROM            validation_run_details VRD " +
                "                INNER JOIN      validation_return_linkage VRL " +
                "                ON              vrd.validation_id = vrl.validation_id " +
                "                AND             vrd.sequence_number = vrl.sequence_no " +
                "                INNER JOIN      validation_return_result VRR " +
                "                ON              vrd.run_id = vrr.run_id " +
                "                WHERE           vrl.reg_report_id = :regReportId " +
                "                AND             vrl.solution_id = :solutionId " +
                "                AND             vrr.period_id =  :periodId " +
                "                AND             vrr.reg_report_id =  :regReportId" +
                "                AND             vrr.org_id = :orgId " +
                "                AND             vrr.version_no = :versionNo " +
                "                AND             vrr.status LIKE 'COMPLETED%' " +
                "                GROUP BY        vrl.validation_group_id ) " +
                " SELECT     VM.* FROM max_run_details MRD " +
                " INNER JOIN validation_run_details VRD " +
                " ON         VRD.run_id = MRD.run_id " +
                " INNER JOIN validation_return_linkage VRL " +
                " ON         VRD.validation_id = VRL.validation_id " +
                " AND        VRD.sequence_number = VRL.sequence_no " +
                " and         VRL.VALIDATION_GROUP_ID = MRD.GROUP_ID " +
                " INNER JOIN  validation_master VM " +
                " ON VM.VALIDATION_ID = VRD.VALIDATION_ID " +
                " AND VM.sequence_no = VRD.sequence_number " +
                " AND        VM.start_date <=:periodIdDate " +
                " AND        (VM.end_date>=:periodIdDate OR VM.end_date IS NULL) " +
                " AND        vrd.status NOT LIKE 'PASSED' " +
                " AND        VM.entity_code= :returnBkey " +
                " AND        VM.status='ACTIVE' " +
                " AND        VM.is_active_record='Y'"
                + " AND VM.VALIDATION_TYPE = 'Mandatory'");
        if (sectionId != null) {
            query = query.concat(" AND VRL.REG_REPORT_SECTION_ID = :sectionId");
        }
        query = query.concat(" ORDER BY VM.VALIDATION_ID");
        Query q = getSession().createNativeQuery(query, ValidationMaster.class);
        q.setParameter("returnBkey", returnBkey);
        q.setParameter("regReportId", regReportId);
        q.setParameter("solutionId", solutionId);
        q.setParameter("periodId", periodId);
        q.setParameter("periodIdDate", periodIdDate);
        q.setParameter("orgId", orgId);
        q.setParameter("versionNo", versionNo);
        if (sectionId != null) {
            q.setParameter("sectionId", sectionId);
        }

        List i = q.getResultList();
        if (i.size() > 0) {
            resultObj.put("result", i);
        } else {
            return null;
        }
        return resultObj;
    }

    @Override
    public JSONObject getOccurrenceValidationCode(String returnBkey, Date periodIdDate, Integer orgId, Integer versionNo,
                                                  String validationCode) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getOccurrenceValidationCode()");
        JSONObject resultObj = new JSONObject();
        String query = ("SELECT VM.* FROM VALIDATION_MASTER VM"
                + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL"
                + " ON VM.VALIDATION_ID = VRL.VALIDATION_ID"
                + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                + " ON VRL.REG_REPORT_ID = VRR.REG_REPORT_ID"
                + " AND VM.SEQUENCE_NO = VRL.SEQUENCE_NO"
                + " AND VM.START_DATE <=:periodIdDate AND  (VM.END_DATE>=:periodIdDate OR VM.END_DATE IS NULL)"
                + " AND VRR.ORG_ID = :orgId"
                + " AND VRR.VERSION_NO = :versionNo"
                + " AND VM.ENTITY_CODE= :returnBkey"
                + " AND VM.VALIDATION_CODE = :validationCode");
        Query q = getSession().createNativeQuery(query);
        q.setParameter("returnBkey", returnBkey);
        q.setParameter("periodIdDate", periodIdDate);
        q.setParameter("orgId", orgId);
        q.setParameter("versionNo", versionNo);
        q.setParameter("validationCode", validationCode);
        resultObj.put("result", q.getResultList());
        List i = q.getResultList();
        if (i.size() > 0) {
            resultObj.put("result", i);
        } else {
            return null;
        }
        return resultObj;
    }

    @Override
    public Integer getReportId(String reportBkey) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getReportIdByBkey()");
        Query query = getSession().createNativeQuery("select distinct(reg_report_id) from DIM_REG_REPORT where report_Bkey=:reportBkey");
        query.setParameter("reportBkey", reportBkey);
        List<Integer> i = query.getResultList();
        if (i.size() > 0) {
            return ((i).get(0));
        }
        return null;
    }

    @Override
    public Integer getValidationIdByCode(String validationCode, String returnBkey) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationIdByCode()");
        Query query = getSession().createNativeQuery("SELECT distinct(VALIDATION_ID) FROM VALIDATION_MASTER WHERE VALIDATION_CODE=:validationCode and ENTITY_CODE = :returnBkey"
                + " and IS_ACTIVE_RECORD = 'Y'");
        query.setParameter("validationCode", validationCode);
        query.setParameter("returnBkey", returnBkey);
        List<Integer> i = query.getResultList();
        if (i.size() > 0) {
            return ((i).get(0));
        }
        return null;
    }

    @Override
    public List<ValidationWaiverDetails> fetchAllTheVAlidationWaiver(Integer solutionId, Integer orgId, int regReportId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchAllTheVAlidationWaiver()");
        Query query = getSession().createQuery("from ValidationWaiverDetails where solutionId = :solutionId and isActive =:isActive and isDeleted = :isDeleted and orgId = :orgId and regReportId = :regReportId and isActiveVersion = :isActiveVersion");
        query.setParameter("solutionId", solutionId);
        query.setParameter("orgId", orgId);
        query.setParameter("regReportId", regReportId);
        query.setParameter("isActive", 1);
        query.setParameter("isDeleted", 0);
        query.setParameter("isActiveVersion", 1);
        return query.list();
    }

    @Override
    public List<ValidationWaiverDetails> fetchAllTheValidationWaiverHistory(Integer solutionId, Integer orgId, Integer regReportId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchAllTheValidationWaiverHistory()");
        String q = "from ValidationWaiverDetails where solutionId = :solutionId";
        if (null != orgId) {
            q = q.concat(" and orgId = :orgId");
        }
        if (null != regReportId) {
            q = q.concat(" and regReportId = :regReportId");
        }
        Query query = getSession().createQuery(q);
        query.setParameter("solutionId", solutionId);
        if (null != orgId) {
            query.setParameter("orgId", orgId);
        }
        if (null != regReportId) {
            query.setParameter("regReportId", regReportId);
        }
        return query.list();
    }

    @Override
    public Map<Integer, String> fetchAllValidationCode(List<String> vwValidationId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> fetchAllValidationCode()");
        Map<Integer, String> vIdCodeMap = new HashMap<Integer, String>();
        Query query = getSession().createNativeQuery("SELECT DISTINCT(VALIDATION_ID),VALIDATION_CODE FROM VALIDATION_MASTER WHERE ( " + getInClauseForStringDatatype(vwValidationId, "VALIDATION_ID") + ")");
        List<Object[]> res = query.list();
        for (Object[] o : res) {
            vIdCodeMap.put((Integer) o[0], (String) o[1]);
        }

        return vIdCodeMap;
    }

    @Override
    public String getReportBkeyById(Integer reportId, Integer solutionId) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getReportBkeyById()");
        Query query = getSession().createNativeQuery("SELECT DISTINCT(REPORT_BKEY) FROM DIM_REG_REPORT WHERE REG_REPORT_ID =:reportId and SOLUTION_ID=:solutionId");
        query.setParameter("solutionId", solutionId);
        query.setParameter("reportId", reportId);
        return query.getResultList().get(0).toString().trim();
    }

    @Override
    public List<ValidationRunDetails> getValidationRunDetails(Integer runId, String validationIdCsv) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetails()");

        String query = " from ValidationRunDetails where runId = :runId ";

        List<Integer> valIds = null;
        if (validationIdCsv != null && !validationIdCsv.trim().equals("")) {
            valIds = Arrays.stream(validationIdCsv.split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
            query += " and validationId IN (:validationIds)";
        }

        Query q = getSession().createQuery(query);
        q.setParameter("runId", runId);

        if (validationIdCsv != null && !validationIdCsv.trim().equals("")) {
            q.setParameterList("validationIds", valIds);
        }

        return q.list();
    }

    @Override
    public List<Object[]> getValidationRunDetailsForSpark(Integer runId, String validationIdCsv, boolean distinctRecord) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationRunDetailsForSpark()");
        String query;

        if (distinctRecord) {
            query = "SELECT DISTINCT vgcl.GROUP_FOLDER_NAME AS groupFolderName, vgcl.GROUP_CSV_NAME AS groupCsvName ";
        } else {
            query = "SELECT vrd.*, vgcl.GROUP_FOLDER_NAME, vgcl.GROUP_CSV_NAME ";
        }

        query += " FROM VALIDATION_RUN_DETAILS vrd INNER JOIN VALIDATION_GROUP_CSV_LINKAGE vgcl " +
                " ON vrd.RUN_ID = vgcl.RUN_ID AND vrd.VALIDATION_ID = vgcl.VALIDATION_ID " +
                " WHERE vrd.RUN_ID = :runId ";

        List<Integer> valIds = null;
        if (validationIdCsv != null && !validationIdCsv.trim().equals("")) {
            valIds = Arrays.stream(validationIdCsv.split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
            query += " and vrd.VALIDATION_ID IN (:validationIds)";
        }

        Query q = getSession().createNativeQuery(query);
        q.setParameter("runId", runId);

        if (validationIdCsv != null && !validationIdCsv.trim().equals("")) {
            q.setParameterList("validationIds", valIds);
        }

        return q.list();
    }

    @Override
    public void updateValidationCleanupRecords(List<ValidationCleanupRecord> validationCleanupRecords) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> updateValidationCleanupRecords()");
        saveOrUpdateAll(validationCleanupRecords);
    }

    @Override
    public List<ValidationCleanupRecord> getValidationCleanupRecords(String type, Boolean isDeleted, Date createdDate,
                                                                     String dateFilterOperator, String path) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> getValidationCleanupRecords()");
        StringBuilder whereClause = null;

        if (type != null && !type.trim().equals("")) {
            whereClause = new StringBuilder(" WHERE ");
            whereClause.append(" type = :type ");
        }

        if (isDeleted != null) {
            if (whereClause == null) {
                whereClause = new StringBuilder(" WHERE ");
            } else {
                whereClause.append(" AND ");
            }
            whereClause.append(" isDeleted = :isDeleted ");
        }

        if (createdDate != null) {
            if (whereClause == null) {
                whereClause = new StringBuilder(" WHERE ");
            } else {
                whereClause.append(" AND ");
            }
            whereClause.append(" createdDate ").append(dateFilterOperator).append(" :createdDate ");
        }

        if (path != null && !path.trim().equals("")) {
            if (whereClause == null) {
                whereClause = new StringBuilder(" WHERE ");
            } else {
                whereClause.append(" AND ");
            }
            whereClause.append(" path = :path ");
        }

        String queryStr = " from ValidationCleanupRecord ";
        if (whereClause != null) {
            queryStr += whereClause.toString();
        }

        Query query = getSession().createQuery(queryStr);

        if (type != null && !type.trim().equals("")) {
            query.setParameter("type", type.trim());
        }
        if (isDeleted != null) {
            query.setParameter("isDeleted", isDeleted);
        }
        if (createdDate != null) {
            query.setParameter("createdDate", new java.sql.Date(createdDate.getTime()));
        }
        if (path != null && !path.trim().equals("")) {
            query.setParameter("path", path);
        }

        return query.list();
    }

    @Override
    public void saveValidationGroupCsvLinkage(List<ValidationGroupCsvLinkage> linkages) {
        logger.info("EXEFLOW --> ValidationDaoHibernateImpl --> saveValidationGroupCsvLinkage()");
        saveAll(linkages);
    }
}