package com.fintellix.framework;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkFiles;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import com.fintellix.framework.dto.ExpressionStatus;
import com.fintellix.framework.dto.ValidationExecutionGroup;
import com.fintellix.framework.dto.ValidationExecutionGroups;
import com.fintellix.framework.exceptions.ValidationException;
import com.fintellix.framework.expressionUtil.ExpressionProcessor;
import com.fintellix.framework.utils.FileUtil;
import com.fintellix.framework.utils.SQLUtil;
import com.fintellix.framework.utils.SpelUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author Sanketh Noolee
 *
 */
public class SparkJob {
	/*
	 * TODO refactor and expression replacement logic
	 * */
	private static Properties applicationProperties;
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

	private final Logger logger = Logger.getLogger(SparkJob.class);

	private String sparkMaster; 
	private String outputParent;
	private String jsonPath;
	private String validationGroupJsonName;
	private JavaSparkContext javaSparkContext;
	private SQLContext sqlContext;
	private SparkSession sparkSession;

	private FileUtil fileUtil;
	private SpelUtil spelUtil;
	private SQLUtil sqlUtil;
	private ExpressionProcessor expressionProcessor;
	private ValidationExecutionGroups validationExecutionGroups;

	private String[] args;

	SparkJob(String[] _args) throws JsonSyntaxException, JsonIOException, IOException {
		this.args = _args;

	}

	public void startJob() throws Throwable {
		/*
		 * validate args
		 * */
		validateArguments();


		/*
	       initialize the instance variables.
		 */
		initialize();

		/*
	        Register the required UDFs.
		 */
		registerUdfs();

		/*
        	Register the required helpers.
		 */
		loadHelpers();

		/*
		 * individual folders per csv uncomment below method
		 * */

		//writeEachValidationCSV();

		/*
		 * new columns for each validation
		 * */
		writeToOneCSV();
	}


	private void registerUdfs() {
		spelUtil = spelUtil==null?new SpelUtil(sqlContext):spelUtil;
		this.spelUtil.registerSpelEvalUdf();
	}

	private void initialize() {

		sparkMaster = applicationProperties.getProperty("spark.master")==null?"": applicationProperties.getProperty("spark.master").trim();

		javaSparkContext = createJavaSparkContext();
		sqlContext = new SQLContext(javaSparkContext);
		sparkSession = sqlContext.sparkSession();

	}


	private JavaSparkContext createJavaSparkContext() {

		/* Create the SparkSession.
		 * If config arguments are passed from the command line using --conf,
		 * parse args for the values to set.
		 */

		SparkConf conf = new SparkConf();

		return new JavaSparkContext(conf);
	}

	private void loadHelpers() throws Throwable {
		fileUtil = fileUtil==null?new FileUtil(sparkSession):fileUtil;
		sqlUtil = sqlUtil==null?new SQLUtil(sqlContext,args):sqlUtil;
		
		outputParent = javaSparkContext.getConf().get("spark.custom.app.validations.outputDirectory").trim();
		jsonPath = javaSparkContext.getConf().get("spark.custom.app.spark.filePath.validationGroups").trim();
		validationGroupJsonName= javaSparkContext.getConf().get("spark.custom.app.spark.filePath.validationGroupsFileName").trim();
		Gson gson = new GsonBuilder().create();
		this.validationExecutionGroups = gson.fromJson	(new FileReader(this.args[0]), ValidationExecutionGroups.class);

	}

	private void validateArguments() throws ValidationException {

		if (args.length < 1) {
			logger.error("Invalid arguments.");
			logger.error("1. Input file path.");
			logger.error("Example: java -jar <jarFileName.jar> dto(in json string format)");

			throw new ValidationException("Invalid arguments, check help text for instructions.");
		}
	}

	private void writeEachValidationCSV(){
		//reading from db
		Map<Integer,Dataset<Row>> map = new HashMap<Integer,Dataset<Row>>();
		Dataset<Row> opDataset = null;
		
		for(ValidationExecutionGroup vg: validationExecutionGroups.getValidationExecutionGroups()) {

			Dataset<Row> sqlDataset = sqlUtil.getDatasetFromSQL("("+vg.getQuery()+" )");

			List <ExpressionStatus> em=new ArrayList<ExpressionStatus>();
			em.addAll(vg.getExpressionMap().values());
			Dataset<Row> expressionCol;

			ExpressionProcessor ep;
			expressionCol = sqlDataset
					.withColumn("validationId",functions.lit(-1))
					.withColumn("expression",functions.lit(""))
					.withColumn("validationResult",functions.lit(""));
			for(ExpressionStatus e : em) {

				ep = new ExpressionProcessor(e);

				map.put(e.getExprId(), expressionCol.map(ep.mf,RowEncoder.apply(expressionCol.schema())));



				/*
				 * opDataset.write().mode(SaveMode.Overwrite)
				 * .format("com.databricks.spark.csv") .option("header", "true") .save(
				 * "D:\\sparksampledata\\output\\validationResult\\"+validationExecutionGroups.getRunId()+"
				 * \\"+e.getExprId());
				 */
			}


			//sqlDataset.show();
		}

		for(Integer i : map.keySet()) {
			//opDataset = opDataset==null?map.get(i):opDataset.unionAll(map.get(i));
			map.get(i).write().mode(SaveMode.Overwrite)
			.format("com.databricks.spark.csv") .option("header", "true") .save(
					outputParent+validationExecutionGroups.getRunId()+"\\"+
							i);


		}
		/*
		 * opDataset.write().mode(SaveMode.Overwrite)
		 * .format("com.databricks.spark.csv") .option("header", "true") .save(
		 * "D:\\sparksampledata\\output\\validationResult\\"+validationExecutionGroups.getRunId()+"
		 * \\"+"test");
		 */

	}

