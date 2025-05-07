package com.example.demo.reqscript;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.requete.Requete;





@RestController
@RequestMapping("api/reqScript")
public class ReqScriptController {
	
	
	
	@Autowired
	private ReqScriptService scriptService;
	
	
	 @PostMapping
	    public ReqScript create(@RequestBody ReqScript req) {
	        return scriptService.createReqscript(req);
	    }
	 
	 
	 
	 
	    @GetMapping
	    public List<ReqScript> getAll() {
	        return scriptService.getAllReqscript();
	    }

	    
	    @GetMapping("/{id}")
	    public Optional<ReqScript> getById(@PathVariable Long id) {
	        return scriptService.getReqById(id);
	    }

	    

	    @PutMapping("/{id}")
	    public ReqScript update(@PathVariable Long id, @RequestBody ReqScript updatedreq) {
	        return scriptService.updateReqscript(id, updatedreq);
	    }

	    
	    @DeleteMapping("/{id}")
	    public void delete(@PathVariable Long id) {
	    	scriptService.deleteScript(id);
	    }
	    
	    @GetMapping("/{userId}/scripts")
	    public List<ReqScript> getScriptsByUser(@PathVariable Long userId) {
	        return scriptService.getScriptsByUser(userId);
	    }
	    
	    

	    @GetMapping("/{id}/requetes")
	    public List<Requete> getReqs(@PathVariable Long id)
	    {
	    	return scriptService.getReqs(id);
	    }
}
