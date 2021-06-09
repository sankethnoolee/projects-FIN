package com.fintellix.dld.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPropertyLoader {
	
protected String configFile;
	
static Logger LOGGER = LoggerFactory.getLogger(AbstractPropertyLoader.class);

protected JSONObject init() throws Throwable {

	String configFilePath = System.getenv().get(configFile);
	if ( configFilePath == null){
		// look in system.property.
		configFilePath = System.getProperty(configFile);
	}
	JSONParser parser = new JSONParser();
	JSONObject jsonObject = null;
	
	if ( configFilePath == null ) {
		
		// if we don't find cce-engine.properties passed 
		// to application as system environment variable, let's
		// look for it in class path.
		LOGGER.info("Searching " + configFile + " in classpath..");
		InputStream is = Thread.currentThread().getContextClassLoader().
			getResourceAsStream(configFile);
		jsonObject = (JSONObject) parser
				.parse(new InputStreamReader(new BufferedInputStream(is)));
						
	}
	
					
	return jsonObject;
}

public abstract void load(JSONObject jsonObject) throws Throwable;

}
