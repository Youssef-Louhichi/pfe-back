package com.example.demo.reqscript;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ReqScriptRepository extends JpaRepository<ReqScript,Long>{

	@Query("SELECT s FROM ReqScript s WHERE s.user.identif = :userId")
    List<ReqScript> findByUserIdentif(@Param("userId") Long userId);
}
