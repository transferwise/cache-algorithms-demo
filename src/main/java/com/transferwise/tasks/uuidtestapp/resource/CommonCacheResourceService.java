package com.transferwise.tasks.uuidtestapp.resource;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.expiry.Expirations.timeToLiveExpiration;

@Service
public class CommonCacheResourceService {
	@Autowired
	private ResourceService resourceService;

	private CacheManager cacheManager;
	private Cache<Long, String> cache;

	@PostConstruct
	public void init() {
		cacheManager = newCacheManagerBuilder()
				.withCache("myCache", newCacheConfigurationBuilder(
						Long.class, String.class, ResourcePoolsBuilder.heap(10))
						.withExpiry(timeToLiveExpiration(Duration.of(10, TimeUnit.SECONDS))))
				.build(true);
		cache = cacheManager.getCache("myCache", Long.class, String.class);
	}

	public String getResource(Long id) {
		String resource = cache.get(id);
		if (resource != null) {
			return resource;
		}
		resource = resourceService.fetchResource(id);
		cache.put(id, resource);
		return resource;
	}

	// For Visuals only.
	public String getCurrentCachedResource(Long id){
		return cache.get(id);
	}
}
