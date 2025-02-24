package com.example.demo.requete;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.users.User;


@Service
public class RequeteService {

	
	@Autowired
	private RequeteRepository requeterepository;
	
	
	
	
    public Requete createReq(Requete requete) {
        return requeterepository.save(requete);
    }

   
    public List<Requete> getAllReq() {
        return requeterepository.findAll();
    }


    public Optional<Requete> getReqById(Long id) {
        return requeterepository.findById(id);
    }
    
    
    public void deleteReq(Long id) {
    	requeterepository.deleteById(id);
    }
    
    public Requete updateReq(Long id, Requete updatedReq) {
        return requeterepository.findById(id).map(requete -> {
        	requete.setSentAt(updatedReq.getSentAt());
        	requete.setSender(updatedReq.getSender());
        	requete.setContent(updatedReq.getContent());
            return requeterepository.save(requete);
        }).orElse(null);
    }
	
}
