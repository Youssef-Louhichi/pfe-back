package com.example.demo.dto;

import java.util.Map;

public class InsertRequestDTO {
    private Long tableId;
    private Map<String, Object> columnValues;
    
    // Getters and setters
    public Long getTableId() {
        return tableId;
    }
    
    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }
    
    public Map<String, Object> getColumnValues() {
        return columnValues;
    }
    
    public void setColumnValues(Map<String, Object> columnValues) {
        this.columnValues = columnValues;
    }
}