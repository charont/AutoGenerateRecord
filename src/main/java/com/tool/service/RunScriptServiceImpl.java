package com.tool.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.javafaker.Faker;
import com.tool.contants.Constants;
import com.tool.contants.DatabaseDriverEnum;
import com.tool.dto.ConfigDTO;
import com.tool.dto.ConfigDTO.ColumnInfo;
import com.tool.dto.ConfigDTO.TableJoinInfo;
import com.tool.utils.ConnectionPool;

@Service
public class RunScriptServiceImpl {

	@Autowired
	private ReadFileConfig readFileConfig;
	private Faker faker = new Faker();
	private final String namePool = "read-file-json";

	private Connection getConnection(ConfigDTO configDTO) throws SQLException {
		String url = getConnectionString(configDTO.isSID(), configDTO.getConnectionName(), configDTO.getIpAddress(),
				configDTO.getPort(), configDTO.getDatabaseName());
		ConnectionPool connSource = new ConnectionPool(namePool, url, configDTO.getUsername(), configDTO.getPassword());
		return connSource.getDataSource().getConnection();
	}
	
	private boolean validateConfig(ConfigDTO configDTO) {
		if(StringUtils.isBlank(configDTO.getConnectionName()) || 
				StringUtils.isBlank(configDTO.getIpAddress()) ||
				StringUtils.isBlank(configDTO.getPort()) ||
				StringUtils.isBlank(configDTO.getUsername()) ||
				StringUtils.isBlank(configDTO.getPassword()) ||
				StringUtils.isBlank(configDTO.getDatabaseName()) ||
				configDTO.getSizeRow() < 100)
			return false;
		
		return true;
	}
	
