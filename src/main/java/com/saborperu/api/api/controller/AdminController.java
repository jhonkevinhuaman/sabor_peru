package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.RecetaDTO;
import com.saborperu.api.api.dto.RechazarRecetaRequest;
import com.saborperu.api.api.service.RecetaService;
import com.saborperu.api.dto.response.ApiResponse;
import com.saborperu.api.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Operaciones administrativas de moderación")
public class AdminController {

    private final RecetaService recetaService;

    @GetMapping("/recetas/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar recetas pendientes")
    public ResponseEntity<ApiResponse<PageResponse<RecetaDTO>>> listarRecetasPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Solicitud admin de recetas pendientes");
        Pageable pageable = PageRequest.of(page, size);
        Page<RecetaDTO> recetas = recetaService.listarRecetasPendientes(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(recetas), "Recetas pendientes listadas"));
    }

    @PostMapping("/recetas/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar receta")
    public ResponseEntity<ApiResponse<RecetaDTO>> aprobarReceta(
            Authentication authentication,
            @PathVariable Long id) {
        Long adminId = Long.parseLong(authentication.getName());
        RecetaDTO receta = recetaService.validarReceta(id, adminId, "APROBADA", null);
        return ResponseEntity.ok(ApiResponse.success(receta, "Receta aprobada correctamente"));
    }

    @PostMapping("/recetas/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar receta")
    public ResponseEntity<ApiResponse<RecetaDTO>> rechazarReceta(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RechazarRecetaRequest request) {
        Long adminId = Long.parseLong(authentication.getName());
        RecetaDTO receta = recetaService.validarReceta(id, adminId, "RECHAZADA", request.getMotivo());
        return ResponseEntity.ok(ApiResponse.success(receta, "Receta rechazada correctamente"));
    }
}
