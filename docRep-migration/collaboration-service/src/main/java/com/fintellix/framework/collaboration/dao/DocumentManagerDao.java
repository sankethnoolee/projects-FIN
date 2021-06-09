package com.fintellix.framework.collaboration.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fintellix.administrator.model.SecurityFilterWrapper;
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
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.common.exception.VyasaException;
@Component
public interface DocumentManagerDao {

	public List<DocumentWrapper> getSharedContents(Integer userId, Integer solutionId,Integer orgId, String parentId, List<Integer> accessRoleIdList) throws VyasaBusinessException, Throwable;
	public List<DocumentWrapper> getMyContents(Integer userId, Integer solutionId,Integer orgId, String parentId, List<Integer> accessRoleIdList) throws VyasaBusinessException, Throwable;
	public List<DocumentWrapper> getCollaborationSearchSharedContent(String currentDirectory, String searchValue, Integer userId,Integer orgId, Integer solutionId) throws VyasaBusinessException, Throwable;
	public List<DocumentWrapper> getCollaborationSearchMyContent(String currentDirectory, String searchValue, Integer userId,Integer orgId, Integer solutionId) throws VyasaBusinessException, Throwable;
	public Boolean collaborationRenameEntityNameCheck(String currentDirectory, String selectedCurrentType,String newEntityName, Integer userId, Integer orgId,Integer solutionId) throws VyasaBusinessException, Throwable;
	public Boolean collaborationAddNewFolderNameCheck(String currentDirectory,String newFolderName, Integer userId, Integer orgId,Integer solutionId)throws VyasaBusinessException, Throwable;
	public List<DocumentWrapper> getContentsInDirectory(Integer userId,Integer solutionId, Integer orgId, String parentId,String parentPrivilege, List<Integer> accessRoleIdList,String requestOrigin) throws VyasaBusinessException, Throwable;
	public Map<String, String> getPrivilegeByParent(String finalUidPath, List<Integer> accesRoleIds, Integer userId, Integer orgId,Integer solutionId);
	public File getFileDetailsIfExistByName(String fileName, Integer solutionId, String directoryId);
	public Directory getDirectoryDetailsIfExistByName(String directoryName, Integer solutionId,String parentDirectoryId);
	public void saveFile(File file) throws VyasaException;
	public void saveDirectory(Directory directory) throws VyasaException;
	public void updateFileDetails(File file)  throws VyasaException;
	public File getFileDetailsById(String fileId);
	public Directory getDirectoryDetailsById(String directoryId);
	public Space getSpaceDetailById(Integer spaceId);
	public Space getSpaceDetailByName(String spaceName);
	public void deleteFile(String fileId)throws VyasaException;
	public void deleteDirectory(String directoryId)throws VyasaException;
	public List<Directory> getListOfChildDirectory(String directoryId);
	public List<File> getListOfFileInADirectory(String directoryId);
	public void renameFile(String fileId,String fileName,Integer userId)throws VyasaException;
	public void renameFolder(String directoryId,String directoryName,Integer userId,String directoryDesc)throws VyasaException;
	public String getRootUUIDForMyContents(String orgName, Integer orgId,Integer solutionId);
	public void saveOrUpdatePrivileges(List<ContentSecurity> csList);
	public List<ContentSecurityDetails> getListOfPrivilegesForEntity(String currentUid, String parentUid, String fullPathUid, Integer solutionId) throws VyasaBusinessException, Throwable;
	public void getdeleteAllPrivilegesForTheEntity(String currentElement,Integer solutionId);
	public List<File> getAllVersionOfFile(String fileId);
	List<DocumentTemplate> getAllTemplate();
	List<TemplateProperties> getPropertiesForTemplate(Integer templateId);
	void inserIntoDirectoryTemplate(DirectoryTemplateLink directoryTemplateLink)throws VyasaException;
	void saveContentProperties(List<ContentProperties> properties)throws VyasaException;
	public boolean checkPrivateFolderExistenceForUser(String mY_CONTENTS_ROOT_UID, Integer userId, Integer orgId,Integer solutionID);
	public String fetchPrivateFolderUID(String mY_CONTENTS_ROOT_UID,Integer userId, Integer orgId, Integer solutionID);
	public List<ContentProperties> getContentPropertyByContentIdAndVersion(String contentId,Integer versionNumber,String contentType);
	File getFileByVersion(String fileId, String versionNumber);
	List<DirectoryTemplateLink> templateForDirectory(String directoryId);
	public Map<String, String> getPrivilegeByParentMyContent(String finalUidPath, Integer userId, List<Integer> accesRoleIds, Integer orgId,Integer solutionId);
	public List<ContentSecurityDetailsAccessRole> getListOfPrivilegesForEntityByAccessRole(String currentUid, String parentUid, String fullPathUid,Integer solutionId)  throws VyasaBusinessException, Throwable;
	public void getdeleteAllAccessRolePrivilegesForTheEntity(String currentElement, Integer solutionId);
	public void saveOrUpdatePrivilegesForAccessRole(List<ContentSecurityAccessRole> csListAccess);
	public void deleteExistingPropertiesAndTemplateSetting(String directoryId);
	List<DocumentWrapper> getContentsInDirectoryForSearch(Integer userId,Integer solutionId, Integer orgId, String parentId,String parentPrivilege, List<Integer> accessRoleIdList,String requestOrigin)	throws VyasaBusinessException, Throwable;
	public List<DocumentWrapper> getAllNLevelEntites(List<String> fullPathDetails, Integer userId, Integer orgId,Integer solutionId,List<Integer> accessRoleIdList,String searchVal,String requestOrigin) throws VyasaBusinessException, Throwable;
	public List<ContentSecurity> getAllPrivilegesForTheEntity(String currentElement, Integer solutionId);
	public List<ContentSecurityAccessRole> getAllAccessPrivilegesForTheEntity(String currentElement, Integer solutionId);
	public Directory getDirectoryDetailsIfExistByName(String directoryName, Integer solutionId,	String parentDirectoryId, Integer orgId);
	public List<Integer> getUsersByAccessRole(Integer accessRoleId) throws Throwable;
	List<SecurityFilterWrapper> getUserSecurityFilter(Integer solutionId,Integer userId, Integer orgId,String solutionName) throws Throwable;
	public Integer getSolutionIdByName(String solutionName);
	public String getSolutionNameById(Integer solutionId);
	public Map<String, Integer> getAllSolutionMap();
	public List<DirectoryForUpload> getAllDirectory();
	Directory checkPrivateFolderByNameForUser(String rootId, Integer userId,Integer orgId, Integer solutionID, String folderName);
	public void saveAllDirectories(	List<com.fintellix.framework.collaboration.dto.Directory> directoryList);
	public void saveAllDirectoryTemplateLink(List<DirectoryTemplateLink> directoryTemplate);
	public void saveAllDirectorySecurityProperties(List<ContentProperties> directorySecurityProperties);
	public void getdeleteAllPrivilegesForTheListOfEntities(List<String> collect, Integer solutionId);
	void getdeleteAllPrivilegesForTheListOfEntitiesAccessRole(List<String> collect, Integer solutionId);
	public void saveAllFiles(List<File> files);
	public List<DIMbkeys> getBkeysFromDimTable(String dimTableName, String dimensionBkeyCol, String idColumn,
			String dimensionDescCol, String dataSourceIdCol, String isDataSource, Integer solutionId,
			String solutionName, String dimensionName) throws Exception;

}
