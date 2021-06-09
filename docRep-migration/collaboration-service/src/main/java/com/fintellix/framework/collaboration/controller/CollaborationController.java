package com.fintellix.framework.collaboration.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.RepositoryException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.aspose.cells.FileFormatType;
import com.aspose.cells.Workbook;
import com.fintellix.administrator.AdministratorPropertyReader;
import com.fintellix.administrator.model.AccessRole;
import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
import com.fintellix.administrator.redis.AdminCacheHelper;
import com.fintellix.administrator.redis.impl.UserWrapperCache;
import com.fintellix.administrator.redis.impl.UsersCache;
import com.fintellix.framework.collaboration.bo.DocumentManagerBo;
import com.fintellix.framework.collaboration.dto.CollaborationErrorLog;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.Directory;
import com.fintellix.framework.collaboration.dto.DocumentWrapper;
import com.fintellix.framework.collaboration.dto.DocumentWrapperForSearch;
import com.fintellix.platformcore.common.exception.VyasaException;
import com.fintellix.platformcore.usermanagement.solution.dto.VyasaSolution;
import com.fintellix.platformcore.web.utils.AppContextUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
public class CollaborationController {
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

	private static AdminCacheHelper adminCacheUtil = AdminCacheHelper.getInstance();
	private static final String SEPARATOR = "###";
	private static final Integer PRIVATE_FLAG = 1;
	private static final Integer PUBLIC_FLAG = 0;
	private static final String SHARED_ROOT_UID=CollaborationProperties.getProperty("app.RootPathForSharedContentsUid");
	private static final String SHARED_ROOT_DISPLAY_NAME=CollaborationProperties.getProperty("app.RootPathForSharedContentsDisplay");
	private static final String OWNER=CollaborationProperties.getProperty("app.ownerPrivilegeName");
	private static final String CREATOR=CollaborationProperties.getProperty("app.creatorPrivilegeName");
	private static final String CONTRIBUTOR=CollaborationProperties.getProperty("app.contributorPrivilegeName");
	private static final String CONSUMER=CollaborationProperties.getProperty("app.consumerPrivilegeName");
	private static final String TYPE_FILE = CollaborationProperties.getProperty("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getProperty("app.typeDirectoryName");
	private static final String ORG_UNIT_DIMENSION_NAME= AdministratorPropertyReader.getInstance().getProperty("app.orgDimensionName");
	private static final String ROOT_FOLDER_FOR_MIGRATION= CollaborationProperties.getProperty("app.rootFolderName");
	private static final String migrationFolderPath = CollaborationProperties.getProperty("app.migrationFolderPath");
	//static variable to maintain my contents root and display root folder name with user name.
	private static String MY_CONTENTS_ROOT_UID = CollaborationProperties.getProperty("app.RootPathForMyContentsUid");
	private static  String MY_CONTENTS_ROOT_DISPLAY_NAME =CollaborationProperties.getProperty("app.RootPathForMyContentsDisplayName");

	//private variable for maintaining user files.
	private List<String> MY_CONTENTS_PRIVATE_DIRECTORY = new ArrayList<String>();
	@Autowired
	private DocumentManagerBo documentManagerBo;

	public DocumentManagerBo getDocumentManagerBo() {
		return documentManagerBo;
	}

	public void setDocumentManagerBo(DocumentManagerBo documentManagerBo) {
		this.documentManagerBo = documentManagerBo;
	}



	@RequestMapping(value = "/collaboration/bulkupload", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> bulkUpload(HttpServletRequest request) throws Exception {
		logger.info("EXEFLOW - CollaborationService -CollaborationController - bulkUpload");
		List<CollaborationErrorLog> errorLogList=new ArrayList<CollaborationErrorLog>();
		JSONObject json=new JSONObject(); 
		try {
			String orgName = CollaborationProperties.getProperty("app.organizationName");
			Integer orgId=adminCacheUtil.getOrganisationByName(orgName).getOrgId();
			String solutionName = CollaborationProperties.getProperty("app.solutionName");
			Integer solutionId=documentManagerBo.getSolutionIdByName(solutionName);
			String userName = CollaborationProperties.getProperty("app.userName");
			Integer userId=adminCacheUtil.getUserByName(userName).getUserId();
			//check for root folders and creating if not present.
			String fetchRootUUIDForMyContents = documentManagerBo.getRootUUIDForMyContents(orgName,orgId,solutionId);
			MY_CONTENTS_ROOT_DISPLAY_NAME = orgName;
			if(null!=fetchRootUUIDForMyContents && !"".equalsIgnoreCase(fetchRootUUIDForMyContents)){
				MY_CONTENTS_ROOT_UID =fetchRootUUIDForMyContents;
			}else{

				//TODO template id or default one.
				documentManagerBo.createFolderForUser(MY_CONTENTS_ROOT_DISPLAY_NAME, 
						null, null, orgId,
						solutionId, "", solutionName,null,PUBLIC_FLAG,null,false,null);
				MY_CONTENTS_ROOT_UID=documentManagerBo.getRootUUIDForMyContents(orgName,orgId,solutionId);
			}

			if(documentManagerBo.checkPrivateFolderExistenceForUser(MY_CONTENTS_ROOT_UID,userId,orgId,solutionId)){
				documentManagerBo.createFolderForUser(userName,MY_CONTENTS_ROOT_UID, userId, orgId,
						solutionId, "", solutionName,null,PRIVATE_FLAG,null,false,null);

			}
			//check migration
			if(null==documentManagerBo.checkPrivateFolderByNameForUser(MY_CONTENTS_ROOT_UID,userId,orgId,solutionId,ROOT_FOLDER_FOR_MIGRATION)){
				documentManagerBo.createFolderForUser(ROOT_FOLDER_FOR_MIGRATION,MY_CONTENTS_ROOT_UID, userId, orgId,
						solutionId, "", solutionName,null,PRIVATE_FLAG,null,false,null);

			}
			Directory migrationRootDirectoryDetails = documentManagerBo.checkPrivateFolderByNameForUser(MY_CONTENTS_ROOT_UID,userId,orgId,solutionId,ROOT_FOLDER_FOR_MIGRATION);
			String fileLocation = migrationFolderPath;
			File file = new File(fileLocation);
			
			Workbook uploadedWorkbook=new Workbook(fileLocation);
			String fileName=file.getName();
			if(uploadedWorkbook!=null){
				if(getFileExtension(fileName)){
					//validation of headers.
					errorLogList = documentManagerBo.validateHeaders(uploadedWorkbook);
					if(errorLogList!=null && errorLogList.size()>0){
						//TODO write error log here.
						Workbook workbook=documentManagerBo.generateValidationReport(errorLogList);
						File errFile = new File(CollaborationProperties.getProperty("app.errorFilePath")+"Error_Log_"+fileName);
						OutputStream outputStream = new FileOutputStream(errFile);
						workbook.save(outputStream, FileFormatType.XLSX);
						outputStream.flush();
						outputStream.close();
						json.put("status", "failed");
						json.put("message", "Invalid Template.");
						return new ResponseEntity<JSONObject>(json, HttpStatus.BAD_REQUEST);
					}

					//data validation
					errorLogList=documentManagerBo.processBulkUpload(uploadedWorkbook,orgId, fileLocation,fileName,solutionId,solutionName
							,userId, userName,migrationRootDirectoryDetails);
					if(errorLogList!=null && errorLogList.size()>0){
						//TODO write error log here.
						Workbook workbook=documentManagerBo.generateValidationReport(errorLogList);
						File errFile = new File(CollaborationProperties.getProperty("app.errorFilePath")+"Error_Log_"+fileName);
						OutputStream outputStream = new FileOutputStream(errFile);
						// Write your data
						workbook.save(outputStream, FileFormatType.XLSX);
						outputStream.flush();
						outputStream.close();
						json.put("status", "failed");
						json.put("message", "File contains errors");
						return new ResponseEntity<JSONObject>(json, HttpStatus.BAD_REQUEST);
					}
				}else{
					json.put("status", "failed");
					json.put("message", "Invalid file format.");
					return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else{
				json.put("status", "failed");
				json.put("message", "File not found.");
				return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
			}		
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		json.put("status", "Success");
		json.put("message", "File processed successfully.");
		return new ResponseEntity<JSONObject>(json, HttpStatus.OK);
	}

	private boolean getFileExtension(String fileName) {
		String extension ="";
		try {
			extension= fileName.substring(fileName.lastIndexOf(".")+1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		if(extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")){
			return true;
		}else{
			return false;
		}
	}
	
	
}