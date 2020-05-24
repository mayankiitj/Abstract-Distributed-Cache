package com.unacademy.cache.store;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

public interface IGenericCache {
	public void set(String key, Object value, long period);

	public int expire(String key, long timeout);

	public Object get(String key);

	public void clear();

	public long size();

	public ConcurrentHashMap<String, SoftReference<Object>> getMap();

	public ConcurrentHashMap<String, ExpiryDelayedObject> expirySet();

}
