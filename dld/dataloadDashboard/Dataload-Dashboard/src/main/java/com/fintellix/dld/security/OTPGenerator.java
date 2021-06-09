package com.fintellix.dld.security;

import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fintellix.dld.util.KeyPropertyReader;
@Component
public class OTPGenerator { 

	private static Logger LOGGER = LoggerFactory.getLogger(OTPGenerator.class);
	private static EnvironmentStringPBEConfig config = null;
	private static StandardPBEStringEncryptor encryptor = null;
	private static TokenProvider provider = null;
	
	static

	{
		config = new EnvironmentStringPBEConfig();
		config.setAlgorithm(KeyPropertyReader.getAlgorithm());
		config.setPassword(KeyPropertyReader.getKey());

		encryptor = new StandardPBEStringEncryptor();
		encryptor.setProvider(new BouncyCastleProvider());
		encryptor.setConfig(config);
		try {
			provider = (TokenProvider) Class.forName("com.fintellix.dld.security.DefaultTokenProviderImpl").newInstance();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public OTPGenerator() {
		
		// do nothing.
	}

	public String getOtp() throws Throwable {
		return new String(Base64.getEncoder().encode(encryptor.encrypt(provider.createToken()).getBytes()));

	}
}

