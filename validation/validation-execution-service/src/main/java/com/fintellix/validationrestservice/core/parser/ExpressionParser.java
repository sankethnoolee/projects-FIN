package com.fintellix.validationrestservice.core.parser;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fintellix.framework.validation.dto.ValidationGroupCsvLinkage;
import com.fintellix.validationrestservice.util.BeanUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fintellix.framework.validation.bo.ValidationExecutionBo;
import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.redis.CacheCoordinator;
import com.fintellix.redis.RedisKeys;
import com.fintellix.validationrestservice.core.ValidationCsvDeleteScheduler;
import com.fintellix.validationrestservice.core.evaulator.ValidationFunctions;
import com.fintellix.validationrestservice.core.executor.ExpressionExecutor;
import com.fintellix.validationrestservice.core.executor.ExpressionStatus;
import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroup;
import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroups;
import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;
import com.fintellix.validationrestservice.core.lexer.tokenizer.CharacterStream;
import com.fintellix.validationrestservice.core.lexer.tokenizer.ExpressionTokenizer;
import com.fintellix.validationrestservice.core.lexer.tokenizer.ExpressionTokenizerForSpELFunctions;
import com.fintellix.validationrestservice.core.metadataresolver.MetadataResolver;
import com.fintellix.validationrestservice.core.parser.prefix.PrefixParser;
import com.fintellix.validationrestservice.core.parser.prefix.ast.AstNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.FunctionNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.VariableNode;
import com.fintellix.validationrestservice.core.runprocessor.MetadataBuilder;
import com.fintellix.validationrestservice.definition.EntityMetadataInfo;
import com.fintellix.validationrestservice.definition.ExpressionEntityDetail;
import com.fintellix.validationrestservice.definition.ExpressionResultDetail;
import com.fintellix.validationrestservice.definition.QueryBuilder;
import com.fintellix.validationrestservice.definition.RefEntityDetail;
import com.fintellix.validationrestservice.definition.RefMetadataInfo;
import com.fintellix.validationrestservice.definition.ReturnEntityDetail;
import com.fintellix.validationrestservice.definition.ReturnMetadataInfo;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.definition.ValidationEntityDetail;
import com.fintellix.validationrestservice.exception.BaseValidationException;
import com.fintellix.validationrestservice.exception.InvalidRequestException;
import com.fintellix.validationrestservice.spark.SparkJob;
import com.fintellix.validationrestservice.spark.util.JsonUtilForSpark;
import com.fintellix.validationrestservice.spark.util.SparkProcessUtil;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.CalendarProcessor;
import com.fintellix.validationrestservice.util.FileCompressor;
import com.fintellix.validationrestservice.util.ObjectCloner;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.vo.DynamicFilter;
import com.fintellix.validationrestservice.vo.ExpressionKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Component
@Scope("prototype")
public class ExpressionParser implements Serializable {
    private static final long serialVersionUID = -2053422506945012615L;
    
    @Autowired
	private Function<String, ExpressionExecutor> expressionExecutorBeanFactory;
    
    @Autowired
    private ValidationCsvDeleteScheduler validationCsvDeleteScheduler;
    
    
    private static final Set<String> AGG_VLOOKUP_FUNCTIONS = new HashSet<>(
            Arrays.asList("VLOOKUP", "SUMIF", "MAXIF", "MINIF", "COUNTIF", "SUM", "MAX", "MIN", "COUNT", "AVG", "DCOUNT", "UNIQUE"));
    private static final Set<String> ART_LOG_FUNC = new HashSet<>(Arrays.asList("+", "-", "/", "*", ">", "<", "=", "!", ":", "?", "{", "}", "(", ")","[","]"," "));
    private static Map<String, String> nameOperatorConflitMap = new HashMap<>();
	
    private static Boolean isSparkEnabled = Boolean.FALSE;
    private Map<String,String> columnUUIDMap = new ConcurrentHashMap<String, String>();

	
    static {
        for (String s : ART_LOG_FUNC) {
            nameOperatorConflitMap.put(s, "###" + (int) (s.charAt(0)) + "###");
        }
        String sparkEnabled = ApplicationProperties.getValue("app.spark.enabled").trim();
        
        if (sparkEnabled == null) {
    		sparkEnabled = "false";
    	} else if (sparkEnabled.trim().length() == 0) {
    		sparkEnabled = "false";
    	}
    	isSparkEnabled = Boolean.parseBoolean(sparkEnabled);
    }

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public Map<Integer, List<ExpressionEntityDetail>> expressionEntityDetailsMap = new HashMap<>();
    private Map<ExpressionKey, List<Integer>> expressionGroupMap = new HashMap<>();

    // System variables
    private Integer systemRegReportId;
    private String systemEntityCode;
    private Integer systemSolutionId;
    private String systemOrgCode;
    private Integer systemOrgId;
    private Integer systemPeriodId;
    private Integer systemRegReportVersion;
    private Integer systemVersionNo;
    private String systemReturnStatus;
    private List<DynamicFilter> dynamicFilters = new ArrayList<>();
    private String systemEntityType;
    private String systemSubjectArea;

    private Integer runId;
    private Integer expressionId;
    private Map<Integer, String> expressionIdMap = new HashMap<>();
    private Map<Integer, ValidationMaster> vmMap = new HashMap<>();
    private Map<String, String> meMap = new HashMap<>();
    private Map<String, List<String>> metadataErrorMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, String>> expressionToColumnsMap = new HashMap<>();
    private Map<Integer, Set<String>> expressionToTablesMap = new HashMap<>();

    private Map<Integer, Boolean> expJobLink = new ConcurrentHashMap<>();

    private Map<Integer, Boolean> failedMetaDataExp = new HashMap<>();

    public ExecutorService pipelinePool;
    public List<Future<ExpressionStatus>> pipelinerunningJobs;
    
	@Autowired
	ValidationExecutionBo validationExecutionBo;
	
	public ExpressionExecutor getExpressionExecutorInstance(String name) {
		ExpressionExecutor bean = expressionExecutorBeanFactory.apply(name);
		return bean;
	}
	

    // Testing function
  /* public static void main(String[] args) {
        ExpressionParser expressionParser = new ExpressionParser();
        Map<Integer, String> expressionIdMap = new HashMap<>();

        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"LCR\".\"012A\"){AND(IF(UPPER(ME.\"SyndicateTypeActive\") == \"TRUE\")THEN(UPPER(ME.\"Syndicate_Type\") ==\"TRUE\")ELSE(FALSE), IF(UPPER(ME.\"SyndicateTypeActive\") == \"FALSE\")THEN(UPPER(ME.\"Syndicate_Type\")==\"FALSE\")ELSE(FALSE))}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMB\".\"010D\"){VLOOKUP(ME,RTN.\"QMB\".\"105\",[RTN.\"QMB\".\"105\".\"Pure Year\" == ME.\"Pure Year\"],[RTN.\"QMB\".\"105\".\"GrPremWritCum_CUR_COB_PYR_DCC\"],[ME.\"Pure Year\" NOTIN [2014]])}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMA\".\"100\",[RTN.\"QMA\".\"100\".\"ReportingYearOfAccount\",RTN.\"QMA\".\"100\".\"Currency\"],[RTN.\"QMA\".\"100\".\"Currency\" IN [\"CNV\"]]){IF(ME.\"AcqCostsBrokerage_CUR_RYR_LCALC\" > 1000000) THEN((SUMIF(RTN.\"QMA\".\"100\".\"AcqCostsBrokerage_CUR_RYR_LCALC\"(PERIOD(YEARLY,1)),,[RTN.\"QMA\".\"100\".\"Currency\" IN [\"CNV\"]])/SUMIF(RTN.\"QMA\".\"100\".\"GrPrem_CUR_RYR_LCALC\"(PERIOD(YEARLY,1)),,[RTN.\"QMA\".\"100\".\"Currency\" IN [\"CNV\"]])) < 0.10) ELSE(TRUE)}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMA\".\"360\"){DATEDIFF(ME.\"ClosureRITCDate_RYR\", ME.\"ClosureRITCDate_RYR\", \"Y\")}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMA\".\"360\"){ME.\"ClosureRITCDate_RYR\"}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMB\".\"010\"){ENT.\"SA1\".\"Coverholder PMD UMR\".\"UMR\">UPPER(ME.\"ContactUsername\")}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(REFTBL.\"Coverholder PMD UMR\"){ME.\"UMR\"}>0"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMB\".\"010\"){SUM(ENT.\"SA1\".\"Coverholder PMD UMR\".\"UMR\")}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMB\".\"010E\"){ENT.\"SA1\".\"Coverholder PMD UMR\".\"UMR\" == ME.\"Pure Year\"}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"QMB\".\"010\"){UPPER(ME.\"ContactUsername\") IN [\"ABC\", \"MA001USER1\"]}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(ENT.\"SA1\".\"Table1\"){(ME.\"SA1\") BETWEEN [\"08-04-2019\", \"10-04-2019\"]}"));
        expressionIdMap.put(1, expressionParser.convertIfThenElseToTernaryAndProcessPriorityBrackets("FOREACH(RTN.\"PMD\".\"288\"){IF(AND(VLOOKUP(ME,REFTBL.\"Coverholder PMD UMR\",[ME.\"ExpiringUMR\"==REFTBL.\"Coverholder PMD UMR\".\"UMR\"]),VLOOKUP(ME,REFTBL.\"Coverholder PMD\",[ME.\"CoverholderPin\"==REFTBL.\"Coverholder PMD\".\"Coverholder PIN\"])))THEN(TRUE)ELSE(FALSE)}"));
		
//        expressionParser.expressionIdMap = expressionIdMap;
//        expressionParser.systemSolutionId = 0;
//        expressionParser.systemOrgCode = "2005";
//        expressionParser.systemOrgId = 4;
//        expressionParser.runId = 99999;
//        expressionParser.systemPeriodId = 20180101;
//        expressionParser.systemRegReportId = 5001;
//        expressionParser.systemRegReportName = "SBF_Revisited";
//        expressionParser.systemRegReportVersion = 2;
//        expressionParser.systemVersionNo = 319;

        expressionParser.processRequest(new MetadataResolver());
   }*/

	public void init(Map<Integer, String> expressionIdMap, ValidationRequest request, Map<Integer, ValidationMaster> vmMap) {
        long startTime = System.currentTimeMillis();
        this.systemPeriodId = request.getPeriodId();
        this.expressionIdMap = expressionIdMap;
        this.systemSolutionId = request.getSolutionId();
        this.systemOrgCode = request.getOrgCode();
        this.systemOrgId = request.getOrgId();
        this.runId = request.getRunId();
        this.vmMap = vmMap;
        this.systemEntityType = request.getEntityType();

		String isCsvDeleteRequired = ApplicationProperties.getValue("app.validations.iscsvdeleterequired");
        MetadataResolver resolver = new MetadataResolver();
        resolver.setSolutionId(systemSolutionId);

        try {
            JsonObject payloadObj = new JsonParser().parse(request.getPayload()).getAsJsonObject();

            if (request.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                if (payloadObj.get("regReportId") != null && !payloadObj.get("regReportId").toString().trim().equals("")) {
                    this.systemRegReportId = Integer.parseInt(payloadObj.get("regReportId").toString());
                } else {
                    throw new InvalidRequestException("RegReportId is missing!");
                }

                if (payloadObj.get("regReportName") != null && !payloadObj.get("regReportName").toString().trim().equals("")) {
                    this.systemEntityCode = payloadObj.get("regReportName").toString().replace("\"", "");
                } else {
                    throw new InvalidRequestException("RegReportName is missing!");
                }

                if (payloadObj.get("regReportVersion") != null && !payloadObj.get("regReportVersion").toString().trim().equals("")) {
                    this.systemRegReportVersion = Integer.parseInt(payloadObj.get("regReportVersion").toString());
                } else {
                    throw new InvalidRequestException("RegReportVersion is missing!");
                }

                if (payloadObj.get("versionNo") != null && !payloadObj.get("versionNo").toString().trim().equals("")) {
                    this.systemVersionNo = Integer.parseInt(payloadObj.get("versionNo").toString());
                } else {
                    throw new InvalidRequestException("VersionNo is missing!");
                }

                if (payloadObj.get("returnStatus") != null && !payloadObj.get("returnStatus").toString().trim().equals("")) {
                    this.systemReturnStatus = payloadObj.get("returnStatus").toString().replace("\"", "");
                }

                updateReturnResultStatus(request.getRunId(), request.getSolutionId(),
                        ValidationConstants.VALIDATION_STATUS_PROCESSING, resolver);
            } else if (request.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE) ||
                    request.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
                if (payloadObj.get("entityCode") != null && !payloadObj.get("entityCode").toString().trim().equals("")) {
                    this.systemEntityCode = payloadObj.get("entityCode").toString().replace("\"", "");
                } else {
                    throw new InvalidRequestException("Entity code is missing!");
                }

                if (payloadObj.get("subjectArea") != null && !payloadObj.get("subjectArea").toString().trim().equals("")) {
                    this.systemSubjectArea = payloadObj.get("subjectArea").toString().replace("\"", "");
                }

                extractDynamicFilter(payloadObj, request.getEntityType());
            }

            updateRequestStatus(request, ValidationConstants.VALIDATION_STATUS_PROCESSING, resolver);

            if (!expressionIdMap.isEmpty()) {
                processRequest(resolver);
            }

            /* disabling status to set as VALIDATION_STATUS_COMPLETED_WITH_ERRORS 
            if (metadataErrorMap.isEmpty()) {
                updateRequestStatus(request, ValidationConstants.VALIDATION_STATUS_COMPLETED, resolver);
                updateReturnResultStatus(request.getRunId(), request.getSolutionId(), ValidationConstants.VALIDATION_STATUS_COMPLETED, resolver);
            } else {
                updateRequestStatus(request, ValidationConstants.VALIDATION_STATUS_COMPLETED_WITH_ERRORS, resolver);
                updateReturnResultStatus(request.getRunId(), request.getSolutionId(), ValidationConstants.VALIDATION_STATUS_COMPLETED_WITH_ERRORS, resolver);
            }*/

            updateRequestStatus(request, ValidationConstants.VALIDATION_STATUS_COMPLETED, resolver);

            if (request.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                updateReturnResultStatus(request.getRunId(), request.getSolutionId(),
                        ValidationConstants.VALIDATION_STATUS_COMPLETED, resolver);

                // Todo need to handle for entity type other than RTN | Deepak
                //csv delete
                if(isCsvDeleteRequired.equalsIgnoreCase("true")) {
                    saveValidationResultsCsvDeleteDetails(request.getRunId(), resolver);
                    validationCsvDeleteScheduler.addRunIds(request.getRunId());
                }
            }
        } catch (Exception e) {
            updateRequestStatus(request, ValidationConstants.VALIDATION_STATUS_FAILED, resolver);

            if (request.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                updateReturnResultStatus(request.getRunId(), request.getSolutionId(),
                        ValidationConstants.VALIDATION_STATUS_FAILED, resolver);
            }

            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }

