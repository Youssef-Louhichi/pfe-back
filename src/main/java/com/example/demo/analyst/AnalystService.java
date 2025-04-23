package com.example.demo.analyst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.connexions.Connexion;
import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;
import com.example.demo.dto.RelationDto;
import com.example.demo.relations.Relation;
import com.example.demo.relations.RelationColumn;
import com.example.demo.relations.RelationDatabase;
import com.example.demo.relations.RelationRepository;
import com.example.demo.relations.RelationTable;
import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tablecolumns.TabColumn;
import com.example.demo.tables.DbTable;
import com.example.demo.tables.TableRepository;

import jakarta.transaction.Transactional;

@Service
public class AnalystService {
	
	@Autowired
    private AnalystRepository analystRepository;
    
    @Autowired 
    private DatabaseRepository databaseRepository;
    
    @Autowired
    private TableRepository tableRepository;
    
    @Autowired
   	private PasswordEncoder passwordEncoder;
    
    
    
    @Autowired
    private ColumnRepository columnRepository;
    
    @Autowired
    private RelationRepository relationRepository;
    
    
    public List<Analyst> getAllAnalysts() {
        return analystRepository.findAll();
    }

    public Optional<Analyst> getAnalystById(Long id) {
        return analystRepository.findById(id);
    }

    public Analyst createAnalyst(Analyst analyst) {
    	//analyst.setPassword(passwordEncoder.encode(analyst.getPassword()));
        return analystRepository.save(analyst);
    }

    public Analyst updateAnalyst(Long id, Analyst updatedAnalyst) {
        return analystRepository.findById(id).map(analyst -> {
        	analyst.setMail(updatedAnalyst.getMail());
        	analyst.setPassword(updatedAnalyst.getPassword());
            analyst.setRelations(updatedAnalyst.getRelations());
            return analystRepository.save(analyst);
        }).orElse(null);
    }

