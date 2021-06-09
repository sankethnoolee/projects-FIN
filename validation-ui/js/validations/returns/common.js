var isView=null;
var functionsAndOperatorArray = [{
	"name": "FOREACH",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "FOREACH(RTN.<ReturnBkey>.<SectionDesc>,[GroupByColumnNames],[FilterConditions]){ <Expression> }",
	"internalValue":"FOREACH(){}",
	"expressionFormatAuto": "FOREACH(RTN.ReturnBkey.SectionDesc, GroupByColumnNames, [FilterConditions])",
	"include":"Y"
}, {
	"name": "SUM",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "SUM(<ColumnName>, [<GroupbyColumns>])",
	"internalValue":"SUM()",
	"expressionFormatAuto": "SUM(ColumnName, GroupbyColumns)",
	"include":"Y"
}, {
	"name": "SUMIF",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "SUMIF(<ColumnName>, [<GroupbyColumns>], [<FilterConditions>])",
	"internalValue":"TODATE()",
	"expressionFormatAuto": "SUMIF(ColumnName, GroupbyColumns, FilterConditions)",
	"include":"Y"
},{
	"name": "AND",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "AND(<Condition1>,<Condition2>...<ConditionN>)",
	"internalValue":"AND()",
	"expressionFormatAuto": "AND(Condition1, Condition2, ConditionN)",
	"include":"Y"
}, {
	"name": "OR",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "OR(<Condition1>,<Condition2>...<ConditionN>)",
	"internalValue":"OR()",
	"expressionFormatAuto": "OR(Condition1, Condition2, ConditionN)",
	"include":"Y"
},{
	"name": "IF()THEN()ELSE()",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "IF(Condition1...ConditionN)THEN(Condition1...ConditionN)ELSE(Condition1...ConditionN)",
	"internalValue":"IF()THEN()ELSE()",
	"expressionFormatAuto": "IF(Condition)THEN(Condition)ELSE(Condition)",
	"include":"Y"
},{
	"name": "ISEMPTY",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "ISEMPTY(<ColumnName>)",
	"internalValue":"ISEMPTY()",
	"expressionFormatAuto": "ISEMPTY(ColumnName)",
	"include":"Y"
}, {
	"name": "MAX",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "MAX(<ColumnName>, [<GroupbyColumns>])",
	"internalValue":"TODATE()",
	"expressionFormatAuto": "MAX(ColumnName, [GroupbyColumns])",
	"include":"Y"
}, {
	"name": "MIN",
	"category": "Functions",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "MIN(<ColumnName>, [<GroupbyColumns>])",
	"internalValue":"MIN()",
	"expressionFormatAuto": "MIN(ColumnName, [GroupbyColumns])",
	"include":"Y"
},{
	"name": "IN",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "<ColumnToBeEvaluated> IN [ListOfValues]",
	"internalValue":"IN()",
	"expressionFormatAuto": "IN([ListOfValues])",
	"include":"Y"
},{
	"name": "NOTIN",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "<ColumnToBeEvaluated> NOTIN [ListOfValues]",
	"internalValue":"NOTIN[]",
	"expressionFormatAuto": "NOTIN([ListOfValues])",
	"include":"Y"
}, {
	"name": "CONTAINS",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "<ColumnToBeEvaluated> CONTAINS [ListOfValues]",
	"internalValue":"CONTAINS[]",
	"expressionFormatAuto": "CONTAINS([ListOfValues])",
	"include":"Y"
}, {
	"name": "BEGINSWITH",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "<ColumnToBeEvaluated> BEGINSWITH [ListOfValues]",
	"internalValue":"BEGINSWITH[]",
	"expressionFormatAuto": "BEGINSWITH([ListOfValues])",
	"include":"Y"
}, {
	"name": "ENDSWITH",
	"category": "Operator",
	"subCategory": "COMMONLY USED",
	"expressionFormat": "<ColumnToBeEvaluated> ENDSWITH [ListOfValues]",
	"internalValue":"ENDSWITH[]",
	"expressionFormatAuto": "ENDSWITH([ListOfValues])",
	"include":"Y"
},
{
	"name": "SUBSTR",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "SUBSTR(<ColumnName>,<StartNumber>,<NumberOfCharacters>) ",
	"internalValue":"SUBSTR()",
	"expressionFormatAuto": "SUBSTR(ColumnName, StartNumber, [NumberOfCharacters])",
	"include":"Y"
}, {
	"name": "LOWER",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "LOWER(<ColumnName>)",
	"internalValue":"LOWER()",
	"expressionFormatAuto":"LOWER(ColumnName)",
	"include":"Y"
}, {
	"name": "UPPER",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "UPPER(<ColumnName>)",
	"internalValue":"UPPER()", 
	"expressionFormatAuto":"UPPER(ColumnName)",
	"include":"Y"
}, {
	"name": "LEN",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "LEN(<ColumnName>)",
	"internalValue":"LEN()",
	"expressionFormatAuto":"LEN(ColumnName)",
	"include":"Y"
}, {
	"name": "CONVERT",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "CONVERT(<FieldName>,<toWhichDataType>)",
	"internalValue":"CONVERT()",
	"expressionFormatAuto": "CONVERT(FieldName, toWhichDataType)",
	"include":"Y"
}, {
	"name": "REGEX",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "REGEX(<ColumnName>,<Pattern>)",
	"internalValue":"REGEX()",
	"expressionFormatAuto": "REGEX(ColumnName, Pattern)",
	"include":"Y"
}, {
	"name": "CONCAT",
	"category": "Functions",
	"subCategory": "TEXT",
	"expressionFormat": "Concat(<ColumnName/Value1>,<ColumnName/Value2>…n)",
	"internalValue":"REGEX()",
	"expressionFormatAuto": "Concat(Value1, Value2, ...N)",
	"include":"Y"
}, {
	"name": "SOM",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "SOM(<DateField>)",
	"internalValue":"SOM()",
	"expressionFormatAuto": "SOM(DateField)",
	"include":"Y"
}, {
	"name": "EOM",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "EOM(<DateField>)",
	"internalValue":"EOM()",
	"expressionFormatAuto": "EOM(DateField)",
	"include":"Y"
}, {
	"name": "SOY",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "SOY(<DateField>)",
	"internalValue":"SOY()",
	"expressionFormatAuto": "SOY(DateField)",
	"include":"Y"
}, {
	"name": "EOY",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "EOY(<DateField>)",
	"internalValue":"EOY()",
	"expressionFormatAuto": "EOY(DateField)",
	"include":"Y"
}, {
	"name": "SOFY",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "SOFY(<DateField>)",
	"internalValue":"SOFY()",
	"expressionFormatAuto": "SOFY(DateField)",
	"include":"Y"
}, {
	"name": "EOFY",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "EOFY(<DateField>)",
	"internalValue":"EOFY()",
	"expressionFormatAuto": "EOFY(DateField)",
	"include":"Y"
}, {
	"name": "DATEPART",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "DATEPART (<DateField>,<DatePartToBeExtracted>)",
	"internalValue":"DATEPART()",
	"expressionFormatAuto": "DATEPART (DateField, DatePartToBeExtracted)",
	"include":"Y"
}, {
	"name": "DATEDIFF",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "DATEDIFF( <DateField1>,<DateField2>,<Units>)",
	"internalValue":"DATEDIFF()",
	"expressionFormatAuto": "DATEDIFF(DateField1, DateField2, Units)",
	"include":"Y"
}, {
	"name": "TODATE",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "TODATE(<ValueToBeConvertedToDateFormat>)",
	"internalValue":"TODATE()",
	"expressionFormatAuto": "TODATE(ValueToBeConvertedToDateFormat)",
	"include":"Y"
}, {
	"name": "PERIOD",
	"category": "Functions",
	"subCategory": "DATE & TIME",
	"expressionFormat": "PERIOD(TimeBasis, Offset, <Date>)",
	"internalValue":"PERIOD()",
	"expressionFormatAuto": "PERIOD(TimeBasis, Offset, Date)",
	"include":"Y"
}, {
	"name": "ADD",
	"category": "Operator",
	"subCategory": "NUMERIC OPERATORS",
	"expressionFormat": "<NumericalField1> + <NumericalField2> +...+<NumericalFieldN>",
	"internalValue":"+",
	"include":"N"
}, {
	"name": "SUBTRACT",
	"category": "Operator",
	"subCategory": "NUMERIC OPERATORS",
	"expressionFormat": "<NumericalField1> - <NumericalField2> -...- <NumericalFieldN>",
	"internalValue":"-",
	"include":"N"
}, {
	"name": "DIVIDE",
	"category": "Operator",
	"subCategory": "NUMERIC OPERATORS",
	"expressionFormat": "<NumericalField1> / <NumericalField2> ",
	"internalValue":"/",
	"include":"N"
}, {
	"name": "MULTIPLY",
	"category": "Operator",
	"subCategory": "NUMERIC OPERATORS",
	"expressionFormat": "<NumericalField1> * <NumericalField2> *...* <NumericalFieldN>",
	"internalValue":"*",
	"include":"N"
}, {
	"name": "EQUAL",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression1> == <Expression2>",
	"internalValue":"==",
	"include":"N"
}, {
	"name": "NOT EQUAL",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression1> != <Expression2>",
	"internalValue":"!=",
	"include":"N"
}, {
	"name": "GREATER THAN",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression1> > <Expression2>",
	"internalValue":">",
	"include":"N"
}, {
	"name": "GREATER THAN EQUAL",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression1> >= <Expression2>",
	"internalValue":">=",
	"include":"N"
}, {
	"name": "LESSER THAN",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression1> < <Expression2>",
	"internalValue":"<",
	"include":"N"
}, {
	"name": "LESSER THAN EQUAL",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression1> <= <Expression2>",
	"internalValue":"<=",
	"include":"N"
}, {
	"name": "BETWEEN",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<Expression> BETWEEN [<LowerLimit>,<UpperLimit>]",
	"internalValue":"BETWEEN[]",
	"expressionFormatAuto": "BETWEEN [LowerLimit, UpperLimit]",
	"include":"Y"
}, {
	"name": "AND",
	"category": "Operator",
	"subCategory": "LOGICAL OPERATORS",
	"expressionFormat": "AND(<Condition1>,<Condition2>...<ConditionN>)",
	"internalValue":"AND()",
	"expressionFormatAuto": "AND(Condition1, Condition2, ConditionN)",
	"include":"Y"
}, {
	"name": "OR",
	"category": "Operator",
	"subCategory": "LOGICAL OPERATORS",
	"expressionFormat": "OR(<Condition1>,<Condition2>...<ConditionN>)",
	"internalValue":"OR()",
	"expressionFormatAuto": "OR(Condition1, Condition2, ConditionN)",
	"include":"Y"
}, {
	"name": "NOT",
	"category": "Operator",
	"subCategory": "LOGICAL OPERATORS",
	"expressionFormat": "NOT(<Condition>)",
	"internalValue":"NOT()",
	"expressionFormatAuto": "NOT(Condition)",
	"include":"Y"
}, {
	"name": "IN",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<ColumnToBeEvaluated> IN [ListOfValues]",
	"internalValue":"IN[]",
	"expressionFormatAuto": "IN([ListOfValues])",
	"include":"Y"
}, {
	"name": "NOTIN",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<ColumnToBeEvaluated> NOTIN [ListOfValues]",
	"internalValue":"NOTIN[]",
	"expressionFormatAuto": "NOTIN([ListOfValues])",
	"include":"Y"
}, {
	"name": "CONTAINS",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<ColumnToBeEvaluated> CONTAINS [ListOfValues]",
	"internalValue":"CONTAINS[]",
	"expressionFormatAuto": "CONTAINS([ListOfValues])",
	"include":"Y"
}, {
	"name": "BEGINSWITH",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<ColumnToBeEvaluated> BEGINSWITH [ListOfValues]",
	"internalValue":"BEGINSWITH[]",
	"expressionFormatAuto": "BEGINSWITH([ListOfValues])",
	"include":"Y"
}, {
	"name": "ENDSWITH",
	"category": "Operator",
	"subCategory": "COMPARISION",
	"expressionFormat": "<ColumnToBeEvaluated> ENDSWITH [ListOfValues]",
	"internalValue":"ENDSWITH[]",
	"expressionFormatAuto": "ENDSWITH([ListOfValues])",
	"include":"Y"
},{
	"name": "ISEMPTY",
	"category": "Functions",
	"subCategory": "LOGICAL",
	"expressionFormat": "ISEMPTY(<ColumnName>)",
	"internalValue":"ISEMPTY()",
	"expressionFormatAuto": "ISEMPTY(ColumnName)",
	"include":"Y"
}, {
	"name": "ISNOTEMPTY",
	"category": "Functions",
	"subCategory": "LOGICAL",
	"expressionFormat": "ISNOTEMPTY(<ColumnName>)",
	"internalValue":"ISNOTEMPTY()",
	"expressionFormatAuto": "ISNOTEMPTY(ColumnName)",
	"include":"Y"
}, {
	"name": "UNIQUE",
	"category": "Functions",
	"subCategory": "LOGICAL",
	"expressionFormat": "UNIQUE(<ReturnSectionName>, [<ColumnName1><ColumnName2>...<ColumnN>], [GroupbyColumns],[FilterConditions])",
	"internalValue":"UNIQUE()",
	"expressionFormatAuto": "UNIQUE(ReturnSectionName, [ColumnNames], [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "SUM",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "SUM(<ColumnName>, [<GroupbyColumns>])",
	"internalValue":"SUM()",
	"expressionFormatAuto": "SUM(ColumnName, [GroupbyColumns])",
	"include":"Y"
}, {
	"name": "SUMIF",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "SUMIF(<ColumnName>, [<GroupbyColumns>], [<FilterConditions>])",
	"internalValue":"TODATE()",
	"expressionFormatAuto": "SUMIF(ColumnName, [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "MAX",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "MAX(<ColumnName>, [<GroupbyColumns>])",
	"internalValue":"TODATE()",
	"expressionFormatAuto": "MAX(ColumnName, [GroupbyColumns])",
	"include":"Y"
}, {
	"name": "MAXIF",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "MAXIF(<ColumnName>, [<GroupbyColumns>], [<FilterConditions>])",
	"internalValue":"MAXIF()",
	"expressionFormatAuto": "MAXIF(ColumnName, [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "MIN",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "MIN(<ColumnName>, [<GroupbyColumns>])",
	"internalValue":"MIN()",
	"expressionFormatAuto": "MIN(ColumnName, [GroupbyColumns])",
	"include":"Y"
}, {
	"name": "MINIF",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "MINIF(<ColumnName>, [<GroupbyColumns>], [<FilterConditions>])",
	"internalValue":"MINIF()",
	"expressionFormatAuto": "MINIF(ColumnName, [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "COUNT",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "COUNT(<ReturnSectionName>, [<ColumnName1><ColumnName2>...<ColumnN>], [GroupbyColumns])",
	"internalValue":"COUNT()",
	"expressionFormatAuto": "COUNT(ReturnSectionName, [ColumnNames], [GroupbyColumns])",
	"include":"Y"
}, {
	"name": "COUNTIF",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "COUNTIF(<ReturnSectionName>, [<ColumnName1><ColumnName2>...<ColumnN>], [GroupbyColumns], [FilterConditions])",
	"internalValue":"COUNT()",
	"expressionFormatAuto": "COUNTIF(ReturnSectionName, [ColumnNames], [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "DCOUNT",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "DCOUNT(<ReturnSectionName>, [<ColumnName1><ColumnName2>...<ColumnN>], [GroupbyColumns], [FilterConditions])",
	"internalValue":"DCOUNT()",
	"expressionFormatAuto": "DCOUNT(ReturnSectionName, [ColumnNames], [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "AVG",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "AVG(<ColumnName>, [<GroupbyColumns>], [<FilterConditions>])",
	"internalValue":"AVG()",
	"expressionFormatAuto": "AVG(ColumnName, [GroupbyColumns], [FilterConditions])",
	"include":"Y"
}, {
	"name": "ROUND",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "ROUND(<ColumnName/Value>,<NoOfDecimals>)",
	"internalValue":"ROUND()",
	"expressionFormatAuto": "ROUND(ColumnName/Value, NoOfDecimals)",
	"include":"Y"
}, {
	"name": "ABS",
	"category": "Functions",
	"subCategory": "MATH",
	"expressionFormat": "ABS(<ColumnName/Value>)",
	"internalValue":"ABS()",
	"include":"N"
}, {
	"name": "VLOOKUP",
	"category": "Functions",
	"subCategory": "LOOKUP & REFERENCE",
	"expressionFormat": "VLOOKUP (<SourceDataSet>,<TargetDataset>,<JoinConditions>, <TargetOutputColumn>,<FilterConditions>)",
	"internalValue":"VLOOKUP()",
	"expressionFormatAuto": "VLOOKUP (SourceDataSet, TargetDataset, JoinConditions, TargetOutputColumn, FilterConditions)",
	"include":"Y"
}, {
	"name": "FOREACH",
	"category": "Functions",
	"subCategory": "LOOKUP & REFERENCE",
	"expressionFormat": "FOREACH(RTN.<ReturnBkey>.<SectionDesc>,[GroupByColumnNames],[FilterConditions]){ <Expression> }",
	"internalValue":"FOREACH(){}",
	"expressionFormatAuto": "FOREACH(RTN.ReturnBkey.SectionDesc, [GroupByColumnNames], [FilterConditions])",
	"include":"Y"
}];
