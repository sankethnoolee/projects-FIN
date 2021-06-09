package com.fintellix.framework.collaboration.store;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.stereotype.Component;

import com.fintellix.framework.collaboration.dto.Directory;
import com.fintellix.framework.collaboration.dto.File;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
@Component
public class DocumentStoreImpl implements DocumentStore{

	protected static final Logger logger = LoggerFactory.getLogger(DocumentStoreImpl.class);
	private static ModeShapeEngine engine = DocumentEngine.getEngine();


	@Override
	public void createFolderByPath(String repositoryName,String spaceName,String path, String folderName) throws NoSuchRepositoryException, RepositoryException,VyasaBusinessException {
		logger.info("DocumentStoreImpl --> createFolderByPath()");
		try{
			Session session = getSessionForRepositoryForWorkspace(repositoryName, spaceName);

			//Parent node
			if(path!=null && !"".equalsIgnoreCase(path)){
				Node parentNode = session.getRootNode().getNode(path);
				//Adding a folder in new parent node
				parentNode.addNode(folderName, "nt:folder");
				session.save();

			} else {
				Node parentNode = session.getRootNode();

				//Adding a folder in new parent node
				parentNode.addNode(folderName, "nt:folder");
				session.save();

			}
			session.logout();

		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}

	}

	@Override
	public void createFileByPathNameAndVersion(String repositoryName,String spaceName,String packagePath, String fileName,InputStream fileInputStream,String versionNumber) throws VyasaBusinessException,NoSuchRepositoryException, RepositoryException {
		logger.info("DocumentStoreImpl --> createFileByPathNameAndVersion()");
		try{
			Session session = getSessionForRepositoryForWorkspace(repositoryName, spaceName);
			//Parent node
			Node parentNode = session.getRootNode().getNode(packagePath);

			if(parentNode.hasNode(fileName)){

				//create a new version
				Node fileNode = parentNode.getNode(fileName);
				VersionManager versionManager = session.getWorkspace().getVersionManager();
				versionManager.checkout(fileNode.getPath());

				Node content= fileNode.getNode("jcr:content");

				Binary binary = session.getValueFactory().createBinary(fileInputStream);

				content.setProperty("jcr:data", binary);
				session.save();
				versionManager.checkin(fileNode.getPath());

			} else {
				//create a new file

				Node fileNode =parentNode.addNode(fileName, "nt:file");

				fileNode.addMixin("mix:versionable");

				Node content= fileNode.addNode("jcr:content", "nt:resource");

				Binary binary = session.getValueFactory().createBinary(fileInputStream);

				content.setProperty("jcr:data", binary);

				VersionManager versionManager = session.getWorkspace().getVersionManager();
				session.save();
				versionManager.checkin(fileNode.getPath());

			}
			session.logout();

		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}

	}

	@Override
	public InputStream getFileByPathNameAndVersion(String repositoryName,String spaceName,String path, String fileName, Integer versionNumber) throws VyasaBusinessException,NoSuchRepositoryException, RepositoryException {
		logger.info("DocumentStoreImpl --> getFileByPathNameAndVersion()");
		try{
			Binary binary=null;
			Session session = getSessionForRepositoryForWorkspace(repositoryName, spaceName);
			VersionManager versionManager = session.getWorkspace().getVersionManager();

			Node fileNode = session.getRootNode().getNode(path).getNode(fileName);
			VersionHistory history = versionManager.getVersionHistory(fileNode.getPath());
			Version version = history.getVersion("1."+versionNumber);
			Node contentNode = version.getFrozenNode().getNode("jcr:content");
			binary = contentNode.getProperty("jcr:data").getBinary();

			InputStream stream = binary.getStream();
			session.logout();
			return stream;

		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}
	}

	@Override
	public void deleteContentByPath(String repositoryName,String spaceName, String fullPath) throws VyasaBusinessException,NoSuchRepositoryException, RepositoryException {
		logger.info("DocumentStoreImpl --> deleteContentByPath()");
		try{

			logger.info("DocumentStoreImpl --> deleteContentByPath()");
			Session session = getSessionForRepositoryForWorkspace(repositoryName, spaceName);
			session.getRootNode().getNode(fullPath).remove();;
			session.save();
			session.logout();	
		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}


	}


	private Session getSessionForRepositoryForWorkspace(String repositoryName,String spaceName) throws VyasaBusinessException,NoSuchRepositoryException, RepositoryException{
		try{
			return engine.getRepository(repositoryName).login(spaceName);	
		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}

	}

	@Override
	public boolean isNodeExist(String repositoryName, String spaceName, String fullPath) throws VyasaBusinessException,NoSuchRepositoryException, RepositoryException {
		logger.info("DocumentStoreImpl --> isNodeExist()");
		try{
			return getSessionForRepositoryForWorkspace(repositoryName, spaceName).nodeExists(fullPath);	
		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}

	}
	@Override
	public Set<String> getRepositories() throws VyasaBusinessException{
		try{
			return engine.getRepositoryNames();	
		}catch(Throwable e){
			e.printStackTrace();
			throw new VyasaBusinessException();
		}

	}

	@Override
	public void createBulkFoldersByPath(String repositoryName, String spaceName, LinkedHashMap<Integer,List<Directory>> folderList,List<File> files) throws NoSuchRepositoryException, RepositoryException,VyasaBusinessException {
		logger.info("DocumentStoreImpl --> createFolderByPath()");
		Session session=null;
		try{
			session = getSessionForRepositoryForWorkspace(repositoryName, spaceName);
			String modeShapePath = null;
			String packageLocation = null;
			for(Map.Entry<Integer, List<Directory>> folders:folderList.entrySet()){
				for(Directory d:folders.getValue()){
					logger.info("creating directory in modeshape --- " + d.getDirectoryName());
					if(d.getPackageLocation()!=null && !"".equalsIgnoreCase(d.getPackageLocation())){
						packageLocation=d.getPackageLocation();
						modeShapePath=packageLocation.replace("###", "/");
					} else {
						packageLocation="";
						modeShapePath="";
					}
					//Parent node
					if(modeShapePath!=null && !"".equalsIgnoreCase(modeShapePath)){
						Node parentNode = session.getRootNode().getNode(modeShapePath);
						//Adding a folder in new parent node
						parentNode.addNode(d.getDirectoryId(), "nt:folder");
					} else {
						Node parentNode = session.getRootNode();
						//Adding a folder in new parent node
						parentNode.addNode(d.getDirectoryId(), "nt:folder");
					}
				}
			}
			
			for(File file:files){
				logger.info("creating file in modeshape --- " + file.getFileName());
				Node parentNode = session.getRootNode().getNode(file.getPackageLocation().replace("###", "/"));

				Node fileNode =parentNode.addNode(file.getFileId(), "nt:file");

				fileNode.addMixin("mix:versionable");

				Node content= fileNode.addNode("jcr:content", "nt:resource");
				InputStream fileInputStream = new FileInputStream(new java.io.File(file.getActualFilePath()));

				Binary binary = session.getValueFactory().createBinary(fileInputStream);

				content.setProperty("jcr:data", binary);

				VersionManager versionManager = session.getWorkspace().getVersionManager();
				session.save();
				versionManager.checkin(fileNode.getPath());
			
			}
			session.save();
			session.logout();
		}catch(Throwable e){
			e.printStackTrace();
			session.refresh(false);
			session.logout();
			throw new VyasaBusinessException();
		}

	}
}