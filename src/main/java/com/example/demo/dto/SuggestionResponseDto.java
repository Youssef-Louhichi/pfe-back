package com.example.demo.dto;
//SuggestionResponse.java
import java.util.List;

public class SuggestionResponseDto {
 private boolean model_trained;
 private List<String> schema_tables;
 private String status;
 private List<String> suggestions;

 // Getters and Setters
 public boolean isModel_trained() {
     return model_trained;
 }

 public void setModel_trained(boolean model_trained) {
     this.model_trained = model_trained;
 }

 public List<String> getSchema_tables() {
     return schema_tables;
 }

 public void setSchema_tables(List<String> schema_tables) {
     this.schema_tables = schema_tables;
 }

 public String getStatus() {
     return status;
 }

 public void setStatus(String status) {
     this.status = status;
 }

 public List<String> getSuggestions() {
     return suggestions;
 }

 public void setSuggestions(List<String> suggestions) {
     this.suggestions = suggestions;
 }
}

