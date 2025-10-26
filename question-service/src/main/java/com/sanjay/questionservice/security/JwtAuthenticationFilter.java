package com.sanjay.questionservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        log.debug("Processing request to: {}", request.getRequestURI());
        log.debug("Authorization header present: {}", authHeader != null);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found, continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            log.debug("Extracted JWT token (first 20 chars): {}...", jwt.substring(0, Math.min(20, jwt.length())));

            final Claims claims = extractAllClaims(jwt);
            final String username = claims.getSubject();

            log.debug("Extracted username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Extract role from token
                String roleFromToken = "USER"; // default

                if (claims.get("role") != null) {
                    roleFromToken = claims.get("role").toString();
                    log.debug("Extracted role from token: {}", roleFromToken);
                }

                // Create authority with ROLE_ prefix
                String authorityString = "ROLE_" + roleFromToken;
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(authorityString)
                );

                log.debug("Setting authorities: {}", authorities);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("✅ User '{}' authenticated successfully with role '{}'", username, authorityString);
            } else if (username == null) {
                log.warn("⚠️ Username is null in JWT token");
            } else {
                log.debug("Authentication already exists in context");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("❌ JWT token is expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token expired\"}");
            return;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("❌ JWT signature validation failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid token signature\"}");
            return;
        } catch (Exception e) {
            log.error("❌ JWT authentication error: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}