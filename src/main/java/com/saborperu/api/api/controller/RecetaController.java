package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.CrearRecetaRequest;
import com.saborperu.api.api.dto.RecetaDTO;
import com.saborperu.api.api.service.RecetaSearchService;
import com.saborperu.api.api.service.RecetaService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Recetas", description = "Gestión de recetas")
public class RecetaController {

    private final RecetaService recetaService;
    private final RecetaSearchService recetaSearchService;

    @PostMapping
    @Operation(summary = "Crear receta", description = "Crea una nueva receta (estado PENDIENTE)")
    public ResponseEntity<ApiResponse<RecetaDTO>> crearReceta(
            Authentication authentication,
            @Valid @RequestBody CrearRecetaRequest request) {
        log.info("Solicitud de crear receta");
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        RecetaDTO receta = recetaService.crearReceta(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(receta, "Receta creada exitosamente"));
    }

    @GetMapping("/mis-recetas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar mis recetas")
    public ResponseEntity<ApiResponse<PageResponse<RecetaDTO>>> listarMisRecetas(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de listar recetas del usuario");
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        Long usuarioId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<RecetaDTO> recetas = recetaService.listarRecetasUsuario(usuarioId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(recetas), "Recetas listadas correctamente"));
    }

    @GetMapping("/aprobadas")
    @Operation(summary = "Listar recetas aprobadas")
    public ResponseEntity<ApiResponse<PageResponse<RecetaDTO>>> listarRecetasAprobadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de listar recetas aprobadas");

        Pageable pageable = PageRequest.of(page, size);
        Page<RecetaDTO> recetas = recetaService.listarRecetasAprobadas(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(recetas), "Recetas aprobadas listadas"));
    }

    @GetMapping("/admin/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar recetas pendientes (ADMIN)")
    public ResponseEntity<ApiResponse<PageResponse<RecetaDTO>>> listarRecetasPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de listar recetas pendientes");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RecetaDTO> recetas = recetaService.listarRecetasPendientes(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(recetas), "Recetas pendientes listadas"));
    }

    @PostMapping("/{id}/validar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar o rechazar receta (ADMIN)")
    public ResponseEntity<ApiResponse<RecetaDTO>> validarReceta(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String motivo) {
        log.info("Solicitud de validar receta {} con estado {}", id, estado);
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autorizado", "UNAUTHORIZED"));
        }

        if ("RECHAZADA".equals(estado) && (motivo == null || motivo.isEmpty())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El motivo es requerido para rechazar", "VALIDATION_ERROR"));
        }

        Long adminId = Long.parseLong(authentication.getName());
        RecetaDTO receta = recetaService.validarReceta(id, adminId, estado, motivo);
        return ResponseEntity.ok(ApiResponse.success(receta, "Receta validada correctamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de receta")
    public ResponseEntity<ApiResponse<RecetaDTO>> obtenerReceta(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Solicitud de obtener receta: {}", id);

        Long usuarioId = null;
        boolean esAdmin = false;

        if (authentication != null) {
            usuarioId = Long.parseLong(authentication.getName());
            esAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
        }

        RecetaDTO receta = recetaService.obtenerReceta(id, usuarioId, esAdmin);
        return ResponseEntity.ok(ApiResponse.success(receta, "Receta obtenida correctamente"));
    }

    /**
     * P-007: Search Integration
     * Endpoint de búsqueda de recetas por texto
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar recetas por texto",
               description = "Busca recetas aprobadas por título o descripción")
    public ResponseEntity<ApiResponse<PageResponse<RecetaDTO>>> buscarRecetas(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de búsqueda: query='{}', page={}, size={}", q, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RecetaDTO> resultados = recetaSearchService.buscarRecetas(q, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(PageResponse.from(resultados), 
                "Búsqueda completada: " + resultados.getTotalElements() + " resultados encontrados"));
    }

    /**
     * P-007: Search Integration
     * Búsqueda específica por título
     */
    @GetMapping("/buscar/titulo")
    @Operation(summary = "Buscar recetas por título",
               description = "Busca recetas cuyo título contenga el texto especificado")
    public ResponseEntity<ApiResponse<PageResponse<RecetaDTO>>> buscarPorTitulo(
            @RequestParam String titulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud de búsqueda por título: '{}'", titulo);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RecetaDTO> resultados = recetaSearchService.buscarPorTitulo(titulo, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(PageResponse.from(resultados), 
                "Búsqueda por título completada: " + resultados.getTotalElements() + " resultados"));
    }
}

