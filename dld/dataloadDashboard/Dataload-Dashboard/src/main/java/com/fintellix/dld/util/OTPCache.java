package com.fintellix.dld.util;

public interface OTPCache {

	public void set(String key, Boolean value);
	public Boolean get(String key);
}
