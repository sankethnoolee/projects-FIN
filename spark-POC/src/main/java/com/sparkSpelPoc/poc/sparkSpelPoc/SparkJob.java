package com.sparkSpelPoc.poc.sparkSpelPoc;

import static com.sparkSpelPoc.poc.sparkSpelPoc.config.CustomConstants.*;
import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.col;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.SparkFiles;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.execution.datasources.FileFormat;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.sparkSpelPoc.poc.sparkSpelPoc.domain.FileInputLine;
import com.sparkSpelPoc.poc.sparkSpelPoc.exceptions.ValidationException;
import com.sparkSpelPoc.poc.sparkSpelPoc.expressionUtil.ExpressionProcessor;
import com.sparkSpelPoc.poc.sparkSpelPoc.utils.FileUtil;
import com.sparkSpelPoc.poc.sparkSpelPoc.utils.SQLUtil;
import com.sparkSpelPoc.poc.sparkSpelPoc.utils.SpelUtil;
import com.sparkSpelPoc.poc.sparkSpelPoc.utils.UDFUtil;

import scala.Tuple2;

/**
 * @author Sanketh Noolee
 *
 */
class SparkJob {

	private String[] args;

	private final Logger logger = Logger.getLogger(SparkJob.class);

	private String sparkMaster; 
	

	private JavaSparkContext javaSparkContext;
	private SQLContext sqlContext;
	private SparkSession sparkSession;

	private UDFUtil udfUtil;
	private FileUtil fileUtil;
	private SpelUtil spelUtil;
	private SQLUtil sqlUtil;
	private ExpressionProcessor expressionProcessor;
	private  Map<Integer, String> valIdsExpressionMap;
	private  Map<Integer, String> valIdsColumnsUsedMap;


	SparkJob(String[] _args, Map<Integer, String> _valIdsExpressionMap, Map<Integer, String> _valIdsColumnsUsedMap) {
		this.args = _args;
		this.valIdsExpressionMap = _valIdsExpressionMap;
		this.valIdsColumnsUsedMap = _valIdsColumnsUsedMap;
	}

