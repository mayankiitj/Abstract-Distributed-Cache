package com.unacademy.cache.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.unacademy.cache.service.DistributedCacheService;
import com.unacademy.cache.store.ActiveCacheInstance;

@RestController
@RequestMapping(value = "/node")
public class ClusterNodesContoller {

	@Autowired
	private ActiveCacheInstance instance;

	@Autowired
	private DistributedCacheService service;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public void add(@RequestBody String address) {
		System.out.println("add request " + address);
		instance.setIp(address);

		service.updateInstances();
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public void update(@RequestBody List<String> address) {
		instance.setIps(address);
	}
}
