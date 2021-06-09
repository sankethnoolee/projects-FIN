package com.fintellix.validationrestservice.spark.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroups;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtilForSpark {
	public static void writeValidationGroupsToFile(ValidationExecutionGroups valExecGroups) {
		String path = ApplicationProperties.getValue("app.spark.filePath.validationGroups")+valExecGroups.getRunId()+ File.separator
				+"input"+File.separator;
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		try (Writer writer = new FileWriter(path+ApplicationProperties.getValue("app.spark.filePath.validationGroupsFileName"))) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(valExecGroups, writer);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	public static ValidationExecutionGroups readValidationGroupsToFile(Integer rid) {
		String path = ApplicationProperties.getValue("app.spark.filePath.validationGroups")+rid+File.separator+"output"+File.separator;
		try{
			File directory = new File(path);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			Gson gson = new GsonBuilder().create();
			return gson.fromJson(new FileReader(path+ ApplicationProperties.getValue("app.spark.filePath.validationGroupsFileName")), ValidationExecutionGroups.class);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
