package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class PfeBackApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
        System.setProperty("url", dotenv.get("dburl"));
        System.setProperty("username", dotenv.get("dbuser"));
        System.setProperty("password", dotenv.get("dbpassword"));
        System.setProperty("port", dotenv.get("dbport"));
        
		SpringApplication.run(PfeBackApplication.class, args);
	}

}
