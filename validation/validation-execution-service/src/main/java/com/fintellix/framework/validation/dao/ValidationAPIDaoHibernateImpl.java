package com.fintellix.framework.validation.dao;

import com.fintellix.platformcore.common.hibernate.VyasaHibernateDaoSupport;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ValidationAPIDaoHibernateImpl extends VyasaHibernateDaoSupport implements ValidationAPIDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final String EQ = "EQ";
    private final String LT = "LT";
    private final String GT = "GT";
    private final String LEQ = "LEQ";
    private final String GEQ = "GEQ";
    private final String RANGE = "RANGE";

    @Override
    public Integer getRecordCountForPagination(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                               String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution) {
        logger.info("EXEFLOW --> ValidationAPIDaoHibernateImpl --> getRecordCountForPagination()");

        Integer versionNumber = null;
        Integer periodId = null;
        StringBuilder whereClauseForMaxRun = new StringBuilder("");
        StringBuilder whereClauseForMainSelect = new StringBuilder("");

        if (solution == null || solution.trim().length() == 0) {
            solution = (String) getSession().createSQLQuery("select SOLUTIONDESCRIPTION from VYASASOLUTION where SOLUTIONID=0").getSingleResult();
        }

        whereClauseForMaxRun.append(" WHERE VS.SOLUTIONDESCRIPTION = '" + solution + "'");
        whereClauseForMainSelect.append(" AND VS.SOLUTIONDESCRIPTION = '" + solution + "'");

        if (returnBkey != null && returnBkey.trim().length() > 0) {
            returnBkey = returnBkey.split("\\(").length > 1 ? returnBkey.split("\\(")[1].replace(")", "").trim() : returnBkey;
            returnBkey = ValidationStringUtils.replace(returnBkey, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND DRR.REPORT_BKEY IN ('" + Arrays.asList(returnBkey.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND DRR.REPORT_BKEY IN ('" + Arrays.asList(returnBkey.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (period != null && period.trim().length() > 0) {
            String condition = getPeriodExp(period);
            whereClauseForMaxRun.append(" AND VRR.PERIOD_ID " + condition);
            whereClauseForMainSelect.append(" AND RRV.YEAR_OF_ACCOUNT " + condition);
        }
        if (organizationCode != null && organizationCode.trim().length() > 0) {
            organizationCode = organizationCode.split("\\(").length > 1 ? organizationCode.split("\\(")[1].replace(")", "").trim() : organizationCode;
            organizationCode = ValidationStringUtils.replace(organizationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND DROE.ORG_CODE IN ('" + Arrays.asList(organizationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (createdByUserOrganizationCode != null && createdByUserOrganizationCode.trim().length() > 0) {
            createdByUserOrganizationCode = createdByUserOrganizationCode.split("\\(").length > 1 ? createdByUserOrganizationCode.split("\\(")[1].replace(")", "").trim() : createdByUserOrganizationCode;
            createdByUserOrganizationCode = ValidationStringUtils.replace(createdByUserOrganizationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND DUOE.ORG_CODE IN ('" + Arrays.asList(createdByUserOrganizationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnVersionNumber != null && returnVersionNumber.trim().length() > 0) {
            versionNumber = Integer.parseInt(returnVersionNumber);
            whereClauseForMaxRun.append(" AND VRR.VERSION_NO = " + versionNumber);
            whereClauseForMainSelect.append(" AND RRV.VERSION_NUMBER = " + versionNumber);
        }
        if (returnStatus != null && returnStatus.trim().length() > 0) {
            returnStatus = returnStatus.split("\\(").length > 1 ? returnStatus.split("\\(")[1].replace(")", "").trim() : returnStatus;
            returnStatus = ValidationStringUtils.replace(returnStatus, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND RRV.STATUS IN ('" + Arrays.asList(returnStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND RRV.STATUS IN ('" + Arrays.asList(returnStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationProcessStatus != null && returnValidationProcessStatus.trim().length() > 0) {
            returnValidationProcessStatus = returnValidationProcessStatus.split("\\(").length > 1 ? returnValidationProcessStatus.split("\\(")[1].replace(")", "").trim() : returnValidationProcessStatus;
            returnValidationProcessStatus = ValidationStringUtils.replace(returnValidationProcessStatus, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VRR.STATUS IN ('" + Arrays.asList(returnValidationProcessStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND VRR.STATUS IN ('" + Arrays.asList(returnValidationProcessStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationGroup != null && returnValidationGroup.trim().length() > 0) {
            returnValidationGroup = returnValidationGroup.split("\\(").length > 1 ? returnValidationGroup.split("\\(")[1].replace(")", "").trim() : returnValidationGroup;
            returnValidationGroup = ValidationStringUtils.replace(returnValidationGroup, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VG.GROUP_NAME IN ('" + Arrays.asList(returnValidationGroup.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }

        String query =
                "WITH MAX_RUN_DETAILS"
                        + " AS ("
                        + " 	SELECT VG.GROUP_NAME"
                        + " 		,MAX(VRR.RUN_ID) RUN_ID"
                        + " 	FROM VALIDATION_RETURN_RESULT VRR"
                        + " 	INNER JOIN VALIDATION_RUN_DETAILS VRD ON VRR.RUN_ID = VRD.RUN_ID"
                        + " 	INNER JOIN VALIDATION_RETURN_LINKAGE VRL ON VRR.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " 		AND VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + " 		AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " 	INNER JOIN VALIDATION_GROUP VG ON VRL.VALIDATION_GROUP_ID = VG.GROUP_ID"
                        + " 	INNER JOIN VYASASOLUTION VS ON VRR.SOLUTION_ID = VS.SOLUTIONID"
                        + " 	INNER JOIN REG_REPORT_VERSION RRV ON VRR.SOLUTION_ID = RRV.SOLUTION_ID"
                        + " 		AND VRR.REG_REPORT_ID = RRV.REG_REPORT_ID"
                        + " 		AND VRR.REG_REPORT_VERSION_NUMBER = RRV.REG_REPORT_VERSION"
                        + " 		AND VRR.VERSION_NO = RRV.VERSION_NUMBER"
                        + " 		AND VRR.PERIOD_ID = RRV.YEAR_OF_ACCOUNT"
                        + " 		AND VRR.ORG_ID = RRV.ENTITY_ID"
                        + " 		AND RRV.IS_ACTIVE = 'Y'"
                        + " 	INNER JOIN DIM_REG_REPORT DRR ON RRV.REG_REPORT_ID = DRR.REG_REPORT_ID"
                        + " 		AND RRV.REG_REPORT_VERSION = DRR.REG_REPORT_VERSION"
                        + whereClauseForMaxRun
                        + " 	GROUP BY VRR.REG_REPORT_ID"
                        + " 		,VRR.PERIOD_ID"
                        + " 		,VRR.ORG_ID"
                        + " 		,VRR.VERSION_NO"
                        + " 		,VS.SOLUTIONDESCRIPTION"
                        + " 		,VG.GROUP_NAME"
                        + " 		,DRR.REPORT_BKEY"
                        + " 		,DRR.REPORT_NAME"
                        + " 	)"
                        + " 	SELECT COUNT(*) count"
                        + " 	FROM ("
                        + " 		SELECT VS.SOLUTIONDESCRIPTION AS solutionName"
                        + " 			,RRV.REG_REPORT_ID AS returnID"
                        + " 			,LTRIM(RTRIM(DRR.REPORT_BKEY)) AS returnBkey"
                        + " 			,LTRIM(RTRIM(DRR.REPORT_NAME)) AS returnName"
                        + " 			,RRV.YEAR_OF_ACCOUNT AS period"
                        + " 			,DROE.ORG_CODE AS organizationCode"
                        + " 			,DROE.ORG_NAME AS organizationName"
                        + " 			,DUOE.ORG_CODE AS createdByUserOrganizationCode"
                        + " 			,DUOE.ORG_NAME AS createdByUserOrganizationName"
                        + " 			,RRV.VERSION_NUMBER AS returnVersionNumber"
                        + " 			,RRV.VERSION_NAME AS returnVersionName"
                        + " 			,RRV.STATUS AS returnStatus"
                        + " 			,RRV.YEAR_OF_ACCOUNT AS returnPeriod"
                        + " 			,RRV.VERSION_DATE AS returnCreatedDate"
                        + " 			,users.USER_NAME AS createdByUserID"
                        + " 			,users.FIRST_NAME + CASE WHEN COALESCE(users.MIDDLE_NAME, '') <> '' THEN ' ' + users.MIDDLE_NAME ELSE '' END + ' ' + users.LAST_NAME AS createdByUserName"
                        + " 			,users.EMAIL AS createdByUserEmail"
                        + " 			,VRR.STATUS AS returnValidationProcessStatus"
                        + " 			,MRD.GROUP_NAME AS returnValidationGroup"
                        + " 			,VRR.RUN_ID AS runId"
                        + " 		FROM VALIDATION_RETURN_RESULT VRR"
                        + " 		INNER JOIN VALIDATION_RUN_DETAILS VRD ON VRR.RUN_ID = VRD.RUN_ID"
                        + " 		INNER JOIN VALIDATION_RETURN_LINKAGE VRL ON VRR.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " 			AND VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + " 			AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " 		INNER JOIN VALIDATION_GROUP VG ON VRL.VALIDATION_GROUP_ID = VG.GROUP_ID"
                        + " 		INNER JOIN VYASASOLUTION VS ON VRR.SOLUTION_ID = VS.SOLUTIONID"
                        + " 		INNER JOIN REG_REPORT_VERSION RRV ON VRR.SOLUTION_ID = RRV.SOLUTION_ID"
                        + " 			AND VRR.REG_REPORT_ID = RRV.REG_REPORT_ID"
                        + " 			AND VRR.REG_REPORT_VERSION_NUMBER = RRV.REG_REPORT_VERSION"
                        + " 			AND VRR.VERSION_NO = RRV.VERSION_NUMBER"
                        + " 			AND VRR.PERIOD_ID = RRV.YEAR_OF_ACCOUNT"
                        + " 			AND VRR.ORG_ID = RRV.ENTITY_ID"
                        + " 			AND RRV.IS_ACTIVE = 'Y'"
                        + " 		INNER JOIN DIM_ORGANIZATION_ENTITY DROE ON RRV.ENTITY_ID = DROE.ORG_ID"
                        + " 		INNER JOIN DIM_REG_REPORT DRR ON RRV.REG_REPORT_ID = DRR.REG_REPORT_ID"
                        + " 			AND RRV.REG_REPORT_VERSION = DRR.REG_REPORT_VERSION"
                        + " 		INNER JOIN USERS users ON RRV.USERNAME = users.USER_NAME"
                        + " 		INNER JOIN DIM_ORGANIZATION_ENTITY DUOE ON users.ORG_ID = DUOE.ORG_ID"
                        + " 		INNER JOIN MAX_RUN_DETAILS MRD ON MRD.RUN_ID = VRR.RUN_ID"
                        + " 		WHERE RRV.IS_ACTIVE = 'Y'"
                        + whereClauseForMainSelect
                        + " 		GROUP BY VS.SOLUTIONDESCRIPTION"
                        + " 			,RRV.REG_REPORT_ID"
                        + " 			,DRR.REPORT_BKEY"
                        + " 			,DRR.REPORT_NAME"
                        + " 			,RRV.YEAR_OF_ACCOUNT"
                        + " 			,DROE.ORG_CODE"
                        + " 			,DROE.ORG_NAME"
                        + " 			,DUOE.ORG_CODE"
                        + " 			,DUOE.ORG_NAME"
                        + " 			,RRV.VERSION_NUMBER"
                        + " 			,RRV.VERSION_NAME"
                        + " 			,RRV.STATUS"
                        + " 			,RRV.YEAR_OF_ACCOUNT"
                        + " 			,RRV.VERSION_DATE"
                        + " 			,users.USER_NAME"
                        + " 			,users.FIRST_NAME"
                        + " 			,users.MIDDLE_NAME"
                        + " 			,users.LAST_NAME"
                        + " 			,users.EMAIL"
                        + " 			,VRR.STATUS"
                        + " 			,VRR.RUN_ID"
                        + " 			,MRD.GROUP_NAME"
                        + " 	) subquery";

        logger.info("QUERY --> " + query);
        Query q = getSession().createSQLQuery(query);
        Integer count = (Integer) q.getSingleResult();
        return count;
    }

    @Override
    public List<Object[]> getReturnValidationResultSummary(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                           String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                           String page, String rows) {
        logger.info("EXEFLOW --> ValidationAPIDaoHibernateImpl --> getReturnValidationResultSummary()");

        Integer versionNumber = null;
        Integer periodId = null;
        StringBuilder whereClauseForMaxRun = new StringBuilder("");
        StringBuilder whereClauseForMainSelect = new StringBuilder("");

        if (solution == null || solution.trim().length() == 0) {
            solution = (String) getSession().createSQLQuery("select SOLUTIONDESCRIPTION from VYASASOLUTION where SOLUTIONID=0").getSingleResult();
        }

        whereClauseForMaxRun.append(" WHERE VS.SOLUTIONDESCRIPTION = '" + solution + "'");
        whereClauseForMainSelect.append(" AND VS.SOLUTIONDESCRIPTION = '" + solution + "'");

        if (returnBkey != null && returnBkey.trim().length() > 0) {
            returnBkey = returnBkey.split("\\(").length > 1 ? returnBkey.split("\\(")[1].replace(")", "").trim() : returnBkey;
            returnBkey = ValidationStringUtils.replace(returnBkey, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND DRR.REPORT_BKEY IN ('" + Arrays.asList(returnBkey.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND DRR.REPORT_BKEY IN ('" + Arrays.asList(returnBkey.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (period != null && period.trim().length() > 0) {
            String condition = getPeriodExp(period);
            whereClauseForMaxRun.append(" AND VRR.PERIOD_ID " + condition);
            whereClauseForMainSelect.append(" AND RRV.YEAR_OF_ACCOUNT " + condition);
        }
        if (organizationCode != null && organizationCode.trim().length() > 0) {
            organizationCode = organizationCode.split("\\(").length > 1 ? organizationCode.split("\\(")[1].replace(")", "").trim() : organizationCode;
            organizationCode = ValidationStringUtils.replace(organizationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND DROE.ORG_CODE IN ('" + Arrays.asList(organizationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (createdByUserOrganizationCode != null && createdByUserOrganizationCode.trim().length() > 0) {
            createdByUserOrganizationCode = createdByUserOrganizationCode.split("\\(").length > 1 ? createdByUserOrganizationCode.split("\\(")[1].replace(")", "").trim() : createdByUserOrganizationCode;
            createdByUserOrganizationCode = ValidationStringUtils.replace(createdByUserOrganizationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND DUOE.ORG_CODE IN ('" + Arrays.asList(createdByUserOrganizationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnVersionNumber != null && returnVersionNumber.trim().length() > 0) {
            versionNumber = Integer.parseInt(returnVersionNumber);
            whereClauseForMaxRun.append(" AND VRR.VERSION_NO = " + versionNumber);
            whereClauseForMainSelect.append(" AND RRV.VERSION_NUMBER = " + versionNumber);
        }
        if (returnStatus != null && returnStatus.trim().length() > 0) {
            returnStatus = returnStatus.split("\\(").length > 1 ? returnStatus.split("\\(")[1].replace(")", "").trim() : returnStatus;
            returnStatus = ValidationStringUtils.replace(returnStatus, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND RRV.STATUS IN ('" + Arrays.asList(returnStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND RRV.STATUS IN ('" + Arrays.asList(returnStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationProcessStatus != null && returnValidationProcessStatus.trim().length() > 0) {
            returnValidationProcessStatus = returnValidationProcessStatus.split("\\(").length > 1 ? returnValidationProcessStatus.split("\\(")[1].replace(")", "").trim() : returnValidationProcessStatus;
            returnValidationProcessStatus = ValidationStringUtils.replace(returnValidationProcessStatus, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VRR.STATUS IN ('" + Arrays.asList(returnValidationProcessStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND VRR.STATUS IN ('" + Arrays.asList(returnValidationProcessStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationGroup != null && returnValidationGroup.trim().length() > 0) {
            returnValidationGroup = returnValidationGroup.split("\\(").length > 1 ? returnValidationGroup.split("\\(")[1].replace(")", "").trim() : returnValidationGroup;
            returnValidationGroup = ValidationStringUtils.replace(returnValidationGroup, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VG.GROUP_NAME IN ('" + Arrays.asList(returnValidationGroup.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }

        String query =
                "WITH MAX_RUN_DETAILS"
                        + " AS ("
                        + " 	SELECT VG.GROUP_NAME"
                        + " 		,MAX(VRR.RUN_ID) RUN_ID"
                        + " 	FROM VALIDATION_RETURN_RESULT VRR"
                        + " 	INNER JOIN VALIDATION_RUN_DETAILS VRD ON VRR.RUN_ID = VRD.RUN_ID"
                        + " 	INNER JOIN VALIDATION_RETURN_LINKAGE VRL ON VRR.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " 		AND VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + " 		AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " 	INNER JOIN VALIDATION_GROUP VG ON VRL.VALIDATION_GROUP_ID = VG.GROUP_ID"
                        + " 	INNER JOIN VYASASOLUTION VS ON VRR.SOLUTION_ID = VS.SOLUTIONID"
                        + " 	INNER JOIN REG_REPORT_VERSION RRV ON VRR.SOLUTION_ID = RRV.SOLUTION_ID"
                        + " 		AND VRR.REG_REPORT_ID = RRV.REG_REPORT_ID"
                        + " 		AND VRR.REG_REPORT_VERSION_NUMBER = RRV.REG_REPORT_VERSION"
                        + " 		AND VRR.VERSION_NO = RRV.VERSION_NUMBER"
                        + " 		AND VRR.PERIOD_ID = RRV.YEAR_OF_ACCOUNT"
                        + " 		AND VRR.ORG_ID = RRV.ENTITY_ID"
                        + " 		AND RRV.IS_ACTIVE = 'Y'"
                        + " 	INNER JOIN DIM_REG_REPORT DRR ON RRV.REG_REPORT_ID = DRR.REG_REPORT_ID"
                        + " 		AND RRV.REG_REPORT_VERSION = DRR.REG_REPORT_VERSION"
                        + whereClauseForMaxRun
                        + " 	GROUP BY VRR.REG_REPORT_ID"
                        + " 		,VRR.PERIOD_ID"
                        + " 		,VRR.ORG_ID"
                        + " 		,VRR.VERSION_NO"
                        + " 		,VS.SOLUTIONDESCRIPTION"
                        + " 		,VG.GROUP_NAME"
                        + " 		,DRR.REPORT_BKEY"
                        + " 		,DRR.REPORT_NAME"
                        + " 	)"
                        + " 	SELECT VS.SOLUTIONDESCRIPTION AS solutionName"
                        + " 		,RRV.REG_REPORT_ID AS returnID"
                        + " 		,LTRIM(RTRIM(DRR.REPORT_BKEY)) AS returnBkey"
                        + " 		,LTRIM(RTRIM(DRR.REPORT_NAME)) AS returnName"
                        + " 		,RRV.YEAR_OF_ACCOUNT AS period"
                        + " 		,DROE.ORG_CODE AS organizationCode"
                        + " 		,DROE.ORG_NAME AS organizationName"
                        + " 		,DUOE.ORG_CODE AS createdByUserOrganizationCode"
                        + " 		,DUOE.ORG_NAME AS createdByUserOrganizationName"
                        + " 		,RRV.VERSION_NUMBER AS returnVersionNumber"
                        + " 		,RRV.VERSION_NAME AS returnVersionName"
                        + " 		,RRV.STATUS AS returnStatus"
                        + " 		,RRV.YEAR_OF_ACCOUNT AS returnPeriod"
                        + " 		,RRV.VERSION_DATE AS returnCreatedDate"
                        + " 		,users.USER_NAME AS createdByUserID"
                        + " 		,users.FIRST_NAME + CASE WHEN COALESCE(users.MIDDLE_NAME, '') <> '' THEN ' ' + users.MIDDLE_NAME ELSE '' END + ' ' + users.LAST_NAME AS createdByUserName"
                        + " 		,users.EMAIL AS createdByUserEmail"
                        + " 		,VRR.STATUS AS returnValidationProcessStatus"
                        + " 		,COALESCE(count(VRD.RUN_ID),0) as totalValidations"
                        + "         ,COALESCE(SUM(case VRD.VALIDATION_TYPE when 'Mandatory' THEN 1 ELSE 0 END),0) as totalMandatoryValidations"
                        + "         ,COALESCE(SUM(case VRD.VALIDATION_TYPE when 'Optional' THEN 1 ELSE 0 END),0)  as totalOptionalValidations"
                        + "         ,COALESCE(SUM(case VRD.STATUS when 'PASSED' then 1 ELSE 0  END),0) as totalValidationsPassed"
                        + "         ,COALESCE(SUM(case VRD.VALIDATION_TYPE when 'Mandatory' then ( case VRD.STATUS when 'FAILED' then 1 ELSE 0 END) END),0) as totalErrors"
                        + "         ,COALESCE(SUM(case VRD.VALIDATION_TYPE when 'Optional' then ( case VRD.STATUS when 'FAILED' then 1 ELSE 0 END) END),0)  as totalWarnings"
                        + " 		,MRD.GROUP_NAME AS returnValidationGroup"
                        + " 		,VRR.RUN_ID AS runId"
                        + "			,VCVM.CATEGORYVALUENAME AS categoryValueName"
                        + " 	FROM VALIDATION_RETURN_RESULT VRR"
                        + " 	INNER JOIN VALIDATION_RUN_DETAILS VRD ON VRR.RUN_ID = VRD.RUN_ID"
                        + " 	INNER JOIN VALIDATION_RETURN_LINKAGE VRL ON VRR.SOLUTION_ID = VRL.SOLUTION_ID"
                        + " 		AND VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + " 		AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " 	INNER JOIN VALIDATION_GROUP VG ON VRL.VALIDATION_GROUP_ID = VG.GROUP_ID"
                        + " 	INNER JOIN VYASASOLUTION VS ON VRR.SOLUTION_ID = VS.SOLUTIONID"
                        + " 	INNER JOIN REG_REPORT_VERSION RRV ON VRR.SOLUTION_ID = RRV.SOLUTION_ID"
                        + " 		AND VRR.REG_REPORT_ID = RRV.REG_REPORT_ID"
                        + " 		AND VRR.REG_REPORT_VERSION_NUMBER = RRV.REG_REPORT_VERSION"
                        + " 		AND VRR.VERSION_NO = RRV.VERSION_NUMBER"
                        + " 		AND VRR.PERIOD_ID = RRV.YEAR_OF_ACCOUNT"
                        + " 		AND VRR.ORG_ID = RRV.ENTITY_ID"
                        + " 		AND RRV.IS_ACTIVE = 'Y'"
                        + " 	INNER JOIN DIM_ORGANIZATION_ENTITY DROE ON RRV.ENTITY_ID = DROE.ORG_ID"
                        + " 	INNER JOIN DIM_REG_REPORT DRR ON RRV.REG_REPORT_ID = DRR.REG_REPORT_ID"
                        + " 		AND RRV.REG_REPORT_VERSION = DRR.REG_REPORT_VERSION"
                        + " 	INNER JOIN USERS users ON RRV.USERNAME = users.USER_NAME"
                        + " 	INNER JOIN DIM_ORGANIZATION_ENTITY DUOE ON users.ORG_ID = DUOE.ORG_ID"
                        + " 	INNER JOIN MAX_RUN_DETAILS MRD ON MRD.RUN_ID = VRR.RUN_ID"
                        + "		INNER JOIN VYASAARTEFACT VA ON VA.REPORTID = DRR.REG_REPORT_ID"
                        + "		LEFT OUTER JOIN VYASAARTEFACTCATEGORYVALUELINK VACVL ON VA.ARTEFACTID = VACVL.ARTEFACTID AND VACVL.CATEGORYID = (SELECT CATEGORYID FROM VYASACATEGORYMASTER WHERE CATEGORYINTERNALNAME = 'Period_Format') AND VACVL.ISACTIVE = 1"
                        + "		LEFT OUTER JOIN VYASACATEGORYVALUESMASTER VCVM ON VCVM.CATEGORYID = VACVL.CATEGORYID AND VCVM.CATEGORYVALUEID = VACVL.CATEGORYVALUEID AND VCVM.ISACTIVE = 1"
                        + " 	WHERE RRV.IS_ACTIVE = 'Y'"
                        + whereClauseForMainSelect
                        + " 	GROUP BY VS.SOLUTIONDESCRIPTION"
                        + " 		,RRV.REG_REPORT_ID"
                        + " 		,DRR.REPORT_BKEY"
                        + " 		,DRR.REPORT_NAME"
                        + " 		,RRV.YEAR_OF_ACCOUNT"
                        + " 		,DROE.ORG_CODE"
                        + " 		,DROE.ORG_NAME"
                        + " 		,DUOE.ORG_CODE"
                        + " 		,DUOE.ORG_NAME"
                        + " 		,RRV.VERSION_NUMBER"
                        + " 		,RRV.VERSION_NAME"
                        + " 		,RRV.STATUS"
                        + " 		,RRV.YEAR_OF_ACCOUNT"
                        + " 		,RRV.VERSION_DATE"
                        + " 		,users.USER_NAME"
                        + " 		,users.FIRST_NAME"
                        + " 		,users.MIDDLE_NAME"
                        + " 		,users.LAST_NAME"
                        + " 		,users.EMAIL"
                        + " 		,VRR.STATUS"
                        + " 		,VRR.RUN_ID"
                        + " 		,MRD.GROUP_NAME"
                        + "			,VCVM.CATEGORYVALUENAME"
                        + " 	ORDER BY VS.SOLUTIONDESCRIPTION"
                        + " 		,RRV.REG_REPORT_ID"
                        + " 		,RRV.YEAR_OF_ACCOUNT"
                        + " 		,DROE.ORG_CODE"
                        + " 		,DUOE.ORG_CODE"
                        + " 		,RRV.VERSION_NUMBER"
                        + " 		,MRD.GROUP_NAME";

        logger.info("QUERY --> " + query);
        Query q = getSession().createSQLQuery(query);

        if ((page != null && page.trim().length() > 0) && (rows != null && rows.trim().length() > 0)) {
            q.setFirstResult((Integer.parseInt(page) - 1) * Integer.parseInt(rows));
            q.setMaxResults(Integer.parseInt(rows));
        }

        List<Object[]> result = q.list();
        return result;
    }

    @Override
    public List<Object[]> getReturnValidationResultDetails(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                           String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                           String validationStatus, String validationType, String validationCode, String returnValidationCategory, String hashKey, String page,
                                                           String rows) {
        logger.info("EXEFLOW --> ValidationAPIDaoHibernateImpl --> getReturnValidationResultDetails()");

        Integer versionNumber = null;
        Integer periodId = null;
        StringBuilder whereClauseForMaxRun = new StringBuilder("");
        StringBuilder whereClauseForMainSelect = new StringBuilder("");
        StringBuilder whereClauseForDerivedFields = new StringBuilder(" WHERE ");

        if (solution == null || solution.trim().length() == 0) {
            solution = (String) getSession().createSQLQuery("select SOLUTIONDESCRIPTION from VYASASOLUTION where SOLUTIONID=0").getSingleResult();
        }

        whereClauseForMaxRun.append(" WHERE VS.SOLUTIONDESCRIPTION = '" + solution + "'");
        whereClauseForMainSelect.append(" AND VS.SOLUTIONDESCRIPTION = '" + solution + "'");

        if (returnBkey != null && returnBkey.trim().length() > 0) {
            returnBkey = returnBkey.split("\\(").length > 1 ? returnBkey.split("\\(")[1].replace(")", "").trim() : returnBkey;
            returnBkey = ValidationStringUtils.replace(returnBkey, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND DRR.REPORT_BKEY IN ('" + Arrays.asList(returnBkey.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND DRR.REPORT_BKEY IN ('" + Arrays.asList(returnBkey.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (period != null && period.trim().length() > 0) {
            String condition = getPeriodExp(period);
            whereClauseForMaxRun.append(" AND VRR.PERIOD_ID " + condition);
            whereClauseForMainSelect.append(" AND RRV.YEAR_OF_ACCOUNT " + condition);
        }
        if (organizationCode != null && organizationCode.trim().length() > 0) {
            organizationCode = organizationCode.split("\\(").length > 1 ? organizationCode.split("\\(")[1].replace(")", "").trim() : organizationCode;
            organizationCode = ValidationStringUtils.replace(organizationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND DROE.ORG_CODE IN ('" + Arrays.asList(organizationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (createdByUserOrganizationCode != null && createdByUserOrganizationCode.trim().length() > 0) {
            createdByUserOrganizationCode = createdByUserOrganizationCode.split("\\(").length > 1 ? createdByUserOrganizationCode.split("\\(")[1].replace(")", "").trim() : createdByUserOrganizationCode;
            createdByUserOrganizationCode = ValidationStringUtils.replace(createdByUserOrganizationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND DUOE.ORG_CODE IN ('" + Arrays.asList(createdByUserOrganizationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnVersionNumber != null && returnVersionNumber.trim().length() > 0) {
            versionNumber = Integer.parseInt(returnVersionNumber);
            whereClauseForMaxRun.append(" AND VRR.VERSION_NO = " + versionNumber);
            whereClauseForMainSelect.append(" AND RRV.VERSION_NUMBER = " + versionNumber);
        }
        if (returnStatus != null && returnStatus.trim().length() > 0) {
            returnStatus = returnStatus.split("\\(").length > 1 ? returnStatus.split("\\(")[1].replace(")", "").trim() : returnStatus;
            returnStatus = ValidationStringUtils.replace(returnStatus, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND RRV.STATUS IN ('" + Arrays.asList(returnStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND RRV.STATUS IN ('" + Arrays.asList(returnStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationProcessStatus != null && returnValidationProcessStatus.trim().length() > 0) {
            returnValidationProcessStatus = returnValidationProcessStatus.split("\\(").length > 1 ? returnValidationProcessStatus.split("\\(")[1].replace(")", "").trim() : returnValidationProcessStatus;
            returnValidationProcessStatus = ValidationStringUtils.replace(returnValidationProcessStatus, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VRR.STATUS IN ('" + Arrays.asList(returnValidationProcessStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
            whereClauseForMainSelect.append(" AND VRR.STATUS IN ('" + Arrays.asList(returnValidationProcessStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationGroup != null && returnValidationGroup.trim().length() > 0) {
            returnValidationGroup = returnValidationGroup.split("\\(").length > 1 ? returnValidationGroup.split("\\(")[1].replace(")", "").trim() : returnValidationGroup;
            returnValidationGroup = ValidationStringUtils.replace(returnValidationGroup, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VG.GROUP_NAME IN ('" + Arrays.asList(returnValidationGroup.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (validationType != null && validationType.trim().length() > 0) {
            validationType = validationType.split("\\(").length > 1 ? validationType.split("\\(")[1].replace(")", "").trim() : validationType;
            validationType = ValidationStringUtils.replace(validationType, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND VM.VALIDATION_TYPE IN ('" + Arrays.asList(validationType.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (validationCode != null && validationCode.trim().length() > 0) {
            validationCode = validationCode.split("\\(").length > 1 ? validationCode.split("\\(")[1].replace(")", "").trim() : validationCode;
            validationCode = ValidationStringUtils.replace(validationCode, "\"", "", -1, true);
            whereClauseForMainSelect.append(" AND VM.VALIDATION_CODE IN ('" + Arrays.asList(validationCode.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if (returnValidationCategory != null && returnValidationCategory.trim().length() > 0) {
            returnValidationCategory = returnValidationCategory.split("\\(").length > 1 ? returnValidationCategory.split("\\(")[1].replace(")", "").trim() : returnValidationCategory;
            returnValidationCategory = ValidationStringUtils.replace(returnValidationCategory, "\"", "", -1, true);
            whereClauseForMaxRun.append(" AND VRL.VALIDATION_CATEGORY IN ('" + Arrays.asList(returnValidationCategory.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }

        if (validationStatus != null && validationStatus.trim().length() > 0) {
            validationStatus = validationStatus.split("\\(").length > 1 ? validationStatus.split("\\(")[1].replace(")", "").trim() : validationStatus;
            validationStatus = ValidationStringUtils.replace(validationStatus, "\"", "", -1, true);
            whereClauseForDerivedFields.append(" validationStatus IN ('" + Arrays.asList(validationStatus.split(",")).stream().map(String::trim).collect(Collectors.toList()).stream().collect(Collectors.joining("','")) + "')");
        }
        if ((page != null && page.trim().length() > 0) && (rows != null && rows.trim().length() > 0)) {
            if (whereClauseForDerivedFields.length() > 7)
                whereClauseForDerivedFields.append(" AND ");
            whereClauseForDerivedFields.append(" totalOccurrenceRunningSum > " + (Integer.parseInt(page) - 1) * Integer.parseInt(rows));
        }

        if (whereClauseForDerivedFields.length() == 7)
            whereClauseForDerivedFields = new StringBuilder("");

        String query =
                "WITH MAX_RUN_DETAILS"
                        + " AS ("
                        + "	SELECT VG.GROUP_NAME"
                        + "		,MAX(VRR.RUN_ID) RUN_ID"
                        + "	FROM VALIDATION_RETURN_RESULT VRR"
                        + "	INNER JOIN VALIDATION_RUN_DETAILS VRD ON VRR.RUN_ID = VRD.RUN_ID"
                        + "	INNER JOIN VALIDATION_RETURN_LINKAGE VRL ON VRR.SOLUTION_ID = VRL.SOLUTION_ID"
                        + "		AND VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "		AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + "	INNER JOIN VALIDATION_GROUP VG ON VRL.VALIDATION_GROUP_ID = VG.GROUP_ID"
                        + "	INNER JOIN VYASASOLUTION VS ON VRR.SOLUTION_ID = VS.SOLUTIONID"
                        + "	INNER JOIN REG_REPORT_VERSION RRV ON VRR.SOLUTION_ID = RRV.SOLUTION_ID"
                        + "		AND VRR.REG_REPORT_ID = RRV.REG_REPORT_ID"
                        + "		AND VRR.REG_REPORT_VERSION_NUMBER = RRV.REG_REPORT_VERSION"
                        + "		AND VRR.VERSION_NO = RRV.VERSION_NUMBER"
                        + "		AND VRR.PERIOD_ID = RRV.YEAR_OF_ACCOUNT"
                        + "		AND VRR.ORG_ID = RRV.ENTITY_ID"
                        + "		AND RRV.IS_ACTIVE = 'Y'"
                        + "	INNER JOIN DIM_REG_REPORT DRR ON RRV.REG_REPORT_ID = DRR.REG_REPORT_ID"
                        + "		AND RRV.REG_REPORT_VERSION = DRR.REG_REPORT_VERSION"
                        + "	INNER JOIN DIM_REG_REPORT_SECTION DRRV ON VRL.SOLUTION_ID = DRRV.SOLUTION_ID"
                        + "		AND VRL.REG_REPORT_ID = DRRV.REG_REPORT_ID"
                        + "		AND VRL.REG_REPORT_SECTION_ID = DRRV.REG_REPORT_SECTION_ID"
                        + "		AND VRR.REG_REPORT_VERSION_NUMBER = DRRV.REG_REPORT_VERSION"
                        + whereClauseForMaxRun
                        + "	GROUP BY VRR.REG_REPORT_ID"
                        + "		,VRR.PERIOD_ID"
                        + "		,VRR.ORG_ID"
                        + "		,VRR.VERSION_NO"
                        + "		,VS.SOLUTIONDESCRIPTION"
                        + "		,VG.GROUP_NAME"
                        + "		,DRR.REPORT_BKEY"
                        + "		,DRR.REPORT_NAME"
                        + "	)"
                        + "	SELECT * FROM ("
                        + "	 SELECT VS.SOLUTIONDESCRIPTION AS solutionName"
                        + "		,RRV.REG_REPORT_ID AS returnID"
                        + "		,LTRIM(RTRIM(DRR.REPORT_BKEY)) AS returnBkey"
                        + "		,LTRIM(RTRIM(DRR.REPORT_NAME)) AS returnName"
                        + "		,RRV.YEAR_OF_ACCOUNT AS period"
                        + "		,DROE.ORG_CODE AS organizationCode"
                        + "		,DROE.ORG_NAME AS organizationName"
                        + "		,DUOE.ORG_CODE AS createdByUserOrganizationCode"
                        + "		,DUOE.ORG_NAME AS createdByUserOrganizationName"
                        + "		,RRV.VERSION_NUMBER AS returnVersionNumber"
                        + "		,RRV.VERSION_NAME AS returnVersionName"
                        + "		,RRV.STATUS AS returnStatus"
                        + "		,RRV.YEAR_OF_ACCOUNT AS returnPeriod"
                        + "		,RRV.VERSION_DATE AS returnCreatedDate"
                        + "		,users.USER_NAME AS createdByUserID"
                        + "		,users.FIRST_NAME + CASE "
                        + "			WHEN COALESCE(users.MIDDLE_NAME, '') <> ''"
                        + "				THEN ' ' + users.MIDDLE_NAME"
                        + "			ELSE ''"
                        + "			END + ' ' + users.LAST_NAME AS createdByUserName"
                        + "		,users.EMAIL AS createdByUserEmail"
                        + "		,VRR.STATUS AS returnValidationProcessStatus"
                        + "		,MRD.GROUP_NAME AS returnValidationGroup"
                        + "		,VM.VALIDATION_CODE AS validationCode"
                        + "		,VM.VALIDATION_NAME AS validationName"
                        + "		,VM.VALIDATION_DESC AS validationDescription"
                        + "		,VM.VALIDATION_TYPE AS validationType"
                        + "		,CASE  "
                        + "			WHEN VRD.STATUS = 'PASSED' THEN 'Pass' "
                        + "			WHEN VM.VALIDATION_TYPE = 'Mandatory' THEN ( CASE VRD.STATUS WHEN 'FAILED' THEN 'Error' END)"
                        + "			WHEN VM.VALIDATION_TYPE = 'Optional' THEN ( CASE VRD.STATUS WHEN 'FAILED' THEN 'Warning' END)"
                        + "		 END AS validationStatus"
                        + "		,VM.VALIDATION_EXPRESSION AS validationExpression"
                        + "		,DRRV.REG_REPORT_SECTION_DESC AS returnSectionCode"
                        + "		,DRRV.SECTION_NAME AS returnSectionName"
                        + "		,DRRV.FORM_NAME AS returnFormName"
                        + "		,VRL.VALIDATION_CATEGORY AS returnValidationCategory"
                        + "		,VRD.TOTAL_OCCURRENCE AS totalOccurrences"
                        + "		,(VRD.TOTAL_OCCURRENCE - VRD.TOTAL_FAILED) AS totalOccurrencesPassCount"
                        + "		,VRD.TOTAL_FAILED AS totalOccurrencesFailCount"
                        + "		,SUM (VRD.TOTAL_OCCURRENCE) OVER (ORDER BY VS.SOLUTIONDESCRIPTION,RRV.REG_REPORT_ID,RRV.YEAR_OF_ACCOUNT,DROE.ORG_CODE"
                        + "		,DUOE.ORG_CODE,RRV.VERSION_NUMBER,MRD.GROUP_NAME,VRD.VALIDATION_ID,VRD.SEQUENCE_NUMBER) AS totalOccurrenceRunningSum"
                        + "		,VRD.VALIDATION_ID AS validationId"
                        + "		,VRD.SEQUENCE_NUMBER AS sequenceNumber"
                        + "		,VRR.RUN_ID AS runId"
                        + "		,VRL.IS_COMMENT_AT_VALIDATION AS isCommentAtValidation"
                        + "		,VCVM.CATEGORYVALUENAME AS categoryValueName"
                        + "		,VRD.DIMENSIONS_CSV AS dimensionCSV"
                        + "	FROM VALIDATION_RETURN_RESULT VRR"
                        + "	INNER JOIN VALIDATION_RUN_DETAILS VRD ON VRR.RUN_ID = VRD.RUN_ID"
                        + "	INNER JOIN VALIDATION_RETURN_LINKAGE VRL ON VRR.SOLUTION_ID = VRL.SOLUTION_ID"
                        + "		AND VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + "		AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + "	INNER JOIN VALIDATION_GROUP VG ON VRL.VALIDATION_GROUP_ID = VG.GROUP_ID"
                        + "	INNER JOIN VYASASOLUTION VS ON VRR.SOLUTION_ID = VS.SOLUTIONID"
                        + "	INNER JOIN REG_REPORT_VERSION RRV ON VRR.SOLUTION_ID = RRV.SOLUTION_ID"
                        + "		AND VRR.REG_REPORT_ID = RRV.REG_REPORT_ID"
                        + "		AND VRR.REG_REPORT_VERSION_NUMBER = RRV.REG_REPORT_VERSION"
                        + "		AND VRR.VERSION_NO = RRV.VERSION_NUMBER"
                        + "		AND VRR.PERIOD_ID = RRV.YEAR_OF_ACCOUNT"
                        + "		AND VRR.ORG_ID = RRV.ENTITY_ID"
                        + "		AND RRV.IS_ACTIVE = 'Y'"
                        + "	INNER JOIN DIM_ORGANIZATION_ENTITY DROE ON RRV.ENTITY_ID = DROE.ORG_ID"
                        + "	INNER JOIN DIM_REG_REPORT DRR ON RRV.REG_REPORT_ID = DRR.REG_REPORT_ID"
                        + "		AND RRV.REG_REPORT_VERSION = DRR.REG_REPORT_VERSION"
                        + "	INNER JOIN USERS users ON RRV.USERNAME = users.USER_NAME"
                        + "	INNER JOIN DIM_ORGANIZATION_ENTITY DUOE ON users.ORG_ID = DUOE.ORG_ID"
                        + "	INNER JOIN VALIDATION_MASTER VM ON VRR.SOLUTION_ID = VM.SOLUTION_ID"
                        + "		AND VRD.VALIDATION_ID = VM.VALIDATION_ID"
                        + "		AND VRD.SEQUENCE_NUMBER = VM.SEQUENCE_NO"
                        + "	INNER JOIN DIM_REG_REPORT_SECTION DRRV ON VRL.SOLUTION_ID = DRRV.SOLUTION_ID"
                        + "		AND VRL.REG_REPORT_ID = DRRV.REG_REPORT_ID"
                        + "		AND VRL.REG_REPORT_SECTION_ID = DRRV.REG_REPORT_SECTION_ID"
                        + "		AND VRR.REG_REPORT_VERSION_NUMBER = DRRV.REG_REPORT_VERSION"
                        + "		AND VRD.SEQUENCE_NUMBER = VM.SEQUENCE_NO"
                        + "	INNER JOIN MAX_RUN_DETAILS MRD ON MRD.RUN_ID = VRR.RUN_ID"
                        + "		AND MRD.GROUP_NAME = VG.GROUP_NAME"
                        + "	INNER JOIN VYASAARTEFACT VA ON VA.REPORTID = DRR.REG_REPORT_ID"
                        + "	LEFT OUTER JOIN VYASAARTEFACTCATEGORYVALUELINK VACVL ON VA.ARTEFACTID = VACVL.ARTEFACTID AND VACVL.CATEGORYID = (SELECT CATEGORYID FROM VYASACATEGORYMASTER WHERE CATEGORYINTERNALNAME = 'Period_Format') AND VACVL.ISACTIVE = 1"
                        + "	LEFT OUTER JOIN VYASACATEGORYVALUESMASTER VCVM ON VCVM.CATEGORYID = VACVL.CATEGORYID AND VCVM.CATEGORYVALUEID = VACVL.CATEGORYVALUEID AND VCVM.ISACTIVE = 1"
                        + "	WHERE RRV.IS_ACTIVE = 'Y'"
                        + whereClauseForMainSelect
                        + "	GROUP BY VS.SOLUTIONDESCRIPTION"
                        + "		,RRV.REG_REPORT_ID"
                        + "		,DRR.REPORT_BKEY"
                        + "		,DRR.REPORT_NAME"
                        + "		,RRV.YEAR_OF_ACCOUNT"
                        + "		,DROE.ORG_CODE"
                        + "		,DROE.ORG_NAME"
                        + "		,DUOE.ORG_CODE"
                        + "		,DUOE.ORG_NAME"
                        + "		,RRV.VERSION_NUMBER"
                        + "		,RRV.VERSION_NAME"
                        + "		,RRV.STATUS"
                        + "		,RRV.YEAR_OF_ACCOUNT"
                        + "		,RRV.VERSION_DATE"
                        + "		,users.USER_NAME"
                        + "		,users.FIRST_NAME"
                        + "		,users.MIDDLE_NAME"
                        + "		,users.LAST_NAME"
                        + "		,users.EMAIL"
                        + "		,VRR.STATUS"
                        + "		,VRR.RUN_ID"
                        + "		,MRD.GROUP_NAME"
                        + "		,VRD.VALIDATION_ID"
                        + "		,VRD.SEQUENCE_NUMBER"
                        + "		,VM.VALIDATION_CODE"
                        + "		,VM.VALIDATION_NAME"
                        + "		,VM.VALIDATION_DESC"
                        + "		,VM.VALIDATION_TYPE"
                        + "		,VM.VALIDATION_EXPRESSION"
                        + "		,DRRV.REG_REPORT_SECTION_DESC"
                        + "		,DRRV.SECTION_NAME"
                        + "		,DRRV.FORM_NAME"
                        + "		,VRD.STATUS"
                        + "		,VRD.TOTAL_OCCURRENCE"
                        + "		,VRD.TOTAL_FAILED"
                        + "		,VRL.VALIDATION_CATEGORY"
                        + "		,VRL.IS_COMMENT_AT_VALIDATION"
                        + "		,VCVM.CATEGORYVALUENAME"
                        + "		,VRD.DIMENSIONS_CSV"
                        + " ) subquery"
                        + whereClauseForDerivedFields;

        logger.info("QUERY --> " + query);
        Query q = getSession().createSQLQuery(query);
        List<Object[]> result = q.list();
        return result;
    }

    private String getPeriodExp(String period) {
        String whereClause = null;

        switch (period.split("\\(")[0].trim().toUpperCase()) {
            case EQ:
                period = period.split("\\(")[1].replace(")", "").trim();
                whereClause = " = " + Integer.parseInt(period);
                break;
            case LT:
                period = period.split("\\(")[1].replace(")", "").trim();
                whereClause = " < " + Integer.parseInt(period);
                break;
            case GT:
                period = period.split("\\(")[1].replace(")", "").trim();
                whereClause = " > " + Integer.parseInt(period);
                break;
            case LEQ:
                period = period.split("\\(")[1].replace(")", "").trim();
                whereClause = " <= " + Integer.parseInt(period);
                break;
            case GEQ:
                period = period.split("\\(")[1].replace(")", "").trim();
                whereClause = " >= " + Integer.parseInt(period);
                break;
            case RANGE:
                period = period.split("\\(")[1].replace(")", "").trim();
                whereClause = " BETWEEN " + Integer.parseInt(period.split(",")[0]) + " AND " + Integer.parseInt(period.split(",")[1]);
                break;
        }

        return whereClause;
    }
}
