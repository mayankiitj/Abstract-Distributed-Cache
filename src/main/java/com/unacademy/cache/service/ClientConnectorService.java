package com.unacademy.cache.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.unacademy.cache.config.RestConnector;

@Service
public class ClientConnectorService {

	@Autowired
	DistributedCacheService cacheService;

	@Autowired
	HashingDistributionService hashingDistributionService;

	@Autowired
	Environment env;

	@Autowired
	RestConnector restConnector;

	@Autowired
	private HttpClient connectorClient;

	@Value("${execute.api.path}")
	String api;

	private static final int MAX_COUNTER = 1000000;

	private AtomicInteger counter = new AtomicInteger(0);

	public Object execute(String command) {
		String port = env.getProperty("local.server.port");
		String server = hashingDistributionService.getServerForKey(command.split(" ")[1]);
		String replica = null;
		if (server == null)
			return null;

		if (counter.get() > MAX_COUNTER) {
			counter.set(0);
		}

		if (command.split(" ")[0].equals("GET") && counter.getAndIncrement() % 2 == 1) {
			replica = hashingDistributionService.getReplicaSet().get(server);
		}

		// data is on server
		try {
			if (server.equals(InetAddress.getLocalHost().getHostAddress() + ":" + port)) {
				return cacheService.execute(command);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			if (replica != null)
				return updateDatatoNodes(replica, replica, command, hashingDistributionService.getReplicaSet());
			else {
				return updateDatatoNodes(server, replica, command, hashingDistributionService.getReplicaSet());
			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (replica != null)
					return updateDatatoNodes(server, replica, command, hashingDistributionService.getReplicaSet());
				else
					return updateDatatoNodes(hashingDistributionService.getReplicaSet().get(server), server, command,
							hashingDistributionService.getReplicaSet());
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}
		return null;
	}

	public Object updateDatatoNodes(String server, String replica, String command, Map<String, String> rSet)
			throws Exception {
		Map<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
		headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

		String requestBody = command;
		HttpEntity entity = restConnector.postMessages(connectorClient, requestBody, headers, "http://" + server + api);

		String response = EntityUtils.toString(entity);

		EntityUtils.consume(entity);

		if (replica == null) {
			HttpEntity entity2 = restConnector.postMessages(connectorClient, requestBody, headers,
					"http://" + rSet.get(server) + api);
			EntityUtils.consume(entity2);
		}
		return response;
	}

}
