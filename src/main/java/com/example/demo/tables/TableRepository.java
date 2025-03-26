package com.example.demo.tables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TableRepository extends JpaRepository<DbTable,Long>{

	
	@Query("SELECT t FROM DbTable t WHERE t.name = :name AND t.database.name = :schema")
	DbTable findByNameAndSchema(@Param("name") String name, @Param("schema") String schema);
}
