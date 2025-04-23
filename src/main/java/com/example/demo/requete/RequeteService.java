package com.example.demo.requete;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.querydsl.DynamicQueryService;
import com.example.demo.reqscript.ReqScript;
import com.example.demo.reqscript.ReqScriptRepository;




@Service
public class RequeteService {

	
	@Autowired
	private RequeteRepository requeterepository;
	
	@Autowired
	private ReqScriptRepository scriptrepository;
	
	@Autowired
	private DynamicQueryService queryService;
	
	
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
    
    
    
    public List<Requete> findbySender(Long senderId)
    {
    	return requeterepository.findBySenderIdentif(senderId);
    }
    
    
    
    public String addRequeteToScript(Long scriptId, Long requeteId) {
        ReqScript script = scriptrepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("Script not found with id: " + scriptId));

        Requete requete = requeterepository.findById(requeteId)
                .orElseThrow(() -> new RuntimeException("Requete not found with id: " + requeteId));

        
        requete.setScript(script);

        
        if (!script.getReqs().contains(requete)) {
            script.getReqs().add(requete);
        }

  
        requeterepository.save(requete);

        return "Requete with id " + requeteId + " successfully added to script with id " + scriptId;
    }
    
    
    
    public String removeRequeteFromScript(Long scriptId, Long requeteId) {
        // 1. Check if the script exists
        ReqScript script = scriptrepository.findById(scriptId)
            .orElseThrow(() -> new RuntimeException("Script not found"));

  
        Requete requete = requeterepository.findById(requeteId)
            .orElseThrow(() -> new RuntimeException("Requete not found"));

        
        if (!script.getReqs().contains(requete)) {
            throw new RuntimeException("Requete does not belong to the script");
        }

       
        //script.getReqs().remove(requete);
        requete.setScript(null); 
        scriptrepository.save(script);
        requeterepository.save(requete);

        return "Requete removed from script successfully";
    }
    
    
    
    public List<Map<String, Object>> execReqFromDb(Long id) {
        Requete req = getReqById(id).orElseThrow(() -> new RuntimeException("Requete not found with id: " + id));
        return queryService.fetchTableDataWithCondition2(req);
    }


	
}
