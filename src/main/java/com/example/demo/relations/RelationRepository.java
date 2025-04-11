package com.example.demo.relations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelationRepository extends JpaRepository<Relation,Long>{

	@Modifying
    @Query("DELETE FROM Relation r WHERE r.analyst.id = :analystId")
    int deleteByAnalystId(@Param("analystId") Long analystId);
	

}
