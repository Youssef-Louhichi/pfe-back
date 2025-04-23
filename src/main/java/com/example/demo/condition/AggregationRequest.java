package com.example.demo.condition;

import jakarta.persistence.Embeddable;

@Embeddable
public class AggregationRequest {

	private Long columnId;
    private String functionagg;
	public Long getColumnId() {
		return columnId;
	}
	public void setColumnId(Long columnId) {
		this.columnId = columnId;
	}
	public String getfunctionagg() {
		return functionagg;
	}
	public void setfunctionagg(String functionagg) {
		this.functionagg = functionagg;
	}
	public AggregationRequest(Long columnId, String functionagg) {
		
		this.columnId = columnId;
		this.functionagg = functionagg;
	}
    
	public AggregationRequest()
	{
		
	}
    
}
