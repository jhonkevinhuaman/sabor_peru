package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.ActualizarPerfilRequest;
import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.api.service.UserService;
import com.saborperu.api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> obtenerMiPerfil(Authentication authentication) {
        log.info("Solicitud de obtener perfil del usuario autenticado");
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        UserDTO usuario = userService.obtenerUsuario(usuarioId);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Perfil obtenido correctamente"));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> actualizarMiPerfil(
            Authentication authentication,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        log.info("Solicitud de actualizar perfil del usuario autenticado");

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        UserDTO usuario = userService.actualizarPerfil(
                usuarioId,
                request.getNombre(),
                request.getApellido(),
                request.getPais());
        return ResponseEntity.ok(ApiResponse.success(usuario, "Perfil actualizado correctamente"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponse<UserDTO>> obtenerUsuario(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Solicitud de obtener usuario: {}", id);

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        boolean esAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!usuarioId.equals(id) && !esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No tiene permisos para ver este usuario", "FORBIDDEN"));
        }

        UserDTO usuario = userService.obtenerUsuario(id);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario obtenido correctamente"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<ApiResponse<List<UserDTO>>> listarUsuarios() {
        log.info("Solicitud de listar usuarios");
        List<UserDTO> usuarios = userService.listarUsuarios();
        return ResponseEntity.ok(ApiResponse.success(usuarios, "Usuarios listados correctamente"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar administradores")
    public ResponseEntity<ApiResponse<List<UserDTO>>> listarAdmins() {
        log.info("Solicitud de listar administradores");
        List<UserDTO> admins = userService.listarAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins, "Administradores listados correctamente"));
    }

    @PutMapping("/{id}/perfil")
    @Operation(summary = "Actualizar perfil del usuario")
    public ResponseEntity<ApiResponse<UserDTO>> actualizarPerfil(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String pais) {
        log.info("Solicitud de actualizar perfil del usuario: {}", id);
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        
        // Solo el propietario o admin puede actualizar
        if (!usuarioId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No tiene permisos para actualizar este usuario", "FORBIDDEN"));
        }

        UserDTO usuario = userService.actualizarPerfil(id, firstName, lastName, pais);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Perfil actualizado correctamente"));
    }

    @PostMapping("/{id}/cambiar-contrasena")
    @Operation(summary = "Cambiar contraseña del usuario")
    public ResponseEntity<ApiResponse<?>> cambiarContrasena(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String contraseñaActual,
            @RequestParam String contraseñaNueva) {
        log.info("Solicitud de cambiar contraseña: {}", id);
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        
        // Solo el propietario puede cambiar su contraseña
        if (!usuarioId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No tiene permisos para cambiar esta contraseña", "FORBIDDEN"));
        }

        userService.cambiarContrasena(id, contraseñaActual, contraseñaNueva);
        return ResponseEntity.ok(ApiResponse.success(null, "Contraseña cambiada exitosamente"));
    }

    @PutMapping("/{id}/cambiar-estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Habilitar/deshabilitar usuario")
    public ResponseEntity<ApiResponse<UserDTO>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        log.info("Solicitud de cambiar estado del usuario: {} a {}", id, estado);
        
        if (!estado.equals("ACTIVO") && !estado.equals("INACTIVO")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Estado inválido. Use ACTIVO o INACTIVO", "VALIDATION_ERROR"));
        }

        UserDTO usuario = userService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Estado actualizado correctamente"));
    }

    @PostMapping("/{id}/promover-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Promover usuario a administrador")
    public ResponseEntity<ApiResponse<UserDTO>> promoverAdmin(@PathVariable Long id) {
        log.info("Solicitud de promover usuario a admin: {}", id);
        UserDTO usuario = userService.promoverAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario promovido a administrador"));
    }

    @PostMapping("/{id}/remover-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover permisos de administrador")
    public ResponseEntity<ApiResponse<UserDTO>> removerAdmin(@PathVariable Long id) {
        log.info("Solicitud de remover admin: {}", id);
        UserDTO usuario = userService.removerAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Permisos de administrador removidos"));
    }

    @GetMapping("/admin/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estadísticas de usuarios")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> obtenerEstadisticas() {
        log.info("Solicitud de estadísticas de usuarios");
        long usuariosActivos = userService.contarUsuariosActivos();
        long totalUsuarios = userService.listarUsuarios().size();
        
        java.util.Map<String, Object> estadisticas = new java.util.HashMap<>();
        estadisticas.put("totalUsuarios", totalUsuarios);
        estadisticas.put("usuariosActivos", usuariosActivos);
        estadisticas.put("usuariosInactivos", totalUsuarios - usuariosActivos);
        
        return ResponseEntity.ok(ApiResponse.success(estadisticas, "Estadísticas obtenidas correctamente"));
    }
}
