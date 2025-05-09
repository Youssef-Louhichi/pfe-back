package com.example.demo.having;

import java.util.Map;

import com.example.demo.requete.Requete;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.Embeddable;


@Embeddable
public class HavingCondition {

	 private String functionhaving; // e.g. "count", "sum", "avg"
	    private Long columnId;
	    private String operator; // e.g. ">", "<", "="
	    private String value; // Changed to String

	    private transient Object valueObject; // Cached deserialized value
	    private static final ObjectMapper mapper = new ObjectMapper()
		        .registerModule(new JavaTimeModule());

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
	        return functionhaving;
	    }

	    public void setFunction(String function) {
	        this.functionhaving = function;
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
	        if (valueObject != null) {
	            return valueObject;
	        }
	        if (value == null || value.isEmpty()) {
	            return null;
	        }
	        try {
	            valueObject = mapper.readValue(value, Requete.class);
	        } catch (Exception e) {
	            valueObject = value;
	        }
	        return valueObject;
	    }

	    public void setValue(Object value) {
	        this.valueObject = value;
	        try {
	            if (value == null) {
	                this.value = null;
	            } else if (value instanceof String) {
	                this.value = (String) value;
	            } else if (value instanceof Requete) {
	                this.value = mapper.writeValueAsString(value);
	            } else if (value instanceof Map) {
	                this.value = mapper.writeValueAsString(value);
	                this.valueObject = mapper.convertValue(value, Requete.class);
	            } else {
	                throw new IllegalArgumentException("Value must be a String, Requete, or JSON Object");
	            }
	        } catch (Exception e) {
	            throw new RuntimeException("Failed to serialize value", e);
	        }
	    }

	    public boolean isSubquery() {
	        return getValue() instanceof Requete;
	    }
	
}
