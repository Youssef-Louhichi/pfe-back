package com.example.demo.dto;

import java.util.List;

import com.example.demo.condition.JoinCondition;

public class JoinRequestDTO {
	 private List<JoinCondition> joinConditions;
	    
	    public List<JoinCondition> getJoinConditions() {
	        return joinConditions;
	    }
	    
	    public void setJoinConditions(List<JoinCondition> joinConditions) {
	        this.joinConditions = joinConditions;
	    }

		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}
}
