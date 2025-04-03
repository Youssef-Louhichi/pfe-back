package com.example.demo.creator;

import java.util.List;

import com.example.demo.connexions.Connexion;
import com.example.demo.database.Database;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
public class Creator extends User {
	
	
	
	
	@OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
	@JsonIgnoreProperties("creator")
   	private List<Connexion> connexions;

	public Creator(String mail, String password, String role, List<Connexion> connexions) {
		super(mail, password, role);
		this.connexions = connexions;
	}

	public Creator() {
		super();
	}

	public List<Connexion> getConnexions() {
		return connexions;
	}

	public void setConnexions(List<Connexion> connexions) {
		this.connexions = connexions;
	}
	

}
