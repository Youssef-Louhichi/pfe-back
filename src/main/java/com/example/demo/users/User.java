package com.example.demo.users;

import java.io.Serializable;
import java.util.List;

import com.example.demo.creator.Creator;
import com.example.demo.database.Database;
import com.example.demo.rapport.Rapport;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public  class User implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long identif;
	private String mail;
	private String password;
	
	private String type;

    
	
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonIgnore
   	private List<Rapport> rapports;
	
	
	public String getType() {
        return this instanceof Creator ? "Creator" : "Analyst";
    }
	
	public List<Rapport> getRapports() {
		return rapports;
	}

	public void setRapports(List<Rapport> rapports) {
		this.rapports = rapports;
	}

	public Long getIdentif() {
	    return identif;
	}

	public void setIdentif(Long identif) {
	    this.identif = identif;
	}
	
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	

	public User(String mail, String password, String role) {
		
		this.mail = mail;
		this.password = password;
		
	}
	public User() {
		
	}
	


}
