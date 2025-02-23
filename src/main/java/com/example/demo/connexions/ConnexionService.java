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
