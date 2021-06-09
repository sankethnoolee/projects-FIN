package com.fintellix.framework.collaboration.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.NoSuchRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.utils.CollaborationProperties;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class DocumentStoreImpl implements DocumentStore{

	protected static final Logger logger = LoggerFactory.getLogger(DocumentStoreImpl.class);
	private static final String gridFSBucketName = CollaborationProperties.getValue("conf.gridFSBucket");

	//excluding modeShape
	@Override   
	public void saveFileIntoMongoDB(com.fintellix.framework.collaboration.dto.File dbFile,File uploadedFile) throws IOException {
		String fileNameInMongo = dbFile.getFileId()+"###"+dbFile.getPackageLocation()+"###"+dbFile.getVersionNumber();    
		GridFS gfsPhoto = new GridFS(FileStore.getDbFromMongoClient(), gridFSBucketName);
		GridFSInputFile gfsFile = gfsPhoto.createFile(uploadedFile);
		gfsFile.setFilename(fileNameInMongo);
		gfsFile.save();
	}
	
	@Override
	public InputStream getFileForDownload(com.fintellix.framework.collaboration.dto.File dbFile) {
		String fileNameInMongo = dbFile.getFileId()+"###"+dbFile.getPackageLocation()+"###"+dbFile.getVersionNumber();  
		GridFS gfsPhoto = new GridFS(FileStore.getDbFromMongoClient(), gridFSBucketName);
		GridFSDBFile fileForOutput = gfsPhoto.findOne(fileNameInMongo);
		return fileForOutput.getInputStream();
	}

	@Override
	public void deleteAllVersionsOfTheFromMongoDB(com.fintellix.framework.collaboration.dto.File dbFile) {
		//deletes all file irrespective of version.
		String fileNameInMongo = dbFile.getFileId()+"###"+dbFile.getPackageLocation();
		GridFS gfsfile = new GridFS(FileStore.getDbFromMongoClient(), gridFSBucketName);
		BasicDBObject query = new BasicDBObject();
    	query.append("filename", Pattern.compile("^"+fileNameInMongo+".*$"));
    	gfsfile.remove(query);
	}

	@Override
	public InputStream getFileForDownloadByVersion(com.fintellix.framework.collaboration.dto.File dbFile, Integer versionNo) {
		String fileNameInMongo = dbFile.getFileId()+"###"+dbFile.getPackageLocation()+"###"+versionNo;  
		GridFS gfsPhoto = new GridFS(FileStore.getDbFromMongoClient(), gridFSBucketName);
		GridFSDBFile fileForOutput = gfsPhoto.findOne(fileNameInMongo);
		return fileForOutput.getInputStream();
	}
}