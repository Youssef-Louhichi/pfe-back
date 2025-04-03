package com.example.demo.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.connexions.Connexion;
import com.example.demo.database.Database;
import com.example.demo.database.DatabaseRepository;
import com.example.demo.rapport.Rapport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
   

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
            return userRepository.save(user);
        }).orElse(null);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    
   
    
    public List<User> findUsersByMail(String mail) {
    	List<User> userFound = new ArrayList<User>();
    	
        userFound.add( userRepository.findByMail(mail));
        return userFound;
    }
    
    
    public User loginUser(String email, String pw) {
        User u = this.userRepository.findByMail(email);
        if (u != null) {     
            if (pw.equals(u.getPassword())) {
                return u;
            } else {
                System.out.println("Password mismatch for user: " + u.getMail());
            }
        }
        return null;
    }

	public List<Rapport> getUserRapports(Long id) {
		// TODO Auto-generated method stub
		return userRepository.findById(id).get().getRapports();
	}
    
    
}