        LOGGER.warn("Request completed in " + (System.currentTimeMillis() - startTime) + " milli second's");
    }

    private void extractDynamicFilter(JsonObject payloadObj, String entityType) {
        if (payloadObj.get("filters") != null && !payloadObj.get("filters").toString().trim().equals("")
                && payloadObj.get("filters").isJsonArray()) {
            try {
                final Gson gson = new Gson();
                DynamicFilter dynamicFilter;

                for (JsonElement ele : payloadObj.get("filters").getAsJsonArray()) {
                    dynamicFilter = gson.fromJson(ele, DynamicFilter.class);
                    String columnName = dynamicFilter.getColumnName();

                    if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
                        columnName = entityType + ".\"" + systemEntityCode + "\"" + ".\"" + columnName + "\"";
                    } else if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
                        columnName = entityType + ".\"" + systemSubjectArea + "\"" + ".\"" + systemEntityCode + "\"" + ".\"" + columnName + "\"";
                    }
                    dynamicFilter.setColumnName(columnName);
                    dynamicFilters.add(dynamicFilter);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    private void saveValidationResultsCsvDeleteDetails(Integer runId, MetadataResolver resolver) {
    	String payload = resolver.getValidationRequestPayloadByRunId(runId);
    	prepareValidationResultsCsvDeleteDetails(runId,payload,resolver);
	}
	private void prepareValidationResultsCsvDeleteDetails(Integer runId, String payload, MetadataResolver resolver) {
        //List<String> columns = new ArrayList<String>();
        //columns.add("runId");
        //columns.add("validationCount");
		//List<Integer> runIds = resolver.getObsoleteRunIdsWithGroupFilter(payload,columns);
		List<Integer> runIds = resolver.getObsoleteRunIdsWithGroupFilter(payload);
		
		Set<String> currentValidationTypes = validationTypes(payload);
		Map<Integer,List<Integer>> csvDeleteDetails = new HashMap<>();
		List<Integer> deletableRunIds = new ArrayList<>();
		if(runIds != null) {
		for(Integer valRunId : runIds) {
			String valPayload = resolver.getValidationRequestPayloadByRunId(valRunId);
			Set<String> oldValidationTypes = validationTypes(valPayload);
			Boolean isDelete = isDeleteCsv(currentValidationTypes,oldValidationTypes);
			if(isDelete) {
				deletableRunIds.add(valRunId);
			}
		}
		if(csvDeleteDetails.get(runId) == null) {
			csvDeleteDetails.put(runId, deletableRunIds);
		}
		try {
				CacheCoordinator.save(RedisKeys.CONFIGURED_VALIDATION_CSV_RESULTS_DELETE.getKey(), ValidationConstants.CURRENT_RUN_ID+"_"+runId, csvDeleteDetails);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	}

	private Boolean isDeleteCsv(Set<String> currentValidationTypes, Set<String> oldValidationTypes) {
			
			if(currentValidationTypes.equals(oldValidationTypes)) {
				return Boolean.TRUE;
			}else {
				if(currentValidationTypes.size() > oldValidationTypes.size()) {
					Boolean isVisible = Boolean.FALSE;
					for(String valType : oldValidationTypes) {
						if(currentValidationTypes.contains(valType)) {
							isVisible = Boolean.TRUE;
						}else {
							return Boolean.FALSE;
						}
					}
					if(isVisible) {
						return Boolean.TRUE;
					}
				}
			}		
		return Boolean.FALSE;
	}

	private void processRequest(MetadataResolver resolver) {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        try {
            /*
             * for populating global Maps for expression
             */
            expressionIdMap.forEach((validationId, expression) -> {
                LOGGER.info("Building data for expression id : " + validationId);
                this.expressionId = validationId;
                QueryBuilder builder = new QueryBuilder();
                ValidationFunctions functions = new ValidationFunctions();
                AstNode parsedNode = tokenizeAndParseIntoTree(expression);

                if (parsedNode.getToken().getType().equals(TokenType.FUNCTION)
                        && parsedNode.getToken().getValue().equalsIgnoreCase("FOREACH")) {
                    processForEachForExpressionMap(parsedNode, builder, functions, validationId);
                } else {
                    ExpressionKey key = new ExpressionKey(expression, null, null);
                    populateExpressionGroupMap(key, validationId);
                }

                processNodeForExpressionsMap(parsedNode, new HashMap<String, String>(), expression,
                        builder, functions, validationId);
            });

            /*
             * Execution of expressions
             */
            metadataBuilder.createDataSet(expressionEntityDetailsMap, systemSolutionId, systemOrgCode, systemOrgId,
                    systemPeriodId, systemRegReportVersion, systemVersionNo, systemRegReportId, runId);

            pipelinePool = new ThreadPoolExecutor(Integer.parseInt(ApplicationProperties.getValue("app.validations.expression.corePoolSize")),
                    Integer.parseInt(ApplicationProperties.getValue("app.validations.expression.maximumPoolSize")),
                    Integer.MAX_VALUE, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(Integer.parseInt(ApplicationProperties.getValue("app.validations.expression.queueCapacity"))),
                    new EnquePipeJobRequest());
 
            pipelinerunningJobs = new ArrayList<>();
            Map<Integer, ExpressionStatus> runRecordDetails = new HashMap<>();
            List<DynamicFilter> dynamicFilters;

            for (Integer exprId : expressionIdMap.keySet()) {
                if (failedMetaDataExp.get(exprId) == null) {
                    this.expressionId = exprId;
                    expressionToColumnsMap.put(exprId, new HashMap<>());
                    expressionToTablesMap.put(exprId, new HashSet<>());
//                    runRecordDetails.computeIfAbsent(exprId, i -> new CopyOnWriteArrayList<>());
                    dynamicFilters = (List<DynamicFilter>) ObjectCloner.deepCopy(this.dynamicFilters);

                    ExpressionExecutor expressionExecutor = getExpressionExecutorInstance(System.currentTimeMillis() + "_" + Math.random()+"_"+exprId);
                    expressionExecutor.init(exprId, expressionToColumnsMap, expressionToTablesMap, expressionIdMap,
                            metadataBuilder, this, resolver, systemSolutionId, runId, vmMap,
                            expJobLink, dynamicFilters);
                    expressionExecutor.executeRequest();
//                    pipelinerunningJobs.add(pipelinePool.submit(expressionExecutor));
                    //TODO
                }
            }

            populateError(runRecordDetails);
//            for(Future<ExpressionStatus> job:pipelinerunningJobs) {
//            	ExpressionStatus status = job.get();
//            	if(status.getHasError()) {
//            		expJobLink.put(job.get().getExprId(), Boolean.TRUE);
//            	}
//            	
//            }
            
            pipelinerunningJobs.forEach(job -> {
            	while (!(job.isDone() || job.isCancelled())) {
                    //do nothing
                }
            });
            pipelinerunningJobs.clear();

			for (Integer expId : expJobLink.keySet()) {
				if (expJobLink.get(expId)) {
					// setting the error flag at the first index.
					if (!runRecordDetails.isEmpty()) {

						if (runRecordDetails.get(expId) != null) {
							runRecordDetails.get(expId).setHasError(true);
						} else {
							runRecordDetails.put(expId, new ExpressionStatus(expId, Boolean.TRUE));
						}
					}
					populateErrorMap("Failed to execute validation expression with valId : " + expId);
					LOGGER.error("Failed to execute validation expression with valId : " + expId);
				}
			}

			for (Integer expId : failedMetaDataExp.keySet()) {

				if(runRecordDetails.get(expId) == null) {
					runRecordDetails.put(expId, new ExpressionStatus(expId, Boolean.TRUE));
				}
				populateErrorMap("Failed to execute validation expression with valId : " + expId);
				LOGGER.error("Failed to execute validation expression with valId : " + expId);
			}

            List<ValidationRunDetails> validationRunDetailsList = new ArrayList<>();

            if(isSparkEnabled) {
                ValidationExecutionGroups valExecGroups = new ValidationExecutionGroups();
                valExecGroups.setRunId(runId);
                valExecGroups.setColumnUUIDMap(columnUUIDMap);
                valExecGroups.setDimensionColumnData(metadataBuilder.getDimensionColumnData());

                for (ExpressionKey key : expressionGroupMap.keySet()) {
                    ValidationExecutionGroup group = new ValidationExecutionGroup();
                    String queryStr = "";
                    int count = 0;
                    for (Integer exprId : expressionGroupMap.get(key)) {
                        ExpressionStatus expStatus = runRecordDetails.get(exprId);
                        if (expStatus == null) {
                            expStatus = new ExpressionStatus(exprId, Boolean.TRUE);
                            runRecordDetails.put(exprId, expStatus);
                        }

                        if (expStatus.getHasError()) {
                            group.addFailedExpression(expStatus, exprId);
                        } else {
                            group.addExpression(expStatus, exprId);

                            if (count == 0) {
                                queryStr = expStatus.getQuery();
                            } else {
                                String select = null;
                                Map<String, String> usedColumns = expStatus.getUsedColumnsDataType();
                                Map<String, String> groupByColumns = expStatus.getGroupByCols();
                                for (String col : usedColumns.keySet()) {
                                    if (!groupByColumns.containsKey(col) && col.startsWith("DV_")) {
                                        if(select == null) {
                                            select = "";
                                        }
                                        select += ",t2." + col;
                                    }
                                }

                                // add join condition

                                if(select!=null) {
                                    select = "select t1.*"+select;
                                    String join = null;


                                    for(String col: groupByColumns.keySet()) {
                                        if(join == null) {
                                            join = " ON ";
                                        }else {
                                            join += " AND ";
                                        }
                                        join += "t1."+col+"= t2."+col ;
                                    }
                                    queryStr = select + " from("+queryStr+") t1 LEFT OUTER JOIN ("+expStatus.getQuery()+") t2 "+join;
                                }
                            }
                            count++;
                        }
                    }
                    group.setQuery(queryStr);
                    LOGGER.info("final Query: "+ queryStr);
                    group.setGroupName(key.toString());
                    valExecGroups.addValidationExecutionGroup(group);
                }

                valExecGroups.setRunId(valExecGroups.getRunId()==null?-9999:valExecGroups.getRunId());
                JsonUtilForSpark.writeValidationGroupsToFile(valExecGroups);
                SparkProcessUtil.executeProcess(valExecGroups.getRunId());
                ValidationExecutionGroups veg = JsonUtilForSpark.readValidationGroupsToFile(valExecGroups.getRunId());
                LOGGER.info("Validation Groups : " + veg);

                if(!veg.getValidationExecutionGroups().isEmpty()) {
                    List<ValidationGroupCsvLinkage> linkages = new ArrayList<>();

                    veg.getValidationExecutionGroups().forEach(group -> {
                        final Integer runId = veg.getRunId();
                        final String folderName = group.getGroupName();
                        final String csvName = (group.getCsvNames() != null && !group.getCsvNames().isEmpty())
                                ? group.getCsvNames().get(0)
                                : null;

                        if (!group.getExpressionMap().isEmpty()) {
                            group.getExpressionMap().forEach((k,v)  -> {
                                linkages.add(getValidationToCsvLinkage(runId, k, folderName, csvName, true));
                                validationRunDetailsList.add(getValidationResultsObject(runId, vmMap.get(k), v));
                            });
                        }

                        if (!group.getFailedExpressionMap().isEmpty()) {
                            group.getFailedExpressionMap().forEach((k,v)  -> {
                                linkages.add(getValidationToCsvLinkage(runId, k, folderName, csvName, false));
                                validationRunDetailsList.add(getValidationResultsObject(runId, vmMap.get(k), v));
                            });
                        }
                    });

                    /* saving validation-csv linkage */
                    if (!linkages.isEmpty()) {
                        LOGGER.info("Saving linkages.");
                        saveValidationGroupCsvLinkage(linkages);
                    }
                }
            } else {
                for (Integer exprId : expressionIdMap.keySet()) {
                    validationRunDetailsList.add(getValidationResultsObject(runId, vmMap.get(exprId), runRecordDetails.get(exprId)));
                }
            }

            /* Saving validation run details */
            saveOrUpdateReturnResultStatus(validationRunDetailsList, resolver);
            LOGGER.info("Saving Result finished.");
        } catch (BaseValidationException ex) {
            populateErrorMap(ex.getMessage());
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        } catch (Throwable th) {
            populateErrorMap("Failed to process request with runId : " + runId);
            LOGGER.error("Failed to process request with runId : " + runId);
            th.printStackTrace();
        } finally {
        	try {
        		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId +
                        File.separator;
        		FileCompressor.getInstance().addFolder(outputDirectory);
        	}catch (Exception e) {
        		LOGGER.error("Error occured while adding folder for compression");
			}
            if (!pipelinePool.isShutdown()) {
                pipelinePool.shutdown();
            } 
            try {
                metadataBuilder.dropTables(systemSolutionId);
            } catch (Throwable e) {
                LOGGER.error("Error occured while droping tables " + e);
            }
        }
    }

    private void populateError(Map<Integer, ExpressionStatus> runRecordDetails)
			throws InterruptedException, ExecutionException {

		try {
			Future<ExpressionStatus> futureJob = null;
			for (Future<ExpressionStatus> job : pipelinerunningJobs) {

				futureJob = job;
				ExpressionStatus status = job.get();
				Integer expId = status.getExprId();
				if (status.getHasError()) {
					expJobLink.put(status.getExprId(), Boolean.TRUE);
				}

				runRecordDetails.put(expId, status);
				break;

			}
			pipelinerunningJobs.remove(futureJob);

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (!pipelinerunningJobs.isEmpty()) {
				populateError(runRecordDetails);
			} else {
				System.gc();
			}
		}

	}


	
	public AstNode tokenizeAndParseIntoTree(String expression) {
        final ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer();
        final PrefixParser prefixParser = new PrefixParser();
        String inputStr = expression;

        inputStr = inputStr.replaceAll(",\\s*?,", "," + ValidationConstants.EMPTY_PARAMETER + ",")
                .replaceAll("\\(\\s*?,", "(" + ValidationConstants.EMPTY_PARAMETER + ",");

        String getStringBetweenQuotes = "([\"'])(?:(?=(\\\\?))\\2.)*?\\1";
        Pattern regEx;
        Matcher matcher;
        regEx = Pattern.compile(getStringBetweenQuotes);
        matcher = regEx.matcher(inputStr);
        String MatchedStr;
        Map<String, String> matchedSubMapAndReplacedString = new HashMap<String, String>();
        while (matcher.find()) {
            MatchedStr = matcher.group();
            for (String s : nameOperatorConflitMap.keySet()) {
                if (matchedSubMapAndReplacedString.containsKey(MatchedStr)) {
                    matchedSubMapAndReplacedString.put(MatchedStr, ValidationStringUtils.replace(matchedSubMapAndReplacedString.get(MatchedStr), s, nameOperatorConflitMap.get(s), -1, true));
                } else {
                    matchedSubMapAndReplacedString.put(MatchedStr, ValidationStringUtils.replace(MatchedStr, s, nameOperatorConflitMap.get(s), -1, true));
                }
            }
        }

        for (Map.Entry<String, String> entry : matchedSubMapAndReplacedString.entrySet()) {
            inputStr = ValidationStringUtils.replace(inputStr, entry.getKey(), entry.getValue(), -1, false);
        }
        String matchedForDecimaArray = "\\[-?\\d+(?:\\.\\d+)?(?:,\\s*-?\\d+(?:\\.\\d+)?)*\\]";
    	Pattern regExDecima;
        Matcher matcherDecima;
        regExDecima = Pattern.compile(matchedForDecimaArray);
        matcherDecima = regExDecima.matcher(inputStr);
        String MatchedStrDecima;
        Map<String, String> matchedDecimaMap = new HashMap<String, String>();
        while (matcherDecima.find()) {
        	MatchedStrDecima = matcherDecima.group();
            matchedDecimaMap.put(MatchedStrDecima, ValidationStringUtils.replace(MatchedStrDecima, ",", "~###COMMA###~", -1, true));
        }
        for (Map.Entry<String, String> entry : matchedDecimaMap.entrySet()) {
            inputStr = ValidationStringUtils.replace(inputStr, entry.getKey(), entry.getValue(), -1, false);
        }
        List<Token> tokenReplacedWithOriginalChars = expressionTokenizer.tokenize(new CharacterStream(inputStr));
        regEx = Pattern.compile("###(\\d+)###");

        for (Token t : tokenReplacedWithOriginalChars) {
            if (t.getType().equals(TokenType.VARIABLE)) {
                matcher = regEx.matcher(t.getValue());
                while (matcher.find()) {
                    MatchedStr = matcher.group();
                    t.setValue(ValidationStringUtils.replace(t.getValue(), MatchedStr, (String.valueOf(Character.toChars(Integer.parseInt(MatchedStr.replace("###", ""))))), -1, true));
                    //matchedSubMapAndReplacedString.put(MatchedStr, ""+Character.toChars(Integer.parseInt(MatchedStr.replace("###", ""))));
                }
                if(t.getValue().contains("~###COMMA###~")) {
                	t.setValue(ValidationStringUtils.replace(t.getValue(), "~###COMMA###~", ",", -1, true));
                }
            }
        }
        return prefixParser.parse(tokenReplacedWithOriginalChars);
    }

    public void processForeachNode(AstNode an, QueryBuilder builder, ValidationFunctions functions,
                                   List<DynamicFilter> filters) {
        List<AstNode> children = an.getChildren();
        String tableName = children.get(0).getToken().getType() == TokenType.VARIABLE
                ? children.get(0).getToken().getValue()
                : "";
        List<String> groupByCondition = new ArrayList<String>();
        int groupByEndPos = 1;
        String parsedStr = null;
        List<String> filterCondition = new ArrayList<String>();

        if (children.size() > 1 && children.get(1).getToken().getType() == TokenType.VARIABLE) {
            // processing groupBy clause
            for (int i = groupByEndPos; i < children.size(); i++) {
                if ((children.get(i).getToken().getValue().contains("[")
                        && children.get(i).getToken().getValue().contains("]"))
                        || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    parsedStr = (children.get(i) instanceof VariableNode
                            ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                            : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                            ""));

                    if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                        groupByCondition.add(parsedStr);
                    }
                    groupByEndPos = i;
                    break;
                } else if (children.get(i).getToken().getValue().contains("[")) {
                    parsedStr = (children.get(i) instanceof VariableNode
                            ? children.get(i).getToken().getValue().replace("[", "")
                            : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", ""));
                    if (!parsedStr.equalsIgnoreCase("")) {
                        groupByCondition.add(parsedStr);
                    }
                } else if (children.get(i).getToken().getValue().contains("]")) {
                    parsedStr = (children.get(i) instanceof VariableNode
                            ? children.get(i).getToken().getValue().replace("]", "")
                            : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", ""));
                    if (!parsedStr.equalsIgnoreCase("")) {
                        groupByCondition.add(parsedStr);
                    }
                    groupByEndPos = i;
                    break;
                } else {
                    parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                            : children.get(i).getExpressionDetails().getExpressionOutput());
                    if (!parsedStr.equalsIgnoreCase("")) {
                        groupByCondition.add(parsedStr);
                    }
                }
            }

        }

        // processing filter clause
        StringBuilder filterClause = new StringBuilder();
        for (int i = (groupByEndPos + 1); i < children.size() - 1; i++) {
            if (children.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                filterClause.append(children.get(i).getToken().getValue() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && children.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append("("
                        + ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(),
                        "", builder, functions, null)[2]).getExpressionDetails().getExpression()
                        + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(children.get(i).getToken().getValue());
            } else {
                filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(children.get(i).getToken().getValue() + ",");
            }
        }

        if (!filterClause.toString().trim().equals("")) {
            filterClause = (filterClause.charAt(0) != '[')
                    ? processesFilterCondition(filterClause.substring(0, filterClause.length() - 1))
                    : processesFilterCondition(filterClause.substring(1, filterClause.length() - 2));
        }

        if (filterClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(filterClause.toString().split(ValidationConstants.FILTER_SEPARATER)));
        }
        filterCondition = replaceNotInInFilter(filterCondition);

        List<String> dynamicFilterCondition = new ArrayList<>();
        addDynamicFilters(filters, tableName, dynamicFilterCondition, builder);
        dynamicFilterCondition = replaceNotInInFilter(dynamicFilterCondition);

        functions.forEachBuilder(tableName, groupByCondition, filterCondition, builder, dynamicFilterCondition);
    }

    public Object[] processExpressionNode(AstNode an, Map<String, String> variablesAndExpressionMap,
                                          String replacedExpr, QueryBuilder builder, ValidationFunctions functions,
                                          List<DynamicFilter> filters) {
        List<AstNode> nodes = new ArrayList<AstNode>();
        List<AstNode> childNodes = an.getChildren();
        String hexVal = "";
        String exp = "";
        Object[] res = new Object[3];
        res[0] = variablesAndExpressionMap;
        res[1] = replacedExpr;
        res[2] = an;

        for (AstNode a : childNodes) {
            if (a instanceof FunctionNode
                    && (null == a.getExpressionDetails() || (!a.getExpressionDetails().getExpressionProcessed()))) {
                nodes.add(a);
            }
        }

        if (nodes.isEmpty()) {
            an.setExpressionDetails(formExpressionResultDetail(an, builder));
            hexVal = an.getExpressionDetails().getExpressionOutput();
            exp = an.getExpressionDetails().getExpression();
            replacedExpr = ValidationStringUtils.replace(replacedExpr, exp, hexVal, -1, true);
            variablesAndExpressionMap.put(hexVal, exp);
            res[1] = replacedExpr;

            if (an.getToken().getType().equals(TokenType.FUNCTION)) {
                List<AstNode> children = an.getChildren();

                switch (an.getToken().getValue().toUpperCase()) {
                    case "YEARIN":
                    case "OFFSET":
                        String replacedDate = "YEARINIMPL" + an.getChildren().get(0).getToken().getValue();
                        an.getExpressionDetails().setExpression(replacedDate);
                        break;
                    case "SUMIF":
                    case "MAXIF":
                    case "MINIF":
                    case "SUM":
                    case "MAX":
                    case "MIN":
                    case "AVG":
                        an.getExpressionDetails()
                                .setExpression(resolveAggregateFunction(an, hexVal, builder, functions, filters));
                        builder.setExpression(an.getExpressionDetails().getExpression());
                        break;
                    case "VLOOKUP":
                        an.getExpressionDetails().setExpression(resolveLookup(children, hexVal, builder, functions, filters));
                        builder.setExpression(an.getExpressionDetails().getExpression());
                        break;
                    case "DCOUNT":
                    case "UNIQUE":
                    case "COUNT":
                    case "COUNTIF":
                        an.getExpressionDetails().setExpression(resolveCountFunction(an, hexVal, builder, functions, filters));
                        builder.setExpression(an.getExpressionDetails().getExpression());
                        break;
                    default:
                        an.getExpressionDetails().setExpressionOutput(exp);
                        break;
                }
            }

            return res;
        } else {
            Map<AstNode, Integer> functionExpAndIndex = new HashMap<AstNode, Integer>();
            AstNode currentProccessedNode;
            for (AstNode a : nodes) {
                Object[] temp = processExpressionNode(a, variablesAndExpressionMap, replacedExpr, builder,
                        functions, filters);
                replacedExpr = (String) temp[1];
                currentProccessedNode = (AstNode) temp[2];
                functionExpAndIndex.put(currentProccessedNode, an.getChildren().indexOf(a));
            }

            functionExpAndIndex.forEach((key, value) -> {
                an.getChildren().remove(an.getChildren().get(value));
                an.getChildren().add(value, key);
            });

            return processExpressionNode(an, variablesAndExpressionMap, replacedExpr, builder, functions, filters);
        }
    }

    private String resolveCountFunction(AstNode an, String hexVal, QueryBuilder builder,
                                        ValidationFunctions functions, List<DynamicFilter> filters) {
        List<AstNode> children = an.getChildren();
        String functionName = an.getToken().getValue().replace("IF", "");
        String tableName = "";
        int indexOfOptionalParams = 0;

        if (!children.get(0).getToken().getValue().equals(ValidationConstants.EMPTY_PARAMETER)) {
            tableName = children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? children.get(0).getToken().getValue().split("\\.")[0]
                    : "";

            if (children.size() > 1 && children.get(1).getToken().getType() == TokenType.FUNCTION) {
                indexOfOptionalParams = 2;
            } else {
                indexOfOptionalParams = 1;
            }
        }

        List<String> outputColumns = new ArrayList<String>();
        List<String> groupByColumns = new ArrayList<>();
        List<String> filterCondition = new ArrayList<String>();
        String parsedStr = null;

        // processing output columns
        for (int i = indexOfOptionalParams; i < children.size(); i++) {
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "")).trim();
                if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            } else if (children.get(i).getToken().getValue().contains("]")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            }
        }

        // processing groupby columns
        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                String value = children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "");
                if (!value.equals(ValidationConstants.EMPTY_PARAMETER)) {
                    groupByColumns.add(value);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                groupByColumns.add(children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", ""));

            } else if (children.get(i).getToken().getValue().contains("]")) {
                groupByColumns.add(children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", ""));
                indexOfOptionalParams = i;
                break;
            } else {
                groupByColumns.add(children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput());
            }
        }

        // processing filter conditions
        StringBuilder whereClause = new StringBuilder();

        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if (children.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                whereClause.append(children.get(i).getToken().getValue() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && children.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        "(" + ((AstNode) this.processExpressionNode(children.get(i), new HashMap<String, String>(),
                                "", builder, functions, null)[2]).getExpressionDetails().getExpression() + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue());
            } else {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue() + ",");
            }
        }

        if (!whereClause.toString().trim().equals("")) {
            String tempWhere = whereClause.toString().trim();
            whereClause = (tempWhere.charAt(0) != '[')
                    ? processesFilterCondition(tempWhere.substring(0, tempWhere.length() - 1))
                    : processesFilterCondition(tempWhere.substring(1, tempWhere.length() - 2));
        }

        if (whereClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(whereClause.toString().trim().split(ValidationConstants.FILTER_SEPARATER)));
        }

        addDynamicFilters(filters, tableName, filterCondition, builder);
        filterCondition = replaceNotInInFilter(filterCondition);

        return functions.countFunctionBuilder(tableName, outputColumns, groupByColumns, filterCondition, builder, hexVal,
                expressionToColumnsMap.get(expressionId), functionName);
    }


    private String resolveLookup(List<AstNode> children, String hexVal, QueryBuilder builder,
                                 ValidationFunctions functions, List<DynamicFilter> filters) {
        String parsedStr = null;
        String sourceTable = "";
        String targetTable = "";
        int targetIndex = 0;
        int joinIndex = 0;
        List<String> joinCondition = new ArrayList<String>();
        List<String> outputColumns = new ArrayList<String>();
        List<String> filterCondition = new ArrayList<String>();

        if (!children.get(0).getToken().getValue().equals(ValidationConstants.EMPTY_PARAMETER)) {
            sourceTable = children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? children.get(0).getToken().getValue()
                    : children.get(0).getExpressionDetails().getExpressionOutput();
        }

        if (children.get(1).getToken().getType().equals(TokenType.FUNCTION)) {
            targetTable = children.get(2).getToken().getValue();
            targetIndex = 2;
        } else {
            targetTable = children.get(1).getToken().getValue();
            targetIndex = 1;
        }

        if (children.get(targetIndex + 1).getToken().getType().equals(TokenType.FUNCTION)) {
            joinIndex = targetIndex + 2;
        } else {
            joinIndex = targetIndex + 1;
        }

        int indexOfOptionalParams = 2;

        // processing join conditions
        StringBuilder joinClause = new StringBuilder();
        for (int i = joinIndex; i < children.size(); i++) {
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "")).trim();
                if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    joinClause.append(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    joinClause.append(parsedStr);
                }
            } else if (children.get(i).getToken().getValue().contains("]")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    joinClause.append(parsedStr);
                }
                if (joinClause.charAt(joinClause.length() - 1) == ',') {
                    joinClause.deleteCharAt(joinClause.length() - 1);
                }
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    if (children.get(i) instanceof VariableNode && children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                        if (joinClause.charAt(joinClause.length() - 1) == ',') {
                            joinClause.deleteCharAt(joinClause.length() - 1).append(parsedStr);
                        } else {
                            joinClause.append(parsedStr);
                        }
                    } else {
                        joinClause.append(parsedStr).append(",");
                    }

                }
            }
        }

        if (!joinClause.toString().trim().equals("")) {
            String tempWhere = joinClause.toString().trim();
            joinClause = (tempWhere.charAt(0) != '[') ? processesFilterCondition(tempWhere)
                    : processesFilterCondition(tempWhere.substring(1, tempWhere.length() - 2));
        }

        if (joinClause.toString().trim().length() > 0) {
            joinCondition.addAll(Arrays.asList(joinClause.toString().trim().split(ValidationConstants.FILTER_SEPARATER)));
        }

        // processing output columns
        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {

            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "")).trim();
                if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            } else if (children.get(i).getToken().getValue().contains("]")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            }
        }

        // processing filter conditions
        StringBuilder whereClause = new StringBuilder();

        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if (children.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                whereClause.append(children.get(i).getToken().getValue() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && children.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        "(" + ((AstNode) this.processExpressionNode(children.get(i), new HashMap<String, String>(),
                                "", builder, functions, null)[2]).getExpressionDetails().getExpression() + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue());
            } else {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue() + ",");
            }
        }

        if (!whereClause.toString().trim().equals("")) {
            String tempWhere = whereClause.toString().trim();
            whereClause = (tempWhere.charAt(0) != '[')
                    ? processesFilterCondition(tempWhere.substring(0, tempWhere.length() - 1))
                    : processesFilterCondition(tempWhere.substring(1, tempWhere.length() - 2));
        }

        if (whereClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(whereClause.toString().trim().split(ValidationConstants.FILTER_SEPARATER)));
        }

        addDynamicFilters(filters, sourceTable, filterCondition, builder);
        addDynamicFilters(filters, targetTable, filterCondition, builder);
        filterCondition = replaceNotInInFilter(filterCondition);

        return functions.lookUpBuilder(sourceTable, targetTable, joinCondition, outputColumns,
                filterCondition, builder, hexVal, expressionToColumnsMap.get(expressionId));
    }

    private String resolveAggregateFunction(AstNode an, String hexVal, QueryBuilder builder,
                                            ValidationFunctions functions, List<DynamicFilter> filters) {
        List<AstNode> chilNodes = an.getChildren();
        String tableName = null;
        String columnName = null;
        int groupByEndPos = 0;
        if (!chilNodes.get(0).getToken().getValue().equals(ValidationConstants.EMPTY_PARAMETER)) {
            tableName = chilNodes.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? chilNodes.get(0).getToken().getValue().split("\\.")[0]
                    : "";
            columnName = chilNodes.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? chilNodes.get(0).getToken().getValue()
                    : "";

            if (chilNodes.size() > 1 && chilNodes.get(1).getToken().getType() == TokenType.FUNCTION) {
                groupByEndPos = 2;
            } else {
                groupByEndPos = 1;
            }
        }

        String aggregateName = an.getToken().getValue().replace("IF", "");

        // processing groupBy clause
        List<String> groupByColumns = new ArrayList<>();
        for (int i = groupByEndPos; i < chilNodes.size(); i++) {
            if ((chilNodes.get(i).getToken().getValue().contains("[")
                    && chilNodes.get(i).getToken().getValue().contains("]"))
                    || chilNodes.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                String value = chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "");
                if (!value.equals(ValidationConstants.EMPTY_PARAMETER)) {
                    groupByColumns.add(value);
                }
                groupByEndPos = i;
                break;
            } else if (chilNodes.get(i).getToken().getValue().contains("[")) {
                groupByColumns.add(chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue().replace("[", "")
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput().replace("[", ""));

            } else if (chilNodes.get(i).getToken().getValue().contains("]")) {
                groupByColumns.add(chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue().replace("]", "")
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput().replace("]", ""));
                groupByEndPos = i;
                break;
            } else {
                groupByColumns.add(chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue()
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput());
            }
        }

        // processing filter conditions
        List<String> filterCondition = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder();

        for (int i = (groupByEndPos + 1); i < chilNodes.size(); i++) {
            if (chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                whereClause.append(chilNodes.get(i).getToken().getValue() + ",");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && chilNodes.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append("("
                        + ((AstNode) processExpressionNode(chilNodes.get(i), new HashMap<String, String>(),
                        "", builder, functions, null)[2]).getExpressionDetails().getExpression()
                        + "),");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(chilNodes.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(chilNodes.get(i).getToken().getValue());
            } else {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(chilNodes.get(i).getToken().getValue() + ",");
            }
        }

        if (!whereClause.toString().trim().equals("")) {
            whereClause = (whereClause.charAt(0) != '[')
                    ? processesFilterCondition(whereClause.substring(0, whereClause.length() - 1))
                    : processesFilterCondition(whereClause.substring(1, whereClause.length() - 2));
        }

        if (whereClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(whereClause.toString().split(ValidationConstants.FILTER_SEPARATER)));
        }

        addDynamicFilters(filters, tableName, filterCondition, builder);
        filterCondition = replaceNotInInFilter(filterCondition);

        return (functions.aggregateBuilder(tableName, groupByColumns, filterCondition, columnName, hexVal, builder,
                aggregateName, expressionToColumnsMap.get(expressionId)));
    }

    private ExpressionResultDetail formExpressionResultDetail(AstNode n, QueryBuilder builder) {
        ExpressionResultDetail ex = new ExpressionResultDetail();
        ex.setExpressionProcessed(true);

        List<AstNode> childNodes = n.getChildren();
        String functionName = n.getToken().getValue();

        // since foreach syntax is different than other functions, processing foreach this way.
        if (functionName.equalsIgnoreCase("foreach")) {
            String expression = "";
            AstNode cn = childNodes.get(childNodes.size() - 1);
            {
                if (cn instanceof FunctionNode) {
                    // getting function output
                    if (!AGG_VLOOKUP_FUNCTIONS.contains(cn.getToken().getValue())) {
                        if (cn.getToken().getValue().equalsIgnoreCase("PRIORITYBRACKETS")) {
                            if ((!expression.trim().equalsIgnoreCase(""))
                                    && expression.charAt(expression.length() - 1) == ',') {
                                expression = expression.substring(0, expression.length() - 1) + "("
                                        + cn.getExpressionDetails().getExpression() + "),";
                            } else {
                                expression = expression + "(" + cn.getExpressionDetails().getExpression() + "),";
                            }

                        } else {
                            expression = expression + cn.getExpressionDetails().getExpression() + ",";
                        }
                    } else {
                        if (cn.getToken().getValue().equalsIgnoreCase("PRIORITYBRACKETS")) {
                            if ((!expression.trim().equalsIgnoreCase(""))
                                    && expression.charAt(expression.length() - 1) == ',') {
                                if (Arrays.asList("AND", "OR", "#AND", "#OR").contains(n.getToken().getValue())) {
                                    expression = expression + "("
                                            + cn.getExpressionDetails().getExpressionOutput() + "),";
                                } else {
                                    expression = expression.substring(0, expression.length() - 1) + "("
                                            + cn.getExpressionDetails().getExpressionOutput() + "),";
                                }
                            } else {
                                expression = expression + "(" + cn.getExpressionDetails().getExpressionOutput() + "),";
                            }

                        } else {
                            expression = expression + cn.getExpressionDetails().getExpressionOutput() + ",";
                        }
                    }

                } else if (cn instanceof VariableNode) {
                    // resolve the variable here
                    if (cn.getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                        expression = (!expression.trim().equalsIgnoreCase("")
                                && expression.charAt(expression.length() - 1) == ',')
                                ? expression.substring(0, expression.length() - 1) + cn.getToken().getValue()
                                : expression + cn.getToken().getValue();
                    } else {
                        expression = expression + cn.getToken().getValue() + ",";
                    }

                } else {
                    expression = expression + cn.getToken().getValue() + ",";
                }
            }
            expression = expression.substring(0, expression.length() - 1);
            builder.setExpression(expression);
            //    System.out.println("~~~" + expression + "~~~");
            ex.setExpression(expression);
            ex.setExpressionOutput(getHashValueForExp());
        } else if (functionName.equalsIgnoreCase("prioritybrackets")) {
            String expression = "";
            for (AstNode cn : childNodes) {
                if (cn instanceof FunctionNode) {
                    // getting function output
                    if (cn.getToken().getValue().equalsIgnoreCase("PRIORITYBRACKETS")) {
                        if ((!expression.trim().equalsIgnoreCase(""))
                                && expression.charAt(expression.length() - 1) == ',') {
                            if (Arrays.asList("AND", "OR", "#AND", "#OR").contains(n.getToken().getValue())) {
                                expression = expression + "("
                                        + cn.getExpressionDetails().getExpressionOutput() + "),";
                            } else {
                                expression = expression.substring(0, expression.length() - 1) + "("
                                        + cn.getExpressionDetails().getExpressionOutput() + "),";
                            }

                        } else {
                            expression = expression + "(" + cn.getExpressionDetails().getExpressionOutput() + "),";
                        }

                    } else {
                        expression = expression + cn.getExpressionDetails().getExpressionOutput() + ",";
                    }

                } else if (cn instanceof VariableNode) {
                    // resolve the variable here
                    if (cn.getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                        expression = (!expression.trim().equalsIgnoreCase("")
                                && expression.charAt(expression.length() - 1) == ',')
                                ? expression.substring(0, expression.length() - 1) + cn.getToken().getValue()
                                : expression + cn.getToken().getValue();
                    } else {
                        expression = expression + cn.getToken().getValue() + ",";
                    }

                } else {
                    expression = expression + cn.getToken().getValue() + ",";
                }
            }
            expression = expression.length() > 0 ? expression.substring(0, expression.length() - 1) : "";
            builder.setExpression(expression);
            //    System.out.println("~~~" + expression + "~~~");
            ex.setExpression(expression);
            ex.setExpressionOutput(expression);
        } else if (n.getToken().getType().equals(TokenType.FUNCTION) && (functionName.equals("IN")
                || functionName.equals("NOTIN") || functionName.equals("BETWEEN")
                || functionName.equals("CONTAINS") || functionName.equals("BEGINSWITH")
                || functionName.equals("ENDSWITH"))) {

            String expression = "{";
            int lastIndex = childNodes.size() - 1;
            int counter = 0;
            AstNode actualValToCompare = childNodes.get(lastIndex);

            for (AstNode cn : childNodes) {
                if (lastIndex > counter) {

                    counter++;
                    if (cn instanceof FunctionNode) {
                        // getting function output
                        if (cn.getToken().getValue().equalsIgnoreCase("PRIORITYBRACKETS")) {
                            if (!expression.trim().equalsIgnoreCase("")
                                    && expression.charAt(expression.length() - 1) == ',') {
                                if (Arrays.asList("AND", "OR", "#AND", "#OR").contains(n.getToken().getValue())) {
                                    expression = expression + "("
                                            + cn.getExpressionDetails().getExpressionOutput() + "),";
                                } else {
                                    expression = expression.substring(0, expression.length() - 1) + "("
                                            + cn.getExpressionDetails().getExpressionOutput() + "),";
                                }
                            } else {
                                expression = expression + "(" + cn.getExpressionDetails().getExpressionOutput() + "),";
                            }

                        } else {
                            expression = expression + cn.getExpressionDetails().getExpressionOutput() + ",";
                        }

                    } else if (cn instanceof VariableNode) {
                        // resolve the variable here
                        if (cn.getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                            expression = (!expression.trim().equalsIgnoreCase(""))
                                    && expression.charAt(expression.length() - 1) == ','
                                    ? expression.substring(0, expression.length() - 1) + cn.getToken().getValue()
                                    : expression + cn.getToken().getValue();
                        } else {
                            expression = expression + cn.getToken().getValue() + ",";
                        }

                    } else {
                        expression = expression + cn.getToken().getValue() + ",";
                    }

                }
            }
            String actualValCalculated = actualValToCompare instanceof FunctionNode ? actualValToCompare.getExpressionDetails().getExpressionOutput() : actualValToCompare.getToken().getValue();
            expression = ValidationStringUtils.replace("#" + functionName + "(" + actualValCalculated + "," + expression.substring(1, expression.length() - 1) + ")", "\"", "'", -1, true);

            builder.setExpression(expression);
            //System.out.println("~~~" + expression + "~~~");
            ex.setExpression(expression);
            ex.setExpressionOutput(getHashValueForExp());
        } else if (n.getToken().getType().equals(TokenType.FUNCTION)) {
            String expression = functionName + "(";
            for (AstNode cn : childNodes) {
                if (cn instanceof FunctionNode) {
                    // getting function output
                    if (cn.getToken().getValue().equalsIgnoreCase("PRIORITYBRACKETS")) {
                        if (!expression.trim().equalsIgnoreCase("")
                                && expression.charAt(expression.length() - 1) == ',') {
                            if (Arrays.asList("AND", "OR", "#AND", "#OR").contains(n.getToken().getValue())) {
                                expression = expression + "("
                                        + cn.getExpressionDetails().getExpressionOutput() + "),";
                            } else {
                                expression = expression.substring(0, expression.length() - 1) + "("
                                        + cn.getExpressionDetails().getExpressionOutput() + "),";
                            }
                        } else {
                            expression = expression + "(" + cn.getExpressionDetails().getExpressionOutput() + "),";
                        }

                    } else {
                        expression = expression + cn.getExpressionDetails().getExpressionOutput() + ",";
                    }

                } else if (cn instanceof VariableNode) {
                    // resolve the variable here
                    if (cn.getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                        expression = (!expression.trim().equalsIgnoreCase("")
                                && expression.charAt(expression.length() - 1) == ',')
                                ? expression.substring(0, expression.length() - 1) + cn.getToken().getValue()
                                : expression + cn.getToken().getValue();
                    } else {
                        expression = expression + cn.getToken().getValue() + ",";
                    }

                } else {
                    expression = expression + cn.getToken().getValue() + ",";
                }
            }
            expression = expression.substring(0, expression.length() - 1) + ")";
            builder.setExpression(expression);
            //    System.out.println("~~~" + expression + "~~~");
            ex.setExpression(expression);
            ex.setExpressionOutput(getHashValueForExp());
        }

        return ex;
    }

    private String getHashValueForExp() {
        return "DV_" + new BigInteger(64, new Random());
    }

    private StringBuilder extractColNameFromFilter(String string) {
        CharacterStream cs = new CharacterStream(string);
        StringBuilder finalParam = new StringBuilder();
        int nameWithSpace = 0;
        while (cs.hasNext()) {
            char token = cs.peek();
            if (Character.isAlphabetic(token)) {
                finalParam.append(token);
            } else if (Character.isDigit(token)) {
                finalParam.append(token);
            } else if (token == '-' || token == '_') {
                finalParam.append(token);
            } else if (token == '"' && nameWithSpace == 0) {
                nameWithSpace = 1;
                //   cs.next(); // ignore token
            } else if (token == '"' && nameWithSpace == 1) {
                //    nameWithSpace = 2;
                break; // ignore token
            } else if (nameWithSpace == 1 && token == ' ') {
                finalParam.append(token);
            } else {
                break;
            }
            cs.next();
        }
        return finalParam;
    }

    private Object[] processExpressionNodeForSpelExpressions(AstNode an, Map<String, String> variablesAndExpressionMap,
                                                             String replacedExpr, QueryBuilder builder) {

        List<AstNode> nodes = new ArrayList<>();
        List<AstNode> childNodes = an.getChildren();
        String hexVal = "";
        String exp = "";
        Object[] res = new Object[3];
        res[0] = variablesAndExpressionMap;
        res[1] = replacedExpr;
        res[2] = an;

        for (AstNode a : childNodes) {
            if (a instanceof FunctionNode
                    && (null == a.getExpressionDetails() || (!a.getExpressionDetails().getExpressionProcessed()))) {
                nodes.add(a);
            }
        }

        if (nodes.isEmpty()) {
            if (an.getToken().getType().equals(TokenType.FUNCTION)) {

                switch (an.getToken().getValue().toUpperCase()) {
                    case "LEN":
                    case "UPPER":
                    case "ISNOTEMPTY":
                    case "ISEMPTY":
                    case "LOWER":
                    case "SUBSTR":
                    case "AND":
                    case "OR":
                    case "ABS":
                    case "CONCAT":
                    case "ROUND":
                    case "CONVERT":
                    case "REGEX":
                    case "TODATE":
                    case "SOM":
                    case "EOM":
                    case "SOY":
                    case "EOY":
                    case "SOFY":
                    case "EOFY":
                    case "DATEPART":
                    case "DATEDIFF":
                        an.getToken().setValue("#" + an.getToken().getValue());
                        break;
                    default:
                        break;
                }
            }

            an.setExpressionDetails(formExpressionResultDetail(an, builder));
            hexVal = an.getExpressionDetails().getExpressionOutput();
            exp = an.getExpressionDetails().getExpression();
            an.getExpressionDetails().setExpressionOutput(exp);
            replacedExpr = ValidationStringUtils.replace(replacedExpr, exp, hexVal, -1, true);
            variablesAndExpressionMap.put(hexVal, exp);
            res[1] = replacedExpr;

            return res;
        } else {
            Map<AstNode, Integer> functionExpAndIndex = new HashMap<AstNode, Integer>();
            AstNode currentProccessedNode;
            for (AstNode a : nodes) {
                Object[] temp = processExpressionNodeForSpelExpressions(a, variablesAndExpressionMap, replacedExpr, builder);
                replacedExpr = (String) temp[1];
                currentProccessedNode = (AstNode) temp[2];
                functionExpAndIndex.put(currentProccessedNode, an.getChildren().indexOf(a));
            }

            functionExpAndIndex.forEach((key, value) -> {
                an.getChildren().remove(an.getChildren().get(value));
                an.getChildren().add(value, key);
            });

            return processExpressionNodeForSpelExpressions(an, variablesAndExpressionMap, replacedExpr, builder);
        }
    }

    public String convertIntoSpELExpression(String expression) {
        QueryBuilder builder = new QueryBuilder();
        ValidationFunctions functions = new ValidationFunctions();
        final ExpressionTokenizerForSpELFunctions expressionTokenizer = new ExpressionTokenizerForSpELFunctions();
        final ExpressionParserForSpELFunctions prefixParser = new ExpressionParserForSpELFunctions();
        //changing if then else to spel equivalent.

        expression = expression.replaceAll("(?i)IF\\s*,?\\s*\\(", "\\(").replaceAll(",?(?i)THEN\\s*,?\\s*\\(", "\\?\\(").replaceAll(",?(?i)ELSE\\s*,?\\s*\\(", "\\:\\(");
        String inputStr = expression;

        inputStr = inputStr.replaceAll(",\\s*?,", "," + ValidationConstants.EMPTY_PARAMETER + ",")
                .replaceAll("\\(\\s*?,", "(" + ValidationConstants.EMPTY_PARAMETER + ",");

        String getStringBetweenQuotes = "([\"'])(?:(?=(\\\\?))\\2.)*?\\1";
        Pattern regEx;
        Matcher matcher;
        regEx = Pattern.compile(getStringBetweenQuotes);
        matcher = regEx.matcher(inputStr);
        String MatchedStr;
        Map<String, String> matchedSubMapAndReplacedString = new HashMap<>();
        while (matcher.find()) {
            MatchedStr = matcher.group();
            for (String s : nameOperatorConflitMap.keySet()) {
                if (matchedSubMapAndReplacedString.containsKey(MatchedStr)) {
                    matchedSubMapAndReplacedString.put(MatchedStr, ValidationStringUtils.replace(matchedSubMapAndReplacedString.get(MatchedStr), s, nameOperatorConflitMap.get(s), -1, true));
                } else {
                    matchedSubMapAndReplacedString.put(MatchedStr, ValidationStringUtils.replace(MatchedStr, s, nameOperatorConflitMap.get(s), -1, true));
                }
            }
        }

        for (Map.Entry<String, String> entry : matchedSubMapAndReplacedString.entrySet()) {
            inputStr = ValidationStringUtils.replace(inputStr, entry.getKey(), entry.getValue(), -1, false);
        }
        String matchedForDecimaArray = "\\[-?\\d+(?:\\.\\d+)?(?:,\\s*-?\\d+(?:\\.\\d+)?)*\\]";
    	Pattern regExDecima;
        Matcher matcherDecima;
        regExDecima = Pattern.compile(matchedForDecimaArray);
        matcherDecima = regExDecima.matcher(inputStr);
        String MatchedStrDecima;
        Map<String, String> matchedDecimaMap = new HashMap<String, String>();
        while (matcherDecima.find()) {
        	MatchedStrDecima = matcherDecima.group();
            matchedDecimaMap.put(MatchedStrDecima, ValidationStringUtils.replace(MatchedStrDecima, ",", "~###COMMA###~", -1, true));
        }
        for (Map.Entry<String, String> entry : matchedDecimaMap.entrySet()) {
            inputStr = ValidationStringUtils.replace(inputStr, entry.getKey(), entry.getValue(), -1, false);
        }
        List<Token> tokenReplacedWithOriginalChars = expressionTokenizer.tokenize(new CharacterStream(inputStr));
        regEx = Pattern.compile("###(\\d+)###");

        for (Token t : tokenReplacedWithOriginalChars) {
            if (t.getType().equals(TokenType.VARIABLE)) {
                matcher = regEx.matcher(t.getValue());
                while (matcher.find()) {
                    MatchedStr = matcher.group();
                    t.setValue(ValidationStringUtils.replace(t.getValue(), MatchedStr, (String.valueOf(Character.toChars(Integer.parseInt(MatchedStr.replace("###", ""))))), -1, true));
                }
                if(t.getValue().contains("~###COMMA###~")) {
                	t.setValue(ValidationStringUtils.replace(t.getValue(), "~###COMMA###~", ",", -1, true));
                }
            }
        }

        AstNode expAn = prefixParser.parse(tokenReplacedWithOriginalChars);
        if (expAn.getToken().getType().equals(TokenType.FUNCTION)
                && expAn.getToken().getValue().equalsIgnoreCase("FOREACH")) {
            processForeachNode(expAn, builder, functions, null);
        }

        processExpressionNodeForSpelExpressions(expAn, new HashMap<String, String>(), expression, builder);

        return builder.getExpression();
    }

    private void processForEachForExpressionMap(AstNode an, QueryBuilder builder, ValidationFunctions functions,
                                                Integer validationId) {

        List<AstNode> children = an.getChildren();
        String vLookupSrc = children.get(0).getToken().getType() == TokenType.VARIABLE
                ? children.get(0).getToken().getValue()
                : "";
        String forEachSrcExp = formExpressionEntityDetailMap(vLookupSrc,
                (children.get(1).getToken().getType() == TokenType.FUNCTION && children.size() > 2) ? children.get(1)
                        : null,
                validationId, false, true);

        if (vLookupSrc.toUpperCase().startsWith(ValidationConstants.TYPE_RETURN)) {
            // only process for type return.
            String[] arrayOfParams = vLookupSrc.split("\\.");
            String returnName = arrayOfParams[1];
            String sectionName = arrayOfParams[2];
            meMap.put(ValidationConstants.ME, ValidationConstants.TYPE_RETURN + "." + returnName + "." + sectionName);
        } else if (vLookupSrc.toUpperCase().startsWith(ValidationConstants.TYPE_REFTABLE)) {
            String[] arrayOfParams = vLookupSrc.split("\\.");
            String tableName = arrayOfParams[1];
            meMap.put(ValidationConstants.ME, ValidationConstants.TYPE_REFTABLE + "." + tableName);
        } else if (vLookupSrc.toUpperCase().startsWith(ValidationConstants.TYPE_ENTITY)) {
            String[] arrayOfParams = vLookupSrc.split("\\.");
            String subjectArea = arrayOfParams[1];
            String tableName = arrayOfParams[2];
            meMap.put(ValidationConstants.ME, ValidationConstants.TYPE_ENTITY + "." + subjectArea + "." + tableName);
        }

        List<String> groupByCondition = new ArrayList<String>();
        int groupByEndPos = (children.get(1).getToken().getType() == TokenType.FUNCTION && children.size() > 2) ? 2
                : 1;
        String parsedStr = null;
        List<String> filterCondition = new ArrayList<String>();

        if (children.size() > groupByEndPos && children.get(groupByEndPos).getToken().getType() == TokenType.VARIABLE) {
            // processing groupBy clause
            for (int i = groupByEndPos; i < children.size(); i++) {
                if ((children.get(i).getToken().getValue().contains("[")
                        && children.get(i).getToken().getValue().contains("]"))
                        || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    parsedStr = (children.get(i) instanceof VariableNode
                            ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                            : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                            ""));

                    if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                        groupByCondition.add(parsedStr);
                    }
                    groupByEndPos = i;
                    break;
                } else if (children.get(i).getToken().getValue().contains("[")) {
                    parsedStr = (children.get(i) instanceof VariableNode
                            ? children.get(i).getToken().getValue().replace("[", "")
                            : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", ""));
                    if (!parsedStr.equalsIgnoreCase("")) {
                        groupByCondition.add(parsedStr);
                    }
                } else if (children.get(i).getToken().getValue().contains("]")) {
                    parsedStr = (children.get(i) instanceof VariableNode
                            ? children.get(i).getToken().getValue().replace("]", "")
                            : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", ""));
                    if (!parsedStr.equalsIgnoreCase("")) {
                        groupByCondition.add(parsedStr);
                    }
                    groupByEndPos = i;
                    break;
                } else {
                    parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                            : children.get(i).getExpressionDetails().getExpressionOutput());
                    if (!parsedStr.equalsIgnoreCase("")) {
                        groupByCondition.add(parsedStr);
                    }
                }
            }
        }

        for (String grp : groupByCondition) {
            formExpressionEntityDetailMap(grp, null, validationId, true, false);
        }

        // processing filter clause
        StringBuilder filterClause = new StringBuilder();
        for (int i = (groupByEndPos + 1); i < children.size() - 1; i++) {
            if (children.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                filterClause.append(children.get(i).getToken().getValue() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(((AstNode) processExpressionNode(children.get(i),
                        new HashMap<String, String>(), "", builder, functions, null)[2]).getExpressionDetails()
                        .getExpression().replace("PRIORITYBRACKETS", "")
                        + ",");
            } /*else if (children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(((AstNode) processExpressionNode(children.get(i),
                        new HashMap<String, String>(), "", builder, functions)[2]).getExpressionDetails()
                        .getExpression().replace("PRIORITYBRACKETS", ""));
            } */ else {
                filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(children.get(i).getToken().getValue().replace("PRIORITYBRACKETS", "") + ",");
            }
        }

        if (!filterClause.toString().trim().equals("")) {
            filterClause = (filterClause.charAt(0) != '[')
                    ? processesFilterCondition(filterClause.substring(0, filterClause.length() - 1))
                    : processesFilterCondition(filterClause.substring(1, filterClause.length() - 2));
        }

        if (filterClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(filterClause.toString().split(ValidationConstants.FILTER_SEPARATER)));
        }

        ExpressionKey key = new ExpressionKey(forEachSrcExp, groupByCondition, new ArrayList<>(filterCondition));
        populateExpressionGroupMap(key, validationId);

        addDynamicFilters(getSource(vLookupSrc), filterCondition);

        for (String filter : filterCondition) {
            formExpressionEntityDetailMap(filter, null, validationId, true, false);
        }
    }

    private Object[] processNodeForExpressionsMap(AstNode an, Map<String, String> variablesAndExpressionMap,
                                                  String replacedExpr, QueryBuilder builder,
                                                  ValidationFunctions functions, Integer validationId) {
        List<AstNode> nodes = new ArrayList<AstNode>();
        List<AstNode> childNodes = an.getChildren();
        String hexVal = "";
        String exp = "";
        Object[] res = new Object[4];
        res[0] = variablesAndExpressionMap;
        res[1] = replacedExpr;
        res[2] = an;

        for (AstNode a : childNodes) {
            if (a instanceof FunctionNode
                    && (null == a.getExpressionDetails() || (!a.getExpressionDetails().getExpressionProcessed()))) {
                nodes.add(a);
            }
        }

        if (nodes.isEmpty()) {
            an.setExpressionDetails(formExpressionResultDetail(an, builder));
            hexVal = an.getExpressionDetails().getExpressionOutput();
            exp = an.getExpressionDetails().getExpression();
            replacedExpr = ValidationStringUtils.replace(replacedExpr, exp, hexVal, -1, true);
            variablesAndExpressionMap.put(hexVal, exp);
            res[1] = replacedExpr;

            if (an.getToken().getType().equals(TokenType.FUNCTION)) {
                List<AstNode> children = an.getChildren();

                switch (an.getToken().getValue().toUpperCase()) {
                    case "PERIOD":
                        CalendarProcessor cp = CalendarProcessor.getInstance();
                        String calcPeriod = null;

                        try {
                            if (an.getChildren().get(1).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                                String sign = an.getChildren().get(1).getToken().getValue();
                                an.getChildren().remove(1);
                                an.getChildren().get(1).getToken().setValue(sign + an.getChildren().get(1).getToken().getValue());
                            }
                            calcPeriod = cp.getPeriodIdAsString(
                                    ((an.getChildren().size() <= 2)
                                            ? systemPeriodId : Long.parseLong(
                                            (an.getChildren().get(2).getToken().getType().equals(TokenType.VARIABLE) ? an.getChildren().get(2).getToken().getValue() :
                                                    an.getChildren().get(2).getExpressionDetails().getExpressionOutput()))), an.getChildren().get(0).getToken().getValue()
                                    , Integer.parseInt(an.getChildren().get(1).getToken().getValue()), systemSolutionId);
                            an.getExpressionDetails().setExpressionOutput(calcPeriod);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        break;
                    case "YEARIN":
                    case "OFFSET":
                        formExpressionEntityDetailMap(an.getChildren().get(0).getToken().getValue(), null,
                                validationId, false, false);
                        break;
                    case "SUMIF":
                    case "MAXIF":
                    case "MINIF":
                    case "SUM":
                    case "MAX":
                    case "MIN":
                    case "AVG":
                        processAggregateFunctionForExpressionMap(an, builder, functions,
                                validationId);
                        break;
                    case "VLOOKUP":
                        processLookupForExpressionMap(children, builder, functions, validationId);
                        break;
                    case "DCOUNT":
                    case "UNIQUE":
                    case "COUNT":
                    case "COUNTIF":
                        processCountFunctionForExpressionMap(children, builder, functions, validationId);
                        break;
                    default:
                        an.getExpressionDetails().setExpressionOutput(exp);
                        for (AstNode ch : children) {
                            if (ch instanceof VariableNode) {
                                formExpressionEntityDetailMap(ch.getToken().getValue(), null,
                                        validationId, true, false);
                                // todo need to check whether to add metadata in this case or not?
                            }
                        }
                        break;
                }
            }

            return res;
        } else {
            Map<AstNode, Integer> functionExpAndIndex = new HashMap<AstNode, Integer>();
            AstNode currentProccessedNode;
            for (AstNode a : nodes) {
                Object[] temp = processNodeForExpressionsMap(a, variablesAndExpressionMap, replacedExpr,
                        builder, functions, validationId);
                replacedExpr = (String) temp[1];
                currentProccessedNode = (AstNode) temp[2];
                functionExpAndIndex.put(currentProccessedNode, an.getChildren().indexOf(a));
            }

            functionExpAndIndex.forEach((key, value) -> {
                an.getChildren().remove(an.getChildren().get(value));
                an.getChildren().add(value, key);
            });

            return processNodeForExpressionsMap(an, variablesAndExpressionMap, replacedExpr, builder,
                    functions, validationId);
        }
    }

    private void processCountFunctionForExpressionMap(List<AstNode> children, QueryBuilder builder,
                                                      ValidationFunctions functions, Integer validationId) {
        int indexOfOptionalParams = 0;
        String source = "";

        if (!children.get(0).getToken().getValue().equals(ValidationConstants.EMPTY_PARAMETER)) {
            String sourceTable = "";
            sourceTable = children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? children.get(0).getToken().getValue()
                    : children.get(0).getExpressionDetails().getExpressionOutput();
            if (children.get(1).getToken().getType() == TokenType.FUNCTION) {
                formExpressionEntityDetailMap(sourceTable, children.get(1), validationId, false, false);
                indexOfOptionalParams = 2;
            } else {
                formExpressionEntityDetailMap(sourceTable, null, validationId, false, false);
                indexOfOptionalParams = 1;
            }

            source = getSource(sourceTable);
        } else {
            indexOfOptionalParams = 1;
        }

        List<String> outputColumns = new ArrayList<String>();
        List<String> groupByColumns = new ArrayList<>();
        List<String> filterCondition = new ArrayList<String>();
        String parsedStr = null;

        // processing output columns
        String tempColNameForOPcol = "";
        for (int i = indexOfOptionalParams; i < children.size(); i++) {
            if ((!"".equalsIgnoreCase(tempColNameForOPcol)) && children.get(i) instanceof FunctionNode) {
                formExpressionEntityDetailMap(tempColNameForOPcol, children.get(i), validationId, true, false);
                tempColNameForOPcol = "";
            }
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {

                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "")).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "")).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            } else if (children.get(i).getToken().getValue().contains("]")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", "")).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            }
        }

        for (String col : outputColumns) {
            formExpressionEntityDetailMap(col, null, validationId, true, false);
        }

        // processing groupBy columns
        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                String value = children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "");
                if (!value.equals(ValidationConstants.EMPTY_PARAMETER)) {
                    groupByColumns.add(value);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                groupByColumns.add(children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", ""));

            } else if (children.get(i).getToken().getValue().contains("]")) {
                groupByColumns.add(children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", ""));
                indexOfOptionalParams = i;
                break;
            } else {
                groupByColumns.add(children.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput());
            }
        }

        for (String grp : groupByColumns) {
        	String patternString = "\"[\\s]+(?i)AS[\\s]+\"";
        	Pattern pattern = Pattern.compile(patternString);
        	Matcher matcher = pattern.matcher(grp);
        	boolean matches = matcher.find();

        	if(matches) {
        		formExpressionEntityDetailMap(grp.split("[\\s]+(?i)AS[\\s]+")[0], null, validationId, true, false);
        		formExpressionEntityDetailMap(ValidationConstants.ME+"."+grp.split("[\\s]+(?i)AS[\\s]+")[1], null, validationId, true, false);
        	}else {
        		formExpressionEntityDetailMap(grp, null, validationId, true, false);
        	}

        }

        // processing filter conditions
        StringBuilder whereClause = new StringBuilder();

        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if (children.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                whereClause.append(children.get(i).getToken().getValue() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && children.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        "(" + ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(),
                                "", builder, functions, null)[2]).getExpressionDetails().getExpression() + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue());
            } else {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue() + ",");
            }
        }

        if (!whereClause.toString().trim().equals("")) {
            String tempWhere = whereClause.toString().trim();
            whereClause = (tempWhere.charAt(0) != '[')
                    ? processesFilterCondition(tempWhere.substring(0, tempWhere.length() - 1))
                    : processesFilterCondition(tempWhere.substring(1, tempWhere.length() - 2));
        }

        if (whereClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(whereClause.toString().trim().split(ValidationConstants.FILTER_SEPARATER)));
        }

        addDynamicFilters(source, filterCondition);

        for (String filter : filterCondition) {
            formExpressionEntityDetailMap(filter, null, validationId, true, false);
        }
    }

    private void processLookupForExpressionMap(List<AstNode> children, QueryBuilder builder, ValidationFunctions functions,
                                               Integer validationId) {
        String parsedStr = null;
        String sourceTable = "";
        int indexOfOptionalParams = 0;
        String source = "";

        if (!children.get(0).getToken().getValue().equals(ValidationConstants.EMPTY_PARAMETER)) {
            sourceTable = children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? children.get(0).getToken().getValue()
                    : children.get(0).getExpressionDetails().getExpressionOutput();
            if (children.get(1).getToken().getType() == TokenType.FUNCTION) {
                formExpressionEntityDetailMap(sourceTable, children.get(1), validationId, false, false);
                indexOfOptionalParams = 2;
            } else {
                formExpressionEntityDetailMap(sourceTable, null, validationId, false, false);
                indexOfOptionalParams = 1;
            }

            source = getSource(sourceTable);
        } else {
            indexOfOptionalParams = 1;
        }

        String targetTable = children.get(indexOfOptionalParams).getToken().getValue();
        if (children.get(indexOfOptionalParams + 1).getToken().getType() == TokenType.FUNCTION) {
            formExpressionEntityDetailMap(targetTable, children.get(indexOfOptionalParams + 1), validationId,
                    false, false);
            indexOfOptionalParams = indexOfOptionalParams + 2;
        } else {
            formExpressionEntityDetailMap(targetTable, null, validationId, false, false);
            indexOfOptionalParams = indexOfOptionalParams + 1;
        }

        /* For system filters */
        List<String> filterCond = new ArrayList<>();
        addDynamicFilters(getSource(targetTable), filterCond);

        for (String filter : filterCond) {
            formExpressionEntityDetailMap(filter, null, validationId, true, false);
        }

        List<String> joinCondition = new ArrayList<String>();
        List<String> outputColumns = new ArrayList<String>();
        List<String> filterCondition = new ArrayList<String>();

        // processing join conditions
        StringBuilder joinClause = new StringBuilder();
        for (int i = indexOfOptionalParams; i < children.size(); i++) {
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "")).trim();
                if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    joinClause.append(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    joinClause.append(parsedStr);
                }
            } else if (children.get(i).getToken().getValue().contains("]")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", "")).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    joinClause.append(parsedStr);
                }
                if (joinClause.charAt(joinClause.length() - 1) == ',') {
                    joinClause.deleteCharAt(joinClause.length() - 1);
                }
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    if (children.get(i) instanceof VariableNode && children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                        if (joinClause.charAt(joinClause.length() - 1) == ',') {
                            joinClause.deleteCharAt(joinClause.length() - 1).append(parsedStr);
                        } else {
                            joinClause.append(parsedStr);
                        }
                    } else {
                        joinClause.append(parsedStr).append(",");
                    }

                }
            }
        }

        if (!joinClause.toString().trim().equals("")) {
            String tempWhere = joinClause.toString().trim();
            joinClause = (tempWhere.charAt(0) != '[') ? processesFilterCondition(tempWhere)
                    : processesFilterCondition(tempWhere.substring(1, tempWhere.length() - 2));
        }

        if (joinClause.toString().trim().length() > 0) {
            joinCondition.addAll(Arrays.asList(joinClause.toString().trim().split(ValidationConstants.FILTER_SEPARATER)));
        }

        for (String join : joinCondition) {
            formExpressionEntityDetailMap(join.split("==")[1], null, validationId, true, false);
            formExpressionEntityDetailMap(join.split("==")[0], null, validationId, true, false);
        }

        // processing output columns
        String tempColNameForOPcol = "";
        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if ((!"".equalsIgnoreCase(tempColNameForOPcol)) && children.get(i) instanceof FunctionNode) {
                formExpressionEntityDetailMap(tempColNameForOPcol, children.get(i), validationId, true, false);
                tempColNameForOPcol = "";
            }
            if ((children.get(i).getToken().getValue().contains("[")
                    && children.get(i).getToken().getValue().contains("]"))
                    || children.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {

                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "")).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("") && !parsedStr.equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else if (children.get(i).getToken().getValue().contains("[")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("[", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("[", "")).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            } else if (children.get(i).getToken().getValue().contains("]")) {
                parsedStr = (children.get(i) instanceof VariableNode
                        ? children.get(i).getToken().getValue().replace("]", "")
                        : children.get(i).getExpressionDetails().getExpressionOutput().replace("]", "")).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                tempColNameForOPcol = parsedStr;
                if (!parsedStr.equalsIgnoreCase("")) {
                    outputColumns.add(parsedStr);
                }
            }
        }

        for (String col : outputColumns) {
            formExpressionEntityDetailMap(col, null, validationId, true, false);
        }

        // processing filter conditions
        StringBuilder whereClause = new StringBuilder();

        for (int i = (indexOfOptionalParams + 1); i < children.size(); i++) {
            if (children.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                whereClause.append(children.get(i).getToken().getValue() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && children.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        "(" + ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(),
                                "", builder, functions, null)[2]).getExpressionDetails().getExpression() + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue());
            } else {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(children.get(i).getToken().getValue() + ",");
            }
        }

        if (!whereClause.toString().trim().equals("")) {
            String tempWhere = whereClause.toString().trim();
            whereClause = (tempWhere.charAt(0) != '[')
                    ? processesFilterCondition(tempWhere.substring(0, tempWhere.length() - 1))
                    : processesFilterCondition(tempWhere.substring(1, tempWhere.length() - 2));
        }

        if (whereClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(whereClause.toString().trim().split(ValidationConstants.FILTER_SEPARATER)));
        }

        addDynamicFilters(source, filterCondition);

        for (String filter : filterCondition) {
            formExpressionEntityDetailMap(filter, null, validationId, true, false);
        }
    }

    private void processAggregateFunctionForExpressionMap(AstNode an, QueryBuilder builder,
                                                          ValidationFunctions functions, Integer validationId) {
        List<AstNode> chilNodes = an.getChildren();
        String source = "";
        String tableName;
        int groupByEndPos = 0;

        if (!chilNodes.get(0).getToken().getValue().equals(ValidationConstants.EMPTY_PARAMETER)) {
            tableName = chilNodes.get(0).getToken().getType().equals(TokenType.VARIABLE)
                    ? chilNodes.get(0).getToken().getValue()
                    : chilNodes.get(0).getExpressionDetails().getExpressionOutput();
            if (chilNodes.size() > 1 && chilNodes.get(1).getToken().getType() == TokenType.FUNCTION) {
                formExpressionEntityDetailMap(tableName, chilNodes.get(1), validationId, false, false);
                groupByEndPos = 2;
            } else {
                formExpressionEntityDetailMap(tableName, null, validationId, false, false);
                groupByEndPos = 1;
            }

            source = getSource(tableName);
        }

        // processing groupBy clause
        List<String> groupByColumns = new ArrayList<>();
        for (int i = groupByEndPos; i < chilNodes.size(); i++) {
            if ((chilNodes.get(i).getToken().getValue().contains("[")
                    && chilNodes.get(i).getToken().getValue().contains("]"))
                    || chilNodes.get(i).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)) {
                String value = chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue().replace("[", "").replace("]", "")
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput().replace("[", "").replace("]",
                        "");
                if (!value.equals(ValidationConstants.EMPTY_PARAMETER)) {
                    groupByColumns.add(value);
                }
                groupByEndPos = i;
                break;
            } else if (chilNodes.get(i).getToken().getValue().contains("[")) {
                groupByColumns.add(chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue().replace("[", "")
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput().replace("[", ""));

            } else if (chilNodes.get(i).getToken().getValue().contains("]")) {
                groupByColumns.add(chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue().replace("]", "")
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput().replace("]", ""));
                groupByEndPos = i;
                break;
            } else {
                groupByColumns.add(chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)
                        ? chilNodes.get(i).getToken().getValue()
                        : chilNodes.get(i).getExpressionDetails().getExpressionOutput());
            }
        }
        for (String grp : groupByColumns) {
        	 String patternString = "\"[\\s]+(?i)AS[\\s]+\"";
             Pattern pattern = Pattern.compile(patternString);
             Matcher matcher = pattern.matcher(grp);
             boolean matches = matcher.find();
             
        	if(matches) {
        		
        		formExpressionEntityDetailMap(grp.split("[\\s]+(?i)AS[\\s]+")[0], null, validationId, true, false);
        		formExpressionEntityDetailMap(ValidationConstants.ME+"."+grp.split("[\\s]+(?i)AS[\\s]+")[1], null, validationId, true, false);
        	}else {
        		formExpressionEntityDetailMap(grp, null, validationId, true, false);
        	}            
        }

        // processing filter clause
        List<String> filterCondition = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder();

        for (int i = (groupByEndPos + 1); i < chilNodes.size(); i++) {
            if (chilNodes.get(i).getToken().getType().equals(TokenType.VARIABLE)) {
                whereClause.append(chilNodes.get(i).getToken().getValue() + ",");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.FUNCTION)
                    && chilNodes.get(i).getToken().getValue().equals("PRIORITYBRACKETS")) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append("("
                        + ((AstNode) processExpressionNode(chilNodes.get(i), new HashMap<String, String>(),
                        "", builder, functions, null)[2]).getExpressionDetails().getExpression()
                        + "),");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(chilNodes.get(i), new HashMap<String, String>(), "",
                                builder, functions, null)[2]).getExpressionDetails().getExpression() + ",");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(chilNodes.get(i).getToken().getValue());
            } else {
                whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(chilNodes.get(i).getToken().getValue() + ",");
            }
        }

        if (!whereClause.toString().trim().equals("")) {
            whereClause = (whereClause.charAt(0) != '[')
                    ? processesFilterCondition(whereClause.substring(0, whereClause.length() - 1))
                    : processesFilterCondition(whereClause.substring(1, whereClause.length() - 2));
        }

        if (whereClause.toString().trim().length() > 0) {
            filterCondition.addAll(Arrays.asList(whereClause.toString().split(ValidationConstants.FILTER_SEPARATER)));
        }

        addDynamicFilters(source, filterCondition);

        for (String grp : filterCondition) {
            formExpressionEntityDetailMap(grp, null, validationId, true, false);
        }
    }

    private StringBuilder processesFilterCondition(String expression) {
        Pattern regEx;
        Matcher matcher;
        String patternToMatchCommasOutsideSquareBreackets = ",(?=(((?!\\]).)*\\[)|[^\\[\\]]*$)";
        String patterToMatchDoubleQuotesInBrackets = "\\(.*?\\)";
        // modifying and conditions.
        String patternToReplaceCommasInAndOR = ",(?=[^\\(\\)]*\\))(?=(((?!\\]).)*\\[)|[^\\[\\]]*$)";
        regEx = Pattern.compile(patternToReplaceCommasInAndOR);
        expression = regEx.matcher(expression).replaceAll("###LO###");

        String patternToReplaceANDCommas = "AND(\\([^\\)]+\\))(?![^\\s,])(?![^\\[]*\\])";
        regEx = Pattern.compile(patternToReplaceANDCommas);
        matcher = regEx.matcher(expression);
        Map<String, String> matchedSubMapAndReplacedString = new HashMap<>();
        String MatchedStr;
        while (matcher.find()) {
            MatchedStr = matcher.group();
            matchedSubMapAndReplacedString.put(MatchedStr, MatchedStr.replace("AND", "").replace("###LO###", " AND "));
        }

        // modifying or conditions
        String patternToReplaceORCommas = "OR(\\([^\\)]+\\))(?![^\\s,])(?![^\\[]*\\])";
        regEx = Pattern.compile(patternToReplaceORCommas);
        matcher = regEx.matcher(expression);
        while (matcher.find()) {
            MatchedStr = matcher.group();
            matchedSubMapAndReplacedString.put(MatchedStr, MatchedStr.replace("OR", "").replace("###LO###", " OR "));
        }

        for (Map.Entry<String, String> entry : matchedSubMapAndReplacedString.entrySet()) {
            expression = ValidationStringUtils.replace(expression, entry.getKey(), entry.getValue(), -1, true);
        }

        // Create a Pattern object
        regEx = Pattern.compile(patternToMatchCommasOutsideSquareBreackets);
        expression = regEx.matcher(expression).replaceAll(ValidationConstants.FILTER_SEPARATER);
        expression = expression.replace("[", "(").replace("]", ")");
        regEx = Pattern.compile(patterToMatchDoubleQuotesInBrackets);
        matcher = regEx.matcher(expression);
        while (matcher.find()) {
            MatchedStr = matcher.group();
            matchedSubMapAndReplacedString.put(MatchedStr, MatchedStr.replace("\"", "'"));
        }
        for (Map.Entry<String, String> entry : matchedSubMapAndReplacedString.entrySet()) {
            expression = ValidationStringUtils.replace(expression, entry.getKey(), entry.getValue(), -1, true);
        }
        return new StringBuilder(expression);
    }

    private String formExpressionEntityDetailMap(String expression, AstNode an, Integer validationId,
                                               boolean notAddMetadata, boolean isMePresent) {
        MetadataResolver resolver = new MetadataResolver();
        resolver.setSolutionId(systemSolutionId);
        String expressionKey = expression;

        if (expression.toUpperCase().startsWith(ValidationConstants.TYPE_RETURN)) {
            // only process for type return.
            String[] arrayOfParams = expression.split("\\.");
            String returnName = arrayOfParams[1].replace("\"", "");
            String sectionDesc = arrayOfParams[2].replace("\"", "");
            String lineItemCode = arrayOfParams.length > 3 ? extractColNameFromFilter(arrayOfParams[3]).toString().trim() : null;
            Integer reportId = resolver.getRegReportId(returnName.replace("\"", ""), null, systemSolutionId);
            String pId = null;
            String rStatus = null;
            String orgCode = null;

            if (null != an) {
                List<AstNode> children = an.getChildren();
                pId = (children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? null
                        : (children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new ValidationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpressionOutput()
                        : children.get(0).getExpressionDetails().getExpressionOutput()));

                String periodExpression = children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? ""
                        : children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new ValidationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpression()
                        : children.get(0).getExpressionDetails().getExpression();

                expressionKey = expressionKey + "(" + (periodExpression != null ? periodExpression : "");

                if (children.size() > 1) {
                    rStatus = !children.get(1).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)
                            ? children.get(1).getToken().getValue()
                            : null;

                    if (rStatus != null && !rStatus.equals("")) {
                        expressionKey = expressionKey + "," + rStatus;
                        rStatus = rStatus.replace("\"", "");
                    } else {
                        expressionKey = expressionKey + ",";
                    }

                    if (children.size() > 2) {
                        orgCode = !children.get(2).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)
                                ? children.get(2).getToken().getValue()
                                : null;

                        if (orgCode != null && !orgCode.equals("")) {
                            expressionKey = expressionKey + "," + orgCode;
                            orgCode = orgCode.replace("\"", "");
                        }
                    }
                }

                expressionKey = expressionKey + ")";
            }

            ExpressionEntityDetail eed = null;

            if (expressionEntityDetailsMap.get(validationId) == null) {
                eed = populateExpressionEntityDetails(returnName, reportId, sectionDesc, lineItemCode, orgCode, pId,
                        rStatus, ValidationConstants.TYPE_RETURN, resolver, expressionKey, notAddMetadata, isMePresent, null,null,null);
                List<ExpressionEntityDetail> eedArr = new ArrayList<>();
                eedArr.add(eed);
                expressionEntityDetailsMap.put(validationId, eedArr);
            } else {
                eed = expressionEntityDetailsMap.get(validationId).stream()
                        .filter(s -> {
                            if (s.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                                ReturnEntityDetail entityDetail1 = (ReturnEntityDetail) s;

                                return entityDetail1.getSectionDesc().equalsIgnoreCase(sectionDesc)
                                        && entityDetail1.getEntityCode().equalsIgnoreCase(returnName);
                            }
                            return false;
                        }).findFirst().orElse(null);

                if (null == eed) {
                    eed = populateExpressionEntityDetails(returnName, reportId, sectionDesc, lineItemCode, orgCode, pId,
                            rStatus, ValidationConstants.TYPE_RETURN, resolver, expressionKey, notAddMetadata, isMePresent, null,null,null);
                    expressionEntityDetailsMap.get(validationId).add(eed);
                } else {
                    if (null != lineItemCode) {
                        eed.getEntityElements().add("\"" + lineItemCode.trim() + "\"");
                    }

                    populateMetaDataInfo(eed, orgCode, pId, rStatus, resolver, expressionKey, notAddMetadata, reportId);
                }
            }
        } else if (expression.toUpperCase().startsWith(ValidationConstants.TYPE_REFTABLE)) {

            // only process for type ref table.
            String[] arrayOfParams = expression.split("\\.");
            String tableName = arrayOfParams[1].replace("\"", "");
            String columnName = arrayOfParams.length > 2 ? extractColNameFromFilter(arrayOfParams[2]).toString().trim() : null;
            String pId = null;
            String orgCode = null;

            if (null != an) {
                List<AstNode> children = an.getChildren();

                pId = (children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? null
                        : (children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<>(), "", new QueryBuilder(),
                        new ValidationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpressionOutput()
                        : children.get(0).getExpressionDetails().getExpressionOutput()));

                String periodExpression = children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? ""
                        : children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new ValidationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpression()
                        : children.get(0).getExpressionDetails().getExpression();

                expressionKey = expressionKey + "(" + (periodExpression != null ? periodExpression : "");

                if (children.size() > 1) {
                    orgCode = !children.get(1).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)
                            ? children.get(1).getToken().getValue()
                            : null;

                    if (orgCode != null && !orgCode.equals("")) {
                        expressionKey = expressionKey + "," + orgCode;
                        orgCode = orgCode.replace("\"", "");
                    }
                }

                expressionKey = expressionKey + ")";
            }

            ExpressionEntityDetail eed = null;

            if (expressionEntityDetailsMap.get(validationId) == null) {
                eed = populateExpressionEntityDetails(tableName, null, null, columnName, orgCode, pId, null,
                        ValidationConstants.TYPE_REFTABLE, resolver, expressionKey, notAddMetadata, isMePresent, null,null,null);
                List<ExpressionEntityDetail> eedArr = new ArrayList<>();
                eedArr.add(eed);
                expressionEntityDetailsMap.put(validationId, eedArr);
            } else {
                eed = expressionEntityDetailsMap.get(validationId).stream()
                        .filter(s -> s.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE) &&
                                s.getEntityCode().equalsIgnoreCase(tableName))
                        .findFirst().orElse(null);

                if (null == eed) {
                    eed = populateExpressionEntityDetails(tableName, null, null, columnName, orgCode, pId, null,
                            ValidationConstants.TYPE_REFTABLE, resolver, expressionKey, notAddMetadata, isMePresent, null,null,null);
                    expressionEntityDetailsMap.get(validationId).add(eed);
                } else {
                    if (null != columnName) {
                        eed.getEntityElements().add("\"" + columnName.trim() + "\"");
                    }
                    populateMetaDataInfo(eed, orgCode, pId, null, resolver, expressionKey, notAddMetadata, null);
                }
            }
        } else if (expression.toUpperCase().startsWith(ValidationConstants.TYPE_ENTITY)) {

            // only process for type ref table.
            String[] arrayOfParams = expression.split("\\.");
            String subjectArea = arrayOfParams[1].replace("\"", "");
            String tableName = arrayOfParams[2].replace("\"", "");
            String columnName = arrayOfParams.length > 3 ? extractColNameFromFilter(arrayOfParams[3]).toString().trim() : null;
            String pId = null;
            String orgCode = null;

            if (null != an) {
                List<AstNode> children = an.getChildren();

                pId = (children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? null
                        : (children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<>(), "", new QueryBuilder(),
                        new ValidationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpressionOutput()
                        : children.get(0).getExpressionDetails().getExpressionOutput()));

                String periodExpression = children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? ""
                        : children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new ValidationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpression()
                        : children.get(0).getExpressionDetails().getExpression();

                expressionKey = expressionKey + "(" + (periodExpression != null ? periodExpression : "");

                if (children.size() > 1) {
                    orgCode = !children.get(1).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER)
                            ? children.get(1).getToken().getValue()
                            : null;

                    if (orgCode != null && !orgCode.equals("")) {
                        expressionKey = expressionKey + "," + orgCode;
                        orgCode = orgCode.replace("\"", "");
                    }
                }

                expressionKey = expressionKey + ")";
            }

            ExpressionEntityDetail eed = null;

            if (expressionEntityDetailsMap.get(validationId) == null) {
                eed = populateExpressionEntityDetails(tableName, null, null, columnName, orgCode, pId, null,
                        ValidationConstants.TYPE_ENTITY, resolver, expressionKey, notAddMetadata, isMePresent, subjectArea, tableName, columnName);
                List<ExpressionEntityDetail> eedArr = new ArrayList<>();
                eedArr.add(eed);
                expressionEntityDetailsMap.put(validationId, eedArr);
            } else {
                eed = expressionEntityDetailsMap.get(validationId).stream()
                        .filter(s -> s.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_ENTITY) &&
                                s.getEntityCode().equalsIgnoreCase(tableName))
                        .findFirst().orElse(null);

                if (null == eed) {
                    eed = populateExpressionEntityDetails(tableName, null, null, columnName, orgCode, pId, null,
                            ValidationConstants.TYPE_ENTITY, resolver, expressionKey, notAddMetadata, isMePresent, subjectArea, tableName, columnName);
                    expressionEntityDetailsMap.get(validationId).add(eed);
                } else {
                    if (null != columnName) {
                        eed.getEntityElements().add("\"" + columnName.trim() + "\"");
                    }
                    populateMetaDataInfo(eed, orgCode, pId, null, resolver, expressionKey, notAddMetadata, null);
                }
            }
        } else if (expression.startsWith(ValidationConstants.ME + ".")) {
            String replacedExp = ValidationConstants.ME + ".";
            String exp = ValidationStringUtils.replace(expression, replacedExp, meMap.get(ValidationConstants.ME) + ".", -1, true);
            formExpressionEntityDetailMap(exp, an, validationId, true, isMePresent);
        }

        return expressionKey;
    }

    private ExpressionEntityDetail populateExpressionEntityDetails(String entityCode, Integer reportId, String sectionDesc,
                                                                   String entityElement, String orgCode, String periodId,
                                                                   String returnStatus, String entityType,
                                                                   MetadataResolver resolver, String expressionKey,
                                                                   boolean notAddMetadata, boolean isMePresent, String subjectArea,
                                                                   String ddTableName, String ddColumnName) {
        Set<String> entityElements = new HashSet<>();
        if (null != entityElement) {
            entityElements.add("\"" + entityElement.trim() + "\"");
        }

        if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            Map<String, ReturnMetadataInfo> metaDataInfoMap = new HashMap<>();

            if (!notAddMetadata) {
                ReturnMetadataInfo metaDataInfo = new ReturnMetadataInfo();
                if (periodId != null) {
                    metaDataInfo.setPeriodId(Integer.parseInt(periodId));
                } else {
                    metaDataInfo.setPeriodId(systemPeriodId);
                }

                if (orgCode != null && !orgCode.trim().equals("")) {
                    metaDataInfo.setOrgCode(orgCode);
                } else {
                    metaDataInfo.setOrgCode(systemOrgCode);
                }

                if (returnStatus != null && !returnStatus.trim().equals("")) {
                    metaDataInfo.setReturnStatus(returnStatus);
                } else {
                    metaDataInfo.setReturnStatus(systemReturnStatus);
                }

                // populating orgId
                metaDataInfo.setOrgId(resolver.getOrgId(metaDataInfo.getOrgCode(), systemSolutionId));

                if (systemRegReportId.equals(reportId) && systemPeriodId.equals(metaDataInfo.getPeriodId()) &&
                        (systemReturnStatus == null || systemReturnStatus.equalsIgnoreCase(metaDataInfo.getReturnStatus()))) {
                    metaDataInfo.setVersionNo(systemVersionNo);
                    metaDataInfo.setReportVersion(systemRegReportVersion);
                } else {
                    Object[] result = resolver.getReportVersionAndVersionNo(metaDataInfo.getOrgId(), reportId,
                            metaDataInfo.getReturnStatus(), metaDataInfo.getPeriodId());

                    if (result != null) {
                        metaDataInfo.setVersionNo(Integer.parseInt(result[0].toString()));
                        metaDataInfo.setReportVersion(Integer.parseInt(result[1].toString()));
                    } else {
                        failedMetaDataExp.put(new Integer(expressionId), Boolean.TRUE);
                    }
                }

                if (failedMetaDataExp.get(expressionId) == null) {
                    metaDataInfoMap.put(expressionKey, metaDataInfo);
                }

            }

            return new ReturnEntityDetail(entityType, entityCode, entityElements,
                    isMePresent, reportId, sectionDesc, metaDataInfoMap);

        } else if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
            Map<String, RefMetadataInfo> metaDataInfoMap = new HashMap<>();

            if (!notAddMetadata) {
                RefMetadataInfo metaDataInfo = new RefMetadataInfo();
                if (periodId != null) {
                    metaDataInfo.setPeriodId(Integer.parseInt(periodId));
                } else {
                    metaDataInfo.setPeriodId(systemPeriodId);
                }

                if (orgCode != null && !orgCode.trim().equals("")) {
                    metaDataInfo.setOrgCode(orgCode);
                } else {
                    metaDataInfo.setOrgCode(systemOrgCode);
                }

                // populating orgId
                metaDataInfo.setOrgId(resolver.getOrgId(metaDataInfo.getOrgCode(), systemSolutionId));
                if (failedMetaDataExp.get(expressionId) == null) {
                    metaDataInfoMap.put(expressionKey, metaDataInfo);
                }
            }

            return new RefEntityDetail(entityType, entityCode, entityElements, isMePresent,
                    metaDataInfoMap);
        } else if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            Map<String, EntityMetadataInfo> metaDataInfoMap = new HashMap<>();

            if (!notAddMetadata) {
            	EntityMetadataInfo metaDataInfo = new EntityMetadataInfo();
                if (periodId != null) {
                    metaDataInfo.setPeriodId(Integer.parseInt(periodId));
                } else {
                    metaDataInfo.setPeriodId(systemPeriodId);
                }

                if (orgCode != null && !orgCode.trim().equals("")) {
                    metaDataInfo.setOrgCode(orgCode);
                } else {
                    metaDataInfo.setOrgCode(systemOrgCode);
                }

                // populating orgId
                metaDataInfo.setOrgId(resolver.getOrgId(metaDataInfo.getOrgCode(), systemSolutionId));
                if (failedMetaDataExp.get(expressionId) == null) {
                    metaDataInfoMap.put(expressionKey, metaDataInfo);
                }
            }

            return new ValidationEntityDetail(entityType, entityCode, entityElements, isMePresent,
                    metaDataInfoMap, subjectArea, ddTableName, ddColumnName);
        }

        return null;
    }

    private void populateMetaDataInfo(ExpressionEntityDetail eed, String orgCode, String periodId, String returnStatus,
                                      MetadataResolver resolver, String expressionKey,
                                      boolean notAddMetadata, Integer reportId) {

        if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            ReturnEntityDetail entityDetail = (ReturnEntityDetail) eed;

            if (!entityDetail.getMetaDataInfoMap().containsKey(expressionKey.trim()) && !notAddMetadata) {
                ReturnMetadataInfo metaDataInfo = new ReturnMetadataInfo();

                if (periodId != null) {
                    metaDataInfo.setPeriodId(Integer.parseInt(periodId));
                } else {
                    metaDataInfo.setPeriodId(systemPeriodId);
                }

                if (orgCode != null && !orgCode.trim().equals("")) {
                    metaDataInfo.setOrgCode(orgCode);
                } else {
                    metaDataInfo.setOrgCode(systemOrgCode);
                }

                if (returnStatus != null && !returnStatus.trim().equals("")) {
                    metaDataInfo.setReturnStatus(returnStatus);
                } else {
                    metaDataInfo.setReturnStatus(systemReturnStatus);
                }

                // populating orgId
                metaDataInfo.setOrgId(resolver.getOrgId(metaDataInfo.getOrgCode(), systemSolutionId));

                if (systemRegReportId.equals(reportId) && systemPeriodId.equals(metaDataInfo.getPeriodId()) &&
                        (systemReturnStatus == null || systemReturnStatus.equalsIgnoreCase(metaDataInfo.getReturnStatus()))) {
                    metaDataInfo.setVersionNo(systemVersionNo);
                    metaDataInfo.setReportVersion(systemRegReportVersion);
                } else {
                    Object[] result = resolver.getReportVersionAndVersionNo(metaDataInfo.getOrgId(), reportId,
                            metaDataInfo.getReturnStatus(), metaDataInfo.getPeriodId());

                    if (result != null) {
                        metaDataInfo.setVersionNo(Integer.parseInt(result[0].toString()));
                        metaDataInfo.setReportVersion(Integer.parseInt(result[1].toString()));
                    } else {
                        failedMetaDataExp.put(new Integer(expressionId), Boolean.TRUE);
                    }
                }

                if (failedMetaDataExp.get(expressionId) == null) {
                    entityDetail.getMetaDataInfoMap().put(expressionKey, metaDataInfo);
                }
            }
        } else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
            RefEntityDetail entityDetail = (RefEntityDetail) eed;

            if (!entityDetail.getMetaDataInfoMap().containsKey(expressionKey.trim()) && !notAddMetadata) {
                RefMetadataInfo metaDataInfo = new RefMetadataInfo();

                if (periodId != null) {
                    metaDataInfo.setPeriodId(Integer.parseInt(periodId));
                } else {
                    metaDataInfo.setPeriodId(systemPeriodId);
                }

                if (orgCode != null && !orgCode.trim().equals("")) {
                    metaDataInfo.setOrgCode(orgCode);
                } else {
                    metaDataInfo.setOrgCode(systemOrgCode);
                }

                // populating orgId
                metaDataInfo.setOrgId(resolver.getOrgId(metaDataInfo.getOrgCode(), systemSolutionId));

                if (failedMetaDataExp.get(expressionId) == null) {
                    entityDetail.getMetaDataInfoMap().put(expressionKey, metaDataInfo);
                }
            }
        } else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
        	ValidationEntityDetail entityDetail = (ValidationEntityDetail) eed;

            if (!entityDetail.getMetaDataInfoMap().containsKey(expressionKey.trim()) && !notAddMetadata) {
            	EntityMetadataInfo metaDataInfo = new EntityMetadataInfo();

                if (periodId != null) {
                    metaDataInfo.setPeriodId(Integer.parseInt(periodId));
                } else {
                    metaDataInfo.setPeriodId(systemPeriodId);
                }

                if (orgCode != null && !orgCode.trim().equals("")) {
                    metaDataInfo.setOrgCode(orgCode);
                } else {
                    metaDataInfo.setOrgCode(systemOrgCode);
                }

                // populating orgId
                metaDataInfo.setOrgId(resolver.getOrgId(metaDataInfo.getOrgCode(), systemSolutionId));

                if (failedMetaDataExp.get(expressionId) == null) {
                    entityDetail.getMetaDataInfoMap().put(expressionKey, metaDataInfo);
                }
            }
        }
    }

    public String convertIfThenElseToTernaryAndProcessPriorityBrackets(String inputStr) {
        String getStringBetweenQuotes = "([\"'])(?:(?=(\\\\?))\\2.)*?\\1";
        Pattern regEx;
        Matcher matcher;
        regEx = Pattern.compile(getStringBetweenQuotes);
        matcher = regEx.matcher(inputStr);
        String MatchedStr;
        Map<String, String> matchedSubMapAndReplacedString = new HashMap<>();

        while (matcher.find()) {
            MatchedStr = matcher.group();
            for (String s : nameOperatorConflitMap.keySet()) {
                if (matchedSubMapAndReplacedString.containsKey(MatchedStr)) {
                    matchedSubMapAndReplacedString.put(MatchedStr, ValidationStringUtils.replace(matchedSubMapAndReplacedString.get(MatchedStr), s, nameOperatorConflitMap.get(s), -1, true));
                } else {
                    matchedSubMapAndReplacedString.put(MatchedStr, ValidationStringUtils.replace(MatchedStr, s, nameOperatorConflitMap.get(s), -1, true));
                }
            }
        }

        for (Map.Entry<String, String> entry : matchedSubMapAndReplacedString.entrySet()) {
            inputStr = ValidationStringUtils.replace(inputStr, entry.getKey(), entry.getValue(), -1, false);
        }

        if (inputStr.toLowerCase().startsWith("foreach")) {
            inputStr = inputStr.replaceAll("\\{", "\\{\\(").replaceAll("\\}", "\\)\\}").replaceAll("(?i)IF\\(", "IF\\(").replaceAll("(?i)THEN\\(", "\\THEN\\(").replaceAll("(?i)ELSE\\(", "\\ELSE\\(");
        } else {
            inputStr = "(" + inputStr.replaceAll("(?i)IF\\(", "IF\\(").replaceAll("(?i)THEN\\(", "\\THEN\\(").replaceAll("(?i)ELSE\\(", "\\ELSE\\(") + ")";
        }

        regEx = Pattern.compile("###(\\d+)###");

        matcher = regEx.matcher(inputStr);
        while (matcher.find()) {
            MatchedStr = matcher.group();
            inputStr = ValidationStringUtils.replace(inputStr, MatchedStr, (String.valueOf(Character.toChars(Integer.parseInt(MatchedStr.replace("###", ""))))), -1, true);
        }

        return inputStr;
    }

    private void updateRequestStatus(ValidationRequest vr, String status, MetadataResolver resolver) {
        resolver.updateRequestStatus(vr, status);
    }

    private void updateReturnResultStatus(Integer runId, Integer solutionId, String status, MetadataResolver resolver) {
        resolver.updateReturnResultStatus(runId, solutionId, status);
    }

    public void populateErrorMap(String... errorMsg) {
        metadataErrorMap.computeIfAbsent(ValidationConstants.PARSER_ERROR_KEY, k -> new ArrayList<>());
        metadataErrorMap.get(ValidationConstants.PARSER_ERROR_KEY).addAll(Arrays.asList(errorMsg));
    }

    private ValidationRunDetails getValidationResultsObject(Integer runId, ValidationMaster validationMaster,
    		ExpressionStatus status) {
        
    	if(status == null) {
    		status = new ExpressionStatus(validationMaster.getValidationId(),Boolean.TRUE);
    	}
        ValidationRunDetails vrd = new ValidationRunDetails();
        
        vrd.setRunId(runId);
        vrd.setValidationId(validationMaster.getValidationId());
        vrd.setSequenceNumber(validationMaster.getSequenceNo());
        vrd.setStatus((status.getErrorCount().get() == 0 && !status.getHasError()) ? ValidationConstants.VALIDATION_STATUS_PASSED : ValidationConstants.VALIDATION_STATUS_FAILED);
        vrd.setEvaluatedExpression(validationMaster.getValidationExpression());
        vrd.setTotalOccurrence(status.getTotalCount());
        vrd.setTotalFailed(status.getErrorCount().get());
        vrd.setValidationType(validationMaster.getValidationType());
        vrd.setDimensionsCSV(status.getDimensionsCSV() != null ? status.getDimensionsCSV() : "");
        vrd.setReplacedExpression(status.getReplacedExpression());
        
        return vrd;
    }

    private void saveOrUpdateReturnResultStatus(List<ValidationRunDetails> vrdList, MetadataResolver resolver) {
        resolver.saveOrUpdateReturnResultStatus(vrdList);
    }

    private List<String> replaceNotInInFilter(List<String> filterConditions) {
        List<String> replacedFilterConditions = new ArrayList<>();

        for (String filter : filterConditions) {
            replacedFilterConditions.add(filter.replaceAll("(?i)NOTIN\\s*\\(", "NOT IN ("));
        }

        return replacedFilterConditions;
    }

    private class EnquePipeJobRequest implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                try {
                    LOGGER.info("Adding request to queue " + r.toString());
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    private Set<String> validationTypes(String valPayload){
		String valGrp = null;
		String valGrpCsv = null;
		String[] valGrpTypes = null;
		String[] valGrpCsvTypes = null;
		Set<String> validationTypes = new HashSet<>();
		Gson g = new Gson();
		JsonObject p = g.fromJson(valPayload,JsonObject.class);
		if(p.get("validationGroups") != null) {
			valGrp = p.get("validationGroups").toString().substring(1, p.get("validationGroups").toString().length()-1).replace("\"", "");
			if(valGrp.contains(",")) {
				valGrpTypes = valGrp.split(",");
			}
			else {
				valGrpTypes = new String[1];
				valGrpTypes[0] = valGrp;
			}
			for(String s : valGrpTypes) {
				validationTypes.add(s);
			}
		}
		if(p.get("validationGroupIdCSV") != null) {
			valGrpCsv = p.get("validationGroupIdCSV").toString().substring(1, p.get("validationGroupIdCSV").toString().length()-1).replace("\"", "");
			if(valGrpCsv.contains(",")) {
				valGrpCsvTypes = valGrpCsv.split(",");
			}else {
				valGrpCsvTypes = new String[1];
				valGrpCsvTypes[0] = valGrpCsv;
			}
			for(String s : valGrpCsvTypes) {
				validationTypes.add(s);
			}
		}
		return validationTypes;
    }

    private void addDynamicFilters(List<DynamicFilter> filters, String tableName, List<String> filterCondition,
                                   QueryBuilder builder) {
        if (filters != null && !filters.isEmpty() && tableName != null && !tableName.trim().equals("")) {
            String tempTableName;
            boolean isMePresent = false;

            if (tableName.trim().equalsIgnoreCase(ValidationConstants.ME_IDENTIFIER)) {
                tempTableName = builder.getBaseTableName().trim();
                isMePresent = true;
            } else {
                tempTableName = tableName.split("#")[tableName.split("#").length - 1].trim();
            }

            for (DynamicFilter f : filters) {
                if (f.getColumnName().startsWith(tempTableName)) {
                    String columnName = f.getColumnName();
                    if (isMePresent) {
                        columnName = ValidationStringUtils.replace(columnName, tempTableName,
                                ValidationConstants.ME_IDENTIFIER, -1, true);
                    }
                    filterCondition.add(columnName + " " + f.getCondition());
                }
            }
        }
    }

    private void addDynamicFilters(String source, List<String> filterCondition) {
        if (dynamicFilters != null && !dynamicFilters.isEmpty()) {
            if (systemEntityType.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
                String systemSource = systemEntityType + ".\"" + systemEntityCode + "\"";
                if (systemSource.equalsIgnoreCase(source)) {
                    dynamicFilters.forEach(f -> filterCondition.add(f.getColumnName() + " " + f.getCondition()));
                }
            } else if (systemEntityType.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
                String systemSource = systemEntityType + ".\"" + systemSubjectArea + "\"" + ".\"" + systemEntityCode + "\"";
                if (systemSource.equalsIgnoreCase(source)) {
                    dynamicFilters.forEach(f -> filterCondition.add(f.getColumnName() + " " + f.getCondition()));
                }
            }
        }
    }

    private String getSource(String sourceExp) {
	    String source = "";

        if (sourceExp.toUpperCase().startsWith(ValidationConstants.ME)) {
            sourceExp = ValidationStringUtils.replace(sourceExp, ValidationConstants.ME,
                    meMap.get(ValidationConstants.ME), -1, true);
        }

        String[] expArr = sourceExp.split("\\.");
        if (systemEntityType.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
            source = systemEntityType + "." + expArr[1];
        } else if (systemEntityType.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            source = systemEntityType + "." + expArr[1] + "." + expArr[2];
        }

        return source;
    }

    private void populateExpressionGroupMap(ExpressionKey key, Integer validationId) {
        expressionGroupMap.computeIfAbsent(key, k -> new ArrayList<>());
        expressionGroupMap.get(key).add(validationId);
    }


    synchronized public String getColumnUUID(String columnName) {
		// TODO Auto-generated method stub
    	if(columnUUIDMap.containsKey(columnName.toUpperCase())){
    		return columnUUIDMap.get(columnName.toUpperCase());
    	}else {
    		String uuid = UUID.randomUUID().toString().replaceAll("-", "_");
    		columnUUIDMap.put(columnName.toUpperCase(), uuid);
    		return uuid;
    	}
		
	}

    private ValidationGroupCsvLinkage getValidationToCsvLinkage(Integer runId, Integer validationid, String folderName,
                                                                String csvName, boolean csvGenerationStatus) {
        return new ValidationGroupCsvLinkage((long) runId, (long) validationid, folderName, csvName, csvGenerationStatus);
    }

    private void saveValidationGroupCsvLinkage(List<ValidationGroupCsvLinkage> linkages) {
        ValidationExecutionBo validationExecutionBo = BeanUtil.getBean(ValidationExecutionBo.class);
        validationExecutionBo.saveValidationGroupCsvLinkage(linkages);
    }
}