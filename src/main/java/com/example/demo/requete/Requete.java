package com.example.demo.requete;

import java.io.Serializable;
import java.time.LocalDate;

import com.example.demo.tables.DbTable;
import com.example.demo.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "requete")
public class Requete implements Serializable{
	

	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate SentAt;
    
    @ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;
    private String content ;
    
    
    @ManyToOne
   	@JoinColumn(name = "table_id", nullable = false)
   	private DbTable tableReq;

	public DbTable getTable() {
		return tableReq;
	}

	public void setTable(DbTable table) {
		this.tableReq = table;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getSentAt() {
		return SentAt;
	}

	public void setSentAt(LocalDate sentAt) {
		SentAt = sentAt;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public Requete(LocalDate sentAt, User sender,String content) {
		
		SentAt = sentAt;
		this.sender = sender;
		this.content = content;
	}
	public Requete()
	{
		
	}
    
    

}
