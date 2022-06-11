package com.tool.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ConfigDTO {
	private String connectionName;
	private int sizeRow;
	private String ipAddress;
	private String port;
	private String username;
	private String password;
	private String databaseName;
	private String schema;
	private boolean isSID;
	private TableInfo tableInfo;
	private List<TableJoinInfo> tableJoinInfo = new ArrayList<>();
	
	
	@Data
	public static class TableInfo{
		private String tableName;
		private List<ColumnInfo> columnInfo = new ArrayList<>();
	}
	
	@Data
	public static class ColumnInfo{
		private String columnName;
		private String columnType;
		private boolean isAutoIncrement;
	}
	
	@Data
	public static class TableJoinInfo{
		private String tableNameJoin;
		private String columnNameJoin;
		private String fk;
	}
	
}