	void startJob() throws ValidationException, IOException {

		/*
        Validate to check if we have all the required arguments.
		 */
		//TODO


		//validateArguments();

		/*
        Load all the properties from the .properties file and initialize the instance variables.
		 */
		loadProperties();

		/*
        Register the required UDFs.
		 */
		registerUdfs();

		/*
        Get the dataset from the input file.
		 */
		//using dto to map
		System.out.println("sanketh_____"+new Date().getTime());
		Dataset<Row> inputFileDataset = //fileUtil.getDatasetFromFileGeneric(inputFilePath);
		sqlUtil.getDatasetFromSQL("(SELECT  EX_5873477174204979409 as EX_16505831853529111673,EX_15785297445060410556 as EX_9881565075046590856,EX_370080051702586760 as EX_538497545781638284,SUM(EX_7871099884844905310) " + 
				"as EX_1187551083090087214  FROM  ( SELECT EX_7871099884844905310,EX_8704408192289855630,EX_5873477174204979409,EX_15785297445060410556,EX_370080051702586760 FROM ( SELECT " + 
				"PNC_SB_81.AD_active_accounts AS EX_7871099884844905310,runid.RUNDESC AS EX_8704408192289855630,PNC_SB_81.AD_product AS EX_5873477174204979409,PNC_SB_81.month AS EX_15785297445060410556,PNC_SB_81.vintageName AS EX_370080051702586760 from  PNC_SB_81 PNC_SB_81  left outer join DIM_PNC_SB_81_RUN                                                                                                                runid on PNC_SB_81.runid=runid.RUNID  ) IEX_8433014227906308048 WHERE ( EX_8704408192289855630  IN ('R-202133-81-122612')) ) T1  GROUP BY EX_5873477174204979409,EX_15785297445060410556,EX_370080051702586760" + 
				") as test");
		inputFileDataset.createOrReplaceTempView("dp");
		
		
		/*
		
		Dataset<Row> test = sqlContext.sql("select row_number() over ( order by "+"16505831853529111673"+") as serialNumber,* from dp" )
		.toDF();
		//test.first();
		//System.out.println("sanketh_____"+new Date().getTime());
		test.collect();
		System.out.println("sanketh_____"+new Date().getTime());
		
		*/
		
		/* dynamic sorting at code
		Column[] sort = new Column[2];
		sort[0] = new Column("spelExpr").desc_nulls_last();
		sort[1] = new Column("srno").asc_nulls_last();
		inputFileDataset.show();
		WindowSpec w = Window.orderBy(sort);
		w.orderBy(inputFileDataset.col("srno").desc_nulls_last());
		*/
		
		
		/*
		 * 
		 * 
		System.out.println("sanketh_____"+new Date().getTime());
		JavaPairRDD<Row, Long> rddzip  = inputFileDataset.orderBy("EX_16505831853529111673").javaRDD().zipWithIndex();
		JavaRDD rdd = rddzip.map(s->{
            Row r = s._1;
            Object[] arr = new Object[r.size()+1];
            for (int i = 0; i < arr.length-1; i++) {
                arr[i] = r.get(i);
            }
            arr[arr.length-1] = s._2;
            return RowFactory.create(arr);
        });

        StructType newSchema = inputFileDataset.schema().add(new StructField("rowid",
                DataTypes.LongType, false, Metadata.empty()));

        Dataset<Row> df2 = sqlContext.createDataFrame(rdd,newSchema);
        df2.collect();
        System.out.println("sanketh_____"+new Date().getTime()); 
        */
        
		
		
		/*
		 Dataset<Row> df2 = inputFileDataset.withColumn("serialNumber", functions.row_number()
					.over((Window.orderBy(inputFileDataset.col("EX_16505831853529111673").desc_nulls_last()))));
	        df2.collect();
	        System.out.println("sanketh_____"+new Date().getTime());
        df2.show();
        
        
        */
        
		
		Dataset<Row> df2 = inputFileDataset.withColumn("serialNumber", functions.monotonicallyIncreasingId());
        df2.collect();
        System.out.println("sanketh_____"+new Date().getTime());
        df2.show();
		
      //Dataset<Row> test = inputFileDataset.withColumn("serialNumber", functions.row_number()
		//	.over(w));
		//test.show();		
		//inputFileDataset.write().parquet("hdfs://blrdevapp038.fintellix.com:9000//opt/Fintellix/ADEPT_314/ADEPT_DEV/ValidationResults/test/");


		/*Dataset<Row> doubledColumnDataset = inputFileDataset.withColumn(DOUBLED_COLUMN_NAME,
                callUDF(COLUMN_DOUBLE_UDF_NAME, col(NUMBER_COLUMN_NAME)));

        doubledColumnDataset.show();

        Dataset<Row> upperCaseColumnDataset = doubledColumnDataset.withColumn(UPPSERCASE_NAME_COLUMN_NAME,
                callUDF(COLUMN_UPPERCASE_UDF_NAME, col(NAME_COLUMN_NAME)));

        upperCaseColumnDataset.show();*/

		/*sample spel testin block

		//using generic

		Dataset<Row> inputFileDataset = fileUtil.getDatasetFromFileGeneric(inputFilePath);
		inputFileDataset.show();


		Dataset<Row> spelEvaluatedDataSet = inputFileDataset.withColumn(SPEL_EVAL_COLUMN_NAME,
				callUDF(SPEL_EVAL_UDF_NAME, col(SPEL_COLUMN_NAME)));

		spelEvaluatedDataSet.show();
		//spelEvaluatedDataSet.write().mode(SaveMode.Overwrite).csv("D:\\sparksampledata\\output");
		spelEvaluatedDataSet.write().mode(SaveMode.Overwrite)
		.format("com.databricks.spark.csv")
		.option("header", "true")
		.save("D:\\sparksampledata\\output\\test");*/

		//reading from db
		/*Dataset<Row> sqlDataset = sqlUtil.getDatasetFromSQL("(select * from C_DIRECTORY) as test");
		sqlDataset.show();
		Dataset<Row> expressionCol = sqlDataset
				.withColumn("expression",functions.lit("DIRECTORY_NAME==DIRECTORY_DESC"));

		Dataset<Row> upperCaseColumnDataset =
				expressionCol.map(expressionProcessor.mf,RowEncoder.apply(expressionCol.schema()))
				.withColumn(SPEL_EVAL_COLUMN_NAME,
						callUDF(SPEL_EVAL_UDF_NAME, col("expression")));

		upperCaseColumnDataset.show();
		upperCaseColumnDataset.write().parquet("C:\\Users\\i21156\\Desktop\\ss\\sanketh.parquet");*/
		//System.out.println(upperCaseColumnDataset.rdd().getNumPartitions()+"--------------------------");
	}


	private void loadProperties() throws IOException {
		Properties properties = new Properties();
		String propFileName = "application.properties";

		InputStream inputStream = App.class.getClassLoader().getResourceAsStream(propFileName);

		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw e;
		}

		initialize(properties);
	}

	private void registerUdfs() {

		this.udfUtil.registerColumnDoubleUdf();
		this.udfUtil.registerColumnUppercaseUdf();
		this.spelUtil.registerSpelEvalUdf();
	}

	private void initialize(Properties properties) {
		
		sparkMaster = properties.getProperty("spark.master");
		SparkSession spark = SparkSession.builder()
	            .appName("Databricks spark example").master("local[*,2]")
	            .getOrCreate();
		
		javaSparkContext = JavaSparkContext.fromSparkContext(spark.sparkContext());
		sqlContext = new SQLContext(javaSparkContext);
		sparkSession = sqlContext.sparkSession();
		

		udfUtil = new UDFUtil(sqlContext);
		fileUtil = new FileUtil(sparkSession);
		spelUtil = new SpelUtil(sqlContext);
		sqlUtil = new SQLUtil(sqlContext);
		expressionProcessor = new ExpressionProcessor(valIdsExpressionMap, valIdsColumnsUsedMap);
	}

	private void validateArguments() throws ValidationException {

		if (args.length < 1) {
			logger.error("Invalid arguments.");
			logger.error("1. Input file path.");
			logger.error("Example: java -jar <jarFileName.jar> /path/to/input/file");

			throw new ValidationException("Invalid arguments, check help text for instructions.");
		}
	}

	private JavaSparkContext createJavaSparkContext() {

		/* Create the SparkSession.
		 * If config arguments are passed from the command line using --conf,
		 * parse args for the values to set.
		 */

		SparkConf conf = new SparkConf();

		return new JavaSparkContext(conf);
	}
}
