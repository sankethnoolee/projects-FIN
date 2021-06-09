package com.fintellix.framework.collaboration.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.fintellix.administrator.AdministratorPropertyReader;
import com.fintellix.administrator.model.AccessRole;
import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
import com.fintellix.administrator.redis.impl.UserWrapperCache;
import com.fintellix.administrator.redis.impl.UsersCache;
import com.fintellix.framework.collaboration.bo.DocumentManagerBo;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.DocumentWrapper;
import com.fintellix.framework.collaboration.dto.DocumentWrapperForSearch;
import com.fintellix.platformcore.common.annotation.LoadMenuItemPage;
import com.fintellix.platformcore.common.exception.VyasaException;
import com.fintellix.platformcore.usermanagement.solution.dto.VyasaSolution;
import com.fintellix.platformcore.utils.CollaborationProperties;
import com.fintellix.platformcore.web.utils.AppContextUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class CollaborationController extends MultiActionController {
	private static final String SEPARATOR = "###";
	private static final Integer PRIVATE_FLAG = 1;
	private static final Integer PUBLIC_FLAG = 0;
	private static final String SHARED_ROOT_UID=CollaborationProperties.getValue("app.RootPathForSharedContentsUid");
	private static final String SHARED_ROOT_DISPLAY_NAME=CollaborationProperties.getValue("app.RootPathForSharedContentsDisplay");
	private static final String OWNER=CollaborationProperties.getValue("app.ownerPrivilegeName");
	private static final String CREATOR=CollaborationProperties.getValue("app.creatorPrivilegeName");
	private static final String CONTRIBUTOR=CollaborationProperties.getValue("app.contributorPrivilegeName");
	private static final String CONSUMER=CollaborationProperties.getValue("app.consumerPrivilegeName");
	private static final String TYPE_FILE = CollaborationProperties.getValue("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getValue("app.typeDirectoryName");
	private static final String ORG_UNIT_DIMENSION_NAME= AdministratorPropertyReader.getInstance().getProperty("app.orgDimensionName");

	//static variable to maintain my contents root and display root folder name with user name.
	private static String MY_CONTENTS_ROOT_UID = CollaborationProperties.getValue("app.RootPathForMyContentsUid");
	private static  String MY_CONTENTS_ROOT_DISPLAY_NAME =CollaborationProperties.getValue("app.RootPathForMyContentsDisplayName");

	//private variable for maintaining user files.
	private List<String> MY_CONTENTS_PRIVATE_DIRECTORY = new ArrayList<String>();
	private DocumentManagerBo documentManagerBo;

	public DocumentManagerBo getDocumentManagerBo() {
		return documentManagerBo;
	}

	public void setDocumentManagerBo(DocumentManagerBo documentManagerBo) {
		this.documentManagerBo = documentManagerBo;
	}

	@LoadMenuItemPage
	public ModelAndView loadCollaborationLandingPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> loadCollaborationLandingPage()");
		//Change logic for default dir creation of update uuid 
		Map<String,Object> model = new HashMap<String, Object>();
		VyasaSolution solution = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION));
		OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
		Users userDetails = AppContextUtil.getCurrentUser();//SecurityContextUtil.getCurrentUser();
		String fetchRootUUIDForMyContents = documentManagerBo.getRootUUIDForMyContents(currentOrgDetails.getOrgName(),currentOrgDetails.getOrgId(),solution.getSolutionID());
		MY_CONTENTS_ROOT_DISPLAY_NAME = currentOrgDetails.getOrgName();
		try {
			if(null!=fetchRootUUIDForMyContents && !"".equalsIgnoreCase(fetchRootUUIDForMyContents)){
				MY_CONTENTS_ROOT_UID =fetchRootUUIDForMyContents;
			}else{

				//TODO template id or default one.
				documentManagerBo.createFolderForUser(MY_CONTENTS_ROOT_DISPLAY_NAME, 
						null, userDetails.getUserId(), currentOrgDetails.getOrgId(),
						solution.getSolutionID(), "", solution.getSolutionName(),null,PUBLIC_FLAG,null,false,null);
				MY_CONTENTS_ROOT_UID=documentManagerBo.getRootUUIDForMyContents(currentOrgDetails.getOrgName(),currentOrgDetails.getOrgId(),solution.getSolutionID());
				//creating private folder for user.


			}

			if(documentManagerBo.checkPrivateFolderExistenceForUser(MY_CONTENTS_ROOT_UID,userDetails.getUserId(),currentOrgDetails.getOrgId(),solution.getSolutionID())){
				documentManagerBo.createFolderForUser(userDetails.getUserName(), 
						MY_CONTENTS_ROOT_UID, userDetails.getUserId(), currentOrgDetails.getOrgId(),
						solution.getSolutionID(), "", solution.getSolutionName(),null,PRIVATE_FLAG,null,false,null);

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		model.put("myContentsRootPathDisplayName", MY_CONTENTS_ROOT_DISPLAY_NAME);
		model.put("myContentsRootPathUid", MY_CONTENTS_ROOT_UID);
		model.put("sharedRootPathDisplayName", SHARED_ROOT_DISPLAY_NAME);
		model.put("sharedRootPathUid", SHARED_ROOT_UID);
		return new ModelAndView("collaborationLandingPageView","model",model);
	}

	public ModelAndView collaborationUploadFile(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationUploadFile()");
		Map<String,Object> model = new HashMap<String, Object>();
		try{
			VyasaSolution solution = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION));
			String currentPath = request.getParameter("currentPath");
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();//SecurityContextUtil.getCurrentUser();
			MultipartFile file =((MultipartHttpServletRequest) request).getFile("file");
			JSONArray propertyJsonArr = (JSONArray) new JSONParser().parse(request.getParameter("propertyArray"));
			if(file!=null){
				File convFile = new File(file.getOriginalFilename());
			    convFile.createNewFile(); 
			    FileOutputStream fos = new FileOutputStream(convFile); 
			    fos.write(file.getBytes());
			    fos.close();
				documentManagerBo.uploadFileForUser(convFile, currentPath, file.getOriginalFilename(), userDetails.getUserId(),
						currentOrgDetails.getOrgId(), solution.getSolutionID(), "", solution.getSolutionName(),propertyJsonArr);
			}
			model.put("status", CollaborationProperties.getValue("app.success"));
			model.put("statusMessage", "File Uploaded Successfully");
		}catch(Throwable e){
			logger.error("ERROR - CollaborationController -> collaborationUploadFile()");
			model.put("status", "error");
			model.put("statusMessage", "Error processing file");
			e.printStackTrace();
		}
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView getMyContentForCurrentUser(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getMyContentForCurrentUser()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<DocumentWrapper> myContents = new ArrayList<DocumentWrapper>();
		String finalDisplayPath = "";
		String finalUidPath = "";
		String currentPrivilegeOnFolder = CONSUMER;
		try{
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			
			String requestOriginForCollab = request.getParameter("requestOriginForCollab");
			//display name and path details.
			String currentRequestDirectory = request.getParameter("currentDirectory");
			String currentDirectory = "".equalsIgnoreCase(currentRequestDirectory)?MY_CONTENTS_ROOT_DISPLAY_NAME:currentRequestDirectory;
			String currentDirectoryFullPath = request.getParameter("currentDirectoryFullPath");

			//uid name and path details - to fetch all privileges.
			String currentRequestDirectoryUid = request.getParameter("currentDirectoryUid");
			String currentDirectoryUid = "".equalsIgnoreCase(currentRequestDirectoryUid)?MY_CONTENTS_ROOT_UID:currentRequestDirectoryUid;
			String currentDirectoryFullPathUid = request.getParameter("currentDirectoryFullPathUid");

			/*if(currentDirectoryFullPathUid.indexOf(SEPARATOR+currentRequestDirectoryUid+SEPARATOR)>-1){
				finalDisplayPath = currentDirectoryFullPath.
						substring(0,currentDirectoryFullPath.indexOf(SEPARATOR+currentDirectory+SEPARATOR))
						+SEPARATOR+currentDirectory+SEPARATOR;
				finalUidPath = currentDirectoryFullPathUid.
						substring(0, currentDirectoryFullPathUid.indexOf(SEPARATOR+currentRequestDirectoryUid+SEPARATOR))
						+SEPARATOR+currentDirectoryUid+SEPARATOR;
			}*/
			if(MY_CONTENTS_ROOT_UID.equalsIgnoreCase(currentDirectoryUid)){
				//refresh the current creator and owner contents.
				finalDisplayPath=MY_CONTENTS_ROOT_DISPLAY_NAME;
				finalUidPath=MY_CONTENTS_ROOT_UID;
				myContents=documentManagerBo.getMyContentForCurrentUser(currentDirectoryUid,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId);
				MY_CONTENTS_PRIVATE_DIRECTORY = new ArrayList<String>();
				for(DocumentWrapper dw:myContents){
					if(dw.getEntityType().equalsIgnoreCase("DIRECTORY") && (dw.getPrivilegeName().equalsIgnoreCase(CREATOR))){
						MY_CONTENTS_PRIVATE_DIRECTORY.add(dw.getEntityId());
					}
				}
			}else {
				String [] displayArr = currentDirectoryFullPath.replaceAll("^\\###|\\###$", "").split("###");
				String [] UidArr = currentDirectoryFullPathUid.replaceAll("^\\###|\\###$", "").split("###");
				for(int i =0;i<UidArr.length;i++){
					
					if(!UidArr[i].equalsIgnoreCase(currentDirectoryUid)){
						finalUidPath = finalUidPath+SEPARATOR+UidArr[i];
						finalDisplayPath = finalDisplayPath+SEPARATOR+displayArr[i];
					}else{
						finalDisplayPath=finalDisplayPath+SEPARATOR+currentDirectory+SEPARATOR;
						finalUidPath=finalUidPath+SEPARATOR+currentDirectoryUid+SEPARATOR;
						break;
					}
				}
				
				//find privilege based on parent
				currentPrivilegeOnFolder =MY_CONTENTS_PRIVATE_DIRECTORY.contains(currentDirectoryUid)?CREATOR:documentManagerBo.getPrivilegeByParentMyContent(finalUidPath.replaceAll("^\\###|\\###$", "")
						,currentRequestDirectoryUid,userDetails.getUserId(), currentOrgDetails.getOrgId(), solutionId);
				myContents =documentManagerBo.getContentForCurrentUserAtDirectory(currentDirectoryUid,userDetails.getUserId(),
						currentOrgDetails.getOrgId(),solutionId,currentPrivilegeOnFolder,requestOriginForCollab); 
			}
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getMyContentForCurrentUser()");
		}
		model.put("currentPrivilegeOnFolder", currentPrivilegeOnFolder);
		model.put("finalDisplayPath", finalDisplayPath);
		model.put("finalUidPath", finalUidPath);
		model.put("myContents", myContents);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView getSharedContentForCurrentUser(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getSharedContentForCurrentUser()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		String finalDisplayPath = "";
		String finalUidPath = "";
		String currentPrivilegeOnFolder = CONSUMER;
		try{
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			
			String requestOriginForCollab = request.getParameter("requestOriginForCollab");
			//display name and path details.
			String currentRequestDirectory = request.getParameter("currentDirectory");
			String currentDirectory = "".equalsIgnoreCase(currentRequestDirectory)?SHARED_ROOT_DISPLAY_NAME:currentRequestDirectory;
			String currentDirectoryFullPath = request.getParameter("currentDirectoryFullPath");

			//uid name and path details - to fetch all privileges.
			String currentRequestDirectoryUid = request.getParameter("currentDirectoryUid");
			String currentDirectoryUid = "".equalsIgnoreCase(currentRequestDirectoryUid)?SHARED_ROOT_UID:currentRequestDirectoryUid;
			String currentDirectoryFullPathUid = request.getParameter("currentDirectoryFullPathUid");


			/*if(currentDirectoryFullPathUid.indexOf(SEPARATOR+currentRequestDirectoryUid+SEPARATOR)>-1){
				finalDisplayPath = currentDirectoryFullPath.
						substring(0,currentDirectoryFullPath.indexOf(SEPARATOR+currentDirectory+SEPARATOR))
						+SEPARATOR+currentDirectory+SEPARATOR;
				finalUidPath = currentDirectoryFullPathUid.
						substring(0, currentDirectoryFullPathUid.indexOf(SEPARATOR+currentRequestDirectoryUid+SEPARATOR))
						+SEPARATOR+currentDirectoryUid+SEPARATOR;
			}*/
			if(SHARED_ROOT_UID.equalsIgnoreCase(currentDirectoryUid)){
				//refresh the current creator and owner contents.
				finalDisplayPath=SHARED_ROOT_DISPLAY_NAME;
				finalUidPath=SHARED_ROOT_UID;
				
			}else {
				String [] displayArr = currentDirectoryFullPath.replaceAll("^\\###|\\###$", "").split("###");
				String [] UidArr = currentDirectoryFullPathUid.replaceAll("^\\###|\\###$", "").split("###");
				for(int i =0;i<UidArr.length;i++){
					
					if(!UidArr[i].equalsIgnoreCase(currentDirectoryUid)){
						finalUidPath = finalUidPath+SEPARATOR+UidArr[i];
						finalDisplayPath = finalDisplayPath+SEPARATOR+displayArr[i];
					}else{
						finalDisplayPath=finalDisplayPath+SEPARATOR+currentDirectory+SEPARATOR;
						finalUidPath=finalUidPath+SEPARATOR+currentDirectoryUid+SEPARATOR;
						break;
					}
				}
			}
			
			
			
			//if current request is root path default privilege at root for shared is consumer else figure out folder level privilege. 
			currentPrivilegeOnFolder = currentRequestDirectoryUid.equalsIgnoreCase(SHARED_ROOT_UID)?CONSUMER:documentManagerBo.getPrivilegeByParent(finalUidPath.replaceAll("^\\###|\\###$", "")
					,currentRequestDirectoryUid,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId);
			sharedContents = SHARED_ROOT_UID.equalsIgnoreCase(currentDirectoryUid)?
					documentManagerBo.getSharedContentForCurrentUser(currentDirectoryUid,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId):
						documentManagerBo.getContentForCurrentUserAtDirectory(currentDirectoryUid,
								userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId,currentPrivilegeOnFolder,requestOriginForCollab);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getSharedContentForCurrentUser()");
		}
		model.put("currentPrivilegeOnFolder", currentPrivilegeOnFolder);
		model.put("finalDisplayPath", finalDisplayPath);
		model.put("finalUidPath", finalUidPath);
		model.put("sharedContents", sharedContents);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView collaborationDownloadFile(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController ->  collaborationDownloadFile()");
		response.reset();
		try{
			VyasaSolution solution  = (VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION);
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			Boolean accessCheckFlag = false;
			
			//	Integer versionNumber = Integer.parseInt(request.getParameter("versionNumber"));
			String entityName = request.getParameter("entityName");
			String entityUid = request.getParameter("entityUid");
			accessCheckFlag = documentManagerBo.resolveEntityForDownload(entityUid, userDetails.getUserId(), currentOrgDetails.getOrgId(), solution.getSolutionID());
			if(null==entityName || "".equalsIgnoreCase(entityName) || null==entityUid || "".equalsIgnoreCase(entityUid) || !accessCheckFlag) {
				return new ModelAndView("accessDeniedView","model",null); 
			}else {
				response.setHeader("Content-Disposition", "download;filename=" +entityName);
				ServletOutputStream outputStream = response.getOutputStream();
				MimetypesFileTypeMap mimetypesFileTypeMap=new MimetypesFileTypeMap();
		        response.setContentType(mimetypesFileTypeMap.getContentType(entityName));
				InputStream fileInputStream= documentManagerBo.downloadFile(entityUid, solution.getSolutionName());
				byte [] byteArray = IOUtils.toByteArray(fileInputStream);
				outputStream.write(byteArray);
				outputStream.flush();
				fileInputStream.close();
				outputStream.close();
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
			return new ModelAndView("accessDeniedView","model",null);  
		}
		return null; 

	}

	public ModelAndView collaborationAddNewFolder(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationAddNewFolder()");
		Map<String,Object> model = new HashMap<String, Object>();
		try{
			VyasaSolution solution = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION));
			String currentDirectory = request.getParameter("currentDirectory");
			String newFolderName = request.getParameter("newFolderName");
			Integer templateId = request.getParameter("templateId")==null? null:"".equalsIgnoreCase(request.getParameter("templateId"))?null:Integer.parseInt(request.getParameter("templateId"));
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			String description = request.getParameter("folderDescription");
			Integer securityTemplateId= request.getParameter("securityTemplateId")==null?null:Integer.parseInt(request.getParameter("securityTemplateId"));
			Integer isSecurityTemplate =Integer.parseInt(request.getParameter("isSecurityTemplateApplied"));
			
			JSONArray secPropertyJsonArr = request.getParameter("secPropertyArray")==null?new JSONArray():(JSONArray) new JSONParser().parse(request.getParameter("secPropertyArray"));
			//check if folder name exist in the directory
			//Boolean folderNameCheck = documentManagerBo.collaborationAddNewFolderNameCheck(currentDirectory,newFolderName,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId);

			//if(folderNameCheck){

			//TODO input templateId
			documentManagerBo.createFolderForUser(newFolderName, 
					currentDirectory, userDetails.getUserId(), currentOrgDetails.getOrgId(),
					solution.getSolutionID(), description, solution.getSolutionName(),templateId,PUBLIC_FLAG,secPropertyJsonArr
					,isSecurityTemplate==1?true:false,securityTemplateId);
			
			//documentManagerBo.collaborationAddNewFolder(currentDirectory,newFolderName,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId);
			/*}else{
				model.put("status", CollaborationProperties.getValue("app.failure"));
				model.put("statusMessage", CollaborationProperties.getValue("app.error.folderNameExist"));
			}*/

			model.put("status", CollaborationProperties.getValue("app.success"));
			model.put("statusMessage", "Folder Created Successfully");
		}catch(VyasaException e){
			model.put("status", CollaborationProperties.getValue("app.failure"));
			model.put("statusMessage", CollaborationProperties.getValue("app.error.fileNameExist"));
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationAddNewFolder()");
		}
		catch(Throwable e){
			model.put("status", CollaborationProperties.getValue("app.failure"));
			model.put("statusMessage", CollaborationProperties.getValue("app.error.createNewFolderMsg"));
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationAddNewFolder()");
		}

		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView collaborationRenameEntity(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationRenameEntity()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<DocumentWrapper> sharedContents = new ArrayList<DocumentWrapper>();
		JSONObject res=new JSONObject() ;
		try{
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			String currentEntityUid = request.getParameter("currentEntityUid");
			String newEntityName = request.getParameter("newEntityName");
			String parentUidId = request.getParameter("parentUidId");
			String selectedCurrentType = request.getParameter("currentFolderType");
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			Integer securityTemplateId= request.getParameter("securityTemplateId")==null?null:Integer.parseInt(request.getParameter("securityTemplateId"));
			Integer isSecurityTemplate =Integer.parseInt(request.getParameter("isSecurityTemplateApplied"));
			JSONArray secPropertyJsonArr = request.getParameter("secPropertyArray")==null?new JSONArray():(JSONArray) new JSONParser().parse(request.getParameter("secPropertyArray"));
			String description = request.getParameter("folderDescription");
			Integer isNameModified = Integer.parseInt(request.getParameter("isNameModified"));
			
			//check if folder/file name exist in the directory
			if(TYPE_FILE.equalsIgnoreCase(selectedCurrentType)){
				res = documentManagerBo.renameFile(currentEntityUid, newEntityName, parentUidId, solutionId, userDetails.getUserId());
			}else if(TYPE_DIRECTORY.equalsIgnoreCase(selectedCurrentType)){
				res = documentManagerBo.renameDirectory(currentEntityUid, newEntityName, parentUidId, solutionId, userDetails.getUserId(),
						description,PUBLIC_FLAG,secPropertyJsonArr,isSecurityTemplate==1?true:false,securityTemplateId,isNameModified);
			}

		}catch(Throwable e){
			model.put("status", CollaborationProperties.getValue("app.failure"));
			model.put("statusMessage", CollaborationProperties.getValue("app.error.createNewFolderMsg"));
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationRenameEntity()");
		}
		model.put("respJson", res);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView collaborationSearchMyContent(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationSearchMyContent()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<DocumentWrapperForSearch> myContents = new ArrayList<DocumentWrapperForSearch>();
		try{
			
			String searchValue = request.getParameter("searchValue");
			String currentRequestDirectoryUid = request.getParameter("currentDirectoryUid");
			String currentSearchDirDisplayName = request.getParameter("currentSearchDirDisplayName");
			String currentDirectoryFullPathUid = request.getParameter("currentDirectoryFullPathUid");
			String currentDirectoryFullPath = request.getParameter("currentDirectoryFullPath");
			String requestOriginForCollab = request.getParameter("requestOriginForCollab");
			
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			
			
			String currentDirectoryUid = "".equalsIgnoreCase(currentRequestDirectoryUid)?MY_CONTENTS_ROOT_UID:currentRequestDirectoryUid;
			
			String finalUidPath = "";
			if(currentDirectoryFullPathUid.indexOf(SEPARATOR+currentRequestDirectoryUid+SEPARATOR)>-1){
				finalUidPath = currentDirectoryFullPathUid.
						substring(0, currentDirectoryFullPathUid.indexOf(SEPARATOR+currentRequestDirectoryUid+SEPARATOR))
						+SEPARATOR+currentDirectoryUid+SEPARATOR;
			}
			String parentPrivilege = documentManagerBo.getPrivilegeByParentMyContent(finalUidPath.replaceAll("^\\###|\\###$", "")
					,currentRequestDirectoryUid,userDetails.getUserId(), currentOrgDetails.getOrgId(), solutionId);
			
			
			myContents=documentManagerBo.getCollaborationSearchMyContent(currentRequestDirectoryUid,
					searchValue,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId,parentPrivilege,
					currentDirectoryFullPathUid.replaceAll("^\\###|\\###$", ""),currentSearchDirDisplayName,
					currentDirectoryFullPath.replaceAll("^\\###|\\###$", ""),requestOriginForCollab,MY_CONTENTS_ROOT_UID);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationSearchMyContent()");
		}
		model.put("myContents", myContents);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView collaborationSearchSharedContent(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationSearchSharedContent()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<DocumentWrapper> myContents = new ArrayList<DocumentWrapper>();
		try{
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			String currentDirectory = request.getParameter("currentDirectory");
			String currentDirectoryFullPath = request.getParameter("currentDirectoryFullPath");
			String searchValue = request.getParameter("searchValue");
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			myContents=documentManagerBo.getCollaborationSearchSharedContent(currentDirectory,searchValue,userDetails.getUserId(),currentOrgDetails.getOrgId(),solutionId);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationSearchSharedContent()");
		}
		model.put("myContents", myContents);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView collaborationDeleteEntity(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationDeleteEntity()");
		Map<String,Object> model = new HashMap<String, Object>();
		try{
			VyasaSolution solution = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION));
			String currentEntityUid = request.getParameter("currentEntityUid");
			String currentEntityType = request.getParameter("currentEntityType");
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			if(TYPE_FILE.equalsIgnoreCase(currentEntityType)){
				documentManagerBo.deleteFile(currentEntityUid, solution.getSolutionName());
			}else if(TYPE_DIRECTORY.equalsIgnoreCase(currentEntityType)){
				documentManagerBo.deleteFolder(currentEntityUid, solution.getSolutionName());
			}
			model.put("status", CollaborationProperties.getValue("app.success"));
			model.put("statusMessage", "Deleted successfully.");
		}catch(Throwable e){
			model.put("status", CollaborationProperties.getValue("app.failure"));
			model.put("statusMessage", "Error occured.");
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationDeleteEntity()");
		}
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView getListOfApplicableOrgsForCurrentUser(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getListOfApplicableOrgsForCurrentUser()");
		Map<String,Object> model = new HashMap<String, Object>();
		JsonArray applicableOrgs = new JsonArray();
		try{
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			boolean isLeadOrg = AppContextUtil.isLeadOrganisation();
			if(isLeadOrg){
				applicableOrgs = documentManagerBo.getOrgForUserInCurrentContent(solutionId, userDetails.getUserId(), currentOrgDetails.getOrgId(), ORG_UNIT_DIMENSION_NAME);
			} else {
				JsonObject organizationJsonObject = new JsonObject();
			      organizationJsonObject.addProperty("orgId", currentOrgDetails.getOrgId());
			      organizationJsonObject.addProperty("orgName",currentOrgDetails.getOrgName());
			      applicableOrgs.add(organizationJsonObject);
			}
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getListOfApplicableOrgsForCurrentUser()");
		}
		model.put("applicableOrgs", applicableOrgs.toString());
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView getListOfUsersForChosenOrg(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getListOfUsersForChosenOrg()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<Users> usersList = new ArrayList<Users>();
		try{
			Long chosenOrgId = Long.parseLong(request.getParameter("chosenOrgId"));
			//fetch user id list for chosen org.
			UsersCache usersCache = UsersCache.getInstance();
			Set<String> userIds = usersCache.getOrgToUsersRelation(chosenOrgId);
			userIds.addAll(UserWrapperCache.getInstance().getOrgToUserRelation(chosenOrgId.toString()));
			List<Integer> userIdsInteger = userIds.stream().map(Integer::parseInt).collect(Collectors.toList());
			usersList.addAll(usersCache.getAll(userIdsInteger));
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getListOfUsersForChosenOrg()");
		}
		model.put("usersList", usersList);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView saveOrUpdatePrivilege(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> saveOrUpdatePrivilege()");
		Map<String,Object> model = new HashMap<String, Object>();
		try{
			String currentElement = request.getParameter("currentElement");
			String currentElementDisplayName = request.getParameter("currentElementDisplayName");
			String jsonArrOfPrivilegeString = request.getParameter("jsonArrOfPrivilegeString");
			String jsonArrOfPrivilegeStringForAccess = request.getParameter("jsonArrOfPrivilegeStringForAccess");
			Boolean isNotificationRequired = Boolean.parseBoolean(request.getParameter("isNotificationRequired"));
			Integer solutionId = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION)).getSolutionID();
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			documentManagerBo.saveOrUpdatePrivileges(jsonArrOfPrivilegeString,jsonArrOfPrivilegeStringForAccess,currentElement
					,solutionId,userDetails,currentOrgDetails,currentElementDisplayName,isNotificationRequired);
			model.put("status", CollaborationProperties.getValue("app.success"));
			model.put("statusMessage", "Saved privileges successfully.");
		}catch(Throwable e){
			model.put("status", CollaborationProperties.getValue("app.failure"));
			model.put("statusMessage", "Failed to save privileges.");
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> saveOrUpdatePrivilege()");
		}

		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	public ModelAndView getListOfPrivilegesForEntity(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getListOfPrivilegesForEntity()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<ContentSecurityDetails> privilegeList = new ArrayList<ContentSecurityDetails>();
		List<ContentSecurityDetailsAccessRole> privilegeListAccessRole = new ArrayList<ContentSecurityDetailsAccessRole>();
		try{
			String currentUid = request.getParameter("currentUid");
			String parentUid = request.getParameter("parentUid");
			String fullPathUid = request.getParameter("fullPathUid");
			VyasaSolution solution= (VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION);
			privilegeList = documentManagerBo.getListOfPrivilegesForEntity(currentUid,parentUid,fullPathUid,solution.getSolutionID());
			privilegeListAccessRole = documentManagerBo.getListOfPrivilegesForEntityByAccessRole(currentUid,parentUid,fullPathUid,solution.getSolutionID());
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getListOfPrivilegesForEntity()");
		}
		model.put("privilegeList", privilegeList);
		model.put("privilegeListAccessRole", privilegeListAccessRole);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

	public ModelAndView getListOfVersionForFile(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getListOfVersionForFile()");
		Map<String,Object> model = new HashMap<String, Object>();
		try{
			String fileId = request.getParameter("fileId");
			VyasaSolution solution= (VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION);
			JSONArray versions = documentManagerBo.getAllVersionForFile(fileId, solution.getSolutionName());
			model.put("versions", versions);
			model.put("success", true);
		}catch(Throwable e){
			logger.error("ERROR - CollaborationController -> getListOfPrivilegesForEntity()",e);
			model.put("success", false);
		}
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	public ModelAndView collaborationValidateAndReuploadFile(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationValidateAndReuploadFile()");
		Map<String,Object> model = new HashMap<String, Object>();
		try{
			VyasaSolution solution = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION));
			String currentPath = request.getParameter("currentPath");
			String oldNameForCheck = request.getParameter("oldNameForCheck");
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();//SecurityContextUtil.getCurrentUser();
			MultipartFile file =((MultipartHttpServletRequest) request).getFile("file");
			JSONArray propertyJsonArr = (JSONArray) new JSONParser().parse(request.getParameter("propertyArray"));
			if(file!=null){
				if(!oldNameForCheck.equalsIgnoreCase(file.getOriginalFilename())){
					throw new VyasaException();
				}
				File convFile = new File(file.getOriginalFilename());
			    convFile.createNewFile(); 
			    FileOutputStream fos = new FileOutputStream(convFile); 
			    fos.write(file.getBytes());
			    fos.close();
				documentManagerBo.uploadFileForUser(convFile, currentPath, file.getOriginalFilename(), userDetails.getUserId(),
						currentOrgDetails.getOrgId(), solution.getSolutionID(), "", solution.getSolutionName(),propertyJsonArr);

			}
			model.put("status", CollaborationProperties.getValue("app.success"));
			model.put("statusMessage", "File Uploaded Successfully");
		}catch(VyasaException v){
			logger.error("ERROR - CollaborationController -> collaborationValidateAndReuploadFile()");
			model.put("status", "error");
			model.put("statusMessage", "File names doesn't match.");
		}catch(Throwable e){
			logger.error("ERROR - CollaborationController -> collaborationValidateAndReuploadFile()");
			model.put("status", "error");
			model.put("statusMessage", "Error processing file");
			e.printStackTrace();
		}

		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	public ModelAndView collaborationGetInfoForAFile(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> collaborationGetInfoForAFile()");
		//TODO bring in file info comments and other stuffs.
		Map<String,Object> model = new HashMap<String, Object>();
		List<ContentSecurityDetails> privilegeList = new ArrayList<ContentSecurityDetails>();
		try{
			throw new Exception();
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> collaborationGetInfoForAFile()");
		}
		model.put("privilegeList", privilegeList);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	public ModelAndView collaborationDownloadFileByVersion(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController ->  collaborationDownloadFileByVersion()");
		response.reset();
		try{
			VyasaSolution solution  = (VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION);
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();
			Boolean accessCheckFlag = false;
			String entityName = request.getParameter("entityName");
			String entityUid = request.getParameter("entityUid");
			String entityVersionNumber = request.getParameter("versionNumber"); 
			accessCheckFlag = documentManagerBo.resolveEntityForDownload(entityUid, userDetails.getUserId(), currentOrgDetails.getOrgId(), solution.getSolutionID());
			if(null==entityVersionNumber || "".equalsIgnoreCase(entityVersionNumber) ||  null==entityName || "".equalsIgnoreCase(entityName) || null==entityUid || "".equalsIgnoreCase(entityUid) || !accessCheckFlag) {
				return new ModelAndView("accessDeniedView","model",null); 
			}else {
				response.setHeader("Content-Disposition", "download;filename=" +entityName);
				ServletOutputStream outputStream = response.getOutputStream();
				MimetypesFileTypeMap mimetypesFileTypeMap=new MimetypesFileTypeMap();
		        response.setContentType(mimetypesFileTypeMap.getContentType(entityName));
				InputStream fileInputStream= documentManagerBo.downloadFileForAVersion(entityUid, solution.getSolutionName(), entityVersionNumber);
				byte [] byteArray = IOUtils.toByteArray(fileInputStream);
				outputStream.write(byteArray);
				outputStream.flush();
				fileInputStream.close();
				outputStream.close();
			}


		}
		catch (Throwable e) {
			
			e.printStackTrace(); 
			return new ModelAndView("accessDeniedView","model",null); 
		}
		return null; 
	}

	public ModelAndView getPropertiesAndTemplateDetails(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getPropertiesAndTemplateDetails()");
		Map<String,Object> model = new HashMap<String, Object>();
		JSONArray myContents = new JSONArray();

		try{

			myContents=documentManagerBo.getAllTemplateDetails();
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getPropertiesAndTemplateDetails()");
		} 
		model.put("templateDetails", myContents.toJSONString()); 
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	
	public ModelAndView getUserSecurityProfileForCurrentContext(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getUserSecurityProfileForCurrentContext()");
		Map<String,Object> model = new HashMap<String, Object>();
		JSONArray myContents = new JSONArray();

		try{
			VyasaSolution solution = ((VyasaSolution)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_SOLUTION));
			OrganisationUnit currentOrgDetails = ((OrganisationUnit)AppContextUtil.getUserContextAttribute(AppContextUtil.CURRENT_ORG_UNIT));
			Users userDetails = AppContextUtil.getCurrentUser();//SecurityContextUtil.getCurrentUser();
			
			myContents=documentManagerBo.getUserSecurityProfileForCurrentContext( currentOrgDetails.getOrgId(), userDetails.getUserId(), solution.getSolutionID());
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getPropertiesAndTemplateDetails()");
		} 
		model.put("templateDetails", myContents.toJSONString()); 
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	
	public ModelAndView getTemplatesForDirectory(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getTemplatesForDirectory()");
		Map<String,Object> model = new HashMap<String, Object>();
		JSONArray myContents = new JSONArray();

		try{
			String directoryId = request.getParameter("directoryId");
			myContents=documentManagerBo.getTemplatesForDirectory(directoryId);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getTemplatesForDirectory()");
		} 
		model.put("templateDetails", myContents.toJSONString()); 
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	
	public ModelAndView getPropertiesOfContent(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getPropertiesOfContent()");
		Map<String,Object> model = new HashMap<String, Object>();
		JsonObject myContents = new JsonObject();

		try{
			String contentId = request.getParameter("contentId");
			String contentType = request.getParameter("contentType");
			myContents=documentManagerBo.getPropertiesOfContent(contentId, contentType);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getPropertiesOfContent()");
		}
		model.put("entityInfo", myContents+""); 
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	
	public ModelAndView getPropertiesOfContentVersion(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - CollaborationController -> getPropertiesOfContentVersion()");
		Map<String,Object> model = new HashMap<String, Object>();
		JsonObject myContents = new JsonObject();

		try{
			String contentId = request.getParameter("contentId");
			String contentType = request.getParameter("contentType");
			Integer versionNumber = Integer.parseInt(request.getParameter("versionNumber"));
			myContents=documentManagerBo.getPropertiesOfVersionedFile(contentId, contentType, versionNumber);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getPropertiesOfContentVersion()");
		}
		model.put("entityInfo", myContents+""); 
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}
	
	public ModelAndView getAccessRoleForCurrentOrg(HttpServletRequest request, HttpServletResponse response) {
		//change to roles fetch.
		logger.info("EXEFLOW - CollaborationController -> getAccessRoleForCurrentOrg()");
		Map<String,Object> model = new HashMap<String, Object>();
		List<AccessRole> accessRoleList = new ArrayList<AccessRole>();
		try{
			Integer chosenOrgId = Integer.parseInt(request.getParameter("chosenOrgId"));
			//fetch user id list for chosen org.
			accessRoleList = documentManagerBo.getListAccessRolesForOrg(chosenOrgId);
		}catch(Throwable e){
			e.printStackTrace();
			logger.error("ERROR - CollaborationController -> getAccessRoleForCurrentOrg()");
		}
		model.put("accessRoleList", accessRoleList);
		return new ModelAndView("ajaxResponseJsonView", "model",model );
	}

}