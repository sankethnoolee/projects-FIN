/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.fintellix.validationrestservice.util.connectionManager.CalciteConnectionManager;
import com.google.gson.Gson;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class MultiCSVExpressionResultHandler implements ExpressionResultHandler {

	@Override
	public void writeValidationResult(Integer exprId, Integer runId, List<Map<String, Object>> rows,
			Map<String, String> columnNameMap, List<String> headerColumnSequence) {

		int rowId = 0;
		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

		String fileName = "Validation_Result_" + runId + "_" + exprId + ".csv";
		String jsonFileName = "Validation_Result_" + runId + "_" + exprId + "_JSON" + ".csv";

		createCsvFile(outputDirectory, fileName);
		createCsvFile(outputDirectory, jsonFileName);

		Workbook wb = new Workbook();
		Worksheet ws = wb.getWorksheets().add(exprId + "");

		Workbook jsonWb = new Workbook();
		Worksheet jsonWs = jsonWb.getWorksheets().add(exprId + "");
		
		//ignoring 32k aspose limit
		wb.getSettings().setCheckExcelRestriction(Boolean.FALSE);
		jsonWb.getSettings().setCheckExcelRestriction(Boolean.FALSE);
		
		jsonWs.getCells().get(0, 0).setValue("ROW_ID");
		jsonWs.getCells().get(0, 1).setValue("JSON");

		populateHeaders(ws, columnNameMap, rowId, headerColumnSequence);
		rowId++;
		Integer jsonRowId = 1;
		for (Map<String, Object> row : rows) {

			jsonWs.getCells().get(jsonRowId, 0).setValue(rowId);
			jsonWs.getCells().get(jsonRowId, 1).setValue(row.get("Expression_Meta_data"));
			jsonRowId++;

			row.put("Expression_Meta_data", rowId);

			populateRow(ws, row, rowId, headerColumnSequence);
			rowId++;
		}
		writeCsv(wb, fileName, runId);
		writeCsv(jsonWb, jsonFileName, runId);

	}
	

	@Override
	public String handlerType() {
		return ExpressionResultHandlerType.MULTICSV.getValue();
	}

	@Override
	public ExpressionMetaData getExpressionMetaData(Integer runId, Integer exprId, String metadata) throws Throwable {
		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

		String fileName = "Validation_Result_" + runId + "_" + exprId + "_JSON";
		String schema = "S_" + System.currentTimeMillis() + "_S";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = CalciteConnectionManager.getCalciteConnection(outputDirectory, schema);
			ps = conn.prepareStatement(
					"select * from " + schema + "." + fileName + " where ROW_ID = '" + metadata.trim() + "'");
			rs = ps.executeQuery();
			if (rs.next()) {
				return getExpressionMetaData(rs.getString("JSON"));
			}

		} catch (Throwable e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// do-nothing
			}

			try {
				ps.close();
			} catch (SQLException e) {
				// do-nothing
			}

			try {
				conn.close();
			} catch (SQLException e) {
				// do-nothing
			}

		}
		return new ExpressionMetaData();
	}

	@Override
	public ExpressionMetaData getExpressionMetaDataForZeroOccurrence(Integer runId, Integer exprId, String metadata)
			throws Throwable {

		if (!Boolean.parseBoolean(ApplicationProperties.getValue("app.ignore.expressionmetadata"))) {
			try {
				return getExpressionMetaData(runId, exprId, metadata);
			} catch (Exception e) {
				// do-nothing
			}
		}
		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

		String fileName = "Validation_Result_" + runId + "_" + exprId + "_JSON";
		String schema = "S_" + System.currentTimeMillis() + "_S";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = CalciteConnectionManager.getCalciteConnection(outputDirectory, schema);
			ps = conn.prepareStatement("select * from " + schema + "." + fileName);
			rs = ps.executeQuery();
			while (rs.next()) {

				if (rs.getString("JSON") != null && rs.getString("JSON").trim().length() > 0) {
					return getExpressionMetaData(rs.getString("JSON"));
				}

			}

		} catch (Throwable e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// do-nothing
			}

			try {
				ps.close();
			} catch (SQLException e) {
				// do-nothing
			}

			try {
				conn.close();
			} catch (SQLException e) {
				// do-nothing
			}

		}
		return new ExpressionMetaData();
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
				ws.getCells().get(rowID, column).setValue(row.get(header) != null ? row.get(header).toString() : null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			column++;
		}
	}

	private void writeCsv(Workbook workbook, String fileName, Integer runId) {
		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

//		
//		try {
//			workbook.getWorksheets().setActiveSheetIndex(1);
//			workbook.save(outputDirectory + fileName, SaveFormat.CSV);
//		} catch (Exception e) {
//			//do-nothing
//			e.printStackTrace();
//		}
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
	public void deleteExpressionResultByRunId(Integer runId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeRow(ValidationResult validationResult, Map<String, Object> row) {
		Integer rowId = validationResult.getRowId();

		validationResult.getJsonWorkSheet().getCells().get(rowId, 0).setValue(rowId);
		validationResult.getJsonWorkSheet().getCells().get(rowId, 1).setValue(row.get("Expression_Meta_data"));

		row.put("Expression_Meta_data", rowId);

		populateRow(validationResult.getMainWorkSheet(), row, rowId, validationResult.getHeaderColumnSequence());

	}

	@Override
	public void writeFile(ValidationResult validationResult) {

		writeCsv(validationResult.getMainWorkBook(), validationResult.getMainFileName(), validationResult.getRunId());
		validationResult.getMainWorkBook().dispose();

		writeCsv(validationResult.getJsonWorkBook(), validationResult.getJsonFileName(), validationResult.getRunId());
		validationResult.getJsonWorkBook().dispose();
	}
}
