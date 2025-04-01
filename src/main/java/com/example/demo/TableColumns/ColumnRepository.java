package com.example.demo.tablecolumns;


import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.tables.DbTable;

public interface ColumnRepository extends JpaRepository<TabColumn,Long>{
	
	TabColumn findByNameAndTable(String name, DbTable table);

	
	}
