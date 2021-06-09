/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.resultwriter.pojo.ExpressionResultInfo;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public interface ExpressionResultMongoDao {

	public List<ExpressionResultInfo> getExpressionMetaDataForZeroOccurrence(Integer runId, Integer exprId);

	public List<ExpressionResultInfo> getExpressionMetaDataByInfoId(String id);
	
	public void deleteExpressionResultByRunId(Integer runId);

}
