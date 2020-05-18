package com.unacademy.cache.store;

public interface IGenericCache {
	public void set(String key, Object value, long period);

	public void expire(String key);

	public Object get(String key);

	public void clear();

	public long size();

}
