package com.unacademy.cache.store;

import java.lang.ref.SoftReference;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.springframework.stereotype.Repository;

@Repository
public class CacheDataStore implements IGenericCache {
	private final ConcurrentHashMap<String, SoftReference<Object>> cache = new ConcurrentHashMap<>(16, .9f,32);
	private final DelayQueue<ExpiryDelayedObject> expiryQueue = new DelayQueue<>();

	public CacheDataStore() {
		Thread expiryThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					ExpiryDelayedObject delayedCacheObject = expiryQueue.take();
					cache.remove(delayedCacheObject.getKey(), delayedCacheObject.getKeyReference());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		expiryThread.setDaemon(true);
		expiryThread.start();
	}

	@Override
	public void set(String key, Object value, long period) {
		if (key == null) {
			return;
		}
		long expiryTime = System.currentTimeMillis() + period;
		SoftReference<Object> reference = new SoftReference<>(value);
		cache.put(key, reference);
		expiryQueue.put(new ExpiryDelayedObject(key, reference, expiryTime));

	}

	@Override
	public void expire(String key) {
		cache.remove(key);
	}

	@Override
	public Object get(String key) {
		if (key == null)
			return null;
		return Optional.ofNullable(cache.get(key)).map(SoftReference::get).orElse(null);
	}

	@Override
	public void clear() {
		cache.clear();

	}

	@Override
	public long size() {
		return cache.size();
	}

}
