package com.fintellix.framework.collaboration.bo;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.jcr.RepositoryException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modeshape.jcr.NoSuchRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Color;
import com.aspose.cells.Style;
import com.aspose.cells.StyleFlag;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.fintellix.administrator.model.AccessRole;
import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.SecurityDimensionMaster;
import com.fintellix.administrator.model.SecurityFilterWrapper;
import com.fintellix.administrator.model.Users;
import com.fintellix.administrator.redis.AdminCacheHelper;
import com.fintellix.administrator.redis.CacheCoordinator;
import com.fintellix.administrator.redis.RedisKeys;
import com.fintellix.administrator.redis.impl.AccessRoleCache;
import com.fintellix.administrator.redis.impl.AccessRoleWrapperCache;
import com.fintellix.administrator.redis.impl.OrgUnitCache;
import com.fintellix.administrator.redis.impl.PermSchemeWrapperCache;
import com.fintellix.administrator.redis.impl.SecurityDimensionMasterCache;
import com.fintellix.administrator.redis.impl.UsersCache;
import com.fintellix.framework.collaboration.dao.DaoFactory;
import com.fintellix.framework.collaboration.dto.CollaborationErrorLog;
import com.fintellix.framework.collaboration.dto.CollaborationNode;
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
import com.fintellix.framework.collaboration.dto.DocumentWrapperForSearch;
import com.fintellix.framework.collaboration.dto.File;
import com.fintellix.framework.collaboration.dto.FileContentProperties;
import com.fintellix.framework.collaboration.dto.FileForUpload;
import com.fintellix.framework.collaboration.dto.ShareDetailsForUpload;
import com.fintellix.framework.collaboration.dto.TemplateProperties;
import com.fintellix.framework.collaboration.notification.CollaborationNotification;
import com.fintellix.framework.collaboration.store.DocumentStore;
import com.fintellix.framework.collaboration.utils.CollaborationUtils;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.common.exception.VyasaException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
@Component
public class DocumentManagerBoImpl implements DocumentManagerBo {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DaoFactory documentManagerHibernateFactory;
	@Autowired
	private DocumentStore documentStore; 
	@Autowired
	private CollaborationNotification collaborationNotification;
	private static Map<Integer,String> directoryHeadersAndIndex=new HashMap<Integer,String>();
	private static Map<Integer,String> directorySecurityHeadersAndIndex=new HashMap<Integer,String>();
	private static Map<Integer,String> fileHeadersAndIndex=new HashMap<Integer,String>();
	private static Map<Integer,String> filePropertiesHeadersFromProperty=new HashMap<Integer,String>();
	private static Map<Integer,String> contentShareHeadersAndIndex=new HashMap<Integer,String>();
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

