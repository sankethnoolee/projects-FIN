package com.fintellix.validationrestservice.spark.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.spark.launcher.SparkLauncher;

import com.fintellix.validationrestservice.util.ApplicationProperties;

public class SparkProcessUtil {
	private static Properties sparkJdbcProperties;
	static {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("jdbc-spark.properties");
			sparkJdbcProperties = new Properties();
			sparkJdbcProperties.load(is);
		} catch (Exception e) {
			throw new RuntimeException("Coudnt read sparkJdbcProperties properties from class path", e);
		}
	}
	public static void executeProcess(Integer rid) {
		int exitCode=-999;
		try {
			
			SparkLauncher launcher = new SparkLauncher();
			launcher
			.setMaster(ApplicationProperties.getValue("spark.master"))
			.setAppName(ApplicationProperties.getValue("app.spark.APP-NAME")+":RID-"+rid)
			//.setSparkHome(ApplicationProperties.getValue("app.SPARK_HOME"))
			.setDeployMode(ApplicationProperties.getValue("spark.deploy-mode"))
			.setAppResource(ApplicationProperties.getValue("app.validation-spark-jar.path")) // Specify user app jar path
			.setMainClass(ApplicationProperties.getValue("app.spark.main-class"))
			.addAppArgs(ApplicationProperties.getValue("app.spark.filePath.validationGroups")+rid+File.separator+"input"+File.separator+
					ApplicationProperties.getValue("app.spark.filePath.validationGroupsFileName"))
			/*.setConf("spark.executor.instances", ApplicationProperties.getValue("spark.executor.instances"))
			.setConf("spark.executor.cores", ApplicationProperties.getValue("spark.executor.cores"))
			.setConf("spark.executor.memory", ApplicationProperties.getValue("spark.executor.memory"))*/
			.addAppArgs(ApplicationProperties.getValue("jdbc-spark-properties-path"))
			//.setConf("spark.dynamicAllocation.enabled", ApplicationProperties.getValue("spark.dynamicAllocation.enabled"))
			//.setConf("spark.shuffle.service.enabled", ApplicationProperties.getValue("spark.shuffle.service.enabled"))
			.setConf("spark.custom.app.validations.outputDirectory", ApplicationProperties.getValue("app.validations.outputDirectory"))
			.setConf("spark.custom.app.spark.filePath.validationGroups", ApplicationProperties.getValue("app.spark.filePath.validationGroups"))
			.setConf("spark.custom.app.spark.filePath.validationGroupsFileName", ApplicationProperties.getValue("app.spark.filePath.validationGroupsFileName"))
			;
			for(Entry<Object,Object> e : SparkProperties.getEntries()){
				launcher.setConf((String)e.getKey(), (String)e.getValue());
			}
			String value;
			for (String key : sparkJdbcProperties.stringPropertyNames()) {
				value = sparkJdbcProperties.getProperty(key);
				launcher.setConf("spark.custom." + key, value);
			}

			Process process = launcher.launch();
			InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(process.getInputStream(), "input");
			Thread inputThread = new Thread(inputStreamReaderRunnable, "LogStreamReader input");
			inputThread.start();

			InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(process.getErrorStream(), "error");
			Thread errorThread = new Thread(errorStreamReaderRunnable, "LogStreamReader error");
			errorThread.start();

			System.out.println("Started spark-submit");
			exitCode = process.waitFor();
			System.out.println("Finished! Exit code:" + exitCode);

		}catch(Throwable e) {
			e.printStackTrace();
			System.out.println("Finished with exception! Exit code:" + exitCode);
		}



	}
}
class InputStreamReaderRunnable implements Runnable {

	private BufferedReader reader;

	private String name;

	public InputStreamReaderRunnable(InputStream is, String name) {
		this.reader = new BufferedReader(new InputStreamReader(is));
		this.name = name;
	}

	public void run() {
		System.out.println("InputStream " + name + ":");
		try {
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class SparkProperties{
	public static Properties sparkProp;
	static {
		try (InputStream in = Thread.currentThread().getContextClassLoader()
		.getResourceAsStream("spark-job-conf.properties")) {
			sparkProp = new Properties();
			sparkProp.load(in);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String get(String key){
		return (String) sparkProp.get(key);
	}

	public static Set<Entry<Object, Object>> getEntries() {
		return  sparkProp.entrySet();
	}
	
}
