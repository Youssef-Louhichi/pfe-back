package com.example.demo.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.connexions.Connexion;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    
    @PostMapping("/{userId}/databases/{databaseId}")
    public ResponseEntity<?> addDatabaseToUser(@PathVariable Long userId, @PathVariable Long databaseId) {
        userService.addDatabaseToUser(userId, databaseId);
        return ResponseEntity.ok("Database linked successfully");
    }

    @GetMapping("/{userId}/connexions")
    public ResponseEntity<List<Connexion>> getUserConnexions(@PathVariable Long userId) {
    	List<Connexion> connexions = userService.getUserConnexions(userId);
        return ResponseEntity.ok(connexions);
    }

    
}