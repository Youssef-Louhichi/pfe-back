package com.example.demo.condition;

import jakarta.persistence.Embeddable;

@Embeddable
public class FilterCondition {

	
	private String columnName;
    private String operator;  
    private String value;
    private String tableName;
    
    
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public FilterCondition(String columnName, String operator, String value) {
		
		this.columnName = columnName;
		this.operator = operator;
		this.value = value;
	}
    
	public FilterCondition()
	{
		
	}
}
