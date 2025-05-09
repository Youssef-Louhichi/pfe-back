package com.example.demo.condition;

import java.util.Map;

import com.example.demo.requete.Requete;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

@Embeddable
public class FilterCondition {

	
	private String columnName;
    private String operator;  
    @Lob
    private String value;
    private String tableName;
    private boolean test;
    
    private transient Object valueObject;
    
    public boolean isTest() {
        return test;
    }
    
    public void setTest(boolean test) {
        this.test = test;
    }
    
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	private static final ObjectMapper mapper = new ObjectMapper()
	        .registerModule(new JavaTimeModule());

    // Getter for value (returns either String or Requete)
    public Object getValue() {
        if (valueObject != null) {
            return valueObject; // Return cached object if already deserialized
        }
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
        	System.out.println("ezezez");
            // Try to deserialize as Requete
            valueObject = mapper.readValue(value, Requete.class);
        } catch (Exception e) {
            // If deserialization fails, treat it as a String
            valueObject = value;
        }
        return valueObject;
    }

    // Setter for value (accepts String or Requete)
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
                // Attempt to convert JSON object to Requete
                this.value = mapper.writeValueAsString(value);
                this.valueObject = mapper.convertValue(value, Requete.class);
            } else {
                throw new IllegalArgumentException("Value must be a String, Requete, or JSON Object");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize value", e);
        }
    }
	public FilterCondition(String columnName, String operator, String value) {
		
		this.columnName = columnName;
		this.operator = operator;
		this.value = value;
	}
    
	public FilterCondition()
	{
		
	}
}
