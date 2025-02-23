package com.example.demo.connexions;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.database.Database;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name="connexions")
public class Connexion implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private DatabaseType dbtype;

	private String host;
	private int port;
	private String password;
	private String username;
	private LocalDate createdAt;
	private LocalDate updatedAt;

	@OneToMany(mappedBy = "connexion", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Database> databases;
	
	@ManyToOne
	@JoinColumn(name = "creator_id", nullable = false)
	@JsonIgnoreProperties("databases")
	private User creator;

	public Connexion(DatabaseType dbtype, String host, int port, String password, String username,
			LocalDate createdAt, LocalDate updatedAt, List<Database> databases,User creator) {
		this.dbtype = dbtype;
		this.host = host;
		this.port = port;
		this.password = password;
		this.username = username;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.databases = databases;
		this.creator=creator;
	}

	public Connexion() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DatabaseType getDbtype() {
		return dbtype;
	}

	public void setDbtype(DatabaseType dbtype) {
		this.dbtype = dbtype;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public List<Database> getDatabases() {
		return databases;
	}

	public void setDatabases(List<Database> databases) {
		this.databases = databases;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	
}
