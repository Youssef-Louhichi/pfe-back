package com.example.demo.tablecolumns;

import java.io.Serializable;
import java.util.List;

import com.example.demo.relations.RelationColumn;
import com.example.demo.relations.RelationTable;
import com.example.demo.tables.DbTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name="Columns")
public class TabColumn implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;	
	private String type;
	
	  @OneToMany(mappedBy = "column", cascade = CascadeType.ALL,orphanRemoval = true)
	  @JsonIgnore
	  private List<RelationColumn> relationColumn;

   

	public String getType() {
		return type;
	}
	

	public void setType(String type) {
		this.type = type;
	}

	@ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    @JsonIgnoreProperties("columns")
    private DbTable table;



	
	public Long getId() {
		return id;
	}
	
	
	public void setId(Long id) {
		this.id = id;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public DbTable getTable() {
		return table;
	}


	public void setTable(DbTable table) {
		this.table = table;
	}
	
	
	public TabColumn(String name, DbTable table) {
		
		this.name = name;
		this.table = table;
	}
	
	public TabColumn()
	{
		
	}
	



}


