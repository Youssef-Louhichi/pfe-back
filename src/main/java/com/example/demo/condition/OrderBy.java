package com.example.demo.condition;

import jakarta.persistence.Embeddable;

@Embeddable
public class OrderBy {
	
	private Long colId; // Use column ID instead of name
    private String orderType;
    
    public Long getColId() {
        return colId;
    }
    public void setColId(Long colId) {
        this.colId = colId;
    }
    
    public String getOrderType() {
        return orderType;
    }
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
	

}
