package com.example.demo.requete;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.condition.AggregationRequest;
import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;
import com.example.demo.rapport.Rapport;
import com.example.demo.reqscript.ReqScript;
import com.example.demo.tables.DbTable;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "requete")
public class Requete implements Serializable{
	

	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate SentAt;
    
    @ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;
    private String content ;
    
    @ElementCollection
    private List<JoinCondition> joinConditions;
    
    @ElementCollection
    private List<FilterCondition> filters;
    
    @ElementCollection
    private List<AggregationRequest> aggregation;
    
    private List<Long> tables;
    
    private List<Long> ColumnId;
    
    
    private List<Long> groupByColumns; 
    
    
    
    
    
    
    
    @ManyToOne
   	@JoinColumn(name = "table_id", nullable = false)
   	private DbTable tableReq;
    
    
    
    
    
    @ManyToOne
    @JoinColumn(name = "script_id", nullable = true)
    @JsonIgnoreProperties("reqs")
    private ReqScript script;
    
    

    
    
    
	public List<FilterCondition> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterCondition> filters) {
		this.filters = filters;
	}

	public List<AggregationRequest> getAggregation() {
		return aggregation;
	}

	public void setAggregation(List<AggregationRequest> aggregation) {
		this.aggregation = aggregation;
	}

	public List<Long> gettables() {
		return tables;
	}

	public void settables(List<Long> tables) {
		this.tables = tables;
	}

	public List<Long> getColumnId() {
		return ColumnId;
	}

	public void setColumnId(List<Long> columnId) {
		ColumnId = columnId;
	}

	public List<Long> getGroupByColumns() {
		return groupByColumns;
	}

	public void setGroupByColumns(List<Long> groupByColumns) {
		this.groupByColumns = groupByColumns;
	}

	public DbTable getTableReq() {
		return tableReq;
	}

	public void setTableReq(DbTable tableReq) {
		this.tableReq = tableReq;
	}

	public ReqScript getScript() {
		return script;
	}

	public void setScript(ReqScript script) {
		this.script = script;
	}

	public DbTable getTable() {
		return tableReq;
	}

	public void setTable(DbTable table) {
		this.tableReq = table;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getSentAt() {
		return SentAt;
	}

	public void setSentAt(LocalDate sentAt) {
		SentAt = sentAt;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}
	
	public List<JoinCondition> getJoinConditions() {
        return joinConditions;
    }
    
    public void setJoinConditions(List<JoinCondition> joinConditions) {
        this.joinConditions = joinConditions;
    }
    

	public Requete(LocalDate sentAt, User sender,String content) {
		
		SentAt = sentAt;
		this.sender = sender;
		this.content = content;
	}
	public Requete()
	{
		
	}
    
    

}
