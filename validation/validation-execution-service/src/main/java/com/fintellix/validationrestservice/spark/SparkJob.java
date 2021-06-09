package com.fintellix.validationrestservice.spark;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import com.fintellix.validationrestservice.core.executor.ExpressionStatus;
import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroup;
import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroups;
import com.fintellix.validationrestservice.spark.expressionUtils.ExpressionProcessor;
import com.fintellix.validationrestservice.spark.util.FileUtil;
import com.fintellix.validationrestservice.spark.util.SQLUtil;
import com.fintellix.validationrestservice.spark.util.SpelUtil;
import com.fintellix.validationrestservice.util.connectionManager.ConnectionPropertiesLoader;
import static com.fintellix.validationrestservice.spark.config.CustomConstants.*;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.callUDF;

/**
 * @author Sanketh Noolee
 *
 */
public class SparkJob {
	private static final SparkJob instance;
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
            instance = new SparkJob();

        } catch (Exception e) {
            throw new RuntimeException("Coudnt read application properties from class path", e);
        }
    }

	private final Logger logger = Logger.getLogger(SparkJob.class);

	private String sparkMaster; 
	private String inputFilePath="D:\\spark-SpEL-POC\\speldata.csv";

	private JavaSparkContext javaSparkContext;
	private SQLContext sqlContext;
	private SparkSession sparkSession;

	private FileUtil fileUtil;
	private SpelUtil spelUtil;
	private SQLUtil sqlUtil;
	private ExpressionProcessor expressionProcessor;
	private ValidationExecutionGroups validationExecutionGroups;

	private SparkJob() {
		/*
	       initialize the instance variables.
		 */
		initialize();

		/*
	        Register the required UDFs.
		 */
		registerUdfs();

	}

	public void startJob(ValidationExecutionGroups valExecGroups) throws IOException {
		this.validationExecutionGroups = valExecGroups;
		loadHelpers();
		
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
				//opDataset = expressionCol.map(ep.mf,RowEncoder.apply(expressionCol.schema()));
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
			opDataset = opDataset==null?map.get(i):opDataset.unionAll(map.get(i));
			
		}
		opDataset.write().partitionBy("validationId").mode(SaveMode.Overwrite)
		  .format("com.databricks.spark.csv") .option("header", "true") .save(
		  "D:\\sparksampledata\\output\\validationResult\\"+validationExecutionGroups.getRunId()+"\\"+"test");
		
		
	}

	private void registerUdfs() {
		spelUtil = spelUtil==null?new SpelUtil(sqlContext):spelUtil;
		this.spelUtil.registerSpelEvalUdf();
	}

	private void initialize() {

		sparkMaster =  applicationProperties.getProperty("spark.master").trim();

		javaSparkContext = createJavaSparkContext();
		sqlContext = new SQLContext(javaSparkContext);
		sparkSession = sqlContext.sparkSession();

	}

	
	private JavaSparkContext createJavaSparkContext() {

		/* Create the SparkSession.
		 * If config arguments are passed from the command line using --conf,
		 * parse args for the values to set.
		 */

		SparkConf conf = new SparkConf().setAppName("SparkSql-UDF-POC")
				.setMaster(sparkMaster);

		return new JavaSparkContext(conf);
	}

	public static SparkJob getInstance() {
		return instance;
	}
	
	private void loadHelpers() {
		fileUtil = fileUtil==null?new FileUtil(sparkSession):fileUtil;
		sqlUtil = sqlUtil==null?new SQLUtil(sqlContext):sqlUtil;
		
	}
}
