package com.example.demo.relations;

import com.example.demo.analyst.Analyst;
import com.example.demo.tables.DbTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
@Entity
@DiscriminatorValue("TABLE")
public class RelationTable  extends Relation {
	
	@ManyToOne
	@JoinColumn(name = "table_id" , nullable = true)
	@JsonIgnoreProperties("relationTables columns")
    private DbTable table;

	public RelationTable(Analyst analyst,DbTable table) {
		super(analyst);
		this.table = table;
	}

	public RelationTable() {
		super();
	}

	@JsonIgnoreProperties("relationTables columns")
	public DbTable getTable() {
		return table;
	}

	public void setTable(DbTable table) {
		this.table = table;
	}
	
	

}
