package com.example.demo.condition;

import java.util.List;

import jakarta.persistence.Embeddable;

@Embeddable
public class AggregationRequest {

	private Long columnId;
    private List<String> functionagg;
	public Long getColumnId() {
		return columnId;
	}
	public void setColumnId(Long columnId) {
		this.columnId = columnId;
	}
	public List<String> getfunctionagg() {
		return functionagg;
	}
	public void setfunctionagg(List<String> functionagg) {
		this.functionagg = functionagg;
	}
	public AggregationRequest(Long columnId, List<String> functionagg) {
		
		this.columnId = columnId;
		this.functionagg = functionagg;
	}
    
	public AggregationRequest()
	{
		
	}
    
}
