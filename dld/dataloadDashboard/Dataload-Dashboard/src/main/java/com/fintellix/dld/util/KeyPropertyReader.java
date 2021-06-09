package com.fintellix.dld.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyPropertyReader {
	
	private static Logger LOGGER = LoggerFactory.getLogger(KeyPropertyReader.class);
	private static final String CONFIG_FILE = "key.properties";
	private static final String ALGORITHM = "decrypt.algo";
	private static final String KEY = "aes.key";

	private static String key;
	private static String algorithm;

	static {
		try {

			// read encryption.properties from class path
			Properties properties = new Properties();
			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(CONFIG_FILE);
			if (is != null) {
				properties.load(is);
			}

			algorithm = (properties.getProperty(ALGORITHM) != null) ? properties.getProperty(ALGORITHM)
					: "PBEWITHSHA256AND256BITAES-CBC-BC";
			key = (properties.getProperty(KEY) != null) ? properties.getProperty(KEY)
					: "!cr3@t3#b@nk!ng!nt3ll!53n53#1601";

		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public static String getKey() {
		return key;
	}

	public static String getAlgorithm() {
		return algorithm;
	}

}
