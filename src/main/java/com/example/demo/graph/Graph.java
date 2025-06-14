package com.example.demo.graph;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.example.demo.rapport.Rapport;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="graphiques")
public class Graph implements Serializable{
	

	
private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    
    private String chartType ; 
    private List<String> colors ;
    private String fontSize; 
    private String columnX ;
    private String columnY ;
    @Lob
    private String data; 
    private String format ;
    private List<String> headers ;
    private int height;
    private int width ;
    
    private int leftpos ;
    
    private int toppos ;
    
    
    @ManyToOne
    @JoinColumn(name = "rapport_id", nullable = false)
    @JsonIgnoreProperties("graphs")
    private Rapport rapport;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	public List<String> getColors() {
		return colors;
	}
	public void setColors(List<String> colors) {
		this.colors = colors;
	}
	public String getColumnX() {
		return columnX;
	}
	public void setColumnX(String columnX) {
		this.columnX = columnX;
	}
	public String getColumnY() {
		return columnY;
	}
	public void setColumnY(String columnY) {
		this.columnY = columnY;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public List<String> getHeaders() {
		return headers;
	}
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}

	
	
	
	public int getLeftpos() {
		return leftpos;
	}
	public void setLeftpos(int leftpos) {
		this.leftpos = leftpos;
	}
	public int getToppos() {
		return toppos;
	}
	public void setToppos(int toppos) {
		this.toppos = toppos;
	}
	public Rapport getRapport() {
		return rapport;
	}
	public void setRapport(Rapport rapport) {
		this.rapport = rapport;
	}
	
	public void setData(Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.data = objectMapper.writeValueAsString(data);  
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    
    public List<Map<String, Object>> getData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(this.data, objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	
	
	public Graph(Long id, String chartType, List<String> colors, String columnX, String columnY, String data,
			String format, List<String> headers, int height, int width, int left, int top) {
		
		this.id = id;
		this.chartType = chartType;
		this.colors = colors;
		this.columnX = columnX;
		this.columnY = columnY;
		this.data = data;
		this.format = format;
		this.headers = headers;
		this.height = height;
		this.width = width;
		this.leftpos = left;
		this.toppos = top;
	}
	
	public Graph ()
	{
		
	}
	public String getFontSize() {
		return fontSize;
	}
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}
    
    
    
    

	
}
