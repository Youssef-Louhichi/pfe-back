package com.example.demo.requete;

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




@RestController
@RequestMapping("api/requete")
public class RequeteController {

	
	
	
	@Autowired
	private RequeteService requeteService ;
	
	
	 @PostMapping
	    public Requete createUser(@RequestBody Requete req) {
	        return requeteService.createReq(req);
	    }
	 
	 
	 
	 
	    @GetMapping
	    public List<Requete> getAllUsers() {
	        return requeteService.getAllReq();
	    }

	    
	    @GetMapping("/{id}")
	    public Optional<Requete> getUserById(@PathVariable Long id) {
	        return requeteService.getReqById(id);
	    }

	    
	    @PutMapping("/{id}")
	    public Requete updateUser(@PathVariable Long id, @RequestBody Requete updatedreq) {
	        return requeteService.updateReq(id, updatedreq);
	    }

	    
	    @DeleteMapping("/{id}")
	    public void deleteUser(@PathVariable Long id) {
	    	requeteService.deleteReq(id);
	    }
}
