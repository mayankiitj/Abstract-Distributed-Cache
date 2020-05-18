package com.unacademy.cache.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ActiveCacheInstance {
	private Set<String> ips = new HashSet<>();

	public List<String> getIps() {
		return new ArrayList<>(ips);
	}

	public void setIp(String ip) {
		this.ips.add(ip);
		System.out.println("active instance " + ips.size());
	}

	public void setIps(List<String> ipAddress) {
		this.ips.addAll(ipAddress);
		System.out.println("active instance " + ips.size());
	}

}
