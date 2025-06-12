package com.example.demo.requete;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.dto.SuggestionRequestDto;
import com.example.demo.dto.SuggestionResponseDto;




@RestController
@RequestMapping("api/requete")
public class RequeteController {

	
	
	
	@Autowired
	private RequeteService requeteService ;
	
	
	 @PostMapping
	    public Requete create(@RequestBody Requete req) {
	        return requeteService.createReq(req);
	    }
	 
	 
	 
	 
	    @GetMapping
	    public List<Requete> getAll() {
	        return requeteService.getAllReq();
	    }

	    
	    @GetMapping("/{id}")
	    public Optional<Requete> getById(@PathVariable Long id) {
	        return requeteService.getReqById(id);
	    }

	    
	    @PutMapping("/{id}")
	    public Requete update(@PathVariable Long id, @RequestBody Requete updatedreq) {
	        return requeteService.updateReq(id, updatedreq);
	    }

	    
	    @DeleteMapping("/{id}")
	    public void delete(@PathVariable Long id) {
	    	requeteService.deleteReq(id);
	    }
	    
	    
	    @GetMapping("user/{senderId}")
	    public List<Requete> getBysenderId(@PathVariable Long senderId) {
	        return requeteService.findbySender(senderId);
	    }
	    
	    
	  
	    @PostMapping("/add-requete/{requeteId}")
	    public ResponseEntity<String> addRequeteToScripts(
	            @RequestParam List<Long> scriptIds,
	            @PathVariable Long requeteId) {
	        String message = requeteService.addRequeteToScripts(scriptIds, requeteId);
	        return ResponseEntity.ok(message);
	    }

	    @PostMapping("/remove-requete/{requeteId}")
	    public ResponseEntity<String> removeRequeteFromScripts(
	            @RequestParam List<Long> scriptIds,
	            @PathVariable Long requeteId) {
	        String result = requeteService.removeRequeteFromScripts(scriptIds, requeteId);
	        return ResponseEntity.ok(result);
	    }
	    
	    
	    @PostMapping("/exec/{id}")
	    public ResponseEntity<List<Map<String, Object>>> fetchTableData(@PathVariable Long id)
	    {
	    	List<Map<String, Object>> result = requeteService.execReqFromDb(id);
     
	        return ResponseEntity.ok(result);
	    }

	    @PostMapping("/{scriptId}/execute")
	    public ResponseEntity<List<List<Map<String, Object>>>> executeScript(@PathVariable Long scriptId) {
	        try {
	            List<List<Map<String, Object>>> results = requeteService.executeScriptById(scriptId);
	            return ResponseEntity.ok(results);
	        } catch (RuntimeException e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	        }
	    }
	    
	    @PostMapping("/suggestions")
	    public ResponseEntity<?> getSuggestions(@RequestBody Map<String, Object> body) {
	        SuggestionResponseDto response = requeteService.fetchSuggestions(body);
	        return ResponseEntity.ok(response);
	    }

	    
	    
	    
	    
	    
}
