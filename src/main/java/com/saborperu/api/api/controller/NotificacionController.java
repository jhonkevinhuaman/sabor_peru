package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.NotificacionDTO;
import com.saborperu.api.api.service.NotificacionService;
import com.saborperu.api.dto.response.ApiResponse;
import com.saborperu.api.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Validated
@Tag(name = "Notificaciones", description = "Gestión de notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar notificaciones del usuario")
    public ResponseEntity<ApiResponse<PageResponse<NotificacionDTO>>> listarNotificaciones(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de listar notificaciones");
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionDTO> notificaciones = notificacionService.listarNotificacionesUsuario(usuarioId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(notificaciones), "Notificaciones listadas correctamente"));
    }

    @GetMapping("/contar-no-leidas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Contar notificaciones no leídas")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> contarNoLeidas(Authentication authentication) {
        log.info("Solicitud de contar notificaciones no leídas");
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        Long noLeidas = notificacionService.contarNoLeidas(usuarioId);
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("noLeidas", noLeidas);
        return ResponseEntity.ok(ApiResponse.success(resultado, "Conteo realizado correctamente"));
    }

    @PutMapping("/{id}/marcar-leida")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<ApiResponse<NotificacionDTO>> marcarComoLeida(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Solicitud de marcar notificación como leída: {}", id);
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        NotificacionDTO notificacion = notificacionService.marcarComoLeida(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success(notificacion, "Notificación marcada como leída"));
    }

    @GetMapping("/admin/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar notificaciones de recetas pendientes (ADMIN)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listarNotificacionesPendientesAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de listar notificaciones pendientes");
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long adminId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionDTO> pendientes = notificacionService.listarPendientesRecetasAdmin(adminId, pageable);
        Long totalPendientes = notificacionService.contarPendientesRecetasAdmin(adminId);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("pendientes", totalPendientes);
        resultado.put("items", PageResponse.from(pendientes));
        return ResponseEntity.ok(ApiResponse.success(resultado, "Notificaciones pendientes listadas"));
    }
}
