package com.fintellix.validationrestservice.core.resultwriter.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fintellix.validationrestservice.core.resultwriter.pojo.ExpressionResultInfo;

@Repository
public interface ExpressionResultRepository extends MongoRepository<ExpressionResultInfo, String> {

    public List<ExpressionResultInfo> findByid(String resultId);
    

}
