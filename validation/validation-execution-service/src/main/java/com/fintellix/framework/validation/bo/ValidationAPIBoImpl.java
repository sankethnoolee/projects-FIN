package com.fintellix.framework.validation.bo;

import com.fintellix.framework.validation.dao.DaoFactory;
import com.fintellix.validationrestservice.api.transformer.CSVTransformer;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.util.connectionManager.CalciteConnectionManager;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;
import com.northconcepts.datapipeline.core.DataReader;
import com.northconcepts.datapipeline.core.FieldList;
import com.northconcepts.datapipeline.core.Record;
import com.northconcepts.datapipeline.jdbc.JdbcReader;
import com.northconcepts.datapipeline.job.Job;
import com.northconcepts.datapipeline.memory.MemoryWriter;
import com.northconcepts.datapipeline.transform.BasicFieldTransformer;
import com.northconcepts.datapipeline.transform.SetField;
import com.northconcepts.datapipeline.transform.Transformer;
import com.northconcepts.datapipeline.transform.TransformingReader;
import com.northconcepts.datapipeline.transform.lookup.CachedLookup;
import com.northconcepts.datapipeline.transform.lookup.DataReaderLookup;
import com.northconcepts.datapipeline.transform.lookup.Lookup;
import com.northconcepts.datapipeline.transform.lookup.LookupTransformer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Component
public class ValidationAPIBoImpl implements ValidationAPIBo {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static Properties applicationProperties;
    private final String GETRETURNVALIDATIONRESULTSUMMARY = "GETRETURNVALIDATIONRESULTSUMMARY";
    private final String GETRETURNVALIDATIONRESULTGROUPSUMMARY = "GETRETURNVALIDATIONRESULTGROUPSUMMARY";
    private final String GETRETURNVALIDATIONRESULTDETAILS = "GETRETURNVALIDATIONRESULTDETAILS";
    private final String YEAR = "YYYYMM(01)DD(01)";
    private final String QUARTER = "YYYYQQDD(31)";
    private final String MONTH = "YYYYMMMMDD(31)";
    private final String Default = "Default";

    @Autowired
    DaoFactory validationDaoFactory;

