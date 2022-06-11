package com.tool.contants;

import java.util.Arrays;

public enum DatabaseDriverEnum {
	ORACLE("Oracle"),
	MYSQL("MySQL"),
	SQL_SERVER("MS SQL"),
	H2("h2");
	
	private String value;
	
	DatabaseDriverEnum(String value){
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static DatabaseDriverEnum getDriverByValue(String value) {
		return Arrays
				.asList(DatabaseDriverEnum.values())
				.stream()
				.filter(d -> d.value.equalsIgnoreCase(value))
				.findAny().orElse(null);
	}
}
