package com.example.demo.requete;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RequeteRepository extends JpaRepository<Requete,Long>{

	
	List<Requete> findBySenderIdentif(Long senderId);
}
