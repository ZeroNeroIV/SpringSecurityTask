package com.zerowhisper.secondtask.service;

import com.zerowhisper.secondtask.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtility {

    // Time
    @Value("${security.jwt.access-token-expiration-time}")
    private long accessTokenValidity; // 15 min in milliseconds

    @Value("${security.jwt.refresh-token-expiration-time}")
    private long refreshTokenValidity; // 7 days in milliseconds

    @Value("${security.jwt.access-secret-key}")
    private String accessSecretKeyString;

    @Value("${security.jwt.refresh-secret-key}")
    private String refreshSecretKeyString;

    public String getToken(String token) {
        return token.replace("Bearer", "");
    }

    // For Access Token
    public String generateAccessToken(UserAccount userAccount) {
        return Jwts.builder()
                .setSubject(userAccount.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(getSigningKeyForAccessToken())
                .setId(userAccount.getId().toString())
                .compact();
    }

    // For Refresh Token
    public String generateRefreshToken(UserAccount userAccount) {
        return Jwts.builder()
                .setSubject(userAccount.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(getSigningKeyForRefreshToken())
                .setId(userAccount.getId().toString())
                .compact();
    }

    public List<String> extractAuthoritiesFromAccessToken(String token) {
        return Arrays
                .stream(extractClaimsFromAccessToken(token)
                        .get("authorities",
                                String[].class))
                .toList();
    }

    public Long extractUserIdFromAccessToken(String jwtToken) {
        return Long.valueOf(extractClaimsFromAccessToken(jwtToken).getId());
    }

    //Extract UserName from Token
    public String extractUsernameFromAccessToken(String token) {
        return extractClaimsFromAccessToken(token).getSubject();
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaimsFromRefreshToken(token).getSubject();
    }

    // Check if accessToken is Valid
    public boolean isAccessTokenValid(String token, String username) {
        final String tokenUsername = extractUsernameFromAccessToken(token);
        return (username.equals(tokenUsername) && !isAccessTokenExpired(token));
    }

    // Check if refreshToken is Valid
    public boolean isRefreshTokenValid(String token, String username) {
        final String tokenUsername = extractUsernameFromRefreshToken(token);
        return (username.equals(tokenUsername) && !isRefreshTokenExpired(token));
    }

    // Check if accessToken is Expired
    public boolean isAccessTokenExpired(String token) {
        return extractClaimsFromAccessToken(token).getExpiration().before(new Date());
    }

    // Check if refreshToken is Expired
    public boolean isRefreshTokenExpired(String refreshToken) {
        return extractClaimsFromRefreshToken(refreshToken).getExpiration().before(new Date());
    }

    // Extract Claims from accessToken
    private Claims extractClaimsFromAccessToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKeyForAccessToken())
                .build()
                .parseClaimsJws(getToken(token))
                .getBody();
    }

    // Extract Claims from refreshToken
    private Claims extractClaimsFromRefreshToken(String refreshToken) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKeyForRefreshToken())
                .build()
                .parseClaimsJws(getToken(refreshToken))
                .getBody();
    }

    private SecretKey getSigningKeyForAccessToken() {
        return Keys
                .hmacShaKeyFor(accessSecretKeyString
                        .getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getSigningKeyForRefreshToken() {
        return Keys
                .hmacShaKeyFor(refreshSecretKeyString
                        .getBytes(StandardCharsets.UTF_8));
    }

    public Long extractUserIdFromRefreshToken(String refreshToken) {
        return Long.valueOf(
                extractClaimsFromRefreshToken(refreshToken)
                        .getId());
    }
}
