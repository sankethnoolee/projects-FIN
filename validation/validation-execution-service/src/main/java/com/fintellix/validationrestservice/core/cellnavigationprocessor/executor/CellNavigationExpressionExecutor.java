package com.fintellix.validationrestservice.core.cellnavigationprocessor.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fintellix.framework.validation.dto.ValidationLineItemLink;
import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.validationrestservice.core.cellnavigationprocessor.CellNavigationExpressionParser;
import com.fintellix.validationrestservice.core.cellnavigationprocessor.CellNavigationFunctions;
import com.fintellix.validationrestservice.core.cellnavigationprocessor.CellNavigationMetadataBuilder;
import com.fintellix.validationrestservice.core.cellnavigationprocessor.CellNavigationMetadataResolver;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;
import com.fintellix.validationrestservice.core.parser.prefix.ast.AstNode;
import com.fintellix.validationrestservice.definition.ExpressionEntityDetail;
import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.definition.QueryBuilder;
import com.fintellix.validationrestservice.definition.RefEntityDetail;
import com.fintellix.validationrestservice.definition.RefMetadataInfo;
import com.fintellix.validationrestservice.definition.ReturnEntityDetail;
import com.fintellix.validationrestservice.definition.ReturnMetadataInfo;
import com.fintellix.validationrestservice.definition.SubExpressionMetaData;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;

public class CellNavigationExpressionExecutor {
    private Integer exprId;
    private Map<Integer, Map<String, String>> expressionToColumnsMap = new HashMap<>();
    private Map<Integer, Set<String>> expressionToTablesMap = new HashMap<>();
    private Map<Integer, String> expressionIdMap = new HashMap<>();
    private CellNavigationMetadataBuilder metadataBuilder;
    private CellNavigationExpressionParser expressionParser;
    private CellNavigationMetadataResolver resolver;
    private Integer systemSolutionId;
    private Integer runId;
    private Map<Integer, ValidationMaster> vmMap = new HashMap<>();
    
    public void init(Integer exprId, Map<Integer, Map<String, String>> expressionToColumnsMap,
                     Map<Integer, Set<String>> expressionToTablesMap, Map<Integer, String> expressionIdMap,
                     CellNavigationMetadataBuilder metadataBuilder, CellNavigationExpressionParser expressionParser,
                     CellNavigationMetadataResolver resolver, Integer systemSolutionId, Integer runId, Map<Integer, ValidationMaster> vmMap) {
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
        
    }

    public void executeRequest() {
        QueryBuilder builder = new QueryBuilder();
        CellNavigationFunctions functions = new CellNavigationFunctions();
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
                    metadataBuilder.getAliaisedLineColumnData(),exprId,systemSolutionId,vmMap.get(exprId).getSequenceNo());

            if (expAn.getToken().getType().equals(TokenType.FUNCTION)
                    && expAn.getToken().getValue().equalsIgnoreCase("FOREACH")) {
                expressionParser.processForeachNode(expAn, builder, functions);
                isForEachPresent = true;
            }

            expressionParser.processExpressionNode(expAn, new HashMap<>(), originalExpr, builder, functions);

            Map<String, Integer> columnMetaData =  new HashMap<>();
            //resolver.getMetadata("select * from (" + builder.getQuery().trim() + ") as t2 where 1=2", systemSolutionId);

