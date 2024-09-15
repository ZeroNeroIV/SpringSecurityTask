package com.zerowhisper.secondtask.filter;

import com.zerowhisper.secondtask.service.JWTUtility;
import com.zerowhisper.secondtask.service.UserDetailServiceCustom;
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

import java.beans.JavaBean;
import java.io.IOException;

@Component
@JavaBean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtility jwtUtility;
    private final UserDetailServiceCustom userDetailServiceCustom;

    @Autowired
    public JwtAuthenticationFilter(
            JWTUtility jwtUtility,
            UserDetailServiceCustom userDetailServiceCustom
    ) {
        this.jwtUtility = jwtUtility;
        this.userDetailServiceCustom = userDetailServiceCustom;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    jakarta.servlet.FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Validate the token
                if (jwtUtility.isAccessTokenExpired(token)) {
                    // Token is expired, no further processing
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract user information from token
                String username = jwtUtility.extractUsernameFromAccessToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user details from the service
                    UserDetails userDetails = userDetailServiceCustom.loadUserByUsername(username);
                    // UserDetails userDetails=(UserDetails) userService.findUserByUsername(username);
                    if (userDetails != null) {
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, null);

                        // Set details 
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) request));

                        // Set the authentication in the security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // Handle exceptions and invalid tokens --> securtiy context
                SecurityContextHolder.clearContext();

            }
        }

        filterChain.doFilter(request, response);
    }


}
