package com.fintellix.dld.application.licence;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.cells.License;

@WebListener
public class XLeratorLicenseLoader implements ServletContextListener{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static Properties applicationProperties;
	
	static{
		try {
			InputStream is1  = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(is1);

		}catch (Exception e) {
			throw new RuntimeException("Coudnt read application / data-dashboard-queries  properties from class path",e);
		}
	}

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";
	private static final String key = applicationProperties.getProperty("dld.licencekey") ;
	
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Loading XLerator license - Start");
		  try {
			
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("aspose.cells.lic");
			byte[] inputBytes = IOUtils.toByteArray(inputStream);
			Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] outputBytes = cipher.doFinal(Base64.getDecoder().decode(inputBytes));
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputBytes);
	    	License license = new License();
			license.setLicense(byteArrayInputStream);
			byteArrayInputStream.close();
			inputStream.close();
			} catch (Exception e) {
		        logger.error("Error encountered trying to set Aspose.Cells license! check encryption details", e);
		    }
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
	
	
	static void encryptFile () throws Exception{
		String content = new String(Files.readAllBytes(Paths.get("E:\\lic.xml")));
		Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] outputBytes = cipher.doFinal(content.getBytes());
		String encodedString = Base64.getEncoder().encodeToString(outputBytes);
		FileOutputStream fos = new FileOutputStream("E:\\lic.xml_enc");
		fos.write(encodedString.getBytes());
		fos.close();
		
		content = new String(Files.readAllBytes(Paths.get("E:\\lic.xml_enc")));
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		outputBytes = cipher.doFinal(Base64.getDecoder().decode(content));
		FileOutputStream fos1 = new FileOutputStream("E:\\lic.xml_retrive");
		fos1.write(outputBytes);
		fos1.close();
		
	}
	
	public static void main(String[] args) throws Exception {
		encryptFile();
	}
	
	

}
