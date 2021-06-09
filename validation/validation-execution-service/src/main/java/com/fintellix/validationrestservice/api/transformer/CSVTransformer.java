package com.fintellix.validationrestservice.api.transformer;

import com.northconcepts.datapipeline.core.Record;
import com.northconcepts.datapipeline.transform.Transformer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVTransformer extends Transformer {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private JSONObject staticFields;
    private JSONArray array = new JSONArray();
    private int rowCounter;
    private int rowLowerLimit;
    private int rowUpperLimit;
    private String dimensionCSV;

    public CSVTransformer(JSONObject staticFields, int rowCounter, int rowLowerLimit, int rowUpperLimit, String dimensionCSV) {
        this.staticFields = staticFields;
        this.rowCounter = rowCounter;
        this.rowLowerLimit = rowLowerLimit;
        this.rowUpperLimit = rowUpperLimit;
        this.dimensionCSV = dimensionCSV;
    }

    public JSONArray getArray() {
        return array;
    }

    public int getRowCounter() {
        return rowCounter;
    }

    @Override
    public boolean transform(Record record) throws Throwable {
        rowCounter += 1;
        if (rowCounter > rowLowerLimit && rowCounter <= rowUpperLimit) {
            staticFields.remove("totalOccurrenceRunningSum");
            staticFields.remove("validationId");
            staticFields.remove("runId");
            //staticFields.put("occurenceKey", record.getField("Hash Key").getValueAsString());
            staticFields.put("dimensionName", dimensionCSV != null ? dimensionCSV : "");
            staticFields.put("dimensionValue", record.getField("GROUP_BY_DIMENSION") != null ? record.getField("GROUP_BY_DIMENSION").getValueAsString() : "");
            staticFields.put("evaluatedExpression", record.getField("Evaluated Expression").getValueAsString());
            staticFields.put("occurenceValidationStatus", record.getField("Evaluation Message").getValueAsString());
            staticFields.put("Comment", record.getField("COMMENT").getValueAsString());
            array.add(this.staticFields);
        }
        return true;
    }
}
