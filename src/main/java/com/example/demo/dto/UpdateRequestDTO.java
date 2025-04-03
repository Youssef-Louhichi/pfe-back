package com.example.demo.dto;

import java.util.List;
import java.util.Map;

import com.example.demo.Condition.FilterCondition;

public class UpdateRequestDTO {
	   private Long tableId;
	    private Map<String, Object> columnValues;
	    
	     private List<FilterCondition> filters;
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

		public List<FilterCondition> getFilters() {
			return filters;
		}

		public void setFilters(List<FilterCondition> filters) {
			this.filters = filters;
		}
}
