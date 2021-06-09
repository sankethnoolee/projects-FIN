package com.fintellix.dld.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellArea;
import com.aspose.cells.Cells;
import com.aspose.cells.Color;
import com.aspose.cells.Range;
import com.aspose.cells.Style;
import com.aspose.cells.StyleFlag;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Validation;
import com.aspose.cells.ValidationAlertType;
import com.aspose.cells.ValidationCollection;
import com.aspose.cells.ValidationType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.fintellix.dld.models.ClientUploaderDTO;
import com.fintellix.dld.models.EntityMasterUploaderDTO;
import com.fintellix.dld.models.EntityOwnerUploaderDTO;
import com.fintellix.dld.models.ErrorLogForUploader;
import com.fintellix.dld.models.FlowTypesUploaderDTO;
import com.fintellix.dld.models.TaskEntityDetailUploaderDTO;
import com.fintellix.dld.models.TaskFrequencyExclusionOffset;
import com.fintellix.dld.models.TaskFrequencyOffset;
import com.fintellix.dld.models.TaskMasterUploaderDTO;
import com.fintellix.dld.models.TaskRepositoriesUploaderDTO;
import com.fintellix.dld.services.DldController;

//import com.fintellix.platformcore.utils.ApplicationProperties;

public class UploaderHelper {


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


	static String colorCode=applicationProperties.getProperty("dld.colorCode").trim();
	//Getting Sheet Name

	static String  client=applicationProperties.getProperty("dld.clientSheetName").trim();
	static String entityOwner=applicationProperties.getProperty("dld.entityOwnerSheetName").trim();
	static String entityMaster=applicationProperties.getProperty("dld.entityMasterSheetName").trim();
	static String flowTypes=applicationProperties.getProperty("dld.flowTypesSheetName").trim();
	static String taskRepository=applicationProperties.getProperty("dld.taskrepositorySheetName").trim();
	static String taskMaster=applicationProperties.getProperty("dld.taskMasterSheetName").trim();
	static String taskEntityDetails=applicationProperties.getProperty("dld.taskEntityDetailsSheetName").trim();

	//Client Sheet Headers
	static String clientName=applicationProperties.getProperty("dld.clientNameHeader").trim();
	static String clientCode=applicationProperties.getProperty("dld.clientCodeHeader").trim();

	//Entity Owner Headers
	static String ownerName=applicationProperties.getProperty("dld.OwnerNameHeader").trim();
	static String entityOwnerDesc=applicationProperties.getProperty("dld.entityOwnerDescHeader").trim();
	static String externalSource=applicationProperties.getProperty("dld.externalSourceHeader").trim();
	static String dataSourceName=applicationProperties.getProperty("dld.dataSourceNameHeader").trim();
	static String solutionName=applicationProperties.getProperty("dld.solutionNameHeader").trim();
	static String entityownerDesc=applicationProperties.getProperty("dld.entityOwnerDescHeader").trim();
	static String contactDetails=applicationProperties.getProperty("dld.contactDetailsHeader").trim();
	static String displayOrder=applicationProperties.getProperty("dld.displayOrderHeader").trim();



	//Entity Master Headers
	static String entityName=applicationProperties.getProperty("dld.entityNameheader").trim();
	static String entityMasterOwnerName=applicationProperties.getProperty("dld.entityMasterOwnerNameHeader").trim();
	static String entityType=applicationProperties.getProperty("dld.entityTypeHeader").trim();
	static String entityDetail=applicationProperties.getProperty("dld.entityDetailHeader").trim();
	static String entityDesc=applicationProperties.getProperty("dld.entityDescHeader").trim();

	//Flow Types Headers
	static String flowType=applicationProperties.getProperty("dld.flowTypeHeader").trim();
	static String flowtypeDesc=applicationProperties.getProperty("dld.flowtypeDescHeader").trim();

	//Task Repositories Types Headers
	static String taskRepositoryName=applicationProperties.getProperty("dld.taskRepositoryNameHeader").trim();
	static String taskRepositoryDesc=applicationProperties.getProperty("dld.taskRepositoryDescHeader").trim();

	//Task Master Headers		

	static String taskMasterRepositoryName=applicationProperties.getProperty("dld.taskMasterRepositoryNameHeader").trim();
	static String taskName=applicationProperties.getProperty("dld.taskNameHeader").trim();
	static String taskType=applicationProperties.getProperty("dld.taskTypeHeader").trim();
	static String taskDesc=applicationProperties.getProperty("dld.taskDescHeader").trim();
	static String taskTechnicalName=applicationProperties.getProperty("dld.taskTechnicalNameHeader").trim();
	static String SubtaskTechnicalName=applicationProperties.getProperty("dld.SubtaskTechnicalNameHeader").trim();
	static String taskFlows=applicationProperties.getProperty("dldtaskFlowsHeader").trim();
	static String taskFrequencyOffsetHeader=applicationProperties.getProperty("dld.taskFrequencyOffsetHeader").trim();
	static String taskFrequencyExclusionOffsetHeader=applicationProperties.getProperty("dld.taskFrequencyExclusionOffsetHeader").trim();

	static String taskSourceOwnerName=applicationProperties.getProperty("dld.taskSourceOwnerNameHeader").trim();
	static String taskSourceEntityName=applicationProperties.getProperty("dld.taskSourceEntityNameHeader").trim();
	static String taskTargetOwnerName=applicationProperties.getProperty("dld.taskTargetOwnerNameHeader").trim();
	static String taskTargetEntityName=applicationProperties.getProperty("dld.taskTargetEntityNameHeader").trim();


	static String taskStatus=applicationProperties.getProperty("dld.taskStatusHeader").trim();
	static String sourceEntity=applicationProperties.getProperty("dld.sourceEntityHeader").trim();
	static String targetEntity=applicationProperties.getProperty("dld.targetEntityHeader").trim();
	static String taskIsValidationRequired=applicationProperties.getProperty("dld.taskIsValidationRequired").trim();

	//Task Entity Details Headers
	static String entityDetailTaskRepository=applicationProperties.getProperty("dld.entityDetailTaskRepository").trim();
	static String entityDetailTaskName=applicationProperties.getProperty("dld.entityDetailTaskName").trim();
	static String entityOwnerName=applicationProperties.getProperty("dld.entityOwnerName").trim();
	static String taskEntityName=applicationProperties.getProperty("dld.taskEntityName").trim();
	static String linkType=applicationProperties.getProperty("dld.linkType").trim();


	private static final Logger logger = LoggerFactory.getLogger(DldController.class);


