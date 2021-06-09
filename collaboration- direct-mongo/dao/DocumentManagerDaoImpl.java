package com.fintellix.framework.collaboration.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.framework.collaboration.dto.ContentProperties;
import com.fintellix.framework.collaboration.dto.ContentSecurity;
import com.fintellix.framework.collaboration.dto.ContentSecurityAccessRole;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.Directory;
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
import com.fintellix.platformcore.utils.CollaborationProperties;
import com.google.common.base.Joiner;

public class DocumentManagerDaoImpl extends VyasaHibernateDaoSupport implements DocumentManagerDao {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	//constant Variables from property file
	private static final String CREATOR_PRIVILEGE_NAME = CollaborationProperties.getValue("app.creatorPrivilegeName");
	private static final String CONSUMER_PRIVILEGE_NAME = CollaborationProperties.getValue("app.consumerPrivilegeName");
	private static final String OWNER_PRIVILEGE_NAME = CollaborationProperties.getValue("app.ownerPrivilegeName");
	private static final String TYPE_FILE = CollaborationProperties.getValue("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getValue("app.typeDirectoryName");
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
				+" AND D.IS_PRIVATE = 0"
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
					+" AND D.IS_PRIVATE = 0"
					//+" AND P.SECURITY_TEMPLATE_NAME = '"+OWNER_PRIVILEGE_NAME+"'"
					+" AND D.PARENT_DIRECTORY_ID = :fileDirectoryId";
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
		String queryStr = "SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE' AS RETURN_ORIGIN,IS_PRIVATE FROM C_CONTENT_SECURITY P INNER JOIN C_DIRECTORY D"
				+" ON P.CONTENT_ID= D.DIRECTORY_ID"
				+" AND P.SOLUTION_ID = D.SOLUTION_ID"
				+" WHERE P.SOLUTION_ID=:solutionId "
				+" AND P.USER_ID=:userId" 
				+" AND P.ORG_ID=:orgId "
				+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
				+" UNION "
				+" SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'PRIVILEGE' AS RETURN_ORIGIN,0 FROM C_CONTENT_SECURITY P INNER JOIN C_FILE F"
				+" ON P.CONTENT_ID= F.FILE_ID"
				+" AND P.SOLUTION_ID = F.SOLUTION_ID"
				+" WHERE P.SOLUTION_ID=:solutionId"
				+" AND P.USER_ID=:userId "
				+" AND P.ORG_ID=:orgId "
				+" AND F.IS_ACTIVE=1"
				+" AND P.CONTENT_TYPE='"+TYPE_FILE+"'";

		if(accessRoleIdList.size()>0){
			queryStr = queryStr+" UNION SELECT D.DIRECTORY_ID,D.DIRECTORY_NAME,'"+TYPE_DIRECTORY+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,D.CREATED_BY,D.LAST_MODIFIED_BY,D.LAST_MODIFIED_TIME,D.PACKAGE_PATH,D.CREATED_TIME AS CREATED_TIME,'1' AS VERSION_NUMBER,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN,IS_PRIVATE FROM C_CONTENT_SECURITY_ACCESS_ROLE P INNER JOIN C_DIRECTORY D"
					+" ON P.CONTENT_ID= D.DIRECTORY_ID"
					+" AND P.SOLUTION_ID = D.SOLUTION_ID"
					+" WHERE P.SOLUTION_ID=:solutionId "
					+" AND P.ORG_ID=:orgId "
					+" AND P.ACCESS_ROLE_ID IN (:accesRoleIds) " 
					+" AND P.CONTENT_TYPE='"+TYPE_DIRECTORY+"'"
					+" UNION "
					+" SELECT F.FILE_ID AS ENTITY_ID,F.FILE_NAME AS ENTITY_NAME,'"+TYPE_FILE+"' AS TYPE,P.SECURITY_TEMPLATE_NAME AS PRIVILEGE_NAME,F.CREATED_BY,F.LAST_MODIFIED_BY,F.LAST_MODIFIED_TIME,F.PACKAGE_PATH AS PACKAGE_PATH,F.CREATED_TIME AS CREATED_TIME,VERSION_NUMBER,'PRIVILEGE_ACCESS_ROLE' AS RETURN_ORIGIN,0 FROM C_CONTENT_SECURITY_ACCESS_ROLE P INNER JOIN C_FILE F"
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
	public Map<String, String> getPrivilegeMapForDownload(String finalUidPath,Integer userId, List<Integer> accesRoleIds, Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getPrivilegeMapForDownload");
		List<Object[]> res = new ArrayList<Object[]>();
		Map<String, String> pMap = new HashMap<String, String>();
		String param = "'"+Joiner.on("','").join(finalUidPath.split(SEPARATOR))+"'";
		String queryStr = "SELECT D.DIRECTORY_ID,SECURITY_TEMPLATE_NAME FROM C_DIRECTORY D LEFT OUTER JOIN C_CONTENT_SECURITY S "
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
							+ " SELECT D.DIRECTORY_ID,SECURITY_TEMPLATE_NAME FROM C_DIRECTORY D LEFT OUTER JOIN C_CONTENT_SECURITY_ACCESS_ROLE S "
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
			if(pMap.containsKey(obj[0].toString()) && pMap.get(obj[0].toString())==null) {
				pMap.put(obj[0].toString(), null==obj[1]?null:obj[1].toString());					
			}else if(!pMap.containsKey(obj[0].toString())) {
				pMap.put(obj[0].toString(), null==obj[1]?null:obj[1].toString());
			}
		}
		return pMap;

	}

	@Override
	public File getFileDetailsbyId(String fileId, Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerDaoImpl - getDirectoryDetailsbyId()");
		String queryString= "from File where ltrim(rtrim(fileId))=:fileId"
				+ " and solutionId=:solutionId and active=1";
		Query query = getSession().createQuery(queryString);
		query.setParameter("fileId", fileId.trim());
		query.setParameter("solutionId", solutionId);
		return (File)query.uniqueResult();
	}

	
	
}
