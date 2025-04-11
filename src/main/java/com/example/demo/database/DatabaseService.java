package com.example.demo.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.analyst.Analyst;
import com.example.demo.users.User;

@Service
public class DatabaseService {

	private final DataSource dataSource;
	
	  @Autowired 
	  private DatabaseRepository databaseRepository;
	
	public DatabaseService(DataSource dataSource)
	{
	this.dataSource = dataSource ;
	}
	
	
	public List<String> getTables(String schemaName) throws Exception {
	    try (Connection conn = dataSource.getConnection()) {
	        DatabaseMetaData metaData = conn.getMetaData();
	        ResultSet tables = metaData.getTables(null, schemaName, "%", new String[]{"TABLE"});

	        List<String> tableNames = new ArrayList<>();
	        while (tables.next()) {
	            tableNames.add(tables.getString("TABLE_NAME"));
	        }
	        return tableNames;
	    }
	}
	
  

    // Create a new database entry
    public Database createDatabase(Database database) {
        return databaseRepository.save(database);
    }

    // Get all databases
    public List<Database> getAllDatabases() {
        return databaseRepository.findAll();
    }

    // Get database by ID
    public Optional<Database> getDatabaseById(Long id) {
        return databaseRepository.findById(id);
    }

    // Update database entry
    public Database updateDatabase(Long id, Database updatedDatabase) {
        Optional<Database> existingDatabaseOpt = databaseRepository.findById(id);

        if (existingDatabaseOpt.isPresent()) {
            Database existingDatabase = existingDatabaseOpt.get();
            existingDatabase.setName(updatedDatabase.getName());
            existingDatabase.setDbtype(updatedDatabase.getDbtype());
            existingDatabase.setConnexion(updatedDatabase.getConnexion());
            existingDatabase.setRelationDatabases(updatedDatabase.getRelationDatabases());
            existingDatabase.setUpdatedAt(LocalDate.now());
            return databaseRepository.save(existingDatabase);
        } else {
            throw new RuntimeException("Database not found");
        }
    }

    // Delete database entry
    public void deleteDatabase(Long id) {
        databaseRepository.deleteById(id);
    }
    
   
} 