	private void writeToOneCSV() {
		System.out.println("root-------->"+SparkFiles.getRootDirectory());
		//reading from db
		Integer groupCount = 1;
		ValidationExecutionGroups vgs = new ValidationExecutionGroups();
		for(ValidationExecutionGroup vg: validationExecutionGroups.getValidationExecutionGroups()) {
			vg.setGroupName("Group-"+groupCount);
			groupCount++;
			Dataset<Row> sqlDataset = sqlUtil.getDatasetFromSQL("("+vg.getQuery()+" )");

			List <ExpressionStatus> em=new ArrayList<ExpressionStatus>();
			em.addAll(vg.getExpressionMap().values());
			Dataset<Row> expressionCol = null;
			expressionCol = sqlDataset.withColumn("RowResult",functions.lit("TRUE"));
			for(ExpressionStatus e : em) {
				//test.add("validationId_"+e.getExprId(), DataTypes.IntegerType); 
				expressionCol = expressionCol== null ? sqlDataset
						.withColumn("expression_"+e.getExprId(),functions.lit(e.getSpelExpression()))
						.withColumn("validationResult_"+e.getExprId(),functions.lit(""))
						: expressionCol
						.withColumn("expression_"+e.getExprId(),functions.lit(e.getSpelExpression()))
						.withColumn("validationResult_"+e.getExprId(),functions.lit(""));
						//vg.getExpressionMap().get(e.getExprId())
			}
			
			ExpressionProcessor ep = new ExpressionProcessor(em);
			
			Dataset<Row> ds = expressionCol.map(ep.multiColumns,RowEncoder.apply(expressionCol.schema()));//.cache();
			Integer totalCount  = (int) ds.count();
			Dataset<Row> failedDataSet = ds.filter(ds.col("RowResult").equalTo("FALSE")).cache();
			Integer eCount = null;
			for(ExpressionStatus e : em) {
				eCount = (int) failedDataSet.select(failedDataSet.col("validationResult_"+e.getExprId())).filter(failedDataSet.col("validationResult_"+e.getExprId()).isNotNull()
						.and(failedDataSet.col("validationResult_"+e.getExprId()).equalTo("false").or(failedDataSet.col("validationResult_"+e.getExprId()).equalTo("FALSE")))).count();
				vg.getExpressionMap().get(e.getExprId()).setTotalCount(totalCount);
				vg.getExpressionMap().get(e.getExprId()).setErrorCount(eCount);
				
			}
			 
			
			//ds.show();	
			try{
				failedDataSet
				.filter(failedDataSet.col("RowResult").equalTo("FALSE"))
				.write()
				.mode(SaveMode.Overwrite)
				.format("com.databricks.spark.csv")
				.option("header", "true")
				.save(outputParent+validationExecutionGroups.getRunId()+File.separator+vg.getGroupName()+File.separator);
				
				//use this to merge post writing
				//org.apache.hadoop.fs.FileUtil.copyMerge(srcFS, srcDir, dstFS, dstFile, deleteSource, conf, addString)
				
			}catch(Exception e) {
				e.printStackTrace();
			}
			List<String> filenames = new LinkedList<String>();

			final File folder = new File(
					outputParent+validationExecutionGroups.getRunId()+File.separator+vg.getGroupName()+File.separator);
			listFilesForFolder(folder,filenames);
			System.out.println(filenames);
			vg.setCsvNames(filenames);
			vgs.addValidationExecutionGroup(vg);
		}
		vgs.setColumnUUIDMap(validationExecutionGroups.getColumnUUIDMap());
		vgs.setDimensionColumnData(validationExecutionGroups.getDimensionColumnData());
		vgs.setRunId(validationExecutionGroups.getRunId());
		writeValidationGroupsToFile(vgs);
		System.out.println("");
	}

	private void listFilesForFolder(File folder,List<String> filenames) {
		for (File fileEntry : folder.listFiles()) {
			
			if(fileEntry.getName().endsWith(".csv")) {
				File newFileName = new File(fileEntry.getParent(),fileEntry.getName().replace("-", "_"));
				fileEntry.renameTo(newFileName);
				filenames.add(newFileName.getName());
			}
		}
	}
	
	public void writeValidationGroupsToFile(ValidationExecutionGroups valExecGroups) {
		String path = jsonPath+valExecGroups.getRunId()+File.separator+"output"+File.separator;
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		try (Writer writer = new FileWriter(path+ validationGroupJsonName)) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(valExecGroups, writer);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
