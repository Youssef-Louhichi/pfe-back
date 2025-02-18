package com.example.demo.users;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long identif;
	private String mail;
	private String password;
	private String role;
	
	
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
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public User(String mail, String password, String role) {
		
		this.mail = mail;
		this.password = password;
		this.role = role;
	}
	public User() {
		
	}
	


}
