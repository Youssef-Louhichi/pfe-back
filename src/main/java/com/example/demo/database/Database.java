package com.example.demo.database;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.connexions.Connexion;
import com.example.demo.connexions.DatabaseType;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "db")
public class Database implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
	private DatabaseType dbtype;

    @ManyToOne
    @JoinColumn(name = "connexion_id", nullable = false)
    @JsonIgnoreProperties("databases")
    private Connexion connexion;
    
    @ManyToMany
    @JoinTable(
        name = "db_user",
        joinColumns = @JoinColumn(name = "db_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    private LocalDate createdAt;
    private LocalDate updatedAt;

   
    public Database() {
    }

    public Database(Long id, String name, DatabaseType dbtype, Connexion connexion, List<User> users,
			LocalDate createdAt, LocalDate updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.dbtype = dbtype;
		this.connexion = connexion;
		this.users = users;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}







	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
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

    public DatabaseType getDbtype() {
		return dbtype;
	}

	public void setDbtype(DatabaseType dbtype) {
		this.dbtype = dbtype;
	}

	public Connexion getConnexion() {
        return connexion;
    }

    public void setConnexion(Connexion connexion) {
        this.connexion = connexion;
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
}

