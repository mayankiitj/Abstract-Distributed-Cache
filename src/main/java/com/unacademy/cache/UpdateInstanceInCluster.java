package com.unacademy.cache;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.unacademy.cache.config.RestConnector;

@Component
public class UpdateInstanceInCluster {

	@Autowired
	Environment environment;

	@Value("${seed.hosts}")
	private String seedHost;

	@Value("${add.node.api.path}")
	private String api;
	
	@Autowired
	RestConnector restConnector;
	
	@Autowired
	private HttpClient connectorClient;

	public void updateInstances() {
		InetAddress ip;

		String port = environment.getProperty("local.server.port");

		try {

			ip = InetAddress.getLocalHost();

			Map<String, String> headers = new HashMap<>();
			headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			
			String requestBody = ip.getHostAddress()+":"+port;
			HttpEntity entity = restConnector.postMessages(connectorClient, requestBody, headers, "http://" + seedHost + api);
			EntityUtils.consume(entity);
		} catch (Exception e) {

			e.printStackTrace();

		}
	}
}
