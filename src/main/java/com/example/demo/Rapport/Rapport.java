package com.example.demo.rapport;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.connexions.Connexion;

import com.example.demo.graph.Graph;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="Rapports")
public class Rapport implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String titre;
	private LocalDate createdAt;
	private LocalDate updatedAt;
	
	@OneToMany(mappedBy = "rapport", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnoreProperties("rapport")
	private List<Graph> graphs;
	
	@ManyToOne
    @JoinColumn(name = "cnx_id", nullable = false)
	@JsonIgnoreProperties("rapports databases")
    private Connexion cnxrapport;
	
	
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
	@JsonIgnoreProperties("rapports")
    private User user;
	
	
	
	

	public Connexion getCnxrapport() {
		return cnxrapport;
	}

	public void setCnxrapport(Connexion cnxrapport) {
		this.cnxrapport = cnxrapport;
	}

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

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public List<Graph> getGraphs() {
		return graphs;
	}

	public void setGraphs(List<Graph> graphs) {
		this.graphs = graphs;
		for (Graph item : graphs) {
            item.setRapport(this); 
        }
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

	

	public Rapport(Long id, String titre, LocalDate createdAt, LocalDate updatedAt, List<Graph> graphs,
			Connexion cnxrapport, User user) {
		super();
		this.id = id;
		this.titre = titre;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.graphs = graphs;
		this.cnxrapport = cnxrapport;
		this.user = user;
	}

	public Rapport()
	{
		
	}

	@Override
	public String toString() {
		return "Rapport [id=" + id + ", titre=" + titre + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ ", graphs=" + graphs + ", cnxrapport=" + cnxrapport + ", user=" + user + "]";
	}
	
	
	
}
