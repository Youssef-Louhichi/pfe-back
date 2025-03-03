package com.example.demo.database;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DatabaseRepository extends JpaRepository<Database,Long>{
	
	@Query("SELECT d FROM Database d JOIN FETCH d.users WHERE d.id = :dbId")
    Optional<Database> findByIdWithUsers(@Param("dbId") Long dbId);
	
	
	@Query("SELECT d FROM Database d WHERE d.name = :dbName")
    Database findByName(@Param("dbName") String dbName);

}
