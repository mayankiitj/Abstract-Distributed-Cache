package com.unacademy.cache.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.unacademy.cache.store.ActiveCacheInstance;
import com.unacademy.cache.store.ExpiryDelayedObject;
import com.unacademy.cache.store.IGenericCache;

@Service
public class HashingDistributionService {

	private Map<String, String> replicaSet = new HashMap<>();

	@Autowired
	ActiveCacheInstance cacheInstance;

	@Autowired
	@Qualifier("cacheDataStore")
	private IGenericCache genericCache;

	@Autowired
	Environment environment;

	@Autowired
	ClientConnectorService connectorService;

	public String getServerForKey(String key) {
		int noDataNodes = 0;
		int index = key.hashCode() % 360;
		if (index < 0)
			index = 360 + index;
		int min = Integer.MAX_VALUE;
		String server = null;

		Map<String, Integer> map = cacheInstance.getMap();
		if (map == null || map.size() == 0)
			return null;
		for (String k : map.keySet()) {
			if (map.get(k) == -1) {
				noDataNodes++;
				continue;
			}
			if (Math.abs(map.get(k) - index) < min) {
				min = Math.abs(map.get(k) - index);
				server = k;
			}
		}

		if (noDataNodes == map.size()) {
			updateHashDistribution();
			return getServerForKey(key);
		} else if (noDataNodes > 0) {
			reassignkeys(1, null);
		}

		return server;
	}

	public void updateReplicaSet(Map<String, String> rSet) {
		int size = cacheInstance.getIps().size();

		for (int i = 0; i < size; i++) {
			rSet.put(cacheInstance.getIps().get(i % size), cacheInstance.getIps().get((i + 1) % size));
		}
	}

	public void updateHashDistribution() {
		Map<String, Integer> map = cacheInstance.getMap();

		int size = 360 / map.size();
		int count = 0;

		for (String key : map.keySet()) {
			map.put(key, count);
			count += size;
		}

		updateReplicaSet(replicaSet);

	}

	@Async
	public void reassignkeys(int nodeAddCount, String address) {
		List<String> map = cacheInstance.getIps();
		Map<String, Integer> tempMap = new HashMap<>();
		Map<String, String> rSet = new HashMap<>();

		updateReplicaSet(rSet);

		int size = 360 / map.size();
		int count = 0;

		for (String key : map) {
			tempMap.put(key, count);
			count += size;
		}

		if (nodeAddCount <= 0) {
			int degree = -nodeAddCount;
			int range = 360 / (map.size() + 1);
			int maxRange = (degree + (range / 2)) % 360;
			int minRange = (degree - (range - range / 2)) % 360;
			if (minRange < 0) {
				minRange += 360;
			}

			if (maxRange < minRange) {
				degree = maxRange;
				maxRange = minRange;
				minRange = degree;
			}

			String replicaNode = replicaSet.get(address);
			String nodeReplica = null;
			for (String s : replicaSet.keySet()) {
				if (replicaSet.get(s).equals(address)) {
					nodeReplica = s;
				}
			}
			String port = environment.getProperty("local.server.port");
			try {
				String currentNode = InetAddress.getLocalHost().getHostAddress() + ":" + port;
				if (replicaNode.equals(currentNode)) {
					System.out.println("range " + maxRange + " " + minRange);
					for (String key : genericCache.getMap().keySet()) {
						int i = key.hashCode() % 360;
						if (i < 0) {
							i = 360 + i;
						}
						if ((maxRange - minRange == range && i > minRange && i < maxRange)
								|| ((maxRange - minRange) != range && (i > maxRange || i < minRange))) {

							int min = Integer.MAX_VALUE;
							String server = null;
							for (String k : tempMap.keySet()) {
								if (Math.abs(tempMap.get(k) - i) < min) {
									server = k;
									min = Math.abs(tempMap.get(k) - i);
								}
							}
							if (server != null) {
								String replica = null;
								if (rSet.get(server).equals(currentNode)) {
									replica = server;
								} else if (server.equals(currentNode)) {
									server = rSet.get(server);
									replica = server;
								} else {
									replica = null;
								}
								try {
									String commnd = "SET " + key + " " + genericCache.get(key);
									ExpiryDelayedObject edo = genericCache.expirySet().get(key);
									if (edo != null) {
										commnd += " PX "
												+ String.valueOf(edo.getExpiryTime() - System.currentTimeMillis());
									}
									connectorService.updateDatatoNodes(server, replica, commnd, rSet);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				} else if (nodeReplica.equals(currentNode)) {
					int pos = tempMap.get(currentNode);
					range = 360 / tempMap.size();
					maxRange = (pos + (range / 2)) % 360;
					minRange = (pos - (range - range / 2)) % 360;
					if (minRange < 0)
						minRange += 360;

					if (maxRange < minRange) {
						degree = maxRange;
						maxRange = minRange;
						minRange = degree;
					}

					String repServer = rSet.get(currentNode);
					for (String key : genericCache.getMap().keySet()) {
						int i = key.hashCode() % 360;
						if (i < 0) {
							i = 360 + i;
						}

						if ((maxRange - minRange == range && i > minRange && i < maxRange)
								|| (maxRange - minRange != range && (i > maxRange || i < minRange))) {
							try {
								String commnd = "SET " + key + " " + genericCache.get(key);
								ExpiryDelayedObject edo = genericCache.expirySet().get(key);
								if (edo != null) {
									commnd += " PX " + String.valueOf(edo.getExpiryTime() - System.currentTimeMillis());
								}
								connectorService.updateDatatoNodes(repServer, repServer, commnd, rSet);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

					}
				}
				cacheInstance.setMap(tempMap);
				replicaSet = rSet;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

		} else if (nodeAddCount > 0) {
			// TODO
		}

	}

	public Map<String, String> getReplicaSet() {
		return replicaSet;
	}

}
