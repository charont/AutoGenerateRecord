package com.tool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tool.service.RunScriptServiceImpl;

@RestController
public class RunScriptController {
	@Autowired
	private RunScriptServiceImpl runScript;
	
	@PostMapping(value = "/api")
	public void runScript() {
		runScript.runScript();
	}
}
