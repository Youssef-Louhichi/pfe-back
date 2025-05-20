package com.example.demo.requete;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.tables.DbTable;


public interface RequeteRepository extends JpaRepository<Requete,Long>{

	
	List<Requete> findBySenderIdentif(Long senderId);

	List<Requete> findByTableReq(DbTable table);

	
}
