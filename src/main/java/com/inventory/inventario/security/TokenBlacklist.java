package com.inventory.inventario.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    // Store tokens with their expiration time
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Add a token to the blacklist
     */
    public void blacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Attempted to blacklist null or empty token");
            return;
        }
        
        try {
            // Extract the expiration date from the token
            Date expiration = jwtUtil.extractExpiration(token);
            
            // If we can't extract expiration, use a default (1 day from now)
            if (expiration == null) {
                expiration = new Date(System.currentTimeMillis() + 86400000); // 24 hours
                System.out.println("Using default expiration for token");
            }
            
            blacklistedTokens.put(token, expiration);
            System.out.println("Token blacklisted. Current blacklist size: " + blacklistedTokens.size());
        } catch (Exception e) {
            // Even if there's an error extracting expiration, still blacklist the token
            // with a default expiration
            Date defaultExpiration = new Date(System.currentTimeMillis() + 86400000); // 24 hours
            blacklistedTokens.put(token, defaultExpiration);
            
            System.out.println("Error processing token for blacklist, using default expiration: " + e.getMessage());
        }
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        boolean blacklisted = blacklistedTokens.containsKey(token);
        System.out.println("Token blacklist check: " + (blacklisted ? "blacklisted" : "valid") + 
                           " (Blacklist size: " + blacklistedTokens.size() + ")");
        return blacklisted;
    }
    
    /**
     * Clean expired tokens from the blacklist - runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredTokens() {
        Date now = new Date();
        int beforeSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
        
        int afterSize = blacklistedTokens.size();
        if (beforeSize != afterSize) {
            System.out.println("Cleaned up " + (beforeSize - afterSize) + " expired tokens from blacklist. " +
                             "Current size: " + afterSize);
        }
    }
}
