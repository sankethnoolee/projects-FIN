package com.fintellix.framework.collaboration.bo;


import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.jcr.RepositoryException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modeshape.jcr.NoSuchRepositoryException;

import com.fintellix.administrator.model.AccessRole;
import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.DocumentWrapper;
import com.fintellix.framework.collaboration.dto.DocumentWrapperForSearch;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.common.exception.VyasaException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface DocumentManagerBo {

	public void uploadFileForUser(File convFile, String packagePath, String fileName,Integer userId, 
			Integer orgId,Integer solutionId,String fileDesc,String solutioName,JSONArray properties)  throws VyasaException , Throwable;
	public void createFolderForUser(String folderName, String packagePath,Integer userId, 
			Integer orgId,Integer solutionId,String desc,String solutioName,Integer templateId
			,Integer isPrivate,JSONArray securityProperties,boolean isSecurityTemplateApplicable,Integer securityTemplateId) throws VyasaException,NoSuchRepositoryException, RepositoryException ;
	public void shareDocuements(JSONObject sharedUserWithPrivilegesAndPackage);
	public void deleteFile(String fileId,String solutioName) throws VyasaException, NoSuchRepositoryException, RepositoryException ;
	public void deleteFolder(String folderId,String solutioName) throws VyasaException, NoSuchRepositoryException, RepositoryException ;
	public List<DocumentWrapper> getMyContentForCurrentUser(String currentDirectory,Integer userId, Integer orgId, Integer solutionId);
	public List<DocumentWrapper> getSharedContentForCurrentUser(String currentDirectory, Integer userId, Integer orgId,Integer solutionId);
	public void collaborationAddNewFolder(String currentDirectory,String newFolderName, Integer userId, Integer orgId,Integer solutionId) throws VyasaBusinessException;
	public Boolean collaborationAddNewFolderNameCheck(String currentDirectory,String newFolderName, Integer userId, Integer orgId,Integer solutionId) throws VyasaBusinessException,Throwable;
	public void collaborationRenameEntity(String currentDirectory,String selectedCurrentType, String newFolderName, Integer userId, Integer orgId,Integer solutionId)  throws VyasaBusinessException;
	public List<DocumentWrapperForSearch> getCollaborationSearchMyContent(String currentDirectory, String searchValue, Integer userId,Integer orgId, Integer solutionId,String parentPrivilege, String finalUidPath, String currentSearchDirDisplayName, String fullDirDisplayName,String requestOrigin,String myContentRootID);
	public List<DocumentWrapper> getCollaborationSearchSharedContent(String currentDirectory, String searchValue, Integer userId,Integer orgId, Integer solutionId);
	public Boolean collaborationRenameEntityNameCheck(String currentDirectory,String selectedCurrentType, String newEntityName, Integer userId, Integer orgId,Integer solutionId)  throws VyasaBusinessException,Throwable;
	public String getPrivilegeByParent(String finalUidPath,String currentChoosenDir, Integer userId, Integer orgId,Integer solutionId);
	public List<DocumentWrapper> getContentForCurrentUserAtDirectory(String currentDirectory, Integer userId, Integer orgId,Integer solutionId, String currentPrivilegeOnFolder,String requestOrigin);
	public InputStream downloadFile(String fileId,String solutionId) throws NumberFormatException, NoSuchRepositoryException, VyasaBusinessException, RepositoryException ;
	public JSONObject renameDirectory(String directoryId, String newDirName, String parentDirectoryId, Integer solutionId,
			Integer userId,String description,Integer PUBLIC_FLAG,JSONArray secPropertyJsonArr,Boolean isSecurityTemplate,Integer securityTemplateId, Integer isNameModified);
	public JSONObject renameFile(String fileId, String newFileName, String directoryId, Integer solutionId, Integer userId);
	public void collaborationDeleteEntity(String currentEntityUid);
	public String getRootUUIDForMyContents(String orgName, Integer orgId,Integer solutionID);
	public void saveOrUpdatePrivileges(String jsonArrOfPrivilegeString, String jsonArrOfPrivilegeStringForAccess, String currentElement,Integer solutionId, Users userDetails,OrganisationUnit currentOrgDetails, String currentElementDisplayName,Boolean isNotificationRequired) throws Exception;
	public List<ContentSecurityDetails> getListOfPrivilegesForEntity(String currentUid,String parentUid, String fullPathUid, Integer solutionId) throws VyasaBusinessException, Throwable;
	public InputStream downloadFileForAVersion(String fileId, String solutionName,	String versionId) throws NumberFormatException,NoSuchRepositoryException, VyasaBusinessException,RepositoryException;
	public JSONArray getAllVersionForFile(String fileId, String solutionName) throws Throwable;
	public JSONArray getAllTemplateDetails ();
	public boolean checkPrivateFolderExistenceForUser(String mY_CONTENTS_ROOT_UID, Integer userId, Integer orgId,Integer solutionID);
	public String fetchPrivateFolderUID(String mY_CONTENTS_ROOT_UID,Integer userId, Integer orgId, Integer solutionID);
	public JsonObject getPropertiesOfContent(String contentId, String contentType);
	public JSONArray getTemplatesForDirectory(String directoryId);
	public JsonObject getPropertiesOfVersionedFile(String contentId, String contentType, Integer versionNumber);
	public JSONArray getUserSecurityProfileForCurrentContext(Integer orgId, Integer userId, Integer solutionId)
			throws Throwable;
	public JsonArray getOrgForUserInCurrentContent(Integer solutionId, Integer userId, Integer orgId,
			String ORG_UNIT_DIMENSION_NAME) throws Throwable;
	public String getPrivilegeByParentMyContent(String replaceAll,String currentRequestDirectoryUid, Integer userId, Integer orgId,Integer solutionId);
	public List<ContentSecurityDetailsAccessRole> getListOfPrivilegesForEntityByAccessRole(	String currentUid, String parentUid, String fullPathUid,Integer solutionID) throws VyasaBusinessException, Throwable;
	public List<AccessRole> getListAccessRolesForOrg(Integer chosenOrgId) throws Throwable;
	public Boolean resolveEntityForDownload(String directoryId, Integer userId, Integer orgId, Integer solutionId) throws Throwable;
	
}
