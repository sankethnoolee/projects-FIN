/**
 * 
 */
package com.fintellix.validationrestservice.util.connectionManager;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.validationrestservice.util.KeyPropertyReader;

/**
 * @author sumeet.tripathi
 *
 */
public class CryptoDataSource extends BasicDataSource {

	private static Logger LOGGER = LoggerFactory.getLogger(CryptoDataSource.class);

	@Override
	public void setPassword(String encryptedPassword) {
		super.setPassword(decrypt(encryptedPassword));

	}

	/**
	 * Decrypt password
	 * 
	 * @param encryptedPassword
	 * @return
	 */
	public String decrypt(String encryptedPassword) {

		String password = null;
		if (!encryptedPassword.contains("ENC(")) {
			return encryptedPassword;
		}

		try {

			KeyPropertyReader reader = new KeyPropertyReader();
			EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
			config.setAlgorithm(reader.getAlgorithm());
			config.setPassword(reader.getKey());
			StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
			decryptor.setProvider(new BouncyCastleProvider());
			decryptor.setConfig(config);
			password = decryptor
					.decrypt(encryptedPassword.trim().substring(4, (encryptedPassword.trim().length() - 1)));

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new IllegalArgumentException("Failed decryption, may be key.properties is missing.");
		}

		return password;

	}

}
