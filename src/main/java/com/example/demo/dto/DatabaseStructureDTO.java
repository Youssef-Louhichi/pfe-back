package com.example.demo.dto;

import java.util.List;

import com.example.demo.database.Database;


public class DatabaseStructureDTO {
	
	private Database database;
    public Database getDatabase() {
		return database;
	}
	public void setDatabase(Database database) {
		this.database = database;
	}
	public List<RelationTablesDto> getRelations() {
		return relations;
	}
	public void setRelations(List<RelationTablesDto> relations) {
		this.relations = relations;
	}
	private List<RelationTablesDto> relations;

}