	public void runScript() {
		try {
			ConfigDTO configDTO = readFileConfig.readFileConfig();
			this.validateConfig(configDTO);
			Connection conn = this.getConnection(configDTO);
			List<List<Object>> insertBuffer = new ArrayList<>();
			
			long sizeRow = configDTO.getSizeRow();
			int loop = (int) sizeRow / 10000;
			long modLoop = (int) sizeRow % 10000;
			if (loop <= 0) {
				loop = 1;
				modLoop = 0L;
			} else
				sizeRow = 10000L;
			
			CallableStatement insertStatement = null;
			String prefix = getPrefix(configDTO);
			String insertQuery = buildInsertQuery(prefix, configDTO);
			insertStatement = conn.prepareCall(insertQuery);
			// du7a9 for ra ngoÃ i
			try {
				conn.setAutoCommit(false);
				for (int i = 0; i < loop; i++) {
					insertBuffer = buildDataInsert(conn,sizeRow, configDTO);
					writeToTarget(insertStatement, insertBuffer);
					conn.commit();
					insertBuffer.clear();
				}
				if (modLoop != 0) {
					insertBuffer = buildDataInsert(conn,modLoop, configDTO);
					writeToTarget(insertStatement, insertBuffer);
					conn.commit();
					insertBuffer.clear();
				}
			} catch (Exception e) {
				conn.rollback();
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	public String gennerateRandomString(String columnName) {
		String rs = "";
		if (columnName.contains("name") || columnName.contains("hoten")) {
			rs = faker.name().name();
			if (rs.length() >= 40)
				rs = rs.substring(0, 40);
			return rs;
		}
		if (columnName.contains("Address") || columnName.contains("dchi")) {
			rs = faker.address().fullAddress();
			if (rs.length() >= 50)
				rs = rs.substring(0, 50);
			;
			return rs;
		}
		if (columnName.contains("Column")) {
			rs = generateString(columnName);
			if (rs.length() > 10)
				rs = rs.substring(0, 10);
			return rs;
		}
		if (columnName.contains("Detail")) {
			rs = faker.commerce().material();
			if (rs.length() > 10)
				rs = rs.substring(0, 10);
			return rs;
		}
		if (columnName.contains("email"))
			return faker.internet().emailAddress();
		if (columnName.contains("Phone") || columnName.contains("dt"))
			return faker.phoneNumber().subscriberNumber(9).replaceFirst("[0-9]", "09");
		if (columnName.contains("tensp"))
			return faker.commerce().productName();
		if (columnName.contains("ngsinh") || columnName.contains("ngvl") || columnName.contains("Date"))
			return new SimpleDateFormat("dd/MM/yyyy").format(faker.date().future(1, TimeUnit.DAYS));
		if (columnName.contains("ngdk"))
			return String.valueOf(faker.random().nextInt(1, 31));
		if (columnName.contains("ma"))
			return faker.commerce().promotionCode(10) + UUID.randomUUID().toString().substring(0, 4);
		if (columnName.contains("nuocsx")) {
			rs = faker.country().name();
			if (rs.length() > 40)
				rs = rs.substring(0, 40);
			return rs;
		}
		if (columnName.contains("city"))
			return faker.address().cityName();
		return generateString(columnName);
	}

	public long generateBigNumber(String columnName) {
		return faker.number().numberBetween(1, 300000000L);
	}

	public int generateNumber(String columnName) {
		return faker.number().numberBetween(100, 1000000);
	}

	public Date generateDateTime(String columnName) {
		return faker.date().birthday();
	}

	public Double generateDouble(String columnName) {
		return faker.number().randomDouble(5, 0, 100000);
	}

	public BigDecimal generateBigDecimal(long min, long max) {
		if (min == max) {
			return new BigDecimal(min);
		}
		final long trueMin = Math.min(min, max);
		final long trueMax = Math.max(min, max);

		final double range = (double) trueMax - (double) trueMin;

		final double chunkCount = Math.sqrt(Math.abs(range));
		final double chunkSize = chunkCount;
		final long randomChunk = faker.random().nextLong((long) chunkCount);

		final double chunkStart = trueMin + randomChunk * chunkSize;
		final double adj = chunkSize * faker.random().nextDouble();
		return new BigDecimal(chunkStart + adj).setScale(0, RoundingMode.CEILING);
	}

	public boolean generateBoolean(String columnName) {
		return faker.bool().bool();
	}

	public String generateString(String columnName) {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

		return generatedString;
	}

	private String getConnectionString(boolean isSID, String... object) {
		String url = "";
		switch (DatabaseDriverEnum.getDriverByValue(object[0])) {
		case ORACLE:
			url = String.format(Constants.URL_CONNECTION_ORACLE_SERVICE, object[1], object[2], object[3]);
			if (isSID)
				url = String.format(Constants.URL_CONNECTION_ORACLE_SID, object[1], object[2], object[3]);
			break;
		case MYSQL:
			url = String.format(Constants.URL_CONNECTION_MYSQL, object[1], object[2], object[3]);
			break;
		case SQL_SERVER:
			url = String.format(Constants.URL_CONNECTION_SQLSERVER, object[1], object[2], object[3]);
			break;
			// ERROR
		case H2:
			url = String.format(Constants.URL_CONNECTION_H2, object[3], object[3]);
			break;
		}
		return url;
	}
	
	public Object getRndObject(List<Object> pkData) {
		return pkData.get(RandomUtils.nextInt(0, pkData.size()));
	}

	public String buildInsertQuery(String prefix, ConfigDTO configDTO) {
		String result = "";
		List<ColumnInfo> columns = configDTO.getTableInfo().getColumnInfo();
		if (!configDTO.getTableInfo().getColumnInfo().isEmpty()) {
			String colInsert = "";
			String valueInsert = "";
			for (ColumnInfo column : columns) {
				if (!column.isAutoIncrement()) {
					colInsert += column.getColumnName() + ",";
					valueInsert += "?,";
				}

			}
			colInsert = colInsert.substring(0, colInsert.length() - 1);
			valueInsert = valueInsert.substring(0, valueInsert.length() - 1);
			StringBuilder sqlQuery = new StringBuilder();
			sqlQuery.append("INSERT INTO " + configDTO.getSchema() + configDTO.getTableInfo().getTableName() + "(");
			sqlQuery.append(colInsert + ")");
			sqlQuery.append(" VALUES(");
			sqlQuery.append(valueInsert + ")");
			result = sqlQuery.toString();
		}
		return result;
	}

	public Map<String, Method> invokeRandomMethod(List<ColumnInfo> columnInfo)
			throws NoSuchMethodException, SecurityException {
		Map<String, Method> result = new HashMap<String, Method>();
		for (ColumnInfo col : columnInfo) {
			if (!col.isAutoIncrement()) {
				String columnType = col.getColumnType().toUpperCase();
				switch (columnType) {
				case "CHAR":
				case "VARCHAR":
				case "TEXT":
				case "NCHAR":
				case "VARCHAR2":
				case "NVARCHAR2":
				case "CLOB":
				case "NCLOB":
				case "LONG":
				case "TINYTEXT":
				case "MEDIUMTEXT":
				case "LONGTEXT":
				case "ENUM":
				case "SET":
				case "NVARCHAR":
				case "NTEXT":
					Method randomStrInstanceMethod = RunScriptServiceImpl.class.getMethod("gennerateRandomString",
							String.class);
					result.put(col.getColumnName(), randomStrInstanceMethod);
					break;
				case "BIGINT":
				case "LONGINTEGER":
					Method randomBigIntInstanceMethod = RunScriptServiceImpl.class.getMethod("generateBigNumber",
							String.class);
					result.put(col.getColumnName(), randomBigIntInstanceMethod);
//						rowInsert.add(generateBigNumber());
					break;
				case "INT":
				case "SMALLINT":
				case "NUMBER":
				case "SHORTINTEGER":
				case "INT8":
				case "TINYINT":
				case "MEDIUMINT":
					Method randomIntInstanceMethod = RunScriptServiceImpl.class.getMethod("generateNumber",
							String.class);
					result.put(col.getColumnName(), randomIntInstanceMethod);
//					rowInsert.add(generateNumber(jsonCol.getString("columnName")));
					break;
				case "BIT":
					Method randomIBoolInstanceMethod = RunScriptServiceImpl.class.getMethod("generateBoolean",
							String.class);
					result.put(col.getColumnName(), randomIBoolInstanceMethod);
					break;
				case "NUMERIC":
				case "DECIMAL":
				case "DEC":
				case "MONEY":
					Method randomDecInstanceMethod = RunScriptServiceImpl.class.getMethod("generateBigDecimal",
							String.class);
					result.put(col.getColumnName(), randomDecInstanceMethod);
					break;
				case "DATE":
				case "TIMESTAMP":
				case "TIMESTAMP WITH TIME ZONE":
				case "TIMESTAMP WITH LOCAL TIME ZONE":
				case "INTERVAL YEAR TO MONTH":
				case "INTERVAL DAY TO SECOND":
				case "DATETIME":
				case "YEAR":
				case "DATETIMEOFFSET":
				case "TIME":
				case "DATETIME2":
				case "SMALLDATETIME":
					Method randomDateInstanceMethod = RunScriptServiceImpl.class.getMethod("generateDateTime",
							String.class);
					result.put(col.getColumnName(), randomDateInstanceMethod);
					break;
				case "REAL":
				case "SHORTDECIMAL":
				case "SMALLFLOAT":
				case "FLOAT":
				case "BINARY_FLOAT":
				case "BINARY_DOUBLE":
				case "DOUBLE PRECISION":
				case "FIXED":
				case "SMALLMONEY":
					Method randomDoubInstanceMethod = RunScriptServiceImpl.class.getMethod("generateDouble",
							String.class);
					result.put(col.getColumnName(), randomDoubInstanceMethod);
					break;
				}
			}
		}
		return result;
	}

	public List<List<Object>> buildDataInsert(Connection conn,long sizeRow, ConfigDTO configDTO) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<List<Object>> result = new ArrayList<>();
		Map<String, Method> methods = invokeRandomMethod(configDTO.getTableInfo().getColumnInfo());
		Map<String, List<Object>> dataOfPk = getPkData(conn,configDTO);
		List<String> columns = getColumnNameFromConfig(configDTO.getTableInfo().getColumnInfo());
		if (!configDTO.getTableInfo().getColumnInfo().isEmpty()) {
			for (long i = 0; i < sizeRow; i++) {
				List<Object> rowInsert = new ArrayList<Object>();
				for (String columnName : columns) {
					if(dataOfPk.containsKey(columnName))
						rowInsert.add(getRndObject(dataOfPk.get(columnName)));
					else
						rowInsert.add(methods.get(columnName).invoke(this, columnName));
				}
				result.add(rowInsert);
			}

		}
		return result;
	}

	private List<String> getColumnNameFromConfig(List<ColumnInfo> columnInfo) {
		return columnInfo.stream().filter(col -> !col.isAutoIncrement()).map(col -> col.getColumnName())
				.collect(Collectors.toList());
	}

	private void buildParam(CallableStatement callableStatement, List<Object> listColumns) throws Exception {
		int i = 1;
		for (Object col : listColumns) {
			setValue(callableStatement, i, col);
			i++;
		}
	}

	private void writeToTarget(CallableStatement callableStatement, List<List<Object>> insertBuffer) throws Exception {
		for (List<Object> item : insertBuffer) {
			buildParam(callableStatement, item);
			callableStatement.addBatch();
		}
		callableStatement.executeBatch();
	}

	public void setValue(CallableStatement callableStatement, int paramNum, Object value) throws SQLException {
		if (value instanceof String) {
			callableStatement.setString(paramNum, value.toString());
		}
		if (value instanceof Integer) {
			callableStatement.setInt(paramNum, (Integer) value);
		}
		if (value instanceof Long) {
			callableStatement.setLong(paramNum, (long) value);
		}
		if (value instanceof Double) {
			callableStatement.setDouble(paramNum, (double) value);
		}
		if (value instanceof Date) {
			Date valueDate = (Date) value;
			callableStatement.setDate(paramNum, new java.sql.Date(valueDate.getTime()));
		}
		if (value instanceof Boolean) {
			callableStatement.setBoolean(paramNum, (boolean) value);
		}
		if (value instanceof BigDecimal) {
			callableStatement.setBigDecimal(paramNum, (BigDecimal) value);
		}

	}

	@SuppressWarnings("incomplete-switch")
	private String getPrefix(ConfigDTO configDTO) {
		String prefix = "";
		switch (DatabaseDriverEnum.getDriverByValue(configDTO.getConnectionName())) {
		case ORACLE:
		case H2:
			break;
		case MYSQL:
			prefix = configDTO.getDatabaseName();
			break;
		case SQL_SERVER:
			prefix = configDTO.getSchema();
			break;
		}
		return prefix;
	}

	private Map<String, List<Object>> getPkData(Connection conn, ConfigDTO configDTO) {
		Map<String, List<Object>> result = new HashMap<>();
		try {
			StringBuilder str = new StringBuilder();
			CallableStatement selectStatement = null;
			ResultSet rs = null;
			if (!configDTO.getTableJoinInfo().isEmpty()) {
				for (TableJoinInfo t : configDTO.getTableJoinInfo()) {
					List<Object> values = new ArrayList<>();
					int limitRow = getMaxRowOfTableJoin(getCountTableJoin(conn, t));
					switch (DatabaseDriverEnum.getDriverByValue(configDTO.getConnectionName())) {
					case ORACLE:
						selectStatement = conn.prepareCall(String.format(Constants.SELECT_STATEMENT_ORACLE, t.getColumnNameJoin(),
								t.getTableNameJoin(), limitRow));
						break;
					case H2:
					case MYSQL:
						selectStatement = conn.prepareCall(String.format(Constants.SELECT_STATEMENT_MYSQL_H2, t.getColumnNameJoin(),
							t.getTableNameJoin(), limitRow));
						System.out.println(String.format(Constants.SELECT_STATEMENT_MYSQL_H2, t.getColumnNameJoin(),
							t.getTableNameJoin(), limitRow));
						break;
					case SQL_SERVER:
						selectStatement = conn.prepareCall(String.format(Constants.SELECT_STATEMENT_SQLSERVER, limitRow,
								t.getColumnNameJoin(), t.getTableNameJoin()));
						break;
					}
					rs = selectStatement.executeQuery();
					while(rs.next()) {
						values.add(rs.getObject(1));
					}
					result.put(t.getFk(), values);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result; 
	}

	private long getCountTableJoin(Connection conn, TableJoinInfo tableJoinInfo) {
		CallableStatement countStatement;
		long result =0L;
		try {
			countStatement = conn.prepareCall(String.format(Constants.COUNT_STATEMENT,
					tableJoinInfo.getColumnNameJoin(), tableJoinInfo.getTableNameJoin()));
			ResultSet rs = null;
			rs = countStatement.executeQuery();
			if(rs.next())
				result = rs.getLong(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private int getMaxRowOfTableJoin(long row) {
		if (row < 500 && row > 99)
			return 100;
		if (row < 999 && row > 500)
			return 500;
		if (row >= 1000)
			return 1000;
		return 0;

	}
}