    static {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("application.properties");
            applicationProperties = new Properties();
            applicationProperties.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Coudnt read application properties from class path", e);
        }
    }

    @Override
    public Integer getRecordCountForPagination(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                               String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution) {
        logger.info("EXEFLOW --> ValidationAPIBoImpl --> getRecordCountForPagination()");
        logger.info("DB fetch start -> " + getCurrentTimeStamp());
        Integer count = validationDaoFactory.getValidationAPIDao().getRecordCountForPagination(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution);
        logger.info("DB fetch done -> " + getCurrentTimeStamp());
        return count;
    }

    @Override
    public JSONArray getReturnValidationResultSummary(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                      String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                      String page, String rows) {
        logger.info("EXEFLOW --> ValidationAPIBoImpl --> getReturnValidationResultSummary()");
        logger.info("DB fetch start -> " + getCurrentTimeStamp());
        List<Object[]> summary = validationDaoFactory.getValidationAPIDao().getReturnValidationResultSummary(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution, page, rows);
        logger.info("DB fetch done -> " + getCurrentTimeStamp());
        return this.getJSON(summary, GETRETURNVALIDATIONRESULTSUMMARY);
    }

    @Override
    public JSONArray getReturnValidationResultGroupSummary(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                           String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                           String page, String rows) {
        logger.info("EXEFLOW --> ValidationAPIBoImpl --> getReturnValidationResultGroupSummary()");
        logger.info("DB fetch start -> " + getCurrentTimeStamp());
        List<Object[]> summary = validationDaoFactory.getValidationAPIDao().getReturnValidationResultSummary(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution, page, rows);
        logger.info("DB fetch done -> " + getCurrentTimeStamp());
        return this.getJSON(summary, GETRETURNVALIDATIONRESULTGROUPSUMMARY);
    }

    @Override
    public JSONArray getReturnValidationResultDetails(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                      String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                      String validationStatus, String validationType, String validationCode, String returnValidationCategory, String hashKey, String page,
                                                      String rows) throws Throwable {
        logger.info("EXEFLOW --> ValidationAPIBoImpl --> getReturnValidationResultDetails()");
        logger.info("DB fetch start -> " + getCurrentTimeStamp());
        List<Object[]> summary = validationDaoFactory.getValidationAPIDao().getReturnValidationResultDetails(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution, validationStatus, validationType,
                validationCode, returnValidationCategory, hashKey, page, rows);
        logger.info("DB fetch done -> " + getCurrentTimeStamp());

        logger.info("Transformation start -> " + getCurrentTimeStamp());
        JSONArray array = this.transformer(this.getJSON(summary, GETRETURNVALIDATIONRESULTDETAILS), solution, page, rows);
        logger.info("Transformation done -> " + getCurrentTimeStamp());
        System.out.println(array.toString());

        return array;
    }

    private JSONArray getJSON(List<Object[]> summary, String api) {
        JSONArray array = new JSONArray();
        JSONObject obj;
        for (Object[] element : summary) {
            obj = new JSONObject();
            obj.put("solutionName", element[0]);
            obj.put("returnID", element[1]);
            obj.put("return", element[2]);
            obj.put("returnName", element[3]);
            obj.put("period", element[4]);
            obj.put("organizationCode", element[5]);
            obj.put("organizationName", element[6]);
            obj.put("createdByUserOrganizationCode", element[7]);
            obj.put("createdByUserOrganizationName", element[8]);
            obj.put("returnVersionNumber", element[9]);
            obj.put("returnVersionName", element[10]);
            obj.put("returnStatus", element[11]);
            obj.put("returnCreatedDate", element[13] != null ? element[13].toString() : "");
            obj.put("createdByUserID", element[14]);
            obj.put("createdByUserName", element[15]);
            obj.put("createdByUserEmail", element[16]);
            obj.put("returnValidationProcessStatus", element[17]);

            if (GETRETURNVALIDATIONRESULTSUMMARY.equals(api) || GETRETURNVALIDATIONRESULTGROUPSUMMARY.equals(api)) {
                obj.put("returnPeriod", element[12] != null ? seletedPeriodValue(element[12].toString(), element[26] != null ? element[26].toString() : "") : "");
                obj.put("totalValidations", element[18]);
                obj.put("totalMandatoryValidations", element[19]);
                obj.put("totalOptionalValidations", element[20]);
                obj.put("totalValidationsPassed", element[21]);
                obj.put("totalErrors", element[22]);
                obj.put("totalWarnings", element[23]);
            }

            if (GETRETURNVALIDATIONRESULTGROUPSUMMARY.equals(api)) {
                obj.put("returnValidationGroup", element[24]);
            }

            if (GETRETURNVALIDATIONRESULTDETAILS.equals(api)) {
                obj.put("returnPeriod", element[12] != null ? seletedPeriodValue(element[12].toString(), element[37] != null ? element[37].toString() : "") : "");
                obj.put("returnValidationGroup", element[18]);
                obj.put("validationCode", element[19]);
                obj.put("validationName", element[20]);
                obj.put("validationDescription", element[21]);
                obj.put("validationType", element[22]);
                obj.put("validationStatus", element[23]);
                obj.put("validationExpression", element[24]);
                obj.put("returnSectionCode", element[25]);
                obj.put("returnSectionName", element[26]);
                obj.put("returnFormName", element[27]);
                obj.put("returnValidationCategory", element[28]);
                obj.put("totalOccurrences", element[29]);
                obj.put("totalOccurrencesPassCount", element[30]);
                obj.put("totalOccurrencesFailCount", element[31]);
                obj.put("totalOccurrenceRunningSum", element[32]);
                obj.put("validationId", element[33]);
                obj.put("runId", element[35]);
                obj.put("isCommentAtValidation", element[36]);
                obj.put("dimensionCSV", element[38]);
            }
            array.add(obj);
        }
        return array;
    }

    private JSONArray transformer(JSONArray array, String solution, String page, String rows) throws Throwable {
        JSONArray finalArray = new JSONArray();
        DataReader reader;
        TransformingReader transformingReader;
        CSVTransformer trans;
        String outputDirectory;
        File directory;
        String fileName;
        int rowLowerLimit = 0;
        int rowUpperLimit = 0;
        int previousFileLastRowCount = 0;
        String dimensionCSV;

        if ((page != null && page.trim().length() > 0) && (rows != null && rows.trim().length() > 0)) {
            rowLowerLimit = (Integer.parseInt(page) - 1) * Integer.parseInt(rows);
            rowUpperLimit = (Integer.parseInt(page)) * Integer.parseInt(rows);
        }
        if (array.size() > 0)
            previousFileLastRowCount = (Integer) (((JSONObject) array.get(0)).get("totalOccurrenceRunningSum")) - (Integer) (((JSONObject) array.get(0)).get("totalOccurrences"));

        DataReader lookupReader = new JdbcReader(PersistentStoreManager.getConnection(), "select * from VALIDATION_COMMENTS");
        Lookup lookup = new DataReaderLookup(lookupReader, new FieldList("PERIOD_ID", "REG_REPORT_ID", "VERSION_NO", "VALIDATION_ID", "OCCURRENCE"), new FieldList("COMMENT"));
        lookup = new CachedLookup(lookup, 10000);

        for (int i = 0; i < array.size(); i++) {
            outputDirectory = applicationProperties.getProperty("app.validations.outputDirectory").trim() + ((JSONObject) array.get(i)).get("runId") + File.separator;
            outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);
            directory = new File(outputDirectory);
            if (directory.exists()) {
                fileName = "Validation_Result_" + ((JSONObject) array.get(i)).get("runId") + "_" + ((JSONObject) array.get(i)).get("validationId");// + ".csv";

                reader = getJdbcReader(outputDirectory, fileName);
//				reader = new CSVReader(new File(outputDirectory + fileName)).setFieldSeparator(",").setTrimFields(false).setFieldNamesInFirstRow(true);

                transformingReader = new TransformingReader(reader);

                transformingReader.add(new BasicFieldTransformer("PERIOD_ID").stringToInt());
                transformingReader.add(new SetField("REG_REPORT_ID", ((JSONObject) array.get(i)).get("returnID")));
                transformingReader.add(new SetField("VALIDATION_ID", ((JSONObject) array.get(i)).get("validationId")));
                transformingReader.add(new SetField("IS_COMMENT_AT_VALIDATION", ((JSONObject) array.get(i)).get("isCommentAtValidation")));

                // if comment is at validation level, replace Hash Key with -1
                transformingReader.add(new Transformer() {
                    public boolean transform(Record record) throws Throwable {
                        if ("Y".equalsIgnoreCase(record.getField("IS_COMMENT_AT_VALIDATION").getValueAsString())) {
                            record.getField("Hash Key").setValue("-1");
                        }
                        return true;
                    }
                });

                transformingReader = new TransformingReader(transformingReader).add(new LookupTransformer(new FieldList("PERIOD_ID", "REG_REPORT_ID", "VERSION_NUMBER", "VALIDATION_ID", "Hash Key"), lookup));

                dimensionCSV = ((JSONObject) array.get(i)).get("dimensionCSV") != null ? ((JSONObject) array.get(i)).get("dimensionCSV").toString() : null;
                trans = new CSVTransformer((JSONObject) array.get(i), previousFileLastRowCount, rowLowerLimit, rowUpperLimit, dimensionCSV);
                reader = transformingReader.add(trans);

                Job.run(reader, new MemoryWriter());
                finalArray.addAll(trans.getArray());
                previousFileLastRowCount = trans.getRowCounter();

                if (previousFileLastRowCount > rowUpperLimit)
                    break;
            }
        }
        return finalArray;
    }

    private String seletedPeriodValue(String period, String format) {
        int year;
        String month = null;
        String quarter = null;
        String day = null;

        switch (format) {
            case YEAR:
            case Default:
                period = period.substring(0, 4);
                break;
            case QUARTER:
                month = period.substring(4, 6);
                switch (month) {
                    case "01":
                    case "02":
                    case "03":
                        quarter = "03";
                        break;
                    case "04":
                    case "05":
                    case "06":
                        quarter = "06";
                        break;
                    case "07":
                    case "08":
                    case "09":
                        quarter = "09";
                        break;
                    case "10":
                    case "11":
                    case "12":
                        quarter = "12";
                        break;
                }
                period = period.substring(0, 4) + quarter;
                break;
            case MONTH:
                year = Integer.parseInt(period.substring(0, 4));
                month = period.substring(4, 6);
                switch (month) {
                    case "01":
                    case "03":
                    case "05":
                    case "07":
                    case "08":
                    case "10":
                    case "12":
                        day = "31";
                        break;
                    case "04":
                    case "06":
                    case "09":
                    case "11":
                        day = "30";
                        break;
                    case "02":
                        day = String.valueOf(new Date(year, 2, 0).getDate());
                        break;
                }
                period = year + month + day;
                break;
        }

        return period;
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private JdbcReader getJdbcReader(String outputDirectory, String fileName) {
        String schema = "S_" + System.currentTimeMillis() + "_S";
        Connection conn = null;
        try {
            conn = CalciteConnectionManager.getCalciteConnection(outputDirectory, schema);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return new JdbcReader(conn, "select * from " + schema + "." + fileName).setAutoCloseConnection(Boolean.TRUE);
    }
}
