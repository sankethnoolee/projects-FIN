/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter;

import java.util.List;
import java.util.Map;

import com.fintellix.validationrestservice.definition.ExpressionMetaData;

/**
 * @author sumeet.tripathi
 *
 */
public interface ExpressionResultHandler {

	public void writeValidationResult(Integer exprId, Integer runId, List<Map<String, Object>> rows,
			Map<String, String> columnNameMap, List<String> headerColumnSequence);

	public String handlerType();

	public ExpressionMetaData getExpressionMetaData(Integer runId, Integer exprId, String metadata) throws Throwable;

	public ExpressionMetaData getExpressionMetaDataForZeroOccurrence(Integer runId, Integer exprId, String metadata)
			throws Throwable;

	public void deleteExpressionResultByRunId(Integer runId);

	public void writeRow(ValidationResult validationResult, Map<String, Object> row);

	public void writeFile(ValidationResult validationResult);

}
