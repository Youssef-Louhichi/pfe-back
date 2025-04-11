package com.example.demo.dto;

import java.util.List;

public class RelationDto {
	
	private Long databaseId;
	private List<Long> tablesIds;
	private List<Long> columnsIds;
	public Long getDatabaseId() {
		return databaseId;
	}
	public void setDatabaseId(Long databaseId) {
		this.databaseId = databaseId;
	}
	public List<Long> getTablesIds() {
		return tablesIds;
	}
	public void setTablesIds(List<Long> tablesIds) {
		this.tablesIds = tablesIds;
	}
	public List<Long> getColumnsIds() {
		return columnsIds;
	}
	public void setColumnsIds(List<Long> columnsIds) {
		this.columnsIds = columnsIds;
	}
	

	
}
