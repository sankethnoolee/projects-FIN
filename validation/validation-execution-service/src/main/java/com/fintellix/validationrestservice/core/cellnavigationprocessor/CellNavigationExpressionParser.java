package com.fintellix.validationrestservice.core.cellnavigationprocessor;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.validationrestservice.core.cellnavigationprocessor.executor.CellNavigationExpressionExecutor;
import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;
import com.fintellix.validationrestservice.core.lexer.tokenizer.CharacterStream;
import com.fintellix.validationrestservice.core.lexer.tokenizer.ExpressionTokenizer;
import com.fintellix.validationrestservice.core.lexer.tokenizer.ExpressionTokenizerForSpELFunctions;
import com.fintellix.validationrestservice.core.parser.ExpressionParserForSpELFunctions;
import com.fintellix.validationrestservice.core.parser.prefix.PrefixParser;
import com.fintellix.validationrestservice.core.parser.prefix.ast.AstNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.FunctionNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.VariableNode;
import com.fintellix.validationrestservice.definition.ExpressionEntityDetail;
import com.fintellix.validationrestservice.definition.ExpressionResultDetail;
import com.fintellix.validationrestservice.definition.QueryBuilder;
import com.fintellix.validationrestservice.definition.RefEntityDetail;
import com.fintellix.validationrestservice.definition.RefMetadataInfo;
import com.fintellix.validationrestservice.definition.ReturnEntityDetail;
import com.fintellix.validationrestservice.definition.ReturnMetadataInfo;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.exception.BaseValidationException;
import com.fintellix.validationrestservice.util.CalendarProcessor;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.vo.RunRecordDetail;
import com.northconcepts.datapipeline.job.Job;

