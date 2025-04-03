package com.example.demo.condition;

import jakarta.persistence.Embeddable;

@Embeddable
public class JoinCondition {
	
    private Long firstTableId;
    private String firstColumnName;
    private Long secondTableId;
    private String secondColumnName;
    private String joinType; // "INNER", "LEFT", "RIGHT"
    
    public Long getFirstTableId() {
        return firstTableId;
    }
    
    public void setFirstTableId(Long firstTableId) {
        this.firstTableId = firstTableId;
    }
    
    public String getFirstColumnName() {
        return firstColumnName;
    }
    
    public void setFirstColumnName(String firstColumnName) {
        this.firstColumnName = firstColumnName;
    }
    
    public Long getSecondTableId() {
        return secondTableId;
    }
    
    public void setSecondTableId(Long secondTableId) {
        this.secondTableId = secondTableId;
    }
    
    public String getSecondColumnName() {
        return secondColumnName;
    }
    
    public void setSecondColumnName(String secondColumnName) {
        this.secondColumnName = secondColumnName;
    }
    
    public String getJoinType() {
        return joinType;
    }
    
    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }
}