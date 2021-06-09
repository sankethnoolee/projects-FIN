package com.fintellix.framework;

public class App {

	public static void main(String[] args) {
		try {

			SparkJob sparkJob = new SparkJob(args);

			sparkJob.startJob();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
