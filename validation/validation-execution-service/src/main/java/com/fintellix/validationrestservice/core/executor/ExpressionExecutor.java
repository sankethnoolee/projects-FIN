package com.fintellix.validationrestservice.core.executor;

import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.validationrestservice.core.evaulator.ValidationFunctions;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;
import com.fintellix.validationrestservice.core.metadataresolver.MetadataResolver;
import com.fintellix.validationrestservice.core.parser.ExpressionParser;
import com.fintellix.validationrestservice.core.parser.prefix.ast.AstNode;
import com.fintellix.validationrestservice.core.runprocessor.MetadataBuilder;
import com.fintellix.validationrestservice.definition.EntityMetadataInfo;
import com.fintellix.validationrestservice.definition.ExpressionEntityDetail;
import com.fintellix.validationrestservice.definition.QueryBuilder;
import com.fintellix.validationrestservice.definition.RefEntityDetail;
import com.fintellix.validationrestservice.definition.RefMetadataInfo;
import com.fintellix.validationrestservice.definition.ReturnEntityDetail;
import com.fintellix.validationrestservice.definition.ReturnMetadataInfo;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.definition.ValidationEntityDetail;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.vo.DynamicFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class ExpressionExecutor {
    private static Logger LOGGER = LoggerFactory.getLogger(ExpressionExecutor.class);
    
    @Autowired
	private Function<String, ExpressionProcessor> expressionProcessorBeanFactory;
    
    private Integer exprId;
    private Map<Integer, Map<String, String>> expressionToColumnsMap = new ConcurrentHashMap<>();
    private Map<Integer, Set<String>> expressionToTablesMap = new ConcurrentHashMap<>();
    private Map<Integer, String> expressionIdMap = new ConcurrentHashMap<>();
    private MetadataBuilder metadataBuilder;
    private ExpressionParser expressionParser;
    private MetadataResolver resolver;
    private Integer systemSolutionId;
    private Integer runId;
    private Map<Integer, ValidationMaster> vmMap = new ConcurrentHashMap<>();
    
    
    private Map<Integer, Boolean> expJobLink;
    private List<DynamicFilter> dynamicFilters;

    public ExpressionProcessor getExpressionProcessorInstance(String name) {
    	ExpressionProcessor bean = expressionProcessorBeanFactory.apply(name);
		return bean;
	}
    
    public void init(Integer exprId, Map<Integer, Map<String, String>> expressionToColumnsMap,
                     Map<Integer, Set<String>> expressionToTablesMap, Map<Integer, String> expressionIdMap,
                     MetadataBuilder metadataBuilder, ExpressionParser expressionParser,
                     MetadataResolver resolver, Integer systemSolutionId, Integer runId, Map<Integer, ValidationMaster> vmMap,
                     Map<Integer, Boolean> expJobLink, List<DynamicFilter> dynamicFilters) {
        this.exprId = exprId;
        this.expressionToColumnsMap = expressionToColumnsMap;
        this.expressionToTablesMap = expressionToTablesMap;
        this.expressionIdMap = expressionIdMap;
        this.metadataBuilder = metadataBuilder;
        this.expressionParser = expressionParser;
        this.resolver = resolver;
        this.systemSolutionId = systemSolutionId;
        this.runId = runId;
        this.vmMap = vmMap;
        
        this.expJobLink = expJobLink;
        this.dynamicFilters = dynamicFilters;
    }

    
    public void executeRequest() {
    	long startTime = System.currentTimeMillis();
        LOGGER.info("Executing expression : " + exprId);
        QueryBuilder builder = new QueryBuilder();
        ValidationFunctions functions = new ValidationFunctions();
        boolean isForEachPresent = false;
        StringBuilder dimensionsCSV = null;

        try {
            Map<String, Map<String, String>> dimensionColumnData = metadataBuilder.getDimensionColumnData();
            String originalExpr = expressionIdMap.get(exprId);

            /*
             * Replacing column name in the actual expression
             */
            originalExpr = replaceOriginalExpression(originalExpr, metadataBuilder, expressionParser, exprId);

            AstNode expAn = expressionParser.tokenizeAndParseIntoTree(originalExpr);
            functions.init(metadataBuilder.getTableMetadata(), metadataBuilder.getDimensionColumnData(),
                    metadataBuilder.getTableQueryInfo(), metadataBuilder.getReturnTableLink(),
                    metadataBuilder.getReturnEntityNameInfo(), metadataBuilder.getReturnFormInfo(),
                    metadataBuilder.getAliaisedLineColumnData());

            if (expAn.getToken().getType().equals(TokenType.FUNCTION)
                    && expAn.getToken().getValue().equalsIgnoreCase("FOREACH")) {
                expressionParser.processForeachNode(expAn, builder, functions, dynamicFilters);
                isForEachPresent = true;
            }

            expressionParser.processExpressionNode(expAn, new HashMap<>(), originalExpr, builder, functions, dynamicFilters);

            LOGGER.info("\nFinal Query : \n" + builder.getQuery().trim() + "\n");
            LOGGER.info("\noriginalExpr\n" + expressionIdMap.get(exprId));
            LOGGER.info("\nreplacedExpr\n" + originalExpr + "\n");

            Map<String, Integer> columnMetaData = resolver.getMetadata("select * from (" + builder.getQuery().trim() + ") t2 where 1=2", systemSolutionId);

            /* creating dimension map from dimensions given at FOR-EACH level */
            Map<String, String> groupByCols = new TreeMap<>();
            for (String groupBy : builder.getGroupBy()) {
                groupBy = groupBy.replace(builder.getBaseTableName() + ".", "");

                if(expressionToColumnsMap.get(exprId).get(groupBy) != null) {
                    groupByCols.put(groupBy, expressionToColumnsMap.get(exprId).get(groupBy));
                } else {
                    groupByCols.put(groupBy, dimensionColumnData.get(builder.getBaseTableName()).get(groupBy));
                }
            }

            /* if there is no dimension provided at FOR-EACH level,
             * then getting dimensions data from sections used in that expression
             */
            if (builder.getBaseTableName() != null && dimensionColumnData.get(builder.getBaseTableName()) != null
                    && !dimensionColumnData.get(builder.getBaseTableName()).isEmpty()) {
                expressionToColumnsMap.get(exprId).putAll(dimensionColumnData.get(builder.getBaseTableName()));

                if (groupByCols.isEmpty()) {
                    groupByCols.putAll(dimensionColumnData.get(builder.getBaseTableName()));
                }
            }

            if (isForEachPresent && !groupByCols.isEmpty()) {
                for (Map.Entry<String, String> entry : groupByCols.entrySet()) {
                    if (dimensionsCSV == null) {
                        dimensionsCSV = new StringBuilder("");
                    } else {
                        dimensionsCSV.append(",");
                    }

                    dimensionsCSV.append(entry.getValue().replace("\"", ""));
                }
            }

            ExpressionProcessor expr = functions.initExpressionProcessor(builder.getQuery(), systemSolutionId,expressionToColumnsMap.get(exprId).keySet(),
                    builder.getExpression(), expressionParser, expressionToTablesMap.get(exprId), groupByCols, exprId,
                    columnMetaData, dimensionsCSV,runId,getExpressionProcessorInstance(System.currentTimeMillis() + "_" + Math.random()+"_"+exprId),
                    expressionToColumnsMap.get(exprId));
            
            expressionParser.pipelinerunningJobs.add(expressionParser.pipelinePool.submit(expr));
            
//            expJobLink.put(exprId, !(expr.getHasError()));
//            DataReader dataReader = functions.read(builder.getQuery(), systemSolutionId);
//
//            DataReader transformedDataReader = functions.addExpr(expressionToColumnsMap.get(exprId).keySet(),
//                    builder.getExpression(), expressionParser, expressionToTablesMap.get(exprId), groupByCols, exprId,
//                    columnMetaData, dataReader, runRecordDetails, dimensionsCSV);

//            runJob(exprId, runId, transformedDataReader);

        } catch (Throwable e) {
            /*
             * In case of exception during parallel processing of Job, code computation will never reach this block.
             * This is handled by populating 'expJobLink' map for every Job.
             */
            expressionParser.populateErrorMap("Failed to execute validation expression with valId : " + exprId);
            LOGGER.error("Failed to execute validation expression with valId : " + exprId);
            e.printStackTrace();
            expJobLink.put(exprId,Boolean.TRUE);
        }finally {
        	LOGGER.info(exprId+" Total validation Execution time:"+((System.currentTimeMillis()-startTime)/1000));
        }
    }

    private String replaceOriginalExpression(String originalExpr, MetadataBuilder metadataBuilder, ExpressionParser ep,
                                             Integer exprId) {

        Map<String, Map<String, String>> mainColumnData = metadataBuilder.getMainColumnData();
        Map<String, String> tableAliasData = metadataBuilder.getTableAliasData();
        List<ExpressionEntityDetail> eed = ep.expressionEntityDetailsMap.get(exprId);
        Map<String, String> columnMap = expressionToColumnsMap.get(exprId);
        Set<String> tableSet = expressionToTablesMap.get(exprId);

        for (ExpressionEntityDetail detail : eed) {
            String entityType = detail.getEntityType();

            if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                ReturnEntityDetail entityDetail = (ReturnEntityDetail) detail;
                Map<String, ReturnMetadataInfo> metaDataInfoMap = new HashMap<>();

                String expKey = entityType + ".\"" + entityDetail.getEntityCode() + "\".\"" + entityDetail.getSectionDesc() + "\"";
                String mapKey = entityType + "." + entityDetail.getEntityCode() + "." + entityDetail.getSectionDesc();

                if (originalExpr.contains(expKey)) {
                    tableSet.add(tableAliasData.get(mapKey));
                }

                for (String col : entityDetail.getEntityElements()) {
                    replaceDynamicFilterColumn(expKey, mapKey, col, columnMap, mainColumnData, tableAliasData);

                    if (originalExpr.contains(expKey + "." + col)) {
                        columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);
                    }

                    originalExpr = ValidationStringUtils.replace(originalExpr, (expKey + "." + col),
                            (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);

                    String patternString = "\"[\\s]+(?i)AS[\\s]+\"";
                    Pattern pattern = Pattern.compile(patternString);
                    Matcher matcher = pattern.matcher(originalExpr);
                    boolean matches = matcher.find();
                    
                    if(matches) {
                    	String[] originalExpArr = originalExpr.split("[\\s]+(?i)AS[\\s]+");
                    	Integer i = 1;
                    	while(i<originalExpArr.length) {
                    		originalExpArr[i] = ValidationConstants.ME+"."+originalExpArr[i];
                    		i=i+2;
                    	}
                    	
                    	originalExpr = String.join(" AS ", originalExpArr);
                    }

                    if (entityDetail.isMePresent()) {

                        //Do not change the order of these lines
                        //1
                        if (originalExpr.contains(ValidationConstants.ME_IDENTIFIER + "." + col)) {
                            columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);
                        }

                        //2
                        originalExpr = ValidationStringUtils.replace(originalExpr, (ValidationConstants.ME_IDENTIFIER + "." + col),
                                (ValidationConstants.ME_IDENTIFIER + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);
                    }

                    for (Map.Entry<String, ReturnMetadataInfo> entry : entityDetail.getMetaDataInfoMap().entrySet()) {
                        String entryKey = entry.getKey();
                        String newKey = "";

                        if (entryKey.startsWith(expKey + "." + col)) {
                            newKey = ValidationStringUtils.replace(entryKey, (expKey + "." + col),
                                    (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);
                        } else {
                            // todo -> what to do here??
                        }

                        if (!newKey.equals("")) {
                            metaDataInfoMap.put(newKey, entry.getValue());
                        }
                    }
                }

                String metaDataValues = "";
                for (Map.Entry<String, ReturnMetadataInfo> entry : metaDataInfoMap.entrySet()) {
                    ReturnMetadataInfo info = entry.getValue();

                    metaDataValues = entityType.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId() + ValidationConstants.VALUEDELIMITER
                            + info.getReportVersion() + ValidationConstants.VALUEDELIMITER + info.getVersionNo() + ValidationConstants.VALUEDELIMITER
                            + info.getPeriodId() + ValidationConstants.VALUEDELIMITER + entry.getKey();

                    if (originalExpr.contains(metaDataValues)) {
                        if (!metaDataValues.endsWith(")")) {
                            originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey() + ","),
                                    (metaDataValues + ","), -1, true);
                        }
                    } else {
                        originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey()),
                                (metaDataValues), -1, true);
                    }
                }
            } else if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {

                RefEntityDetail entityDetail = (RefEntityDetail) detail;
                Map<String, RefMetadataInfo> metaDataInfoMap = new HashMap<>();

                String expKey = entityType + ".\"" + entityDetail.getEntityCode() + "\"";
                String mapKey = entityType + "." + entityDetail.getEntityCode();

                if (originalExpr.contains(expKey)) {
                    tableSet.add(tableAliasData.get(mapKey));
                }

                for (String col : entityDetail.getEntityElements()) {
                    replaceDynamicFilterColumn(expKey, mapKey, col, columnMap, mainColumnData, tableAliasData);

                    if (originalExpr.contains(expKey + "." + col)) {
                        columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);
                    }

                    originalExpr = ValidationStringUtils.replace(originalExpr, (expKey + "." + col),
                            (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);

                    if (entityDetail.isMePresent()) {

                        //Do not change the order of these lines
                        //1
                        if (originalExpr.contains(ValidationConstants.ME_IDENTIFIER + "." + col)) {
                            columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);
                        }

                        //2
                        originalExpr = ValidationStringUtils.replace(originalExpr, (ValidationConstants.ME_IDENTIFIER + "." + col),
                                (ValidationConstants.ME_IDENTIFIER + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);
                    }

                    for (Map.Entry<String, RefMetadataInfo> entry : entityDetail.getMetaDataInfoMap().entrySet()) {
                        String entryKey = entry.getKey();
                        String newKey = "";

                        if (entryKey.startsWith(expKey + "." + col)) {
                            newKey = ValidationStringUtils.replace(entryKey, (expKey + "." + col),
                                    (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);
                        } else {
                            // todo -> what to do here??
                        }

                        if (!newKey.equals("")) {
                            metaDataInfoMap.put(newKey, entry.getValue());
                        }
                    }
                }

                String metaDataValues = "";
                for (Map.Entry<String, RefMetadataInfo> entry : metaDataInfoMap.entrySet()) {
                    RefMetadataInfo info = entry.getValue();

                    metaDataValues = entityType.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId() + ValidationConstants.VALUEDELIMITER
                            + info.getPeriodId() + ValidationConstants.VALUEDELIMITER + entry.getKey();

                    if (originalExpr.contains(metaDataValues)) {
                        if (!metaDataValues.endsWith(")")) {
                            originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey() + ","),
                                    (metaDataValues + ","), -1, true);
                        }
                    } else {
                        originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey()),
                                (metaDataValues), -1, true);
                    }
                }
            } else if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {

            	ValidationEntityDetail entityDetail = (ValidationEntityDetail) detail;
                Map<String, EntityMetadataInfo> metaDataInfoMap = new HashMap<>();

                String expKey = entityType + ".\"" + entityDetail.getSubjectArea() + "\""+ ".\"" + entityDetail.getDdTableName() + "\"";
                String mapKey = entityType + "." + entityDetail.getSubjectArea()+"."+entityDetail.getDdTableName();

                if (originalExpr.contains(expKey)) {
                    tableSet.add(tableAliasData.get(mapKey));
                }

                for (String col : entityDetail.getEntityElements()) {
                    replaceDynamicFilterColumn(expKey, mapKey, col, columnMap, mainColumnData, tableAliasData);

                    if (originalExpr.contains(expKey + "." + col)) {
                        columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);
                    }

                    originalExpr = ValidationStringUtils.replace(originalExpr, (expKey + "." + col),
                            (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);

                    if (entityDetail.isMePresent()) {

                        //Do not change the order of these lines
                        //1
                        if (originalExpr.contains(ValidationConstants.ME_IDENTIFIER + "." + col)) {
                            columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);
                        }

                        //2
                        originalExpr = ValidationStringUtils.replace(originalExpr, (ValidationConstants.ME_IDENTIFIER + "." + col),
                                (ValidationConstants.ME_IDENTIFIER + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);
                    }

                    for (Map.Entry<String, EntityMetadataInfo> entry : entityDetail.getMetaDataInfoMap().entrySet()) {
                        String entryKey = entry.getKey();
                        String newKey = "";

                        if (entryKey.startsWith(expKey + "." + col)) {
                            newKey = ValidationStringUtils.replace(entryKey, (expKey + "." + col),
                                    (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())), -1, true);
                        } else {
                            // todo -> what to do here??
                        }

                        if (!newKey.equals("")) {
                            metaDataInfoMap.put(newKey, entry.getValue());
                        }
                    }
                }

                String metaDataValues = "";
                for (Map.Entry<String, EntityMetadataInfo> entry : metaDataInfoMap.entrySet()) {
                	EntityMetadataInfo info = entry.getValue();

                    metaDataValues = entityType.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId() + ValidationConstants.VALUEDELIMITER
                            + info.getPeriodId() + ValidationConstants.VALUEDELIMITER + entry.getKey();

                    if (originalExpr.contains(metaDataValues)) {
                        if (!metaDataValues.endsWith(")")) {
                            originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey() + ","),
                                    (metaDataValues + ","), -1, true);
                        }
                    } else {
                        originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey()),
                                (metaDataValues), -1, true);
                    }
                }
            }
        }

        // for foreach
    	Map<Integer, List<Map<String, Object>>> rtnTableDetailsInfo = new HashMap<>();
		Map<Integer, List<Map<String, Object>>> refTableDetailsInfo = new HashMap<>();
		Map<Integer, List<Map<String, Object>>> ddTableDetailsInfo = new HashMap<>();

		for (ExpressionEntityDetail data : eed) {
			String type = data.getEntityType();
			if (type.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {

				ReturnEntityDetail entityDetail = (ReturnEntityDetail) data;

				String key = type + ".\"" + entityDetail.getEntityCode() + "\".\"" + entityDetail.getSectionDesc()
						+ "\"";
				String mapKey = type + "." + entityDetail.getEntityCode() + "." + entityDetail.getSectionDesc();

				for (String exprKey : entityDetail.getMetaDataInfoMap().keySet()) {
					Integer length = exprKey.length();
					if (rtnTableDetailsInfo.get(length) == null) {
						rtnTableDetailsInfo.put(length, new ArrayList<>());
					}
					Map<String, Object> info = new HashMap<>();
					info.put("key", key);
					info.put("mapKey", mapKey);
					info.put("exprKey", exprKey);
					info.put("info", entityDetail.getMetaDataInfoMap().get(exprKey));
					rtnTableDetailsInfo.get(length).add(info);
				}
			} else if (type.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {

				RefEntityDetail entityDetail = (RefEntityDetail) data;
				String key = type + ".\"" + entityDetail.getEntityCode() + "\"";
				String mapKey = type + "." + entityDetail.getEntityCode();

				for (String exprKey : entityDetail.getMetaDataInfoMap().keySet()) {
					Integer length = exprKey.length();
					if (refTableDetailsInfo.get(length) == null) {
						refTableDetailsInfo.put(length, new ArrayList<>());
					}
					Map<String, Object> info = new HashMap<>();
					info.put("key", key);
					info.put("mapKey", mapKey);
					info.put("exprKey", exprKey);
					info.put("info", entityDetail.getMetaDataInfoMap().get(exprKey));
					refTableDetailsInfo.get(length).add(info);
				}
			} else if (type.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {

				ValidationEntityDetail entityDetail = (ValidationEntityDetail) data;
				String key = type + ".\"" + entityDetail.getSubjectArea() + "\""+ ".\"" + entityDetail.getEntityCode() + "\"";
				String mapKey = type + "." + entityDetail.getSubjectArea()+ "." + entityDetail.getEntityCode() ;
				

				for (String exprKey : entityDetail.getMetaDataInfoMap().keySet()) {
					Integer length = exprKey.length();
					if (ddTableDetailsInfo.get(length) == null) {
						ddTableDetailsInfo.put(length, new ArrayList<>());
					}
					Map<String, Object> info = new HashMap<>();
					info.put("key", key);
					info.put("mapKey", mapKey);
					info.put("exprKey", exprKey);
					info.put("info", entityDetail.getMetaDataInfoMap().get(exprKey));
					ddTableDetailsInfo.get(length).add(info);
				}
			}
		}

		List<Integer> rtnKeys = new ArrayList<>(rtnTableDetailsInfo.keySet());
		List<Integer> refKeys = new ArrayList<>(refTableDetailsInfo.keySet());
		List<Integer> ddKeys = new ArrayList<>(ddTableDetailsInfo.keySet());

		if (!rtnKeys.isEmpty()) {
			String type = ValidationConstants.TYPE_RETURN;
			Collections.sort(rtnKeys, Collections.reverseOrder());
			for (Integer lenKey : rtnKeys) {
				for (Map<String, Object> dataInfo : rtnTableDetailsInfo.get(lenKey)) {
//					String key = (String) dataInfo.get("key");
					String mapKey = (String) dataInfo.get("mapKey");
					String metaDataValues = "";
					String exprKey = (String) dataInfo.get("exprKey");
					ReturnMetadataInfo info = (ReturnMetadataInfo) dataInfo.get("info");
					metaDataValues = type.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId()
							+ ValidationConstants.VALUEDELIMITER + info.getReportVersion()
							+ ValidationConstants.VALUEDELIMITER + info.getVersionNo()
							+ ValidationConstants.VALUEDELIMITER + info.getPeriodId()
							+ ValidationConstants.VALUEDELIMITER + tableAliasData.get(mapKey);
					originalExpr = ValidationStringUtils.replace(originalExpr, (exprKey), (metaDataValues), -1, true);

				}
			}
		}

		if (!refKeys.isEmpty()) {
			String type = ValidationConstants.TYPE_REFTABLE;
			Collections.sort(refKeys, Collections.reverseOrder());
			for (Integer lenKey : refKeys) {
				for (Map<String, Object> dataInfo : refTableDetailsInfo.get(lenKey)) {
//					String key = (String) dataInfo.get("key");
					String mapKey = (String) dataInfo.get("mapKey");
					String metaDataValues = "";
					String exprKey = (String) dataInfo.get("exprKey");
					RefMetadataInfo info = (RefMetadataInfo) dataInfo.get("info");
					metaDataValues = type.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId()
							+ ValidationConstants.VALUEDELIMITER + info.getPeriodId()
							+ ValidationConstants.VALUEDELIMITER + tableAliasData.get(mapKey);

					originalExpr = ValidationStringUtils.replace(originalExpr, (exprKey), (metaDataValues), -1, true);
				}

			}
		}

		if (!ddKeys.isEmpty()) {
			String type = ValidationConstants.TYPE_ENTITY;
			Collections.sort(ddKeys, Collections.reverseOrder());
			for (Integer lenKey : ddKeys) {
				for (Map<String, Object> dataInfo : ddTableDetailsInfo.get(lenKey)) {
//					String key = (String) dataInfo.get("key");
					String mapKey = (String) dataInfo.get("mapKey");
					String metaDataValues = "";
					String exprKey = (String) dataInfo.get("exprKey");
					EntityMetadataInfo info = (EntityMetadataInfo) dataInfo.get("info");
					metaDataValues = type.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId()
							+ ValidationConstants.VALUEDELIMITER + info.getPeriodId()
							+ ValidationConstants.VALUEDELIMITER + tableAliasData.get(mapKey);

					originalExpr = ValidationStringUtils.replace(originalExpr, (exprKey), (metaDataValues), -1, true);
				}

			}
		}
		
        return originalExpr;
    }

    private void replaceDynamicFilterColumn(String expKey, String mapKey, String col, Map<String, String> columnMap,
                                            Map<String, Map<String, String>> mainColumnData, Map<String, String> tableAliasData) {
        if (dynamicFilters != null && !dynamicFilters.isEmpty()) {
            dynamicFilters.forEach(filter -> {
                if (filter.getColumnName().contains(expKey + "." + col)) {
                    columnMap.put(mainColumnData.get(mapKey).get(col.toUpperCase()), col);

                    filter.setColumnName(ValidationStringUtils.replace(filter.getColumnName(), (expKey + "." + col),
                            (tableAliasData.get(mapKey) + "." + mainColumnData.get(mapKey).get(col.toUpperCase())),
                            -1, true));
                }
            });
        }
    }

//    @NewSpan("validationExpressionJob")
//    private void runJob(@SpanTag("expressionId")Integer exprId, Integer runId, DataReader dataReader) {
//        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId +
//                File.separator;
//        outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);
//
//        File directory = new File(outputDirectory);
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//        String fileName = "Validation_Result_" + runId + "_" + exprId + ".csv";
//        DataWriter dataWriter = new CSVWriter(new File(outputDirectory + fileName)).setFieldNamesInFirstRow(true);
//
//        Job job = new Job(dataReader, dataWriter);
////        expJobLink.put(exprId, job);
////        pipelinerunningJobs.add(pipelinePool.submit(job));
//    }
	
}
