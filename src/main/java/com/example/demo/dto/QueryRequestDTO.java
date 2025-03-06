package com.example.demo.dto;

import java.util.List;

import com.example.demo.Condition.FilterCondition;
import com.example.demo.requete.Requete;

public class QueryRequestDTO {
	
	
	private Requete req;
	private Long tableId;
	private List<Long> columnId;
	
	private List<FilterCondition> filters;
	
	private List<Long> groupByColumns;
	
	
	
	public List<Long> getGroupByColumns() {
		return groupByColumns;
	}
	public void setGroupByColumns(List<Long> groupByColumns) {
		this.groupByColumns = groupByColumns;
	}
	public List<FilterCondition> getFilters() {
		return filters;
	}
	public void setFilters(List<FilterCondition> filters) {
		this.filters = filters;
	}
	
	public Requete getReq() {
		return req;
	}
	public void setReq(Requete req) {
		this.req = req;
	}
	public Long getTableId() {
		return tableId;
	}
	public void setTableId(Long tableId) {
		this.tableId = tableId;
	}
	public List<Long> getColumnId() {
		return columnId;
	}
	public void setColumnId(List<Long> columnId) {
		this.columnId = columnId;
	}
	
	
	
	

}
