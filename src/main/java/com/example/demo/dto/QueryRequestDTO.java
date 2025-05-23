	package com.example.demo.dto;
	
	import java.util.List;

import com.example.demo.condition.AggregationRequest;
import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.OrderBy;
import com.example.demo.having.HavingCondition;
import com.example.demo.requete.Requete;
	
	public class QueryRequestDTO {
		
		
		private Requete req;
		private List<Long> tableId;
		private List<Long> columnId;
		 private JoinRequestDTO joinRequest;
		private List<FilterCondition> filters;
		
		private List<Long> groupByColumns;
		
		 private List<AggregationRequest> aggregations; 
		 private List<HavingCondition> HavingConditions;
		 
		 private List<OrderBy> orderBy ;
		 
		 private Integer  limit ;
		 
		 
		 
		 
		 public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

		

		public List<OrderBy> getOrderBy() {
			return orderBy;
		}

		public void setOrderBy(List<OrderBy> orderBy) {
			this.orderBy = orderBy;
		}

		public List<HavingCondition> getHavingConditions() {
			return HavingConditions;
		}

		public void setHavingConditions(List<HavingCondition> havingConditions) {
			HavingConditions = havingConditions;
		}

		public JoinRequestDTO getJoinRequest() {
		        return joinRequest;
		    }
		    
		    public void setJoinRequest(JoinRequestDTO joinRequest) {
		        this.joinRequest = joinRequest;
		    }
		
		public List<AggregationRequest> getAggregations() {
			return aggregations;
		}
		public void setAggregations(List<AggregationRequest> aggregations) {
			this.aggregations = aggregations;
		}
		public List<Long> getGroupByColumns() {
			return groupByColumns;
		}
		public void setGroupByColumns(List<Long> groupByColumns) {
			this.groupByColumns = groupByColumns;
		}
		public List<FilterCondition> getFilters() {
			return filters;
		}
		public void setFilters(List<FilterCondition> filters) {
			this.filters = filters;
		}
		
		public Requete getReq() {
			return req;
		}
		public void setReq(Requete req) {
			this.req = req;
		}
	
		public List<Long> getTableId() {
			return tableId;
		}
		public void setTableId(List<Long> tableId) {
			this.tableId = tableId;
		}
		public List<Long> getColumnId() {
			return columnId;
		}
		public void setColumnId(List<Long> columnId) {
			this.columnId = columnId;
		}
		
		
		
		
	
	}
