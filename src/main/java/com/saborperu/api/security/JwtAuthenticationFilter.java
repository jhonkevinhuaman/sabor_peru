package com.saborperu.api.security;

import com.saborperu.api.domain.repository.AuthTokenRepository;
import com.saborperu.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthTokenRepository authTokenRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null && jwtProvider.validateToken(jwt)) {
                String jti = jwtProvider.getJtiFromJWT(jwt);
                
                // Verificar si el token está revocado
                if (authTokenRepository != null && jti != null) {
                    var token = authTokenRepository.findByJti(jti);
                    if (token.isPresent() && token.get().getRevoked()) {
                        log.warn("Token revocado");
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
                
                Long userId = jwtProvider.getUserIdFromJWT(jwt);
                String usuarioId = userId.toString();
                var user = userRepository.findById(userId).orElse(null);
                if (user == null || !"ACTIVO".equals(user.getStatus())) {
                    log.warn("Token válido pero usuario inactivo/no existente: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(Boolean.TRUE.equals(user.getIsAdmin()) ? "ROLE_ADMIN" : "ROLE_USER")
                );
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(usuarioId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Token válido, usuario autenticado: {}", usuarioId);
            }
        } catch (Exception ex) {
            log.debug("No se pudo establecer autenticación de usuario: {}", ex.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
