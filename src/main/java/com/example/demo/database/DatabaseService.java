package com.example.demo.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.analyst.Analyst;
import com.example.demo.connexions.Connexion;
import com.example.demo.connexions.ConnexionRepository;
import com.example.demo.creator.Creator;
import com.example.demo.creator.CreatorRepository;
import com.example.demo.dto.DatabaseDashboardDto;
import com.example.demo.dto.DatabaseStructureDTO;
import com.example.demo.dto.RelationTablesDto;
import com.example.demo.requete.Requete;
import com.example.demo.requete.RequeteRepository;
import com.example.demo.tablecolumns.TabColumn;
import com.example.demo.tables.DbTable;
import com.example.demo.users.User;
import com.example.demo.users.UserRepository;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;

@Service
public class DatabaseService {

	private final DataSource dataSource;
	
	  @Autowired 
	  private DatabaseRepository databaseRepository;
	  
	  
	    @Autowired
	    private RequeteRepository requeteRepository; 
	    
	    @Autowired
	    private ConnexionRepository connexionRepository; 
	    
	    @Autowired
	    private CreatorRepository creatorRepository; 
	
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
    
   
    public List<RelationTablesDto> extractStructure(Long dbId) throws SQLException {
    	
    	Database db = databaseRepository.findById(dbId)
                .orElseThrow(() -> new RuntimeException("database not found"));
    	
       
        List<RelationTablesDto> relations = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
        		db.getJdbcUrl(), db.getConnexion().getUsername(), db.getConnexion().getPassword())) {

            DatabaseMetaData metaData = conn.getMetaData();
            
            for (DbTable table : db.getTables()) {
                ResultSet fkRs = metaData.getImportedKeys(null, null, table.getName());
                while (fkRs.next()) {
                    RelationTablesDto rel = new RelationTablesDto();
                    rel.setFromTable(fkRs.getString("FKTABLE_NAME"));
                    rel.setFromColumn(fkRs.getString("FKCOLUMN_NAME"));
                    rel.setToTable(fkRs.getString("PKTABLE_NAME"));
                    rel.setToColumn(fkRs.getString("PKCOLUMN_NAME"));
                    relations.add(rel);
                }
                fkRs.close();
            }

        }

        
        return relations;
    }
    
    


    public List<DatabaseDashboardDto> getDashboardForUser(Long creatorId,Long cnxId) {
    	
    	Creator currentUser = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("creator not found"));
    	
    	Connexion cnx = connexionRepository.findById(cnxId)
                .orElseThrow(() -> new RuntimeException("connexion not found"));
    	
        List<Database> userDbs = cnx.getDatabases(); 

        
        List<DatabaseDashboardDto> dashboardList = new ArrayList<>();
        
        

        for (Database db : userDbs) {
            DatabaseDashboardDto dto = new DatabaseDashboardDto();
            dto.setId(db.getId());
            dto.setName(db.getName());
            dto.setType(db.getDbtype().name());
            dto.setCreatedAt(db.getCreatedAt());
            dto.setUpdatedAt(db.getUpdatedAt());

            List<DbTable> tables = db.getTables();
            dto.setTableCount(tables.size());
            dto.setTableNames(tables.stream().map(DbTable::getName).toList());
            
            int colcount = 0;
          
            List<Requete> queries = new ArrayList<>();
            
            for(DbTable tab :tables) {
            	colcount += tab.getColumns().size();
            	queries.addAll( requeteRepository.findByTableReq(tab) );
            }
            		
            dto.setQueryCount(queries.size());
            dto.setColumnCount(colcount);

           
            queries.stream()
                   .map(Requete::getSentAt)
                   .max(LocalDateTime::compareTo)
                   .ifPresent(dto::setLastQueryAt);
            
           

            Map<String, Long> collaboratorCount = queries.stream()
                .filter(r -> !r.getSender().getIdentif().equals(currentUser.getIdentif()))
                .collect(Collectors.groupingBy(r -> r.getSender().getMail(), Collectors.counting()));
            
           
            Map<String, Long> topCollaborators = collaboratorCount.entrySet().stream()
            	    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            	    .limit(3)
            	    .collect(Collectors.toMap(
            	        Map.Entry::getKey,
            	        Map.Entry::getValue,
            	        (e1, e2) -> e1,
            	        LinkedHashMap::new // to preserve order
            	    ));

            	dto.setTopCollaborators(topCollaborators);

            /*int modificationCount = (int) queries.stream()
                .filter(r -> r.getSender().getIdentif().equals(currentUser.getIdentif()))
                .filter(r -> r.getContent().toLowerCase().matches(".*\\b(insert|update|delete)\\b.*"))
                .count();

            dto.setModificationCount(modificationCount);*/
            
            Map<YearMonth, Long> monthlyQueryCountMap = queries.stream()
            	    .collect(Collectors.groupingBy(
            	        q -> YearMonth.from(q.getSentAt()),
            	        Collectors.counting()
            	    ));
            
            Map<String, Long> monthlyStats = new LinkedHashMap<>(); 
            YearMonth currentMonth = YearMonth.now();

            for (int i = 0; i < 12; i++) {
                YearMonth targetMonth = currentMonth.minusMonths(i);
                String monthLabel = targetMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                                    + " " + targetMonth.getYear();
                Long count = monthlyQueryCountMap.getOrDefault(targetMonth, 0L);
                monthlyStats.put(monthLabel, count);
            }

            dto.setMonthlyQueryStats(monthlyStats); 
            
            try {
                Long storageInfo = getDatabaseStorageSize(db);
                dto.setUsedSizeBytes(storageInfo);
               
            } catch (SQLException e) {
                dto.setUsedSizeBytes(0L);
               
            }


            dashboardList.add(dto);
        }

        return dashboardList;
    }
    
    
    private Long getDatabaseStorageSize(Database db) throws SQLException {
       Long size = 0L;
        
        try (Connection conn = DriverManager.getConnection(
                db.getJdbcUrl(), 
                db.getConnexion().getUsername(), 
                db.getConnexion().getPassword())) {
            
            switch (db.getDbtype()) {
                case MySQL:
                    // Updated MySQL query to get actual used vs allocated space
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(
                             "SELECT " +
                             "SUM(data_length + index_length) as used_size, " +
                             "SUM(data_length + index_length + data_free) as total_size " +
                             "FROM information_schema.TABLES " +
                             "WHERE table_schema = DATABASE()")) {
                        if (rs.next()) {
                            size= rs.getLong("used_size");
                            
                        }
                    }
                    break;
                    
                case Oracle:
                    // Updated Oracle query to get proper space allocation
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(
                             "SELECT " +
                             "SUM(bytes) as used_size, " +
                             "(SELECT sum(bytes) + sum(decode(autoextensible,'YES',maxbytes,bytes)) " +
                             "FROM dba_data_files WHERE tablespace_name IN " +
                             "(SELECT tablespace_name FROM dba_tablespaces)) as total_size " +
                             "FROM dba_segments")) {
                        if (rs.next()) {
                        	size= rs.getLong("used_size");
                        }
                    }
                    break;
                    
               
            }
        }
        return size;
    }

} 
