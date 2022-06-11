# AutoGenerateRecord
## Description

Can add huge record for database like as : MySQL, SQL Server, Oracle, H2 database,...


## Usage
Use file config.json to add information of database.
- Example: use Mysql Database, add 1000 record, ip: localhost, port:8080
```json
{
	"connectionName": "MySQL",
	"sizeRow": 1000,
	"ipAddress": "localhost",
	"port": "8080",
	"username": "root",
	"password": "xxxxxxx",
	"databaseName": "Example",
	"schema": "",
	"isSID": false,
	"tableInfo": {
		"tableName": "ExmpleTable",
		"columnInfo": [
			{
				"columnName": "id",
				"columnType": "int",
				"isAutoIncrement": true
			},
			{
				"columnName": "title",
				"columnType": "varchar",
				"isAutoIncrement": false
			}
		]
	},
	"tableJoinInfo": [
       {
			"tableNameJoin": "Name of table reference if any",
			"columnNameJoin": "Name of Column reference if any",
			"fk": "Name of column is foreign key"
		}
     ]
}
```

## Next Updating
> Fix bug about H2 database.

> Add more database like PostgreSql.

> Add more beautiful data for database base on name of column

> Add more length of column

Please make sure to update tests as appropriate.
