package com.unacademy.cache.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ActiveCacheInstance {
	private Map<String, Integer> ips = new HashMap<>();

	public List<String> getIps() {
		return new ArrayList<>(ips.keySet());
	}

	public void setIp(String ip) {
		this.ips.put(ip, -1);
		System.out.println("active instance " + ips.size());
	}

	public void setIps(List<String> ipAddress) {
		for (String ip : ipAddress)
			this.ips.put(ip, -1);
		System.out.println("active instance " + ips.size());
	}

	public Map<String, Integer> getMap() {
		return ips;
	}

	public void setMap(Map<String, Integer> map) {
		this.ips = map;
	}

}
