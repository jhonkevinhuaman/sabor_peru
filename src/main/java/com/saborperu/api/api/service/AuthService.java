package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.LoginRequest;
import com.saborperu.api.api.dto.LoginResponse;
import com.saborperu.api.api.dto.RegistroRequest;
import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.domain.entity.AuthToken;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.AuthTokenRepository;
import com.saborperu.api.domain.repository.UserRepository;
import com.saborperu.api.dto.mapper.UserMapper;
import com.saborperu.api.exception.UnauthorizedException;
import com.saborperu.api.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AuthTokenRepository authTokenRepository;
    private final UserMapper userMapper;

    public UserDTO registrar(RegistroRequest request) {
        log.info("Registrando nuevo usuario con correo: {}", request.getCorreo());

        if (userRepository.findByEmail(request.getCorreo()).isPresent()) {
            log.warn("Correo ya existe: {}", request.getCorreo());
            throw new UnauthorizedException("Correo ya registrado");
        }

        User usuario = userMapper.registroRequestToUser(request);
        usuario.setPassword(request.getContraseña());
        User guardado = userRepository.save(usuario);

        log.info("Usuario registrado exitosamente con ID: {}", guardado.getId());
        return userMapper.userToUserDTO(guardado);
    }

    public LoginResponse iniciarSesion(LoginRequest request) {
        log.info("Intento de login para correo: {}", request.getCorreo());

        User usuario = userRepository.findByEmail(request.getCorreo())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", request.getCorreo());
                    return new UnauthorizedException.InvalidCredentialsException();
                });

        if (!usuario.checkPassword(request.getContraseña())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.getCorreo());
            throw new UnauthorizedException.InvalidCredentialsException();
        }

        if (!usuario.getStatus().equals("ACTIVO")) {
            log.warn("Intento de login con usuario inactivo: {}", request.getCorreo());
            throw new UnauthorizedException("Usuario no activo");
        }

        String accessToken = jwtProvider.generateAccessToken(usuario.getId(), usuario.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(usuario.getId(), usuario.getEmail());

        persistToken(accessToken, "ACCESS", usuario);
        persistToken(refreshToken, "REFRESH", usuario);

        long accessExpiresAt = jwtProvider.getExpirationDate(accessToken).getTime();
        log.info("Tokens generados exitosamente para usuario: {}", usuario.getId());

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessExpiresAt)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .correo(usuario.getEmail())
                .nombre(usuario.getFirstName() + " " + usuario.getLastName())
                .esAdmin(usuario.getIsAdmin())
                .build();
    }

    public LoginResponse refreshToken(String authHeader) {
        log.info("Solicitud de refresh token");
        
        String token = authHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            log.warn("Token inválido en refresh");
            throw new UnauthorizedException.InvalidTokenException();
        }

        String jti = jwtProvider.getJtiFromJWT(token);
        AuthToken authToken = authTokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("Token no encontrado en base de datos");
                    return new UnauthorizedException.InvalidTokenException();
                });

        if (authToken.getRevoked() || jwtProvider.isTokenExpired(token)) {
            log.warn("Token expirado o revocado");
            throw new UnauthorizedException.TokenExpiredException();
        }

        User usuario = authToken.getUsuario();
        if (!"ACTIVO".equals(usuario.getStatus())) {
            log.warn("Refresh token denegado para usuario no activo: {}", usuario.getId());
            throw new UnauthorizedException("Usuario no activo");
        }

        String newAccessToken = jwtProvider.generateAccessToken(usuario.getId(), usuario.getEmail());
        String newRefreshToken = jwtProvider.generateRefreshToken(usuario.getId(), usuario.getEmail());

        // Revocar tokens anteriores
        authToken.setRevoked(true);
        authToken.setRevokedAt(LocalDateTime.now());
        authTokenRepository.save(authToken);

        // Guardar nuevos tokens
        persistToken(newAccessToken, "ACCESS", usuario);
        persistToken(newRefreshToken, "REFRESH", usuario);

        long accessExpiresAt = jwtProvider.getExpirationDate(newAccessToken).getTime();
        log.info("Token refrescado exitosamente para usuario: {}", usuario.getId());

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresAt(accessExpiresAt)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .correo(usuario.getEmail())
                .nombre(usuario.getFirstName() + " " + usuario.getLastName())
                .esAdmin(usuario.getIsAdmin())
                .build();
    }

    public void logout(String authHeader) {
        log.info("Solicitud de logout");
        
        String token = authHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            log.warn("No hay token para logout");
            return;
        }

        String jti = jwtProvider.getJtiFromJWT(token);
        authTokenRepository.findByJti(jti).ifPresent(authToken -> {
            authToken.setRevoked(true);
            authToken.setRevokedAt(LocalDateTime.now());
            authTokenRepository.save(authToken);
            log.info("Token revocado en logout para usuario: {}", authToken.getUsuario().getId());
        });
    }

    private void persistToken(String token, String type, User usuario) {
        String jti = jwtProvider.getJtiFromJWT(token);
        LocalDateTime expiresAt = jwtProvider.getExpirationDate(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        AuthToken authToken = AuthToken.builder()
                .jti(jti)
                .tokenType(type)
                .usuario(usuario)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        authTokenRepository.save(authToken);
    }
}
