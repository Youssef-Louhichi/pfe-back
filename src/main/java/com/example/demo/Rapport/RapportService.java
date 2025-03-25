package com.example.demo.Rapport;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




@Service
public class RapportService {

	
	
	 @Autowired
	    private RapportRepository RapportRepository;

	    public List<Rapport> getAllRapports() {
	        return RapportRepository.findAll();
	    }

	    public Optional<Rapport> getRapportById(Long id) {
	        return RapportRepository.findById(id);
	    }

	    public Rapport saveRapport(Rapport Rapport) {
	        return RapportRepository.save(Rapport);
	    }

	    public Rapport updateRapport(Long id, Rapport updatedRapport) {
	        return RapportRepository.findById(id)
	                .map(Rapport -> {
	                    Rapport.setTitre(updatedRapport.getTitre());
	                    Rapport.setGraphs(updatedRapport.getGraphs());
	                    Rapport.setSender(updatedRapport.getSender());
	                    Rapport.setCnxrapport(updatedRapport.getCnxrapport());
	                    
	                    return RapportRepository.save(Rapport);
	                }).orElse(null);
	    }

	    public void deleteRapport(Long id) {
	        RapportRepository.deleteById(id);
	    }
	
	
}
