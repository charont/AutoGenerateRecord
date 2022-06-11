package com.tool.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.tool.dto.ConfigDTO;

@Service
public class ReadFileConfig {
	private final String pathFile = "/config/config.json";
	
	public ConfigDTO readFileConfig() {
		
		try {
			String path = ReadFileConfig.class.getResource(pathFile).getPath();
			String jsonStr;
			jsonStr = new String(Files.readAllBytes(Paths.get(path.replaceFirst("/", ""))));
			ConfigDTO config = new Gson().fromJson(jsonStr, ConfigDTO.class);
			return config;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
