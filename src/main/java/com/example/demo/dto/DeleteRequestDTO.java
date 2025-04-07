package com.example.demo.dto;

import java.util.List;
import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;

public class DeleteRequestDTO {

	
	private Long tableId;
	
	private List<FilterCondition> filters;
    
    private List<JoinCondition> joins;
    
    public DeleteRequestDTO(Long tableId, List<FilterCondition> filters, List<JoinCondition> joins) {
        this.tableId = tableId;
        this.filters = filters;
        this.joins = joins;
    }
  
   
   public Long getTableId() {
       return tableId;
   }
   
   public void setTableId(Long tableId) {
       this.tableId = tableId;
   }
   
 
	public List<FilterCondition> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterCondition> filters) {
		this.filters = filters;
	}

	public List<JoinCondition> getJoins() {
		return joins;
	}

	public void setJoins(List<JoinCondition> joins) {
		this.joins = joins;
	}
}
