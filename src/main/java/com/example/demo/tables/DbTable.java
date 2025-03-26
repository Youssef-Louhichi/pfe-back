package com.example.demo.tables;

import java.io.Serializable;
import java.util.List;


import com.example.demo.TableColumns.TabColumn;
import com.example.demo.database.Database;
import com.example.demo.requete.Requete;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="tables")
public class DbTable implements Serializable{
	
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	

    @ManyToOne
    @JoinColumn(name = "database_id", nullable = false)
    @JsonIgnoreProperties("tables")
    private Database database;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
	private List<TabColumn> columns;
   
    
    
    @OneToMany(mappedBy = "tableReq", cascade = CascadeType.ALL)
   	private List<Requete> requetes;
      
    
	public List<TabColumn> getColumns() {
		return columns;
	}


	public void setColumns(List<TabColumn> columns) {
		this.columns = columns;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public DbTable(String name, Database database) {
		
		this.name = name;
		this.database = database;
	}
	public DbTable()
	{
		
	}
	
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Database getDatabase() {
		return database;
	}


	public void setDatabase(Database database) {
		this.database = database;
	}
	
}
