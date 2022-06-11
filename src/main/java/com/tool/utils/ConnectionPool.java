package com.tool.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;



public class ConnectionPool {
	private String poolName;
	private String urlDataSource;
	private String username;
	private String password;
	private HikariDataSource dataSource;
	
	public ConnectionPool(String poolName, String urlDataSource, String username, String password) {
		this.urlDataSource = urlDataSource;
		this.username = username;
		this.password = password;
		dataSource = getDataSourceFromConfig(poolName, urlDataSource, username, password);
	}
	
	public HikariDataSource getDataSourceFromConfig(String poolName, String urlDb, String userDb, String passDb) {
		HikariConfig jdbcConfig = new HikariConfig();
		jdbcConfig.setPoolName(poolName);
		jdbcConfig.setMaximumPoolSize(1000);
		jdbcConfig.setMinimumIdle(0);
		jdbcConfig.setIdleTimeout(300000);
		jdbcConfig.setJdbcUrl(urlDb);
		jdbcConfig.setUsername(username);
		jdbcConfig.setPassword(passDb);
		jdbcConfig.addDataSourceProperty("tcpKeepAlive", true);
		jdbcConfig.addDataSourceProperty("autoReconnect", true);
		jdbcConfig.addDataSourceProperty("cachePrepStmts", true);
		jdbcConfig.addDataSourceProperty("prepStmtCacheSize", 256);
		jdbcConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		jdbcConfig.addDataSourceProperty("useServerPrepStmts", true);
		jdbcConfig.setMaxLifetime(580000);
		
		return new HikariDataSource(jdbcConfig);
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getUrlDataSource() {
		return urlDataSource;
	}

	public void setUrlDataSource(String urlDataSource) {
		this.urlDataSource = urlDataSource;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public HikariDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	
}