    public void deleteAnalyst(Long id) {
        analystRepository.deleteById(id);
    }
	

public Analyst addRelationsToAnalyst(Long analystId, RelationDto relationsDto) {
    Analyst analyst = analystRepository.findById(analystId)
        .orElseThrow(() -> new RuntimeException("Analyst not found"));
    
    List<Relation> relations = new ArrayList<>();

    if (relationsDto.getDatabaseId() != null) {
        Database database = databaseRepository.findById(relationsDto.getDatabaseId())
            .orElseThrow(() -> new RuntimeException("Database not found"));
        relations.add(new RelationDatabase(analyst, database));
    }

    if (!relationsDto.getTablesIds().isEmpty()) {
        relationsDto.getTablesIds().forEach(tableId -> {
            DbTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
            relations.add(new RelationTable(analyst, table));
        });
    }

    if (!relationsDto.getColumnsIds().isEmpty()) {
        relationsDto.getColumnsIds().forEach(columnId -> {
            TabColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
            relations.add(new RelationColumn(analyst, column));
        });
    }

    // Save all relations at once
    relationRepository.saveAll(relations);

    return analystRepository.findById(analystId).orElseThrow(); // Refresh
}

public List<Database> getAnalystDatabases( Long analystId) {
	   Analyst analyst = analystRepository.findById(analystId)
		        .orElseThrow(() -> new RuntimeException("User not found"));
		    
		    // First get all related databases (like your original method)
		    Set<Database> allRelatedDbs = new HashSet<>();
		    Map<Database, Set<DbTable>> dbToTablesMap = new HashMap<>();
		    Map<DbTable, Set<TabColumn>> tableToColumnsMap = new HashMap<>();
		    
		    // Process all relations to build the structure
		    for (Relation r : analyst.getRelations()) {
		        if (r instanceof RelationDatabase) {
		            RelationDatabase rd = (RelationDatabase) r;
		            Database db = rd.getDatabase();
		            allRelatedDbs.add(db);
		            // Full access to database means all tables/columns are visible
		            dbToTablesMap.computeIfAbsent(db, k -> new HashSet<>(db.getTables()));
		        } 
		        else if (r instanceof RelationTable) {
		            RelationTable rt = (RelationTable) r;
		            DbTable table = rt.getTable();
		            Database db = table.getDatabase();
		            allRelatedDbs.add(db);
		            dbToTablesMap.computeIfAbsent(db, k -> new HashSet<>()).add(table);
		        } 
		        else if (r instanceof RelationColumn) {
		            RelationColumn rc = (RelationColumn) r;
		            TabColumn column = rc.getColumn();
		            DbTable table = column.getTable();
		            Database db = table.getDatabase();
		            allRelatedDbs.add(db);
		            
		            // Add the table to database's visible tables
		            dbToTablesMap.computeIfAbsent(db, k -> new HashSet<>()).add(table);
		            // Add the column to table's visible columns
		            tableToColumnsMap.computeIfAbsent(table, k -> new HashSet<>()).add(column);
		        }
		    }
		    
		    // Now build the filtered database structure
		    List<Database> result = new ArrayList<>();
		    for (Database originalDb : allRelatedDbs) {
		        // Create a copy of the database to avoid modifying the original
		        Database filteredDb = new Database();
		        filteredDb.setId(originalDb.getId());
		        filteredDb.setName(originalDb.getName());
		        filteredDb.setConnexion(originalDb.getConnexion());
		        filteredDb.setCreatedAt(originalDb.getCreatedAt());
		        filteredDb.setDbtype(originalDb.getDbtype());
		        filteredDb.setUpdatedAt(originalDb.getUpdatedAt());
		        
		        // ... copy other database properties
		        
		        Set<DbTable> visibleTables = dbToTablesMap.get(originalDb);
		        List<DbTable> filteredTables = new ArrayList<>();
		        
		        for (DbTable visibleTable : visibleTables) {
		            // Check if we have column-level restrictions for this table
		            Set<TabColumn> visibleColumns = tableToColumnsMap.get(visibleTable);
		            
		            DbTable filteredTable = new DbTable();
		            filteredTable.setId(visibleTable.getId());
		            filteredTable.setName(visibleTable.getName());
		            // ... copy other table properties
		            
		            if (visibleColumns != null) {
		                // Column-level restriction - only include specific columns
		                List<TabColumn> filteredColumns = new ArrayList<>();
		                for (TabColumn visibleColumn : visibleColumns) {
		                	TabColumn filteredColumn = new TabColumn();
		                    filteredColumn.setId(visibleColumn.getId());
		                    filteredColumn.setName(visibleColumn.getName());
		                    filteredColumn.setType(visibleColumn.getType());
		                    filteredColumn.setTable(visibleColumn.getTable());
		                    // ... copy other column properties
		                    filteredColumns.add(filteredColumn);
		                }
		                filteredTable.setColumns(filteredColumns);
		            } else {
		                // No column-level restriction - include all columns
		                filteredTable.setColumns(new ArrayList<>(visibleTable.getColumns()));
		            }
		            
		            filteredTables.add(filteredTable);
		        }
		        
		        filteredDb.setTables(filteredTables);
		        result.add(filteredDb);
		    }
		    
		    return result;
	 }
	 
	 

public List<Connexion> getAnalystConnexions( Long analystId) {
	 Analyst analyst = analystRepository.findById(analystId).orElseThrow(() -> new RuntimeException("User not found"));
	 List<Connexion> connexions = new ArrayList<>();
	 for (Database db : this.getAnalystDatabases(analystId)) {
	     if (!connexions.contains(db.getConnexion())) {
	         connexions.add(db.getConnexion());
	     }
	 }

     return connexions;
}

public List<Analyst> getUsersByDatabaseId(Long dbId) {
    // First find the database
    Database database = databaseRepository.findById(dbId)
        .orElseThrow(() -> new RuntimeException("Database not found"));
    
    // Get all relations that are connected to this database at any level
    List<Relation> relations = relationRepository.findAll().stream()
        .filter(relation -> isRelatedToDatabase(relation, database))
        .collect(Collectors.toList());
    
    // Extract unique analysts from these relations
    return relations.stream()
        .map(Relation::getAnalyst)
        .distinct()
        .collect(Collectors.toList());
}

private boolean isRelatedToDatabase(Relation relation, Database database) {
    if (relation instanceof RelationDatabase) {
        return ((RelationDatabase) relation).getDatabase().equals(database);
    } else if (relation instanceof RelationTable) {
        return ((RelationTable) relation).getTable().getDatabase().equals(database);
    } else if (relation instanceof RelationColumn) {
        return ((RelationColumn) relation).getColumn().getTable().getDatabase().equals(database);
    }
    return false;
}

 // Assuming you have a relation repository

@Transactional
public boolean deleteAnalystsRelations(Long analystId) {
    try {
        // First, verify the analyst exists
        if (!analystRepository.existsById(analystId)) {
            return false;
        }
        
        // Delete all relations where this analyst is involved
        int deletedCount = relationRepository.deleteByAnalystId(analystId);
        
        // Alternatively, if you have a more complex relationship:
        // List<Relation> relations = relationRepository.findByAnalystId(analystId);
        // relationRepository.deleteAll(relations);
        
        return true;
    } catch (Exception e) {
        // Log the exception
        return false;
    }
}

}
