package com.example.demo.condition;

public class AggregationRequest {

	private Long columnId;
    private String function;
	public Long getColumnId() {
		return columnId;
	}
	public void setColumnId(Long columnId) {
		this.columnId = columnId;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public AggregationRequest(Long columnId, String function) {
		
		this.columnId = columnId;
		this.function = function;
	}
    
	public AggregationRequest()
	{
		
	}
    
}
