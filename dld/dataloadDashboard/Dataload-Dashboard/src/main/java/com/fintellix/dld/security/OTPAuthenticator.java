package com.fintellix.dld.security;

import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.dld.util.KeyPropertyReader;
import com.fintellix.dld.util.OTPCache;

public class OTPAuthenticator {
	static Logger LOGGER = LoggerFactory.getLogger(OTPAuthenticator.class);

	private static EnvironmentStringPBEConfig config = null;
	private static StandardPBEStringEncryptor encryptor = null;
	private static final OTPAuthenticator instance = new OTPAuthenticator();
	private OTPCache cacheX = null;
	private TokenAuthenticator authenticator = null;

	/**
	 * @param args
	 */

	private OTPAuthenticator() {
		String clazz = null;
		try {
			clazz = "com.fintellix.dld.util.OTPCacheDefaultImpl";
			LOGGER.info("cache provider ::"+clazz);
			cacheX = (OTPCache) Class.forName(clazz).newInstance();
			
			clazz = "com.fintellix.dld.security.DefaultTokenAuthenticator";
			LOGGER.info("authentication provider ::"+clazz);
			authenticator = (TokenAuthenticator) Class.forName(clazz).newInstance();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		config = new EnvironmentStringPBEConfig();
		config.setAlgorithm(KeyPropertyReader.getAlgorithm());
		config.setPassword(KeyPropertyReader.getKey());

		encryptor = new StandardPBEStringEncryptor();
		encryptor.setProvider(new BouncyCastleProvider());
		encryptor.setConfig(config);

	}

	public static OTPAuthenticator getInstance() {
		return instance;
	}

	public void assertToken(String token) throws Throwable {

		if (token == null)
			throw new IllegalArgumentException("Token missing in request");
		String decrytedOTP = encryptor.decrypt(new String(Base64.getDecoder().decode(token)));
		if(cacheX==null){
			LOGGER.info("cache is null");
		}
		
		if (authenticator.validateToken(decrytedOTP)) {
			cacheX.set(decrytedOTP, Boolean.TRUE);
		}else{
			throw new IllegalStateException("Not a valid token");
		}

		

	}
}
