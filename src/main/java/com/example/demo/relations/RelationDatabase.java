package com.example.demo.relations;

import com.example.demo.analyst.Analyst;
import com.example.demo.database.Database;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
@Entity
@DiscriminatorValue("DATABASE")
public class RelationDatabase extends Relation {
	
	@ManyToOne
	@JoinColumn(name = "db_id", nullable = true)
	@JsonIgnoreProperties("relationDatabases tables connexion")
    private Database database;

	public RelationDatabase(Analyst analyst,Database database) {
		super(analyst);
		this.database = database;
	}

	public RelationDatabase() {
		super();
	}

	@JsonIgnoreProperties("relationDatabases tables connexion")
	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	
}
