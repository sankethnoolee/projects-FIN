package com.fintellix.framework.collaboration.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.jcr.RepositoryException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ConfigurationException;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.ModeShapeEngine.State;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.schematic.document.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.fintellix.administrator.PasswordDecrypterUtil;
@Component
public class DocumentEngine {
	protected static final Logger logger = LoggerFactory.getLogger(DocumentEngine.class);
	private static ModeShapeEngine engine=new ModeShapeEngine();
	static {
		try {
			DocumentEngine.initModeShapeEngine();
		} catch (ConfigurationException | RepositoryException | IOException|ParseException e) {
			logger.error(e.getMessage());
		}
	}


	public static ModeShapeEngine getEngine(){
		return engine;
	}
	public static ModeShapeEngine initModeShapeEngine() throws ConfigurationException, ParsingException, RepositoryException, FileNotFoundException,IOException,ParseException {
		startEngine();
		return engine;
	}

	protected static void startEngine() throws ConfigurationException, ParsingException, RepositoryException, FileNotFoundException,IOException, ParseException {
		if(engine.getState() == State.NOT_RUNNING) {
			engine.start();
			loadRepositories();
		} else if(engine.getState() == State.RESTORING) {
			startEngine();
		} else if(engine.getState() == State.STARTING || engine.getState() == State.STOPPING) {
			startEngine();
		}
	}

	@SuppressWarnings("unchecked")
	protected static void loadRepositories() throws ConfigurationException, RepositoryException, IOException, ParseException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("document-manager-prop.json");
		File file = ResourceUtils.getFile(url);
		
		JSONParser parser = new JSONParser();
		JSONObject obj = (JSONObject) parser.parse(new FileReader(file));
		JSONObject storage = (JSONObject) obj.get("storage");
		
		JSONObject persistence =(JSONObject) storage.get("persistence");
		String persistencePassword = (String) persistence.get("password");
		persistencePassword = PasswordDecrypterUtil.passwordDecrypter(persistencePassword);
		persistence.put("password", persistencePassword);
		
		JSONObject binaryStorage =(JSONObject) storage.get("binaryStorage");
		String binaryStoragePassword = (String) binaryStorage.get("password");
		binaryStoragePassword= PasswordDecrypterUtil.passwordDecrypter(binaryStoragePassword);
		binaryStorage.put("password", binaryStoragePassword);
		
		storage.put("binaryStorage", binaryStorage);
		storage.put("persistence", persistence);
		obj.put("storage", storage);
		
		
		RepositoryConfiguration config = RepositoryConfiguration.read(obj.toString());

		// Verify the configuration for the repository ...
		Problems problems = config.validate();
		if (problems.hasErrors()) {
			logger.info("Problems starting the engine.");
			logger.info(problems.toString());
			return;
		}
		// Deploy the repository ...
		engine.deploy(config);


	}

}
