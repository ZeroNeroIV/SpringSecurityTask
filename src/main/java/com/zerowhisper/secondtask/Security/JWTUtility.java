package com.zerowhisper.secondtask.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtility {

    // Time
    private final long accessTokenValidity = 900000; // 15 min in milliseconds
    //    // Static key for HS256 algorithm
//    private final static SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long refreshTokenValidity = 604800000; // 7 days in milliseconds

    @Value("${security.jwt.secret-key}")
    private String secretKeyString;

    // For Access Token
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(getSigningKey())
                .compact();
    }

    // For Refresh Token
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract Claims from Token
    public Claims extractClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }

    // There is no email in the token (subject is username)
//    // Extract Email from Token
//    public String extractEmail(String token) {
//        return extractClaims(token).getSubject();
//    }

    //Extract UserName from Token
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Check if Token is Expired
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Validate Token
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }
}
