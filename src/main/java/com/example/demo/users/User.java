package com.example.demo.users;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.creator.Creator;

import com.example.demo.database.Database;
import com.example.demo.rapport.Rapport;
import com.example.demo.reqscript.ReqScript;
import com.example.demo.task.Task;
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
public  class User implements UserDetails{
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long identif;
	private String mail;
	private String password;
	
	private String type;

	 @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	    @JsonIgnore
	    private List<ReqScript> scripts;
	
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonIgnore
   	private List<Rapport> rapports;
	
	 @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
	 @JsonIgnore
	 private List<Task> sentTasks;

	    
	 @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
	 @JsonIgnore
	 private List<Task> receivedTasks;
	
	 
	 
	 
	 
	
	public List<ReqScript> getScripts() {
		return scripts;
	}

	public void setScripts(List<ReqScript> scripts) {
		this.scripts = scripts;
	}

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
	
	

	public List<Task> getSentTasks() {
		return sentTasks;
	}

	public void setSentTasks(List<Task> sentTasks) {
		this.sentTasks = sentTasks;
	}

	public List<Task> getReceivedTasks() {
		return receivedTasks;
	}

	public void setReceivedTasks(List<Task> receivedTasks) {
		this.receivedTasks = receivedTasks;
	}

	public User(String mail, String password, String role) {
		
		this.mail = mail;
		this.password = password;
		
	}
	public User() {
		
	}
	

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

}
