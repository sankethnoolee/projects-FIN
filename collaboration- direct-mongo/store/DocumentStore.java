package com.fintellix.framework.collaboration.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;

import org.modeshape.jcr.NoSuchRepositoryException;

import com.fintellix.framework.collaboration.dto.File;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;

public interface DocumentStore {

	public void saveFileIntoMongoDB(File dbFile, java.io.File imageFile) throws IOException;
	public InputStream getFileForDownload(File dbFile);
	public void deleteAllVersionsOfTheFromMongoDB(File dbFile);
	public InputStream getFileForDownloadByVersion(File file, Integer vId);

}


