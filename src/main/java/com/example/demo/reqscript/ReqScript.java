package com.example.demo.reqscript;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.example.demo.requete.Requete;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
    
    @ManyToMany
    @JoinTable(
        name = "requete_reqscript",
        joinColumns = @JoinColumn(name = "reqscript_id"),
        inverseJoinColumns = @JoinColumn(name = "requete_id")
    )
    @JsonIgnoreProperties("scripts")
    private List<Requete> reqs;
    
    
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public ReqScript(String name, LocalDate createdAt, List<Requete> reqs,User user) {
		
		this.name = name;
		this.createdAt = createdAt;
		this.reqs = reqs;
		this.user = user;
	}
    
    
    public ReqScript()
    {
    	
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReqScript reqScript = (ReqScript) o;
        return Objects.equals(id, reqScript.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    
	
}
