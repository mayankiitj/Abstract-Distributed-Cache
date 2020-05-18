package com.unacademy.cache.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/gossip")
public class GossipController {

	@RequestMapping(method = RequestMethod.GET)
	public boolean gossip() {
		return true;
	}
}
