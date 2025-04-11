package com.example.demo.analyst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.connexions.Connexion;
import com.example.demo.database.Database;
import com.example.demo.dto.RelationDto;


@RestController
@RequestMapping("api/analysts")
public class AnalystController {
	
	 @Autowired
	  private AnalystService analystService;
	 
	 
	 @GetMapping
	    public List<Analyst> getAllAnalysts() {
	        return analystService.getAllAnalysts();
	    }

	    @GetMapping("/{id}")
	    public Optional<Analyst> getAnalystById(@PathVariable Long id) {
	        return analystService.getAnalystById(id);
	    }

	    @PostMapping
	    public Analyst createAnalyst(@RequestBody Analyst analyst) {
	        return analystService.createAnalyst(analyst);
	    }

	    @PutMapping("/{id}")
	    public Analyst updateAnalyst(@PathVariable Long id, @RequestBody Analyst updatedAnalyst) {
	        return analystService.updateAnalyst(id, updatedAnalyst);
	    }

	    @DeleteMapping("/{id}")
	    public void deleteAnalyst(@PathVariable Long id) {
	        analystService.deleteAnalyst(id);
	    }

	@PostMapping("/{analystId}/relations")
    public ResponseEntity<?> addDatabaseToUser(@PathVariable Long analystId, @RequestBody RelationDto relations) {
		Analyst a = analystService.addRelationsToAnalyst(analystId, relations);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Database linked successfully");
        response.put("analyst", a);

        return ResponseEntity.ok(response);
    }
	
	 @GetMapping("/database/{dbId}")
	    public ResponseEntity<List<Analyst>> getUsersByDatabaseId(@PathVariable Long dbId) {
	        List<Analyst> users = analystService.getUsersByDatabaseId(dbId);
	        return ResponseEntity.ok(users);
	    }

	 @GetMapping("/{analystId}/databases")
	    public ResponseEntity<List<Database>> getUserDatabases(@PathVariable Long analystId) {
	    	List<Database> databases = analystService.getAnalystDatabases(analystId);
	        return ResponseEntity.ok(databases);
	    }
   @GetMapping("/{analystId}/connexions")
    public ResponseEntity<List<Connexion>> getUserConnexions(@PathVariable Long analystId) {
    	List<Connexion> connexions = analystService.getAnalystConnexions(analystId);
        return ResponseEntity.ok(connexions);
    }
   
   @DeleteMapping("/{analystId}/relations")
   public boolean deleteAnalystsRelations(@PathVariable Long analystId) {
   	boolean r = analystService.deleteAnalystsRelations(analystId);
       return r;
   }
	
	
}
