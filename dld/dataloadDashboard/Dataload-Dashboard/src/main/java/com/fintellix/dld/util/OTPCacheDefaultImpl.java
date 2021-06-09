package com.fintellix.dld.util;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class OTPCacheDefaultImpl implements OTPCache{
	private static final long MAX_SIZE = 10000;

	private Cache<String,Boolean> otpCache;
	private Integer expiryMinutes=5;
	
	public OTPCacheDefaultImpl(){
		otpCache = CacheBuilder.newBuilder().expireAfterAccess(expiryMinutes, TimeUnit.MINUTES).maximumSize(MAX_SIZE).build();
	}
	
	@Override
	public void set(String key, Boolean value) {
		otpCache.put(key, value);		
	}

	@Override
	public Boolean get(String key) {
		if (otpCache.getIfPresent(key)==null)
			return Boolean.FALSE;
		else
			return otpCache.getIfPresent(key);
	}

}
