package com.unacademy.cache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.unacademy.cache.service.DistributedCacheService;

@RestController
@RequestMapping(value = "/cache")
public class DistibutedCacheController {

	@Autowired
	DistributedCacheService cacheService;

	@RequestMapping(value = "/execute", method = RequestMethod.POST)
	public Object executeCommand(@RequestBody String command) {

		return cacheService.execute(command);
	}
}
