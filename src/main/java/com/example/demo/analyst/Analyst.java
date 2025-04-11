package com.example.demo.analyst;

import java.util.List;

import com.example.demo.database.Database;
import com.example.demo.relations.Relation;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
public class Analyst extends User {
	
	
	@OneToMany(mappedBy = "analyst", cascade = CascadeType.ALL,  orphanRemoval = true)
	@JsonIgnoreProperties("analyst")
    private List<Relation> relations;

	

	

	public Analyst(String mail, String password, String role, List<Relation> relations) {
		super(mail, password, role);
		this.relations = relations;
	}





	@JsonIgnoreProperties("analyst")
	public List<Relation> getRelations() {
		return relations;
	}





	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}





	public Analyst() {
		super();
	}
	
	
	

}
