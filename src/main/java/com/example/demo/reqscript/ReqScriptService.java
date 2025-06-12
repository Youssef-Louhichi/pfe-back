package com.example.demo.reqscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.requete.Requete;
import com.example.demo.requete.RequeteRepository;
import com.example.demo.users.UserRepository;

import jakarta.persistence.EntityNotFoundException;


@Service
public class ReqScriptService {

	
	@Autowired
	private ReqScriptRepository scriptrepository;
	
	@Autowired
	private RequeteRepository reqrepo;
	
	@Autowired
	private UserRepository userrepository;
	

    public ReqScript createReqscript(ReqScript script) {
        return scriptrepository.save(script);
    }

   
    public List<ReqScript> getAllReqscript() {
        return scriptrepository.findAll();
    }


    public Optional<ReqScript> getReqById(Long id) {
        return scriptrepository.findById(id);
    }
    
    public String deleteScript(Long scriptId) {
        ReqScript script = scriptrepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("Script not found with id: " + scriptId));

        // Remove the script from all associated requetes' scripts list
        if (script.getReqs() != null) {
            for (Requete requete : script.getReqs()) {
                if (requete.getScripts() != null) {
                    requete.getScripts().remove(script);
                    reqrepo.save(requete);
                }
            }
        }

        // Delete the script
        scriptrepository.delete(script);

        return "Script with id " + scriptId + " successfully deleted";
    }
    public ReqScript updateReqscript(Long id, ReqScript updatedReq) {
        return scriptrepository.findById(id).map(requete -> {
        	requete.setName(updatedReq.getName());
        	requete.setCreatedAt(updatedReq.getCreatedAt());
        	requete.setReqs(updatedReq.getReqs());
            return scriptrepository.save(requete);
        }).orElse(null);
    }
    
    
    
    public List<ReqScript> getScriptsByUser(Long userId) {
        // Check if the user exists
        userrepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<ReqScript> scripts = scriptrepository.findByUserIdentif(userId);
        if (scripts.isEmpty()) {
           return scripts ;
        }
        return scripts;
    }
    
    
    public List <Requete> getReqs(Long scriptId)
    {
    	ReqScript script = scriptrepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("Script not found with id: " + scriptId));
    	
    	return script.getReqs();
    }
}
