package com.adp.esi.digitech.ds.config.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.util.ValidationUtil;

@Service
public class CacheService {
	
	@Autowired
	private CacheManager cacheManager;

	public void evictCache(String cacheName, String key) {
		var cache = cacheManager.getCache(cacheName);
		if(cache == null) {
			throw new ConfigurationException("Invalid cacheName");
		}
		boolean isKeyPresent = true;
		
		if(ValidationUtil.isHavingValue(key))			
			isKeyPresent = cache.evictIfPresent(key);
			if(!isKeyPresent)
				throw new ConfigurationException("Invalid key, cache not cleared");
		else cache.clear();
		
		
	}


}
