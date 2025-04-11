package com.example.demo.relations;


import com.example.demo.analyst.Analyst;
import com.example.demo.tablecolumns.TabColumn;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
@Entity
@DiscriminatorValue("COLUMN")
public class RelationColumn extends Relation {
	
	@ManyToOne
	@JoinColumn(name = "column_id", nullable = true)
	@JsonIgnoreProperties("realtionColumns table")
    private TabColumn column;

	public RelationColumn(Analyst analyst,TabColumn column) {
		super(analyst);
		this.column = column;
	}

	public RelationColumn() {
		super();
	}

	@JsonIgnoreProperties("realtionColumns table")
	public TabColumn getColumn() {
		return column;
	}

	public void setColumn(TabColumn column) {
		this.column = column;
	}
	
	

}
