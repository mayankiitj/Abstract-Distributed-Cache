package com.unacademy.cache.store;

import java.lang.ref.SoftReference;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ExpiryDelayedObject implements Delayed {

    private final String key;
    private final SoftReference<Object> keyReference;
    private final long expiryTime;
    
	@Override
	public int compareTo(Delayed o) {
		return Long.compare(expiryTime, ((ExpiryDelayedObject) o).expiryTime);
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
}
