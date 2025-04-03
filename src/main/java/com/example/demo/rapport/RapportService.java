package com.example.demo.rapport;

import java.time.LocalDate;
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

	    public Rapport saveRapport(Rapport rapport) {
	    	if(rapport.getCreatedAt() == null)
	    		rapport.setCreatedAt(LocalDate.now());
	    	else
	    		rapport.setUpdatedAt(LocalDate.now());
	        return RapportRepository.save(rapport);
	    }

	    

	    public void deleteRapport(Long id) {
	        RapportRepository.deleteById(id);
	    }
	
	
}
