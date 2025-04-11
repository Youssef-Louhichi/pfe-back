package com.example.demo.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.analyst.Analyst;

public interface DatabaseRepository extends JpaRepository<Database,Long>{
	
    
 
	
	
	@Query("SELECT d FROM Database d WHERE d.name = :dbName")
    Database findByName(@Param("dbName") String dbName);

}
