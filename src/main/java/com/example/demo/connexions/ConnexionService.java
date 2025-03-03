package com.example.demo.connexions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.example.demo.TableColumns.ColumnRepository;
import com.example.demo.TableColumns.TabColumn;
import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;
import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;

@Service
public class ConnexionService {
	
	@Autowired 
	private ConnexionRepository connexionRepository;
	
	@Autowired 
    private DatabaseRepository databaseRepository;
	
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TableRepository tableRepository;
	
    
    @Autowired
    private ColumnRepository columnRepository;
	
	public Connexion createConnexion(Connexion cnx) {
		
		Optional<Connexion> existingConnexion = connexionRepository.findByHostAndPortAndUsernameAndDbtype(cnx.getHost(), cnx.getPort(), cnx.getUsername(), cnx.getDbtype());

		if (existingConnexion.isPresent()) {
			throw new RuntimeException("This connexion already exists!");
		  }
		    
	    if (!testConnection(cnx)) {
	        throw new RuntimeException("Connection failed! Unable to add connexion.");
	    }
	    
	    cnx.setCreatedAt(LocalDate.now());
	    Connexion savedConnexion = connexionRepository.save(cnx);
	    List<Database> databases = fetchDatabases(savedConnexion);
	    for (Database db : databases) {
	        db.setConnexion(savedConnexion);
	    }
	    databaseRepository.saveAll(databases);
	    savedConnexion.setDatabases(databases);
	    
	    User creator = userRepository.findById(cnx.getCreator().getIdentif()).get();
	    creator.getDatabases().addAll(databases);
	    userRepository.save(creator);

	    return savedConnexion;
	}
	
