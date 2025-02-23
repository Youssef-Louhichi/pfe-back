package com.example.demo.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.connexions.Connexion;
import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired 
    private DatabaseRepository databaseRepository;

    // Create a new user
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Update user
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setMail(updatedUser.getMail());
            user.setPassword(updatedUser.getPassword());
            user.setDatabases(updatedUser.getDatabases());
            return userRepository.save(user);
        }).orElse(null);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    
    public User addDatabaseToUser( Long userId,Long databaseId) {
    	User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Database database = databaseRepository.findById(databaseId).orElseThrow(() -> new RuntimeException("Database not found"));
        if(!user.getDatabases().contains(database))
        	user.getDatabases().add(database);
        return userRepository.save(user);
    }
    
    public List<Connexion> getUserConnexions( Long userId) {
    	 User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    	 List<Connexion> connexions = new ArrayList<>();
    	 for (Database db : user.getDatabases()) {
    	     if (!connexions.contains(db.getConnexion())) {
    	         connexions.add(db.getConnexion());
    	     }
    	 }

         return connexions;
    }
    
    
}