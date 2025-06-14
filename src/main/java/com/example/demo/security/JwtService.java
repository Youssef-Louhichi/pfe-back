package com.example.demo.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.users.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	private static final String SECRET_KEY = "YzNlYzUyM2NiZDEyZjNkZjVjYzM4YjBkNzRiY2RkZTgwMjg=";


	public String extractUserEmail(String token) {
		
		return extractClaim(token, Claims::getSubject);
	}
	
	public <T> T extractClaim(String token,Function<Claims,T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	public String generateToken(User user) {
		return generateToken(new HashMap<>(),user);
	}
	
	public String generateToken(Map<String,Object> extractClaims, User user) {
		return Jwts
				.builder()
				.setClaims(extractClaims)
				.setSubject(user.getMail())
				.issuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
				.signWith(getSignInKey(),SignatureAlgorithm.HS256)
				.compact();
	}
	
	public boolean isTokenValid(String token,User user) {
		final String email = extractUserEmail(token);
		return (email.equals(user.getMail())) && !isTokenExpired(token);
	}
	
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token,Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts
				.parser()
				.setSigningKey(getSignInKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Key getSignInKey() {
	    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); 
	    return Keys.hmacShaKeyFor(keyBytes); 
	}


}
