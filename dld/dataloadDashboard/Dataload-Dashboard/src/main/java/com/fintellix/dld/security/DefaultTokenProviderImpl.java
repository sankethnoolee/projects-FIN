package com.fintellix.dld.security;

import com.fintellix.dld.util.DigestUtils;

public class DefaultTokenProviderImpl implements TokenProvider{

	@Override
	public String createToken() throws Throwable {
		return DigestUtils.getDigest() + "," + System.currentTimeMillis();
	}

}
