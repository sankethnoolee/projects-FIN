package com.fintellix.validationrestservice.util.connectionManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.util.KeyPropertyReader;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class DecryptDataSource extends HikariDataSource{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DecryptDataSource.class);
	private static final String key = KeyPropertyReader.getKey();
	private static final String ALGORITHM = KeyPropertyReader.getAlgorithm();

	@Override
	public void setPassword(String encryptedPassword) {
		super.setPassword(decryptPassword(encryptedPassword));

	}
	
	private String decryptPassword(String encryptedPassword) {
		LOGGER.debug("DecryptDataSource > decryptPassword -->");
		if(encryptedPassword.toUpperCase().contains("ENC(")){
    		EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
    		config.setAlgorithm(ALGORITHM);
    		config.setPassword(key);
    		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    		encryptor.setProvider(new BouncyCastleProvider());                                                                                                    
    		encryptor.setConfig(config);
    		String password = encryptor.decrypt(encryptedPassword.substring(3, encryptedPassword.length()-1));
    		return password;
    	}else{
    		return encryptedPassword;
    	}
		
	}
	

}
