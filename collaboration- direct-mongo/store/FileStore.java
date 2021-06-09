package com.fintellix.framework.collaboration.store;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.administrator.PasswordDecrypterUtil;
import com.fintellix.platformcore.utils.CollaborationProperties;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class FileStore {
	
	protected static final Logger logger = LoggerFactory.getLogger(FileStore.class);
	private static final String MongoAuthReq = CollaborationProperties.getValue("conf.mongoAuthRequired");
	private static final String mongoDBHost = CollaborationProperties.getValue("conf.mongoDBHost");
	private static final Integer mongoDBPort = Integer.parseInt(CollaborationProperties.getValue("conf.mongoDBPort"));
	private static final String mongoDBDatabaseName = CollaborationProperties.getValue("conf.mongoDBDatabaseName");
	private static final String mongoUserName = CollaborationProperties.getValue("conf.mongoUserName");
	private static final String mongoPassword = CollaborationProperties.getValue("conf.mongoPassword");
	private static MongoClient mongo =  null;
	static {
		try {
			setMongoClient();
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
	}
	
	
	private static void setMongoClient() {
		if("N".equalsIgnoreCase(MongoAuthReq)) {
			mongo = new MongoClient(new ServerAddress(mongoDBHost, mongoDBPort));
			
		}else if("Y".equalsIgnoreCase(MongoAuthReq)) {
			MongoCredential credential = MongoCredential.createCredential(mongoUserName, mongoDBDatabaseName,
					PasswordDecrypterUtil.passwordDecrypter(mongoPassword).toCharArray());
			 mongo = new MongoClient(new ServerAddress(mongoDBHost, mongoDBPort),Arrays.asList(credential));

		}
	}
	
	public static DB getDbFromMongoClient(){
		if(null==mongo) {
			setMongoClient();
		}
		return (mongo.getDB(mongoDBDatabaseName));
	}

	
}
