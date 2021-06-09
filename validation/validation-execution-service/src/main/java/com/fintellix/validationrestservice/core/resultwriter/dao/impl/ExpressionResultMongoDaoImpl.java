/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.resultwriter.dao.ExpressionResultMongoDao;
import com.fintellix.validationrestservice.core.resultwriter.pojo.ExpressionResultInfo;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class ExpressionResultMongoDaoImpl implements ExpressionResultMongoDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<ExpressionResultInfo> getExpressionMetaDataForZeroOccurrence(Integer runId, Integer exprId) {

		final Pageable pageableRequest = PageRequest.of(0, 1);

		Query dynamicQuery = new Query();

		Criteria runIdCriteria = Criteria.where("runId").is(runId);
		dynamicQuery.addCriteria(runIdCriteria);

		Criteria exprIdCriteria = Criteria.where("validationId").is(exprId);
		dynamicQuery.addCriteria(exprIdCriteria);

		Criteria jsonCriteria = Criteria.where("json").not();
		dynamicQuery.addCriteria(jsonCriteria);

		dynamicQuery.with(pageableRequest);

		List<ExpressionResultInfo> result = mongoTemplate.find(dynamicQuery, ExpressionResultInfo.class,
				"expressionresultinfo");
		return result;
	}

	@Override
	public List<ExpressionResultInfo> getExpressionMetaDataByInfoId(String id) {
		Query dynamicQuery = new Query();

		Criteria idCriteria = Criteria.where("id").is(id);
		dynamicQuery.addCriteria(idCriteria);

		List<ExpressionResultInfo> result = mongoTemplate.find(dynamicQuery, ExpressionResultInfo.class,
				"expressionresultinfo");
		return result;
	}

	@Override
	public void deleteExpressionResultByRunId(Integer runId) {
		Query dynamicQuery = new Query();

		Criteria idCriteria = Criteria.where("runId").is(runId);
		dynamicQuery.addCriteria(idCriteria);
		mongoTemplate.remove(dynamicQuery, ExpressionResultInfo.class, "expressionresultinfo");
	}
}
