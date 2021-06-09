package com.fintellix.framework.collaboration.bo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.jcr.RepositoryException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modeshape.jcr.NoSuchRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.administrator.model.AccessRole;
import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.SecurityDimensionMaster;
import com.fintellix.administrator.model.SecurityFilterWrapper;
import com.fintellix.administrator.model.Users;
import com.fintellix.administrator.redis.AdminCacheHelper;
import com.fintellix.administrator.redis.impl.SecurityDimensionMasterCache;
import com.fintellix.framework.collaboration.dao.DaoFactory;
import com.fintellix.framework.collaboration.dto.ContentProperties;
import com.fintellix.framework.collaboration.dto.ContentSecurity;
import com.fintellix.framework.collaboration.dto.ContentSecurityAccessRole;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.Directory;
import com.fintellix.framework.collaboration.dto.DirectoryTemplateLink;
import com.fintellix.framework.collaboration.dto.DocumentTemplate;
import com.fintellix.framework.collaboration.dto.DocumentWrapper;
import com.fintellix.framework.collaboration.dto.DocumentWrapperForSearch;
import com.fintellix.framework.collaboration.dto.File;
import com.fintellix.framework.collaboration.dto.TemplateProperties;
import com.fintellix.framework.collaboration.notification.CollaborationNotification;
import com.fintellix.framework.collaboration.store.DocumentStore;
import com.fintellix.framework.collaboration.utils.CollaborationUtils;
import com.fintellix.platformcore.administration.bo.AdministrationBo;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.common.exception.VyasaException;
import com.fintellix.platformcore.usermanagement.solution.dto.VyasaSolution;
import com.fintellix.platformcore.utils.CollaborationProperties;
import com.fintellix.platformcore.web.utils.AppContextUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DocumentManagerBoImpl implements DocumentManagerBo {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private DaoFactory documentManagerHibernateFactory;
	private DocumentStore documentStore; 
	private AdministrationBo administrationBo;
	private CollaborationNotification collaborationNotification;


	private static AdminCacheHelper adminCacheUtil = AdminCacheHelper.getInstance();
	private static final String SEPARATOR = "###";
	private static final String SHARED_ROOT_UID=CollaborationProperties.getValue("app.RootPathForSharedContentsUid");
	private static final String CONSUMER=CollaborationProperties.getValue("app.consumerPrivilegeName");
	private static final String TYPE_FILE = CollaborationProperties.getValue("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getValue("app.typeDirectoryName");
	public static Map<String,Integer> priorityMap = new HashMap<String,Integer>();
	static{
		// to find the top prior override the duplicate objects

		priorityMap.put("DENIED", 7);
		priorityMap.put("CREATOR", 6);
		priorityMap.put("OWNER", 5);
		priorityMap.put("CONTRIBUTOR", 4);
		priorityMap.put("CONSUMER", 3);
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

	public AdministrationBo getAdministrationBo() {
		return administrationBo;
	}

	public void setAdministrationBo(AdministrationBo administrationBo) {
		this.administrationBo = administrationBo;
	}

	public CollaborationNotification getCollaborationNotification() {
		return collaborationNotification;
	}

	public void setCollaborationNotification(CollaborationNotification collaborationNotification) {
		this.collaborationNotification = collaborationNotification;
	}

	@Override
	public void uploadFileForUser( java.io.File stream, String directoryId, String fileName, Integer userId,
			Integer orgId, Integer solutionId,String fileDesc,String solutionName,JSONArray properties) throws VyasaException, Throwable {

		logger.info("EXEFLOW - DocumentManagerBoImpl - uploadFileForUser()");
		logger.debug("File Name - "+fileName +" Directory Id - "+directoryId+" Org - " + orgId +"");
		File file;

		String fileId="";

		file=documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsIfExistByName(fileName,  solutionId, directoryId);
		Directory dir = documentManagerHibernateFactory.getDocumentManagerDao().getDirectoryDetailsById(directoryId);
		//String repositoryName =getRepositoryName();


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
			documentStore.saveFileIntoMongoDB(updateFile,stream);


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
			documentStore.saveFileIntoMongoDB(file,stream);
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

		//String repositoryName =getRepositoryName();

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
			directory.setCreator(AppContextUtil.getCurrentUser().getUserId());
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
			//documentStore.createFolderByPath(repositoryName, solutionName, modeShapePath, directoryId);

		}

	}

	@Override
	public void deleteFile(String fileId,String solutionName) throws VyasaException, NoSuchRepositoryException, RepositoryException {
		logger.info("EXEFLOW - DocumentManagerBoImpl - deleteFile()");
		File file = documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsById(fileId);
		String modeShapePath = file.getPackageLocation().replace("###", "/")+"/"+file.getFileId();
		documentManagerHibernateFactory.getDocumentManagerDao().deleteFile(fileId);
		//String repositoryName =getRepositoryName();
		documentStore.deleteAllVersionsOfTheFromMongoDB(file);
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
			documentStore.deleteAllVersionsOfTheFromMongoDB(file);
		}
		documentManagerHibernateFactory.getDocumentManagerDao().deleteDirectory(directoryId);

		String path = (directory.getPackageLocation()==null?"":directory.getPackageLocation().replace("###", "/"));

		if(path.length()>0){
			path=path+"/"+directory.getDirectoryId();
		} else {
			path=directory.getDirectoryId();
		}
		//documentStore.deleteContentByPath(getRepositoryName(), solutionName, path);

	}

	@Override
	public void shareDocuements(JSONObject sharedUserWithPrivilegesAndPackage) {

	}

	@Override
	public List<DocumentWrapper> getMyContentForCurrentUser(String currentDirectory, Integer userId, Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getMyContentForCurrentUser");
		List<DocumentWrapper> myContents = new ArrayList<DocumentWrapper>();
		List<DocumentWrapper> finalContents = new ArrayList<DocumentWrapper>();
		try {
			List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			myContents = documentManagerHibernateFactory.getDocumentManagerDao().getMyContents(userId, solutionId, orgId, currentDirectory,accessRoleIdList);
			//myContents = myContents.stream().filter(mc->mc.getPrivilegeName().equalsIgnoreCase("OWNER")||mc.getPrivilegeName().equalsIgnoreCase("CREATOR")).collect(Collectors.toList());
			finalContents=returnFinalListAfterSecurityFilter(myContents, userId, orgId, solutionId);
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
	public List<DocumentWrapper> getSharedContentForCurrentUser(String currentDirectory, Integer userId, Integer orgId,Integer solutionId) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getSharedContentForCurrentUser");
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		List<DocumentWrapper> finalContent= new ArrayList<>();

		try {

			List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			sharedContents = documentManagerHibernateFactory.getDocumentManagerDao().getSharedContents(userId, solutionId, orgId, currentDirectory,accessRoleIdList);

			finalContent=returnFinalListAfterSecurityFilter(sharedContents, userId, orgId, solutionId);
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
			, String currentSearchDirDisplayName, String fullDirDisplayName,String requestOrigin,String myContentRootID) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getCollaborationSearchMyContent");
		List<DocumentWrapper> firstLevelContent = new ArrayList<DocumentWrapper>();
		List<String> tempLevelContent = new ArrayList<String>();
		List<DocumentWrapper> finalLevelContent = new ArrayList<DocumentWrapper>();
		List<String> fullPathDetails = new ArrayList<String>();
		List<DocumentWrapperForSearch> finalSearchResults = new ArrayList<DocumentWrapperForSearch>(); 
		try {
			List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
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
			finalSearchResults=returnFinalListAfterSecurityFilterForSearch(finalSearchResults, userId, orgId, solutionId);
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

			List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
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
			Integer solutionId, String currentPrivilegeOnFolder,String requestOrigin) {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getContentForCurrentUserAtDirectory");
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		List<DocumentWrapper> finalContents = new ArrayList<DocumentWrapper>();
		try {

			List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
			List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
			sharedContents = documentManagerHibernateFactory.getDocumentManagerDao().getContentsInDirectory(userId, solutionId,
					orgId, currentDirectory, currentPrivilegeOnFolder,accessRoleIdList,requestOrigin);
			finalContents=returnFinalListAfterSecurityFilter(sharedContents, userId, orgId, solutionId);
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

	/*private String getRepositoryName() throws VyasaBusinessException{
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
	}*/

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
		InputStream content = documentStore.getFileForDownload(file);

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
					userList=administrationBo.getUserDetailsFromAccessRoleId(accessRoleId);
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
		InputStream content = documentStore.getFileForDownloadByVersion(file,Integer.parseInt(versionId));

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


	private boolean resolveSecurityForContent(ContentProperties property,Integer userId,Integer orgId,Integer solutionId) throws Throwable{
		List<SecurityFilterWrapper> sfwl=administrationBo.getUserSecurityFilter(solutionId, userId, orgId);
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

	private List<DocumentWrapper> returnFinalListAfterSecurityFilter(List<DocumentWrapper> contents,Integer userId,Integer orgId,Integer solutionId) throws Throwable{
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
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId);
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
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId);
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
	public JsonArray getOrgForUserInCurrentContent(Integer solutionId,Integer userId,Integer orgId,String ORG_UNIT_DIMENSION_NAME) throws Throwable{
		JsonArray organizationsList = new JsonArray();
		JsonObject organizationJsonObject;
		List<SecurityFilterWrapper> userSecurityFilter = administrationBo.getUserSecurityFilter(solutionId,userId,  orgId);
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
		try{List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
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
		return administrationBo.getAccessRolesForOrgUnit(chosenOrgId);
	}

	private List<DocumentWrapperForSearch> returnFinalListAfterSecurityFilterForSearch(List<DocumentWrapperForSearch> contents,Integer userId,Integer orgId,Integer solutionId) throws Throwable{
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
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId);
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
						keepContent=resolveSecurityForContent(props, userId, orgId, solutionId);
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
	public Boolean resolveEntityForDownload(String fileId,Integer userId, Integer orgId,Integer solutionId) throws Throwable {
		logger.info("EXEFLOW - DocumentManagerBoImpl - resolveEntityForDownload");
		String privilege = null;
		List<ContentProperties> securityProperties;
		boolean keepContent=false;
		File fileDetails = documentManagerHibernateFactory.getDocumentManagerDao().getFileDetailsbyId(fileId, solutionId);
		privilege = getPrivilegeMapForDownload(fileDetails.getPackageLocation(), fileDetails.getDirectoryId(), userId, orgId, solutionId);
		if(null!=privilege) {
			securityProperties = documentManagerHibernateFactory.getDocumentManagerDao().
					getContentPropertyByContentIdAndVersion(fileDetails.getDirectoryId(), 1, TYPE_DIRECTORY);
			if(securityProperties.size()>0){

				for(ContentProperties props:securityProperties){
					keepContent=resolveSecurityForContent(props, userId, orgId, solutionId);
					if(keepContent){
						keepContent=true;
					}
				}

			} else {
				keepContent = true;
			}

		}
		return keepContent;
	}


	private String getPrivilegeMapForDownload(String finalUidPath,String currentChoosenDir,Integer userId, Integer orgId,Integer solutionId) throws Throwable {
		logger.info("EXEFLOW - DocumentManagerBoImpl - getPrivilegeMapForDownload");
		Map<String,String> mapOfPrivileges = new HashMap<String, String>();
		List<AccessRole> accessRole = administrationBo.getAccessRolesForCurrentUser(userId, orgId, solutionId);
		List<Integer> accessRoleIdList = accessRole.stream().map(ar->ar.getAccessRoleId()).collect(Collectors.toList());
		mapOfPrivileges=documentManagerHibernateFactory.getDocumentManagerDao().getPrivilegeByParentMyContent(finalUidPath, userId,  accessRoleIdList, orgId,solutionId);

		String [] pathUidArr = finalUidPath.split(SEPARATOR);
		//updating map based on parent if it doesn't have any privilege in table.
		for(int i=1;i<pathUidArr.length;i++){
			String curUid = pathUidArr[i];
			if(null==mapOfPrivileges.get(curUid)){
				mapOfPrivileges.put(curUid, mapOfPrivileges.get(pathUidArr[i-1]));
			}
		}
		return mapOfPrivileges.get(currentChoosenDir)==null?null:mapOfPrivileges.get(currentChoosenDir);
	}
}
