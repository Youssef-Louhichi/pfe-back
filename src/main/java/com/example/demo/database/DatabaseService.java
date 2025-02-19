package com.example.demo.database;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired 
    private DatabaseRepository databaseRepository;

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
            existingDatabase.setUsers(updatedDatabase.getUsers());
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
