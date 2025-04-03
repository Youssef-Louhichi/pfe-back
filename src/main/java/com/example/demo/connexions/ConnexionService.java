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

        String query = "";

        if (connexion.getDbtype() == DatabaseType.MySQL) {
        	query = "SHOW DATABASES WHERE `Database` NOT IN ('information_schema', 'performance_schema')";
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
                db.setConnexion(connexion);
                
                databases.add(db);
            }
            databaseRepository.saveAll(databases);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch databases: " + e.getMessage());
        }
        
        for (Database db : databases) {
	        List<DbTable> tables = fetchTables(db,connexion);
		    db.setTables(tables);

	    }
	    databaseRepository.saveAll(databases);
	    
        return databases;
    }

    public List<DbTable> fetchTables(Database database, Connexion connexion) {
        DataSource dataSource = createDataSource(connexion);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<DbTable> tables = new ArrayList<>();

        String query = "";
        if (connexion.getDbtype() == DatabaseType.MySQL) {
            query = "SHOW TABLES FROM "+ database.getName();
        } else if (connexion.getDbtype() == DatabaseType.Oracle) {
            query = "SELECT table_name FROM all_tables WHERE owner = USER";
        }

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
            for (Map<String, Object> row : rows) {
                String tableName = row.values().iterator().next().toString();
                DbTable table = new DbTable();
                table.setName(tableName);
                table.setDatabase(database);
                tables.add(table);
                
            }
            
            tableRepository.saveAll(tables);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch tables: " + e.getMessage());
        }
        
        for (DbTable table : tables) {
	        table.setDatabase(database);
	        List<TabColumn> columns = fetchColumns(table,connexion);
		    table.setColumns(columns);

	    }
	    tableRepository.saveAll(tables);

        return tables;
    }

    public List<TabColumn> fetchColumns(DbTable table, Connexion connexion) {
        DataSource dataSource = createDataSource(connexion);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<TabColumn> columns = new ArrayList<>();

        String query = "";
        Object[] params;

        if (connexion.getDbtype() == DatabaseType.MySQL) {
            query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
            params = new Object[]{table.getDatabase().getName(), table.getName()};
        } else if (connexion.getDbtype() == DatabaseType.Oracle) {
            query = "SELECT COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ? AND OWNER = ?";
            params = new Object[]{table.getName().toUpperCase(), connexion.getUsername().toUpperCase()};
        } else {
            throw new RuntimeException("Unsupported database type");
        }

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, params);
            for (Map<String, Object> row : rows) {
                TabColumn column = new TabColumn();
                column.setName(row.get("COLUMN_NAME").toString());
                column.setType(row.get("DATA_TYPE").toString());
                column.setTable(table);  
                columns.add(column);
            }
            
            columnRepository.saveAll(columns);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch columns: " + e.getMessage());
        }
        
        for (TabColumn column : columns) {
	        column.setTable(table);
	    }
	    columnRepository.saveAll(columns);

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
            jdbcTemplate.execute("SELECT 1 from dual");
            System.out.println("Connected");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }



}