	public List<Database> fetchDatabases(Connexion connexion) {
		
	    DataSource dataSource = createDataSource(connexion);
	    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    List<Database> databases = new ArrayList<>();

	    String query = "";

	    if (connexion.getDbtype() == DatabaseType.MySQL) {
	        query = "SHOW DATABASES";
	    } else if (connexion.getDbtype() == DatabaseType.Oracle) {
	        query = "SELECT name FROM v$database"; 
	    }

	    try {
	        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
	        for (Map<String, Object> row : rows) {
	            String dbName = row.values().iterator().next().toString();
	            Database db = new Database();
	            db.setName(dbName);
	            db.setDbtype(connexion.getDbtype());
	            db.setCreatedAt(LocalDate.now());
	            db.setUpdatedAt(LocalDate.now());
	            
	            
	            databases.add(db);
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to fetch databases: " + e.getMessage());
	    }

	    return databases;
	}
	
	
	public List<DbTable> fetchTables(JdbcTemplate jdbcTemplate, String connectionUser, DatabaseType dbType) {
	    List<DbTable> tables = new ArrayList<>();
	    
	    // Fetch schemas for the current connection
	    List<String> schemas = fetchSchemas2(jdbcTemplate, connectionUser, dbType,"youssef");
	    //List<String> schemas = fetchSchemas(jdbcTemplate, connectionUser, dbType);

	    for (String schema : schemas) {
	        String tableQuery = "";

	        if (dbType == DatabaseType.MySQL) {
	            tableQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?";
	        } else if (dbType == DatabaseType.Oracle) {
	            tableQuery = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ?";
	        }

	        try {
	            List<Map<String, Object>> tableRows = jdbcTemplate.queryForList(tableQuery, schema);
	            System.out.println("Fetching tables for schema: " + schema + " - Found: " + tableRows.size());
	            for (Map<String, Object> tableRow : tableRows) {
	            	
	            	
	            	
	                String tableName = tableRow.get("TABLE_NAME").toString();
	                
	      
	                DbTable table = tableRepository.findByNameAndSchema(tableName, schema);
	                if (table == null) {
	                    table = new DbTable();
	                    table.setName(tableName);
	                    table.setDatabase(databaseRepository.findByName(schema));
	                    tableRepository.save(table);
	                } else {
	                    System.out.println("Table already exists, skipping save: " + tableName);
	                }
	                 List<TabColumn> columns = fetchColumns(jdbcTemplate, schema, tableName, dbType);
	                 table.setColumns(columns);

	                tables.add(table);
	            	
	            }
	        } catch (Exception e) {
	            throw new RuntimeException("Failed to fetch tables for schema " + schema + ": " + e.getMessage());
	        }
	    }

	    return tables;
	}
	
	
	private List<String> fetchSchemas2(JdbcTemplate jdbcTemplate, String connectionUser, DatabaseType dbType, String schemaId) {
	    List<String> schemas = new ArrayList<>();
	    String schemaQuery = "";

	    // Check if schemaId is provided; if so, filter by schemaId.
	    if (dbType == DatabaseType.MySQL) {
	        // If schemaId is provided, fetch the specific schema, otherwise fetch all
	        if (schemaId != null && !schemaId.isEmpty()) {
	            schemaQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
	        } else {
	            schemaQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA";
	        }
	    } else if (dbType == DatabaseType.Oracle) {
	        // Similarly, filter by schemaId for Oracle if provided
	        if (schemaId != null && !schemaId.isEmpty()) {
	            schemaQuery = "SELECT USERNAME FROM ALL_USERS WHERE USERNAME = ?";
	        } else {
	            schemaQuery = "SELECT USERNAME FROM ALL_USERS";
	        }
	    }

	    try {
	        List<Map<String, Object>> schemaRows;
	        // Use the parameterized query to prevent issues with SQL injection
	        if (schemaId != null && !schemaId.isEmpty()) {
	            schemaRows = jdbcTemplate.queryForList(schemaQuery, schemaId.toUpperCase()); // Make sure schemaId is passed as a parameter
	        } else {
	            // Otherwise, fetch all schemas
	            schemaRows = jdbcTemplate.queryForList(schemaQuery);
	        }

	        for (Map<String, Object> row : schemaRows) {
	            schemas.add(row.values().iterator().next().toString());
	        }

	        System.out.println("Fetched schemas: " + schemas);
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to fetch schemas: " + e.getMessage());
	    }

	    return schemas;
	}


	private List<TabColumn> fetchColumns(JdbcTemplate jdbcTemplate, String schema, String tableName, DatabaseType dbType) {
	    List<TabColumn> columns = new ArrayList<>();
	    String columnQuery = "";

	    if (dbType == DatabaseType.MySQL) {
	        columnQuery = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
	    } else if (dbType == DatabaseType.Oracle) {
	        columnQuery = "SELECT COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ?";
	    }

	    try {
	        List<Map<String, Object>> columnRows = jdbcTemplate.queryForList(columnQuery, schema, tableName);
	        DbTable table = tableRepository.findByNameAndSchema(tableName, schema); // Fetch the table once

	        if (table == null) {
	            throw new RuntimeException("Table " + tableName + " not found in schema " + schema);
	        }

	        for (Map<String, Object> columnRow : columnRows) {
	            String columnName = columnRow.get("COLUMN_NAME").toString();

	            // Check if the column already exists in the database
	            TabColumn existingColumn = columnRepository.findByNameAndTable(columnName, table);
	            if (existingColumn == null) {
	                // Column does not exist, so we create and save it
	                TabColumn column = new TabColumn();
	                column.setName(columnName);
	                 column.setType(columnRow.get("DATA_TYPE").toString());
	                column.setTable(table);
	                columnRepository.save(column);
	                columns.add(column);
	            } else {
	                System.out.println("Column already exists: " + columnName + " in table " + tableName);
	            }
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to fetch columns for table " + tableName + " in schema " + schema + ": " + e.getMessage());
	    }

	    return columns;
	}




    public List<Connexion> getAllConnexions() {
        return connexionRepository.findAll();
    }

    public List<Database> getConnexionDatabases(Long id){
    	return connexionRepository.findById(id).get().getDatabases();
    }
    
    public Optional<Connexion> getConnexionById(Long id) {
        return connexionRepository.findById(id);
    }

    
    public Connexion updateConnexion(Long id, Connexion updatedConnexion) {
        Optional<Connexion> existingConnexionOpt = connexionRepository.findById(id);

        if (existingConnexionOpt.isPresent()) {
            Connexion existingConnexion = existingConnexionOpt.get();
            existingConnexion.setHost(updatedConnexion.getHost());
            existingConnexion.setPort(updatedConnexion.getPort());
            existingConnexion.setPassword(updatedConnexion.getPassword());
            existingConnexion.setUsername(updatedConnexion.getUsername());

            existingConnexion.setUpdatedAt(LocalDate.now());
            return connexionRepository.save(existingConnexion);
        } else {
            throw new RuntimeException("Connexion not found");
        }
    }

    
    public void deleteConnexion(Long id) {
    	connexionRepository.deleteById(id);
    }
    
    
    public DataSource createDataSource(Connexion connexion) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String url = "";
        
        if(connexion.getDbtype().equals(DatabaseType.MySQL)) {
        	dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        	url = "jdbc:mysql://";
        }
        
        if(connexion.getDbtype().equals(DatabaseType.Oracle)) {
        	dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        	url = "jdbc:oracle://";
        }

        dataSource.setUrl(url+connexion.getHost()+":"+connexion.getPort());
        dataSource.setUsername(connexion.getUsername());
        dataSource.setPassword(connexion.getPassword());
        
        return dataSource;
    }
    
    
    /*public void testConnection(Connexion connexion) {
        DataSource dataSource = createDataSource(connexion);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        try {
            jdbcTemplate.queryForList("SHOW SCHEMAS").forEach(row -> System.out.println(row));
            System.out.println("connected");
        } catch (Exception e) {
            System.err.println("failed to connect: " + e.getMessage());
        }
    }*/
    
    
    public boolean testConnection(Connexion connexion) {
        DataSource dataSource = createDataSource(connexion);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            jdbcTemplate.execute("SELECT 1");
            System.out.println("Connected");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }



}
