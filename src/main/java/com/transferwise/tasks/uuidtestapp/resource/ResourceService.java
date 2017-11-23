package com.transferwise.tasks.uuidtestapp.resource;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ResourceService {
	private AtomicInteger concurrentRequestsCount = new AtomicInteger();

	private AtomicLong startTimeMs = new AtomicLong();
	private volatile String resourceValue = "0";
	private int maxConcurrency = 0;

	/**
	 * Normally takes 2.5 seconds to respond.
	 */
	public String fetchResource(Long id) {
		concurrentRequestsCount.incrementAndGet();
		try {
			for (int i = 0; i < 10; i++) {
				int concurrency = concurrentRequestsCount.get();
				synchronized (this){
					maxConcurrency = Math.max(maxConcurrency, concurrency);
				}
				long timeTakenMs = 250;
				timeTakenMs = (long) (timeTakenMs * Math.pow(1.4, concurrency - 1));
				sleep(timeTakenMs);
			}
			return resourceValue;
		} finally {
			concurrentRequestsCount.decrementAndGet();
		}
	}

	public int getConcurrency() {
		return concurrentRequestsCount.get();
	}

	public String getResourceValue() {
		startTimeMs.compareAndSet(0, System.currentTimeMillis());
		int version = (int) ((System.currentTimeMillis() - startTimeMs.get()) / 1000.0);
		resourceValue = String.valueOf(version);
		return resourceValue;
	}

	private void sleep(long timeTakenMs) {
		try {
			Thread.sleep(timeTakenMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getMaxConcurrency() {
		return maxConcurrency;
	}
}
