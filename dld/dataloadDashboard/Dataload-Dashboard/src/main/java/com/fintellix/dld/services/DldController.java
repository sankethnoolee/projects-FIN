package com.fintellix.dld.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.aspose.cells.FileFormatType;
import com.aspose.cells.HtmlSaveOptions;
import com.aspose.cells.Workbook;
import com.fintellix.dld.bo.DldBo;
import com.fintellix.dld.models.DldStatusDownload;
import com.fintellix.dld.models.EntityMasterUploaderDTO;
import com.fintellix.dld.models.EntityOwnerUploaderDTO;
import com.fintellix.dld.models.ErrorLogForUploader;
import com.fintellix.dld.models.FlowTypesUploaderDTO;
import com.fintellix.dld.models.TaskEntityDetailUploaderDTO;
import com.fintellix.dld.models.TaskMasterUploaderDTO;
import com.fintellix.dld.models.TaskRepositoriesUploaderDTO;
import com.fintellix.dld.util.SolutionURLMappingPropertiesLoader;
import com.fintellix.dld.util.UploaderHelper;




@RestController
public class DldController { 
	@Autowired
	private DldBo dldBo;
	private static final Logger logger = LoggerFactory.getLogger(DldController.class);
	
	@Value("${dld.clientSheetName}")
	private String clientSheetName;
	
	@Value("${dld.entityOwnerSheetName}")
	private String entityOwnerSheetName;
	
	@Value("${dld.entityMasterSheetName}")
	private String entityMasterSheetName;
	
	@Value("${dld.flowTypesSheetName}")
	private String flowTypeSheetName;
	
	@Value("${dld.taskrepositorySheetName}")
	private String taskRepositorySheetName;
	
	@Value("${dld.taskMasterSheetName}")
	private String taskMasterSheetName;	
	
	private Map<String, List<String>> allCacheMap=new HashMap<String, List<String>>();
    private List<String> userTokenList=null;
    
    
private static Properties applicationProperties;
	
	static{
		try {
			InputStream inputSteram  = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(inputSteram);
			
		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application properties from class path",e);
		}
	}
    