public class CellNavigationExpressionParser implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Set<String> AGG_VLOOKUP_FUNCTIONS = new HashSet<>(
            Arrays.asList("VLOOKUP", "SUMIF", "MAXIF", "MINIF", "COUNTIF", "SUM", "MAX", "MIN", "COUNT", "AVG"));
    private static final Set<String> ART_LOG_FUNC = new HashSet<>(Arrays.asList("+", "-", "/", "*", ">", "<", "=", "!", ":", "?","{","}","(",")"));
    private static Map<String, String> nameOperatorConflitMap = new HashMap<>();

    static {
        for (String s : ART_LOG_FUNC) {
            nameOperatorConflitMap.put(s, "###" + (int) (s.charAt(0)) + "###");
        }
    }

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public Map<Integer, List<ExpressionEntityDetail>> expressionEntityDetailsMap = new HashMap<>();

    // System variables
    private Integer systemRegReportId;
    private String systemRegReportName;
    private Integer systemSolutionId;
    private String systemOrgCode;
    private Integer systemOrgId;
    private Integer systemPeriodId;
    private Integer systemRegReportVersion;
    private Integer systemVersionNo;
    private String systemReturnStatus;

    private Integer runId;
    private Integer expressionId;
    private Map<Integer, String> expressionIdMap = new HashMap<>();
    private Map<Integer, ValidationMaster> vmMap = new HashMap<>();
    private Map<String, String> meMap = new HashMap<>();
    private Map<String, List<String>> metadataErrorMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, String>> expressionToColumnsMap = new HashMap<>();
    private Map<Integer, Set<String>> expressionToTablesMap = new HashMap<>();

    private Map<Integer, Job> expJobLink = new ConcurrentHashMap<>();

    private Map<Integer, Boolean> failedMetaDataExp = new HashMap<>();

   

    
    public void init(Map<Integer, String> expressionIdMap, Map<Integer, ValidationMaster> vmMap) {
        ValidationMaster vm = vmMap.get(new ArrayList<>(vmMap.keySet()).get(0));
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
        this.expressionIdMap = expressionIdMap;
        this.systemSolutionId =vm.getSolutionId();
        
        this.systemRegReportName = vm.getEntityCode().trim();
        this.systemPeriodId = Integer.parseInt(s.format(vm.getStartDate()));
        
        this.runId = 1;
        this.vmMap = vmMap;
        
        CellNavigationMetadataResolver resolver = new CellNavigationMetadataResolver();
        resolver.setSolutionId(systemSolutionId);

        this.systemRegReportId = resolver.getRegReportId(systemRegReportName, systemPeriodId, systemSolutionId);
        this.systemOrgId = resolver.getOrgId(systemRegReportId, "", systemPeriodId, systemSolutionId);
        
        this.systemOrgCode = resolver.getOrgCode(systemOrgId, systemSolutionId);
        
        
        Object[] data = resolver.getReportVersionAndVersionNo(systemOrgId, systemRegReportId, "", systemPeriodId);
        
        if(data != null && data.length>0) {
        	systemRegReportVersion = Integer.parseInt(data[1].toString());
        	systemVersionNo = Integer.parseInt(data[0].toString());
        }
        
        
        try {
            if (!expressionIdMap.isEmpty()) {
                processRequest(resolver);
            }

            
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void processRequest(CellNavigationMetadataResolver resolver) {
        CellNavigationMetadataBuilder metadataBuilder = new CellNavigationMetadataBuilder();
        try {
            /*
             * for populating global Maps for expression
             */
            expressionIdMap.forEach((validationId, expression) -> {
                this.expressionId = validationId;
                QueryBuilder builder = new QueryBuilder();
                CellNavigationFunctions functions = new CellNavigationFunctions();
                AstNode parsedNode = tokenizeAndParseIntoTree(expression);

                if (parsedNode.getToken().getType().equals(TokenType.FUNCTION)
                        && parsedNode.getToken().getValue().equalsIgnoreCase("FOREACH")) {
                    processForEachForExpressionMap(parsedNode, builder, functions, validationId);
                }

                processNodeForExpressionsMap(parsedNode, new HashMap<String, String>(), expression,
                        builder, functions, validationId);
            });

            /*
             * Execution of expressions
             */
            metadataBuilder.createDataSet(expressionEntityDetailsMap, systemSolutionId, systemOrgCode, systemOrgId,
                    systemPeriodId, systemRegReportVersion, systemVersionNo, systemRegReportId, runId);

            Map<Integer, List<RunRecordDetail>> runRecordDetails = new ConcurrentHashMap<>();

            for (Integer exprId : expressionIdMap.keySet()) {
                if (failedMetaDataExp.get(exprId) == null) {
                    this.expressionId = exprId;
                    expressionToColumnsMap.put(exprId, new HashMap<>());
                    expressionToTablesMap.put(exprId, new HashSet<>());
                    runRecordDetails.computeIfAbsent(exprId, i -> new CopyOnWriteArrayList<>());

                    CellNavigationExpressionExecutor expressionExecutor = new CellNavigationExpressionExecutor();
                    expressionExecutor.init(exprId, expressionToColumnsMap, expressionToTablesMap, expressionIdMap,
                            metadataBuilder, this, resolver, systemSolutionId, runId, vmMap);
                    
                    
                    expressionExecutor.executeRequest();
                }
            }

            for (Integer expId : expJobLink.keySet()) {
                if (expJobLink.get(expId).isFailed()) {
                    // setting the error flag at the first index.
                    if (!runRecordDetails.isEmpty() && runRecordDetails.get(expId) != null) {
                        if (!runRecordDetails.get(expId).isEmpty()) {
                            runRecordDetails.get(expId).get(0).setHasError(true);
                        } else {
                            runRecordDetails.get(expId).add(new RunRecordDetail(false, "", true, ""));
                        }
                    }
                    populateErrorMap("Failed to execute validation expression with valId : " + expId);
                    
                }
            }


            for (Integer expId : failedMetaDataExp.keySet()) {
                runRecordDetails.put(expId, new ArrayList<>());
                runRecordDetails.get(expId).add(new RunRecordDetail(false, "", true, ""));
                populateErrorMap("Failed to execute validation expression with valId : " + expId);
                
            }

            /* Saving validation run details */
            List<ValidationRunDetails> validationRunDetailsList = new ArrayList<>();
            List<RunRecordDetail> runRecordDetailsList;

            for (Integer exprId : expressionIdMap.keySet()) {
                runRecordDetailsList = runRecordDetails.get(exprId);
                validationRunDetailsList.add(getValidationResultsObject(runId, vmMap.get(exprId), runRecordDetailsList));
            }

        } catch (BaseValidationException ex) {
            populateErrorMap(ex.getMessage());
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        } catch (Throwable th) {
            populateErrorMap("Failed to process request with runId : " + runId);
            th.printStackTrace();
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

            }
        }
        return prefixParser.parse(tokenReplacedWithOriginalChars);
    }

    public void processForeachNode(AstNode an, QueryBuilder builder, CellNavigationFunctions functions) {
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
                        "", builder, functions)[2]).getExpressionDetails().getExpression()
                        + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // filterClause.delete(filterClause.length() - 1, filterClause.length());
                filterClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions)[2]).getExpressionDetails().getExpression() + ",");
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

        functions.forEachBuilder(tableName, groupByCondition, filterCondition, builder);
    }

    public Object[] processExpressionNode(AstNode an, Map<String, String> variablesAndExpressionMap,
                                          String replacedExpr, QueryBuilder builder, CellNavigationFunctions functions) {
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
                    case "COUNTIF":
                    case "SUM":
                    case "MAX":
                    case "MIN":
                    case "COUNT":
                    case "AVG":
                        an.getExpressionDetails()
                                .setExpression(resolveAggregateFunction(an, hexVal, builder, functions));
                        builder.setExpression(an.getExpressionDetails().getExpression());
                        break;
                    case "VLOOKUP":
                        an.getExpressionDetails().setExpression(resolveLookup(children, hexVal, builder, functions));
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
                        functions);
                replacedExpr = (String) temp[1];
                currentProccessedNode = (AstNode) temp[2];
                functionExpAndIndex.put(currentProccessedNode, an.getChildren().indexOf(a));
            }

            functionExpAndIndex.forEach((key, value) -> {
                an.getChildren().remove(an.getChildren().get(value));
                an.getChildren().add(value, key);
            });

            return processExpressionNode(an, variablesAndExpressionMap, replacedExpr, builder, functions);
        }
    }

    private String resolveLookup(List<AstNode> children, String hexVal, QueryBuilder builder,
                                 CellNavigationFunctions functions) {
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
                                "", builder, functions)[2]).getExpressionDetails().getExpression() + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions)[2]).getExpressionDetails().getExpression() + ",");
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

        return functions.lookUpBuilder(sourceTable, targetTable, joinCondition, outputColumns,
                filterCondition, builder, hexVal, expressionToColumnsMap.get(expressionId));
    }

    private String resolveAggregateFunction(AstNode an, String hexVal, QueryBuilder builder,
                                            CellNavigationFunctions functions) {
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
                        "", builder, functions)[2]).getExpressionDetails().getExpression()
                        + "),");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(chilNodes.get(i), new HashMap<String, String>(), "",
                                builder, functions)[2]).getExpressionDetails().getExpression() + ",");
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
                    case "INDEXOF":
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
        CellNavigationFunctions functions = new CellNavigationFunctions();
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
        List<Token> tokenReplacedWithOriginalChars = expressionTokenizer.tokenize(new CharacterStream(inputStr));
        regEx = Pattern.compile("###(\\d+)###");

        for (Token t : tokenReplacedWithOriginalChars) {
            if (t.getType().equals(TokenType.VARIABLE)) {
                matcher = regEx.matcher(t.getValue());
                while (matcher.find()) {
                    MatchedStr = matcher.group();
                    t.setValue(ValidationStringUtils.replace(t.getValue(), MatchedStr, (String.valueOf(Character.toChars(Integer.parseInt(MatchedStr.replace("###", ""))))), -1, true));
                }
            }
        }

        AstNode expAn = prefixParser.parse(tokenReplacedWithOriginalChars);
        if (expAn.getToken().getType().equals(TokenType.FUNCTION)
                && expAn.getToken().getValue().equalsIgnoreCase("FOREACH")) {
            processForeachNode(expAn, builder, functions);
        }

        processExpressionNodeForSpelExpressions(expAn, new HashMap<String, String>(), expression, builder);

        return builder.getExpression();
    }

    private void processForEachForExpressionMap(AstNode an, QueryBuilder builder, CellNavigationFunctions functions,
                                                Integer validationId) {

        List<AstNode> children = an.getChildren();
        String vLookupSrc = children.get(0).getToken().getType() == TokenType.VARIABLE
                ? children.get(0).getToken().getValue()
                : "";
        formExpressionEntityDetailMap(vLookupSrc,
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

            // only process for type return.
            String[] arrayOfParams = vLookupSrc.split("\\.");
            String tableName = arrayOfParams[1];
            meMap.put(ValidationConstants.ME, ValidationConstants.TYPE_REFTABLE + "." + tableName);
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
                        new HashMap<String, String>(), "", builder, functions)[2]).getExpressionDetails()
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

        for (String filter : filterCondition) {
            formExpressionEntityDetailMap(filter, null, validationId, true, false);
        }
    }

    private Object[] processNodeForExpressionsMap(AstNode an, Map<String, String> variablesAndExpressionMap,
                                                  String replacedExpr, QueryBuilder builder,
                                                  CellNavigationFunctions functions, Integer validationId) {
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
                    case "COUNTIF":
                    case "SUM":
                    case "MAX":
                    case "MIN":
                    case "COUNT":
                    case "AVG":
                        processAggregateFunctionForExpressionMap(an, hexVal, builder, functions,
                                validationId);
                        break;
                    case "VLOOKUP":
                        processLookupForExpressionMap(children, builder, functions, validationId);
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

    private void processLookupForExpressionMap(List<AstNode> children, QueryBuilder builder, CellNavigationFunctions functions,
                                               Integer validationId) {
        String parsedStr = null;
        String sourceTable = "";
        int indexOfOptionalParams = 0;

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
                if(joinClause.charAt(joinClause.length()-1)==',') {
            		joinClause.deleteCharAt(joinClause.length()-1);
            	}
                indexOfOptionalParams = i;
                break;
            } else {
                parsedStr = (children.get(i) instanceof VariableNode ? children.get(i).getToken().getValue()
                        : children.get(i).getExpressionDetails().getExpressionOutput()).trim();
                if (!parsedStr.equalsIgnoreCase("")) {
                    if(children.get(i) instanceof VariableNode && children.get(i).getToken().getType().equals(TokenType.ARTHEMATICFUNCTION)) {
                    	if(joinClause.charAt(joinClause.length()-1)==',') {
                    		joinClause.deleteCharAt(joinClause.length()-1).append(parsedStr);
                    	}else {
                    		joinClause.append(parsedStr);
                    	}
                    }else {
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
                                "", builder, functions)[2]).getExpressionDetails().getExpression() + "),");
            } else if (children.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(children.get(i), new HashMap<String, String>(), "",
                                builder, functions)[2]).getExpressionDetails().getExpression() + ",");
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

        for (String filter : filterCondition) {
            formExpressionEntityDetailMap(filter, null, validationId, true, false);
        }
    }

    private void processAggregateFunctionForExpressionMap(AstNode an, String hexVal, QueryBuilder builder,
                                                          CellNavigationFunctions functions, Integer validationId) {
        List<AstNode> chilNodes = an.getChildren();
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
            formExpressionEntityDetailMap(grp, null, validationId, true, false);
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
                        "", builder, functions)[2]).getExpressionDetails().getExpression()
                        + "),");
            } else if (chilNodes.get(i).getToken().getType().equals(TokenType.FUNCTION)) {
                // whereClause.delete(whereClause.length() - 1, whereClause.length());
                whereClause.append(
                        ((AstNode) processExpressionNode(chilNodes.get(i), new HashMap<String, String>(), "",
                                builder, functions)[2]).getExpressionDetails().getExpression() + ",");
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

        for (String grp : filterCondition) {
            formExpressionEntityDetailMap(grp, null, validationId, true, false);
        }
    }

    private StringBuilder processesFilterCondition(String expression) {
        Pattern regEx;
        Matcher matcher;
        String patternToMatchCommasOutsideSquareBreackets = ",(?=(((?!\\]).)*\\[)|[^\\[\\]]*$)";
        String patterToMatchDoubleQuotesInBrackets = "\\((.*)(\")(.*)\\)";
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

    private void formExpressionEntityDetailMap(String expression, AstNode an, Integer validationId,
                                               boolean notAddMetadata, boolean isMePresent) {
        CellNavigationMetadataResolver resolver = new CellNavigationMetadataResolver();
        resolver.setSolutionId(systemSolutionId);
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
            String expressionKey = expression;

            if (null != an) {
                List<AstNode> children = an.getChildren();
                pId = (children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? null
                        : (children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new CellNavigationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpressionOutput()
                        : children.get(0).getExpressionDetails().getExpressionOutput()));

                String periodExpression = children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? ""
                        : children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new CellNavigationFunctions(), validationId)[2]).getExpressionDetails()
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
                        rStatus, ValidationConstants.TYPE_RETURN, resolver, expressionKey, notAddMetadata, isMePresent);
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
                            rStatus, ValidationConstants.TYPE_RETURN, resolver, expressionKey, notAddMetadata, isMePresent);
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
            String expressionKey = expression;

            if (null != an) {
                List<AstNode> children = an.getChildren();

                pId = (children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? null
                        : (children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<>(), "", new QueryBuilder(),
                        new CellNavigationFunctions(), validationId)[2]).getExpressionDetails()
                        .getExpressionOutput()
                        : children.get(0).getExpressionDetails().getExpressionOutput()));

                String periodExpression = children.get(0).getToken().getValue().equalsIgnoreCase(ValidationConstants.EMPTY_PARAMETER) ? ""
                        : children.get(0).getToken().getType().equals(TokenType.VARIABLE)
                        ? children.get(0).getToken().getValue()
                        : children.get(0).getExpressionDetails() == null
                        ? ((AstNode) processNodeForExpressionsMap(children.get(0),
                        new HashMap<String, String>(), "", new QueryBuilder(),
                        new CellNavigationFunctions(), validationId)[2]).getExpressionDetails()
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
                        ValidationConstants.TYPE_REFTABLE, resolver, expressionKey, notAddMetadata, isMePresent);
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
                            ValidationConstants.TYPE_REFTABLE, resolver, expressionKey, notAddMetadata, isMePresent);
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
    }

    private ExpressionEntityDetail populateExpressionEntityDetails(String entityCode, Integer reportId, String sectionDesc,
                                                                   String entityElement, String orgCode, String periodId,
                                                                   String returnStatus, String entityType,
                                                                   CellNavigationMetadataResolver resolver, String expressionKey,
                                                                   boolean notAddMetadata, boolean isMePresent) {
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
        }

        return null;
    }

    private void populateMetaDataInfo(ExpressionEntityDetail eed, String orgCode, String periodId, String returnStatus,
                                      CellNavigationMetadataResolver resolver, String expressionKey,
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

    
    public void populateErrorMap(String... errorMsg) {
        metadataErrorMap.computeIfAbsent(ValidationConstants.PARSER_ERROR_KEY, k -> new ArrayList<>());
        metadataErrorMap.get(ValidationConstants.PARSER_ERROR_KEY).addAll(Arrays.asList(errorMsg));
    }

    private ValidationRunDetails getValidationResultsObject(Integer runId, ValidationMaster validationMaster,
                                                            List<RunRecordDetail> runRecordDetails) {
        long totalFailedCount = 0L;
        long totalSuccessCount = 0L;
        ValidationRunDetails vrd = new ValidationRunDetails();
        boolean hasError = false;
        String dimensionCSV = "";

        if (!runRecordDetails.isEmpty()) {
            if (runRecordDetails.size() == 1 && runRecordDetails.get(0).isHasError()) {
                hasError = runRecordDetails.get(0).isHasError();
            } else {
                hasError = runRecordDetails.get(0).isHasError();
                dimensionCSV = runRecordDetails.get(0).getDimensionsCSV();

                totalSuccessCount = runRecordDetails.stream().filter(detail -> detail.isValidationResult()).count();
                totalFailedCount = runRecordDetails.stream().filter(detail -> !detail.isValidationResult()).count();
            }
        }

        long totalOccurrence = totalSuccessCount + totalFailedCount;

        vrd.setRunId(runId);
        vrd.setValidationId(validationMaster.getValidationId());
        vrd.setSequenceNumber(validationMaster.getSequenceNo());
        vrd.setStatus((totalFailedCount == 0 && !hasError) ? ValidationConstants.VALIDATION_STATUS_PASSED : ValidationConstants.VALIDATION_STATUS_FAILED);
        vrd.setEvaluatedExpression(validationMaster.getValidationExpression());
        vrd.setTotalOccurrence((int) totalOccurrence);
        vrd.setTotalFailed((int) totalFailedCount);
        vrd.setValidationType(validationMaster.getValidationType());
        vrd.setDimensionsCSV(dimensionCSV != null ? dimensionCSV : "");

        if (totalOccurrence == 1 && (dimensionCSV == null || dimensionCSV.trim().length() == 0)) {
            vrd.setReplacedExpression(runRecordDetails.get(0).getReplacedExpression());
        }

        return vrd;
    }
    
}
