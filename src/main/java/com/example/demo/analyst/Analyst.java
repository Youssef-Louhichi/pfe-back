package com.example.demo.analyst;

import java.util.List;

import com.example.demo.database.Database;
import com.example.demo.users.User;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Analyst extends User {
	
	
	@ManyToMany
    @JoinTable(
        name = "db_user",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "db_id")
    )
    private List<Database> databases;

	public List<Database> getDatabases() {
		return databases;
	}

	public void setDatabases(List<Database> databases) {
		this.databases = databases;
	}

	public Analyst(String mail, String password, String role, List<Database> databases) {
		super(mail, password, role);
		this.databases = databases;
	}

	public Analyst() {
		super();
	}
	
	
	

}