	// Standard set of braces.
	private static final String openBraces = "(";
	// Matching close set.
	private static final String closeBraces = ")";
	public static boolean getFileExtension(String fileName) {
		String extension ="";
		try {
			extension= fileName.substring(fileName.lastIndexOf(".")+1);

		} catch (Exception e) {
			logger.error("Error occured while gettiing File Extension", e);
		}
		if(extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")){
			return true;
		}else{
			return false;
		}

	}


	//Check
	public static Map<String, Boolean> getFileFormatValidation(Workbook workBook){
		//boolean statusForClient=false;
		boolean statusForEntityOwner=false;
		boolean statusForEntityMaster=false;
		boolean statusForFlowTypes=false;
		boolean statusForTaskRepositories=false;
		boolean statusForTaskMaster=false;
		boolean statusForTaskEntityDetails=false;


		Map<String, Boolean> fileFormatErrorMap= new HashMap<String, Boolean>();
		List<String> entityOwnerHeader=new ArrayList<String>();
		List<String> entityMasterHeader=new ArrayList<String>();
		List<String> flowTypesHeader=new ArrayList<String>();
		List<String> taskRepositoryHeader=new ArrayList<String>();
		List<String> taskMasterHeader=new ArrayList<String>();
		List<String> taskEntityDetailsHeader=new ArrayList<String>();

		//Entity Owner Sheet header to List
		entityOwnerHeader.add(ownerName);
		entityOwnerHeader.add(entityOwnerDesc);
		entityOwnerHeader.add(externalSource);
		entityOwnerHeader.add(dataSourceName);
		entityOwnerHeader.add(solutionName); 
		entityOwnerHeader.add(contactDetails);
		entityOwnerHeader.add(displayOrder);

		//Entity Master Sheet header to List
		entityMasterHeader.add(entityName);
		entityMasterHeader.add(entityMasterOwnerName);
		entityMasterHeader.add(entityType);
		entityMasterHeader.add(entityDetail);
		entityMasterHeader.add(entityDesc);

		//Flow Types Sheet header to List
		flowTypesHeader.add(flowType);
		flowTypesHeader.add(flowtypeDesc);

		//Task Repositories Types Sheet header to List
		taskRepositoryHeader.add(taskRepositoryName);
		taskRepositoryHeader.add(taskRepositoryDesc);

		//Task Master Sheet header to List
		taskMasterHeader.add(taskMasterRepositoryName);
		taskMasterHeader.add(taskName);
		taskMasterHeader.add(taskType);
		taskMasterHeader.add(taskDesc);
		taskMasterHeader.add(taskTechnicalName);
		taskMasterHeader.add(SubtaskTechnicalName);
		taskMasterHeader.add(taskFlows);
		taskMasterHeader.add(taskFrequencyOffsetHeader);
		taskMasterHeader.add(taskFrequencyExclusionOffsetHeader);
		taskMasterHeader.add(taskStatus);
		taskMasterHeader.add(taskIsValidationRequired);

		//Task Entity Details Sheet header to List
		taskEntityDetailsHeader.add(entityDetailTaskRepository);
		taskEntityDetailsHeader.add(entityDetailTaskName);
		taskEntityDetailsHeader.add(entityOwnerName);
		taskEntityDetailsHeader.add(taskEntityName);
		taskEntityDetailsHeader.add(linkType);


		//Worksheet worksheetForClient = workBook.getWorksheets().get(client);
		Worksheet worksheetForEntityOwner = workBook.getWorksheets().get(entityOwner);
		Worksheet worksheetForEntityMaster = workBook.getWorksheets().get(entityMaster);
		Worksheet worksheetForFlowTypes = workBook.getWorksheets().get(flowTypes);
		Worksheet worksheetForTaskRepositories = workBook.getWorksheets().get(taskRepository);
		Worksheet worksheetForTaskMaster = workBook.getWorksheets().get(taskMaster);
		Worksheet worksheetForTaskEntityDetails = workBook.getWorksheets().get(taskEntityDetails);


		//List<String> clientHeaderFromSheet=getHeaderFromSheet(worksheetForClient);
		List<String> entityOwnerHeaderFromSheet=getHeaderFromSheet(worksheetForEntityOwner);
		List<String> entityMasterHeaderFromSheet=getHeaderFromSheet(worksheetForEntityMaster);
		List<String> flowTypesFromSheet=getHeaderFromSheet(worksheetForFlowTypes);
		List<String> taskRepositoriesHeaderFromSheet=getHeaderFromSheet(worksheetForTaskRepositories);
		List<String> taskMasterHeaderFromSheet=getHeaderFromSheetForTaskMaster(worksheetForTaskMaster);
		List<String> taskEntityDetailsHeaderFromSheet=getHeaderFromSheet(worksheetForTaskEntityDetails);


		//statusForClient=equalHeaderLists(clientHeader,clientHeaderFromSheet);
		statusForEntityOwner=equalHeaderLists(entityOwnerHeader,entityOwnerHeaderFromSheet);
		statusForEntityMaster=equalHeaderLists(entityMasterHeader,entityMasterHeaderFromSheet);
		statusForFlowTypes=equalHeaderLists(flowTypesHeader,flowTypesFromSheet);
		statusForTaskRepositories=equalHeaderLists(taskRepositoryHeader,taskRepositoriesHeaderFromSheet);
		statusForTaskMaster=equalHeaderLists(taskMasterHeader,taskMasterHeaderFromSheet);
		statusForTaskEntityDetails=equalHeaderLists(taskEntityDetailsHeader,taskEntityDetailsHeaderFromSheet);


		if(statusForEntityOwner && statusForEntityMaster && 	statusForFlowTypes && statusForTaskRepositories && statusForTaskMaster && statusForTaskEntityDetails){
			fileFormatErrorMap.put("isHeaderCorrect",true);
		}else{
			fileFormatErrorMap.put("isHeaderCorrect",false);
			//	fileFormatErrorMap.put("client",statusForClient);
			fileFormatErrorMap.put("entityOwner",statusForEntityOwner);
			fileFormatErrorMap.put("entityMaster",statusForEntityMaster);
			fileFormatErrorMap.put("flowTypes",statusForFlowTypes);
			fileFormatErrorMap.put("taskRepositories",statusForTaskRepositories);
			fileFormatErrorMap.put("taskMaster",statusForTaskMaster);
			fileFormatErrorMap.put("taskMaster",statusForTaskEntityDetails);
		}
		return fileFormatErrorMap;
	}


	public  static List<String> getHeaderFromSheet(Worksheet workSheet){
		List<String> headerFromSheet=new ArrayList<String>();
		Cells cells = workSheet.getCells();
		cells.deleteBlankColumns();
		int colCount = workSheet.getCells().getMaxColumn();
		for (int i = 0; i <= colCount; i++) {
			headerFromSheet.add(cells.get(0, i).getValue().toString().trim().toLowerCase());
		}
		return headerFromSheet;
	}