            /* creating dimension map from dimensions given at FOR-EACH level */
            Map<String, String> groupByCols = new TreeMap<>();
            for (String groupBy : builder.getGroupBy()) {
                groupBy = groupBy.replace(builder.getBaseTableName() + ".", "");
                groupByCols.put(groupBy, expressionToColumnsMap.get(exprId).get(groupBy));
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

            
            functions.addIndividualCols(builder.getExpression(),builder);
            functions.delete();
            
            //saveValidationLineLinkage(functions);
            
        } catch (Throwable e) {
            /*
             * In case of exception during parallel processing of Job, code computation will never reach this block.
             * This is handled by populating 'expJobLink' map for every Job.
             */
            expressionParser.populateErrorMap("Failed to execute validation expression with valId : " + exprId);
            e.printStackTrace();
        }
    }

    
    private void saveValidationLineLinkage(CellNavigationFunctions functions) throws Throwable {
        
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = PersistentStoreManager.getConnection();
    		ps = conn.prepareStatement("INSERT INTO VALIDATION_LINE_ITEM_LINK ("
    				+ "VALIDATION_ID,SEQUENCE_NO,SOLUTION_ID,REG_REPORT_ID,SECTION_ID,LINE_ITEM_NAME"
    				//+ ",SECTION_TYPE"
    				+ ") VALUES ("
    				+ "?,?,?,?,?,?"
    				//+ ",?"
    				+ ")");
        	Integer counter = 0;
        	Integer parameterIndex = 1;
    		
    			ValidationMaster vm = vmMap.get(exprId);
    			ExpressionMetaData emd = functions.getExpressionMetaData();

    			List<ValidationLineItemLink> vllList = new ArrayList<>();
    			

    				List<String> entityCols = emd.getEntityCols();
    				if (entityCols != null && !entityCols.isEmpty()) {

    					for (String colName : entityCols) {

    						if (emd.getReturnType().equalsIgnoreCase("GRID")) {
    							colName = colName.replace("A_", "");
    						} else {

    							if (emd.getColumnInfo().get(colName) != null) {
    								colName = emd.getColumnInfo().get(colName).get("BUSSINESS_NAME");
    							} else {
    								// col info not available

    							}
    						}
    						
    						if (colName != null) {
//    							VALIDATION_ID,SEQUENCE_NO,SOLUTION_ID,REG_REPORT_ID,SECTION_ID,LINE_ITEM_NAME,SECTION_TYPE
    							parameterIndex = 1;
    							ps.clearParameters();
    							ps.setInt(parameterIndex++, exprId);
    							ps.setInt(parameterIndex++, vm.getSequenceNo());
    							ps.setInt(parameterIndex++, systemSolutionId);
    							ps.setInt(parameterIndex++, emd.getReportId());
    							ps.setInt(parameterIndex++, emd.getSectionId());
    							
    							ps.setString(parameterIndex++, colName.trim().toUpperCase());
    					//		ps.setString(parameterIndex++, emd.getReturnType().trim());
    							
    							ps.addBatch();
    							ps.clearParameters();
    							
    							counter++;
    						}
    					}
    					
    					if(emd.getGroupByDetailsBySubExpr() != null && !emd.getGroupByDetailsBySubExpr().isEmpty()) {
    						List<SubExpressionMetaData> semdList  = new ArrayList<>();
    						
    						for(SubExpressionMetaData semd:semdList) {
    							String colName = semd.getLineItemCode();
    							
    							if (semd.getReturnType().equalsIgnoreCase("GRID")) {
        							colName = colName.replace("A_", "");
        						}
    							
    							parameterIndex = 1;
    							ps.clearParameters();
    							ps.setInt(parameterIndex++, exprId);
    							ps.setInt(parameterIndex++, vm.getSequenceNo());
    							ps.setInt(parameterIndex++, systemSolutionId);
    							ps.setInt(parameterIndex++, semd.getReportId());
    							ps.setInt(parameterIndex++, semd.getSectionId());
    							
    							ps.setString(parameterIndex++, colName.trim().toUpperCase());
    						//	ps.setString(parameterIndex++, semd.getReturnType().trim());
    							
    							ps.addBatch();
    							ps.clearParameters();
    							
    							counter++;
    						}
    					}
    				}

    			

    			if(counter>0) {
    				ps.executeBatch();
    				conn.commit();
    				ps.clearBatch();
    			}

    		   		
    	}catch (Exception e) {
    		try {
				conn.rollback();
			} catch (Exception e1) {
				//do-nothing
			}
		}finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					//do-nothing
				}
			}
			if(conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					//do-nothing
				}
			}
		}
    }

    private String replaceOriginalExpression(String originalExpr, CellNavigationMetadataBuilder metadataBuilder, CellNavigationExpressionParser ep,
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


            }
        }

        // for foreach
        for (ExpressionEntityDetail data : eed) {
            String type = data.getEntityType();

            if (type.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                ReturnEntityDetail entityDetail = (ReturnEntityDetail) data;
                String key = type + ".\"" + entityDetail.getEntityCode() + "\".\"" + entityDetail.getSectionDesc() + "\"";
                String mapKey = type + "." + entityDetail.getEntityCode() + "." + entityDetail.getSectionDesc();
                String metaDataValues = "";

                for (Map.Entry<String, ReturnMetadataInfo> entry : entityDetail.getMetaDataInfoMap().entrySet()) {
                    ReturnMetadataInfo info = entry.getValue();

                    metaDataValues = type.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId() + ValidationConstants.VALUEDELIMITER
                            + info.getReportVersion() + ValidationConstants.VALUEDELIMITER + info.getVersionNo() + ValidationConstants.VALUEDELIMITER
                            + info.getPeriodId() + ValidationConstants.VALUEDELIMITER
                            + ValidationStringUtils.replace(entry.getKey(), key, (tableAliasData.get(mapKey)), -1, true);

                    originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey()),
                            (metaDataValues), -1, true);

                }
            } else if (type.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
                RefEntityDetail entityDetail = (RefEntityDetail) data;
                String key = type + ".\"" + entityDetail.getEntityCode() + "\"";
                String mapKey = type + "." + entityDetail.getEntityCode();
                String metaDataValues = "";

                for (Map.Entry<String, RefMetadataInfo> entry : entityDetail.getMetaDataInfoMap().entrySet()) {
                    RefMetadataInfo info = entry.getValue();

                    metaDataValues = type.toUpperCase() + ValidationConstants.VALUEDELIMITER + info.getOrgId() + ValidationConstants.VALUEDELIMITER
                            + info.getPeriodId() + ValidationConstants.VALUEDELIMITER
                            + ValidationStringUtils.replace(entry.getKey(), key, (tableAliasData.get(mapKey)), -1, true);

                    originalExpr = ValidationStringUtils.replace(originalExpr, (entry.getKey()),
                            (metaDataValues), -1, true);

                }
            }
        }

        return originalExpr;
    }
}
