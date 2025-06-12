package com.example.demo.dto;

import java.util.List;

public class SuggestionRequestDto {
	
	private Long user_id;
    private Database database;

    // Getters and setters

    public static class Database {
        private String id;
        private String name;
        private List<Table> tables;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Table> getTables() {
			return tables;
		}
		public void setTables(List<Table> tables) {
			this.tables = tables;
		}

        // Getters and setters
    }

    public static class Table {
        private String id;
        private String name;
        private List<Column> columns;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Column> getColumns() {
			return columns;
		}
		public void setColumns(List<Column> columns) {
			this.columns = columns;
		}

        // Getters and setters
    }

    public static class Column {
        private String name;
        private String type;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}

        
    }

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}
    
    

}
