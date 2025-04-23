package com.example.demo.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.example.demo.rapport.Rapport;
import com.example.demo.security.JwtService;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
   	private JwtService jwtService;

    
   

    
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
    
    

    @GetMapping("/getmail")
    public ResponseEntity<List<User>> getUsersByMail(@RequestParam String mail) {
        List<User> users = userService.findUsersByMail(mail);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}/rapports")
    public List<Rapport> getUserRapports(@PathVariable Long id) {
        return userService.getUserRapports(id);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User userReq) {
        User user = userService.loginUser(userReq.getMail(), userReq.getPassword());
        if (user != null) {
        	String token = jwtService.generateToken(user); 
       
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
    }
    
}