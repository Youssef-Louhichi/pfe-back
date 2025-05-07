package com.example.demo.having;

import com.example.demo.requete.Requete;

public class HavingCondition {

	 private String function; // e.g. "count", "sum", "avg"
	    private Long columnId;
	    private String operator; // e.g. ">", "<", "="
	    private Object value;
	    private String subqueryComparator;
	    private boolean test;
	    
	    
	    
		public boolean isTest() {
			return test;
		}
		public void setTest(boolean test) {
			this.test = test;
		}
		public String getSubqueryComparator() {
			return subqueryComparator;
		}
		public void setSubqueryComparator(String subqueryComparator) {
			this.subqueryComparator = subqueryComparator;
		}
		public String getFunction() {
			return function;
		}
		public void setFunction(String function) {
			this.function = function;
		}
		public Long getColumnId() {
			return columnId;
		}
		public void setColumnId(Long columnId) {
			this.columnId = columnId;
		}
		public String getOperator() {
			return operator;
		}
		public void setOperator(String operator) {
			this.operator = operator;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	    
		  public boolean isSubquery() {
		        return value instanceof Requete;
		    }
	    
	    
	    
	
}
