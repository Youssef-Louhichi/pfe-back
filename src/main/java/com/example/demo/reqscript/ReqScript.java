package com.example.demo.reqscript;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.requete.Requete;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ReqScript")
public class ReqScript implements Serializable{
	

	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private LocalDate createdAt ;
    
    @OneToMany(mappedBy = "script")
	@JsonIgnoreProperties("script")
	private List<Requete> reqs;

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

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public List<Requete> getReqs() {
		return reqs;
	}

	public void setReqs(List<Requete> reqs) {
		this.reqs = reqs;
	}

	public ReqScript(String name, LocalDate createdAt, List<Requete> reqs) {
		
		this.name = name;
		this.createdAt = createdAt;
		this.reqs = reqs;
	}
    
    
    public ReqScript()
    {
    	
    }
    
    
	
}
