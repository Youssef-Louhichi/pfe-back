package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DatabaseDashboardDto {
    private Long id;
    private String name;
    private String type;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private int tableCount;
    private int columnCount;
    private List<String> tableNames;
    private int queryCount;
    private LocalDateTime lastQueryAt;
    private int modificationCount;
    
    
    private Map<String, Long> monthlyQueryStats;
    
    private Long usedSizeBytes;  
    
    private Map<String, Long> topCollaborators;

    public Map<String, Long> getTopCollaborators() {
        return topCollaborators;
    }

    public void setTopCollaborators(Map<String, Long> topCollaborators) {
        this.topCollaborators = topCollaborators;
    }


    public Long getUsedSizeBytes() {
        return usedSizeBytes;
    }

    public void setUsedSizeBytes(Long usedSizeBytes) {
        this.usedSizeBytes = usedSizeBytes;
    }

  

    public Map<String, Long> getMonthlyQueryStats() {
        return monthlyQueryStats;
    }

    public void setMonthlyQueryStats(Map<String, Long> monthlyQueryStats) {
        this.monthlyQueryStats = monthlyQueryStats;
    }

    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDate getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDate updatedAt) {
		this.updatedAt = updatedAt;
	}
	public int getTableCount() {
		return tableCount;
	}
	public void setTableCount(int tableCount) {
		this.tableCount = tableCount;
	}
	public List<String> getTableNames() {
		return tableNames;
	}
	public void setTableNames(List<String> tableNames) {
		this.tableNames = tableNames;
	}
	public int getQueryCount() {
		return queryCount;
	}
	public void setQueryCount(int queryCount) {
		this.queryCount = queryCount;
	}
	public LocalDateTime getLastQueryAt() {
		return lastQueryAt;
	}
	public void setLastQueryAt(LocalDateTime lastQueryAt) {
		this.lastQueryAt = lastQueryAt;
	}

	public int getModificationCount() {
		return modificationCount;
	}
	public void setModificationCount(int modificationCount) {
		this.modificationCount = modificationCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

    
}
