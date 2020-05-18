package com.unacademy.cache.job;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.unacademy.cache.config.RestConnector;
import com.unacademy.cache.store.ActiveCacheInstance;

@Component
public class ClusterConnectionCheckJob {

	private Map<String, Integer> connection = new HashMap<>();

	@Autowired
	RestConnector restConnector;

	@Value("${gossip.node.api.path}")
	private String api;

	@Autowired
	private ActiveCacheInstance cacheInstance;

	@Autowired
	Environment environment;

	@Scheduled(fixedDelayString = "5000")
	public void sendGossip() {

		InetAddress ip;

		String port = environment.getProperty("local.server.port");

		HashMap<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		try {
			ip = InetAddress.getLocalHost();
			for (String address : cacheInstance.getIps()) {
				if ((ip.getHostAddress() + ":" + port).equals(address))
					continue;
				System.out.println(address+" gossip");
				Map<String, String> entity = restConnector.getMessage("http://" + address + api, headers);
				if (entity == null || !entity.get("responseBody").equals("true")) {
					if (connection.get(address) != null && connection.get(address) < 2) {
						connection.put(address, connection.get(address) + 1);
					} else if (connection.get(address) != null && connection.get(address) >= 2) {
						updateActiveNodesandHashing(address);
					} else {
						connection.put(address, 1);
					}
				} else {
					if (connection.get(address) != null) {
						connection.put(address, 0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateActiveNodesandHashing(String address) {
		System.out.println("Node down "+address);

	}
}
