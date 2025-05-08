package com.example.demo.requete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.condition.FilterCondition;
import com.example.demo.condition.JoinCondition;
import com.example.demo.querydsl.DynamicQueryService;
import com.example.demo.reqscript.ReqScript;
import com.example.demo.reqscript.ReqScriptRepository;
import com.example.demo.tablecolumns.ColumnRepository;
import com.example.demo.tables.TableRepository;




@Service
public class RequeteService {

	
	@Autowired
	private RequeteRepository requeterepository;
	
	@Autowired
	private ReqScriptRepository scriptrepository;
	
	@Autowired
	private DynamicQueryService queryService;
	
	
	@Autowired
    private ColumnRepository columnRepository;
    
    @Autowired
    private TableRepository tableRepository;
	
	
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
        	//requete.setScript(updatedReq.getScript());
            return requeterepository.save(requete);
        }).orElse(null);
    }
    
    
    
    public List<Requete> findbySender(Long senderId)
    {
    	return requeterepository.findBySenderIdentif(senderId);
    }
    
    
    
    public String addRequeteToScripts(List<Long> scriptIds, Long requeteId) {
        Requete requete = requeterepository.findById(requeteId)
                .orElseThrow(() -> new RuntimeException("Requete not found with id: " + requeteId));

        if (requete.getScripts() == null) {
            requete.setScripts(new java.util.ArrayList<>());
        }

        String result = " ";
        boolean anyAdded = false;

        for (Long scriptId : scriptIds) {
            ReqScript script = scriptrepository.findById(scriptId)
                    .orElseThrow(() -> new RuntimeException("Script not found with id: " + scriptId));

            if (script.getReqs() == null) {
                script.setReqs(new java.util.ArrayList<>());
            }

            boolean relationshipExists = requete.getScripts().stream()
                    .anyMatch(s -> s.getId().equals(scriptId));

            if (!relationshipExists) {
                requete.getScripts().add(script);
                //script.getReqs().add(requete);
                anyAdded = true;
                result = result + "added";
                System.out.println(" oooki ");
            } else {
               result = result + "not added";
                System.out.println(" already in script ");
            }
        }

        if (anyAdded) {
            requeterepository.save(requete);
            scriptrepository.saveAll(scriptrepository.findAllById(scriptIds));
        }

        if (result.length() > 0) {
            return result;
        } else {
            return "No scripts were updated";
        }
    }

    public String removeRequeteFromScripts(List<Long> scriptIds, Long requeteId) {
        Requete requete = requeterepository.findById(requeteId)
                .orElseThrow(() -> new RuntimeException("Requete not found with id: " + requeteId));

        List<ReqScript> scripts = scriptrepository.findAllById(scriptIds);
        if (scripts.size() != scriptIds.size()) {
            throw new RuntimeException("One or more scripts not found");
        }

        String result = " ";
        boolean anyRemoved = false;

        for (ReqScript script : scripts) {
            if (script.getReqs().contains(requete) && requete.getScripts().contains(script)) {
                script.getReqs().remove(requete);
                requete.getScripts().remove(script);
                anyRemoved = true;
                result = result+ "removed";
            } else {
                result = result+ "not removed";
            }
        }

        if (anyRemoved) {
            requeterepository.save(requete);
            scriptrepository.saveAll(scripts);
        }

        if (result.length() > 0) {
            return result;
        } else {
            return "No scripts were updated";
        }
    }
    
    public List<Map<String, Object>> execReqFromDb(Long id) {
        Requete req = getReqById(id).orElseThrow(() -> new RuntimeException("Requete not found with id: " + id));
        return queryService.fetchTableDataWithCondition2(req);
    }

    public List<List<Map<String, Object>>> executeScriptById(Long scriptId) {
        // Find the script by ID
        ReqScript script = scriptrepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("Script not found with ID: " + scriptId));
        
        // Get all the requests associated with this script
        List<Requete> scriptRequests = script.getReqs();
        
        if (scriptRequests == null || scriptRequests.isEmpty()) {
            throw new RuntimeException("No requests found in script with ID: " + scriptId);
        }
        
        // Execute each request and collect results
        List<List<Map<String, Object>>> allResults = new ArrayList<>();
        
        for (Requete req : scriptRequests) {
            // Execute each request using the existing method
            List<Map<String, Object>> reqResult = execReqFromDb(req.getId());
            allResults.add(reqResult);
        }
        
        return allResults;
    }
    
    
    
	
}
