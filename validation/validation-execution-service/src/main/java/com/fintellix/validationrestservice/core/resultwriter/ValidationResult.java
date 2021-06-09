/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ValidationStringUtils;

/**
 * @author sumeet.tripathi
 *
 */

@Component
@Scope("prototype")
public class ValidationResult {

	private AtomicInteger rowCount = new AtomicInteger(0);

	private Workbook mainWorkBook;
	private Worksheet mainWorkSheet;

	private Workbook jsonWorkBook;
	private Worksheet jsonWorkSheet;

	private List<String> headerColumnSequence;
	private Map<String, String> columnNameMap;
	private Integer exprId;
	private Integer runId;
	private String outputDirectory;
	private String mainFileName;
	private String jsonFileName;

	public Integer getRowId() {
		return rowCount.incrementAndGet();
	}

	public Workbook getMainWorkBook() {
		return mainWorkBook;
	}

	public Worksheet getMainWorkSheet() {
		return mainWorkSheet;
	}

	public Workbook getJsonWorkBook() {
		return jsonWorkBook;
	}

	public Worksheet getJsonWorkSheet() {
		return jsonWorkSheet;
	}

	public List<String> getHeaderColumnSequence() {
		return headerColumnSequence;
	}

	public Map<String, String> getColumnNameMap() {
		return columnNameMap;
	}

	public Integer getExprId() {
		return exprId;
	}

	public Integer getRunId() {
		return runId;
	}

	public String getMainFileName() {
		return mainFileName;
	}

	public String getJsonFileName() {
		return jsonFileName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void init(List<String> headerColumnSequence, Map<String, String> columnNameMap, Integer exprId,
			Integer runId) {

		this.headerColumnSequence = headerColumnSequence;
		this.columnNameMap = columnNameMap;
		this.runId = runId;
		this.exprId = exprId;
		
		outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

		mainWorkBook = new Workbook();
		mainWorkBook.getSettings().setCheckExcelRestriction(Boolean.FALSE);

		mainWorkSheet = mainWorkBook.getWorksheets().add(exprId + "");

		populateHeaders();
		mainFileName = "Validation_Result_" + runId + "_" + exprId + ".csv";
		createCsvFile(outputDirectory, mainFileName);
		if (ApplicationProperties.getValue("app.validations.resulthandler")
				.equalsIgnoreCase(ExpressionResultHandlerType.MULTICSV.getValue())) {
			jsonWorkBook = new Workbook();
			jsonWorkBook.getSettings().setCheckExcelRestriction(Boolean.FALSE);

			jsonWorkSheet = jsonWorkBook.getWorksheets().add(exprId + "");

			jsonWorkSheet.getCells().get(0, 0).setValue("ROW_ID");
			jsonWorkSheet.getCells().get(0, 1).setValue("JSON");

			jsonFileName = "Validation_Result_" + runId + "_" + exprId + "_JSON" + ".csv";
			createCsvFile(outputDirectory, jsonFileName);
		}

	}

	private void createCsvFile(String outputDirectory, String fileName) {
		try {
			File file = new File(outputDirectory + fileName);
			file.createNewFile();
		} catch (IOException e) {
			// do-nothing
		}
	}

	private void populateHeaders() {
		int column = 0;
		for (String header : headerColumnSequence) {
			mainWorkSheet.getCells().get(0, column).setValue(columnNameMap.get(header));
			column++;
		}
	}

}
