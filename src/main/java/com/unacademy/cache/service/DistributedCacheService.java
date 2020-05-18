package com.unacademy.cache.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unacademy.cache.config.RestConnector;
import com.unacademy.cache.store.ActiveCacheInstance;
import com.unacademy.cache.store.IGenericCache;

@Service
public class DistributedCacheService {

	@Autowired
	@Qualifier("cacheDataStore")
	private IGenericCache genericCache;

	@Autowired
	RestConnector restConnector;

	@Autowired
	private HttpClient connectorClient;

	@Autowired
	private ActiveCacheInstance cacheInstance;

	@Value("${update.node.api.path}")
	private String api;

	public Object execute(String command) {

		boolean isValid = validateInput(command);

		if (!isValid) {
			return null;
		}

		try {
			String subInput[] = command.split(" ");
			if (subInput[0] == "GET") {
				return genericCache.get(subInput[1]);
			}

			if (subInput[0] == "SET") {
				int period = -1;
				if (subInput.length == 5 && subInput[3] == "EX") {
					period = (Integer.parseInt(subInput[4])) * 1000;
				} else if (subInput.length == 5 && subInput[3] == "PX") {
					period = (Integer.parseInt(subInput[4]));
				}

				genericCache.set(subInput[1], subInput[2], period);

				return "OK";
			}

			if (subInput[0] == "EXPIRE") {
				return genericCache.expire(subInput[1], (Integer.parseInt(subInput[4])) * 1000);
			}

		} catch (Exception e) {
			System.out.println("Error while processing command " + e);
		}
		return "Invalid input";
	}

	private boolean validateInput(String command) {

		if (command == null || command.split(" ").length < 2)
			return false;

		String split[] = command.split(" ");

		if (split[0] == "GET" && split.length != 2) {
			return false;
		}

		if (split[0] == "SET" && split.length != 3 && split.length != 5) {
			return false;
		}

		if (split[0] == "EXPIRE" && split.length != 3) {
			return false;
		}

		return true;
	}

	public void updateInstances() {
		Map<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		String requestBody;
		try {
			requestBody = new ObjectMapper().writeValueAsString(cacheInstance.getIps());
			for (String ip : cacheInstance.getIps()) {
				System.out.println(ip+" update "+requestBody);
				HttpEntity entity = restConnector.postMessages(connectorClient, requestBody, headers,
						"http://" + ip + api);
				EntityUtils.consume(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