	private static AdminCacheHelper adminCacheUtil = AdminCacheHelper.getInstance();
	private static final String SEPARATOR = "###";
	private static final String SHARED_ROOT_UID=CollaborationProperties.getProperty("app.RootPathForSharedContentsUid");
	private static final String CONSUMER=CollaborationProperties.getProperty("app.consumerPrivilegeName");
	private static final String TYPE_FILE = CollaborationProperties.getProperty("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getProperty("app.typeDirectoryName");

	//sheet names
	private static final String Directory=CollaborationProperties.getProperty("app.directorySheetName");
	private static final String Directory_Security=CollaborationProperties.getProperty("app.directorySecuritySheetName");
	private static final String File=CollaborationProperties.getProperty("app.fileSheetName");
	private static final String File_Properties=CollaborationProperties.getProperty("app.filePropertiesSheetName");
	private static final String Content_Share=CollaborationProperties.getProperty("app.contentShareSheetName");


	//header list
	private static final String Directory_Headers=CollaborationProperties.getProperty("app.directorHeaders");
	private static final String Directory_Security_Headers=CollaborationProperties.getProperty("app.directorySecurityHeaders");
	private static final String File_Headers=CollaborationProperties.getProperty("app.fileHeaders");
	private static final String File_Properties_Headers=CollaborationProperties.getProperty("app.filePropertiesHeaders");
	private static final String Content_Share_Headers=CollaborationProperties.getProperty("app.contentShareHeaders");
	private static final Set<String> FILE_FORMATS_SET=new HashSet<>(Arrays.asList(CollaborationProperties.getProperty("app.fileFormats").split(",")));


	//date format
	private static final String periodIdFormat = "yyyymmdd";
	private static final SimpleDateFormat periodIdFormatFormatter = new SimpleDateFormat(periodIdFormat);


	public static Map<String,Integer> priorityMap = new HashMap<String,Integer>();
	static{
		// to find the top prior override the duplicate objects

		priorityMap.put("DENIED", 7);
		priorityMap.put("CREATOR", 6);
		priorityMap.put("OWNER", 5);
		priorityMap.put("CONTRIBUTOR", 4);
		priorityMap.put("CONSUMER", 3);

		//headers map
		int i =0;
		for(String dh: Directory_Headers.toLowerCase().split(",")){
			directoryHeadersAndIndex.put( i,dh);
			i++;
		}

		i =0;
		for(String dh: Directory_Security_Headers.toLowerCase().split(",")){
			directorySecurityHeadersAndIndex.put(i,dh);
			i++;
		}
		i =0;
		for(String dh: File_Headers.toLowerCase().split(",")){
			fileHeadersAndIndex.put(i,dh);
			i++;
		}
		i =0;
		for(String dh: File_Properties_Headers.toLowerCase().split(",")){
			filePropertiesHeadersFromProperty.put(i,dh);
			i++;
		}
		i =0;
		for(String dh: Content_Share_Headers.toLowerCase().split(",")){
			contentShareHeadersAndIndex.put(i,dh);
			i++;
		}

	}

	public DocumentStore getDocumentStore() {
		return documentStore;
	}

	public void setDocumentStore(DocumentStore documentStore) {
		this.documentStore = documentStore;
	}

	public DaoFactory getDocumentManagerHibernateFactory() {
		return documentManagerHibernateFactory;
	}

	public void setDocumentManagerHibernateFactory(
			DaoFactory documentManagerHibernateFactory) {
		this.documentManagerHibernateFactory = documentManagerHibernateFactory;
	}

	public CollaborationNotification getCollaborationNotification() {
		return collaborationNotification;
	}

	public void setCollaborationNotification(CollaborationNotification collaborationNotification) {
		this.collaborationNotification = collaborationNotification;
	}

	@Override
	public void uploadFileForUser(InputStream stream, String directoryId, String fileName, Integer userId,
			Integer orgId, Integer solutionId,String fileDesc,String solutionName,JSONArray properties) throws VyasaException, NoSuchRepositoryException, RepositoryException {

		logger.info("EXEFLOW - DocumentManagerBoImpl - uploadFileForUser()");
		logger.debug("File Name - "+fileName +" Directory Id - "+directoryId+" Org - " + orgId +"");
		File file;

		String fileId="";

		file=documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsIfExistByName(fileName,  solutionId, directoryId);
		Directory dir = documentManagerHibernateFactory.getDocumentManagerDao().getDirectoryDetailsById(directoryId);
		String repositoryName =getRepositoryName();


		String modeShapeDirectoryPath="";
		if(file!=null){

			//deactivating previous version
			Long time =System.currentTimeMillis();
			file.setActive(0);
			file.setModifiedTime(time);
			file.setLastModifiedById(userId);
			Integer versionNumber = Integer.parseInt(file.getVersionNumber());
			versionNumber++;

			fileId=file.getFileId();
			//setting version for file
			File updateFile = new File();
			updateFile.setFileId(file.getFileId());
			updateFile.setFileName(fileName);
			updateFile.setFileDesc(fileDesc);
			updateFile.setCreatedTime(file.getCreatedTime());
			updateFile.setModifiedTime(time);			updateFile.setCreatorId(file.getCreatorId());
			updateFile.setLastModifiedById(userId);

			updateFile.setDirectoryId(file.getDirectoryId());
			updateFile.setOrgId(file.getOrgId());
			updateFile.setPackageLocation(file.getPackageLocation());
			updateFile.setSolutionId(file.getSolutionId());
			updateFile.setVersionNumber(versionNumber+"");

			updateFile.setActive(1);
			modeShapeDirectoryPath=file.getPackageLocation().replace("###", "/");

			JSONObject prop;
			List<ContentProperties> fileProperties = new ArrayList<>();
			ContentProperties fileProperty;
			for(int i=0;i<properties.size();i++){
				prop=(JSONObject) properties.get(i);
				fileProperty=new ContentProperties();
				fileProperty.setContentId(fileId);
				fileProperty.setContentType(TYPE_FILE);
				fileProperty.setVersionNumber(versionNumber);
				fileProperty.setTemplateId(Integer.parseInt(prop.get("templateId").toString()));
				fileProperty.setPropertyId(Integer.parseInt(prop.get("propertyId").toString()));
				fileProperty.setPropertyValue(prop.get("propertyValue").toString());
				fileProperty.setPropertyDataType(prop.get("propertyDataType").toString());
				fileProperty.setIsMandatory(Integer.parseInt(prop.get("isMandatory").toString()));
				fileProperty.setIsSecurityTemplate(Integer.parseInt(prop.get("isSecurityTemplate").toString()));
				fileProperty.setVisibility(Integer.parseInt(prop.get("visibility").toString()));

				fileProperties.add(fileProperty);

			}

			documentManagerHibernateFactory.getDocumentManagerDao().updateFileDetails(file);
			documentManagerHibernateFactory.getDocumentManagerDao().saveFile(updateFile);
			documentManagerHibernateFactory.getDocumentManagerDao().saveContentProperties(fileProperties);
			documentStore.createFileByPathNameAndVersion(repositoryName, solutionName, modeShapeDirectoryPath, fileId,stream , versionNumber+"");


		} else {

			//Setting up new file details
			String filePackgePath="";
			if(dir.getPackageLocation()!=null && !"".equalsIgnoreCase(dir.getPackageLocation())){
				filePackgePath = dir.getPackageLocation()+"###"+dir.getDirectoryId();
				modeShapeDirectoryPath=filePackgePath.replace("###", "/");
			} else {
				filePackgePath = dir.getDirectoryId();
				modeShapeDirectoryPath=filePackgePath.replace("###", "/");
			}
			file = new File();
			fileId=UUID.randomUUID().toString();
			file.setFileId(fileId);
			file.setFileName(fileName);
			file.setFileDesc(fileDesc);
			file.setCreatedTime(System.currentTimeMillis());
			file.setCreatorId(userId);
			file.setDirectoryId(directoryId);
			file.setOrgId(orgId);
			file.setPackageLocation(filePackgePath);
			file.setSolutionId(solutionId);
			file.setVersionNumber("0");
			file.setActive(1);

			JSONObject prop;
			List<ContentProperties> fileProperties = new ArrayList<>();
			ContentProperties fileProperty;
			for(int i=0;i<properties.size();i++){
				prop=(JSONObject) properties.get(i);
				fileProperty=new ContentProperties();
				fileProperty.setContentId(fileId);
				fileProperty.setContentType(TYPE_FILE);
				fileProperty.setVersionNumber(0);
				fileProperty.setTemplateId(Integer.parseInt(prop.get("templateId").toString()));
				fileProperty.setPropertyId(Integer.parseInt(prop.get("propertyId").toString()));
				fileProperty.setPropertyValue(prop.get("propertyValue").toString());
				fileProperty.setPropertyDataType(prop.get("propertyDataType").toString());
				fileProperty.setIsMandatory(Integer.parseInt(prop.get("isMandatory").toString()));
				fileProperty.setIsSecurityTemplate(Integer.parseInt(prop.get("isSecurityTemplate").toString()));
				fileProperty.setVisibility(Integer.parseInt(prop.get("visibility").toString()));
				fileProperties.add(fileProperty);

			}

			documentManagerHibernateFactory.getDocumentManagerDao().saveFile(file);
			documentManagerHibernateFactory.getDocumentManagerDao().saveContentProperties(fileProperties);
			documentStore.createFileByPathNameAndVersion(repositoryName, solutionName, modeShapeDirectoryPath, fileId,stream , "0");
		}

	}

	@Override
	public void createFolderForUser(String directoryName, String parentDirectoryId, Integer userId, Integer orgId,
			Integer solutionId,String desc,String solutionName,Integer templateId,Integer isPrivate
			,JSONArray securityProperties,boolean isSecurityTemplateApplicable,Integer securityTemplateId) throws VyasaException ,NoSuchRepositoryException, RepositoryException{
		logger.info("EXEFLOW - DocumentManagerBoImpl - createFolderForUser()");
		logger.debug("Directory Name - "+directoryName +" Parent Directory Id - "+parentDirectoryId==null?"Root folder":parentDirectoryId+" Org - " + orgId +"");


		Directory existingFolder = documentManagerHibernateFactory.getDocumentManagerDao()
				.getDirectoryDetailsIfExistByName(directoryName, solutionId, parentDirectoryId,orgId);

		String repositoryName =getRepositoryName();

		if(existingFolder!=null){
			throw new VyasaException();
		} else {
			Directory directory = new Directory();
			Directory parentDirectory=new Directory();
			String directoryId = UUID.randomUUID().toString();
			String packageLocation="";
			String modeShapePath="";

			if(parentDirectoryId!=null) {
				parentDirectory=documentManagerHibernateFactory.getDocumentManagerDao().getDirectoryDetailsById(parentDirectoryId);
			}

			if(parentDirectory!=null && parentDirectory.getDirectoryId()!=null){
				if(parentDirectory.getPackageLocation()!=null && !"".equalsIgnoreCase(parentDirectory.getPackageLocation())){
					packageLocation=parentDirectory.getPackageLocation()+SEPARATOR+parentDirectory.getDirectoryId();
					modeShapePath=packageLocation.replace("###", "/");
				} else {
					packageLocation=parentDirectory.getDirectoryId();
					modeShapePath=packageLocation.replace("###", "/");
				}

			} else {
				packageLocation="";
				modeShapePath="";
			}

			directory.setCreatedTime(System.currentTimeMillis());
			directory.setCreator(userId);
			directory.setDirectoryDesc(desc);
			directory.setDirectoryId(directoryId);
			directory.setDirectoryName(directoryName);
			directory.setOrgId(orgId);
			directory.setPackageLocation(packageLocation);
			directory.setParentDirectoryId(parentDirectoryId);
			directory.setSolutionId(solutionId);
			directory.setIsPrivate(isPrivate);
			List<ContentProperties> directoryProperties = new ArrayList<>();
			if(isSecurityTemplateApplicable){

				ContentProperties directoryProperty=new ContentProperties();
				List<TemplateProperties> propertyDetail = documentManagerHibernateFactory.getDocumentManagerDao()
						.getPropertiesForTemplate(securityTemplateId);

				directoryProperty.setContentId(directoryId);
				directoryProperty.setContentType(TYPE_DIRECTORY);
				directoryProperty.setIsMandatory(propertyDetail.get(0).getIsMandatory());
				directoryProperty.setTemplateId(securityTemplateId);
				directoryProperty.setIsSecurityTemplate(1);
				directoryProperty.setVersionNumber(1);
				directoryProperty.setVisibility(propertyDetail.get(0).getToShow());
				directoryProperty.setPropertyDataType(propertyDetail.get(0).getPropertyType());
				directoryProperty.setPropertyId(propertyDetail.get(0).getPropertyId());
				directoryProperty.setPropertyValue(securityProperties.toJSONString());
				directoryProperties.add(directoryProperty);

			}
			documentManagerHibernateFactory.getDocumentManagerDao().saveDirectory(directory);

			if(templateId!=null){
				DirectoryTemplateLink dtl = new DirectoryTemplateLink();
				dtl.setDirectoryId(directoryId);
				dtl.setTemplateId(templateId);
				dtl.setIsSecurityTemplate(0);
				documentManagerHibernateFactory.getDocumentManagerDao().inserIntoDirectoryTemplate(dtl);	
			}

			if(isSecurityTemplateApplicable){
				DirectoryTemplateLink dtl = new DirectoryTemplateLink();
				dtl.setDirectoryId(directoryId);
				dtl.setTemplateId(securityTemplateId);
				dtl.setIsSecurityTemplate(1);
				documentManagerHibernateFactory.getDocumentManagerDao().inserIntoDirectoryTemplate(dtl);

			}




			documentManagerHibernateFactory.getDocumentManagerDao().saveContentProperties(directoryProperties);
			documentStore.createFolderByPath(repositoryName, solutionName, modeShapePath, directoryId);

		}

	}

	@Override
	public void deleteFile(String fileId,String solutionName) throws VyasaException, NoSuchRepositoryException, RepositoryException {
		logger.info("EXEFLOW - DocumentManagerBoImpl - deleteFile()");
		File file = documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(fileId);
		String modeShapePath = file.getPackageLocation().replace("###", "/")+"/"+file.getFileId();
		documentManagerHibernateFactory.getDocumentManagerDao().deleteFile(fileId);
		String repositoryName =getRepositoryName();
		documentStore.deleteContentByPath(repositoryName, solutionName, modeShapePath);
	}

	@Override
	public void deleteFolder(String directoryId,String solutionName) throws VyasaException, NoSuchRepositoryException, RepositoryException {
		logger.info("EXEFLOW - DocumentManagerBoImpl - deleteFolder()");

		Directory directory = documentManagerHibernateFactory.getDocumentManagerDao().getDirectoryDetailsById(directoryId);

		List<Directory> childDirectory=documentManagerHibernateFactory.getDocumentManagerDao().getListOfChildDirectory(directoryId);
		List<File> files  = documentManagerHibernateFactory.getDocumentManagerDao().getListOfFileInADirectory(directoryId);
		for(Directory dir:childDirectory){

			deleteDependentChild(dir.getDirectoryId());
		}

		for(File file :files){
			documentManagerHibernateFactory.getDocumentManagerDao().deleteFile(file.getFileId());
		}
		documentManagerHibernateFactory.getDocumentManagerDao().deleteDirectory(directoryId);

		String path = (directory.getPackageLocation()==null?"":directory.getPackageLocation().replace("###", "/"));

		if(path.length()>0){
			path=path+"/"+directory.getDirectoryId();
		} else {
			path=directory.getDirectoryId();
		}
		documentStore.deleteContentByPath(getRepositoryName(), solutionName, path);

	}

	@Override
	public void shareDocuements(JSONObject sharedUserWithPrivilegesAndPackage) {

	}

	@Override
	public List<DocumentWrapper> getMyContentForCurrentUser(String currentDirectory,
			Integer userId, Integer orgId,Integer solutionId,String solutionName) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getMyContentForCurrentUser");
		List<DocumentWrapper> myContents = new ArrayList<DocumentWrapper>();
		List<DocumentWrapper> finalContents = new ArrayList<DocumentWrapper>();
		try {
			List<AccessRole> accessRole = getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			myContents = documentManagerHibernateFactory.getDocumentManagerDao().getMyContents(userId, solutionId, orgId, currentDirectory,accessRoleIdList);
			//myContents = myContents.stream().filter(mc->mc.getPrivilegeName().equalsIgnoreCase("OWNER")||mc.getPrivilegeName().equalsIgnoreCase("CREATOR")).collect(Collectors.toList());
			finalContents=returnFinalListAfterSecurityFilter(myContents, userId, orgId, solutionId,solutionName);
			//sorting 
			Collections.sort(finalContents,Collections.reverseOrder(Comparator.comparing(DocumentWrapper::getIsPrivate))
					.thenComparing( Comparator.comparing(DocumentWrapper::getSortName)));

		} catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getMyContentForCurrentUser");
			e.printStackTrace();
		} catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getMyContentForCurrentUser");
			e.printStackTrace();
		}
		return finalContents;
	}

	@Override
	public List<DocumentWrapper> getSharedContentForCurrentUser(String currentDirectory, Integer userId,
			Integer orgId,Integer solutionId,String solutionName) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getSharedContentForCurrentUser");
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		List<DocumentWrapper> finalContent= new ArrayList<>();

		try {

			List<AccessRole> accessRole = getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			sharedContents = documentManagerHibernateFactory.getDocumentManagerDao().getSharedContents(userId, solutionId, orgId, currentDirectory,accessRoleIdList);

			finalContent=returnFinalListAfterSecurityFilter(sharedContents, userId, orgId, solutionId,solutionName);
			//sorting 
			Collections.sort(finalContent,Collections.reverseOrder(Comparator.comparing(DocumentWrapper::getIsPrivate))
					.thenComparing( Comparator.comparing(DocumentWrapper::getSortName)));
		}catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getSharedContentForCurrentUser");
			e.printStackTrace();

		}catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getSharedContentForCurrentUser");
			e.printStackTrace();
		}
		return finalContent;
	}

	@Override
	public void collaborationAddNewFolder(String currentDirectory,
			String newFolderName, Integer userId, Integer orgId,
			Integer solutionId) throws VyasaBusinessException {

	}

	@Override
	public Boolean collaborationAddNewFolderNameCheck(String currentDirectory,
			String newFolderName, Integer userId, Integer orgId,
			Integer solutionId) throws VyasaBusinessException,Throwable {
		logger.info("EXEFLOW - DocumentManagerBoImpl - collaborationAddNewFolderNameCheck");
		return documentManagerHibernateFactory.getDocumentManagerDao().collaborationAddNewFolderNameCheck( currentDirectory,
				newFolderName,  userId,  orgId,
				solutionId);
	}

	@Override
	public void collaborationRenameEntity(String currentDirectory, String selectedCurrentType,String newEntityName, Integer userId, Integer orgId,Integer solutionId) throws VyasaBusinessException{

	}

	@Override
	public Boolean collaborationRenameEntityNameCheck(String currentDirectory,String selectedCurrentType,String newEntityName ,
			Integer userId, Integer orgId,Integer solutionId) throws VyasaBusinessException,Throwable{
		logger.info("EXEFLOW - DocumentManagerBoImpl - collaborationRenameEntityNameCheck");
		return documentManagerHibernateFactory.getDocumentManagerDao().collaborationRenameEntityNameCheck( currentDirectory,selectedCurrentType , newEntityName , 
				userId,  orgId, solutionId);
	}

	@Override
	public List<DocumentWrapperForSearch> getCollaborationSearchMyContent(
			String currentDirectory, String searchValue, Integer userId,
			Integer orgId, Integer solutionId,String parentPrivilege,String finalUidPath
			, String currentSearchDirDisplayName, String fullDirDisplayName,String requestOrigin,String myContentRootID,String solutionName) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getCollaborationSearchMyContent");
		List<DocumentWrapper> firstLevelContent = new ArrayList<DocumentWrapper>();
		List<String> tempLevelContent = new ArrayList<String>();
		List<DocumentWrapper> finalLevelContent = new ArrayList<DocumentWrapper>();
		List<String> fullPathDetails = new ArrayList<String>();
		List<DocumentWrapperForSearch> finalSearchResults = new ArrayList<DocumentWrapperForSearch>(); 
		try {
			List<AccessRole> accessRole = getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			//fetching first level entities in a directory

			if(currentDirectory.equalsIgnoreCase(SHARED_ROOT_UID)){
				firstLevelContent=documentManagerHibernateFactory.getDocumentManagerDao().getSharedContents(userId, solutionId, orgId, null, accessRoleIdList);
			}else if(currentDirectory.equalsIgnoreCase(myContentRootID)){
				firstLevelContent=documentManagerHibernateFactory.getDocumentManagerDao().getMyContents(userId, solutionId, orgId, myContentRootID, accessRoleIdList);
			}else{
				firstLevelContent=documentManagerHibernateFactory.getDocumentManagerDao().getContentsInDirectoryForSearch(userId,
						solutionId, orgId, currentDirectory, parentPrivilege, accessRoleIdList,requestOrigin);
			}

			//firstLevelContent = currentDirectory.equalsIgnoreCase(SHARED_ROOT_UID)?documentManagerHibernateFactory.getDocumentManagerDao().getSharedContents(userId, solutionId, orgId, null, accessRoleIdList)
			//	:documentManagerHibernateFactory.getDocumentManagerDao().getContentsInDirectoryForSearch(userId,
			//		solutionId, orgId, currentDirectory, parentPrivilege, accessRoleIdList,requestOrigin);

			tempLevelContent=firstLevelContent.stream().map(dw->dw.getEntityId()).collect(Collectors.toList());
			//fetching full path details of those contents (only on type directory since file cannot have any child entities.)
			fullPathDetails = firstLevelContent.stream().filter(dw->dw.getEntityType().equalsIgnoreCase("DIRECTORY"))
					.map(dw->dw.getEntityPath()+"###"+dw.getEntityId()).collect(Collectors.toList());

			//try to fetch next level entities.
			if(fullPathDetails.size()>0){
				finalLevelContent = documentManagerHibernateFactory.getDocumentManagerDao().getAllNLevelEntites(fullPathDetails,userId,orgId,solutionId,accessRoleIdList,searchValue,requestOrigin);
			}
			finalLevelContent.addAll(firstLevelContent);
			//Storing all directory name and uuid.
			Map<String, String> mapOfIDName = new HashMap<String, String>();

			for(DocumentWrapper dw : finalLevelContent){
				mapOfIDName.put(dw.getEntityId(),dw.getEntityName());
			}
			//creating search objects and updating full path
			for(DocumentWrapper dw:finalLevelContent){
				DocumentWrapperForSearch dws = new DocumentWrapperForSearch(dw);
				String fullUidPath = dw.getEntityPath();
				if(currentDirectory.equalsIgnoreCase(SHARED_ROOT_UID) && tempLevelContent.contains(dw.getEntityId()) ){
					tempLevelContent.remove(dw.getEntityId());
					fullUidPath = SHARED_ROOT_UID;
				}else if(currentDirectory.indexOf(SHARED_ROOT_UID)>-1){
					fullUidPath = SHARED_ROOT_UID	+SEPARATOR+fullUidPath.substring(fullUidPath.indexOf(dw.getEntityPath().substring(dw.getEntityPath().lastIndexOf("###") + 3)), fullUidPath.length());
				}else{
					fullUidPath =finalUidPath.substring(0,finalUidPath.indexOf(currentDirectory)).replaceAll("^\\###|\\###$", "")
							+SEPARATOR+fullUidPath.substring(fullUidPath.indexOf(currentDirectory), fullUidPath.length());
				}

				String fullPathDisplayName = fullUidPath.replace(finalUidPath,fullDirDisplayName);
				for(String dirUID : fullUidPath.split(SEPARATOR)){
					if( null!=mapOfIDName.get(dirUID)){
						fullPathDisplayName=fullPathDisplayName.replace(dirUID, mapOfIDName.get(dirUID));
					}
				}
				dws.setFileLocationUUID(fullUidPath.replaceAll("^\\###|\\###$", ""));
				dws.setFileLocationDisplayName(fullPathDisplayName.replaceAll("^\\###|\\###$", ""));
				finalSearchResults.add(dws);
			}



			//find pending privileges and update the list 
			List<String> pendingPrivileges=new ArrayList<String>();// finalSearchResults.stream().filter(dw->dw.getPrivilegeName()==null).map(dw->dw.getEntityId()+SEPARATOR+dw.getFileLocationUUID()).collect(Collectors.toList());
			Map<String, String> mapOfPrivileges =new HashMap<String, String>();//finalSearchResults.stream().filter(dw->dw.getPrivilegeName()!=null).collect(Collectors.toMap( dw->dw.getEntityId()+SEPARATOR+dw.getFileLocationUUID(),dw->dw.getPrivilegeName()));
			Map<String, String> mapOfParent =new HashMap<String, String>();//finalSearchResults.stream().collect(Collectors.toMap(dw->dw.getEntityId()+SEPARATOR+dw.getFileLocationUUID(), dw->dw.getEntityPath().substring(dw.getEntityPath().lastIndexOf("###") + 3)));
			for(DocumentWrapperForSearch dw :finalSearchResults){
				if(dw.getPrivilegeName()==null){
					pendingPrivileges.add(dw.getEntityId()+SEPARATOR+dw.getFileLocationUUID());				
				}
				mapOfPrivileges.put( dw.getEntityId(),dw.getPrivilegeName());
				mapOfParent.put(dw.getEntityId()+SEPARATOR+dw.getFileLocationUUID(), dw.getEntityPath().substring(dw.getEntityPath().lastIndexOf("###") + 3));
			}


			//recursive function to find privileges.
			mapOfPrivileges = evalPrivileges(pendingPrivileges,mapOfPrivileges,mapOfParent);

			//now filtering based on search value and security filter.
			finalSearchResults=finalSearchResults.stream().filter(dw->dw.getEntityName().toLowerCase().contains(searchValue.toLowerCase()))
					.collect(Collectors.toList());
			List<DocumentWrapperForSearch> tempListForSecurityFiltering = finalSearchResults;
			finalSearchResults=returnFinalListAfterSecurityFilterForSearch(finalSearchResults, userId, orgId, solutionId,solutionName);
			//removing sub folders which were filtered by security filter.
			tempListForSecurityFiltering.removeAll(finalSearchResults);
			List<String> parentIds = tempListForSecurityFiltering.stream().map(dw->dw.getEntityId()).collect(Collectors.toList());
			tempListForSecurityFiltering = new ArrayList<DocumentWrapperForSearch>();
			for(DocumentWrapperForSearch dws : finalSearchResults){
				for(String pId :parentIds){
					if(dws.getEntityPath().contains(pId)){
						tempListForSecurityFiltering.add(dws);
					}
				}
			}
			finalSearchResults.removeAll(tempListForSecurityFiltering);
			//updating the privileges.

			for(DocumentWrapperForSearch dw:finalSearchResults.stream().filter(dw->dw.getPrivilegeName()==null).collect(Collectors.toList())){
				dw.setPrivilegeName(mapOfPrivileges.get(dw.getEntityId()));
			}

			//sorting 
			Collections.sort(finalSearchResults,Collections.reverseOrder(Comparator.comparing(DocumentWrapper::getIsPrivate))
					.thenComparing( Comparator.comparing(DocumentWrapper::getSortName)));

		}catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getCollaborationSearchMyContent");
			e.printStackTrace();

		}catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getCollaborationSearchMyContent");
			e.printStackTrace();
		}
		return finalSearchResults;
	}

	private Map<String,String> evalPrivileges(List<String> pendingPrivileges,
			Map<String, String> mapOfPrivileges,Map<String, String> mapOfParent) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - evalPrivileges");
		List<String> proccessedList = new ArrayList<String>();
		for(String pp : pendingPrivileges){
			String parent = mapOfParent.get(pp);
			if(null==parent){
				mapOfPrivileges.put(pp,CONSUMER);
				proccessedList.add(pp);
			}else{
				String parentPrivilege = mapOfPrivileges.get(parent);
				if(null!=parentPrivilege){
					mapOfPrivileges.put(pp.substring(0,pp.indexOf(SEPARATOR)),parentPrivilege);
					proccessedList.add(pp);
				}
			}
		}
		pendingPrivileges.removeAll(proccessedList);
		if(pendingPrivileges.size()>0){
			evalPrivileges(pendingPrivileges,mapOfPrivileges,mapOfParent);
		}
		return mapOfPrivileges;
	}

	@Override
	public List<DocumentWrapper> getCollaborationSearchSharedContent(
			String currentDirectory, String searchValue, Integer userId,
			Integer orgId, Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getCollaborationSearchSharedContent");
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		try {
			sharedContents = documentManagerHibernateFactory.getDocumentManagerDao().getCollaborationSearchSharedContent(
					currentDirectory, searchValue, userId,
					orgId, solutionId);
		}catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getCollaborationSearchSharedContent");
			e.printStackTrace();

		}catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getCollaborationSearchSharedContent");
			e.printStackTrace();
		}
		return sharedContents;
	}

	@Override
	public String getPrivilegeByParent(String finalUidPath,String currentChoosenDir,Integer userId,Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getCollaborationSearchSharedContent");
		Map<String,String> mapOfPrivileges = new HashMap<String, String>();
		try{

			List<AccessRole> accessRole = getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			mapOfPrivileges=documentManagerHibernateFactory.getDocumentManagerDao().getPrivilegeByParent(finalUidPath,accessRoleIdList,
					userId,orgId,solutionId);
			//adding default value.
			mapOfPrivileges.put(SHARED_ROOT_UID, CONSUMER);

			String [] pathUidArr = finalUidPath.split(SEPARATOR);
			//updating map based on parent if it doesn't have any privilege in table.
			for(int i=1;i<pathUidArr.length;i++){
				String curUid = pathUidArr[i];
				if(null==mapOfPrivileges.get(curUid)){
					mapOfPrivileges.put(curUid, mapOfPrivileges.get(pathUidArr[i-1]));
				}
			}
		}catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
			e.printStackTrace();

		}catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
			e.printStackTrace();
		}
		return mapOfPrivileges.get(currentChoosenDir)==null?CONSUMER:mapOfPrivileges.get(currentChoosenDir);
	}

	@Override
	public List<DocumentWrapper> getContentForCurrentUserAtDirectory(
			String currentDirectory, Integer userId, Integer orgId,
			Integer solutionId, String currentPrivilegeOnFolder,String requestOrigin,String solutionName) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		List<DocumentWrapper> finalContents = new ArrayList<DocumentWrapper>();
		try {

			List<AccessRole> accessRole = getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			sharedContents = documentManagerHibernateFactory.getDocumentManagerDao().getContentsInDirectory(userId, solutionId,
					orgId, currentDirectory, currentPrivilegeOnFolder,accessRoleIdList,requestOrigin);
			finalContents=returnFinalListAfterSecurityFilter(sharedContents, userId, orgId, solutionId,solutionName);
			//sorting 
			Collections.sort(finalContents,Collections.reverseOrder(Comparator.comparing(DocumentWrapper::getIsPrivate))
					.thenComparing( Comparator.comparing(DocumentWrapper::getSortName)));
		}catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
			e.printStackTrace();

		}catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
			e.printStackTrace();
		}
		return finalContents;
	}

	private String getRepositoryName() throws VyasaBusinessException{
		Set<String> repositories = documentStore.getRepositories();

		String repositoryName = "";

		if(repositories.size()>1){
			throw new VyasaBusinessException();
		} else {
			for(String repo:repositories){
				repositoryName=repo;
			}
		}
		return repositoryName;
	}

	private void deleteDependentChild(String directoryId) throws VyasaException{
		logger.info("EXEFLOW - DocumentManagerBoImpl - deleteDependentChild()");
		List<Directory> listOfChildDirectory = documentManagerHibernateFactory.getDocumentManagerDao().getListOfChildDirectory(directoryId);
		List<File> files = documentManagerHibernateFactory.getDocumentManagerDao().getListOfFileInADirectory(directoryId);

		for(File file :files){
			documentManagerHibernateFactory.getDocumentManagerDao().deleteFile(file.getFileId());
		}

		for(Directory dir:listOfChildDirectory){
			deleteDependentChild(dir.getDirectoryId());
		}

		documentManagerHibernateFactory.getDocumentManagerDao().deleteDirectory(directoryId);

	}

	@Override
	public InputStream downloadFile(String fileId, String solutionName) throws NumberFormatException, NoSuchRepositoryException, VyasaBusinessException, RepositoryException {
		logger.info("EXEFLOW - DocumentManagerBoImpl - downloadFile()");
		File file = documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(fileId);
		String modeShapePath = file.getPackageLocation().replace("###", "/");

		InputStream content = documentStore.getFileByPathNameAndVersion(getRepositoryName(), solutionName, modeShapePath, file.getFileId(), Integer.parseInt(file.getVersionNumber()));

		return content;
	}

	@Override
	public void collaborationDeleteEntity(String currentEntityUid) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - collaborationDeleteEntity()");

	}	


	@SuppressWarnings("unchecked")
	@Override
	public JSONObject renameFile(String fileId,String newFileName,String directoryId,Integer solutionId,Integer userId){
		logger.info("EXEFLOW - DocumentManagerBoImpl - renameFile()");
		JSONObject msg= new JSONObject();
		try{
			File existingFileWithSameName = documentManagerHibernateFactory.getDocumentManagerDao()
					.getFileDetailsIfExistByName(newFileName, solutionId, directoryId);

			if(existingFileWithSameName!=null){
				throw new VyasaBusinessException();
			} else {

				documentManagerHibernateFactory.getDocumentManagerDao().renameFile(fileId, newFileName, userId);

				msg.put("success", true);
				msg.put("msg", "Renamed Succesfully!");
				return msg;
			}

		} catch (VyasaException e){
			logger.error("Error -- File Name - "+newFileName+" already exist in directory");
			msg.put("success", true);
			msg.put("msg", "Renamed Succesfully!");
			return msg;
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public JSONObject renameDirectory(String directoryId,String newDirName,String parentDirectoryId,Integer solutionId,Integer userId
			,String description,Integer PUBLIC_FLAG,JSONArray securityProperties,Boolean isSecurityTemplateApplicable,Integer securityTemplateId,Integer isNameModified){
		logger.info("EXEFLOW - DocumentManagerBoImpl - renameDirectory()");
		JSONObject msg= new JSONObject();
		try{
			Directory existingFileWithSameName = isNameModified.equals(1)?
					documentManagerHibernateFactory.getDocumentManagerDao().
					getDirectoryDetailsIfExistByName(newDirName, solutionId, parentDirectoryId):null;

					if(existingFileWithSameName!=null){
						throw new VyasaBusinessException();
					} else {
						documentManagerHibernateFactory.getDocumentManagerDao().renameFolder(directoryId, newDirName, userId,description);
						//delete existing security template and content properties before updating.
						documentManagerHibernateFactory.getDocumentManagerDao().deleteExistingPropertiesAndTemplateSetting(directoryId);


						List<ContentProperties> directoryProperties = new ArrayList<>();
						if(isSecurityTemplateApplicable){

							ContentProperties directoryProperty=new ContentProperties();
							List<TemplateProperties> propertyDetail = documentManagerHibernateFactory.getDocumentManagerDao()
									.getPropertiesForTemplate(securityTemplateId);

							directoryProperty.setContentId(directoryId);
							directoryProperty.setContentType(TYPE_DIRECTORY);
							directoryProperty.setIsMandatory(propertyDetail.get(0).getIsMandatory());
							directoryProperty.setTemplateId(securityTemplateId);
							directoryProperty.setIsSecurityTemplate(1);
							directoryProperty.setVersionNumber(1);
							directoryProperty.setVisibility(propertyDetail.get(0).getToShow());
							directoryProperty.setPropertyDataType(propertyDetail.get(0).getPropertyType());
							directoryProperty.setPropertyId(propertyDetail.get(0).getPropertyId());
							directoryProperty.setPropertyValue(securityProperties.toJSONString());
							directoryProperties.add(directoryProperty);
							documentManagerHibernateFactory.getDocumentManagerDao().saveContentProperties(directoryProperties);					
						}
						if(isSecurityTemplateApplicable){
							DirectoryTemplateLink dtl = new DirectoryTemplateLink();
							dtl.setDirectoryId(directoryId);
							dtl.setTemplateId(securityTemplateId);
							dtl.setIsSecurityTemplate(1);
							documentManagerHibernateFactory.getDocumentManagerDao().inserIntoDirectoryTemplate(dtl);

						}


						msg.put("success", true);
						msg.put("msg", "Folder modified succesfully!");
						return msg;
					}
		} catch (VyasaException e){
			logger.error("Error -- Directory Name - "+newDirName+" already exist in current path!");
			msg.put("success", false);
			msg.put("msg", "Directory Name exist. You may or may not have View Privileges!");
			return msg;
		}
	}

	@Override
	public String getRootUUIDForMyContents(String orgName, Integer orgId,
			Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getRootUUIDForMyContents()");
		return documentManagerHibernateFactory.getDocumentManagerDao().getRootUUIDForMyContents(orgName, orgId,
				solutionId);
	}

	@Override
	public void saveOrUpdatePrivileges(String jsonArrOfPrivilegeString, String jsonArrOfPrivilegeStringForAccess,String currentElement
			,Integer solutionId, Users userDetails,OrganisationUnit currentOrgDetails,String currentElementDisplayName ,Boolean isNotificationRequired)
					throws Exception {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getRootUUIDForMyContents()");
		// preparing mail list
		Set<Integer> existingUserIdList = new HashSet<Integer>();
		Map<Integer,String> userIdList = new HashMap<Integer,String>();
		Map<Integer,String> accessRoleIdList = new HashMap<Integer,String>();
		Map<Integer,String> accessRoleUserList = new HashMap<Integer,String>();
		//check for existence before
		List<ContentSecurity> existingCsList = documentManagerHibernateFactory.getDocumentManagerDao().getAllPrivilegesForTheEntity(currentElement,solutionId);
		List<ContentSecurity> duplicateCsList = null;

		List<ContentSecurityAccessRole> existingCsListAccess = documentManagerHibernateFactory.getDocumentManagerDao().getAllAccessPrivilegesForTheEntity(currentElement,solutionId);
		List<ContentSecurityAccessRole> duplicateAccessCsList = null;

		ContentSecurity[] jsonArray = new Gson().fromJson(jsonArrOfPrivilegeString,ContentSecurity[].class);
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		for(ContentSecurity cs:jsonArray){
			duplicateCsList =existingCsList.stream().filter(ecs->ecs.getUserId().equals(cs.getUserId()) && ecs.getSecurityTemplateName().equalsIgnoreCase(cs.getSecurityTemplateName()))
					.collect(Collectors.toList());
			if(null==cs.getContentSecurityId()||"".equalsIgnoreCase(cs.getContentSecurityId())){
				cs.setContentSecurityId(UUID.randomUUID().toString());
				cs.setSolutionId(solutionId);
			}
			if(duplicateCsList!=null &&  duplicateCsList.size()==0){
				userIdList.put(cs.getUserId(),cs.getSecurityTemplateName());
			}
			existingUserIdList.add(cs.getUserId());
			csList.add(cs);
		}

		ContentSecurityAccessRole[] jsonArrayAccess = new Gson().fromJson(jsonArrOfPrivilegeStringForAccess,ContentSecurityAccessRole[].class);
		List<ContentSecurityAccessRole> csListAccess = new ArrayList<ContentSecurityAccessRole>();
		for(ContentSecurityAccessRole cs:jsonArrayAccess){
			duplicateAccessCsList = existingCsListAccess.stream().filter(ecs->ecs.getRoleId().equals(cs.getRoleId()) && ecs.getSecurityTemplateName().equalsIgnoreCase(cs.getSecurityTemplateName()))
					.collect(Collectors.toList());
			if(null==cs.getContentSecurityId()||"".equalsIgnoreCase(cs.getContentSecurityId())){
				cs.setContentSecurityId(UUID.randomUUID().toString());
				cs.setSolutionId(solutionId);
			}
			if(duplicateAccessCsList!=null &&  duplicateAccessCsList.size()==0){
				accessRoleIdList.put(cs.getRoleId(),cs.getSecurityTemplateName());
			}
			csListAccess.add(cs);
		}

		//deleting existing user privileges
		documentManagerHibernateFactory.getDocumentManagerDao().getdeleteAllPrivilegesForTheEntity(currentElement,solutionId);
		//deleting existing Role privileges
		documentManagerHibernateFactory.getDocumentManagerDao().getdeleteAllAccessRolePrivilegesForTheEntity(currentElement,solutionId);
		//saving
		documentManagerHibernateFactory.getDocumentManagerDao().saveOrUpdatePrivileges(csList);
		documentManagerHibernateFactory.getDocumentManagerDao().saveOrUpdatePrivilegesForAccessRole(csListAccess);

		//notify users.
		if(isNotificationRequired && csList.size()>0){
			List<Users> userList = new ArrayList<Users>();
			for(Integer accessRoleId:accessRoleIdList.keySet()){
				try {
					userList=getUserDetailsFromAccessRoleId(accessRoleId);
					for(Users u : userList){
						if(!existingUserIdList.contains(u.getUserId())){
							if(accessRoleUserList.get(u.getUserId())!=null){
								if((priorityMap.get(accessRoleUserList.get(u.getUserId()))<priorityMap.get(accessRoleIdList.get(accessRoleId)))){
									//adding only if priority is more than existing 
									accessRoleUserList.put(u.getUserId(),accessRoleIdList.get(accessRoleId));
								}
							}else{
								accessRoleUserList.put(u.getUserId(),accessRoleIdList.get(accessRoleId));
							}
						}
					}
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//merging distinct users to mail list
			for(Integer uid : accessRoleUserList.keySet()){
				if(!userIdList.containsKey(uid)){
					userIdList.put(uid, accessRoleUserList.get(uid));
				}
			}
			String fileType = csList.get(0).getContentTypeId();
			collaborationNotification.notifyShareDetails(userDetails, solutionId, currentOrgDetails, userIdList,currentElementDisplayName,fileType);	
		}

	}
	@Override
	public InputStream downloadFileForAVersion(String fileId, String solutionName,String versionId) throws NumberFormatException, NoSuchRepositoryException, VyasaBusinessException, RepositoryException {
		logger.info("EXEFLOW - DocumentManagerBoImpl - downloadFileForAVersion()");
		File file = documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(fileId);
		String modeShapePath = file.getPackageLocation().replace("###", "/");

		InputStream content = documentStore.getFileByPathNameAndVersion(getRepositoryName(), solutionName, modeShapePath, fileId, Integer.parseInt(versionId));

		return content;
	}

	@Override
	public List<ContentSecurityDetails> getListOfPrivilegesForEntity(String currentUid,
			String parentUid, String fullPathUid,Integer solutionId) throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerBoImpl - getListOfPrivilegesForEntity()");
		return documentManagerHibernateFactory.getDocumentManagerDao().getListOfPrivilegesForEntity( currentUid,
				parentUid,  fullPathUid, solutionId);
	}


	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getAllVersionForFile(String fileId,String solutionName) throws Throwable{
		logger.info("EXEFLOW - DocumentManagerBoImpl - downloadFileForAVersion()");
		JSONArray versionsArray = new JSONArray();
		JSONObject versionObject;


		List<File> fileVersion = documentManagerHibernateFactory.getDocumentManagerDao().getAllVersionOfFile(fileId);
		Users user;

		for(File f:fileVersion){
			versionObject= new JSONObject();
			versionObject.put("versionNumber", f.getVersionNumber());
			versionObject.put("modifiedTime", f.getModifiedTime()!=null?f.getModifiedTime():"");
			if(f.getLastModifiedById()!=null){
				user= CollaborationUtils.getUserDetailById(f.getLastModifiedById());
				versionObject.put("lastModifiedBy", user.getFirstName()+" "+user.getLastName());
			}else {
				versionObject.put("lastModifiedBy", "");
			}

			versionsArray.add(versionObject);
		}
		return versionsArray;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getAllTemplateDetails() {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getAllTemplateDetails()");

		JSONArray templateArray = new JSONArray();

		List<DocumentTemplate> templates = documentManagerHibernateFactory.getDocumentManagerDao().getAllTemplate();
		List<TemplateProperties> templateProperties = new ArrayList<>();
		JsonObject template = new  JsonObject();
		Gson gson = new Gson();
		JsonArray properties;
		for(DocumentTemplate dt:templates){
			template = new  JsonObject();
			template=(JsonObject)new JsonParser().parse( gson.toJson(dt));
			templateProperties = documentManagerHibernateFactory.getDocumentManagerDao().getPropertiesForTemplate(dt.getTemplateId());
			properties= new JsonArray();
			properties =(JsonArray) new JsonParser().parse(gson.toJson(templateProperties));

			template.add("properties", properties);
			templateArray.add(template);
		}
		return templateArray;
	}

	@Override
	public boolean checkPrivateFolderExistenceForUser(String mY_CONTENTS_ROOT_UID, Integer userId, Integer orgId,Integer solutionID) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - checkPrivateFolderExistenceForUser()");
		return documentManagerHibernateFactory.getDocumentManagerDao().checkPrivateFolderExistenceForUser(mY_CONTENTS_ROOT_UID, userId, orgId, solutionID);
	}
	@Override
	public Directory checkPrivateFolderByNameForUser(String mY_CONTENTS_ROOT_UID, Integer userId, Integer orgId,Integer solutionID,String folderName) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - checkPrivateFolderExistenceForUser()");
		return documentManagerHibernateFactory.getDocumentManagerDao().checkPrivateFolderByNameForUser(mY_CONTENTS_ROOT_UID, userId, orgId, solutionID,folderName);
	}

	@Override
	public String fetchPrivateFolderUID(String mY_CONTENTS_ROOT_UID,Integer userId, Integer orgId, Integer solutionID) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - fetchPrivateFolderUID()");
		return documentManagerHibernateFactory.getDocumentManagerDao().fetchPrivateFolderUID(mY_CONTENTS_ROOT_UID, userId, orgId, solutionID);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getTemplatesForDirectory(String directoryId){
		logger.info("EXEFLOW - DocumentManagerBoImpl - getTemplatesForDirectory()");
		List<DirectoryTemplateLink> properties=documentManagerHibernateFactory.getDocumentManagerDao().templateForDirectory(directoryId);

		Set<Integer> distinctTemplates = new HashSet<>(); 
		JSONArray templates = new JSONArray();
		JSONObject template;
		for(DirectoryTemplateLink prop:properties){
			if(prop.getIsSecurityTemplate()!=1){
				distinctTemplates.add(prop.getTemplateId());
				template=new JSONObject();
				template.put("templateId", prop.getTemplateId());
				template.put("isSecurityTemplate", prop.getIsSecurityTemplate());
				templates.add(template);
			}
		}
		return templates;
	}

	@Override
	public JsonObject getPropertiesOfContent(String contentId,String contentType){
		logger.info("EXEFLOW - DocumentManagerBoImpl - getPropertiesOfContent()");
		Gson gson = new Gson();
		JsonObject info = new JsonObject();
		JsonArray properties= new JsonArray();
		List<ContentProperties> propertiesFromDb=new ArrayList<>();;
		if(TYPE_DIRECTORY.equalsIgnoreCase(contentType)){
			Directory directory = documentManagerHibernateFactory.getDocumentManagerDao().getDirectoryDetailsById(contentId);
			info.addProperty("name", directory.getDirectoryName());
			info.addProperty("desc", directory.getDirectoryDesc());

			propertiesFromDb = documentManagerHibernateFactory.getDocumentManagerDao()
					.getContentPropertyByContentIdAndVersion(contentId, 1, contentType);
		} else if(TYPE_FILE.equalsIgnoreCase(contentType)){
			File file = documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(contentId);
			info.addProperty("name", file.getFileName());
			info.addProperty("desc",file.getFileDesc());
			propertiesFromDb = documentManagerHibernateFactory.getDocumentManagerDao()
					.getContentPropertyByContentIdAndVersion(contentId, Integer.parseInt(file.getVersionNumber()), contentType);
		}

		properties.addAll((JsonArray)new JsonParser().parse(gson.toJson(propertiesFromDb)));
		info.add("properties", properties);
		return info;
	}

	@Override
	public JsonObject getPropertiesOfVersionedFile(String contentId,String contentType,Integer versionNumber){
		logger.info("EXEFLOW - DocumentManagerBoImpl - getPropertiesOfVersionedFile()");
		Gson gson = new Gson();
		JsonObject info = new JsonObject();
		JsonArray properties= new JsonArray();

		File file = documentManagerHibernateFactory.getDocumentManagerDao().getFileByVersion(contentId, versionNumber.toString());
		List<ContentProperties> propertiesFromDb = documentManagerHibernateFactory.getDocumentManagerDao()
				.getContentPropertyByContentIdAndVersion(contentId, versionNumber, contentType);
		info.addProperty("name", file.getFileName());
		info.addProperty("desc",file.getFileDesc());
		properties.addAll((JsonArray)new JsonParser().parse(gson.toJson(propertiesFromDb)));
		info.add("properties", properties);


		return info;
	}

	@Override
	public JSONArray getUserSecurityProfileForCurrentContext(Integer orgId,Integer userId,Integer solutionId) throws Throwable{

		List<SecurityDimensionMaster> sdm = SecurityDimensionMasterCache.getInstance().getAll();
		List<SecurityDimensionMaster> tsdm = sdm.stream().filter(col->col.getSolutionId().equals(solutionId)
				&& col.getDimensionType().equalsIgnoreCase("time")).collect(Collectors.toList());


		JSONArray userSecurityDetails = new JSONArray();
		JSONObject userSecDet;
		for(SecurityDimensionMaster tsf:tsdm){
			userSecDet=new JSONObject();
			userSecDet.put("dimensionId", tsf.getSecurityDimensionId());
			userSecDet.put("dimensionName", tsf.getDimensionName());
			userSecDet.put("dimensionBusinessName", tsf.getDimensionBusinessName());
			userSecDet.put("dimensionTableName", tsf.getDimensionTableName());
			userSecDet.put("dimensionType", tsf.getDimensionType());
			userSecDet.put("timeKey", tsf.getTimeKey());
			userSecurityDetails.add(userSecDet);
		}
		return userSecurityDetails;

	}


	private boolean resolveSecurityForContent(ContentProperties property,Integer userId,Integer orgId,Integer solutionId,String solutionName) throws Throwable{
		List<SecurityFilterWrapper> sfwl=getUserSecurityFilter(solutionId, userId, orgId,solutionName);
		List<SecurityFilterWrapper> timeSf = sfwl.stream().filter(col->col.getDimension().getDimensionType()
				.equalsIgnoreCase("TIME")).collect(Collectors.toList());
		Map<Integer,Set<Integer>> dimensionWiseTimeValues = getDistinctValuesForTimeDimension((JsonArray) new JsonParser().parse(property.getPropertyValue()));

		boolean isTrue=true;
		for(SecurityFilterWrapper tsf:timeSf){
			if(null !=dimensionWiseTimeValues.get(tsf.getDimension().getSecurityDimensionId())){
				for(Integer val:dimensionWiseTimeValues.get(tsf.getDimension().getSecurityDimensionId())){
					if(tsf.getTimeDimension().contains(val)){
						isTrue=true;
						break;
					} else {
						isTrue=false;
					}
				}
			}

		}

		return isTrue;
	}

	private Map<Integer,Set<Integer>> getDistinctValuesForTimeDimension(JsonArray securityValues){

		JsonObject prop;

		Map<Integer,Set<Integer>> distinctValues = new HashMap<Integer, Set<Integer>>();
		Integer to;
		Integer from;
		Integer dimensionId;
		Set<Integer> tempIds;
		for(JsonElement val:securityValues){
			prop=new JsonObject();
			prop=(JsonObject) val;
			dimensionId=Integer.parseInt(prop.get("dimensionId").toString().replace("\"", ""));
			to=Integer.parseInt(prop.get("toDate").toString().replace("\"", ""));
			from=Integer.parseInt(prop.get("fromDate").toString().replace("\"", ""));
			prop.get("format");
			if(distinctValues.containsKey(dimensionId)){
				tempIds=distinctValues.get(dimensionId);
				while(from<=to){
					tempIds.add(from);
					from++;

				}
				distinctValues.replace(dimensionId, tempIds);
			} else {
				tempIds= new HashSet<>();
				while(from<=to){
					tempIds.add(from);
					from++;
				}
				distinctValues.put(dimensionId, tempIds);
			}
		}

		return distinctValues;

	}

	private List<DocumentWrapper> returnFinalListAfterSecurityFilter(List<DocumentWrapper> contents,
			Integer userId,Integer orgId,Integer solutionId,String solutionName) throws Throwable{
		List<DocumentWrapper> finalContent= new ArrayList<>();
		List<ContentProperties> securityProperties;
		File file;
		boolean keepContent=false;
		for(DocumentWrapper dw:contents){
			keepContent=false;
			if(TYPE_FILE.equalsIgnoreCase(dw.getEntityType())){
				file=documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(dw.getEntityId());
				securityProperties = documentManagerHibernateFactory.getDocumentManagerDao().
						getContentPropertyByContentIdAndVersion(file.getDirectoryId(), 1, TYPE_DIRECTORY);
				if(securityProperties.size()>0){

					for(ContentProperties props:securityProperties){
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId,solutionName);
						if(keepContent){
							finalContent.add(dw);
						}
					}

				} else {
					finalContent.add(dw);
				}

			} else if(TYPE_DIRECTORY.equalsIgnoreCase(dw.getEntityType())){
				securityProperties = documentManagerHibernateFactory.getDocumentManagerDao().
						getContentPropertyByContentIdAndVersion(dw.getEntityId(), 1, TYPE_DIRECTORY);
				if(securityProperties.size()>0){
					for(ContentProperties props:securityProperties){
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId,solutionName);
						if(keepContent){
							finalContent.add(dw);
						}
					}
				} else {
					finalContent.add(dw);
				}
			}

		}
		return finalContent;
	}

	@Override
	public JsonArray getOrgForUserInCurrentContent(Integer solutionId,Integer userId,Integer orgId,String ORG_UNIT_DIMENSION_NAME,String solutionName) throws Throwable{
		JsonArray organizationsList = new JsonArray();
		JsonObject organizationJsonObject;
		List<SecurityFilterWrapper> userSecurityFilter = getUserSecurityFilter(solutionId,userId,  orgId,solutionName);
		for (SecurityFilterWrapper securityFilterWrapper : userSecurityFilter) {
			if(securityFilterWrapper.getDimension().getDimensionName().equals(ORG_UNIT_DIMENSION_NAME)) {
				Map<Integer,String> dimensionValues = securityFilterWrapper.getBkeyIds();
				if(dimensionValues.size() > 0) {
					for (Integer dimensionValueId  : dimensionValues.keySet()) {
						organizationJsonObject = new JsonObject();
						organizationJsonObject.addProperty("orgId", dimensionValueId);
						organizationJsonObject.addProperty("orgName",dimensionValues.get(dimensionValueId) );
						organizationsList.add(organizationJsonObject);
					}
				}
			}
		}

		return organizationsList;

	}

	@Override
	public String getPrivilegeByParentMyContent(String finalUidPath,String currentChoosenDir,Integer userId, Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getCollaborationSearchSharedContent");
		Map<String,String> mapOfPrivileges = new HashMap<String, String>();
		try{List<AccessRole> accessRole = getAccessRolesForCurrentUser(userId, orgId, solutionId);
		List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
		mapOfPrivileges=documentManagerHibernateFactory.getDocumentManagerDao().getPrivilegeByParentMyContent(finalUidPath, userId,  accessRoleIdList, orgId,solutionId);
		//adding default value.
		mapOfPrivileges.put(SHARED_ROOT_UID, CONSUMER);
		String [] pathUidArr = finalUidPath.split(SEPARATOR);
		//updating map based on parent if it doesn't have any privilege in table.
		for(int i=1;i<pathUidArr.length;i++){
			String curUid = pathUidArr[i];
			if(null==mapOfPrivileges.get(curUid)){
				mapOfPrivileges.put(curUid, mapOfPrivileges.get(pathUidArr[i-1]));
			}
		}
		}catch (VyasaBusinessException e) {
			logger.info("ERROR - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
			e.printStackTrace();

		}catch (Throwable e) {
			logger.info("ERROR - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
			e.printStackTrace();
		}
		return mapOfPrivileges.get(currentChoosenDir)==null?CONSUMER:mapOfPrivileges.get(currentChoosenDir);
	}

	@Override
	public List<ContentSecurityDetailsAccessRole> getListOfPrivilegesForEntityByAccessRole(String currentUid,
			String parentUid, String fullPathUid,Integer solutionId) throws VyasaBusinessException, Throwable{
		logger.info("EXEFLOW - DocumentManagerBoImpl - getListOfPrivilegesForEntityByAccessRole()");
		return documentManagerHibernateFactory.getDocumentManagerDao().getListOfPrivilegesForEntityByAccessRole( currentUid,
				parentUid,  fullPathUid, solutionId);
	}

	@Override
	public List<AccessRole> getListAccessRolesForOrg(Integer chosenOrgId) throws Throwable {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getListAccessRolesForOrg()");
		return getAccessRolesForOrgUnit(chosenOrgId);
	}

	private List<DocumentWrapperForSearch> returnFinalListAfterSecurityFilterForSearch(List<DocumentWrapperForSearch> contents,
			Integer userId,Integer orgId,Integer solutionId,String solutionName) throws Throwable{
		List<DocumentWrapperForSearch> finalContent= new ArrayList<DocumentWrapperForSearch>();
		List<ContentProperties> securityProperties;
		File file;
		boolean keepContent=false;
		for(DocumentWrapperForSearch dw:contents){
			keepContent=false;
			if(TYPE_FILE.equalsIgnoreCase(dw.getEntityType())){
				file=documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(dw.getEntityId());
				securityProperties = documentManagerHibernateFactory.getDocumentManagerDao().
						getContentPropertyByContentIdAndVersion(file.getDirectoryId(), 1, TYPE_DIRECTORY);
				if(securityProperties.size()>0){

					for(ContentProperties props:securityProperties){
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId,solutionName);
						if(keepContent){
							finalContent.add(dw);
						}
					}

				} else {
					finalContent.add(dw);
				}

			} else if(TYPE_DIRECTORY.equalsIgnoreCase(dw.getEntityType())){
				securityProperties = documentManagerHibernateFactory.getDocumentManagerDao().
						getContentPropertyByContentIdAndVersion(dw.getEntityId(), 1, TYPE_DIRECTORY);
				if(securityProperties.size()>0){
					for(ContentProperties props:securityProperties){
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId,solutionName);
						if(keepContent){
							finalContent.add(dw);
						}
					}
				} else {
					finalContent.add(dw);
				}
			}

		}
		return finalContent;
	}

	// methods from admin bo
	private List<AccessRole> getAccessRolesForCurrentUser(Integer userId, Integer orgId, Integer solutionId)
			throws Throwable {
		List<AccessRole> roles = (List<AccessRole>) CacheCoordinator
				.get(RedisKeys.CONFIGURED_FIN_USER_ACCESS_ROLE.getKey(), orgId + "_" + userId + "_" + solutionId);
		if (roles == null) {
			roles = new ArrayList<AccessRole>();
		}
		return roles;
	}

	private List<Users> getUserDetailsFromAccessRoleId(Integer accessRoleId) throws Throwable {
		return UsersCache.getInstance().getAll(documentManagerHibernateFactory.getDocumentManagerDao().getUsersByAccessRole(accessRoleId));
	}

	private List<SecurityFilterWrapper> getUserSecurityFilter(Integer solutionId, Integer userId, Integer orgId,String solutionName)
			throws Throwable {
		return documentManagerHibernateFactory.getDocumentManagerDao().getUserSecurityFilter(solutionId, userId, orgId,solutionName);
	}
	private  List<AccessRole> getAccessRolesForOrgUnit(Integer orgId) throws Throwable {
		List<AccessRole> accessRoles = new ArrayList<AccessRole>();

		OrganisationUnit orgUnit = OrgUnitCache.getInstance().get(orgId.longValue());

		Set<String> permSchemeIds = PermSchemeWrapperCache.getInstance()
				.getOrgToPermRelation(orgUnit.getOrgTypeId().toString());

		Set<String> orgAccessRoleIds = new HashSet<String>();
		for (String permId : permSchemeIds) {
			orgAccessRoleIds.addAll(AccessRoleWrapperCache.getInstance().getPermToRoleRelation(permId));
		}

		for (String accessRoleId : orgAccessRoleIds) {
			accessRoles.add(AccessRoleCache.getInstance().get(Long.parseLong(accessRoleId)));
		}
		return accessRoles;
	}
	@Override
	public Integer getSolutionIdByName(String solutionName){
		return documentManagerHibernateFactory.getDocumentManagerDao().getSolutionIdByName(solutionName);

	}

	@Override
	public Workbook generateValidationReport(List<CollaborationErrorLog> errorLogList) throws VyasaException {
		logger.info("EXEFLOW - DocumentManagerBoImpl - generateValidationReport()");
		Workbook workbook = new Workbook();
		int sheetIndex = 0;
		Worksheet worksheet = null;
		Style style=null;
		Cell header=null;
		StyleFlag flag=null;
		Style styleForWrap=null;
		try {
			//styling for wrapping text in each worksheet
			styleForWrap= new Style();
			styleForWrap.setTextWrapped(true);
			flag = new StyleFlag();
			flag.setWrapText(true);

			WorksheetCollection worksheets = workbook.getWorksheets();
			//if (workbook.getWorksheets().get("Validation Report") == null) {
			//sheetIndex = worksheets.add();
			worksheet = worksheets.get(sheetIndex);
			worksheet.setName("Validation Report");
			worksheet.getCells().setStandardWidth(50.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			worksheet.getCells().applyStyle(styleForWrap, flag);
			//}
			//setting style for validation report sheet
			String strColor="DDEBF7";
			Integer intColor = Integer.parseInt(strColor, 16);
			Color clr = Color.fromArgb(intColor);
			style = workbook.getStyles().get(workbook.getStyles().add());
			style.setPattern(BackgroundType.SOLID);
			style.setForegroundColor(clr);
			style.getFont().setSize(10);
			style.getFont().setBold(true);
			Cells cells = workbook.getWorksheets().get("Validation Report").getCells();
			header = cells.get("A1");
			header.putValue("Error Type");
			header.setStyle(style);

			header = cells.get("B1");
			header.putValue("Sheet Name");
			header.setStyle(style);

			header = cells.get("C1");
			header.putValue("Entity Name");
			header.setStyle(style);

			header = cells.get("D1");
			header.putValue("Error Message");
			style.getFont().setColor(Color.getRed());
			header.setStyle(style);

			CollaborationErrorLog errorLogForUploader;
			for (int i = 0; i < errorLogList.size(); i++) {
				errorLogForUploader = errorLogList.get(i);
				for (int j = 0; j < 4; j++) {
					if (j == 0) {
						Cell cell1 = cells.get(i + 1, j);
						cell1.setValue(errorLogForUploader.getErrorType());
					}
					if (j == 1) {
						Cell cell2 = cells.get(i + 1, j);
						cell2.setValue(errorLogForUploader.getSheetName());
					}

					if (j == 2) {
						Cell cell3 = cells.get(i + 1, j);

						cell3.setValue(errorLogForUploader.getEntityName());

					}

					if (j == 3) {
						Cell cell4 = cells.get(i + 1, j);
						cell4.setValue(errorLogForUploader.getErrorMessage());
					}

				}
			}
		} catch (Exception e) {
			throw new VyasaException(e);
		}
		return workbook;
	}

	@Override
	public List<CollaborationErrorLog> validateHeaders(Workbook workBook) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - generateValidationReport()");
		List<CollaborationErrorLog> errorList = new ArrayList<CollaborationErrorLog>();

		List<String> directoryHeadersFromProperty=Arrays.asList(Directory_Headers.toLowerCase().split(","));
		List<String> directorySecurityHeadersFromProperty=Arrays.asList(Directory_Security_Headers.toLowerCase().split(","));;
		List<String> fileHeadersFromProperty=Arrays.asList(File_Headers.toLowerCase().split(","));;
		List<String> filePropertiesHeadersFromProperty=Arrays.asList(File_Properties_Headers.toLowerCase().split(","));;
		List<String> contentShareHeadersFromProperty=Arrays.asList(Content_Share_Headers.toLowerCase().split(","));;


		Worksheet worksheetForDirectory = workBook.getWorksheets().get(Directory);
		Worksheet worksheetForDirectorySecurity = workBook.getWorksheets().get(Directory_Security);
		Worksheet worksheetForFile = workBook.getWorksheets().get(File);
		Worksheet worksheetForFileProperties = workBook.getWorksheets().get(File_Properties);
		Worksheet worksheetForContentShare = workBook.getWorksheets().get(Content_Share);

		List<String> directoryHeaders=getHeaderFromSheet(worksheetForDirectory);
		List<String> directorySecurityHeaders=getHeaderFromSheet(worksheetForDirectorySecurity);
		List<String> fileHeaders=getHeaderFromSheet(worksheetForFile);
		List<String> filePropertiesHeaders=getHeaderFromSheet(worksheetForFileProperties);
		List<String> contentShareHeaders=getHeaderFromSheet(worksheetForContentShare);

		errorList.addAll(equalHeaderLists(directoryHeadersFromProperty, directoryHeaders, Directory));
		errorList.addAll(equalHeaderLists(directorySecurityHeadersFromProperty, directorySecurityHeaders, Directory_Security));
		errorList.addAll(equalHeaderLists(fileHeadersFromProperty, fileHeaders, File));
		errorList.addAll(equalHeaderLists(filePropertiesHeadersFromProperty, filePropertiesHeaders, File_Properties));
		errorList.addAll(equalHeaderLists(contentShareHeadersFromProperty, contentShareHeaders, Content_Share));

		return errorList;
	}

	private List<String> getHeaderFromSheet(Worksheet workSheet){
		List<String> headerFromSheet=new ArrayList<String>();
		Cells cells = workSheet.getCells();
		cells.deleteBlankColumns();
		int colCount = workSheet.getCells().getMaxColumn();
		for (int i = 0; i <= colCount; i++) {
			headerFromSheet.add(cells.get(0, i).getValue().toString().trim().toLowerCase());
		}
		return headerFromSheet;
	}
	private List<CollaborationErrorLog> equalHeaderLists(List<String> templateHeaderList, List<String> sheetHeaderList,String sheetName){ 
		List<CollaborationErrorLog> elist= new ArrayList<CollaborationErrorLog>();
		if( templateHeaderList.size() != sheetHeaderList.size()){
			CollaborationErrorLog log= new CollaborationErrorLog();
			log.setEntityName("-");
			log.setErrorMessage("Mismatch in number of columns");
			log.setSheetName(sheetName);
			log.setErrorType("Template Error");
			elist.add(log);
			return elist;
		}else{
			CollaborationErrorLog log= null;
			for(int i=0;i<templateHeaderList.size();i++){
				if(!templateHeaderList.get(i).equals(sheetHeaderList.get(i))){
					log=new CollaborationErrorLog();
					log.setEntityName("-");
					log.setErrorMessage("Expecting \""+templateHeaderList.get(i) +
							"\" as header at \""+sheetHeaderList.get(i)+"\"");
					log.setSheetName(sheetName);
					log.setErrorType("Template Error");
					elist.add(log);
				}
			}      
			return elist;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CollaborationErrorLog> processBulkUpload(
			Workbook uploadedWorkbook, Integer orgId, String fileLocation,
			String fileName, Integer solutionId, String solutionName,
			Integer userId, String userName,Directory migrationParentDirectory) throws Throwable {
		logger.info("EXEFLOW - DocumentManagerBoImpl - processBulkUpload()");
		List<CollaborationErrorLog> mainErrorLog = new ArrayList<CollaborationErrorLog>();

		//processing the excel and forming respective dto.
		Worksheet worksheetForDirectory = uploadedWorkbook.getWorksheets().get(Directory);
		Worksheet worksheetForDirectorySecurity = uploadedWorkbook.getWorksheets().get(Directory_Security);
		Worksheet worksheetForFile = uploadedWorkbook.getWorksheets().get(File);
		Worksheet worksheetForFileProperties = uploadedWorkbook.getWorksheets().get(File_Properties);
		Worksheet worksheetForContentShare = uploadedWorkbook.getWorksheets().get(Content_Share);

		Map<String,Integer> solutionNameIdMap = documentManagerHibernateFactory.getDocumentManagerDao().getAllSolutionMap();
		Map<String,Integer> templateIdMap = documentManagerHibernateFactory.getDocumentManagerDao().getAllTemplate().stream().
				collect(Collectors.toMap( dw->dw.getTemplateName(),dw->dw.getTemplateId()));
		String repositoryName =getRepositoryName();
		//try removing the dependency of this map
		//Map<String,String> directoryNameLevelUUIDMap = null;
		//Map<String,String> directoryNameLevelUUIDMap = documentManagerHibernateFactory.getDocumentManagerDao().getAllDirectory();
		List<DirectoryForUpload> existingFolders= documentManagerHibernateFactory.getDocumentManagerDao().getAllDirectory();
		SortedSet<Integer> distinctLevels = new TreeSet<Integer>();
		existingFolders.stream().filter(p -> distinctLevels.add(p.getHierarchyLevel())).collect(Collectors.toList());
		CollaborationNode rootNode = new CollaborationNode("ROOT","ROOT","");
		for(Integer i:distinctLevels){
			rootNode = prepareTreeStructureForExistingFolders(existingFolders.stream()
					.filter(dw->dw.getHierarchyLevel().equals(i)).collect(Collectors.toList()),rootNode);
		}
		//share objs
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		List<ContentSecurityAccessRole> csAccessList = new ArrayList<ContentSecurityAccessRole>();


		//processing directory.
		Object[] directoryDetails = extractDirectoryDetails(
				worksheetForDirectory,worksheetForDirectorySecurity, orgId, fileLocation,
				fileName, solutionId, solutionName,
				userId, userName,solutionNameIdMap,templateIdMap,rootNode,migrationParentDirectory);

		List<Directory> directoryList = (List<Directory>) directoryDetails[0];
		rootNode =  (CollaborationNode) directoryDetails[1];
		List<DirectoryTemplateLink> directoryTemplate = (List<DirectoryTemplateLink>) directoryDetails[2];
		List<ContentProperties> directorySecurityProperties =(List<ContentProperties>) directoryDetails[3];
		mainErrorLog.addAll((List<CollaborationErrorLog>)directoryDetails[4]);
		LinkedHashMap<Integer,List<Directory>> levelAndDirectoriesToBeProcessed =  (LinkedHashMap<Integer, List<Directory>>) directoryDetails[5];





		//processing directory share
		List<ShareDetailsForUpload> directoryShareDetails = extractContentSecurityForDirectories(worksheetForContentShare, orgId, 
				solutionId, solutionName,
				userId, userName,solutionNameIdMap,rootNode);
		Object[] securityList = processShareObjectsForDirectory(directoryShareDetails.stream().filter(s->s.getContentType()
				.equalsIgnoreCase("DIRECTORY")).collect(Collectors.toList()),rootNode,solutionId);
		csList.addAll((List<ContentSecurity>) securityList[0]);
		csAccessList.addAll((List<ContentSecurityAccessRole>) securityList[1]);
		mainErrorLog.addAll((List<CollaborationErrorLog>)securityList[2]);


		/*
		 * processing files
		 */

		Object[] files = extractFileDetails(worksheetForFile, worksheetForFileProperties, orgId, fileLocation,
				fileName, solutionId, solutionName, userId, userName, solutionNameIdMap, directoryShareDetails, directoryTemplate, rootNode, migrationParentDirectory);

		List<CollaborationErrorLog> err =  (List<CollaborationErrorLog>) files[0];
		List<File> fileList = (List<com.fintellix.framework.collaboration.dto.File>) files[1];

		directorySecurityProperties.addAll((Collection<? extends ContentProperties>) files[2]);
		csList.addAll((Collection<? extends ContentSecurity>) files[3]);
		csAccessList.addAll((Collection<? extends ContentSecurityAccessRole>) files[4]);

		mainErrorLog.addAll(err);


		if(mainErrorLog.size()==0){
			//saving all the details.

			//processing directories by node levels in order to make valid entries in mode shape.
			/*for (Map.Entry<Integer, List<Directory>> entry : levelAndDirectoriesToBeProcessed.entrySet()){
				//updating migrationRootdirectory path and details.
				String migId = migrationParentDirectory.getDirectoryId();
				String migParentPath = migrationParentDirectory.getPackageLocation()+SEPARATOR+migId;
				for(Directory d: entry.getValue()){
					if(entry.getKey()==0){
						d.setParentDirectoryId(migId);
						d.setPackageLocation(migParentPath);
					}else if(!d.getPackageLocation().contains(migrationParentDirectory.getPackageLocation())){
						d.setPackageLocation(migParentPath+SEPARATOR+d.getPackageLocation());
					}

				}
				//documentStore.createBulkFoldersByPath(repositoryName, solutionName,  entry.getValue());
			}*/
			documentManagerHibernateFactory.getDocumentManagerDao().saveAllDirectories(directoryList);
			documentManagerHibernateFactory.getDocumentManagerDao().saveAllDirectoryTemplateLink(directoryTemplate);
			documentManagerHibernateFactory.getDocumentManagerDao().saveAllDirectorySecurityProperties(directorySecurityProperties);
			documentManagerHibernateFactory.getDocumentManagerDao().saveAllFiles(fileList);

			Set<String> directoryAccessList = new HashSet<>();
			if(csList.size()>0){
				directoryAccessList.addAll(csList.
						stream().map(dw->dw.getContentId()).collect(Collectors.toList()));
			}

			if(csAccessList.size()>0){
				directoryAccessList.addAll(csAccessList.
						stream().map(dw->dw.getContentId()).collect(Collectors.toList()));
			}

			if(!directoryAccessList.isEmpty()){
				documentManagerHibernateFactory.getDocumentManagerDao().getdeleteAllPrivilegesForTheListOfEntitiesAccessRole(new ArrayList<>(directoryAccessList),solutionId);
				documentManagerHibernateFactory.getDocumentManagerDao().getdeleteAllPrivilegesForTheListOfEntities(new ArrayList<>(directoryAccessList),solutionId);
			}

			documentManagerHibernateFactory.getDocumentManagerDao().saveOrUpdatePrivileges(csList);
			documentManagerHibernateFactory.getDocumentManagerDao().saveOrUpdatePrivilegesForAccessRole(csAccessList);


			documentStore.createBulkFoldersByPath(repositoryName, solutionName,  levelAndDirectoriesToBeProcessed,fileList);
		}


		//list to save.
		//directoryList
		//directoryTemplate
		//directorySecurityProperties
		return mainErrorLog;
	}

	private Object[] processShareObjectsForDirectory(
			List<ShareDetailsForUpload> collect, CollaborationNode rootNode,Integer solutionId) {
		Object[] res = new Object[3];
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		List<ContentSecurityAccessRole> csAccessList = new ArrayList<ContentSecurityAccessRole>();
		List<CollaborationErrorLog> errList = new ArrayList<CollaborationErrorLog>();
		CollaborationErrorLog err =null;
		ContentSecurity cs = null;
		ContentSecurityAccessRole csr = null;
		String currUUID = null;
		Set<String> userPermCheck = new HashSet<String>();
		Set<String> rolePermCheck = new HashSet<String>();
		for(ShareDetailsForUpload sd : collect){
			logger.info("validating share details for --- " + sd.getContentName());
			currUUID = validatePathAndFolderExistence(sd.getDirectoryPath()
					, sd.getContentName(), rootNode);
			if(currUUID!=null){
				if(null!=sd.getSharedOrganisationName()){
					if(null!=sd.getRoleOrUserId()){
						if(priorityMap.containsKey(sd.getPrivilege().toUpperCase())){
							if(sd.getShareWith().equalsIgnoreCase("ROLE")){
								//role
								if(!rolePermCheck.contains(currUUID+SEPARATOR+sd.getRoleOrUserId()+SEPARATOR+sd.getSharedOrganisationName())){
									csr=new ContentSecurityAccessRole();
									csr.setContentId(currUUID);
									csr.setContentSecurityId(UUID.randomUUID().toString());
									csr.setContentTypeId("DIRECTORY");
									csr.setOrgId(sd.getSharedOrganisationName());
									csr.setSecurityTemplateName(sd.getPrivilege());
									csr.setSolutionId(solutionId);
									csr.setRoleId(sd.getRoleOrUserId());
									rolePermCheck.add(currUUID+SEPARATOR+sd.getRoleOrUserId()+SEPARATOR+sd.getSharedOrganisationName());
									csAccessList.add(csr);
								}else{
									err = new CollaborationErrorLog();
									err.setEntityName(sd.getContentName());
									err.setErrorType("Validation Error");
									err.setSheetName(Content_Share);
									err.setErrorMessage("Duplicate permissions found for a role on the current entity.");
									errList.add(err);
								}

							}else{
								//user
								if(!userPermCheck.contains(currUUID+SEPARATOR+sd.getRoleOrUserId()+SEPARATOR+sd.getSharedOrganisationName())){
									cs=new ContentSecurity();
									cs.setContentId(currUUID);
									cs.setContentSecurityId(UUID.randomUUID().toString());
									cs.setContentTypeId("DIRECTORY");
									cs.setOrgId(sd.getSharedOrganisationName());
									cs.setSecurityTemplateName(sd.getPrivilege());
									cs.setSolutionId(solutionId);
									cs.setUserId(sd.getRoleOrUserId());
									userPermCheck.add(currUUID+SEPARATOR+sd.getRoleOrUserId()+SEPARATOR+sd.getSharedOrganisationName());
									csList.add(cs);
								}else{
									err = new CollaborationErrorLog();
									err.setEntityName(sd.getContentName());
									err.setErrorType("Validation Error");
									err.setSheetName(Content_Share);
									err.setErrorMessage("Duplicate permissions found for a user on the current entity.");
									errList.add(err);
								}
							}
						}else{
							err = new CollaborationErrorLog();
							err.setEntityName(sd.getContentName());
							err.setErrorType("Validation Error");
							err.setSheetName(Content_Share);
							err.setErrorMessage("Invalid permission name.");
							errList.add(err);
						}

					}else{
						err = new CollaborationErrorLog();
						err.setEntityName(sd.getContentName());
						err.setErrorType("Validation Error");
						err.setSheetName(Content_Share);
						err.setErrorMessage("Invalid role/ user name.");
						errList.add(err);
					}
				}else{
					err = new CollaborationErrorLog();
					err.setEntityName(sd.getContentName());
					err.setErrorType("Validation Error");
					err.setSheetName(Content_Share);
					err.setErrorMessage("Invalid organisation name.");
					errList.add(err);
				}
			}else{
				err = new CollaborationErrorLog();
				err.setEntityName(sd.getContentName());
				err.setErrorType("Validation Error");
				err.setSheetName(Content_Share);
				err.setErrorMessage("Invalid Path / Error processing the parent path.");
				errList.add(err);
			}
		}


		res[0] = csList;
		res[1] = csAccessList;
		res[2] = errList;
		return res;
	}

	@SuppressWarnings("unchecked")
	private Object[] extractDirectoryDetails(Worksheet worksheetForDirectory,Worksheet worksheetForDirectorySecurity,
			Integer orgId, String fileLocation, String fileName,
			Integer solutionId, String solutionName, Integer userId,
			String userName,Map<String,Integer> solutionNameIdMap,Map<String ,Integer>templateIdMap,CollaborationNode rootNode,Directory migrationParentDirectory) throws Throwable {
		List<DirectoryForUpload> directoryList =  new ArrayList<DirectoryForUpload>();
		LinkedHashMap<Integer,List<Directory>> levelAndDirectoriesToBeProcessed = new LinkedHashMap<Integer, List<Directory>>();
		List<DirectoryTemplateLink> directoryTemplateLinkList = new ArrayList<DirectoryTemplateLink>();
		List<ContentProperties> directorySecurityProperties = new ArrayList<ContentProperties>();
		List<CollaborationErrorLog> directoryErrors = new ArrayList<CollaborationErrorLog>();
		Object[] finalArr = new Object[7];
		List<DocumentTemplate> templates = documentManagerHibernateFactory.getDocumentManagerDao().getAllTemplate();
		Map<Integer,List<TemplateProperties>> templateIdAndProperties = new HashMap<Integer, List<TemplateProperties>>();
		for(DocumentTemplate dt : templates){
			templateIdAndProperties.put(dt.getTemplateId(), documentManagerHibernateFactory.getDocumentManagerDao().getPropertiesForTemplate(dt.getTemplateId()));
		}


		//processing directories
		directoryList =processDirectory(worksheetForDirectory,solutionNameIdMap,userId,templateIdMap,solutionId,solutionName,orgId);
		SortedSet<Integer> distinctLevels = new TreeSet<Integer>();
		directoryList.stream().filter(p -> distinctLevels.add(p.getHierarchyLevel())).collect(Collectors.toList());
		Object[] tempRes = new Object[3];
		List<Directory> directoryMainDto =  new ArrayList<Directory>();

		for(Integer hierarchyLevel :distinctLevels){
			tempRes = validateAndGetFinalizedDtoByHierarchy(directoryList.stream()
					.filter(dw->dw.getHierarchyLevel().equals(hierarchyLevel)).collect(Collectors.toList())
					,rootNode, migrationParentDirectory);
			directoryMainDto.addAll((List<Directory>)tempRes[0]);
			directoryErrors.addAll((List<CollaborationErrorLog>)tempRes[1]);
			rootNode = (CollaborationNode) tempRes[2];
			directoryTemplateLinkList.addAll((List<DirectoryTemplateLink>) tempRes[3]);
			levelAndDirectoriesToBeProcessed.put(hierarchyLevel, (List<Directory>)tempRes[0]);
		}

		//preparing directory security
		tempRes = processDirectorySecurity(worksheetForDirectorySecurity,solutionNameIdMap,userId,templateIdMap,rootNode,templates,templateIdAndProperties,solutionId,solutionName,orgId,
				directoryMainDto.stream().map(dw->dw.getDirectoryId()).collect(Collectors.toList()));
		directorySecurityProperties = (List<ContentProperties>) tempRes[0];
		directoryErrors.addAll((List<CollaborationErrorLog>)tempRes[1]);
		directoryTemplateLinkList.addAll((List<DirectoryTemplateLink>) tempRes[2]);

		finalArr[0] = directoryMainDto;
		finalArr[1] = rootNode;
		finalArr[2] = directoryTemplateLinkList;
		finalArr[3] = directorySecurityProperties;
		finalArr[4] = directoryErrors;
		finalArr[5] = levelAndDirectoriesToBeProcessed;


		return finalArr;
	}

	private String validatePathAndFolderExistence(String parentPath,
			String currentFileName, CollaborationNode rootNode) {

		if(parentPath!=null && !"".equalsIgnoreCase(parentPath)){
			parentPath = (String) CollaborationProperties.get("app.organizationName")+"/"+(String) CollaborationProperties.get("app.rootFolderName")+"/"+parentPath;
		} else {
			parentPath = (String) CollaborationProperties.get("app.organizationName")+"/"+(String) CollaborationProperties.get("app.rootFolderName");
		}

		List<String> pathBreak = new ArrayList<String>();
		pathBreak.addAll(Arrays.asList(parentPath.split("/")));
		pathBreak.add(currentFileName);
		CollaborationNode currentNode = rootNode;
		List<CollaborationNode> temp = null;
		for(String p : pathBreak){
			if(null!=currentNode){
				temp = currentNode.getChildrenList().stream().filter(cn->cn.getNodeBusinessName().equals(p)).collect(Collectors.toList());
				currentNode=temp.isEmpty()?null:temp.get(0);
			}else{
				return null;
			}
		}
		return currentNode.getNodeId();
	}

	private Object[] validateAndGetFinalizedDtoByHierarchy(
			List<DirectoryForUpload> collect,CollaborationNode rootNode,Directory migrationParentDirectory) {
		List<Directory> directoryList =  new ArrayList<Directory>();
		List<DirectoryTemplateLink> directoryTemplateLink =  new ArrayList<DirectoryTemplateLink>();
		DirectoryTemplateLink dtl = new DirectoryTemplateLink();
		List<CollaborationErrorLog> directoryErrors = new ArrayList<CollaborationErrorLog>();
		CollaborationErrorLog err = null;
		Object[] finalArr = new Object[4];
		CollaborationNode newNode = null;
		List<String> pathFolders = null;
		List<CollaborationNode> temp = null;
		String packagePath = null;
		String path;
		boolean errFlag = true;
		for(DirectoryForUpload d : collect){
			logger.info("validating direcotry details for --- " + d.getDirectoryName());
			if(null==d.getTemplateId()){
				err = new CollaborationErrorLog();
				err.setEntityName(d.getDirectoryName());
				err.setErrorType("Validation Error");
				err.setSheetName(Directory);
				err.setErrorMessage("Invalid template.");
				directoryErrors.add(err);
				errFlag=false;
			}
			if(null==d.getSolutionId()){
				err = new CollaborationErrorLog();
				err.setEntityName(d.getDirectoryName());
				err.setErrorType("Validation Error");
				err.setSheetName(Directory);
				err.setErrorMessage("Invalid solution name.");
				directoryErrors.add(err);
				errFlag=false;
			}
			if(null==d.getOrgId()){
				err = new CollaborationErrorLog();
				err.setEntityName(d.getDirectoryName());
				err.setErrorType("Validation Error");
				err.setSheetName(Directory);
				err.setErrorMessage("Invalid organisation name.");
				directoryErrors.add(err);
				errFlag=false;
			}
			if(errFlag){

				if(d.getPackageLocation()!=null && !"".equalsIgnoreCase(d.getPackageLocation())){
					path=(String) CollaborationProperties.get("app.organizationName")+"/"+(String) CollaborationProperties.get("app.rootFolderName")+"/"+d.getPackageLocation();
				} else {
					path=(String) CollaborationProperties.get("app.organizationName")+"/"+(String) CollaborationProperties.get("app.rootFolderName");
				}
				pathFolders = Arrays.asList(path.split("/"));
				CollaborationNode currentNode = rootNode;
				for(String p : pathFolders){
					if(null!=currentNode){
						temp = currentNode.getChildrenList().stream().filter(cn->cn.getNodeBusinessName().equals(p)).collect(Collectors.toList());
						currentNode=temp.isEmpty()?null:temp.get(0);
					}else{
						err = new CollaborationErrorLog();
						err.setEntityName(d.getDirectoryName());
						err.setErrorType("Validation Error");
						err.setSheetName(Directory);
						err.setErrorMessage("Invalid path.");
						directoryErrors.add(err);
						break;
					}
				}
				if(null!=currentNode){
					if(checkFolderDuplicateAtCurrentNode(currentNode,d.getDirectoryName())){
						d.setDirectoryId(UUID.randomUUID().toString());
						d.setParentDirectoryId(currentNode.getNodeId());
						packagePath = "".equalsIgnoreCase(currentNode.getParentPathUUID())?currentNode.getNodeId():currentNode.getParentPathUUID()+SEPARATOR+currentNode.getNodeId();
						d.setPackageLocation(packagePath);
						newNode = new CollaborationNode(d.getDirectoryName(),d.getDirectoryId(), packagePath);
						currentNode.getChildrenList().add(newNode);
						directoryList.add(new Directory(d));
						if(!d.getTemplateId().equals(-999)){
							dtl = new DirectoryTemplateLink();
							dtl.setDirectoryId(d.getDirectoryId());
							dtl.setIsSecurityTemplate(0);
							dtl.setTemplateId(d.getTemplateId());
							directoryTemplateLink.add(dtl);
						}
					}else{

						/*ignoring existing folders 
						 * 
						 * 
						err = new CollaborationErrorLog();
						err.setEntityName(d.getDirectoryName());
						err.setErrorType("Validation Error");
						err.setSheetName(Directory);
						err.setErrorMessage("Duplicate directory name.");
						directoryErrors.add(err);*/
					}
				}else{
					err = new CollaborationErrorLog();
					err.setEntityName(d.getDirectoryName());
					err.setErrorType("Validation Error");
					err.setSheetName(Directory);
					err.setErrorMessage("Invalid path.");
					directoryErrors.add(err);
				}

			}
		}
		finalArr[0] = directoryList;
		finalArr[1] = directoryErrors;
		finalArr[2] = rootNode;
		finalArr[3] = directoryTemplateLink;
		return finalArr;

	}

	private boolean checkFolderDuplicateAtCurrentNode(CollaborationNode currentNode,
			String directoryName) {
		List<CollaborationNode> tempNode = currentNode.getChildrenList().stream().filter(cn->cn.getNodeBusinessName().toLowerCase().equals(directoryName.toLowerCase())).collect(Collectors.toList());
		if(tempNode.isEmpty()){
			return true;	
		}else{
			return false;
		}
	}

	private CollaborationNode prepareTreeStructureForExistingFolders(
			List<DirectoryForUpload> collect, CollaborationNode rootNode) {
		CollaborationNode newNode = null;
		List<String> pathFolders = null;
		List<CollaborationNode> temp = null;
		String path;
		for(DirectoryForUpload du :collect ){
			if(du.getParentDirectoryId()==null){
				newNode = new CollaborationNode(du.getDirectoryName(), du.getDirectoryId(),du.getPackageLocation());
				rootNode.getChildrenList().add(newNode);
			}else{
				path = du.getPackageLocation();
				pathFolders = Arrays.asList(path.split(SEPARATOR));
				CollaborationNode currentNode = rootNode;
				for(String p : pathFolders){
					if(null!=currentNode){
						temp = currentNode.getChildrenList().stream().filter(cn->cn.getNodeId().equals(p)).collect(Collectors.toList());
						currentNode=temp.isEmpty()?null:temp.get(0);
					}

				}
				if(null!=currentNode){
					newNode = new CollaborationNode(du.getDirectoryName(), du.getDirectoryId(),du.getPackageLocation());
					currentNode.getChildrenList().add(newNode);
				}
			}
		}
		return rootNode;
	}

	private Object[] extractShareDetails(Worksheet worksheetForContentShare,
			Integer orgId, String fileLocation, String fileName,
			Integer solutionId, String solutionName, Integer userId,
			String userName, Map<String, String> directoryNameLevelUUIDMap,
			Map<String, String> fileNameLevelUUIDMap,Map<String,Integer> solutionNameIdMap) {
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		List<ContentSecurityAccessRole> csAccessList = new ArrayList<ContentSecurityAccessRole>();
		Object[] finalArr = new Object[2];


		finalArr[0] =csList;
		finalArr[1] =csAccessList;
		return finalArr;

	}

	private Object[] extractFileDetails(
			Worksheet worksheetForFile, Worksheet worksheetForFileProperties,Integer orgId, String fileLocation,
			String fileName, Integer solutionId, String solutionName,
			Integer userId, String userName,Map<String,Integer> solutionNameIdMap,List<ShareDetailsForUpload> directoryShareDetails,List<DirectoryTemplateLink> dtl
			,CollaborationNode rootNode,Directory migrationParentDirectory) {
		CollaborationErrorLog collaborationErrorLog;
		List<File> files = new ArrayList<>();
		File document;

		List<CollaborationErrorLog> fileErrorLogs = new ArrayList<>();

		List<FileForUpload> fileList = new ArrayList<FileForUpload>();
		List<FileContentProperties> fileProperties = new ArrayList<>();
		List<ContentProperties> contentProperties = new ArrayList<>();
		ContentProperties cp;


		Object[] finalArr = new Object[5];
		Map<Integer,List<TemplateProperties>> templateProperties = getAllTemplatePropertiesMap();
		List<ContentSecurity> csList = new ArrayList<ContentSecurity>();
		List<ContentSecurityAccessRole> csAccessList = new ArrayList<ContentSecurityAccessRole>();
		CollaborationErrorLog err =null;
		ContentSecurity cs = null;
		ContentSecurityAccessRole csr = null;


		fileList = getFileUploadListFromWorkSheet(worksheetForFile, solutionNameIdMap, userId, solutionId, solutionName, orgId);
		fileProperties = getFileContentPropListFromWorkSheet(worksheetForFileProperties, solutionNameIdMap, userId, solutionId, solutionName, orgId);

		java.io.File file;
		List<DirectoryTemplateLink> tempDtl;
		List<FileContentProperties> propertiesForAFile;
		List<FileContentProperties> property;
		List<ShareDetailsForUpload> sharedDetailsListForAFile;
		boolean error=false;
		List<FileForUpload> tempFileList = new ArrayList<>();

		for(FileForUpload fileUp:fileList){
			logger.info("validating file details for --- " + fileUp.getFileName());
			error=false;
			CollaborationNode directory=validateFolderForFile(fileUp.getPackageLocation(),rootNode);
			file = new java.io.File(fileUp.getActualPath());
			propertiesForAFile = fileProperties.stream().filter(prop->prop.getFileName().equals(fileUp.getFileName())
					&& prop.getPackageLocation().equals(fileUp.getPackageLocation())).collect(Collectors.toList());

			sharedDetailsListForAFile = directoryShareDetails.stream().filter(col->col.getContentType().equalsIgnoreCase("FILE")
					&& col.getContentName().equals(fileUp.getFileName()) && col.getDirectoryPath().equals(fileUp.getPackageLocation())).collect(Collectors.toList());

			if(!file.exists()){
				collaborationErrorLog = new CollaborationErrorLog();
				collaborationErrorLog.setEntityName(fileUp.getFileName());
				collaborationErrorLog.setErrorType("Validation Error");
				collaborationErrorLog.setErrorMessage("File is not present at source location - "+fileUp.getActualPath());
				collaborationErrorLog.setSheetName(File);
				fileErrorLogs.add(collaborationErrorLog);
				error=true;
			} else{
				String fileType=file.getName().substring(file.getName().lastIndexOf(".")+1);
				if(!FILE_FORMATS_SET.contains(fileType.toUpperCase())){
					collaborationErrorLog = new CollaborationErrorLog();
					collaborationErrorLog.setEntityName(fileUp.getFileName());
					collaborationErrorLog.setErrorType("Validation Error");
					collaborationErrorLog.setErrorMessage("File format "+fileType.toUpperCase()+" is not allowed.");
					collaborationErrorLog.setSheetName(File);
					fileErrorLogs.add(collaborationErrorLog);
					error=true;
				}

			}

			tempFileList= fileList.stream().filter(col->col.getPackageLocation().equalsIgnoreCase(fileUp.getPackageLocation())
					&& col.getFileName().equalsIgnoreCase(fileUp.getFileName())).collect(Collectors.toList());

			if(tempFileList.size()>1){
				collaborationErrorLog = new CollaborationErrorLog();
				collaborationErrorLog.setEntityName(fileUp.getFileName());
				collaborationErrorLog.setErrorType("Validation Error");
				collaborationErrorLog.setErrorMessage("Duplicate file at - "+fileUp.getPackageLocation());
				collaborationErrorLog.setSheetName(File);
				fileErrorLogs.add(collaborationErrorLog);
				error=true;
			}

			if(directory!=null){
				tempDtl = dtl.stream().filter(col->col.getDirectoryId().equalsIgnoreCase(directory.getNodeId()) && col.getIsSecurityTemplate().equals(0))
						.collect(Collectors.toList());
				if(tempDtl.isEmpty()){
					tempDtl = documentManagerHibernateFactory.getDocumentManagerDao().templateForDirectory(directory.getNodeId());
				}

				File tempFile =documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsIfExistByName(fileUp.getFileName(), solutionId, directory.getNodeId());
				if(tempFile!=null){
					collaborationErrorLog = new CollaborationErrorLog();
					collaborationErrorLog.setEntityName(fileUp.getFileName());
					collaborationErrorLog.setErrorType("Validation Error");
					collaborationErrorLog.setErrorMessage("Duplicate file at - "+fileUp.getPackageLocation());
					collaborationErrorLog.setSheetName(File);
					fileErrorLogs.add(collaborationErrorLog);
					error=true;
				}
				/*
				 * validating properties for file
				 */
				if(!tempDtl.isEmpty()){

					if(!propertiesForAFile.isEmpty()){

						for(FileContentProperties fcp:propertiesForAFile){

							boolean match=false;
							for(TemplateProperties tempProp:templateProperties.get(tempDtl.get(0).getTemplateId())){
								if(fcp.getPropertyName().equalsIgnoreCase(tempProp.getPropertyName())){
									match=true;
									break;
								}
							}

							if(!match){
								collaborationErrorLog = new CollaborationErrorLog();
								collaborationErrorLog.setEntityName(fileUp.getFileName());
								collaborationErrorLog.setErrorType("Validation Error");
								collaborationErrorLog.setErrorMessage("File Property - "+fcp.getPropertyName() +" is not present in properties template.");
								collaborationErrorLog.setSheetName(File);
								fileErrorLogs.add(collaborationErrorLog);
								error=true;
							}
						}
						for(TemplateProperties tempProp:templateProperties.get(tempDtl.get(0).getTemplateId())){
							if(tempProp.getIsMandatory().equals(1)){
								property = propertiesForAFile.stream().filter(prop->prop.getPropertyName().equalsIgnoreCase(tempProp.getPropertyName())).collect(Collectors.toList());
								if(property.isEmpty()){
									collaborationErrorLog = new CollaborationErrorLog();
									collaborationErrorLog.setEntityName(fileUp.getFileName());
									collaborationErrorLog.setErrorType("Validation Error");
									collaborationErrorLog.setErrorMessage("File Property - "+tempProp.getPropertyName() +" is mandatory but not present.");
									collaborationErrorLog.setSheetName(File);
									fileErrorLogs.add(collaborationErrorLog);
								}
							}
						}

					} else {
						for(TemplateProperties tempProp:templateProperties.get(tempDtl.get(0).getTemplateId())){
							if(tempProp.getIsMandatory().equals(1)){
								collaborationErrorLog = new CollaborationErrorLog();
								collaborationErrorLog.setEntityName(fileUp.getFileName());
								collaborationErrorLog.setErrorType("Validation Error");
								collaborationErrorLog.setErrorMessage("File Property - "+tempProp.getPropertyName() +" is mandatory but not present");
								collaborationErrorLog.setSheetName(File);
								fileErrorLogs.add(collaborationErrorLog);
								error=true;
							}
						}
					}
				}

				/*
				 * validating share details
				 */

				Set<String> userPermCheck = new HashSet<String>();
				Set<String> rolePermCheck = new HashSet<String>();
				if(!sharedDetailsListForAFile.isEmpty()){
					for(ShareDetailsForUpload sd:sharedDetailsListForAFile){

						if(null!=sd.getSharedOrganisationName()){
							if(null!=sd.getRoleOrUserId()){
								if(priorityMap.containsKey(sd.getPrivilege().toUpperCase())){
									if(sd.getShareWith().equalsIgnoreCase("ROLE")){
										//role
										if(!rolePermCheck.contains(sd.getDirectoryPath()+SEPARATOR+sd.getContentName()+SEPARATOR+sd.getRoleOrUserId()+SEPARATOR+sd.getSharedOrganisationName())){

										}else{
											err = new CollaborationErrorLog();
											err.setEntityName(sd.getContentName());
											err.setErrorType("Validation Error");
											err.setSheetName(Content_Share);
											err.setErrorMessage("Duplicate permissions found for a role on the current entity.");
											fileErrorLogs.add(err);
											error=true;
										}

									}else{
										//user
										if(!userPermCheck.contains(sd.getDirectoryPath()+SEPARATOR+sd.getContentName()+SEPARATOR+sd.getRoleOrUserId()+SEPARATOR+sd.getSharedOrganisationName())){

										}else{
											err = new CollaborationErrorLog();
											err.setEntityName(sd.getContentName());
											err.setErrorType("Validation Error");
											err.setSheetName(Content_Share);
											err.setErrorMessage("Duplicate permissions found for a user on the current entity.");
											fileErrorLogs.add(err);
											error=true;
										}
									}
								}else{
									err = new CollaborationErrorLog();
									err.setEntityName(sd.getContentName());
									err.setErrorType("Validation Error");
									err.setSheetName(Content_Share);
									err.setErrorMessage("Invalid permission name.");
									fileErrorLogs.add(err);
									error=true;
								}

							}else{
								err = new CollaborationErrorLog();
								err.setEntityName(sd.getContentName());
								err.setErrorType("Validation Error");
								err.setSheetName(Content_Share);
								err.setErrorMessage("Invalid role/ user name.");
								fileErrorLogs.add(err);
								error=true;
							}
						}else{
							err = new CollaborationErrorLog();
							err.setEntityName(sd.getContentName());
							err.setErrorType("Validation Error");
							err.setSheetName(Content_Share);
							err.setErrorMessage("Invalid organisation name.");
							fileErrorLogs.add(err);
							error=true;
						}
					}
				}



			} else {
				collaborationErrorLog = new CollaborationErrorLog();
				collaborationErrorLog.setEntityName(fileUp.getFileName());
				collaborationErrorLog.setErrorType("Validation Error");
				collaborationErrorLog.setErrorMessage("Target Directory - "+fileUp.getPackageLocation()+" does not exist");
				collaborationErrorLog.setSheetName(File);
				fileErrorLogs.add(collaborationErrorLog);
				error=true;
			}

			if(!error){
				document = new File();
				document.setFileId(UUID.randomUUID().toString());
				document.setFileName(fileUp.getFileName());
				document.setFileDesc(fileUp.getFileDesc());
				document.setCreatedTime(System.currentTimeMillis());
				document.setCreatorId(userId);
				document.setDirectoryId(directory.getNodeId());
				document.setOrgId(orgId);
				document.setPackageLocation(directory.getParentPathUUID()+SEPARATOR+directory.getNodeId());
				document.setSolutionId(solutionId);
				document.setVersionNumber("0");
				document.setActive(1);
				document.setActualFilePath(fileUp.getActualPath());
				files.add(document);

				tempDtl = dtl.stream().filter(col->col.getDirectoryId().equalsIgnoreCase(directory.getNodeId()) && col.getIsSecurityTemplate().equals(0))
						.collect(Collectors.toList());
				if(tempDtl.isEmpty()){
					tempDtl = documentManagerHibernateFactory.getDocumentManagerDao().templateForDirectory(directory.getNodeId());
				}

				for(FileContentProperties fcp:propertiesForAFile){
					if(!tempDtl.isEmpty()){
						for(TemplateProperties tempProp:templateProperties.get(tempDtl.get(0).getTemplateId())){
							if(fcp.getPropertyName().equalsIgnoreCase(tempProp.getPropertyName())){

								cp = new ContentProperties();
								cp.setContentId(document.getFileId());
								cp.setContentType("FILE");
								cp.setIsMandatory(tempProp.getIsMandatory());
								cp.setIsSecurityTemplate(0);
								cp.setPropertyDataType(tempProp.getPropertyType());
								cp.setPropertyId(tempProp.getPropertyId());
								cp.setPropertyValue(fcp.getPropertyValue());
								cp.setTemplateId(tempProp.getTemplateId());
								cp.setVersionNumber(0);
								cp.setVisibility(tempProp.getToShow());
								contentProperties.add(cp);
							}
						}
					}
				}

				for(ShareDetailsForUpload sd:sharedDetailsListForAFile){
					if(sd.getShareWith().equalsIgnoreCase("ROLE")){
						csr=new ContentSecurityAccessRole();
						csr.setContentId(document.getFileId());
						csr.setContentSecurityId(UUID.randomUUID().toString());
						csr.setContentTypeId("FILE");
						csr.setOrgId(sd.getSharedOrganisationName());
						csr.setSecurityTemplateName(sd.getPrivilege());
						csr.setSolutionId(solutionId);
						csr.setRoleId(sd.getRoleOrUserId());
						csAccessList.add(csr);
					} else {
						cs=new ContentSecurity();
						cs.setContentId(document.getFileId());
						cs.setContentSecurityId(UUID.randomUUID().toString());
						cs.setContentTypeId("FILE");
						cs.setOrgId(sd.getSharedOrganisationName());
						cs.setSecurityTemplateName(sd.getPrivilege());
						cs.setSolutionId(solutionId);
						cs.setUserId(sd.getRoleOrUserId());
						csList.add(cs);
					}
				}

			}
		}

		/*
		 * 0. error log
		 * 1. list of files
		 * 2. content properties
		 * 3. content security
		 * 4. content security access role
		 */


		finalArr[0] = fileErrorLogs;
		finalArr[1] = files;
		finalArr[2] = contentProperties;
		finalArr[3] = csList;
		finalArr[4] = csAccessList;
		return finalArr;
	}


	private List<DirectoryForUpload> processDirectory(Worksheet worksheetForDirectory,Map<String,Integer> solutionNameIdMap
			,Integer userId,Map<String,Integer> templateIdMap,
			Integer solutionId, String solutionName,Integer orgId) throws Throwable {
		List<DirectoryForUpload> directoryList = new ArrayList<DirectoryForUpload>();
		DirectoryForUpload directoryUpload = null;
		Cells cells = worksheetForDirectory.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		String packagePath = null;
		String cellVal = null;
		int colCount = worksheetForDirectory.getCells().getMaxColumn();
		int rowCount = worksheetForDirectory.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (Integer i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}
		//Headers in directory sheet
		/* Solution-0,
		 * Organization Code - 1,
		 * Parent Directory Path - 2,
		 * Directory Name - 3,
		 * Directory Description - 4,
		 * Content Template - 5*/
		for (int i = 1; i <= rowCount; i++) {
			directoryUpload=new DirectoryForUpload();
			for (int j = 0; j <= colCount; j++) {
				cellVal = cells.get(i, j).getValue()==null?"":cells.get(i, j).getValue().toString().trim();
				if(colOrder.get(directoryHeadersAndIndex.get(0)).equals(new Integer(j))){
					directoryUpload.setSolutionId(solutionId);
				}else if(colOrder.get(directoryHeadersAndIndex.get(1)).equals(new Integer(j))){
					directoryUpload.setOrgId(orgId);
				}else if(colOrder.get(directoryHeadersAndIndex.get(2)).equals(new Integer(j))){
					//parent path and figure out hierarchy
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						packagePath = cellVal;
						directoryUpload.setPackageLocation(packagePath);
						directoryUpload.setHierarchyLevel( Integer.parseInt((packagePath.chars().filter(num -> num == '/').count()+1)+""));
					}else{
						//org Folders.
						directoryUpload.setPackageLocation(null);
						directoryUpload.setHierarchyLevel(0);
					}
				}else if(colOrder.get(directoryHeadersAndIndex.get(3)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						directoryUpload.setDirectoryName(cellVal);
					}
				}else if(colOrder.get(directoryHeadersAndIndex.get(4)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						directoryUpload.setDirectoryDesc(cellVal);
					}
				}else if(colOrder.get(directoryHeadersAndIndex.get(5)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						directoryUpload.setTemplateId(templateIdMap.get(cellVal));
					}else{
						directoryUpload.setTemplateId(-999);
					}
				}
			}
			directoryUpload.setCreator(userId);
			directoryUpload.setIsPrivate(0);
			directoryUpload.setCreatedTime(System.currentTimeMillis());
			directoryList.add(directoryUpload);
		}
		return directoryList;
	}

	@SuppressWarnings("unchecked")
	private Object[] processDirectorySecurity(
			Worksheet worksheetForDirectorySecurity,
			Map<String, Integer> solutionNameIdMap, Integer userId,
			Map<String, Integer> templateIdMap, CollaborationNode rootNode,
			List<DocumentTemplate> templates,Map<Integer,List<TemplateProperties>> templateIdAndProperties,
			Integer solutionId, String solutionName,Integer orgId, List<String> newDirectories) throws Throwable {
		Object[] res = new  Object[3];
		List<DirectoryTemplateLink> directoryTemplateLinkList = new ArrayList<DirectoryTemplateLink>();
		List<ContentProperties> contentPropertiesLinkList = new ArrayList<ContentProperties>();
		List<CollaborationErrorLog> errLogList = new ArrayList<CollaborationErrorLog>();
		Map<String, Set<String>> entityAndSecurityListValuesMap = new HashMap<String,Set<String>>();
		DirectoryTemplateLink dtl = null;
		ContentProperties cp = null;
		CollaborationErrorLog cel = null;
		String inputPattern =null;
		SimpleDateFormat inputDateFormat = null;
		Date date = null;
		Integer from = null;
		Integer to = null;
		Map<String,JSONArray> contentPropArrLink = new HashMap<String, JSONArray>();
		DocumentTemplate securityTemplate = templates.stream().filter(t->t.getIsSecurityTemplate().equals(1)).collect(Collectors.toList()).get(0);
		TemplateProperties secTemplateProperties = templateIdAndProperties.get(securityTemplate.getTemplateId()).get(0);
		JSONArray jArr = null;
		JSONObject jObj = null;
		Cells cells = worksheetForDirectorySecurity.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		String cellVal = null;
		int colCount = worksheetForDirectorySecurity.getCells().getMaxColumn();
		int rowCount = worksheetForDirectorySecurity.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		List<SecurityDimensionMaster> sfwl=SecurityDimensionMasterCache.getInstance().getAll();
		/*List<SecurityDimensionMaster> timeSf = sfwl.stream().filter(col->col.getSolutionId().equals(solutionId)&&col.getDimensionType()
				.equalsIgnoreCase("TIME")).collect(Collectors.toList());*/
		for (Integer i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}
		//Headers in directory sheet
		/* Organization Code - 0,
		 * Parent Directory Path -1	,
		 * Directory Name-2	,
		 * Security Dimension-3	,
		 * Security Value From	-4,
		 * Security Value To-5
		 */
		for (int i = 1; i <= rowCount; i++) {
			cp=new ContentProperties();
			dtl = new DirectoryTemplateLink();
			Map<String,String> mapToholdExcelHeaderAndVal = new HashMap<String, String>();
			for (int j = 0; j <= colCount; j++) {
				cellVal = cells.get(i, j).getValue()==null?"":cells.get(i, j).getValue().toString().trim();
				if(colOrder.get(directorySecurityHeadersAndIndex.get(2)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("parentPath", cellVal);
				}else if(colOrder.get(directorySecurityHeadersAndIndex.get(3)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("currentDirectory", cellVal);
				}else if(colOrder.get(directorySecurityHeadersAndIndex.get(4)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("securityDim", cellVal);
				}else if(colOrder.get(directorySecurityHeadersAndIndex.get(5)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("from", cellVal);
				}else if(colOrder.get(directorySecurityHeadersAndIndex.get(6)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("to", cellVal);
				}else if(colOrder.get(directorySecurityHeadersAndIndex.get(7)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("operator", cellVal);
				}else if(colOrder.get(directorySecurityHeadersAndIndex.get(8)).equals(new Integer(j))){
					mapToholdExcelHeaderAndVal.put("valueList", cellVal);
				}
			}
			//validations
			Integer year;
			String currentDirectoryUUID = validatePathAndFolderExistence(mapToholdExcelHeaderAndVal.get("parentPath"),mapToholdExcelHeaderAndVal.get("currentDirectory"),rootNode);

			if(null!=currentDirectoryUUID){
				if(newDirectories.contains(currentDirectoryUUID)){
					List<SecurityDimensionMaster> tempSecDim = sfwl.stream().filter(td->td.getDimensionBusinessName().equals(mapToholdExcelHeaderAndVal.get("securityDim")))
							.collect(Collectors.toList());
					if(!tempSecDim.isEmpty()){
						//checking if its date dimension or normal dimension and the value mentioned in excel.
						
						
						if(null!=tempSecDim.get(0).getTimeKey() && !tempSecDim.get(0).getTimeKey().equalsIgnoreCase("") && mapToholdExcelHeaderAndVal.get("operator").equalsIgnoreCase(CollaborationProperties.getProperty("app.securityDimensionOperatorInRange"))) {
							//date security dimension.
							inputPattern = tempSecDim.get(0).getTimeKey();
							inputDateFormat = new SimpleDateFormat(inputPattern);
							date = null;
							try {
								if(mapToholdExcelHeaderAndVal.get("from").equals("") || mapToholdExcelHeaderAndVal.get("to").equals("")){
									cel = new CollaborationErrorLog();
									cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
									cel.setErrorType("Validation Error");
									cel.setSheetName(Directory_Security);
									cel.setErrorMessage("Invalid date range. Date ranges cannot be empty");
									errLogList.add(cel);
								}else{
									date = inputDateFormat.parse(mapToholdExcelHeaderAndVal.get("from"));
									year = Integer.parseInt(periodIdFormatFormatter.format(date));
									from = year;
									date = inputDateFormat.parse(mapToholdExcelHeaderAndVal.get("to"));
									year = Integer.parseInt(periodIdFormatFormatter.format(date));
									to = year;
									if(from>to){
										cel = new CollaborationErrorLog();
										cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
										cel.setErrorType("Validation Error");
										cel.setSheetName(Directory_Security);
										cel.setErrorMessage("Invalid date range. From date cannot be greater than to date.");
										errLogList.add(cel);
									}else{

										//directory template link
										List<DirectoryTemplateLink> existingCheck =  directoryTemplateLinkList.stream().
												filter(d ->d.getDirectoryId().equals(currentDirectoryUUID) &&
														d.getTemplateId().equals(securityTemplate.getTemplateId())&& d.getIsSecurityTemplate().equals(1)).collect(Collectors.toList());
										if(existingCheck!=null && existingCheck.size()==(0)) {

											dtl.setDirectoryId(currentDirectoryUUID);
											dtl.setIsSecurityTemplate(1);
											dtl.setTemplateId(securityTemplate.getTemplateId());
											directoryTemplateLinkList.add(dtl);
										}

										if(contentPropArrLink.containsKey(currentDirectoryUUID)){
											jObj=new JSONObject();
											jObj.put("fromDate", mapToholdExcelHeaderAndVal.get("from"));
											jObj.put("toDate", mapToholdExcelHeaderAndVal.get("to"));
											jObj.put("dimensionName", tempSecDim.get(0).getDimensionBusinessName());
											jObj.put("dimensionId", tempSecDim.get(0).getSecurityDimensionId());
											jObj.put("dateformat", tempSecDim.get(0).getTimeKey());
											jObj.put("valueList", null);
											contentPropArrLink.get(currentDirectoryUUID).add(jObj);
										}else{
											jObj=new JSONObject();
											jObj.put("fromDate", mapToholdExcelHeaderAndVal.get("from"));
											jObj.put("toDate", mapToholdExcelHeaderAndVal.get("to"));
											jObj.put("dimensionName", tempSecDim.get(0).getDimensionBusinessName());
											jObj.put("dimensionId", tempSecDim.get(0).getSecurityDimensionId());
											jObj.put("dateformat", tempSecDim.get(0).getTimeKey());
											jObj.put("valueList", null);
											jArr = new JSONArray();
											jArr.add(jObj);
											contentPropArrLink.put(currentDirectoryUUID,jArr);
										}

									}
								}

							} catch (ParseException e) {
								cel = new CollaborationErrorLog();
								cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
								cel.setErrorType("Validation Error");
								cel.setSheetName(Directory_Security);
								cel.setErrorMessage("Invalid date format.");
								errLogList.add(cel);
								e.printStackTrace();
							}

						
						}else if((null==tempSecDim.get(0).getTimeKey() || !tempSecDim.get(0).getTimeKey().equalsIgnoreCase("") )&& mapToholdExcelHeaderAndVal.get("operator").equalsIgnoreCase(CollaborationProperties.getProperty("app.securityDimensionOperatorIn"))) {
							//normal dimension.
							if(mapToholdExcelHeaderAndVal.get("valueList").trim().equals("")) {
								cel = new CollaborationErrorLog();
								cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
								cel.setErrorType("Validation Error");
								cel.setSheetName(Directory_Security);
								cel.setErrorMessage("Security dimension Value list cannot be empty.");
								errLogList.add(cel);
							}else {
								//validating bkeys
								Boolean validBkeys = Boolean.TRUE;
								SecurityDimensionMaster sdm =tempSecDim.get(0);
								List<DIMbkeys> bkeyList = documentManagerHibernateFactory.getDocumentManagerDao().getBkeysFromDimTable(sdm.getDimensionTableName(),
										sdm.getBkeyColumn(), sdm.getIdColumn(), sdm.getDescriptionColumn(), sdm.getDataSourceColumn(), (sdm.getIsDataSourceAvailable().equals(0)?"N":"Y"), solutionId, solutionName, sdm.getDimensionName());
								List<String> availableBkeys = bkeyList.stream().map(bk->bk.getDimensionBkeyCol()).collect(Collectors.toList());
								
								for(String bk :mapToholdExcelHeaderAndVal.get("valueList").split(",") ) {
									if(!availableBkeys.contains(bk.trim())) {
										cel = new CollaborationErrorLog();
										cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
										cel.setErrorType("Validation Error");
										cel.setSheetName(Directory_Security);
										cel.setErrorMessage("Invalid Business key found for the security dimension. - "+bk);
										errLogList.add(cel);
										validBkeys = Boolean.FALSE;
										break;
											
									}
								}
								if(validBkeys) {
									//directory template link
									List<DirectoryTemplateLink> existingCheck =  directoryTemplateLinkList.stream().
											filter(d ->d.getDirectoryId().equals(currentDirectoryUUID) &&
													d.getTemplateId().equals(securityTemplate.getTemplateId())&& d.getIsSecurityTemplate().equals(1)).collect(Collectors.toList());
									if(existingCheck!=null && existingCheck.size()==(0)) {

										dtl.setDirectoryId(currentDirectoryUUID);
										dtl.setIsSecurityTemplate(1);
										dtl.setTemplateId(securityTemplate.getTemplateId());
										directoryTemplateLinkList.add(dtl);
									}
									String securityKeyForProp = currentDirectoryUUID+SEPARATOR+tempSecDim.get(0).getSecurityDimensionId()+SEPARATOR+tempSecDim.get(0).getDimensionBusinessName();
									if(entityAndSecurityListValuesMap.containsKey(securityKeyForProp)) {
										entityAndSecurityListValuesMap.get(securityKeyForProp).addAll(Arrays.asList(mapToholdExcelHeaderAndVal.get("valueList").split(",")));
									}else{
										Set<String> tempSet = new HashSet<String>();
										tempSet.addAll( Arrays.asList(mapToholdExcelHeaderAndVal.get("valueList").split(",")));
										entityAndSecurityListValuesMap.put(securityKeyForProp,tempSet);
									}
								}
							}
							
						}else {

							cel = new CollaborationErrorLog();
							cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
							cel.setErrorType("Validation Error");
							cel.setSheetName(Directory_Security);
							cel.setErrorMessage("Invalid operator for the security dimension.");
							errLogList.add(cel);
						
						}
					}else{
						cel = new CollaborationErrorLog();
						cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
						cel.setErrorType("Validation Error");
						cel.setSheetName(Directory_Security);
						cel.setErrorMessage("Invalid security dimension name.");
						errLogList.add(cel);
					}
				}
			}else{
				cel = new CollaborationErrorLog();
				cel.setEntityName(mapToholdExcelHeaderAndVal.get("currentDirectory"));
				cel.setErrorType("Validation Error");
				cel.setSheetName(Directory_Security);
				cel.setErrorMessage("Invalid path.");
				errLogList.add(cel);
			}
		}
		for (Map.Entry<String, Set<String>> entry : entityAndSecurityListValuesMap.entrySet())
		{
			String cId = entry.getKey().split(SEPARATOR)[0];
			String dId = entry.getKey().split(SEPARATOR)[1];
			String dName = entry.getKey().split(SEPARATOR)[2];
			JSONArray valList = new JSONArray();
			valList.addAll(entry.getValue());
			if(contentPropArrLink.containsKey(cId)){
				jObj=new JSONObject();
				jObj.put("fromDate", null);
				jObj.put("toDate", null);
				jObj.put("dimensionName", dName);
				jObj.put("dimensionId", Integer.parseInt(dId));
				jObj.put("dateformat", null);
				jObj.put("valueList", valList);
				contentPropArrLink.get(cId).add(jObj);
			}else{
				jObj=new JSONObject();
				jObj.put("fromDate", null);
				jObj.put("toDate", null);
				jObj.put("dimensionName", dName);
				jObj.put("dimensionId", Integer.parseInt(dId));
				jObj.put("dateformat", null);
				jObj.put("valueList", valList);
				jArr = new JSONArray();
				jArr.add(jObj);
				contentPropArrLink.put(cId,jArr);
			}
		}
		
		
		for (Map.Entry<String, JSONArray> entry : contentPropArrLink.entrySet())
		{
			cp = new ContentProperties();
			cp.setContentId(entry.getKey());
			cp.setContentType("DIRECTORY");
			cp.setIsMandatory(secTemplateProperties.getIsMandatory());
			cp.setIsSecurityTemplate(1);
			cp.setPropertyDataType(secTemplateProperties.getPropertyType());
			cp.setPropertyId(secTemplateProperties.getPropertyId());
			cp.setPropertyValue(entry.getValue().toJSONString());
			cp.setTemplateId(securityTemplate.getTemplateId());
			cp.setVersionNumber(1);
			cp.setVisibility(secTemplateProperties.getToShow());
			contentPropertiesLinkList.add(cp);
		}
		res[0] = contentPropertiesLinkList;
		res[1] = errLogList;
		res[2] = directoryTemplateLinkList;
		return res;

	}

	private List<ShareDetailsForUpload> extractContentSecurityForDirectories(
			Worksheet worksheetForContentShare, Integer orgId,
			Integer solutionId, String solutionName, Integer userId,
			String userName, Map<String, Integer> solutionNameIdMap,
			CollaborationNode rootNode) throws Throwable {
		List<ShareDetailsForUpload> shareList = new ArrayList<ShareDetailsForUpload>();
		ShareDetailsForUpload share = null;
		Cells cells = worksheetForContentShare.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		String packagePath = null;
		int colCount = worksheetForContentShare.getCells().getMaxColumn();
		int rowCount = worksheetForContentShare.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (Integer i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}

		//Headers in directory sheet
		/* Solution	-0,
		 * Organization Code	-1,
		 * Directory Path	-2,
		 * Content Type	-3,
		 * Content Name	-4,
		 * Share With	-5,
		 * Share with Organization Name	-6,
		 * Share with Role/User Name	-7,
		 * Permission-8*/
		OrganisationUnit shareOrgUnit;
		for (int i = 1; i <= rowCount; i++) {
			share=new ShareDetailsForUpload();
			for (int j = 0; j <= colCount; j++) {

				String cellVal = cells.get(i, j).getValue()==null?"":cells.get(i, j).getValue().toString().trim();
				if(colOrder.get(contentShareHeadersAndIndex.get(0)).equals(new Integer(j))){
					share.setSolutionName(cellVal);
				}else if(colOrder.get(contentShareHeadersAndIndex.get(1)).equals(new Integer(j))){
					share.setOrganisationName(cellVal);
				}else if(colOrder.get(contentShareHeadersAndIndex.get(2)).equals(new Integer(j))){
					share.setDirectoryPath(cellVal);
				}else if(colOrder.get(contentShareHeadersAndIndex.get(3)).equals(new Integer(j))){
					share.setContentType(cellVal);
				}else if(colOrder.get(contentShareHeadersAndIndex.get(4)).equals(new Integer(j))){
					share.setContentName(cellVal);
				}else if(colOrder.get(contentShareHeadersAndIndex.get(5)).equals(new Integer(j))){
					share.setShareWith(cellVal);
				}else if(colOrder.get(contentShareHeadersAndIndex.get(6)).equals(new Integer(j))){
					shareOrgUnit=adminCacheUtil.getOrganisationByName(cellVal);
					share.setSharedOrganisationName(shareOrgUnit==null?null:shareOrgUnit.getOrgId());
				}else if(colOrder.get(contentShareHeadersAndIndex.get(7)).equals(new Integer(j))){
					if(null==share.getSharedOrganisationName()){
						share.setRoleOrUserId(null);
					}else{
						if(share.getShareWith().equalsIgnoreCase("ROLE")){
							List<AccessRole> ar = getListAccessRolesForOrg(share.getSharedOrganisationName());
							ar = ar.stream().filter(a->a.getAccessRoleName().equals(cellVal)).collect(Collectors.toList());
							if(ar.isEmpty()){
								share.setRoleOrUserId(null);
							}else{
								share.setRoleOrUserId(ar.get(0).getAccessRoleId());
							}
						}else{
							Users u = adminCacheUtil.getUserByName(cellVal);
							share.setRoleOrUserId(u==null?null:u.getUserId());
						}
					}
				}else if(colOrder.get(contentShareHeadersAndIndex.get(8)).equals(new Integer(j))){
					share.setPrivilege(cellVal);
				}
			}
			shareList.add(share);
		}
		return shareList;
	}

	private CollaborationNode validateFolderForFile(String parentPath,
			CollaborationNode rootNode) {
		if(parentPath!=null && !"".equalsIgnoreCase(parentPath)){
			parentPath = (String) CollaborationProperties.get("app.organizationName")+"/"+(String) CollaborationProperties.get("app.rootFolderName")+"/"+parentPath;
		} else {
			parentPath = (String) CollaborationProperties.get("app.organizationName")+"/"+(String) CollaborationProperties.get("app.rootFolderName");
		}

		List<String> pathBreak = Arrays.asList(parentPath.split("/"));
		CollaborationNode currentNode = rootNode;
		List<CollaborationNode> temp = null;
		for(String p : pathBreak){
			if(null!=currentNode){
				temp = currentNode.getChildrenList().stream().filter(cn->cn.getNodeBusinessName().equals(p)).collect(Collectors.toList());
				currentNode=temp.isEmpty()?null:temp.get(0);
			}else{
				return null;
			}
		}
		return currentNode;
	}

	private List<FileForUpload> getFileUploadListFromWorkSheet(Worksheet file,Map<String,Integer> solutionNameIdMap
			,Integer userId,Integer solutionId, String solutionName,Integer orgId){

		List<FileForUpload> fileUploadList = new ArrayList<>();
		FileForUpload fileUpload;
		Cells cells = file.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		String packagePath = null;
		String cellVal = null;
		int colCount = file.getCells().getMaxColumn();
		int rowCount = file.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (Integer i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}
		//Headers in directory sheet
		/* Solution-0,
		 * Organization Code - 1,
		 * Directory Path - 2,
		 * File Name - 3,
		 * Actual File Path - 4
		 * */

		for (int i = 1; i <= rowCount; i++) {
			fileUpload=new FileForUpload();
			for (int j = 0; j <= colCount; j++) {
				cellVal = cells.get(i, j).getValue()==null?"":cells.get(i, j).getValue().toString().trim();
				if(colOrder.get(fileHeadersAndIndex.get(0)).equals(new Integer(j))){
					fileUpload.setSolutionId(solutionNameIdMap.get(solutionId));
				}else if(colOrder.get(fileHeadersAndIndex.get(1)).equals(new Integer(j))){
					fileUpload.setOrgId(orgId);
				}else if(colOrder.get(fileHeadersAndIndex.get(2)).equals(new Integer(j))){
					//parent path and figure out hierarchy
					packagePath = cellVal;
					fileUpload.setPackageLocation(packagePath);
				}else if(colOrder.get(fileHeadersAndIndex.get(3)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						fileUpload.setFileName(cellVal);
						fileUpload.setFileDesc(cellVal);
					}
				}else if(colOrder.get(fileHeadersAndIndex.get(4)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						fileUpload.setActualPath(cellVal);
					}
				}
			}
			fileUpload.setCreatorId(userId);
			fileUpload.setCreatedTime(System.currentTimeMillis());
			fileUploadList.add(fileUpload);
		}

		return fileUploadList;

	}

	private List<FileContentProperties> getFileContentPropListFromWorkSheet(Worksheet file,Map<String,Integer> solutionNameIdMap
			,Integer userId,Integer solutionId, String solutionName,Integer orgId){

		List<FileContentProperties> fileUploadList = new ArrayList<>();
		FileContentProperties fileUpload;
		Cells cells = file.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		String packagePath = null;
		String cellVal = null;
		int colCount = file.getCells().getMaxColumn();
		int rowCount = file.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (Integer i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}
		//Headers in directory sheet
		/* Solution-0,
		 * Organization Code - 1,
		 * Directory Path - 2,
		 * File Name - 3,
		 * Property Name - 4
		 * Property Value - 5
		 * */

		for (int i = 1; i <= rowCount; i++) {
			fileUpload=new FileContentProperties();
			for (int j = 0; j <= colCount; j++) {
				cellVal = cells.get(i, j).getValue()==null?"":cells.get(i, j).getValue().toString().trim();
				if(colOrder.get(filePropertiesHeadersFromProperty.get(0)).equals(new Integer(j))){
					fileUpload.setSolutionId(solutionNameIdMap.get(solutionId));
				}else if(colOrder.get(filePropertiesHeadersFromProperty.get(1)).equals(new Integer(j))){
					fileUpload.setOrgId(orgId);
				}else if(colOrder.get(filePropertiesHeadersFromProperty.get(2)).equals(new Integer(j))){
					//parent path and figure out hierarchy
					packagePath = cellVal;
					fileUpload.setPackageLocation(packagePath);
				}else if(colOrder.get(filePropertiesHeadersFromProperty.get(3)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						fileUpload.setFileName(cellVal);
					}
				}else if(colOrder.get(filePropertiesHeadersFromProperty.get(4)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						fileUpload.setPropertyName(cellVal);
					}
				}
				else if(colOrder.get(filePropertiesHeadersFromProperty.get(5)).equals(new Integer(j))){
					if(cellVal!=null && !"".equalsIgnoreCase(cellVal)){
						fileUpload.setPropertyValue(cellVal);
					}
				}
			}
			fileUploadList.add(fileUpload);
		}
		return fileUploadList;

	}

	private Map<Integer,List<TemplateProperties>> getAllTemplatePropertiesMap() {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getAllTemplateDetails()");

		Map<Integer,List<TemplateProperties>> templateMap= new HashMap<>();

		List<DocumentTemplate> templates = documentManagerHibernateFactory.getDocumentManagerDao().getAllTemplate();
		List<TemplateProperties> templateProperties = new ArrayList<>();
		for(DocumentTemplate dt:templates){
			if(dt.getIsSecurityTemplate().equals(0)){
				templateProperties = documentManagerHibernateFactory.getDocumentManagerDao().getPropertiesForTemplate(dt.getTemplateId());

				templateMap.put(dt.getTemplateId(),templateProperties);

			}
		}
		return templateMap;
	}

	
}
