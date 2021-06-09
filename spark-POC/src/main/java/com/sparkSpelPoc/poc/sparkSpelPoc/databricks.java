/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.edmunds.rest.databricks.DatabricksRestException;
import com.edmunds.rest.databricks.DatabricksServiceFactory;
import com.edmunds.rest.databricks.DTO.dbfs.FileInfoDTO;
import com.edmunds.rest.databricks.DTO.jobs.SparkSubmitTaskDTO;
import com.edmunds.rest.databricks.service.DbfsService;

/**
 * @author Sanketh Noolee
 *
 */
public class databricks {
	private static final String APPLICATION_PROPERTIES = "application.properties";
	private static Properties prop = new Properties();
	public static void main(String[] args) throws Throwable {
		InputStream is=null;
		InputStream is1=null;
		try {
SparkSubmitTaskDTO s = new SparkSubmitTaskDTO();

			// load properties
			prop.load(databricks.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES));
			DatabricksServiceFactory serviceFactory = DatabricksServiceFactory.Builder
					.createTokenAuthentication(prop.getProperty("app.databricks.auth.token"),
							prop.getProperty("app.databricks.workspace.url"))
					.withMaxRetries(Integer.parseInt(prop.getProperty("app.databricks.maxretries")))
					.withRetryInterval(Long.parseLong(prop.getProperty("app.databricks.retry.interval.millis"))).build();
			DbfsService dbfs = serviceFactory.getDbfsService();
			//("dbfs:/Validation-DataValidation-Groups/", true);
			/*"("dbfs:/Validation_Results/15451/Group-1/"
					+ "part-00000-tid-2092798038274554626-40228bcc-bcfd-4793-ae4b-4c5eaae7bef4-1418-1-c000.csv" , dbfs);
			writeToLocalFromFileSystem("dbfs:/Validation_Results/15451/Group-1/"
					+ "part-00000-tid-2092798038274554626-40228bcc-bcfd-4793-ae4b-4c5eaae7bef4-1418-1-c000.csv" , dbfs);
			*/
			downloadContentsToLocalRecursive("dbfs:/Validation-Data/", dbfs);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			//is.close();
		}
		
		
	}
	public static String pathFormatter(String path ,DbfsService dbfs) throws Throwable {
		System.out.println("requ path->"+path);
		path = path.replace('\\', '/');
		String remotePath = path;
		remotePath = (remotePath.startsWith("dbfs:/") 
						? remotePath 
						: ("dbfs:/"+remotePath));
		// create remote directory
		if(!pathExists(remotePath, dbfs)) {
			//dbfs.mkdirs(remotePath);
			System.out.println("mkdri");
		}
		
		return remotePath;
	}
	
	public static String getJSONStringFromStorage(String path,DbfsService dbfs) throws Throwable {

		try {
			ByteArrayOutputStream os = null;
			try {
				os = new ByteArrayOutputStream();
				os.write(dbfs.read(pathFormatter(path,dbfs)).getData());
				System.out.println(os.toString(StandardCharsets.UTF_8.name()));
				return os.toString(StandardCharsets.UTF_8.name());
			} finally {
				//in.close();
				os.close();
			}

		}catch(Throwable e) {
			e.printStackTrace();
			return "";
		}finally {

		}

	}
	public static Boolean pathExists(String path,DbfsService dbfs) throws Throwable {
		try {
			FileInfoDTO dsds = dbfs.getInfo((path));
			if(dsds==null) {
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		}catch(Throwable e ) {
			System.out.println("Path not found - "+path);
			return Boolean.FALSE;
		}
	}
	
	public static Boolean writeToLocalFromFileSystem(String srcPath,DbfsService dbfs)
			throws Throwable {

		
		try {
			OutputStream os = null;
			try {
				os = new FileOutputStream("C:\\Users\\i21156\\Desktop\\azurefiles\\test.csv");
				os.write(dbfs.read((srcPath)).getData());
			} finally {
				//in.close();
				os.close();
			}

			return Boolean.TRUE;
		}catch(Throwable e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}finally {

		}

	}
	
	public static void downloadContentsToLocalRecursive(String path,DbfsService dbfs) throws Throwable {
		try {
			FileInfoDTO[] d = dbfs.ls(pathFormatter(path,dbfs));
			if(d!=null) {
				for(FileInfoDTO f : d) {
					if(f.isDir()) {
						String newpath = f.getPath().substring(1);
						String localRoot = "C:\\Users\\i21156\\Desktop\\azurefiles\\";
						File directory = new File(localRoot+newpath);
						if (!directory.exists()) {
							directory.mkdirs();
						}
						System.out.println("dir->"+newpath);
						downloadContentsToLocalRecursive( newpath, dbfs);
					}else {
						String newpath = f.getPath().substring(1);
						String localRoot = "C:\\Users\\i21156\\Desktop\\azurefiles\\";
						OutputStream os = null;
						try {
							os = new FileOutputStream(localRoot+(newpath.substring(0,newpath.lastIndexOf('/'))+File.separator+
									(newpath.substring(newpath.lastIndexOf('/')+1)).replace("-", "_")));
							os.write(dbfs.read(pathFormatter(newpath,dbfs)).getData());
						} finally {
							//in.close();
							os.close();
						}
					}
				}
			}
		}catch(Throwable e) {
			e.printStackTrace();
		}
	}

}
