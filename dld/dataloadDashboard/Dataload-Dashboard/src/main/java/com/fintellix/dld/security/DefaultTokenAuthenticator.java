package com.fintellix.dld.security;

public class DefaultTokenAuthenticator implements TokenAuthenticator{

	private long MAX_TOKEN_ALIVE_TIME=300000;
	
	@Override
	public Boolean validateToken(String token) throws Throwable {

		Boolean isValid = Boolean.FALSE;
		long currentTime = System.currentTimeMillis();
		String[] decryptedPwdArray = token.split(",");
		Long x = Long.parseLong(decryptedPwdArray[1]);

		if (((x.longValue() + MAX_TOKEN_ALIVE_TIME) >= currentTime) && ((x.longValue() - MAX_TOKEN_ALIVE_TIME) <= currentTime)) {
			isValid = Boolean.TRUE;
		}

		return isValid;
	}
}
