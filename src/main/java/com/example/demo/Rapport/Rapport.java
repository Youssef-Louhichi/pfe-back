package com.example.demo.rapport;

import java.io.Serializable;
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
	
	@OneToMany(mappedBy = "rapport", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Graph> graphs;
	
	@ManyToOne
    @JoinColumn(name = "cnx_id", nullable = false)
    private Connexion cnxrapport;
	
	
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
	@JsonIgnoreProperties("databases")
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

	public Rapport(String titre, List<Graph> graphs) {
		this.titre = titre;
		this.graphs = graphs;
	}

	public Rapport()
	{
		
	}
	
}
