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

import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;
import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tablecolumns.TabColumn;
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
    private TableRepository tableRepository;
	
    
    @Autowired
    private ColumnRepository columnRepository;
	
	public Connexion createConnexion(Connexion cnx) {   
	    if (!testConnection(cnx)) {
	        throw new RuntimeException("Connection failed! Unable to add connexion.");
	    }
	    
	    cnx.setCreatedAt(LocalDate.now());
	    Connexion savedConnexion = connexionRepository.save(cnx);
	    
	    List<Database> databases = fetchDatabases(savedConnexion);
	   
	    savedConnexion.setDatabases(databases);
	    return savedConnexion;
	}
	
	public List<Database> fetchDatabases(Connexion connexion) {
	    DataSource dataSource = createDataSource(connexion);
	    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    List<Database> databases = new ArrayList<>();

	    try {
	        if (connexion.getDbtype() == DatabaseType.MySQL) {
	            // Exclude system databases and handle case sensitivity
	            String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA " +
	                         "WHERE SCHEMA_NAME NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')";
	            jdbcTemplate.queryForList(query).forEach(row -> {
	                Database db = createDatabaseEntity(row.get("SCHEMA_NAME").toString(), connexion);
	                databases.add(db);
	            });
	        } 
	        else if (connexion.getDbtype() == DatabaseType.Oracle) {
	            
	                Database db = createDatabaseEntity("XE", connexion);
	                databases.add(db);
	            
	        }
	        
	        databaseRepository.saveAll(databases);
	        databases.forEach(db -> db.setTables(fetchTables(db, connexion)));
	        return databases;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to fetch databases: " + e.getMessage(), e);
	    }
	}

	private Database createDatabaseEntity(String name, Connexion connexion) {
	    Database db = new Database();
	    db.setName(name);
	    db.setDbtype(connexion.getDbtype());
	    db.setCreatedAt(LocalDate.now());
	    db.setUpdatedAt(LocalDate.now());
	    db.setConnexion(connexion);
	    return db;
	}

	public List<DbTable> fetchTables(Database database, Connexion connexion) {
	    DataSource dataSource = createDataSource(connexion);
	    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    List<DbTable> tables = new ArrayList<>();

	    try {
	        if (connexion.getDbtype() == DatabaseType.MySQL) {
	            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
	                         "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'";
	            jdbcTemplate.queryForList(query, database.getName())
	                .forEach(row -> tables.add(createTableEntity(row.get("TABLE_NAME").toString(), database)));
	        } 
	        else if (connexion.getDbtype() == DatabaseType.Oracle) {
	            // Oracle: Using ALL_TABLES with explicit schema name
	            String query = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ?";
	            jdbcTemplate.queryForList(query, database.getName().toUpperCase())
	                .forEach(row -> tables.add(createTableEntity(row.get("TABLE_NAME").toString(), database)));
	        }
	        
	        tableRepository.saveAll(tables);
	        tables.forEach(table -> table.setColumns(fetchColumns(table, connexion)));
	        return tables;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to fetch tables for database " + database.getName() + ": " + e.getMessage(), e);
	    }
	}
	
	private DbTable createTableEntity(String name, Database database) {
	    DbTable table = new DbTable();
	    table.setName(name);
	    table.setDatabase(database);
	    return table;
	}

	public List<TabColumn> fetchColumns(DbTable table, Connexion connexion) {
	    DataSource dataSource = createDataSource(connexion);
	    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    List<TabColumn> columns = new ArrayList<>();

	    try {
	        if (connexion.getDbtype() == DatabaseType.MySQL) {
	            String query = "SELECT COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, IS_NULLABLE " +
	                         "FROM INFORMATION_SCHEMA.COLUMNS " +
	                         "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
	                         "ORDER BY ORDINAL_POSITION";
	            jdbcTemplate.queryForList(query, table.getDatabase().getName(), table.getName())
	                .forEach(row -> columns.add(createColumnEntity(row, table)));
	        } 
	        else if (connexion.getDbtype() == DatabaseType.Oracle) {
	            String query = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE " +
	                         "FROM ALL_TAB_COLUMNS " +
	                         "WHERE OWNER = ? AND TABLE_NAME = ? " +
	                         "ORDER BY COLUMN_ID";
	            jdbcTemplate.queryForList(query, 
	                    table.getDatabase().getName().toUpperCase(), 
	                    table.getName().toUpperCase())
	                .forEach(row -> columns.add(createColumnEntity(row, table)));
	        }
	        
	        return columnRepository.saveAll(columns);
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to fetch columns for table " + table.getName() + ": " + e.getMessage(), e);
	    }
	}

	private TabColumn createColumnEntity(Map<String, Object> row, DbTable table) {
	    TabColumn column = new TabColumn();
	    column.setName(row.get("COLUMN_NAME").toString());
	    column.setType(row.get("DATA_TYPE").toString());
	    column.setTable(table);
	    return column;
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
        	url = "jdbc:mysql://"+connexion.getHost()+":"+connexion.getPort();
        }
        
        if(connexion.getDbtype().equals(DatabaseType.Oracle)) {
        	dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        	url = "jdbc:oracle:thin:@"+connexion.getHost()+":"+connexion.getPort()+":xe";
        }

        dataSource.setUrl(url);
        dataSource.setUsername(connexion.getUsername());
        dataSource.setPassword(connexion.getPassword());
        
        return dataSource;
    }
    
    

    
    
    public boolean testConnection(Connexion connexion) {
        DataSource dataSource = createDataSource(connexion);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            jdbcTemplate.execute("SELECT 1 from dual");
            System.out.println("Connected");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }



}
