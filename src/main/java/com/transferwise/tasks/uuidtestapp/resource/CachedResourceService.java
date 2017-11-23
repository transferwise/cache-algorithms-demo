package com.transferwise.tasks.uuidtestapp.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CachedResourceService {
	public static enum CacheAlgorithm {
		COMMON, BLOCKING, REFRESHING
	}

	@Autowired
	private CommonCacheResourceService commonCacheResourceService;
	@Autowired
	private BlockingCacheResourceService blockingCacheResourceService;
	@Autowired
	private RefreshingCacheResourceService refreshingCacheResourceService;

	@Value("${caching.algorithm}")
	public void setCachingAlgorithm(String name) {
		cacheAlgorithm = CacheAlgorithm.valueOf(name);
	}

	private CacheAlgorithm cacheAlgorithm;

	public String getCachedResource(Long id) {
		switch (cacheAlgorithm) {
			case COMMON:
				return commonCacheResourceService.getResource(id);
			case BLOCKING:
				return blockingCacheResourceService.getResource(id);
			case REFRESHING:
				return refreshingCacheResourceService.getResource(id);
		}
		throw new IllegalStateException("Invalid algorithm.");
	}

	public String getCurrentCachedResource(Long id){
		switch (cacheAlgorithm) {
			case COMMON:
				return commonCacheResourceService.getCurrentCachedResource(id);
			case BLOCKING:
				return blockingCacheResourceService.getCurrentCachedResource(id);
			case REFRESHING:
				return refreshingCacheResourceService.getCurrentCachedResource(id);
		}
		throw new IllegalStateException("Invalid algorithm.");
	}
}
