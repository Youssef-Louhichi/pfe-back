package com.example.demo.rapport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rapports")
public class RapportController {

    @Autowired
    private RapportService rapportService;

    
    @GetMapping
    public ResponseEntity<List<Rapport>> getAllRapports() {
        List<Rapport> rapports = rapportService.getAllRapports();
        return new ResponseEntity<>(rapports, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rapport> getRapportById(@PathVariable Long id) {
        Optional<Rapport> rapport = rapportService.getRapportById(id);
        return rapport.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    
    @PostMapping
    public ResponseEntity<Rapport> createRapport(@RequestBody Rapport rapport) {
        Rapport savedRapport = rapportService.saveRapport(rapport);
        return new ResponseEntity<>(savedRapport, HttpStatus.CREATED);
    }

    
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRapport(@PathVariable Long id) {
        try {
            rapportService.deleteRapport(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}