package com.example.demo.TableColumns;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.database.Database;
import com.example.demo.tables.DbTable;

public interface ColumnRepository extends JpaRepository<TabColumn,Long>{
	
	TabColumn findByNameAndTable(String name, DbTable table);

	
	}
