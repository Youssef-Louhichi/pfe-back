package com.example.demo.analyst;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.connexions.Connexion;
import com.example.demo.analyst.Analyst;
import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;

@Service
public class AnalystService {
	
	@Autowired
    private AnalystRepository analystRepository;
    
    @Autowired 
    private DatabaseRepository databaseRepository;
    
    
    public List<Analyst> getAllAnalysts() {
        return analystRepository.findAll();
    }

    public Optional<Analyst> getAnalystById(Long id) {
        return analystRepository.findById(id);
    }

    public Analyst createAnalyst(Analyst analyst) {
        return analystRepository.save(analyst);
    }

    public Analyst updateAnalyst(Long id, Analyst updatedAnalyst) {
        return analystRepository.findById(id).map(analyst -> {
        	analyst.setMail(updatedAnalyst.getMail());
        	analyst.setPassword(updatedAnalyst.getPassword());
            analyst.setDatabases(updatedAnalyst.getDatabases());
            return analystRepository.save(analyst);
        }).orElse(null);
    }

    public void deleteAnalyst(Long id) {
        analystRepository.deleteById(id);
    }
	
	 public Analyst addDatabaseToAnalyst( Long analystId,Long databaseId) {
	Analyst user = analystRepository.findById(analystId).orElseThrow(() -> new RuntimeException("User not found"));
    Database database = databaseRepository.findById(databaseId).orElseThrow(() -> new RuntimeException("Database not found"));
    if(!user.getDatabases().contains(database))
    	user.getDatabases().add(database);
    return analystRepository.save(user);
}

public List<Connexion> getAnalystConnexions( Long analystId) {
	 Analyst analyst = analystRepository.findById(analystId).orElseThrow(() -> new RuntimeException("User not found"));
	 List<Connexion> connexions = new ArrayList<>();
	 for (Database db : analyst.getDatabases()) {
	     if (!connexions.contains(db.getConnexion())) {
	         connexions.add(db.getConnexion());
	     }
	 }

     return connexions;
}

}
