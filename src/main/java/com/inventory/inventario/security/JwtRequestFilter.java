package com.inventory.inventario.security;

import com.inventory.inventario.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Skip filter for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Get authorization header
        final String authorizationHeader = request.getHeader("Authorization");
        
        // Debug logging for all requests
        String requestPath = request.getRequestURI();
        System.out.println("Request Path: " + requestPath);
        System.out.println("Method: " + request.getMethod());
        
        // Special handling for logout endpoint
        if (requestPath.equals("/auth/logout") || requestPath.equals("/api/auth/logout")) {
            System.out.println("Processing logout request. Auth header present: " + (authorizationHeader != null));
            // For logout, we pass through to the controller which will handle the token properly
            chain.doFilter(request, response);
            return;
        }
        
        // Regular token handling for other endpoints
        System.out.println("Authorization Header: " + authorizationHeader);

        String username = null;
        String jwt = null;

        // Extract token if present
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            
            // Check if token is blacklisted
            if (tokenBlacklist.isBlacklisted(jwt)) {
                System.out.println("Token is blacklisted. Access denied.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session expired or logged out");
                return;
            }
            
            try {
                // Extract username from token
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Extracted username: " + username);
            } catch (Exception e) {
                System.out.println("Error extracting username from token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No valid Authorization header found");
        }

        // Authenticate user if token is valid
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("User authenticated: " + username);
                    System.out.println("Authorities: " + userDetails.getAuthorities());
                } else {
                    System.out.println("Token validation failed for user: " + username);
                }
            } catch (Exception e) {
                System.out.println("Error loading user details: " + e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip authentication for public endpoints, but NOT for logout
        String path = request.getRequestURI();
        return (path.startsWith("/auth/") && !path.equals("/auth/logout")) || 
               (path.startsWith("/api/auth/") && !path.equals("/api/auth/logout")) || 
               path.startsWith("/roles/") || 
               path.startsWith("/api/roles/");
    }
}
