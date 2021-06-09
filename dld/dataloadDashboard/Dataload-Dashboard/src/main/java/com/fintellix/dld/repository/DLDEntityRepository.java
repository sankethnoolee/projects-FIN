package com.fintellix.dld.repository;

import java.util.Collection;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.fintellix.dld.domain.DLDEntity;

@RepositoryRestResource(collectionResourceRel = "entity", path = "entity")
public interface  DLDEntityRepository<T> extends PagingAndSortingRepository<T, Long>{

	Collection<T> findAll();
	
	@Query("MATCH (m:DLDEntity)-[r:BELONGS_TO]->(a:DataRepository) RETURN m,r,a LIMIT {limit}")
	Collection<DLDEntity> graph(@Param("limit") int limit);
	@Query("MATCH p=(c:Client)<-[t:BELONGS_TO_CLIENT]-(sourceEntity:DLDEntity)<-[h:HAS_A]-(ec:DldEntityCollection)<-[task:DEPENDENT_ON]-(ec1:DldEntityCollection)-[h1:HAS_A]->(targetEntity:DLDEntity)-[t1:BELONGS_TO_CLIENT]->(c1:Client) where c.clientCode={client} AND c1.clientCode={client}  return task.taskName AS taskName,task.taskType AS taskType,task.taskRepository AS taskRepo,sourceEntity.entityName AS SOURCE,targetEntity.entityName AS TARGET,sourceEntity.entityOwnerName AS SOURCEOWNER,targetEntity.entityOwnerName AS TARGETOWNER")
	Iterable<Map<String, Object>>  getSourceTargetDependencyTask(@Param("client") String client);
	@Query("MATCH p=(c:Client)<-[t:BELONGS_TO_CLIENT]-(sourceEntity:DLDEntity)<-[h:HAS_A]-(ec:DldEntityCollection)<-[task:DEPENDENT_ON]-(ec1:DldEntityCollection)-[h1:HAS_A]->(targetEntity:DLDEntity)-[t1:BELONGS_TO_CLIENT]->(c1:Client) where c.clientCode={client} AND c1.clientCode={client}  DETACH DELETE c,t,sourceEntity,h,ec,task,ec1,h1,targetEntity,t1,c1")
	void deleteExistingData(@Param("client") String client);
	
	@Query("MATCH p=(c:Client)<-[t:BELONGS_TO_CLIENT]-(sourceEntity:DLDEntity)<-[h:HAS_A]-(ec:DldEntityCollection)<-[task:DEPENDENT_ON]-(ec1:DldEntityCollection)-[h1:HAS_A]->(targetEntity:DLDEntity)-[t1:BELONGS_TO_CLIENT]->(c1:Client) where c.clientCode={client} AND c1.clientCode={client} AND targetEntity.entityName={entityName} AND targetEntity.entityOwnerName={entityOwnerName} return task.taskName AS taskName,task.taskType AS taskType,task.taskRepository AS taskRepo,sourceEntity.entityName AS SOURCE,targetEntity.entityName AS TARGET,sourceEntity.entityOwnerName AS SOURCEOWNER,targetEntity.entityOwnerName AS TARGETOWNER")
	Iterable<Map<String, Object>>  getSourceForAEntity(@Param("client") String client,@Param("entityName") String entityName,@Param("entityOwnerName") String entityOwnerName);
	
}
