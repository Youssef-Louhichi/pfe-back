package com.example.demo.creator;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/creators")
public class CreatorController {

    @Autowired
    private CreatorService creatorService;

    @GetMapping
    public List<Creator> getAllCreators() {
        return creatorService.getAllCreators();
    }

    @GetMapping("/{id}")
    public Optional<Creator> getCreatorById(@PathVariable Long id) {
        return creatorService.getCreatorById(id);
    }

    @PostMapping
    public Creator createCreator(@RequestBody Creator creator) {
        return creatorService.createCreator(creator);
    }

    @PutMapping("/{id}")
    public Creator updateCreator(@PathVariable Long id, @RequestBody Creator updatedCreator) {
        return creatorService.updateCreator(id, updatedCreator);
    }

    @DeleteMapping("/{id}")
    public void deleteCreator(@PathVariable Long id) {
        creatorService.deleteCreator(id);
    }
}
