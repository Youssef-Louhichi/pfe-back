package com.example.demo.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.analyst.Analyst;
import com.example.demo.dto.DatabaseDashboardDto;
import com.example.demo.dto.DatabaseStructureDTO;
import com.example.demo.dto.RelationTablesDto;
import com.example.demo.users.User;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @PostMapping
    public Database createDatabase(@RequestBody Database database) {
        return databaseService.createDatabase(database);
    }

    @GetMapping
    public List<Database> getAllDatabases() {
        return databaseService.getAllDatabases();
    }

    @GetMapping("/{id}")
    public Optional<Database> getDatabaseById(@PathVariable Long id) {
        return databaseService.getDatabaseById(id);
    }

    @PutMapping("/{id}")
    public Database updateDatabase(@PathVariable Long id, @RequestBody Database updatedDatabase) {
        return databaseService.updateDatabase(id, updatedDatabase);
    }

    @DeleteMapping("/{id}")
    public void deleteDatabase(@PathVariable Long id) {
        databaseService.deleteDatabase(id);
    }
    
   
    
    
    @GetMapping("/tables/{schemaName}")
    public List<String> getTables(@PathVariable String schemaName) throws Exception {
        return databaseService.getTables(schemaName);
    }
    
    @GetMapping("/{id}/structure")
    public ResponseEntity<List<RelationTablesDto>> getStructure(@PathVariable Long id) {
        
        try {
        	List<RelationTablesDto> structure = databaseService.extractStructure(id);
            return ResponseEntity.ok(structure);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<DatabaseDashboardDto>> getDashboardForUser(
            @RequestParam Long creatorId,
            @RequestParam Long cnxId) {
        
        List<DatabaseDashboardDto> dashboard = databaseService.getDashboardForUser(creatorId, cnxId);
        return ResponseEntity.ok(dashboard);
    }

}
