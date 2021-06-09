package com.fintellix.dld.security;

public interface TokenAuthenticator {
	public Boolean validateToken(String token) throws Throwable;

}
