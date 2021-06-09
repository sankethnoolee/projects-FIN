package com.fintellix.framework.collaboration.dao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fintellix.administrator.AdministratorPropertyReader;
import com.fintellix.administrator.model.OrgSecurityFilter;
import com.fintellix.administrator.model.OrgTypeUserLink;
import com.fintellix.administrator.model.OrganisationType;
import com.fintellix.administrator.model.SecurityDimensionMaster;
import com.fintellix.administrator.model.SecurityFilterWrapper;
import com.fintellix.administrator.model.UserSecurityFilter;
import com.fintellix.administrator.model.UserWrapper;
import com.fintellix.administrator.redis.CacheCoordinator;
import com.fintellix.administrator.redis.RedisKeys;
import com.fintellix.administrator.redis.impl.AccessRestrictionLevelCache;
import com.fintellix.administrator.redis.impl.OrgTypeCache;
import com.fintellix.administrator.redis.impl.OrgUnitCache;
import com.fintellix.administrator.redis.impl.SecurityDimensionMasterCache;
import com.fintellix.administrator.redis.impl.UserWrapperCache;
import com.fintellix.framework.collaboration.dto.ContentProperties;
import com.fintellix.framework.collaboration.dto.ContentSecurity;
import com.fintellix.framework.collaboration.dto.ContentSecurityAccessRole;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.DIMbkeys;
import com.fintellix.framework.collaboration.dto.Directory;
import com.fintellix.framework.collaboration.dto.DirectoryForUpload;
import com.fintellix.framework.collaboration.dto.DirectoryTemplateLink;
import com.fintellix.framework.collaboration.dto.DocumentTemplate;
import com.fintellix.framework.collaboration.dto.DocumentWrapper;
import com.fintellix.framework.collaboration.dto.File;
import com.fintellix.framework.collaboration.dto.Space;
import com.fintellix.framework.collaboration.dto.TemplateProperties;
import com.fintellix.framework.collaboration.utils.CollaborationUtils;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.common.exception.VyasaException;
import com.fintellix.platformcore.common.hibernate.VyasaHibernateDaoSupport;
import com.google.common.base.Joiner;
@Component
public class DocumentManagerDaoImpl extends VyasaHibernateDaoSupport implements DocumentManagerDao {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private static Properties CollaborationProperties;
	static{
		try {
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("collaboration.properties");
			CollaborationProperties = new Properties();
			CollaborationProperties.load(is);
		}catch (Exception e) {
			throw new RuntimeException("Coudnt read collaboration  properties from class path",e);
		}
	}


	//constant Variables from property file
	private static final String CREATOR_PRIVILEGE_NAME = CollaborationProperties.getProperty("app.creatorPrivilegeName");
	private static final String CONSUMER_PRIVILEGE_NAME = CollaborationProperties.getProperty("app.consumerPrivilegeName");
	private static final String OWNER_PRIVILEGE_NAME = CollaborationProperties.getProperty("app.ownerPrivilegeName");
	private static final String TYPE_FILE = CollaborationProperties.getProperty("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getProperty("app.typeDirectoryName");
	private static final String SEPARATOR = "###";
	private static final String MY_CONTENT_ORIGIN = "MYCONTENT";
	private static final String SHARED_CONTENT_ORIGIN = "SHAREDCONTENT";

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<DocumentWrapper> getMyContents(Integer userId,Integer solutionId,Integer orgId,String parentId, List<Integer> accessRoleIdList) throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getMyContents");
		List<Object[]> res = new ArrayList<Object[]>();

		/*
		 * columns order required
		 * 
		 * column 0 - ENTITY_ID,
		 * column 1 - ENTITY_NAME,
		 * column 2 - TYPE,
		 * column 3 - PRIVILEGE_NAME,
		 * column 4 - CREATED_BY,
		 * column 5 - LAST_MODIFIED_BY,
		 * column 6 - LAST_MODIFIED_TIME,
		 * column 7 - PACKAGE_PATH,
		 * COLUMN 8 - CREATED_TIME
		 * COLUMN 9 - IS_PRIVATE
		 * */
		String queryStr = " SELECT DIRECTORY_ID,DIRECTORY_NAME,'"+TYPE_DIRECTORY+"','"+CONSUMER_PRIVILEGE_NAME+"' as PRIVILEGE_NAME,CREATED_BY,LAST_MODIFIED_BY,LAST_MODIFIED_TIME,PACKAGE_PATH,CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'CREATOR' AS RETURN_ORIGIN,IS_PRIVATE FROM C_DIRECTORY"
				+" WHERE PARENT_DIRECTORY_ID = :fileDirectoryId"
				+" AND SOLUTION_ID = :solutionId"
				+" AND ORG_ID = :orgId"
				+" AND IS_PRIVATE = 0"
				/*+" AND DIRECTORY_ID NOT IN ( "
				+" SELECT DIRECTORY_ID FROM C_DIRECTORY"
				+" WHERE PARENT_DIRECTORY_ID = :fileDirectoryId"
				+" AND SOLUTION_ID = :solutionId"
				+" AND ORG_ID = :orgId"
				+" AND CREATED_BY = :createdBy"
				+" AND IS_PRIVATE = 0"
				+" )"
				+" AND DIRECTORY_ID NOT IN("
				+" SELECT D.DIRECTORY_ID FROM C_CONTENT_SECURITY P INNER JOIN C_DIRECTORY D "
				+" ON P.CONTENT_ID= D.DIRECTORY_ID"
				+" AND P.SOLUTION_ID = D.SOLUTION_ID"
				+" WHERE P.SOLUTION_ID=:solutionId" 
				+" AND P.ORG_ID=:orgId "
				+" AND P.USER_ID=:createdBy "
				+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
				+" AND P.SECURITY_TEMPLATE_NAME='"+OWNER_PRIVILEGE_NAME+"'"
				+" )"*/
				+" UNION "
				+" SELECT DIRECTORY_ID,DIRECTORY_NAME,'"+TYPE_DIRECTORY+"','"+CREATOR_PRIVILEGE_NAME+"' as PRIVILEGE_NAME,CREATED_BY,LAST_MODIFIED_BY,LAST_MODIFIED_TIME,PACKAGE_PATH,CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'CREATOR' AS RETURN_ORIGIN,IS_PRIVATE FROM C_DIRECTORY"
				+" WHERE PARENT_DIRECTORY_ID = :fileDirectoryId"
				+" AND SOLUTION_ID = :solutionId"
				+" AND ORG_ID = :orgId"
				+" AND CREATED_BY = :createdBy"
				+" AND IS_PRIVATE = 1"
				+" UNION "
				+" SELECT DIRECTORY_ID,DIRECTORY_NAME,'"+TYPE_DIRECTORY+"','"+CREATOR_PRIVILEGE_NAME+"' as PRIVILEGE_NAME,CREATED_BY,LAST_MODIFIED_BY,LAST_MODIFIED_TIME,PACKAGE_PATH,CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE' AS RETURN_ORIGIN,IS_PRIVATE FROM C_DIRECTORY"
				+" WHERE PARENT_DIRECTORY_ID = :fileDirectoryId"
				+" AND SOLUTION_ID = :solutionId"
				+" AND ORG_ID = :orgId"
				+" AND CREATED_BY = :createdBy"
				+" AND IS_PRIVATE = 0"
				+" UNION"
				+" SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE' AS RETURN_ORIGIN,IS_PRIVATE FROM C_CONTENT_SECURITY P INNER JOIN C_DIRECTORY D "
				+" ON P.CONTENT_ID= D.DIRECTORY_ID"
				+" AND P.SOLUTION_ID = D.SOLUTION_ID"
				+" WHERE P.SOLUTION_ID=:solutionId" 
				+" AND P.ORG_ID=:orgId "
				+" AND P.USER_ID=:createdBy "
				+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
			//	+" AND P.SECURITY_TEMPLATE_NAME = '"+OWNER_PRIVILEGE_NAME+"'"
				+" AND D.PARENT_DIRECTORY_ID = :fileDirectoryId";
		//+" AND P.SECURITY_TEMPLATE_NAME='"+OWNER_PRIVILEGE_NAME+"'";
		if(accessRoleIdList.size()>0){
			queryStr = queryStr+" UNION"
					+" SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN,IS_PRIVATE FROM C_CONTENT_SECURITY_ACCESS_ROLE P INNER JOIN C_DIRECTORY D "
					+" ON P.CONTENT_ID= D.DIRECTORY_ID"
					+" AND P.SOLUTION_ID = D.SOLUTION_ID"
					+" WHERE P.SOLUTION_ID=:solutionId" 
					+" AND P.ORG_ID=:orgId "
					+" AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
					//+" AND P.SECURITY_TEMPLATE_NAME = '"+OWNER_PRIVILEGE_NAME+"'"
					+" AND D.PARENT_DIRECTORY_ID = :fileDirectoryId";;
			//+" AND P.SECURITY_TEMPLATE_NAME='"+OWNER_PRIVILEGE_NAME+"'";
		}

		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("fileDirectoryId", parentId);
		query.setParameter("solutionId", solutionId);
		query.setParameter("createdBy", userId);
		query.setParameter("orgId", orgId);
		if(accessRoleIdList.size()>0){
			query.setParameterList("accesRoleIds", accessRoleIdList);
		}
		res = query.list();
		return CollaborationUtils.convertResultSetToDocumentWrapper(res);
	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<DocumentWrapper> getSharedContents(Integer userId,Integer solutionId,Integer orgId,String parentId,List<Integer> accessRoleIdList) throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getSharedContents");
		List<Object[]> res = new ArrayList<Object[]>();
		/*
		 * columns order required
		 * 
		 * column 0 - ENTITY_ID,
		 * column 1 - ENTITY_NAME,
		 * column 2 - TYPE,
		 * column 3 - PRIVILEGE_NAME,
		 * column 4 - CREATED_BY,
		 * column 5 - LAST_MODIFIED_BY,
		 * column 6 - LAST_MODIFIED_TIME,
		 * column 7 - PACKAGE_PATH,
		 * COLUMN 8 - CREATED_TIME
		 * */
		String queryStr = "SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE' AS RETURN_ORIGIN FROM C_CONTENT_SECURITY P INNER JOIN C_DIRECTORY D"
				+" ON P.CONTENT_ID= D.DIRECTORY_ID"
				+" AND P.SOLUTION_ID = D.SOLUTION_ID"
				+" WHERE P.SOLUTION_ID=:solutionId "
				+" AND P.USER_ID=:userId" 
				+" AND P.ORG_ID=:orgId "
				+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
				+" UNION "
				+" SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'PRIVILEGE' AS RETURN_ORIGIN FROM C_CONTENT_SECURITY P INNER JOIN C_FILE F"
				+" ON P.CONTENT_ID= F.FILE_ID"
				+" AND P.SOLUTION_ID = F.SOLUTION_ID"
				+" WHERE P.SOLUTION_ID=:solutionId"
				+" AND P.USER_ID=:userId "
				+" AND P.ORG_ID=:orgId "
				+" AND F.IS_ACTIVE=1"
				+" AND P.CONTENT_TYPE='"+TYPE_FILE+"'";

		if(accessRoleIdList.size()>0){
			queryStr = queryStr+" UNION SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_CONTENT_SECURITY_ACCESS_ROLE P INNER JOIN C_DIRECTORY D"
					+" ON P.CONTENT_ID= D.DIRECTORY_ID"
					+" AND P.SOLUTION_ID = D.SOLUTION_ID"
					+" WHERE P.SOLUTION_ID=:solutionId "
					+" AND P.ORG_ID=:orgId "
					+" AND P.ACCESS_ROLE_ID IN (:accesRoleIds) " 
					+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
					+" UNION "
					+" SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_CONTENT_SECURITY_ACCESS_ROLE P INNER JOIN C_FILE F"
					+" ON P.CONTENT_ID= F.FILE_ID"
					+" AND P.SOLUTION_ID = F.SOLUTION_ID"
					+" WHERE P.SOLUTION_ID=:solutionId"
					+" AND P.ORG_ID=:orgId "
					+" AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+" AND F.IS_ACTIVE=1"
					+" AND P.CONTENT_TYPE='"+TYPE_FILE+"'";
		}
		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("solutionId", solutionId);
		query.setParameter("userId", userId);
		query.setParameter("orgId", orgId);
		if(accessRoleIdList.size()>0){
			query.setParameterList("accesRoleIds", accessRoleIdList);
		}
		res = query.list();
		return CollaborationUtils.convertResultSetToDocumentWrapper(res);
	}

