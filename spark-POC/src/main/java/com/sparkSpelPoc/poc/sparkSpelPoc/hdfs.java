/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

import com.edmunds.rest.databricks.service.DbfsService;

/**
 * @author Sanketh Noolee
 *
 */
public class hdfs {

	public static void main(String[] args) throws Throwable {
		//writeToLocal();
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get( new URI( "hdfs://blrdevapp038.fintellix.com:9000" ), conf );
		//the second boolean parameter here sets the recursion to true
		
		
		
		
		downloadContentsToLocalRecursive("hdfs://blrdevapp038.fintellix.com:9000/opt/", fs);
	
		
		
		
		
		
	    RemoteIterator<LocatedFileStatus> fileStatusListIterator = fs.listFiles(
	            new Path("hdfs://blrdevapp038.fintellix.com:9000/opt/Fintellix/ADEPT_314/ADEPT_DEV/ValidationResults/test1/"), true);
	    while(fileStatusListIterator.hasNext()){
	        LocatedFileStatus fileStatus = fileStatusListIterator.next();
	        //do stuff with the file like ...
	        //System.out.println(fileStatus.getPath());
	    }
	}
	
	private static void writeToLocal() throws IOException, URISyntaxException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get( new URI( "hdfs://blrdevapp038.fintellix.com:9000" ), conf );
		// Hadoop DFS Path - Input & Output file
		File file = new File("C:\\Users\\i21156\\Downloads\\naruto.jpg");
		Path inFile = new Path("hdfs://blrdevapp038.fintellix.com:9000/opt/Fintellix/ADEPT_314/ADEPT_DEV/ValidationResults/test1/"+file.getName());
		// Verification
		FSDataInputStream in = fs.open(inFile);
		    OutputStream os = null;
		    try {
		        os = new FileOutputStream("C:\\Users\\i21156\\Desktop\\naruto.jpg");
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = in.read(buffer)) > 0) {
		            os.write(buffer, 0, length);
		        }
		    } finally {
		    	in.close();
		        os.close();
		    }
		}
	
	
	private void writetoHDFS() throws URISyntaxException{

		Configuration conf = new Configuration();
		try {
			FileSystem fs = FileSystem.get( new URI( "hdfs://blrdevapp038.fintellix.com:9000" ), conf );
			// Hadoop DFS Path - Input & Output file
			File file = new File("C:\\Users\\i21156\\Downloads\\naruto.jpg");
			Path outFile = new Path("hdfs://blrdevapp038.fintellix.com:9000/opt/Fintellix/ADEPT_314/ADEPT_DEV/ValidationResults/test1/"+file.getName());
			// Verification
			if(fs.exists(outFile)) {
				System.out.println("asdas");
			}
			// open and read from file
			// FSDataInputStream in = fs.open(inFile);
			InputStream is = new FileInputStream(file);
			// Create file to write
			FSDataOutputStream out = fs.create(outFile,true);
			
			byte buffer[] = new byte[256];
			try {
				// open and read from file
				// in = fs.open(inFile);
				// Create file to write
				out = fs.create(outFile);
				IOUtils.copyBytes(is, out, 512, false);
			} catch (IOException e) {
				System.out.println("Error while copying file");
			} finally {
				// in.close();
				out.close();
				is.close();
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	public static void downloadContentsToLocalRecursive(String path,FileSystem fs) throws Throwable{
		String hdfsURI = "hdfs://blrdevapp038.fintellix.com:9000/";
		String localRootPath = "C:\\Users\\i21156\\Desktop\\azurefiles\\hdfs\\";
		
		try {
			 RemoteIterator<LocatedFileStatus> fileStatusListIterator = fs.listFiles(
			            new Path(path), true);
			    while(fileStatusListIterator.hasNext()){
			        LocatedFileStatus fileStatus = fileStatusListIterator.next();
			        System.out.println(fileStatus.getPath());
					if(fileStatus.isDirectory()) {
						String newpath = fileStatus.getPath().toString().replace(hdfsURI, "");
						File directory = new File(localRootPath+newpath);
						if (!directory.exists()) {
							directory.mkdirs();
						}
						downloadContentsToLocalRecursive( fileStatus.getPath().toString(),fs);
					}else {
						String newpath = fileStatus.getPath().toString().replace(hdfsURI, "");
						File directory = new File(localRootPath+(newpath.substring(0,newpath.lastIndexOf('/'))));
						if (!directory.exists()) {
							directory.mkdirs();
						}
							FSDataInputStream in = fs.open(fileStatus.getPath());
						    OutputStream os = null;
						    try {
						        os = new FileOutputStream(localRootPath+(newpath.substring(0,newpath.lastIndexOf('/'))+File.separator+
										(newpath.substring(newpath.lastIndexOf('/')+1)).replace("-", "_")));
						        byte[] buffer = new byte[1024];
						        int length;
						        while ((length = in.read(buffer)) > 0) {
						            os.write(buffer, 0, length);
						        }
						    } finally {
						    	in.close();
						        os.close();
						    }
					}
				
			        System.out.println(fileStatus.getPath());
			    }
			
		}catch(Throwable e) {
			e.printStackTrace();
		}
	}
}