	public  static List<String> getHeaderFromSheetForTaskMaster(Worksheet workSheet){
		List<String> headerFromSheet=new ArrayList<String>();
		Cells cells = workSheet.getCells();
		cells.deleteBlankColumns();
		int colCount = workSheet.getCells().getMaxColumn();
		for (int i = 0; i <= colCount; i++) {	
			headerFromSheet.add(cells.get(0, i).getValue().toString().trim().toLowerCase());
		}
		return headerFromSheet;
	}

	public  static boolean equalHeaderLists(List<String> templateHeaderList, List<String> sheetHeaderList){ 
		//converting to lower case
		for(int i=0; i < templateHeaderList.size(); i++) {
			templateHeaderList.set(i, templateHeaderList.get(i).toLowerCase());
		}
		if( templateHeaderList.size() != sheetHeaderList.size()){
			return false;
		}else{
			Collections.sort(templateHeaderList);
			Collections.sort(sheetHeaderList);      
			return templateHeaderList.equals(sheetHeaderList);
		}
	}




	public static List<EntityOwnerUploaderDTO> setDTOForEntityOwner(Worksheet worksheet){
		List<EntityOwnerUploaderDTO> entityOwnerList=new ArrayList<EntityOwnerUploaderDTO>();
		EntityOwnerUploaderDTO entity=null;
		Cells cells = worksheet.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		int colCount = worksheet.getCells().getMaxColumn();
		int rowCount = worksheet.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (int i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}

		for (int i = 1; i <= rowCount; i++) {
			entity=new EntityOwnerUploaderDTO();
			for (int j = 0; j <= colCount; j++) {
				if(colOrder.get(ownerName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setOwner_Name(cells.get(i, j).getValue().toString().trim());

					}
				}
				else if(colOrder.get(entityOwnerDesc.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setDescription(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(externalSource.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setExternal_Source(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(dataSourceName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setData_source_Name(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(solutionName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setSolution_Name(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(entityownerDesc.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setContact_Details(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(contactDetails.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setContact_Details(cells.get(i, j).getValue().toString().trim());
					}
				}

				else if(colOrder.get(displayOrder.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setDisplay_Sorting_Order(cells.get(i, j).getValue().toString().trim());
					}
				}
			}

			entityOwnerList.add(entity);
		}

		return entityOwnerList;
	}

	public static List<EntityMasterUploaderDTO> setDTOForEntityMaster(Worksheet worksheet){
		List<EntityMasterUploaderDTO> entityMasterList=new ArrayList<EntityMasterUploaderDTO>();
		EntityMasterUploaderDTO entity=null;


		Cells cells = worksheet.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		int colCount = worksheet.getCells().getMaxColumn();
		int rowCount = worksheet.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (int i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}


		for (int i = 1; i <= rowCount; i++) {
			entity=new EntityMasterUploaderDTO();
			for (int j = 0; j <= colCount; j++) {
				if(colOrder.get(entityName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setEntity_Name(cells.get(i, j).getValue().toString().trim());

					}
				}
				else if(colOrder.get(entityMasterOwnerName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setOwner_Name(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(entityType.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setEntity_Type(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(entityDetail.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setEntity_Detail(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(entityDesc.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entity.setDescription(cells.get(i, j).getValue().toString().trim());
					}
				}

			}

			entityMasterList.add(entity);
		}

		return entityMasterList;
	}

	public static List<FlowTypesUploaderDTO> setDTOForFlowTypes(Worksheet worksheet){
		FlowTypesUploaderDTO flow=null;
		List<FlowTypesUploaderDTO> flowTypesList=new ArrayList<FlowTypesUploaderDTO>();
		Cells cells = worksheet.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		int colCount = worksheet.getCells().getMaxColumn();
		int rowCount = worksheet.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (int i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}


		for (int i = 1; i <= rowCount; i++) {

			flow=new FlowTypesUploaderDTO();
			for (int j = 0; j <= colCount; j++) {
				if(colOrder.get(flowType.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						flow.setFlow_type(cells.get(i, j).getValue().toString().trim());

					}
				}
				else if(colOrder.get(flowtypeDesc.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						flow.setDescription(cells.get(i, j).getValue().toString().trim());
					}
				}

			}

			flowTypesList.add(flow);
		}
		return flowTypesList;

	}

	public static List<TaskRepositoriesUploaderDTO> setDTOForTaskRepositories(Worksheet worksheet){
		List<TaskRepositoriesUploaderDTO> taskRepositoriesList=new ArrayList<TaskRepositoriesUploaderDTO>();
		TaskRepositoriesUploaderDTO taskRepositories=null;
		Cells cells = worksheet.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		int colCount = worksheet.getCells().getMaxColumn();
		int rowCount = worksheet.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		for (int i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}



		for (int i = 1; i <= rowCount; i++) {

			taskRepositories=new TaskRepositoriesUploaderDTO();
			for (int j = 0; j <= colCount; j++) {
				if(colOrder.get(taskRepositoryName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskRepositories.setName(cells.get(i, j).getValue().toString().trim());

					}
				}
				else if(colOrder.get(taskRepositoryDesc.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskRepositories.setDescription(cells.get(i, j).getValue().toString().trim());
					}
				}

			}
			taskRepositoriesList.add(taskRepositories);
		}
		return taskRepositoriesList;
	}


	public static List<TaskMasterUploaderDTO> setDTOForTaskMaster(Worksheet worksheet){
		List<TaskMasterUploaderDTO> taskMasterList=new ArrayList<TaskMasterUploaderDTO>();
		TaskMasterUploaderDTO taskMaster=null;
		Cells cells = worksheet.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		int colCount = worksheet.getCells().getMaxColumn();
		int rowCount = worksheet.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();

		for(int i=0;i<=colCount;i++){
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);		
		}

		for (int i = 1; i <= rowCount; i++) {
			taskMaster=new TaskMasterUploaderDTO();
			List<String> frequencyList=null;
			List<String> frequencyExclusionsList=null;
			String	frequency=null;
			String frequencyExclusion=null;
			List<TaskFrequencyOffset> taskFrequencyOffsetList=new ArrayList<TaskFrequencyOffset>();
			List<TaskFrequencyExclusionOffset> taskFrequencyOffsetExclusionList=new ArrayList<TaskFrequencyExclusionOffset>();
			TaskFrequencyOffset taskFrequencyOffset;
			TaskFrequencyExclusionOffset taskFrequencyExclusionOffset;
			Integer offset=null; 
			Integer offsetExclusion=null;
			for (int j = 0; j <= colCount; j++) {
				if(colOrder.get(taskMasterRepositoryName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setTask_Repository(cells.get(i, j).getValue().toString().trim());

					}
				}
				else if(colOrder.get(taskName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setTask_Name(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(taskType.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setTask_Type(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(taskDesc.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setDescription(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(taskTechnicalName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setTask_Technical_Name(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(SubtaskTechnicalName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setSub_Task_Technical_Name(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(taskFlows.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setTask_Flows(Arrays.asList(cells.get(i, j).getValue().toString().trim().split("\\s*,\\s*")));


					}
				}
				else if(colOrder.get(taskFrequencyOffsetHeader.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						frequencyList=split(cells.get(i, j).getValue().toString().trim());
						if(frequencyList.size()>0){
							for(int k=0;k<frequencyList.size();k++){
								taskFrequencyOffset=new TaskFrequencyOffset();
								String fr=frequencyList.get(k).trim();
								frequency=fr.split(",")[0].replace("(", "").trim();
								if(fr.split(",")[1].replace(")", "").trim()!=null)
									offset= Integer.parseInt(fr.split(",")[1].replace(")", "").trim());
								else
									offset=0;
								taskFrequencyOffset.setFrequency(frequency);
								taskFrequencyOffset.setOffset(offset);
								taskFrequencyOffsetList.add(taskFrequencyOffset);
							}
						}
						taskMaster.setTask_Frequency_Offset(taskFrequencyOffsetList);
					}
				}
				else if(colOrder.get(taskFrequencyExclusionOffsetHeader.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						frequencyExclusionsList=split(cells.get(i, j).getValue().toString().trim());

						if(frequencyExclusionsList.size()>0){
							for(int k=0;k<frequencyExclusionsList.size();k++){
								taskFrequencyExclusionOffset=new TaskFrequencyExclusionOffset();
								String fr=frequencyExclusionsList.get(k).trim();
								frequencyExclusion=fr.split(",")[0].replace("(", "").trim();
								if(fr.split(",")[1].replace(")", "").trim()!=null)
									offsetExclusion= Integer.parseInt(fr.split(",")[1].replace(")", "").trim());
								else
									offsetExclusion=0;
								taskFrequencyExclusionOffset.setFrequency(frequencyExclusion);
								taskFrequencyExclusionOffset.setOffset(offsetExclusion);
								taskFrequencyOffsetExclusionList.add(taskFrequencyExclusionOffset);
							}
						}
						taskMaster.setTask_Frequency_Exclusions_Offset(taskFrequencyOffsetExclusionList);
					}
				}
				else if(colOrder.get(taskStatus.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setStatus(cells.get(i, j).getValue().toString().trim());
					}
				}

				else if(colOrder.get(taskIsValidationRequired.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskMaster.setIsValidationRequired(cells.get(i, j).getValue().toString().trim());
					}
				}
			}



			taskMasterList.add(taskMaster);

		}
		return taskMasterList;
	}

	public static boolean balancedParenthensies(String s) {
		Stack<Character> stack = new Stack<Character>();
		Stack<Integer> stackForPosition = new Stack<Integer>();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '[' || c == '(' || c == '{') {
				stack.push(c);
				stackForPosition.add(i);

			} else if (c == ']') {
				if (stack.isEmpty())
					return false;
				if (stack.pop() != '[') {
					return false;
				}
			} else if (c == ')') {
				if (stack.isEmpty())
					return false;
				if (stack.pop() != '(')
					return false;

			} else if (c == '}') {
				if (stack.isEmpty())
					return false;
				if (stack.pop() != '{')
					return false;
			}

		}
		return stack.isEmpty();
	}
	public static boolean validateSubExpressionsForStringColumns(List<String> subExpressionList, String input1){
		//String input = input1.replaceAll("\\s+","");;

		for(String str : subExpressionList){
			input1 = input1.replace(str, ",");
		}
		input1 =input1.replace("(", "").replace("{", "").replace("[", "").replace("}", "").replace(")", "").replace("]", "");

		if (input1.split(",").length>1)
			for(String str : input1.split(",")){
				if(!("OR".equalsIgnoreCase(str) || "AND".equalsIgnoreCase(str) || "".equalsIgnoreCase(str) ||input1.equals(input1) ))
					return false;
			}



		for (String splitSting: subExpressionList){
			if (splitSting.indexOf("{")>=0 && splitSting.indexOf("}")>0 && (splitSting.substring(splitSting.indexOf("{"), splitSting.indexOf("}")).contains("%")||splitSting.substring(splitSting.indexOf("{"), splitSting.indexOf("}")).contains("="))){
				return false;
			}
			if (!(splitSting.indexOf("{")>-1 && splitSting.indexOf("(")>-1 && splitSting.indexOf("[")>-1) && splitSting.indexOf("{")==splitSting.lastIndexOf("{") && splitSting.indexOf("(")==splitSting.lastIndexOf("(") && splitSting.indexOf("[")==splitSting.lastIndexOf("[") ){

			}
			else {
				//System.err.println("Multi Expression > "+splitSting );
				//TODO Write validation for And and OR
				if (splitSting.indexOf("~(")>0 )
					return false;
				if((splitSting.split("([\\}\\)\\]]+OR+~?+[\\{\\(\\[])").length==1 && splitSting.split("([\\}\\)\\]]+AND+~?+[\\{\\(\\[])").length==1 ))
					return false;
			}
		}
		return true;
	}


	public static ArrayList<String> split(String s) {
		s = s.replaceAll("\\s+","");
		// The splits.
		ArrayList<String> split = new ArrayList<String>();
		// The stack.
		ArrayList<Start> stack = new ArrayList<Start>();
		// Walk the string.
		for (int i = 0; i < s.length(); i++) {
			// Get the char there.
			char ch = s.charAt(i);
			// Is it an open brace?
			int o = openBraces.indexOf(ch);
			// Is it a close brace?
			int c = closeBraces.indexOf(ch);
			if (o >= 0) {
				// Its an open! Push it.
				stack.add(new Start(o, i));
			} else if ( c >= 0 && stack.size() > 0 ) {
				// Pop (if matches).
				int tosPos = stack.size() - 1;
				Start tos = stack.get(tosPos);
				// Does the brace match?
				if ( tos.brace == c) {
					// Matches!
					if(tos.pos>=1 && s.charAt(tos.pos-1)=='~')
						split.add(s.substring(tos.pos-1, i+1));
					else
						split.add(s.substring(tos.pos, i+1));
					// Done with that one.
					stack.remove(tosPos);
				}
			}
		}
		return split;
	}

	private static class Start {
		// The brace number from the braces string in use.
		final int brace;
		// The position in the string it was seen.
		final int pos;

		// Constructor.
		public Start(int brace, int pos) {
			this.brace = brace;
			this.pos = pos;
		}

		@Override
		public String toString() {
			return "{"+openBraces.charAt(brace)+","+pos+"}";
		}
	}


	public static int getActualClientDTOCount(List<ClientUploaderDTO> dtoList){
		Set<String> uniqueClient=new HashSet<String>();
		for(int i=0;i<dtoList.size();i++){
			uniqueClient.add(dtoList.get(i).getClientName());
		}
		return uniqueClient.size();
	}

	public static int getActualErrorDTOCount(List<ErrorLogForUploader> errorDTOList){
		Set<String> errorDTO=new HashSet<String>();
		for(int i=0;i<errorDTOList.size();i++){
			errorDTO.add(errorDTOList.get(i).getEntityName());
		}
		return errorDTO.size();
	}


	public static int getActualEntityOwnerDTOCount(List<EntityOwnerUploaderDTO> entityownerUploadList) {

		Set<String> uniqueEntityOwnerList=new HashSet<String>();
		for(int i=0;i<entityownerUploadList.size();i++){
			uniqueEntityOwnerList.add(entityownerUploadList.get(i).getOwner_Name());
		}
		return uniqueEntityOwnerList.size();


	}


	public static int getActualEntityMasterDTOCount(List<EntityMasterUploaderDTO> enitityMasterUploaderlist) {

		Set<String> uniqueEntityMasterList=new HashSet<String>();
		for(int i=0;i<enitityMasterUploaderlist.size();i++){
			uniqueEntityMasterList.add(enitityMasterUploaderlist.get(i).getEntity_Name()+enitityMasterUploaderlist.get(i).getOwner_Name());
		}
		return uniqueEntityMasterList.size();



	}


	public static int getActualFlowTypesDTOCount(List<FlowTypesUploaderDTO> flowTypesList) {
		Set<String> uniqueFlowList=new HashSet<String>();
		for(int i=0;i<flowTypesList.size();i++){
			uniqueFlowList.add(flowTypesList.get(i).getFlow_type());
		}
		return uniqueFlowList.size();

	}


	public static int getActualTaskRepositoriesDTOCount(List<TaskRepositoriesUploaderDTO> taskRepositoriesList) {
		Set<String> uniquetaskRepositoriesList=new HashSet<String>();
		for(int i=0;i<taskRepositoriesList.size();i++){
			uniquetaskRepositoriesList.add(taskRepositoriesList.get(i).getName());
		}
		return uniquetaskRepositoriesList.size();
	}


	public static int getActualTaskMasterDTOCount(List<TaskMasterUploaderDTO> taskMasterList) {
		Set<String> uniquetaskMasterList=new HashSet<String>();
		for(int i=0;i<taskMasterList.size();i++){
			uniquetaskMasterList.add(taskMasterList.get(i).getTask_Name());
		}
		return uniquetaskMasterList.size();
	}



	public static int getActualTaskEntityDetailDTOCount(List<TaskEntityDetailUploaderDTO> taskEntityDetailList) {
		Set<String> uniqueEntityDetailList=new HashSet<String>();
		for(int i=0;i<taskEntityDetailList.size();i++){
			uniqueEntityDetailList.add(taskEntityDetailList.get(i).getTaskName());
		}
		return uniqueEntityDetailList.size();
	}



	public static void setComboValidation(Worksheet worksheet,String rangeName,int rowNo,int colNo){
		if(worksheet!=null){
			ValidationCollection validations = worksheet.getValidations();

			// Create a validation object adding to the collection list.
			CellArea ca = new CellArea();
			ca.StartRow = rowNo;
			ca.EndRow = rowNo;
			ca.StartColumn = colNo;
			ca.EndColumn = colNo;

			int index = validations.add(ca);
			Validation validation = validations.get(index);

			// Set the validation type.
			validation.setType(ValidationType.LIST);

			// Set the in cell drop down.
			validation.setInCellDropDown(true);

			// Set the formula1.
			validation.setFormula1("="+rangeName);

			// Enable it to show error.
			validation.setShowError(true);

			// Set the alert type severity level.
			validation.setAlertStyle(ValidationAlertType.STOP);

			// Set the error title.
			validation.setErrorTitle("Error");

			// Set the error message.
			validation.setErrorMessage("Please select a value from the list");

			// Specify the validation area of cells.
			CellArea area = new CellArea();
			area.StartRow = rowNo;
			area.StartColumn = colNo;
			area.EndRow = 65536;
			area.EndColumn = colNo;
			// Add the Validation area.
			validation.addArea(area);
		}
	}


	public static List<TaskEntityDetailUploaderDTO> setDTOForTaskEntityDetails(Worksheet worksheet){
		List<TaskEntityDetailUploaderDTO> taskEntityDetailList=new ArrayList<TaskEntityDetailUploaderDTO>();
		TaskEntityDetailUploaderDTO taskEntities=null;
		Cells cells = worksheet.getCells();
		cells.deleteBlankRows();
		cells.deleteBlankColumns();
		int colCount = worksheet.getCells().getMaxColumn();
		int rowCount = worksheet.getCells().getMaxRow();
		Map<String ,Integer> colOrder=new HashMap<String,Integer>();
		TaskEntityDetailUploaderDTO taskEntities1=new TaskEntityDetailUploaderDTO();
		for (int i = 0; i <= colCount; i++) {
			colOrder.put(cells.get(0, i).getValue().toString().trim().toLowerCase(),i);
		}		
		for (int i = 1; i <= rowCount; i++) {
			String entityNames[]={};

			taskEntities=new TaskEntityDetailUploaderDTO();
			for (int j = 0; j <= colCount; j++) {
				if(colOrder.get(entityDetailTaskRepository.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskEntities.setTaskRepository(cells.get(i, j).getValue().toString().trim());

					}
				}
				else if(colOrder.get(entityDetailTaskName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskEntities.setTaskName(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(entityOwnerName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskEntities.setEntityOwnerName(cells.get(i, j).getValue().toString().trim());
					}
				}
				else if(colOrder.get(taskEntityName.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						entityNames=cells.get(i, j).getValue().toString().trim().split(",");
					}
				}
				else if(colOrder.get(linkType.toLowerCase()).equals(new Integer(j))){
					if(cells.get(i, j).getValue()!=null && !"".equalsIgnoreCase(cells.get(i, j).getValue().toString())){
						taskEntities.setLinkType(cells.get(i, j).getValue().toString().trim());
					}
				}


			}
			for(int k=0;k<entityNames.length;k++){
				taskEntities1=new TaskEntityDetailUploaderDTO();
				taskEntities1.setEntityName(entityNames[k]);
				taskEntities1.setEntityOwnerName(taskEntities.getEntityOwnerName());
				taskEntities1.setTaskName(taskEntities.getTaskName());
				taskEntities1.setLinkType(taskEntities.getLinkType());
				taskEntities1.setTaskRepository(taskEntities.getTaskRepository());
				taskEntityDetailList.add(taskEntities1);
			}
			
			
		}
		return taskEntityDetailList;
	}

	@SuppressWarnings("unchecked")
	public static Workbook getTemplateDownload(Map<String, Object> listParameter,boolean hiddenFlag){
		int sheetIndex = 0;
		Workbook workbook=null;
		Worksheet worksheet =null;
		Cells cells=null;
		Style style=null;
		StyleFlag flag=null;
		//Cell clientHeader=null;
		Cell entityOwnerHeader=null;
		Cell enitityMasterHeader=null;
		Cell flowTypeHeader=null;
		Cell taskrepositoriesHeader=null;
		Cell taskMasterHeader=null;
		Range entityTypeRange=null;
		Range tasktypeTypeRange=null;
		Range externalSourceRange=null;
		Range isValidationRange=null;
		Range statusRange=null;
		Worksheet comboColDataSheet=null;
		List<String> taskTypeValue=null;
		String[] taskTypes=null;
		List<String> entityTypeValue=null;
		String[] entityTypes=null;
		Cell entityDetailHeader=null;
		Range linkTypeRange=null;


		try {
			taskTypeValue=(List<String>) listParameter.get("taskType");
			entityTypeValue=(List<String>) listParameter.get("entityType");
			workbook = new Workbook();
			//setting style for each sheet header
			Integer intColor = Integer.parseInt(colorCode, 16);
			Integer fntolor = Integer.parseInt("FFFFFF", 16);
			Color clr = Color.fromArgb(intColor);
			Color fntclr = Color.fromArgb(fntolor);
			//style = workbook.getStyles().get(workbook.getStyles().add());
			style=new Style();
			style.setPattern(BackgroundType.SOLID);
			style.setForegroundColor(clr);
			style.getFont().setSize(10);
			style.getFont().setColor(fntclr);
			style.setTextWrapped(true);	

			Style style1 =new Style();
			style1.setHorizontalAlignment(TextAlignmentType.CENTER);
			style1.setPattern(BackgroundType.SOLID);
			style1.setForegroundColor(clr);
			style1.getFont().setSize(10);
			style1.getFont().setColor(fntclr);
			style1.setTextWrapped(true);	

			//styling for wrapping text
			Style styleForWrap= new Style();
			styleForWrap.setTextWrapped(true);
			flag = new StyleFlag();
			flag.setWrapText(true);

			WorksheetCollection worksheets = workbook.getWorksheets();

			worksheet = worksheets.get(0);

			worksheet.setName(entityOwner);
			worksheet.getCells().setStandardWidth(30.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get(entityOwner).getCells();
			cells.applyStyle(styleForWrap, flag);


			sheetIndex = worksheets.add();
			comboColDataSheet = worksheets.get(sheetIndex);
			//hidden sheet
			comboColDataSheet.setName("Hidden");
			comboColDataSheet.setVisible(false);
			if(hiddenFlag){
				comboColDataSheet.getCells().get("O1").setValue(true);
			}else{
				comboColDataSheet.getCells().get("O1").setValue(false);
			}



			entityOwnerHeader = cells.get("A1");
			entityOwnerHeader.putValue(ownerName);
			entityOwnerHeader.setStyle(style);

			entityOwnerHeader = cells.get("B1");
			entityOwnerHeader.putValue(entityOwnerDesc);
			entityOwnerHeader.setStyle(style);

			entityOwnerHeader = cells.get("C1");
			entityOwnerHeader.putValue(externalSource);
			entityOwnerHeader.setStyle(style);

			//Combo range for ExternalSource
			externalSourceRange = comboColDataSheet.getCells().createRange(0, 2, 2, 1);
			externalSourceRange.setName("ExternalSource");
			externalSourceRange.get(0,0).setValue("Y");
			externalSourceRange.get(1,0).setValue("N");
			setComboValidation(worksheet,"ExternalSource",1,2);



			entityOwnerHeader = cells.get("D1");
			entityOwnerHeader.putValue(dataSourceName);
			entityOwnerHeader.setStyle(style);

			entityOwnerHeader = cells.get("E1");
			entityOwnerHeader.putValue(solutionName);
			entityOwnerHeader.setStyle(style);


			entityOwnerHeader = cells.get("F1");
			entityOwnerHeader.putValue(contactDetails);
			entityOwnerHeader.setStyle(style);

			entityOwnerHeader = cells.get("G1");
			entityOwnerHeader.putValue(displayOrder);
			entityOwnerHeader.setStyle(style);




			sheetIndex = worksheets.add();
			worksheet = worksheets.get(sheetIndex);
			worksheet.setName(entityMaster);
			worksheet.getCells().setStandardWidth(35.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get(entityMaster).getCells();
			cells.applyStyle(styleForWrap, flag);

			enitityMasterHeader = cells.get("A1");
			enitityMasterHeader.putValue(entityName);
			enitityMasterHeader.setStyle(style);

			enitityMasterHeader = cells.get("B1");
			enitityMasterHeader.putValue(entityMasterOwnerName);
			enitityMasterHeader.setStyle(style);

			enitityMasterHeader = cells.get("C1");
			enitityMasterHeader.putValue(entityType);
			enitityMasterHeader.setStyle(style);

			//Combo range for entity type
			if(entityTypeValue!=null){
				entityTypes=entityTypeValue.toArray(new String[0]);
				if(entityTypes.length>0){
					entityTypeRange = comboColDataSheet.getCells().createRange(0, 12, entityTypes.length, 1);
					entityTypeRange.setName("EntityTypeRange");
					for(int i=0;i<entityTypes.length;i++){
						entityTypeRange.get(i,0).setValue(entityTypes[i]);
					}
					setComboValidation(worksheet,"EntityTypeRange",1,2);
				}
			}

			enitityMasterHeader = cells.get("D1");
			enitityMasterHeader.putValue(entityDetail);
			enitityMasterHeader.setStyle(style);

			enitityMasterHeader = cells.get("E1");
			enitityMasterHeader.putValue(entityDesc);
			enitityMasterHeader.setStyle(style);


			//For Flow Types
			sheetIndex = worksheets.add();
			worksheet = worksheets.get(sheetIndex);
			worksheet.setName(flowTypes);
			worksheet.getCells().setStandardWidth(30.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get(flowTypes).getCells();
			cells.applyStyle(styleForWrap, flag);

			flowTypeHeader = cells.get("A1");
			flowTypeHeader.putValue(flowType);
			flowTypeHeader.setStyle(style);

			flowTypeHeader = cells.get("B1");
			flowTypeHeader.putValue(flowtypeDesc);
			flowTypeHeader.setStyle(style);


			//For Task Repositories

			sheetIndex = worksheets.add();
			worksheet = worksheets.get(sheetIndex);
			worksheet.setName(taskRepository);
			worksheet.getCells().setStandardWidth(30.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get(taskRepository).getCells();
			cells.applyStyle(styleForWrap, flag);

			taskrepositoriesHeader = cells.get("A1");
			taskrepositoriesHeader.putValue(taskRepositoryName);
			taskrepositoriesHeader.setStyle(style);

			taskrepositoriesHeader = cells.get("B1");
			taskrepositoriesHeader.putValue(taskRepositoryDesc);
			taskrepositoriesHeader.setStyle(style);

			//For Task Master

			sheetIndex = worksheets.add();
			worksheet = worksheets.get(sheetIndex);
			worksheet.setName(taskMaster);
			worksheet.getCells().setStandardWidth(30.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get(taskMaster).getCells();
			cells.applyStyle(styleForWrap, flag);
			//			Range range = worksheet.getCells().createRange("L1:M1");
			//			Range range2 = worksheet.getCells().createRange("J1:K1");
			//			range.merge();
			//			range2.merge();


			taskMasterHeader = cells.get("A1");
			taskMasterHeader.putValue(taskMasterRepositoryName);
			taskMasterHeader.setStyle(style);

			taskMasterHeader = cells.get("B1");
			taskMasterHeader.putValue(taskName);
			taskMasterHeader.setStyle(style);


			taskMasterHeader = cells.get("C1");
			taskMasterHeader.putValue(taskType);
			taskMasterHeader.setStyle(style);

			//Combo range for Task Type
			if(taskTypeValue!=null)
			{
				taskTypes = taskTypeValue.toArray(new String[0]);
				if(taskTypes.length>0){
					tasktypeTypeRange = comboColDataSheet.getCells().createRange(0, 1, taskTypes.length, 1);
					tasktypeTypeRange.setName("TaskTypeRange");
					for(int i=0;i<taskTypes.length;i++){
						tasktypeTypeRange.get(i,0).setValue(taskTypes[i]);
					}
					setComboValidation(worksheet,"TaskTypeRange",1,2);
				}
			}
			taskMasterHeader = cells.get("D1");
			taskMasterHeader.putValue(taskDesc);
			taskMasterHeader.setStyle(style);


			taskMasterHeader = cells.get("E1");
			taskMasterHeader.putValue(taskTechnicalName);
			taskMasterHeader.setStyle(style);

			taskMasterHeader = cells.get("F1");
			taskMasterHeader.putValue(SubtaskTechnicalName);
			taskMasterHeader.setStyle(style);

			taskMasterHeader = cells.get("G1");
			taskMasterHeader.putValue(taskFlows);
			taskMasterHeader.setStyle(style);

			taskMasterHeader = cells.get("H1");
			taskMasterHeader.putValue(taskFrequencyOffsetHeader);
			taskMasterHeader.setStyle(style);

			taskMasterHeader = cells.get("I1");
			taskMasterHeader.putValue(taskFrequencyExclusionOffsetHeader);
			taskMasterHeader.setStyle(style);


			taskMasterHeader = cells.get("J1");
			taskMasterHeader.putValue(taskStatus);
			taskMasterHeader.setStyle(style);

			//Combo range for Status
			statusRange = comboColDataSheet.getCells().createRange(0, 7, 2, 1);
			statusRange.setName("Status");
			statusRange.get(0,0).setValue("Active");
			statusRange.get(1,0).setValue("Deactive");
			setComboValidation(worksheet,"Status",1,13);

			taskMasterHeader = cells.get("K1");
			taskMasterHeader.putValue(taskIsValidationRequired);
			taskMasterHeader.setStyle(style);

			//Combo range for Validation Required
			isValidationRange = comboColDataSheet.getCells().createRange(0, 2, 2, 1);
			isValidationRange.setName("IsValidationRequired");
			isValidationRange.get(0,0).setValue("Y");
			isValidationRange.get(1,0).setValue("N");
			setComboValidation(worksheet,"IsValidationRequired",1,14);

			//For Task Entity Detail
			sheetIndex = worksheets.add();
			worksheet = worksheets.get(sheetIndex);
			worksheet.setName(taskEntityDetails);
			worksheet.getCells().setStandardWidth(30.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get(taskEntityDetails).getCells();
			cells.applyStyle(styleForWrap, flag);

			//Task Entity Details Headers
			entityDetailHeader = cells.get("A1");
			entityDetailHeader.putValue(entityDetailTaskRepository);
			entityDetailHeader.setStyle(style);

			entityDetailHeader = cells.get("B1");
			entityDetailHeader.putValue(entityDetailTaskName);
			entityDetailHeader.setStyle(style);


			entityDetailHeader = cells.get("C1");
			entityDetailHeader.putValue(entityOwnerName);
			entityDetailHeader.setStyle(style);

			entityDetailHeader = cells.get("D1");
			entityDetailHeader.putValue(taskEntityName);
			entityDetailHeader.setStyle(style);

			entityDetailHeader = cells.get("E1");
			entityDetailHeader.putValue(linkType);
			entityDetailHeader.setStyle(style);

			//Combo range for Status
			linkTypeRange = comboColDataSheet.getCells().createRange(0, 8, 2, 1);
			linkTypeRange.setName("LinkType");
			linkTypeRange.get(0,0).setValue("S");
			linkTypeRange.get(1,0).setValue("T");
			setComboValidation(worksheet,"LinkType",1,4);



			//workbook.save(fileName);

		} catch (Exception e) {
			logger.error("Error occured while Template Download", e);
		}
		return workbook ;
	}

	@SuppressWarnings("unchecked")
	public static Workbook getTemplateForLineage(){
		int sheetIndex = 0;
		Workbook workbook=null;
		Worksheet worksheet =null;
		Cells cells=null;
		Style style=null;
		StyleFlag flag=null;
		//Cell clientHeader=null;


		try {

			workbook = new Workbook();
			//setting style for each sheet header
			Integer intColor = Integer.parseInt(colorCode, 16);
			Integer fntolor = Integer.parseInt("FFFFFF", 16);
			Color clr = Color.fromArgb(intColor);
			Color fntclr = Color.fromArgb(fntolor);
			//style = workbook.getStyles().get(workbook.getStyles().add());
			style=new Style();
			style.setPattern(BackgroundType.SOLID);
			style.setForegroundColor(clr);
			style.getFont().setSize(10);
			style.getFont().setColor(fntclr);
			style.setTextWrapped(true);	

			Style style1 =new Style();
			style1.setHorizontalAlignment(TextAlignmentType.CENTER);
			style1.setPattern(BackgroundType.SOLID);
			style1.setForegroundColor(clr);
			style1.getFont().setSize(10);
			style1.getFont().setColor(fntclr);
			style1.setTextWrapped(true);	

			//styling for wrapping text
			Style styleForWrap= new Style();
			styleForWrap.setTextWrapped(true);
			flag = new StyleFlag();
			flag.setWrapText(true);

			WorksheetCollection worksheets = workbook.getWorksheets();

			worksheet = worksheets.get(0);

			worksheet.setName("Data Consumer Status");
			worksheet.getCells().setStandardWidth(30.5f);
			worksheet.getCells().setRowHeight(0, 20.5f);
			cells = worksheets.get("Data Consumer Status").getCells();
			cells.applyStyle(styleForWrap, flag);


			sheetIndex = worksheets.add();			
			cells.get("A1").putValue("Solution Name");
			cells.get("A1").setStyle(style);
			cells.get("B1").putValue("Consumer Type");
			cells.get("C1").putValue("Consumer Name");
			cells.get("D1").putValue("Sub-Item Name");
			cells.get("E1").putValue("Data Availability");
			cells.get("H1").putValue("Data Processing");
			cells.get("E2").putValue("Status");
			cells.get("F2").putValue("Due Date");
			cells.get("G2").putValue("Completion Date");
			cells.get("H2").putValue("End Due Date");
			cells.get("I2").putValue("Last Processing Date");
			cells.get("E2").putValue("Status");
			cells.get("J1").putValue("Consumer Datasets (Solution Entities)");
			cells.get("J2").putValue("Repository");
			cells.get("K2").putValue("Entity Name");
			cells.get("L2").putValue("Status");
			cells.get("M1").putValue("Source Entities");
			cells.get("M2").putValue("Source System");
			cells.get("N2").putValue("Entity Name");
			cells.get("O2").putValue("Status");
			cells.get("A1").setStyle(style);
			cells.get("B1").setStyle(style);
			cells.get("C1").setStyle(style);
			cells.get("D1").setStyle(style);
			cells.get("E1").setStyle(style);
			cells.get("H1").setStyle(style);
			cells.get("E2").setStyle(style);
			cells.get("F2").setStyle(style);
			cells.get("G2").setStyle(style);
			cells.get("H2").setStyle(style);
			cells.get("I2").setStyle(style);
			cells.get("J1").setStyle(style);
			cells.get("E2").setStyle(style);
			cells.get("J2").setStyle(style);
			cells.get("K2").setStyle(style);
			cells.get("L2").setStyle(style);
			cells.get("M2").setStyle(style);
			cells.get("N2").setStyle(style);
			cells.get("O2").setStyle(style);
			cells.get("M1").setStyle(style);
			Range range = worksheet.getCells().createRange("E1:G1");
			Range range2 = worksheet.getCells().createRange("H1:I1");
			Range range3 = worksheet.getCells().createRange("A1:A2");
			Range range4 = worksheet.getCells().createRange("B1:B2");
			Range range5 = worksheet.getCells().createRange("C1:C2");
			Range range6 = worksheet.getCells().createRange("D1:D2");
			Range range7 = worksheet.getCells().createRange("J1:L1");
			Range range8 = worksheet.getCells().createRange("M1:O1");
			range.merge();
			range2.merge();
			range3.merge();
			range4.merge();
			range5.merge();
			range6.merge();
			range7.merge();
			range8.merge();



			//workbook.save(fileName);

		} catch (Exception e) {
			logger.error("Error occured while Template Download", e);
		}
		return workbook ;
	}

	public static List<ErrorLogForUploader> checkCyclicDependency(List<TaskEntityDetailUploaderDTO> taskSTList,List<TaskMasterUploaderDTO> taskMasterList){
		ErrorLogForUploader errorLogObj ;
		List<TaskEntityDetailUploaderDTO> tempSTList = new ArrayList<>();
		Set<String> allSource= new HashSet<>();
		List<ErrorLogForUploader> errorLogList= new ArrayList<>();
		for(TaskMasterUploaderDTO t:taskMasterList){
			String taskName=t.getTask_Name();
			String repoName = t.getTask_Repository();
			tempSTList=taskSTList.stream().filter(col->col.getTaskRepository().equalsIgnoreCase(repoName) 
					&& col.getTaskName().equalsIgnoreCase(taskName) && col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
			
			allSource=getAllSourceNameForATarget(taskSTList,t.getTask_Name(),t.getTask_Repository(),new HashSet<String>(),new HashSet<String>());

			for(TaskEntityDetailUploaderDTO tgt:tempSTList){
				if(allSource.contains(tgt.getEntityName().toLowerCase()+tgt.getEntityOwnerName().toLowerCase())){
					errorLogObj= new ErrorLogForUploader();
					errorLogObj = new ErrorLogForUploader();
					errorLogObj.setEntityType("Task Master");
					errorLogObj.setEntityName(t.getTask_Name());
					errorLogObj.setErrorMsg("Cyclic dependency for becuase of Entity : "+ tgt.getEntityName()+" Belongs to Entity Owner: "+tgt.getEntityOwnerName());
					errorLogList.add(errorLogObj);
				} 
			}
			
			

		}
		return errorLogList;
	}

	private static Set<String> getAllSourceNameForATarget(List<TaskEntityDetailUploaderDTO> taskSTList,String taskName,String repoName,Set<String> allSources,Set<String> completedTask){
		List<TaskEntityDetailUploaderDTO> tempSList = new ArrayList<>();
		List<TaskEntityDetailUploaderDTO> tempTList = new ArrayList<>();
		if(!completedTask.contains(taskName.toLowerCase()+repoName.toLowerCase())){
			completedTask.add(taskName.toLowerCase()+repoName.toLowerCase());
			tempSList=taskSTList.stream().filter(col->col.getTaskRepository().equalsIgnoreCase(repoName) 
					&& col.getTaskName().equalsIgnoreCase(taskName) && col.getLinkType().equalsIgnoreCase("S")).collect(Collectors.toList());
			for(TaskEntityDetailUploaderDTO st:tempSList){
				if(!allSources.contains(st.getEntityName().toLowerCase()+st.getEntityOwnerName().toLowerCase())){
					allSources.add(st.getEntityName().toLowerCase()+st.getEntityOwnerName().toLowerCase());
					String entityName=st.getEntityName();
					String ownerName=st.getEntityOwnerName();

					tempTList=taskSTList.stream().filter(col->col.getEntityName().equalsIgnoreCase(entityName) &&
							col.getEntityOwnerName().equalsIgnoreCase(ownerName) && col.getLinkType().equalsIgnoreCase("T")).collect(Collectors.toList());
					if(tempTList.size()>0){
						for(TaskEntityDetailUploaderDTO tt:tempTList){
							allSources=getAllSourceNameForATarget(taskSTList,tt.getTaskName(),tt.getTaskRepository(),allSources,completedTask);
						}

					}
				}

			}
		}
		

		return allSources;
	}

}
