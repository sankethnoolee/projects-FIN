package com.fintellix.validationrestservice.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class DigestUtils {
	
	private static final String DIGEST_SHA_1 = "SHA-1";
	private static final String DIGEST_SHA1PRNG = "SHA1PRNG";

	private static final char[] digits = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Generates message digest.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static String getDigest() throws Throwable {

		SecureRandom prng = SecureRandom.getInstance(DIGEST_SHA1PRNG);
		String randomNum = new Integer(prng.nextInt()).toString();
		MessageDigest sha = MessageDigest.getInstance(DIGEST_SHA_1);
		return hexEncode(sha.digest(randomNum.getBytes()));
	}

	private static String hexEncode(byte[] aInput) {
		StringBuilder result = new StringBuilder();
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}

}
