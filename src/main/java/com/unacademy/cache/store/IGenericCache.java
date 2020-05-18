package com.unacademy.cache.store;

public interface IGenericCache {
	public void set(String key, Object value, long period);

	public int expire(String key, long timeout);

	public Object get(String key);

	public void clear();

	public long size();

}
