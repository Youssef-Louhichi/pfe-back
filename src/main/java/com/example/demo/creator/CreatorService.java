package com.example.demo.creator;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreatorService {

    @Autowired
    private CreatorRepository creatorRepository;

    public List<Creator> getAllCreators() {
        return creatorRepository.findAll();
    }

    public Optional<Creator> getCreatorById(Long id) {
        return creatorRepository.findById(id);
    }

    public Creator createCreator(Creator creator) {
        return creatorRepository.save(creator);
    }

    public Creator updateCreator(Long id, Creator updatedCreator) {
        return creatorRepository.findById(id).map(creator -> {
        	creator.setMail(updatedCreator.getMail());
        	creator.setPassword(updatedCreator.getPassword());
            creator.setConnexions(updatedCreator.getConnexions());
            return creatorRepository.save(creator);
        }).orElse(null);
    }

    public void deleteCreator(Long id) {
        creatorRepository.deleteById(id);
    }
}
