package com.example.demo.reqscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.requete.Requete;
import com.example.demo.requete.RequeteRepository;

import jakarta.persistence.EntityNotFoundException;


@Service
public class ReqScriptService {

	
	@Autowired
	private ReqScriptRepository scriptrepository;
	
	@Autowired
	private RequeteRepository reqrepo;
	

    public ReqScript createReqscript(ReqScript script) {
        return scriptrepository.save(script);
    }

   
    public List<ReqScript> getAllReqscript() {
        return scriptrepository.findAll();
    }


    public Optional<ReqScript> getReqById(Long id) {
        return scriptrepository.findById(id);
    }
    
    
    public void deleteReqscript(Long id) {
        // Find the script first
        ReqScript script = scriptrepository.findById(id)
                         .orElseThrow(() -> new EntityNotFoundException("Script not found with id: " + id));
        
        // Get all requests associated with this script
        List<Requete> relatedRequests = script.getReqs();
        if (relatedRequests != null && !relatedRequests.isEmpty()) {
            // Create a new collection to avoid ConcurrentModificationException
            List<Requete> requestsToUpdate = new ArrayList<>(relatedRequests);
            
            for (Requete requete : requestsToUpdate) {
                rm(requete);
            }
        }
        
        // Now delete the script
        scriptrepository.deleteById(id);
    }
    
    
    public void rm (Requete req)
    {
    	req.setScript(null);
    	reqrepo.save(req);
    }
    
    public ReqScript updateReqscript(Long id, ReqScript updatedReq) {
        return scriptrepository.findById(id).map(requete -> {
        	requete.setName(updatedReq.getName());
        	requete.setCreatedAt(updatedReq.getCreatedAt());
        	requete.setReqs(updatedReq.getReqs());
            return scriptrepository.save(requete);
        }).orElse(null);
    }
}
