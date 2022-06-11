package com.tool.contants;

public class Constants {
	public static final String URL_CONNECTION_ORACLE_SERVICE = "jdbc:oracle:thin:@%s:%s/%s";
	public static final String URL_CONNECTION_ORACLE_SID = "jdbc:oracle:thin:@%s:%s:%s";
	public static final String URL_CONNECTION_MYSQL = "jdbc:mysql://%s:%s/%s";
	public static final String URL_CONNECTION_SQLSERVER = "jdbc:sqlserver://%s:%s;database=%s";
	//will update another case of h2 database : https://www.h2database.com/html/features.html
	public static final String URL_CONNECTION_H2 = "jdbc:h2:~/%s;INIT=CREATE SCHEMA IF NOT EXISTS %s";
	
	public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
	public static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
	public static final String DRIVER_SQL_SERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String DRIVER_H2 = "org.h2.Driver";
	
	public static final String SELECT_STATEMENT_SQLSERVER = "SELECT TOP %s %s FROM %s";
	public static final String SELECT_STATEMENT_MYSQL_H2 = "SELECT %s FROM %s LIMIT %s";
	public static final String SELECT_STATEMENT_ORACLE = "SELECT %s FROM %s WHERE ROWNUM <= %s";
	
	public static final String COUNT_STATEMENT = "SELECT COUNT(%s) FROM %s";
}
