package com.example.demo.database;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.analyst.Analyst;
import com.example.demo.connexions.Connexion;
import com.example.demo.connexions.DatabaseType;
import com.example.demo.tables.DbTable;
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
    
    @ManyToMany(mappedBy = "databases")
	@JsonIgnoreProperties("databases")
    private List<Analyst> analysts;

    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    @OneToMany(mappedBy = "database", cascade = CascadeType.ALL)
	private List<DbTable> tables;
    
    
    
   
    public Database() {
    }

    


	public List<DbTable> getTables() {
		return tables;
	}

	public void setTables(List<DbTable> tables) {
		this.tables = tables;
	}

	public Database(Long id, String name, DatabaseType dbtype, Connexion connexion, List<Analyst> analysts,
			LocalDate createdAt, LocalDate updatedAt) {
		
		this.id = id;
		this.name = name;
		this.dbtype = dbtype;
		this.connexion = connexion;
		this.analysts = analysts;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}







	public List<Analyst> getAnalysts() {
		return analysts;
	}

	public void setAnalysts(List<Analyst> analysts) {
		this.analysts = analysts;
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

