package com.fintellix.framework.validation.customtransformer;

import com.northconcepts.datapipeline.core.FieldList;
import com.northconcepts.datapipeline.core.Record;
import com.northconcepts.datapipeline.core.RecordList;
import com.northconcepts.datapipeline.transform.lookup.Lookup;
import com.northconcepts.datapipeline.transform.lookup.LookupTransformer;

import java.util.List;

public class LookupValidationTransformer extends LookupTransformer {

    public LookupValidationTransformer(FieldList fields, Lookup lookup) {
        super(fields, lookup);
    }

    @Override
    protected void join(Record originalRecord, RecordList lookupResult, List<?> lookupArguments) {
        if (lookupArguments.contains(null)) {
            noResults(originalRecord, lookupArguments);
        } else {
            super.join(originalRecord, lookupResult, lookupArguments);
        }
    }

    @Override
    protected RecordList noResults(Record originalRecord, List<?> arguments) {
        originalRecord.setField("lineNumber", "NA");
        return new RecordList();
    }

    @Override
    protected RecordList tooManyResults(Record originalRecord, List<?> arguments, RecordList lookupResult) {
        RecordList list = new RecordList();
        for (Record record : lookupResult)
            list.add(record);
        return list;
    }
}
