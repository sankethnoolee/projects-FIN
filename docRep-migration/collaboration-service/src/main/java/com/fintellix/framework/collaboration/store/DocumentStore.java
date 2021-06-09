package com.fintellix.framework.collaboration.store;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.modeshape.jcr.NoSuchRepositoryException;
import org.springframework.stereotype.Component;

import com.fintellix.framework.collaboration.dto.Directory;
import com.fintellix.framework.collaboration.dto.File;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
@Component
public interface DocumentStore {

	/**
	 * Create a folder/directory in workspace.
	 *
	 * @param repositoryName Name of repository.
	 * @param spaceName A workspace name in repository.
	 * @param path where folder/directory needs to be created.
	 * @param folderName name of folder/directory .
	 * @return  void
	 * @throws NoSuchRepositoryException If no accessible node is found at the
	 *                               specified path.
	 * @throws RepositoryException   If another error occurs.
	 * @throws VyasaBusinessException 
	 *
	 */
	public void createFolderByPath(String repositoryName,String spaceName,String path,String folderName) throws NoSuchRepositoryException, RepositoryException, VyasaBusinessException;
	
	/**
	 * Create a file in workspace.
	 *
	 * @param repositoryName Name of repository.
	 * @param spaceName A workspace name in repository.
	 * @param path where file needs to be created.
	 * @param fileName name of file.
	 * @param fileInputStream InputStream of file to created.
	 * @param versionNumber version number of file.
	 * @return  void
	 * @throws NoSuchRepositoryException If no accessible node is found at the
	 *                               specified path.
	 * @throws RepositoryException   If another error occurs.
	 * @throws VyasaBusinessException 
	 *
	 */
	public void createFileByPathNameAndVersion(String repositoryName,String spaceName,String path,String fileName,InputStream fileInputStream,String versionNumber) throws NoSuchRepositoryException, RepositoryException, VyasaBusinessException ;
	/**
	 *
	 * @param repositoryName Name of repository.
	 * @param spaceName A workspace name in repository.
	 * @param path an absolute path to file/folder in workspace.
	 * @param fileName name of file.
	 * @param versionNumber version number of file.
	 * @return  binary content of file.
	 * @throws NoSuchRepositoryException If no accessible node is found at the
	 *                               specified path.
	 * @throws RepositoryException   If another error occurs.
	 * @throws VyasaBusinessException 
	 *
	 */
	public InputStream getFileByPathNameAndVersion(String repositoryName,String spaceName,String path,String fileName,Integer versionNumber) throws NoSuchRepositoryException, RepositoryException, VyasaBusinessException ;
	/**
	 *	Delete a specific file and folder in workspace
	 *
	 * @param repositoryName Name of repository.
	 * @param spaceName A workspace name in repository.
	 * @param path an absolute path to file/folder in workspace.
	 * @return  void.
	 * @throws NoSuchRepositoryException If no accessible node is found at the
	 *                               specified path.
	 * @throws RepositoryException   If another error occurs.
	 * @throws VyasaBusinessException 
	 *
	 */
	public void deleteContentByPath(String repositoryName,String spaceName,String path) throws NoSuchRepositoryException, RepositoryException, VyasaBusinessException;

	/**
	 *
	 *
	 * @param repositoryName Name of repository.
	 * @param spaceName A workspace name in repository.
	 * @param path an absolute path to file/folder in workspace.
	 * @return  Returns if file/folder exist at the specified absolute path in the workspace.
	 * @throws NoSuchRepositoryException If no accessible node is found at the
	 *                               specified path.
	 * @throws RepositoryException   If another error occurs.
	 * @throws VyasaBusinessException 
	 *
	 */
	public boolean isNodeExist(String repositoryName,String spaceName,String path) throws NoSuchRepositoryException, RepositoryException, VyasaBusinessException;
	
	public Set<String> getRepositories() throws VyasaBusinessException;

	void createBulkFoldersByPath(String repositoryName, String spaceName, LinkedHashMap<Integer,List<Directory>> folderList,List<File> files)throws NoSuchRepositoryException, RepositoryException,VyasaBusinessException;

	
	

}


