package com.example.demo.connexions;



import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnexionRepository extends JpaRepository<Connexion,Long>{
	 //Optional<Connexion> findByHostAndPortAndUsernameAndDbtype(Long id,String host, int port, String username, DatabaseType dbtype);

}
