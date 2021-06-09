/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.aspose.cells.TxtSaveOptions;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.fintellix.validationrestservice.core.resultwriter.ExpressionResultHandler;
import com.fintellix.validationrestservice.core.resultwriter.ExpressionResultHandlerType;
import com.fintellix.validationrestservice.core.resultwriter.ValidationResult;
import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.google.gson.Gson;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class CSVExpressionResultHandler implements ExpressionResultHandler {

	@Override
	public void writeValidationResult(Integer exprId, Integer runId, List<Map<String, Object>> rows,
			Map<String, String> columnNameMap, List<String> headerColumnSequence) {

		int rowId = 0;
		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

		String fileName = "Validation_Result_" + runId + "_" + exprId + ".csv";

		createCsvFile(outputDirectory, fileName);

		Workbook wb = new Workbook();
		Worksheet ws = wb.getWorksheets().add(exprId + "");

		//ignoring 32k aspose limit
		wb.getSettings().setCheckExcelRestriction(Boolean.FALSE);
		
		populateHeaders(ws, columnNameMap, rowId, headerColumnSequence);
		rowId++;
		for (Map<String, Object> row : rows) {
			populateRow(ws, row, rowId, headerColumnSequence);
			rowId++;
		}
		writeCsv(wb, fileName, runId);

	}

	@Override
	public String handlerType() {
		return ExpressionResultHandlerType.CSV.getValue();
	}

	@Override
	public ExpressionMetaData getExpressionMetaData(Integer runId, Integer exprId, String metadata) throws Throwable {
		return getExpressionMetaData(metadata);
	}

	@Override
	public ExpressionMetaData getExpressionMetaDataForZeroOccurrence(Integer runId, Integer exprId, String metadata)
			throws Throwable {
		return getExpressionMetaData(metadata);
	}

	@Override
	public void deleteExpressionResultByRunId(Integer runId) {
		// do-nothing

	}

	private void createCsvFile(String outputDirectory, String fileName) {
		try {
			File file = new File(outputDirectory + fileName);
			file.createNewFile();
		} catch (IOException e) {
			// do-nothing
		}
	}

	private void populateHeaders(Worksheet ws, Map<String, String> columnNameMap, int rowID,
			List<String> headerColumnSequence) {
		int column = 0;
		for (String header : headerColumnSequence) {
			ws.getCells().get(rowID, column).setValue(columnNameMap.get(header));
			column++;
		}
	}

	private void populateRow(Worksheet ws, Map<String, Object> row, int rowID, List<String> headerColumnSequence) {
		int column = 0;
		for (String header : headerColumnSequence) {
			try {
				//changing to string to handle default date formatting
				ws.getCells().get(rowID, column).setValue(row.get(header) != null ? row.get(header).toString() : null);
			}catch (Exception e) {
				e.printStackTrace();
			}
			column++;
		}
	}

	private void writeCsv(Workbook workbook, String fileName, Integer runId) {

//		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
//				+ File.separator;
//		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);
//
//		try {
//			workbook.getWorksheets().setActiveSheetIndex(1);
//			workbook.save(outputDirectory + fileName, SaveFormat.CSV);
//		} catch (Exception e) {
//			//do-nothing
//			e.printStackTrace();
//		}

		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

		byte[] workbookData = new byte[0];

		// Text save options. You can use any type of separator
		TxtSaveOptions opts = new TxtSaveOptions();
		opts.setSeparator(',');
		for (int idx = 0; idx < workbook.getWorksheets().getCount(); idx++) {
			// Save the active worksheet into text format
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			workbook.getWorksheets().setActiveSheetIndex(idx);
			try {
				workbook.save(bout, opts);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Save the worksheet data into sheet data array
			byte[] sheetData = bout.toByteArray();

			// Combine this worksheet data into workbook data array
			byte[] combinedArray = new byte[workbookData.length + sheetData.length];
			System.arraycopy(workbookData, 0, combinedArray, 0, workbookData.length);
			System.arraycopy(sheetData, 0, combinedArray, workbookData.length, sheetData.length);

			workbookData = combinedArray;
		}

		File file = new File(outputDirectory + fileName);

		// Save entire workbook data into file
		FileOutputStream fout;
		try {

			fout = new FileOutputStream(file);
			fout.write(workbookData);
			fout.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private ExpressionMetaData getExpressionMetaData(String json) {
		// handled = ! < > ' ;
		json = json.replace("u003d", "=").replace("u0021", "!").replace("u003C", "<").replace("u003E", ">")
				.replace("u0027", "'").replace("u003B", ";").replace("\\=", "=").replace("\"entityName\":\"\",", "\"entityName\":\" \",");
		Gson g = new Gson();
		ExpressionMetaData emd = null;
		try {
			// grid
			emd = g.fromJson(json, ExpressionMetaData.class);
		} catch (Exception e) {
			try {
				// list
				emd = g.fromJson(json.replace("\"\"\"}", "\"\"}").replace("\"\"\"", "\"\\\"").replace("\"\"", "\\\"\""),
						ExpressionMetaData.class);

			} catch (Exception ex) {
				try {
					// grid with sub expr
					emd = g.fromJson(json.replace("\"\"\"}", "\"\"}").replace("\"\"\"", "\"\\\"")
							.replace("\"entityName\":\"\"", "\"entityName\":\" \"").replace("\"\"", "\\\"")
							.replace("\"entityName\":\"\"", "\"entityName\":\"\""), ExpressionMetaData.class);
				} catch (Exception x) {
					try {
						// grid with sub expr
						emd = g.fromJson(json.replace("\"\"\"}", "\\\"\"\"}").replace(":\"\"\"",":\"\\\"").replace("\"\",", "\\\"\",").replace("\"\"}", "\"}"), ExpressionMetaData.class);
					} catch (Exception xe) {
						throw xe;
					}
				}
			}
		}
		return emd;
	}

	@Override
	public void writeRow(ValidationResult validationResult, Map<String, Object> row) {
		populateRow(validationResult.getMainWorkSheet(), row, validationResult.getRowId(),
				validationResult.getHeaderColumnSequence());

	}

	@Override
	public void writeFile(ValidationResult validationResult) {
		writeCsv(validationResult.getMainWorkBook(), validationResult.getMainFileName(), validationResult.getRunId());
		validationResult.getMainWorkBook().dispose();
	}
}
