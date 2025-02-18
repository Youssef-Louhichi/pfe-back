package com.example.demo.connexions;

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
@RequestMapping("/api/connexion")
public class ConnexionController {
	
	@Autowired
    private ConnexionService connexionService;
	
	
	@PostMapping
    public Connexion createConnexion(@RequestBody Connexion connexion) {
        return connexionService.createConnexion(connexion);
    }

    
    @GetMapping
    public List<Connexion> getAllConnexions() {
        return connexionService.getAllConnexions();
    }

    
    @GetMapping("/{id}")
    public Optional<Connexion> getConnexionById(@PathVariable Long id) {
        return connexionService.getConnexionById(id);
    }

    
    @PutMapping("/{id}")
    public Connexion updateConnexion(@PathVariable Long id, @RequestBody Connexion updatedConnexion) {
        return connexionService.updateConnexion(id, updatedConnexion);
    }

    
    @DeleteMapping("/{id}")
    public void deleteConnexion(@PathVariable Long id) {
        connexionService.deleteConnexion(id);
    }
    

}
