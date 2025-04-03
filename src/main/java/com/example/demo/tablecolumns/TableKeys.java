package com.example.demo.tablecolumns;

import java.io.Serializable;

import com.example.demo.tables.DbTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "foreign_keys",
uniqueConstraints = { 
        @UniqueConstraint(columnNames = {"table_id", "fk_column"}) 
    })
public class TableKeys implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    @JsonIgnoreProperties("outgoingForeignKeys")
    private DbTable table;  // The table that has the foreign key

    @ManyToOne
    @JoinColumn(name = "referenced_table_id", nullable = false)
    @JsonIgnoreProperties("incomingForeignKeys")
    private DbTable referencedTable;  // The referenced table

    @Column(name = "fk_column", nullable = false)
    private String fkColumn;  // Column in the current table acting as the foreign key

    @Column(name = "referenced_column", nullable = false)
    private String referencedColumn;  // Column in the referenced table

    private Boolean compositeKey = false; // True if part of a composite foreign key

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DbTable getTable() {
		return table;
	}

	public void setTable(DbTable table) {
		this.table = table;
	}

	public DbTable getReferencedTable() {
		return referencedTable;
	}

	public void setReferencedTable(DbTable referencedTable) {
		this.referencedTable = referencedTable;
	}

	public String getFkColumn() {
		return fkColumn;
	}

	public void setFkColumn(String fkColumn) {
		this.fkColumn = fkColumn;
	}

	public String getReferencedColumn() {
		return referencedColumn;
	}

	public void setReferencedColumn(String referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	public Boolean getCompositeKey() {
		return compositeKey;
	}

	public void setCompositeKey(Boolean compositeKey) {
		this.compositeKey = compositeKey;
	}
    
    
    

}

