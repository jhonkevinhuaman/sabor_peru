package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.LoginRequest;
import com.saborperu.api.api.dto.LoginResponse;
import com.saborperu.api.api.dto.RegistroRequest;
import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.api.service.AuthService;
import com.saborperu.api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Autenticación", description = "Operaciones de autenticación y autorización de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registrar")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o correo duplicado")
    })
    public ResponseEntity<ApiResponse<UserDTO>> registrar(
            @Valid @RequestBody RegistroRequest registroRequest) {
        log.info("Solicitud de registro: {}", registroRequest.getCorreo());
        
        UserDTO usuarioCreado = authService.registrar(registroRequest);
        log.info("Registro exitoso para usuario: {}", registroRequest.getCorreo());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(usuarioCreado, "Usuario registrado exitosamente"));
    }

    @PostMapping("/iniciar-sesion")
    @Operation(summary ="Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> iniciarSesion(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Solicitud de login: {}", loginRequest.getCorreo());
        
        LoginResponse loginResponse = authService.iniciarSesion(loginRequest);
        log.info("Login exitoso para usuario: {}", loginRequest.getCorreo());
        
        return ResponseEntity.ok(
                ApiResponse.success(loginResponse, "Login exitoso"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refrescar token JWT", description = "Genera un nuevo token usando el refresh token")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refrescado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader(value = "Authorization") String authHeader) {
        log.info("Solicitud de refresh token");
        
        LoginResponse refreshedResponse = authService.refreshToken(authHeader);
        log.info("Token refrescado exitosamente");
        
        return ResponseEntity.ok(
                ApiResponse.success(refreshedResponse, "Token refrescado"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el token JWT actual")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout exitoso"),
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization") String authHeader) {
        log.info("Solicitud de logout");
        
        authService.logout(authHeader);
        log.info("Logout exitoso");
        
        return ResponseEntity.ok(
                ApiResponse.success("Sesión cerrada exitosamente"));
    }
}
