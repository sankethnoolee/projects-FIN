package com.fintellix.dld.security;

public interface TokenProvider {

	public String createToken() throws Throwable;
}
