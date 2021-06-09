package com.sparkSpelPoc.poc.sparkSpelPoc;

import org.apache.spark.launcher.SparkLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SparkProcessUtil {
	public static void main(String[] args) {
		executeProcess(99);
	}
	public static void executeProcess(Integer rid) {
		int exitCode=-999;
		try {
			SparkLauncher launcher = new SparkLauncher();
			launcher
			.setMaster("spark://blrdevapp038.fintellix.com:7077")
			.setAppName("adhoc query rn")
			//.setSparkHome(ApplicationProperties.getValue("app.SPARK_HOME"))
			.setDeployMode("client")
			.setAppResource("D:\\spark-SpEL-POC\\target\\SparkSqlUDF-POC-1.0-SNAPSHOT.jar") // Specify user app jar path
			.setMainClass("com.sparkSpelPoc.poc.sparkSpelPoc.App")
			.addAppArgs("")
			.setConf("spark.test", "Hello Sanketh")
			.setConf("spark.authenticate.secret", "LxlnjG8F0GJfIW2sU8MkZUCIFIQwUXXW")
			.setConf("spark.network.crypto.enabled", "true").setConf("spark.authenticate", "true")
			//.setConf("spark.executor.instances", ApplicationProperties.getValue("spark.executor.instances"))
			//.setConf("spark.executor.cores", ApplicationProperties.getValue("spark.executor.cores"))
			//.setConf("spark.executor.memory", ApplicationProperties.getValue("spark.executor.memory"))
			//.addAppArgs(ApplicationProperties.getValue("jdbc-spark-properties-path"))
			.addFile("D:\\spark-SpEL-POC\\target\\classes\\application.properties")
			//.setConf("spark.dynamicAllocation.enabled", ApplicationProperties.getValue("spark.dynamicAllocation.enabled"))
			//.setConf("spark.shuffle.service.enabled", ApplicationProperties.getValue("spark.shuffle.service.enabled"))
			;
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