	private static String  UPLOADER_TEMP_FOLDER=applicationProperties.getProperty("dld.uploaderTempFolder").trim();

	
	@RequestMapping(value = "/getStatsOnChange", method = {RequestMethod.POST, RequestMethod.GET})
	 public ResponseEntity<JSONObject> getMaxBusinessDate(@RequestParam(value="businessDate") String currrentBusinessDate,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getStatsOnChange");
		
		JSONObject json=new JSONObject(); 
		try
		 {
			
			json=dldBo.getStatsOnChange(currrentBusinessDate,(String)request.getSession().getAttribute("clientCode"));
			 return new ResponseEntity<JSONObject>(json, HttpStatus.OK);
			 
		 }
		 catch(Throwable t)
		 {
			return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
		 }
   }
	
	
	
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getcurrrentBusinessDateStatisticsForLoad", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getCurrrentBusinessDateStatistics(HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getCurrrentBusinessDateStatistics");
		JSONObject json = new JSONObject();
		try
		{
			json.put("curBusinessDate",dldBo.getMaxBusinessDate((String)request.getSession().getAttribute("clientCode")));
			return new ResponseEntity<JSONObject>(json, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@RequestMapping(value = "/getloadactualprogressdetails", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONArray> getLoadActualProgressDetails(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getLoadActualProgressDetails");
		JSONArray progressDetails = new JSONArray();
		try
		{
			progressDetails=dldBo.getLoadActualProgressDetails(currrentBusinessDate
					,isFlowFilterApplied,flowFilterCSV,isFrequencyFilterApplied,frequencyFilterCSV,(String)request.getSession().getAttribute("clientCode"));
			return new ResponseEntity<JSONArray>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONArray>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/handleUploadMetadata.htm", method = RequestMethod.POST)
	public String handleUploadMetadata(HttpServletRequest request,@RequestParam("fileName") MultipartFile file){
		logger.info("EXEFLOW - DldController ->  handleUploadMetadata()");
		Map<String, Boolean> fileFormatErrorMap=null;
		JSONObject model = new JSONObject();
		Map<String,Object> uploadDTOMap=new HashMap<String,Object>();
		//List<ClientUploaderDTO> clientList=null;
		List<EntityOwnerUploaderDTO> entityOwnersList=null;
		List<EntityMasterUploaderDTO> entityMasterList=null;
		List<FlowTypesUploaderDTO> flowTypesList=null;
		List<TaskRepositoriesUploaderDTO> taskRepositoriesList=null;
		List<TaskMasterUploaderDTO> taskMasterList=null;
		List<TaskEntityDetailUploaderDTO> taskEntityDetailList=null;
		
		
		String effectiveDate = null;
		String userTokenSession = null;
		Workbook workbook=null;
		if(file!=null){
			if(UploaderHelper.getFileExtension(file.getOriginalFilename())){
				try {
					workbook = new Workbook(file.getInputStream());
					fileFormatErrorMap=UploaderHelper.getFileFormatValidation(workbook);
					if(!fileFormatErrorMap.get("isHeaderCorrect")){
						model.put("isFileFormatCorrect", true);
						model.put("success", false);
						model.put("isHeaderCorrect", false);
						model.put("entityOwnerHeader", fileFormatErrorMap.get("entityOwner"));
						model.put("entityMasterHeader", fileFormatErrorMap.get("entityMaster"));
						model.put("flowTypesHeader", fileFormatErrorMap.get("flowTypes"));
						model.put("taskRepositoryHeader", fileFormatErrorMap.get("taskRepositories"));
						model.put("taskMasterHeader", fileFormatErrorMap.get("taskMaster"));
					}else{
						
						
						List<Object> objList=dldBo.getSheetForEntity(workbook);
						if(objList!=null && objList.size()>0){
							entityOwnersList=(List<EntityOwnerUploaderDTO>) objList.get(0);
							entityMasterList=(List<EntityMasterUploaderDTO>) objList.get(1);
							flowTypesList=(List<FlowTypesUploaderDTO>) objList.get(2);
							taskRepositoriesList=(List<TaskRepositoriesUploaderDTO>) objList.get(3);
							taskMasterList=(List<TaskMasterUploaderDTO>) objList.get(4);
							taskEntityDetailList=(List<TaskEntityDetailUploaderDTO>) objList.get(5);
							uploadDTOMap.put("entityOwnersList",entityOwnersList);
							uploadDTOMap.put("entityMasterList",entityMasterList);
							uploadDTOMap.put("flowTypesList",flowTypesList);
							uploadDTOMap.put("taskRepositoriesList",taskRepositoriesList);
							uploadDTOMap.put("taskMasterList",taskMasterList);
							uploadDTOMap.put("taskEntityDetailList",taskEntityDetailList);
						}
						
						if(entityOwnersList.size()==0  || entityMasterList.size()==0  || flowTypesList.size()==0  || taskRepositoriesList.size()==0  || taskMasterList.size()==0 || taskEntityDetailList.size()==0){
							model.put("success", false);
							model.put("isFileFormatCorrect", true);
							model.put("isHeaderCorrect", true);
							model.put("isFileNotEmpty", false);
						}else
						{
						effectiveDate = request.getParameter("effectiveDate");
						userTokenSession=effectiveDate+file.getOriginalFilename()+new Date();
						//saving DTOs to Cache
						dldBo.putUploadedFileToCache(userTokenSession,uploadDTOMap);
						if(request.getSession().getAttribute("cacheList")==null){
							userTokenList=new ArrayList<String>();
							userTokenList.add(userTokenSession);
							allCacheMap.put("uploadedFileListCache", userTokenList);
							request.getSession().setAttribute("cacheList", allCacheMap);
						}else{
							allCacheMap=(Map<String, List<String>>) request.getSession().getAttribute("cacheList");
							userTokenList=allCacheMap.get("uploadedFileListCache");
							if(userTokenList!=null){
								userTokenList.add(userTokenSession);
							}else{
								userTokenList=new ArrayList<String>();
								userTokenList.add(userTokenSession);
							}
							allCacheMap.put("uploadedFileListCache", userTokenList);
							request.getSession().setAttribute("cacheList", allCacheMap);
							
						}
						//saving Uploaded File 
						
						String uploaderPathTemp = request.getSession().getServletContext().getRealPath(UPLOADER_TEMP_FOLDER) + File.separator + request.getSession().getId() + File.separator;
						File tempDir=new File(uploaderPathTemp);
						tempDir.mkdirs();
						workbook.save(uploaderPathTemp+file.getOriginalFilename());
						HtmlSaveOptions options = new HtmlSaveOptions();
						options.setExportHiddenWorksheet(false);
						workbook.save(uploaderPathTemp+file.getOriginalFilename()+".html", options);
						model.put("isFileFormatCorrect", true);
						model.put("isHeaderCorrect", true);
						model.put("isFileNotEmpty", true);
						model.put("success", true);
						model.put("userTokenSession",userTokenSession);
						model.put("uploadedFilePath",UPLOADER_TEMP_FOLDER+"/"+request.getSession().getId()+File.separator+file.getOriginalFilename()+".html");
						model.put("fileName",file.getOriginalFilename());;
						model.put("entityMasterCount",UploaderHelper.getActualEntityMasterDTOCount(entityMasterList));
						model.put("entityOwnerCount",UploaderHelper.getActualEntityOwnerDTOCount(entityOwnersList));
						model.put("flowTypesCount",UploaderHelper.getActualFlowTypesDTOCount(flowTypesList));
						model.put("taskRepositoryCount",UploaderHelper.getActualTaskRepositoriesDTOCount(taskRepositoriesList));
						model.put("taskMasterCount",UploaderHelper.getActualTaskMasterDTOCount(taskMasterList));
						model.put("taskEntityDetailCount",UploaderHelper.getActualTaskEntityDetailDTOCount(taskEntityDetailList));
							
						}
					}
					
				}catch (Throwable e) {
					model.put("success", false); 
					logger.error("Error occured while Uploading file", e);
				}
		}else{
			model.put("success", false);
			model.put("isFileFormatCorrect", false);
		}
	}else{
		model.put("succes",false);
	}
	
		return model.toString();	
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/handleMetadataValidation.htm", method = RequestMethod.POST)
	public String handleMetadataValidation(HttpServletRequest request, HttpServletResponse response){
		logger.info("EXEFLOW - DldController ->  handleMetadataValidation()");
		String effectiveDate = null;
		String userTokenSession = null;
		String fileName=null;
		JSONObject model=new JSONObject();
		JSONArray allDTOValStatus=new JSONArray();
        Map<String,Object> uploaderMap = new HashMap<String, Object>();
		List<EntityMasterUploaderDTO> enitityMasterUploaderlist;
		List<EntityOwnerUploaderDTO> entityownerUploadList;
		List<FlowTypesUploaderDTO> flowTypesList;
		List<TaskMasterUploaderDTO> taskMasterList;
		List<TaskRepositoriesUploaderDTO> taskRepositoriesList;
		List<TaskEntityDetailUploaderDTO> taskEntityDetailList;
		List<Object> objectList = new ArrayList<Object>();
		List<ErrorLogForUploader> masterErrorMsgList = new ArrayList<ErrorLogForUploader>();
		Boolean hasWarning=false;
		Boolean hasError=false;
		try{
			
			effectiveDate = request.getParameter("effectiveDate");
			fileName=request.getParameter("fileName");
			userTokenSession=request.getParameter("userSessionToken");
			uploaderMap=dldBo.getUploadedFileFromCache(userTokenSession);
			enitityMasterUploaderlist = (List<EntityMasterUploaderDTO>) uploaderMap.get("entityMasterList");
			entityownerUploadList = (List<EntityOwnerUploaderDTO>) uploaderMap.get("entityOwnersList");
			flowTypesList = (List<FlowTypesUploaderDTO>) uploaderMap.get("flowTypesList");
			taskMasterList=(List<TaskMasterUploaderDTO>) uploaderMap.get("taskMasterList");
			taskRepositoriesList=(List<TaskRepositoriesUploaderDTO>) uploaderMap.get("taskRepositoriesList");
			taskEntityDetailList=(List<TaskEntityDetailUploaderDTO>) uploaderMap.get("taskEntityDetailList");
			
			String clientCode=(String) request.getSession().getAttribute("clientCode");
			objectList=dldBo.validateMetaData(clientCode,enitityMasterUploaderlist,entityownerUploadList,flowTypesList,taskMasterList,taskRepositoriesList,taskEntityDetailList,effectiveDate);
			if(objectList!=null && objectList.size()>0){
				
				masterErrorMsgList=(List<ErrorLogForUploader>) objectList.get(0);
				allDTOValStatus=(JSONArray) objectList.get(1);
			}
			
			if(masterErrorMsgList.size()>0){
				
				String uploaderPathTemp = request.getSession().getServletContext().getRealPath(UPLOADER_TEMP_FOLDER) + File.separator + request.getSession().getId() + File.separator;
				Workbook workbook = new Workbook(uploaderPathTemp+fileName);
				dldBo.generateValidationReport(workbook, masterErrorMsgList);
				workbook.save(uploaderPathTemp+fileName);
				HtmlSaveOptions options = new HtmlSaveOptions();
				options.setExportHiddenWorksheet(false);
				workbook.save(uploaderPathTemp+fileName+"_validation.html",options);
				for(ErrorLogForUploader errorLogForUploader:masterErrorMsgList){
					if(errorLogForUploader.getWarningMsg()!=null && errorLogForUploader.getWarningMsg()!="")
					{
						hasWarning=true;
					}
					else
						hasError=true;
				}
				model.put("hasError", hasError);
			    model.put("hasWarning", hasWarning);
			    model.put("valStatus", allDTOValStatus);
				model.put("uploadedFilePath",UPLOADER_TEMP_FOLDER+"/"+request.getSession().getId()+File.separator+fileName+"_validation.html");
			    
			}else{
				model.put("hasError", false);
				model.put("hasWarning", false);
				model.put("valStatus", allDTOValStatus);
				model.put("uploadedFilePath",UPLOADER_TEMP_FOLDER+"/"+request.getSession().getId()+File.separator+fileName+".html");
			}
			model.put("success", true);
		}catch (Throwable e){ 
			model.put("success", false);
			logger.error("Error occured while Validating Metadata", e);
		}
		return model.toString();
	}

	
	@RequestMapping(value = "/getstagingdetailsforsourcesystems", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getStagingDetailsForSourceSystems(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getStagingDetailsForSourceSystems");
		JSONObject progressDetails = new JSONObject();
		try
		{
			progressDetails=dldBo.getStagingDetailsForSourceSystems(currrentBusinessDate
					,isFlowFilterApplied,flowFilterCSV,isFrequencyFilterApplied,frequencyFilterCSV,(String)request.getSession().getAttribute("clientCode"));
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getstagingdetailsfordatarepository", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getStagingDetailsForDataRepository(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getStagingDetailsForDataRepository");
		JSONObject progressDetails = new JSONObject();
		try
		{
			progressDetails=dldBo.getStagingDetailsForDataRepository(currrentBusinessDate
					,isFlowFilterApplied,flowFilterCSV,isFrequencyFilterApplied,frequencyFilterCSV,(String)request.getSession().getAttribute("clientCode"));
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@RequestMapping(value = "/handleDownloadValidationReport.htm", method = RequestMethod.GET)
	public ModelAndView handleDownloadValidationReport(HttpServletRequest request, HttpServletResponse response){
		logger.info("EXEFLOW - DldController ->  handleDownloadValidationReport()");
		String fileName=request.getParameter("fileName");
		response.reset();
		String uploaderPathTemp = request.getSession().getServletContext().getRealPath(UPLOADER_TEMP_FOLDER) + File.separator + request.getSession().getId() + File.separator;
		try{
			File file=new File(uploaderPathTemp+fileName);
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");   
		    response.setHeader("Content-Disposition", "download;filename=" + fileName);
	        response.setContentLength((int) file.length());
			InputStream fileInputStream = new FileInputStream(file);
            ServletOutputStream outputStream = response.getOutputStream();
            byte [] byteArray = IOUtils.toByteArray(fileInputStream);
            outputStream.write(byteArray);
            outputStream.flush();
            fileInputStream.close();
            outputStream.close();
		}catch(Exception e){
			logger.error("Error occured while Downloading", e);
		}
		return null;

	}
	@RequestMapping(value = "/getsdetailsfordataconsumers", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONArray> getDetailsForDataConsumers(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getDetailsForDataConsumers");
		JSONArray progressDetails = new JSONArray();
		try
		{
			progressDetails=dldBo.getDetailsForDataConsumers(currrentBusinessDate,isFrequencyFilterApplied,frequencyFilterCSV
					,isFlowFilterApplied,flowFilterCSV,(String)request.getSession().getAttribute("clientCode"));
			return new ResponseEntity<JSONArray>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONArray>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/handleSaveMetadata.htm", method = RequestMethod.POST)
	public String handleSaveMetadata(HttpServletRequest request, HttpServletResponse response) {
		logger.info("EXEFLOW - DldController ->  handleSaveMetadata()");
		String effectiveDate = null;
		String userTokenSession = null;
		JSONObject model=new JSONObject();
		boolean status=false;
		try{
				String clientCode=(String) request.getSession().getAttribute("clientCode");
				effectiveDate = request.getParameter("effectiveDate");
				userTokenSession = request.getParameter("userSessionToken");
				status = dldBo.saveDataForUploader(clientCode,effectiveDate, userTokenSession);
				model.put("success",status );
				
			} 
			catch(Throwable e){
			model.put("success", status);
			logger.error("Error occured while Saving", e);
		}  
		return model.toString();
	}
	
	
	@RequestMapping(value = "/handleMetadataDownload.htm", method = RequestMethod.GET)
	public String handleMetadataDownload(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		logger.info("EXEFLOW - DldController ->  handleMetadataDownload()");
		String effectiveDate = request.getParameter("effectiveDate");
		response.reset();
		Workbook workbook =null;
		Map<String, Object> parameter=new HashMap<String, Object>();
		String clientCode=(String) request.getSession().getAttribute("clientCode");
		try{
			
			List<String> taskTypeList=dldBo.getAllTaskType(clientCode);
			List<String> entitytype=dldBo.getAllentityType(clientCode);
			parameter.put("entityType", entitytype);
			parameter.put("taskType", taskTypeList);
			workbook = UploaderHelper.getTemplateDownload(parameter,false);
			workbook=dldBo.getDataToDownload(clientCode,effectiveDate, workbook);
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");   
		    response.setHeader("Content-Disposition", "download;filename=Dataload_Uploader.xlsx");
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.save(outputStream, FileFormatType.XLSX);
            outputStream.flush();
		}catch(Exception e){
			logger.error("Error occured while downloading", e);
		}
		return null;
	}
	@RequestMapping(value = "/getdetailsforsourcesystemgrid", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getDetailsForSourceSystemGrid(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV
			,@RequestParam(value="tabIndicator") String tabIndicator,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getDetailsForSourceSystemGrid");
		JSONObject progressDetails = new JSONObject();
		try
		{
			progressDetails=dldBo.getSourceSystemsStagingSummmary(currrentBusinessDate,
					isFlowFilterApplied, flowFilterCSV, isFrequencyFilterApplied, 
					frequencyFilterCSV, tabIndicator,(String)request.getSession().getAttribute("clientCode"));;
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getdetailsfordatarepositorygrid", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<JSONObject> getDetailsForDataRepositoryGrid(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV
			,@RequestParam(value="tabIndicator") String tabIndicator,HttpServletRequest request) {
		logger.info("EXEFLOW->DldController->getDetailsForDataRepositoryGrid");
		JSONObject progressDetails = new JSONObject();
		try
		{
			progressDetails=dldBo.getStagingSummaryForDataRepositoryGrid(currrentBusinessDate,
					isFlowFilterApplied, flowFilterCSV, isFrequencyFilterApplied, 
					frequencyFilterCSV, tabIndicator,(String)request.getSession().getAttribute("clientCode"));
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@RequestMapping(value = "/getunplannedtaskdetails", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getUnplannedTaskDetails(@RequestParam(value="businessDate") String currrentBusinessDate,HttpServletRequest request) {
          logger.info("EXEFLOW->DldController->getUnplannedTaskDetails");
          JSONObject progressDetails = new JSONObject();
          try
          {
          progressDetails=dldBo.getUnplannedTaskDetails(currrentBusinessDate,(String)request.getSession().getAttribute("clientCode"));
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
          }
          catch(Throwable t)
          {
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
          }
    }
    
    @RequestMapping(value = "/getsubgridtaskdetails", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getSubGridTaskDetails(@RequestParam(value="businessDate") String currrentBusinessDate
                ,@RequestParam(value="taskName") String taskName
                ,@RequestParam(value="repoName") String repoName,HttpServletRequest request) {
          logger.info("EXEFLOW->DldController->getSubGridTaskDetails");
          JSONObject progressDetails = new JSONObject();
          try
          {
          progressDetails=dldBo.getSubGridTaskDetails(currrentBusinessDate, taskName, repoName,(String)request.getSession().getAttribute("clientCode"));
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
          }
          catch(Throwable t)
          {
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
          }
    }

    
    @RequestMapping(value = "/getdataconsumersummary", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getDataConsumerSummary(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,HttpServletRequest request
			) {
          logger.info("EXEFLOW->DldController->getDataConsumerSummary");
          JSONObject progressDetails = new JSONObject();
          try
          {
          progressDetails=dldBo.getDetailsForDataConsumersSummary(currrentBusinessDate,
        		  isFrequencyFilterApplied, frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV,(String)request.getSession().getAttribute("clientCode"),"All");
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
          }
          catch(Throwable t)
          {
        	  logger.error("Error occured", t);
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
          }
    }

    
    
    @RequestMapping(value = "/getlineitemsummary", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getLineItemSummary(@RequestParam(value="businessDate") String currrentBusinessDate
			,@RequestParam(value="isFlowFilterApplied") String isFlowFilterApplied
			,@RequestParam(value="flowFilterCSV") String flowFilterCSV
			,@RequestParam(value="isFrequencyFilterApplied") String isFrequencyFilterApplied
			,@RequestParam(value="frequencyFilterCSV") String frequencyFilterCSV,
			@RequestParam(value="reportId") Integer reportId,
			@RequestParam(value="pageNo") Integer pageNo,
			@RequestParam(value="pageSize") Integer pageSize,
			@RequestParam(value="lineItemIdSearch") String lineItemIdSearch,
			@RequestParam(value="lineItemDescSearch") String lineItemDescSearch,
			@RequestParam(value="solutionName") String solutionName,
			@RequestParam(value="solutionId") String solutionId,HttpServletRequest request
			) {
          logger.info("EXEFLOW->DldController->getLineItemSummary");
          JSONObject progressDetails = new JSONObject();
          try
          {
          progressDetails=dldBo.getDetailsForDataConsumersSummaryLineItemGrid(currrentBusinessDate, isFrequencyFilterApplied, frequencyFilterCSV, isFlowFilterApplied, flowFilterCSV, reportId, pageNo, pageSize, lineItemIdSearch, lineItemDescSearch,solutionName,Integer.parseInt(solutionId),(String)request.getSession().getAttribute("clientCode"));
        		  
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.OK);
          }
          catch(Throwable t)
          {
                return new ResponseEntity<JSONObject>(progressDetails, HttpStatus.INTERNAL_SERVER_ERROR);
          }
    }
    
    @RequestMapping(value = "/lineage", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView dataLineageLanding(@RequestParam(value="cbd") String cbd,
			HttpServletRequest request
			) {
          logger.info("EXEFLOW->DldController->dataLineageLanding");
          Map<String, Object> model = new HashMap<String, Object>();
          model.put("cbd", cbd);
          model.put("clientCode", (String)request.getSession().getAttribute("clientCode"));
  			return new ModelAndView("dataLineage", "model", model);
          
          
    }
    
    @RequestMapping(value = "/getDataForlineage", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getDataForLinage(@RequestParam(value="cbd") String cbd,
			@RequestParam(value="clientCode") String clientCode,HttpServletRequest request
			) {
		logger.info("EXEFLOW->DldController->getDataForLinage");
		JSONObject dataLineage = new JSONObject();
		try
		{
			

			dataLineage=dldBo.getDataForLineage(clientCode,cbd);
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage());
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/login", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView login() {
          logger.info("EXEFLOW->DldController->login");
          Map<String, Object> model = new HashMap<String, Object>();
          SolutionURLMappingPropertiesLoader x = SolutionURLMappingPropertiesLoader.getInstance();
          JSONArray solJson=new JSONArray();
  		for(String sol:	x.getSolutions().keySet())
  		{
  			solJson.add(sol);
          
  		} 
  		model.put("solutions", solJson);
  		return new ModelAndView("login", "model", model);
          
          
    }
    @RequestMapping(value = "/handleLineageDownload", method = RequestMethod.GET)
	public String handleLineageDownload(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		logger.info("EXEFLOW - DldController ->  handleLineageDownload()");
		String effectiveDate = request.getParameter("effectiveDate");
		response.reset();
		Workbook workbook =null;
		List<DldStatusDownload> listOfDldStatus=null;
		String clientCode=(String) request.getSession().getAttribute("clientCode");
		try{
			listOfDldStatus=new ArrayList<DldStatusDownload>();
			workbook = UploaderHelper.getTemplateForLineage();
			
			listOfDldStatus=dldBo.getDownloadDetailsForSheetOne(effectiveDate, clientCode, "ALL");
			workbook=dldBo.getLineageWorkBoook(listOfDldStatus,workbook);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");   
		    response.setHeader("Content-Disposition", "download;filename=Lineage_Downloader.xlsx");
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.save(outputStream, FileFormatType.XLSX);
            outputStream.flush();
		}catch(Exception e){
			logger.error("Error occured while downloading", e);
		}
		return null;
	}
    @RequestMapping(value = "/getDataForlineageForLineItem", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getDataForLinageForLineItems(@RequestParam(value="cbd") String cbd,
    		@RequestParam(value="reportId") String reportId,@RequestParam(value="solutionName") String solutionName,
			@RequestParam(value="clientCode") String clientCode,HttpServletRequest request
			) {
		logger.info("EXEFLOW->DldController->getDataForLinage");
		JSONObject dataLineage = new JSONObject();
		try
		{
			

			dataLineage=dldBo.getLineageForLineItems(cbd, clientCode,Integer.parseInt(reportId), 0, 0, solutionName);
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.OK);
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage());
			return new ResponseEntity<JSONObject>(dataLineage, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}

    
}
