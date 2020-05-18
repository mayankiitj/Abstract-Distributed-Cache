package com.unacademy.cache.store;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.springframework.stereotype.Repository;

@Repository("cacheDataStore")
public class CacheDataStore implements IGenericCache {
	private final ConcurrentHashMap<String, SoftReference<Object>> cache = new ConcurrentHashMap<>(16, .9f, 32);
	private final DelayQueue<ExpiryDelayedObject> expiryQueue = new DelayQueue<>();
	private final ConcurrentHashMap<String, ExpiryDelayedObject> expiryObjectSet = new ConcurrentHashMap<>();

	public CacheDataStore() {
		Thread expiryThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					ExpiryDelayedObject delayedCacheObject = expiryQueue.take();
					cache.remove(delayedCacheObject.getKey(), delayedCacheObject.getKeyReference());
					expiryObjectSet.remove(delayedCacheObject.getKey());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					System.out.println("error in removind data " + e);
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
		if (period != -1) {
			ExpiryDelayedObject edo = new ExpiryDelayedObject(key, reference, expiryTime);
			expiryQueue.put(edo);
			expiryObjectSet.put(key, edo);
		}

	}

	@Override
	public int expire(String key, long timeout) {
		if (cache.get(key) == null) {
			return 0;
		}
		if (timeout <= 0)
			cache.remove(key);
		else {
			if (expiryObjectSet.get(key) != null) {
				expiryQueue.remove(expiryObjectSet.get(key));
				ExpiryDelayedObject edo = new ExpiryDelayedObject(key, cache.get(key), timeout);
				expiryQueue.put(edo);
				expiryObjectSet.put(key, edo);
			}
		}

		return 1;
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
