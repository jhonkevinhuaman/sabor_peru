package com.saborperu.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtProvider {

    private String jwtSecret;

    @Value("${app.jwt.secret:}")
    private String configuredJwtSecret;
    
    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;
    
    /**
     * Inicializa el secreto JWT desde configuración.
     * Usa app.jwt.secret (que puede venir de JWT_SECRET por properties) y
     * mantiene fallback aleatorio solo como último recurso en desarrollo.
     */
    @PostConstruct
    public void initSecret() {
        if (configuredJwtSecret != null && !configuredJwtSecret.trim().isEmpty()) {
            this.jwtSecret = configuredJwtSecret;
            return;
        }

        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        this.jwtSecret = Encoders.BASE64.encode(randomBytes);
        log.warn("app.jwt.secret/JWT_SECRET no configurado. Se generó un secreto efímero para esta ejecución (NO USAR EN PRODUCCIÓN)");
    }

    public String generateAccessToken(Long userId, String email) {
        return generateToken(userId, email, jwtExpirationMs, "ACCESS");
    }

    public String generateRefreshToken(Long userId, String email) {
        return generateToken(userId, email, refreshTokenExpirationMs, "REFRESH");
    }

    private String generateToken(Long userId, String email, long expirationTime, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", type)
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("Error extrayendo usuarioId del JWT: {}", e.getMessage());
            return null;
        }
    }

    public String getJtiFromJWT(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getId();
        } catch (Exception e) {
            log.error("Error extrayendo JTI del JWT: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("JWT signature invalida");
        } catch (MalformedJwtException e) {
            log.warn("JWT inválido");
        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT no soportado");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims vacío");
        }
        return false;
    }

    public Date getExpirationDate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (Exception e) {
            log.error("Error extrayendo fecha expiración del JWT: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDate(token);
        return expirationDate != null && expirationDate.before(new Date());
    }
}
