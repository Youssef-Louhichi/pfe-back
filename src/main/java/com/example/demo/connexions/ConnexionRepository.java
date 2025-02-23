package com.example.demo.connexions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnexionRepository extends JpaRepository<Connexion,Long>{
	 Optional<Connexion> findByHostAndPortAndUsernameAndDbtype(String host, int port, String username, DatabaseType dbtype);

}
