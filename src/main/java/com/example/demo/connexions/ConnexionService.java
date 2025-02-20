package com.example.demo.connexions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

@Service
public class ConnexionService {
	
	@Autowired 
	private ConnexionRepository connexionRepository;
	
	
	// Create a new user
    public Connexion createConnexion(Connexion cnx) {
        return connexionRepository.save(cnx);
    }

    // Get all users
    public List<Connexion> getAllConnexions() {
        return connexionRepository.findAll();
    }

    // Get user by ID
    public Optional<Connexion> getConnexionById(Long id) {
        return connexionRepository.findById(id);
    }

    // Update user
    public Connexion updateConnexion(Long id, Connexion updatedConnexion) {
        Optional<Connexion> existingConnexionOpt = connexionRepository.findById(id);

        if (existingConnexionOpt.isPresent()) {
            Connexion existingConnexion = existingConnexionOpt.get();
            existingConnexion.setHost(updatedConnexion.getHost());
            existingConnexion.setDatabaseName(updatedConnexion.getDatabaseName());
            existingConnexion.setPort(updatedConnexion.getPort());
            existingConnexion.setPassword(updatedConnexion.getPassword());
            existingConnexion.setUsername(updatedConnexion.getUsername());

            existingConnexion.setUpdatedAt(LocalDate.now());
            return connexionRepository.save(existingConnexion);
        } else {
            throw new RuntimeException("Connexion not found");
        }
    }

    // Delete user
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
    
    public void testConnection(Connexion connexion) {
        DataSource dataSource = createDataSource(connexion);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        try {
            jdbcTemplate.queryForList("SHOW SCHEMAS").forEach(row -> System.out.println(row));
            System.out.println("connected");
        } catch (Exception e) {
            System.err.println("failed to connect: " + e.getMessage());
        }
    }


}
