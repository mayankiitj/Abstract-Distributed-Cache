package com.unacademy.cache.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.unacademy.cache.service.ClientConnectorService;
import com.unacademy.cache.store.ActiveCacheInstance;

@RestController
@RequestMapping(value = "/server")
public class ClientConnectorController {

	@Autowired
	ClientConnectorService connectorService;

	@Autowired
	ActiveCacheInstance activeInstance;

	@RequestMapping(value = "/execute", method = RequestMethod.POST)
	public Object executeCommand(@RequestBody String command) {

		return connectorService.execute(command);
	}

	@RequestMapping(value = "/nodes", method = RequestMethod.GET)
	public List<String> executeCommand() {

		return activeInstance.getIps();
	}
}
