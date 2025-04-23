package com.example.demo.reqscript;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.requete.Requete;


@Service
public class ReqScriptService {

	
	@Autowired
	private ReqScriptRepository scriptrepository;
	

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
    	scriptrepository.deleteById(id);
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