	@Override
	public List<DocumentWrapper> getCollaborationSearchSharedContent(
			String currentDirectory, String searchValue, Integer userId,
			Integer orgId, Integer solutionId) throws VyasaBusinessException,
			Throwable {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getCollaborationSearchSharedContent");
		return null;
	}

	@Override
	public List<DocumentWrapper> getCollaborationSearchMyContent(
			String currentDirectory, String searchValue, Integer userId,
			Integer orgId, Integer solutionId) throws VyasaBusinessException,
			Throwable {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getCollaborationSearchMyContent");
		return null;
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Boolean collaborationRenameEntityNameCheck(String currentDirectory,String selectedCurrentType,
			String newEntityName, Integer userId, Integer orgId,
			Integer solutionId) throws VyasaBusinessException, Throwable {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - collaborationRenameEntityNameCheck");
		String queryStr = " SELECT COUNT(*) FROM C_DIRECTORY "
				+ " WHERE DIRECTORY_NAME = :newFolderName"
				+ " AND PARENT_DIRECTORY_ID = :fileDirectoryId"
				+ " AND SOLUTION_ID=:solutionId"
				+ " AND ORG_ID=:orgId";
		if(TYPE_FILE.equalsIgnoreCase(selectedCurrentType)){
			queryStr = " SELECT COUNT(*) FROM C_FILE "
					+ " WHERE FILE_NAME = :newFolderName "
					+ " AND DIRECTORY_ID = :fileDirectoryId "
					+ " AND SOLUTION_ID=:solutionId "
					+ " AND ORG_ID=:orgId "
					+ " AND IS_ACTIVE =1 ";
		}
		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("fileDirectoryId", currentDirectory);
		query.setParameter("solutionId", solutionId);
		query.setParameter("orgId", orgId);
		query.setParameter("newFolderName", newEntityName);

		return ((Long)query.uniqueResult()).intValue()==0?Boolean.TRUE:Boolean.FALSE;
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Boolean collaborationAddNewFolderNameCheck(String currentDirectory,
			String newFolderName, Integer userId, Integer orgId,
			Integer solutionId) throws VyasaBusinessException, Throwable {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - collaborationAddNewFolderNameCheck");
		String queryStr = " SELECT COUNT(*) FROM C_DIRECTORY "
				+ " WHERE DIRECTORY_NAME = :newFolderName"
				+ " AND PARENT_DIRECTORY_ID = :fileDirectoryId"
				+ " AND SOLUTION_ID=:solutionId"
				+ " AND ORG_ID=:orgId";
		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("fileDirectoryId", currentDirectory);
		query.setParameter("solutionId", solutionId);
		query.setParameter("orgId", orgId);
		query.setParameter("newFolderName", newFolderName);

		return ((Long)query.uniqueResult()).intValue()==0?Boolean.TRUE:Boolean.FALSE;
	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<DocumentWrapper> getContentsInDirectory(Integer userId,Integer solutionId,
			Integer orgId,String parentId,String parentPrivilege,List<Integer> accessRoleIdList,String requestOrigin) throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getContentsInDirectory");
		List<Object[]> res = new ArrayList<Object[]>();
		String privilegeBasedOnOrgin = null;
		if(MY_CONTENT_ORIGIN.equalsIgnoreCase( requestOrigin)){
			privilegeBasedOnOrgin = "PRIVILEGE";
		}else if(SHARED_CONTENT_ORIGIN.equalsIgnoreCase( requestOrigin)){
			privilegeBasedOnOrgin = "CREATOR";
		}

		/*
		 * columns order required
		 * 
		 * column 0 - ENTITY_ID,
		 * column 1 - ENTITY_NAME,
		 * column 2 - TYPE,
		 * column 3 - CREATED_BY,
		 * column 4 - LAST_MODIFIED_BY,
		 * column 5 - LAST_MODIFIED_TIME,
		 * column 6 - PACKAGE_PATH,
		 * column 7 - CREATED_TIME
		 * column 8 - version number
		 * COLUMN 9 - PRIVILEGE
		 * */


		//first part gives directory list in the current location where the  current user is creator
		String queryStr = " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'CREATOR' AS PRIVILEGE_NAME,'"+privilegeBasedOnOrgin+"' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND D.CREATED_BY=:userId"
				+ " AND D.ORG_ID=:orgId"
				+ " UNION"
				//this part gives directory list in the current location where the  current user is having privilege entries 
				+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " INNER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
				+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND P.USER_ID=:userId"
				+ " AND P.ORG_ID=:orgId"
				/*+ " AND D.DIRECTORY_ID NOT IN ("
				+ " SELECT D.DIRECTORY_ID FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND D.CREATED_BY=:userId"
				+ " )"*/
				+ " UNION"
				//this part is based on folder contents  and parent level privilege.
				+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER, '"+parentPrivilege+"' AS PRIVILEGE_NAME,'OTHERS' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				//+ " AND D.ORG_ID=:orgId"
				/*+ " AND D.DIRECTORY_ID NOT IN ("
				+ " SELECT D.DIRECTORY_ID FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND D.CREATED_BY=:userId"
				+ " UNION"
				+ " SELECT D.DIRECTORY_ID FROM C_DIRECTORY D"
				+ " RIGHT OUTER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
				+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND P.USER_ID=:userId"
				+ " AND D.DIRECTORY_ID NOT IN ("
				+ " SELECT D.DIRECTORY_ID FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND D.CREATED_BY=:userId"
				+ " )"
				+ " )"*/
				//this part gives file list in the current location where the  current user is creator
				+ " UNION "
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'CREATOR' AS PRIVILEGE_NAME,'"+privilegeBasedOnOrgin+"' AS RETURN_ORIGIN FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				+ " AND F.CREATED_BY=:userId"
				+ " AND F.ORG_ID=:orgId"
				//this part gives file list in the current location where the  current user is having privilege entries 
				+ " UNION"
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE' AS RETURN_ORIGIN FROM C_FILE F"
				+ " INNER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= F.FILE_ID"
				+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				+ " AND P.USER_ID=:userId"
				+ " AND P.ORG_ID=:orgId"
				/*+ " AND F.FILE_ID NOT IN ("
				+ " SELECT F.FILE_ID FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId "
				+ " AND F.CREATED_BY=:userId)"*/
				+ " UNION "
				//this part is based on folder contents  and parent level privilege.
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'"+parentPrivilege+"' AS PRIVILEGE_NAME,'OTHERS' AS RETURN_ORIGIN FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				//+ " AND F.ORG_ID=:orgId"
				/*+ " AND F.FILE_ID NOT IN ("
				+ " SELECT F.FILE_ID  FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				+ " AND F.CREATED_BY=:userId"
				+ " UNION"
				+ " SELECT F.FILE_ID  FROM C_FILE F"
				+ " RIGHT OUTER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= F.FILE_ID"
				+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				+ " AND F.CREATED_BY=:userId"
				+ " AND F.FILE_ID NOT IN ("
				+ " SELECT F.FILE_ID FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId "
				+ " AND F.CREATED_BY=:userId)"
				+ " )"*/
				;
		if(accessRoleIdList.size()>0){
			//ROLE QUERY
			queryStr = queryStr + " UNION "
					+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_DIRECTORY D"
					+ " INNER JOIN "
					+ " C_CONTENT_SECURITY_ACCESS_ROLE P"
					+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
					+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
					+ " WHERE D.SOLUTION_ID=:solutionId "
					+ " AND D.PARENT_DIRECTORY_ID =:parentId"
					+ " AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+ " AND P.ORG_ID=:orgId "
					//this part gives file list in the current location where the  current user ROLE is creator
					+ " UNION"
					+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_FILE F"
					+ " INNER JOIN "
					+ " C_CONTENT_SECURITY_ACCESS_ROLE P"
					+ " ON P.CONTENT_ID= F.FILE_ID"
					+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
					+ " WHERE F.SOLUTION_ID=:solutionId"
					+ " AND F.IS_ACTIVE=1"
					+ " AND F.DIRECTORY_ID = :parentId"
					+ " AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+ " AND P.ORG_ID=:orgId";
		}

		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("solutionId", solutionId);
		query.setParameter("parentId", parentId);
		query.setParameter("userId", userId);
		query.setParameter("orgId", orgId);
		if(accessRoleIdList.size()>0){
			query.setParameterList("accesRoleIds", accessRoleIdList);
		}
		res = query.list();
		return CollaborationUtils.convertResultSetToDocumentWrapperWithParentDefaultPrivilege(res,parentPrivilege);
	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })

	@Override
	public Map<String, String> getPrivilegeByParent(String finalUidPath,List<Integer> accesRoleIds,Integer userId,Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getPrivilegeByParent");
		List<Object[]> res = new ArrayList<Object[]>();
		Map<String, String> pMap = new HashMap<String, String>();
		String param = "'"+Joiner.on("','").join(finalUidPath.split(SEPARATOR))+"'";
		String queryStr = "SELECT D.DIRECTORY_ID,SECURITY_TEMPLATE_NAME FROM C_DIRECTORY D LEFT OUTER JOIN C_CONTENT_SECURITY S "
				+ " ON D.DIRECTORY_ID = S.CONTENT_ID "
				+ " AND D.SOLUTION_ID = S.SOLUTION_ID"
				+ " where D.DIRECTORY_ID in ("+param+")"
				+ " AND S.ORG_ID=:orgId "
				+ " AND S.SOLUTION_ID=:solutionId "
				+ " AND S.USER_ID=:userId ";
				if(accesRoleIds.size()>0){
					queryStr= queryStr+ " UNION "
							+ " SELECT D.DIRECTORY_ID,SECURITY_TEMPLATE_NAME FROM C_DIRECTORY D LEFT OUTER JOIN C_CONTENT_SECURITY_ACCESS_ROLE S "
							+ " ON D.DIRECTORY_ID = S.CONTENT_ID "
							+ " AND D.SOLUTION_ID = S.SOLUTION_ID"
							+ " where D.DIRECTORY_ID in ("+param+") "
							+ " AND S.ORG_ID=:orgId "
							+ " AND S.SOLUTION_ID=:solutionId "
							+ " AND S.ACCESS_ROLE_ID IN (:accesRoleIds) ";
				}
		Query query=getSession().createSQLQuery(queryStr);	
		query.setParameter("userId", userId);
		query.setParameter("orgId", orgId);
		query.setParameter("solutionId", solutionId);
		if(accesRoleIds.size()>0){
			query.setParameterList("accesRoleIds", accesRoleIds);
		}
		res = query.list();
		for(Object[] obj : res){
			pMap.put(obj[0].toString(), null==obj[1]?null:obj[1].toString());
		}
		return pMap;

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public File getFileDetailsIfExistByName(String fileName,Integer solutionId,String directoryId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getFileDetailsIfExistByName()");
		logger.debug(" DirectoryId - "+directoryId + " File Name - " + fileName + " solution - "+solutionId);

		String queryString= "from File where directoryId=:directoryId and upper(ltrim(rtrim(fileName)))=:fileName"
				+ " and solutionId=:solutionId and active=1";
		Query query = getSession().createQuery(queryString);
		query.setParameter("directoryId", directoryId);
		query.setParameter("fileName", fileName.toUpperCase().trim());
		query.setParameter("solutionId", solutionId);
		return (File) query.uniqueResult();

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Directory getDirectoryDetailsIfExistByName(String directoryName,Integer solutionId,String parentDirectoryId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getDirectoryDetailsIfExistByName()");
		logger.debug(" DirectoryId - "+parentDirectoryId + " Directory Name - " + directoryName + " solution - "+solutionId);

		String queryString= "from Directory where upper(ltrim(rtrim(directoryName)))=:directoryName"
				+ " and solutionId=:solutionId";

		if(parentDirectoryId!=null && !"".equalsIgnoreCase(parentDirectoryId.trim())){
			queryString=queryString+" and parentDirectoryId=:parentDirectoryId";
		}
		Query query = getSession().createQuery(queryString);
		if(parentDirectoryId!=null && !"".equalsIgnoreCase(parentDirectoryId.trim())){
			query.setParameter("parentDirectoryId", parentDirectoryId);
		}
		query.setParameter("directoryName", directoryName.toUpperCase().trim());
		query.setParameter("solutionId", solutionId);
		return (Directory)query.uniqueResult();
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Directory getDirectoryDetailsIfExistByName(String directoryName,Integer solutionId,String parentDirectoryId,Integer orgId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getDirectoryDetailsIfExistByName()");
		logger.debug(" DirectoryId - "+parentDirectoryId + " Directory Name - " + directoryName + " solution - "+solutionId);

		String queryString= "from Directory where upper(ltrim(rtrim(directoryName)))=:directoryName"
				+ " and solutionId=:solutionId and orgId=:orgId";

		if(parentDirectoryId!=null && !"".equalsIgnoreCase(parentDirectoryId.trim())){
			queryString=queryString+" and parentDirectoryId=:parentDirectoryId";
		}
		Query query = getSession().createQuery(queryString);
		if(parentDirectoryId!=null && !"".equalsIgnoreCase(parentDirectoryId.trim())){
			query.setParameter("parentDirectoryId", parentDirectoryId);
		}
		query.setParameter("directoryName", directoryName.toUpperCase().trim());
		query.setParameter("solutionId", solutionId);
		query.setParameter("orgId", orgId);
		return (Directory)query.uniqueResult();
	}
	
	@Override
	public void saveFile(File file)  throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveFile()");
		logger.debug("File - " +file.toString());
		getSession().save(file);

	}

	@Override
	public void saveDirectory(Directory directory)  throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveDirectory()");
		logger.debug("File - " +directory.toString());
		getSession().save(directory);
	}


	@Override
	public void updateFileDetails(File file) throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - updateFileDetails()");
		logger.debug("File - " +file.toString());
		getSession().saveOrUpdate(file);

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public File getFileDetailsById(String fileId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getFileDetailsById()");
		logger.debug(" File Id - "+fileId);

		String queryString= "from File where fileId=:fileId and active=1";
		Query query = getSession().createQuery(queryString);
		query.setParameter("fileId", fileId);
		return (File)query.uniqueResult();

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Directory getDirectoryDetailsById(String directoryId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getDirectoryDetailsById()");
		logger.debug(" DirectoryId Id - "+directoryId);

		String queryString= "from Directory where directoryId=:directoryId";
		Query query = getSession().createQuery(queryString);
		query.setParameter("directoryId", directoryId);
		return (Directory)query.uniqueResult();

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Space getSpaceDetailById(Integer spaceId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getSpaceDetailById()");
		logger.debug(" spaceId Id - "+spaceId);

		String queryString= "from Space where spaceId=:spaceId";
		Query query = getSession().createQuery(queryString);
		query.setParameter("spaceId", spaceId);
		return (Space) query.uniqueResult();
	}
	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public Space getSpaceDetailByName(String spaceName){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getSpaceDetailByName()");
		logger.debug(" Space Name - "+spaceName);
		String queryString= "from Space where upper(ltrim(rtrim(spaceName)))=:spaceName";
		Query query = getSession().createQuery(queryString);
		query.setParameter("spaceName", spaceName.toUpperCase().trim());
		return (Space)query.uniqueResult();

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public void deleteFile(String fileId) throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - deleteFile()");
		Query query = getSession().createQuery("delete from ContentSecurity where contentId=:contentId"
				+ " and contentTypeId=:contentType");
		query.setParameter("contentType", TYPE_FILE);
		query.setParameter("contentId", fileId);;
		query.executeUpdate();

		Query queryProp = getSession().createQuery("delete from ContentProperties where contentId=:fileId"
				+ " and contentType=:contentType");
		queryProp.setParameter("contentType", TYPE_FILE);
		queryProp.setParameter("fileId", fileId);
		queryProp.executeUpdate();

		Query fileDirectory =getSession().createQuery("delete from File where fileId=:fileId");
		fileDirectory.setParameter("fileId", fileId);
		fileDirectory.executeUpdate();

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public void deleteDirectory(String directoryId) throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - deleteDirectory()");

		Query query = getSession().createQuery("delete from ContentSecurity where contentId=:contentId"
				+ " and contentTypeId=:contentType ");
		query.setParameter("contentType", TYPE_DIRECTORY);
		query.setParameter("contentId", directoryId);
		query.executeUpdate();

		Query queryProp = getSession().createQuery("delete from ContentProperties where contentId=:contentId"
				+ " and contentType=:contentType");
		query.setParameter("contentType", TYPE_DIRECTORY);
		queryProp.setParameter("contentId", directoryId);

		Query deleteDirectory =getSession().createQuery("delete from Directory where directoryId=:directoryId");
		deleteDirectory.setParameter("directoryId", directoryId);
		deleteDirectory.executeUpdate();

	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<File> getListOfFileInADirectory(String directoryId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getListOfFileInADirectory()");

		String queryString= "from File where directoryId=:directoryId";
		Query query = getSession().createQuery(queryString);
		query.setParameter("directoryId", directoryId);
		return query.getResultList();
	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<Directory> getListOfChildDirectory(String directoryId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getListOfFileInADirectory()");

		String queryString= "from Directory where parentDirectoryId=:directoryId";
		Query query = getSession().createQuery(queryString);
		query.setParameter("directoryId", directoryId);
		return query.getResultList();
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public void renameFile(String fileId,String fileName,Integer userId) throws VyasaException {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - renameFile()");
		Long time=System.currentTimeMillis();
		Query renameFile = getSession().createQuery("update File set fileName=:fileName"
				+ ",lastModifiedById=:userId,modifiedTime=:time"
				+ " where fileId=:fileId");

		renameFile.setParameter("fileName", fileName);
		renameFile.setParameter("fileId", fileId);
		renameFile.setParameter("userId", userId);
		renameFile.setParameter("time", time);
		renameFile.executeUpdate();

	}

	@SuppressWarnings({ "rawtypes", "deprecation"})
	@Override
	public void renameFolder(String directoryId,String directoryName,Integer userId,String directoryDesc) throws VyasaException {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - renameFolder()");
		Long time=System.currentTimeMillis();
		Query renameDirectory = getSession().createQuery("update Directory set directoryName=:directoryName,directoryDesc=:directoryDesc"
				+ ",lastModifiedBy=:userId,modifiedTime=:time"
				+ " where directoryId=:directoryId");

		renameDirectory.setParameter("directoryName", directoryName);
		renameDirectory.setParameter("directoryDesc", directoryDesc);
		renameDirectory.setParameter("directoryId", directoryId);
		renameDirectory.setParameter("userId", userId);
		renameDirectory.setParameter("time", time);
		renameDirectory.executeUpdate();

	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public String getRootUUIDForMyContents(String orgName, Integer orgId,
			Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getRootUUIDForMyContents()");
		List<Directory> dirList = null;
		String queryString= "from Directory where (parentDirectoryId='' or parentDirectoryId is null) and directoryName = :orgName and solutionId = :solutionId and orgId = :orgId";
		Query query = getSession().createQuery(queryString);
		query.setParameter("orgName", orgName);
		query.setParameter("orgId", orgId);
		query.setParameter("solutionId", solutionId);
		dirList=query.getResultList();
		return dirList.size()==0?null:dirList.get(0).getDirectoryId();
	}

	@Override
	public void saveOrUpdatePrivileges(List<ContentSecurity> csList) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveOrUpdatePrivileges()");

		for(ContentSecurity cs:csList){
			getSession().saveOrUpdate(cs);
		}


	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<ContentSecurityDetails> getListOfPrivilegesForEntity(
			String currentUid, String parentUid, String fullPathUid,Integer solutionId)
					throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getListOfPrivilegesForEntity()");
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		Query q= getSession().createQuery("from ContentSecurity where contentId = :currentUid and solutionId = :solutionId");
		q.setParameter("currentUid", currentUid);
		q.setParameter("solutionId", solutionId);
		csList = q.list();
		return CollaborationUtils.convertContentSecurityListToDetailList(csList);
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public void getdeleteAllPrivilegesForTheEntity(String currentElement,
			Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getListOfPrivilegesForEntity()");
		Query query = getSession().createQuery("delete from ContentSecurity where contentId=:contentId"
				+ " and solutionId=:solutionId");
		query.setParameter("solutionId", solutionId);
		query.setParameter("contentId", currentElement);
		query.executeUpdate();

	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public List<File> getAllVersionOfFile (String fileId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getAllVersionOfFile()");
		Query query = getSession().createQuery("from File where fileId=:fileId");
		query.setParameter("fileId", fileId);
		return query.list();
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public List<DocumentTemplate> getAllTemplate(){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getAllTemplate()");
		Query query = getSession().createQuery("from DocumentTemplate");
		return query.list();
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public List<TemplateProperties> getPropertiesForTemplate(Integer templateId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getPropertiesForTemplate()");
		Query query = getSession().createQuery("from TemplateProperties where templateId=:templateId");
		query.setParameter("templateId", templateId);
		return query.list();
	}

	@Override
	public void inserIntoDirectoryTemplate(DirectoryTemplateLink directoryTemplateLink)throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - inserIntoDirectoryTemplate()");
		getSession().saveOrUpdate(directoryTemplateLink);
	}

	@Override
	public void saveContentProperties(List<ContentProperties> properties)throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveFileProperties()");

		for(ContentProperties prop:properties){
			getSession().saveOrUpdate(prop);
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public boolean checkPrivateFolderExistenceForUser(String rootId, Integer userId, Integer orgId,Integer solutionID) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveFileProperties()");
		Query q=getSession().createQuery("from Directory where parentDirectoryId = :rootId and creator=:userId and isPrivate=1 and orgId=:orgId and solutionId=:solutionID");
		q.setParameter("rootId", rootId);
		q.setParameter("userId", userId);
		q.setParameter("orgId", orgId);
		q.setParameter("solutionID", solutionID);
		List<Directory> resList = q.list();

		return resList.size()==0;
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public String fetchPrivateFolderUID(String rootId,
			Integer userId, Integer orgId, Integer solutionID) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - fetchPrivateFolderUID()");

		Query q=getSession().createQuery("from Directory where parentDirectoryId = :rootId and creator=:userId and isPrivate=1 and orgId=:orgId and solutionId=:solutionID");
		q.setParameter("rootId", rootId);
		q.setParameter("userId", userId);
		q.setParameter("orgId", orgId);
		q.setParameter("solutionID", solutionID);
		List<Directory> resList = q.list();

		return resList.get(0).getDirectoryId();
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public List<ContentProperties> getContentPropertyByContentIdAndVersion(String contentId, Integer versionNumber,String contentType) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getContentPropertyByContentIdAndVersion()");

		Query q = getSession().createQuery("from ContentProperties where contentId=:contentId and contentType=:contentType"
				+ " and versionNumber=:versionNumber");
		q.setParameter("contentId", contentId);
		q.setParameter("contentType", contentType);
		q.setParameter("versionNumber", versionNumber);
		return q.list();
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Override
	public File getFileByVersion(String fileId,String versionNumber){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getFileByVersion( file id = "+fileId+" version number="+versionNumber+")");
		Query query = getSession().createQuery("from File where fileId=:fileId and versionNumber=:versionNumber");
		query.setParameter("fileId", fileId);
		query.setParameter("versionNumber", versionNumber);
		return (File)query.uniqueResult();
	}

	@SuppressWarnings({ "unchecked", "rawtypes","deprecation" })
	@Override
	public List<DirectoryTemplateLink> templateForDirectory(String directoryId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - templateForDirectory(directoryId ="+directoryId+")");
		Query query = getSession().createQuery("from DirectoryTemplateLink where directoryId=:directoryId");
		query.setParameter("directoryId", directoryId);

		return query.list();
	}

	@Override
	public Map<String, String> getPrivilegeByParentMyContent(String finalUidPath,Integer userId, List<Integer> accesRoleIds, Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getPrivilegeByParentMyContent");
		List<Object[]> res = new ArrayList<Object[]>();
		Map<String, String> pMap = new HashMap<String, String>();
		String param = "'"+Joiner.on("','").join(finalUidPath.split(SEPARATOR))+"'";
		String queryStr = "SELECT D.DIRECTORY_ID,COALESCE(SECURITY_TEMPLATE_NAME,'"+CREATOR_PRIVILEGE_NAME+"') FROM C_DIRECTORY D LEFT OUTER JOIN C_CONTENT_SECURITY S "
				+ " ON D.DIRECTORY_ID = S.CONTENT_ID "
				+ " AND D.SOLUTION_ID = S.SOLUTION_ID"
				+ " where D.DIRECTORY_ID in ("+param+")"
				+ " and s.user_id=:userId "
				+ " and s.ORG_ID = :orgId"
				+ " and s.SOLUTION_ID = :solutionId"
				+ " union "
				+ " SELECT D.DIRECTORY_ID,'"+CREATOR_PRIVILEGE_NAME+"' as PRIVILEGE_NAME FROM C_DIRECTORY D "
				+ " where D.DIRECTORY_ID in ("+param+") "
				+ " and D.ORG_ID = :orgId"
				+ " and D.SOLUTION_ID = :solutionId "
				+ " and D.CREATED_BY = :userId";
				if(accesRoleIds.size()>0){
					queryStr = queryStr +  " union "
							+ " SELECT D.DIRECTORY_ID,COALESCE(SECURITY_TEMPLATE_NAME,'"+CREATOR_PRIVILEGE_NAME+"') FROM C_DIRECTORY D LEFT OUTER JOIN C_CONTENT_SECURITY_ACCESS_ROLE S "
							+ " ON D.DIRECTORY_ID = S.CONTENT_ID "
							+ " AND D.SOLUTION_ID = S.SOLUTION_ID"
							+ " where D.DIRECTORY_ID in ("+param+")"
							+ " AND S.ACCESS_ROLE_ID IN (:accesRoleIds) "
							+ " and s.ORG_ID = :orgId"
							+ " and s.SOLUTION_ID = :solutionId";
				}
				
		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("userId", userId);
		query.setParameter("orgId", orgId);
		query.setParameter("solutionId", solutionId);
		if(accesRoleIds.size()>0){
			query.setParameterList("accesRoleIds", accesRoleIds);
		}
		res = query.list();
		for(Object[] obj : res){
			pMap.put(obj[0].toString(), null==obj[1]?null:obj[1].toString());
		}
		return pMap;

	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<ContentSecurityDetailsAccessRole> getListOfPrivilegesForEntityByAccessRole(
			String currentUid, String parentUid, String fullPathUid,Integer solutionId)
					throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getListOfPrivilegesForEntityByAccessRole()");
		List<ContentSecurityAccessRole> csList = new ArrayList<ContentSecurityAccessRole>();
		Query q= getSession().createQuery("from ContentSecurityAccessRole where contentId = :currentUid and solutionId = :solutionId");
		q.setParameter("currentUid", currentUid);
		q.setParameter("solutionId", solutionId);
		csList = q.list();
		return CollaborationUtils.convertContentSecurityAccessRoleListToDetailList(csList);
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
	public void getdeleteAllAccessRolePrivilegesForTheEntity(String currentElement,
			Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getdeleteAllAccessRolePrivilegesForTheEntity()");
		Query query = getSession().createQuery("delete from ContentSecurityAccessRole where contentId=:contentId"
				+ " and solutionId=:solutionId");
		query.setParameter("solutionId", solutionId);
		query.setParameter("contentId", currentElement);
		query.executeUpdate();

	}

	@Override
	public void saveOrUpdatePrivilegesForAccessRole(List<ContentSecurityAccessRole> csList) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveOrUpdatePrivilegesForAccessRole()");

		for(ContentSecurityAccessRole cs:csList){
			getSession().saveOrUpdate(cs);
		}


	}

	@Override
	public void deleteExistingPropertiesAndTemplateSetting(String directoryId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - deleteExistingPropertiesAndTemplateSetting()");

		Query q = getSession().createQuery("delete from DirectoryTemplateLink where directoryId=:directoryId");
		q.setParameter("directoryId", directoryId);
		q.executeUpdate();

		Query q1 = getSession().createQuery("delete from ContentProperties where contentId=:directoryId and contentType=:contentType");
		q1.setParameter("directoryId", directoryId);
		q1.setParameter("contentType", TYPE_DIRECTORY);
		q1.executeUpdate();
	}

	@SuppressWarnings({ "rawtypes", "deprecation","unchecked" })
	@Override
	public List<DocumentWrapper> getContentsInDirectoryForSearch(Integer userId,Integer solutionId,Integer orgId,
			String parentId,String parentPrivilege,List<Integer> accessRoleIdList,String requestOrigin) throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getContentsInDirectoryForSearch");
		List<Object[]> res = new ArrayList<Object[]>();
		String privilegeBasedOnOrgin = null;
		if(MY_CONTENT_ORIGIN.equalsIgnoreCase( requestOrigin)){
			privilegeBasedOnOrgin = "PRIVILEGE";
		}else if(SHARED_CONTENT_ORIGIN.equalsIgnoreCase( requestOrigin)){
			privilegeBasedOnOrgin = "CREATOR";
		}
		/*
		 * columns order required
		 * 
		 * column 0 - ENTITY_ID,
		 * column 1 - ENTITY_NAME,
		 * column 2 - TYPE,
		 * column 3 - CREATED_BY,
		 * column 4 - LAST_MODIFIED_BY,
		 * column 5 - LAST_MODIFIED_TIME,
		 * column 6 - PACKAGE_PATH,
		 * column 7 - CREATED_TIME
		 * column 8 - version number
		 * COLUMN 9 - PRIVILEGE
		 * */
		//first part gives directory list in the current location where the  current user is creator
		String queryStr = " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'CREATOR' AS PRIVILEGE_NAME,'"+privilegeBasedOnOrgin+"' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND D.CREATED_BY=:userId"
				+ " AND D.ORG_ID=:orgId"
				+ " UNION"
				//this part gives directory list in the current location where the  current user is having privilege entries 
				+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " INNER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
				+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND P.USER_ID=:userId"
				+ " AND P.ORG_ID=:orgId"
				+ " UNION"
				//this part is based on folder contents  and parent level privilege.
				+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER, '"+parentPrivilege+"' AS PRIVILEGE_NAME,'OTHERS' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND D.PARENT_DIRECTORY_ID =:parentId"
				+ " AND D.IS_PRIVATE =0"
				//+ " AND D.ORG_ID=:orgId"
				//this part gives file list in the current location where the  current user is creator
				+ " UNION "
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'CREATOR' AS PRIVILEGE_NAME,'"+privilegeBasedOnOrgin+"' AS RETURN_ORIGIN FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				+ " AND F.CREATED_BY=:userId"
				+ " AND F.ORG_ID=:orgId"
				//this part gives file list in the current location where the  current user is having privilege entries 
				+ " UNION"
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE' AS RETURN_ORIGIN FROM C_FILE F"
				+ " INNER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= F.FILE_ID"
				+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				+ " AND P.USER_ID=:userId"
				+ " AND P.ORG_ID=:orgId"
				+ " UNION "
				//this part is based on folder contents  and parent level privilege.
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'"+parentPrivilege+"' AS PRIVILEGE_NAME,'OTHERS' AS RETURN_ORIGIN FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND F.DIRECTORY_ID = :parentId"
				//+ " AND F.ORG_ID=:orgId"
				;
		if(accessRoleIdList.size()>0){
			//ROLE QUERY
			queryStr = queryStr + " UNION "
					+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_DIRECTORY D"
					+ " INNER JOIN "
					+ " C_CONTENT_SECURITY_ACCESS_ROLE P"
					+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
					+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
					+ " WHERE D.SOLUTION_ID=:solutionId "
					+ " AND D.PARENT_DIRECTORY_ID =:parentId"
					+ " AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+ " AND P.ORG_ID=:orgId "
					//this part gives file list in the current location where the  current user ROLE is creator
					+ " UNION"
					+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_FILE F"
					+ " INNER JOIN "
					+ " C_CONTENT_SECURITY_ACCESS_ROLE P"
					+ " ON P.CONTENT_ID= F.FILE_ID"
					+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
					+ " WHERE F.SOLUTION_ID=:solutionId"
					+ " AND F.IS_ACTIVE=1"
					+ " AND F.DIRECTORY_ID = :parentId"
					+ " AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+ " AND P.ORG_ID=:orgId";
		}

		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("solutionId", solutionId);
		query.setParameter("parentId", parentId);
		query.setParameter("userId", userId);
		query.setParameter("orgId", orgId);
		if(accessRoleIdList.size()>0){
			query.setParameterList("accesRoleIds", accessRoleIdList);
		}
		res = query.list();
		return CollaborationUtils.convertResultSetToDocumentWrapperWithParentDefaultPrivilege(res,parentPrivilege);
	}

	@Override
	public List<DocumentWrapper> getAllNLevelEntites(
			List<String> fullPathDetails, Integer userId, Integer orgId,
			Integer solutionId,List<Integer> accessRoleIdList,String searchVal,String requestOrigin) throws VyasaBusinessException, Throwable {

		logger.info("EXEFLOW - DocumentManagerDaoImpl - getAllNLevelEntites");
		List<Object[]> res = new ArrayList<Object[]>();
		String privilegeBasedOnOrgin = null;
		if(MY_CONTENT_ORIGIN.equalsIgnoreCase( requestOrigin)){
			privilegeBasedOnOrgin = "PRIVILEGE";
		}else if(SHARED_CONTENT_ORIGIN.equalsIgnoreCase( requestOrigin)){
			privilegeBasedOnOrgin = "CREATOR";
		}
		
		/*
		 * columns order required
		 * 
		 * column 0 - ENTITY_ID,
		 * column 1 - ENTITY_NAME,
		 * column 2 - TYPE,
		 * column 3 - PRIVILEGE_NAME,
		 * column 4 - CREATED_BY,
		 * column 5 - LAST_MODIFIED_BY,
		 * column 6 - LAST_MODIFIED_TIME,
		 * column 7 - PACKAGE_PATH,
		 * COLUMN 8 - CREATED_TIME
		 * COLUMN 9 - IS_PRIVATE
		 * */
		String packageWhereClause = "(";
		String packageWhereClauseDirectory = "(";
		String packageWhereClauseFile = "(";
		for(String path:fullPathDetails){
			packageWhereClause=packageWhereClause+ " PACKAGE_PATH LIKE '"+path+"%' OR ";
			packageWhereClauseDirectory=packageWhereClauseDirectory+ " D.PACKAGE_PATH LIKE '"+path+"%' OR ";
			packageWhereClauseFile=packageWhereClauseFile+ " F.PACKAGE_PATH LIKE '"+path+"%' OR ";
		}
		packageWhereClause = packageWhereClause.substring(0, packageWhereClause.length()-3)+" ) ";
		packageWhereClauseDirectory = packageWhereClauseDirectory.substring(0, packageWhereClauseDirectory.length()-3)+" ) ";
		packageWhereClauseFile = packageWhereClauseFile.substring(0, packageWhereClauseFile.length()-3)+" ) ";
		//first part gives directory list in the current location where the  current user is creator
		String queryStr = " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'CREATOR' AS PRIVILEGE_NAME,'"+privilegeBasedOnOrgin+"' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND "+packageWhereClauseDirectory
				+ " AND D.CREATED_BY=:userId"
				+ " AND D.ORG_ID=:orgId"
				+ " UNION"
				//this part gives directory list in the current location where the  current user is having privilege entries 
				+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " INNER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
				+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND "+packageWhereClauseDirectory
				+ " AND P.USER_ID=:userId"
				+ " AND P.ORG_ID=:orgId"
				+ " UNION"
				//this part is based on folder contents  and parent level privilege.
				+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER, "+null+" AS PRIVILEGE_NAME,'OTHERS' AS RETURN_ORIGIN FROM C_DIRECTORY D"
				+ " WHERE D.SOLUTION_ID=:solutionId "
				+ " AND "+packageWhereClauseDirectory
				//+ " AND D.ORG_ID=:orgId"
				//this part gives file list in the current location where the  current user is creator
				+ " UNION "
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'CREATOR' AS PRIVILEGE_NAME,'"+privilegeBasedOnOrgin+"' AS RETURN_ORIGIN FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND "+packageWhereClauseFile
				+ " AND F.CREATED_BY=:userId"
				+ " AND F.ORG_ID=:orgId"
				//this part gives file list in the current location where the  current user is having privilege entries 
				+ " UNION"
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE' AS RETURN_ORIGIN FROM C_FILE F"
				+ " INNER JOIN "
				+ " C_CONTENT_SECURITY P"
				+ " ON P.CONTENT_ID= F.FILE_ID"
				+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND "+packageWhereClauseFile
				+ " AND P.USER_ID=:userId"
				+ " AND P.ORG_ID=:orgId"
				+ " UNION "
				//this part is based on folder contents  and parent level privilege.
				+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,"+null+" AS PRIVILEGE_NAME,'OTHERS' AS RETURN_ORIGIN FROM C_FILE F"
				+ " WHERE F.SOLUTION_ID=:solutionId"
				+ " AND F.IS_ACTIVE=1"
				+ " AND "+packageWhereClauseFile
				//+ " AND F.ORG_ID=:orgId"
				;
		if(accessRoleIdList.size()>0){
			//ROLE QUERY
			queryStr = queryStr + " UNION "
					+ " SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_DIRECTORY D"
					+ " INNER JOIN "
					+ " C_CONTENT_SECURITY_ACCESS_ROLE P"
					+ " ON P.CONTENT_ID= D.DIRECTORY_ID"
					+ " AND P.SOLUTION_ID = D.SOLUTION_ID"
					+ " WHERE D.SOLUTION_ID=:solutionId "
					+ " AND "+packageWhereClauseDirectory
					+ " AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+ " AND P.ORG_ID=:orgId "
					//this part gives file list in the current location where the  current user ROLE is creator
					+ " UNION"
					+ " SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN FROM C_FILE F"
					+ " INNER JOIN "
					+ " C_CONTENT_SECURITY_ACCESS_ROLE P"
					+ " ON P.CONTENT_ID= F.FILE_ID"
					+ " AND P.SOLUTION_ID = F.SOLUTION_ID"
					+ " WHERE F.SOLUTION_ID=:solutionId"
					+ " AND F.IS_ACTIVE=1"
					+ " AND "+packageWhereClauseFile
					+ " AND P.ACCESS_ROLE_ID IN (:accesRoleIds) "
					+ " AND P.ORG_ID=:orgId";
		}

		Query query = getSession().createSQLQuery(queryStr);
		query.setParameter("solutionId", solutionId);
		query.setParameter("userId", userId);
		query.setParameter("orgId", orgId);
		if(accessRoleIdList.size()>0){
			query.setParameterList("accesRoleIds", accessRoleIdList);
		}
		res = query.list();

		return CollaborationUtils.convertResultSetToDocumentWrapperForSearch(res);

	}

	@Override
	public List<ContentSecurity> getAllPrivilegesForTheEntity(String currentElement, Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getAllPrivilegesForTheEntity()");
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		Query query = getSession().createQuery("from ContentSecurity where contentId=:currentElement"
				+ " and solutionId=:solutionId");
		query.setParameter("currentElement", currentElement);
		query.setParameter("solutionId", solutionId);;
		csList = query.list();
		return csList;		
	}

	@Override
	public List<ContentSecurityAccessRole> getAllAccessPrivilegesForTheEntity(
			String currentElement, Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - ContentSecurityAccessRole()");
		List<ContentSecurityAccessRole> csList = new ArrayList<ContentSecurityAccessRole>();
		Query query = getSession().createQuery("from ContentSecurityAccessRole where contentId=:currentElement"
				+ " and solutionId=:solutionId");
		query.setParameter("currentElement", currentElement);
		query.setParameter("solutionId", solutionId);;
		csList = query.list();
		return csList;		
	}
	
	@Override
	public List<Integer> getUsersByAccessRole(Integer accessRoleId) throws Throwable {
		List<Integer> userIds = new ArrayList<Integer>();
		String query = "select distinct(user_id) from users where is_active=1 and user_id in ("
				+ " select user_id from USER_ACCESS_ROLE_LINK where ACCESS_ROLE_ID=" + accessRoleId + " and org_id in ("
				+ " select org_id from ORGANISATION_UNIT where IS_ACTIVE=1 and ORG_TYPE_ID in("
				+ " select ORG_TYPE_ID from ORG_TYPE_PERMISSION_LINK where is_active=1 and permission_id=("
				+ " select permission_id from access_roles where is_active =1 and ACCESS_ROLE_ID=" + accessRoleId + ")"
				+ ")" + ")" + ")";
		Query ps = getSession().createNativeQuery(query);
		List<Object> rs = ps.list();
		for (Object row : rs) {
			if (row != null) {
				userIds.add(Integer.parseInt(row + ""));
			}
		}
		return userIds;
	}
	
	@Override
	public List<SecurityFilterWrapper> getUserSecurityFilter(Integer solutionId, Integer userId, Integer orgId,String solutionName)
			throws Throwable {
		return getSecurityFilterData(solutionId, userId, orgId,solutionName);
	}
	
	private List<SecurityFilterWrapper> getSecurityFilterData(Integer solutionId, Integer userId, Integer orgId,String solutionName)
			throws Throwable {
		List<SecurityFilterWrapper> secuityFilters = new ArrayList<SecurityFilterWrapper>();
		List<UserSecurityFilter> userSecurityFilter = (List<UserSecurityFilter>) CacheCoordinator
				.get(RedisKeys.CONFIGURED_FIN_USER_SECURITY_FILTER.getKey(), orgId + "_" + userId + "_" + solutionId);
		
		List<OrgSecurityFilter> orgSecurityFilter = (List<OrgSecurityFilter>) CacheCoordinator.get(
				RedisKeys.CONFIGURED_FIN_USER_ORG_SECURITY_FILTER.getKey(), orgId + "_" + userId + "_" + solutionId);
		Map<Integer, Map<Integer, String>> userDimensionToBkey = new HashMap<Integer, Map<Integer, String>>();
		Map<Integer, Map<Integer, String>> orgDimensionToBkey = new HashMap<Integer, Map<Integer, String>>();
		if (userSecurityFilter != null) {
			for (UserSecurityFilter filter : userSecurityFilter) {
				Integer dimensionId = Integer.parseInt(filter.getDimensionName());
				SecurityDimensionMaster dimension = SecurityDimensionMasterCache.getInstance()
						.get(dimensionId.longValue());

				String bkeyInList = filter.getBkeyInList();
				String bkeyFromList = filter.getBkeyFromList();
				String bkeyToList = filter.getBkeyToList();
				Map<Integer, String> bkeyIds = new HashMap<Integer, String>();

				String query = "select " + dimension.getIdColumn() + "," + dimension.getBkeyColumn() + " from "
						+ dimension.getDimensionTableName() + " where ";
				if (dimension.getLinkedDimensionTableName() != null
						&& dimension.getLinkedDimensionTableName().trim().length() > 0) {
					String tableName = dimension.getLinkedDimensionTableName();
					String idColumn = dimension.getLinkedDimensionIdColumn();
					String bkeyColumn = dimension.getLinkedDimensionBkeyColumn();
					query = "select " + idColumn + "," + bkeyColumn + " from " + tableName + " where ";
					if (bkeyInList != null) {
						String[] bkeys = bkeyInList.split(",");
						for (int i = 0; i < bkeys.length; i++) {
							if (i != 0) {
								query = query + " or ";
							}
							query = query + "( " + bkeyColumn + " = '" + bkeys[i].split("###")[1].replaceAll("'", "''")
									+ "'";
							if (dimension.getIsDataSourceAvailable().equals(1)) {
								query = query + " and " + dimension.getDataSourceColumn() + " = "
										+ bkeys[i].split("###")[2].replaceAll("'", "''");
							}
							query = query + ")";
						}
					} else {
						query = query + bkeyColumn + " between  '" + bkeyFromList.split("###")[0].replaceAll("'", "''")
								+ "' and '" + bkeyToList.split("###")[0].replaceAll("'", "''") + "'";
						if (dimension.getIsDataSourceAvailable().equals(1)) {
							query = query + " and " + dimension.getDataSourceColumn() + " = "
									+ bkeyFromList.split("###")[1].replaceAll("'", "''");
						}

					}
				} else if (dimension.getDimensionType().trim().equalsIgnoreCase("Time")) {
					String tableName = AdministratorPropertyReader.getInstance().getProperty("app.timeDimTable");
					String idColumn = AdministratorPropertyReader.getInstance().getProperty("app.timeDimIdColumn");
					String bkeyColumn = dimension.getTimeKey();
					query = "select " + idColumn + "," + bkeyColumn + " from " + tableName + " where ";
					if (bkeyInList != null) {
						String[] bkeys = bkeyInList.split(",");
						for (int i = 0; i < bkeys.length; i++) {
							if (i != 0) {
								query = query + " or ";
							}
							query = query + "( " + bkeyColumn + " = '" + bkeys[i].split("###")[1].replaceAll("'", "''")
									+ "'";
							if (dimension.getIsDataSourceAvailable().equals(1)) {
								query = query + " and " + dimension.getDataSourceColumn() + " = "
										+ bkeys[i].split("###")[2].replaceAll("'", "''");
							}
							query = query + ")";
						}
					} else {
						query = query + bkeyColumn + " between  '" + bkeyFromList.split("###")[0].replaceAll("'", "''")
								+ "' and '" + bkeyToList.split("###")[0].replaceAll("'", "''") + "'";
						if (dimension.getIsDataSourceAvailable().equals(1)) {
							query = query + " and " + dimension.getDataSourceColumn() + " = "
									+ bkeyFromList.split("###")[1].replaceAll("'", "''");
						}

					}

				} else {
					if (bkeyInList != null) {
						String[] bkeys = bkeyInList.split(",");
						for (int i = 0; i < bkeys.length; i++) {
							if (bkeys[i].trim().length() > 0) {
								bkeyIds.put(Integer.parseInt(bkeys[i].split("###")[0]), bkeys[i].split("###")[1]);
							}
						}
						query = "";
					} else {
						query = query + dimension.getBkeyColumn() + " between  '"
								+ bkeyFromList.split("###")[0].replaceAll("'", "''") + "' and '"
								+ bkeyToList.split("###")[0].replaceAll("'", "''") + "'";
						if (dimension.getIsDataSourceAvailable().equals(1)) {
							query = query + " and " + dimension.getDataSourceColumn() + " = "
									+ bkeyFromList.split("###")[1].replaceAll("'", "''");
						}

					}

				}

				if (query.length() > 0) {
					bkeyIds = getFirstLevelData(query,solutionName);
				}
				if (dimension.getIsHierarchical().equals(1)) {
					bkeyIds.putAll(getHierarchicalData(bkeyIds, dimension,solutionName));
				}
				if (userDimensionToBkey.get(dimensionId) == null) {
					userDimensionToBkey.put(dimensionId, new HashMap<Integer, String>());
				}
				userDimensionToBkey.get(dimensionId).putAll(bkeyIds);
			}
		}

		if (orgSecurityFilter != null) {

			for (OrgSecurityFilter filter : orgSecurityFilter) {

				Integer dimensionId = Integer.parseInt(filter.getDimensionName());
				SecurityDimensionMaster dimension = SecurityDimensionMasterCache.getInstance()
						.get(dimensionId.longValue());

				String bkeyInList = filter.getBkeyInList();
				String bkeyFromList = filter.getBkeyFromList();
				String bkeyToList = filter.getBkeyToList();
				Map<Integer, String> bkeyIds = new HashMap<Integer, String>();

				String query = "select " + dimension.getIdColumn() + "," + dimension.getBkeyColumn() + " from "
						+ dimension.getDimensionTableName() + " where ";
				if (dimension.getLinkedDimensionTableName() != null
						&& dimension.getLinkedDimensionTableName().trim().length() > 0) {
					String tableName = dimension.getLinkedDimensionTableName();
					String idColumn = dimension.getLinkedDimensionIdColumn();
					String bkeyColumn = dimension.getLinkedDimensionBkeyColumn();
					query = "select " + idColumn + "," + bkeyColumn + " from " + tableName + " where ";
					if (bkeyInList != null) {
						String[] bkeys = bkeyInList.split(",");
						for (int i = 0; i < bkeys.length; i++) {
							if (i != 0) {
								query = query + " or ";
							}
							query = query + "( " + bkeyColumn + " = '" + bkeys[i].split("###")[1].replaceAll("'", "''")
									+ "'";
							if (dimension.getIsDataSourceAvailable().equals(1)) {
								query = query + " and " + dimension.getDataSourceColumn() + " = "
										+ bkeys[i].split("###")[2].replaceAll("'", "''");
							}
							query = query + ")";
						}
					} else {
						query = query + bkeyColumn + " between  '" + bkeyFromList.split("###")[0].replaceAll("'", "''")
								+ "' and '" + bkeyToList.split("###")[0].replaceAll("'", "''") + "'";
						if (dimension.getIsDataSourceAvailable().equals(1)) {
							query = query + " and " + dimension.getDataSourceColumn() + " = "
									+ bkeyFromList.split("###")[1].replaceAll("'", "''");
						}

					}
				} else if (dimension.getDimensionType().trim().equalsIgnoreCase("Time")) {
					String tableName = AdministratorPropertyReader.getInstance().getProperty("app.timeDimTable");
					String idColumn = AdministratorPropertyReader.getInstance().getProperty("app.timeDimIdColumn");
					String bkeyColumn = dimension.getTimeKey();
					query = "select " + idColumn + "," + bkeyColumn + " from " + tableName + " where ";
					if (bkeyInList != null) {
						String[] bkeys = bkeyInList.split(",");
						for (int i = 0; i < bkeys.length; i++) {
							if (i != 0) {
								query = query + " or ";
							}
							query = query + "( " + bkeyColumn + " = '" + bkeys[i].split("###")[1].replaceAll("'", "''")
									+ "'";
							if (dimension.getIsDataSourceAvailable().equals(1)) {
								query = query + " and " + dimension.getDataSourceColumn() + " = "
										+ bkeys[i].split("###")[2].replaceAll("'", "''");
							}
							query = query + ")";
						}
					} else {
						query = query + bkeyColumn + " between  '" + bkeyFromList.split("###")[0].replaceAll("'", "''")
								+ "' and '" + bkeyToList.split("###")[0].replaceAll("'", "''") + "'";
						if (dimension.getIsDataSourceAvailable().equals(1)) {
							query = query + " and " + dimension.getDataSourceColumn() + " = "
									+ bkeyFromList.split("###")[1].replaceAll("'", "''");
						}

					}

				} else {
					if (bkeyInList != null) {
						String[] bkeys = bkeyInList.split(",");
						for (int i = 0; i < bkeys.length; i++) {
							if (bkeys[i].trim().length() > 0) {
								bkeyIds.put(Integer.parseInt(bkeys[i].split("###")[0]), bkeys[i].split("###")[1]);
							}
						}
						query = "";
					} else {
						query = query + dimension.getBkeyColumn() + " between  '"
								+ bkeyFromList.split("###")[0].replaceAll("'", "''") + "' and '"
								+ bkeyToList.split("###")[0].replaceAll("'", "''") + "'";
						if (dimension.getIsDataSourceAvailable().equals(1)) {
							query = query + " and " + dimension.getDataSourceColumn() + " = "
									+ bkeyFromList.split("###")[1].replaceAll("'", "''");
						}

					}

				}

				if (query.length() > 0) {
					bkeyIds = getFirstLevelData(query,solutionName);
				}
				if (dimension.getIsHierarchical().equals(1)) {
					bkeyIds.putAll(getHierarchicalData(bkeyIds, dimension,solutionName));
				}
				if (userDimensionToBkey.get(dimensionId) == null) {
					if (orgDimensionToBkey.get(dimensionId) == null) {
						orgDimensionToBkey.put(dimensionId, new HashMap<Integer, String>());
					}

					orgDimensionToBkey.get(dimensionId).putAll(bkeyIds);
				} else {
					Set<Integer> userDimensionBkeys = new HashSet<Integer>();
					userDimensionBkeys.addAll(userDimensionToBkey.get(dimensionId).keySet());

					Set<Integer> bkeyIdsSet = new HashSet<Integer>();
					bkeyIdsSet.addAll(bkeyIds.keySet());

					userDimensionBkeys.retainAll(bkeyIdsSet);

					Set<Integer> subSet = new HashSet<Integer>();
					subSet.addAll(userDimensionToBkey.get(dimensionId).keySet());
					subSet.removeAll(userDimensionBkeys);

					for (Integer id : subSet) {
						userDimensionToBkey.get(dimensionId).remove(id);
					}
				}
			}

		}
		userDimensionToBkey.putAll(orgDimensionToBkey);

		SecurityDimensionMaster orgDimension = SecurityDimensionMasterCache.getInstance()
				.getByName(AdministratorPropertyReader.getInstance().getProperty("app.orgDimensionName").trim() + "_"
						+ solutionId);
		Set<Integer> orgIds = new HashSet<Integer>();
		if (AccessRestrictionLevelCache.getInstance()
				.get(OrgTypeCache.getInstance()
						.get(OrgUnitCache.getInstance().get(orgId.longValue()).getOrgTypeId().longValue())
						.getDataAccessLevel().longValue())
				.getLevelName().trim()
				.equalsIgnoreCase(AdministratorPropertyReader.getInstance().getProperty("app.noRestrictions").trim())) {

			UserWrapper user = UserWrapperCache.getInstance().get(userId.longValue());
			OrgTypeUserLink[] orgTypeLinks = user.getOrgTypeUserLink();
			if (orgTypeLinks != null) {
				for (OrgTypeUserLink orgTypeUserLink : orgTypeLinks) {
					Set<String> linkedOrgIds = OrgUnitCache.getInstance()
							.getOrgTypeToOrgRelation(orgTypeUserLink.getOrgTypeId().longValue());
					if (linkedOrgIds != null) {
						orgIds.addAll(linkedOrgIds.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList()));
					}
				}
			}
			List<OrganisationType> orgTypes = OrgTypeCache.getInstance().getAll();
			Integer privateAccessLevel = AccessRestrictionLevelCache.getInstance()
					.getByName(AdministratorPropertyReader.getInstance().getProperty("app.privateOrgType").trim())
					.getLevelId();
			for (OrganisationType orgType : orgTypes) {
				if (orgType.getDataAccessLevel().intValue() != privateAccessLevel) {
					Set<String> linkedOrgIds = OrgUnitCache.getInstance()
							.getOrgTypeToOrgRelation(orgType.getOrgTypeId().longValue());
					if (linkedOrgIds != null) {
						orgIds.addAll(linkedOrgIds.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList()));
					}
				}
			}
		} else {
			orgIds.add(orgId);
		}

		if (orgIds.size() > 0) {
			String getOrgSecurityFilterData = "select " + orgDimension.getIdColumn() + ","
					+ orgDimension.getBkeyColumn() + " from " + orgDimension.getDimensionTableName() + " where "
					+ orgDimension.getIdColumn() + " in (";
			int count = 0;
			for (Integer orgID : orgIds) {
				if (count != 0) {
					getOrgSecurityFilterData = getOrgSecurityFilterData + ",";
				}
				getOrgSecurityFilterData = getOrgSecurityFilterData + orgID;
				count++;
			}
			getOrgSecurityFilterData = getOrgSecurityFilterData + ")";
			Map<Integer, String> bkeyIds = new HashMap<Integer, String>();

			bkeyIds = getFirstLevelData(getOrgSecurityFilterData,solutionName);
			if (userDimensionToBkey.get(orgDimension.getSecurityDimensionId()) == null) {
				if (userDimensionToBkey.get(orgDimension.getSecurityDimensionId()) == null) {
					userDimensionToBkey.put(orgDimension.getSecurityDimensionId(), new HashMap<Integer, String>());
				}

				userDimensionToBkey.get(orgDimension.getSecurityDimensionId()).putAll(bkeyIds);
			} else {
				Set<Integer> userDimensionBkeys = new HashSet<Integer>();
				userDimensionBkeys.addAll(userDimensionToBkey.get(orgDimension.getSecurityDimensionId()).keySet());

				Set<Integer> bkeyIdsSet = new HashSet<Integer>();
				bkeyIdsSet.addAll(bkeyIds.keySet());

				userDimensionBkeys.retainAll(bkeyIdsSet);

				Set<Integer> subSet = new HashSet<Integer>();
				subSet.addAll(userDimensionToBkey.get(orgDimension.getSecurityDimensionId()).keySet());
				subSet.removeAll(userDimensionBkeys);

				for (Integer id : subSet) {
					userDimensionToBkey.get(orgDimension.getSecurityDimensionId()).remove(id);
				}
			}

		}
		for (Integer dimensionId : userDimensionToBkey.keySet()) {
			SecurityFilterWrapper filter = new SecurityFilterWrapper(
					SecurityDimensionMasterCache.getInstance().get(dimensionId.longValue()),
					userDimensionToBkey.get(dimensionId));

			secuityFilters.add(filter);
		}
		for (SecurityFilterWrapper local : secuityFilters) {
			logger.info("\n" + local.getDimension().getDimensionName() + "\n\n" + local.getBkeyIds());
		}
		return secuityFilters;
	}
	
	private Map<Integer, String> getHierarchicalData(Map<Integer, String> bkeyIds, SecurityDimensionMaster dimension,String solutionName) {


		Map<Integer, String> childBkeys = new HashMap<Integer, String>();
		String query = "select " + dimension.getIdColumn() + "," + dimension.getBkeyColumn() + " from "
				+ dimension.getDimensionTableName() + " where " + dimension.getParentIdColumn() + " IN ( ";

		int count = 0;
		for (Integer bkeyId : bkeyIds.keySet()) {

			if (count != 0) {
				query = query + ",";
			}
			query = query + bkeyId;
			count++;
		}
		query = query + " ) ";
		logger.info(query);
		try {
			SQLQuery ps = getCurrentSolutionSessionBySolutionName(solutionName).createSQLQuery(query);
			List<Object[]> rs = ps.list();
			for (Object[] row : rs) {
				if (row != null) {
					childBkeys.put(Integer.parseInt(row[0] + ""), row[1] + "");
				}
			}

			Set<Integer> childKeys = new HashSet<Integer>();
			childKeys.addAll(childBkeys.keySet());
			Set<Integer> parentKeys = new HashSet<Integer>();
			parentKeys.addAll(bkeyIds.keySet());
			childKeys.removeAll(parentKeys);

			Set<Integer> childKeyz = new HashSet<Integer>();
			childKeyz.addAll(childBkeys.keySet());
			childKeyz.removeAll(childKeys);

			for (Integer id : childKeyz) {
				childBkeys.remove(id);
			}

			if (childBkeys.size() > 0) {
				bkeyIds.putAll(childBkeys);
				bkeyIds.putAll(getHierarchicalData(childBkeys, dimension,solutionName));
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

		return bkeyIds;
	
	}

	private Map<Integer, String> getFirstLevelData(String query,String solutionName) throws Throwable {
		logger.info(query);
		Map<Integer, String> bkeyIds = new HashMap<Integer, String>();

		SQLQuery ps = getCurrentSolutionSessionBySolutionName(solutionName).createSQLQuery(query);

		List<Object[]> rs = ps.list();
		for (Object[] row : rs) {
			if (row != null) {
				bkeyIds.put(Integer.parseInt(row[0] + ""), row[1] + "");
			}
		}
		return bkeyIds;
	}
	
	@Override
	public Integer getSolutionIdByName(String solutionName){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getSolutionIdByName()");
		String q="SELECT SOLUTIONID FROM VYASASOLUTION WHERE SOLUTIONNAME=:solutionName";
		SQLQuery query = getSession().createSQLQuery(q);
		query.setParameter("solutionName", solutionName);
		Integer solId = (Integer)query.uniqueResult();
		return solId;		
	}
	@Override
	public String getSolutionNameById(Integer solutionId){
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getSolutionIdByName()");
		String q="SELECT SOLUTIONNAME FROM VYASASOLUTION WHERE SOLUTIONID=:solutionName";
		SQLQuery query = getSession().createSQLQuery(q);
		query.setParameter("solutionName", solutionId);
		String solId = (String)query.uniqueResult();
		return solId;		
	}

	@Override
	public Map<String, Integer> getAllSolutionMap() {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getAllSolutionMap()");
		String q="SELECT SOLUTIONNAME,SOLUTIONID FROM VYASASOLUTION";
		SQLQuery query = getSession().createSQLQuery(q);
		List<Object[]> res = query.list();
		Map<String,Integer> solNameIdMap = new HashMap<String,Integer>();
		for(Object[] obj :res){
			solNameIdMap.put(obj[0].toString(), Integer.parseInt(obj[1].toString()));
		}
		return solNameIdMap;
	}

	@Override
	public List<DirectoryForUpload> getAllDirectory() {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getAllSolutionMap()");
		Query query = getSession().createQuery("from Directory");
		List<Directory> res = query.list();
		 List<DirectoryForUpload> duList = new ArrayList<DirectoryForUpload>();
		DirectoryForUpload du = null;
		Map<String, String> getAllDirectoryMap = new HashMap<String, String>();
		Integer level=null;
		for(Directory dir :res){
			level = (dir.getPackageLocation()==null || dir.getPackageLocation().equals(""))?0:  dir.getPackageLocation().split(SEPARATOR).length;
			du = new DirectoryForUpload(dir,level);
			duList.add(du);
		}
		return duList;
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	@Override
	public Directory checkPrivateFolderByNameForUser(String rootId, Integer userId, Integer orgId,Integer solutionID,String folderName) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveFileProperties()");
		Query q=getSession().createQuery("from Directory where parentDirectoryId = :rootId and isPrivate=1 and "
				+ "orgId=:orgId and solutionId=:solutionID and upper(directoryName)=:folderName");
		q.setParameter("rootId", rootId);
		q.setParameter("orgId", orgId);
		q.setParameter("solutionID", solutionID);
		q.setParameter("folderName", folderName);
		List<Directory> resList = q.list();

		return resList.size()==0?null:resList.get(0);
	}

	@Override
	public void saveAllDirectories(List<Directory> directoryList) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveDirectory()");
		
		for(Directory directory:directoryList){
			logger.info("Saving directory in db - " +directory.getDirectoryName());
			getSession().save(directory);
		}
	}
	@Override
	public void saveAllDirectoryTemplateLink(List<DirectoryTemplateLink> directoryList) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveDirectory()");
		
		for(DirectoryTemplateLink directoryTemplateLink:directoryList){
			getSession().save(directoryTemplateLink);
		}
	}
	
	@Override
	public void saveAllDirectorySecurityProperties(List<ContentProperties> directorySecurityProperties) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveDirectory()");
		
		for(ContentProperties contentProperties:directorySecurityProperties){
			getSession().save(contentProperties);
		}
	}

	@Override
	public void getdeleteAllPrivilegesForTheListOfEntities(
			List<String> collect, Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getdeleteAllPrivilegesForTheListOfEntities()");
		Query query = getSession().createQuery("delete from ContentSecurity where contentId in (:contentId)"
				+ " and solutionId=:solutionId");
		query.setParameter("solutionId", solutionId);
		query.setParameterList("contentId", collect);
		query.executeUpdate();

		
	}

	@Override
	public void getdeleteAllPrivilegesForTheListOfEntitiesAccessRole(
			List<String> collect, Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getdeleteAllPrivilegesForTheListOfEntitiesAccessRole()");
		Query query = getSession().createQuery("delete from ContentSecurityAccessRole where contentId in (:contentId)"
				+ " and solutionId=:solutionId");
		query.setParameter("solutionId", solutionId);
		query.setParameterList("contentId", collect);
		query.executeUpdate();

		
	}

	@Override
	public void saveAllFiles(List<File> files) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - saveAllFiles()");
		
		for(File file:files){
			logger.info("saving file in db --- " + file.getFileName());
			getSession().save(file);
		}
		
	}
	
	@Override
	public List<DIMbkeys> getBkeysFromDimTable (String dimTableName, String dimensionBkeyCol,String idColumn, String dimensionDescCol, 
			String dataSourceIdCol,String isDataSource,Integer solutionId,String solutionName,String dimensionName) throws Exception{
		logger.info("EXEFLOW - DocumentManagerDaoImpl -> getBkeysFromDimTable()");
		List<DIMbkeys> dimBkeysList=new ArrayList<DIMbkeys>();

		String bkeyCols = "";//dimensionBkeyCol.replace("~~||~~",",");
		String descCols = "";//dimensionDescCol.replace("~",",");
		String[] bkeyColsArray = dimensionBkeyCol.split("\\~\\~\\|\\|\\~\\~");
		String[] descColsArray = dimensionDescCol.split("~");


		for(int i=0;i<bkeyColsArray.length;i++){
			bkeyCols=bkeyCols+dimTableName+"."+bkeyColsArray[i]+" as " + "bkey_"+i+" ,";
		}

		if(bkeyColsArray.length>=1){
			bkeyCols = bkeyCols.substring(0, bkeyCols.length()-1);
		}

		for(int i=0;i<descColsArray.length;i++){
			descCols=descCols+dimTableName+"."+descColsArray[i]+" as " + "desc_"+i+" ,";
		}

		if(descColsArray.length>=1){
			descCols = descCols.substring(0, descCols.length()-1);
		}

		List<Object[]> dimBkeyObjectList = null;
		DIMbkeys dimBkeys;
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		sb.append(bkeyCols+" , ");
		sb.append(descCols+" , ");
		sb.append(dimTableName+"."+idColumn+" as idCol, ");

		if("Y".equalsIgnoreCase(isDataSource)){
			sb.append("dds."+dataSourceIdCol+", ");
			sb.append(" dds.DATA_SOURCE_NAME ");
		}
		else {
			sb.append("'' as DATA_SOURCE_ID, ");
			sb.append("'' as DATA_SOURCE_NAME ");
		}

		sb.append("from "+dimTableName+" " + dimTableName + " ");

		if("Y".equalsIgnoreCase(isDataSource))
			sb.append("left join DIM_DATA_SOURCE DDS ON " +dimTableName+"."+dataSourceIdCol+" = DDS.data_source_id");


		
		SQLQuery query=null;

		query = getCurrentSolutionSessionBySolutionName(solutionName).createSQLQuery(sb.toString());
		dimBkeyObjectList = query.list(); 

		for (Object[] dimBkeyObject:dimBkeyObjectList){
			String bkeyTemp="";
			String descTemp="";

			dimBkeys = new DIMbkeys();
			//FOR BKEYS
			for (int i=0;i<bkeyColsArray.length;i++){
				if(dimBkeyObject[i]!=null && !"".equals(dimBkeyObject[i].toString().trim()))
					bkeyTemp = bkeyTemp+dimBkeyObject[i].toString().trim()+"~~||~~";
				else 
					bkeyTemp = bkeyTemp+""+"~~||~~";
			}

			if (bkeyColsArray.length>=1)
				bkeyTemp = bkeyTemp.substring(0, bkeyTemp.length()-6);

			dimBkeys.setDimensionBkeyCol(bkeyTemp);
			//FOR Desc 
			for (int j = bkeyColsArray.length;j<bkeyColsArray.length+descColsArray.length;j++){
				if (dimBkeyObject[j]!=null && !"".equals(dimBkeyObject[j].toString().trim()))
					descTemp = descTemp+dimBkeyObject[j].toString().trim()+"~";
				else 
					descTemp = descTemp+""+"~";
			}

			if (descColsArray.length>=1)
				descTemp = descTemp.substring(0, descTemp.length()-1);

			dimBkeys.setDimensionDescCol(descTemp);

			dimBkeys.setIdColumn(dimBkeyObject[bkeyColsArray.length+descColsArray.length].toString());

			if (dimBkeyObject[bkeyColsArray.length+descColsArray.length+1]!=null && !"".equalsIgnoreCase(dimBkeyObject[bkeyColsArray.length+descColsArray.length+1].toString().trim()))
				dimBkeys.setDataSourceIdCol(Integer.parseInt(dimBkeyObject[bkeyColsArray.length+descColsArray.length+1].toString()));
			else 
				dimBkeys.setDataSourceIdCol(0);

			dimBkeys.setIsDeleted("False");
			dimBkeys.setIsInherited("False");

			if ( dimBkeyObject[bkeyColsArray.length+descColsArray.length+2]!=null && !"".equalsIgnoreCase(dimBkeyObject[bkeyColsArray.length+descColsArray.length+2].toString().trim()))
				dimBkeys.setDataSourceName(dimBkeyObject[bkeyColsArray.length+descColsArray.length+2].toString().trim());
			else 
				dimBkeys.setDataSourceName("SYSTEM");

			dimBkeysList.add(dimBkeys);
		}

		Collections.sort(dimBkeysList);

		return dimBkeysList;
	

	}

}
