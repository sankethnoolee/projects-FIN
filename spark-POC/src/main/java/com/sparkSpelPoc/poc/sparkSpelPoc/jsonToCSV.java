/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Sanketh Noolee
 *
 */
public class jsonToCSV {
	
	static String jsonString = "{\r\n" + 
			"    \"Results\": {\r\n" + 
			"        \"auditJson\": [\r\n" + 
			"            {\r\n" + 
			"                \"GL SUB HEAD CODE_meta\": {\r\n" + 
			"                    \"oldval\": \"----\",\r\n" + 
			"                    \"newval\": \"-----\",\r\n" + 
			"                    \"status\": \"existing\",\r\n" + 
			"                    \"errorMsg\": \"test\"\r\n" + 
			"                }\r\n" + 
			"            },\r\n" + 
			"            {\r\n" + 
			"                \"DATA SOURCE ID_meta\": {\r\n" + 
			"                    \"oldval\": \"----\",\r\n" + 
			"                    \"newval\": \"-----\",\r\n" + 
			"                    \"status\": \"existing\",\r\n" + 
			"                    \"errorMsg\": \"test\"\r\n" + 
			"                }\r\n" + 
			"            }\r\n" + 
			"        ]\r\n" + 
			"    }\r\n" + 
			"}"; 
	public static void main(String[] args) throws IOException {
		
		
		
        JSONObject j = new JSONObject(jsonString);
        JSONObject results = j.getJSONObject("Results");
		JSONArray auditJson = results.getJSONArray("auditJson");
		
		 JSONObject tempObj = new JSONObject();
		 JSONArray finalArr = new JSONArray();
		
		for(int i =0; i<auditJson.length(); i++) {
			String entName = (auditJson.getJSONObject(i).keySet().toArray()[0]).toString();
			
			tempObj = auditJson.getJSONObject(i).getJSONObject(entName);
			tempObj.put("entName", entName);
			finalArr.put(tempObj);
		}
		
		
		File file = new File("C:\\Users\\i21156\\Desktop\\antlr bck\\jsontocsv\\yourfile.csv");
        String csv = CDL.toString(finalArr);
        FileUtils.writeStringToFile(file, csv);
		
		/*
		 * CSVWriter writer = new CSVWriter(new
		 * FileWriter("C:\\Users\\i21156\\Desktop\\antlr bck\\jsontocsv\\yourfile.csv"),
		 * ','); // feed in your array (or convert your data to an array) String[]
		 * entries = "first#second#third".split("#"); String[] entries1 =
		 * "first#1#third".split("#"); List<String[]> finalArr = new
		 * ArrayList<String[]>(); finalArr.add(entries); finalArr.add(entries1);
		 * writer.writeAll(finalArr); writer.close();
		 */
		
	}
}
