package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.RecetaExternaDTO;
import com.saborperu.api.api.service.RecetaExternaService;
import com.saborperu.api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/external/recetas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Recetas Externas", description = "Consulta de recetas desde proveedores externos")
public class RecetaExternaController {

    private final RecetaExternaService recetaExternaService;

    @GetMapping("/buscar")
    @Operation(summary = "Buscar recetas externas",
               description = "Consulta TheMealDB y devuelve un contrato interno estable para Android")
    public ResponseEntity<ApiResponse<List<RecetaExternaDTO>>> buscarRecetasExternas(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Solicitud de búsqueda externa: query='{}', limit={}", query, limit);

        List<RecetaExternaDTO> resultados = recetaExternaService.buscarRecetas(query, limit);
        String mensaje = "Búsqueda externa completada: " + resultados.size() + " resultados";

        return ResponseEntity.ok(ApiResponse.success(resultados, mensaje));
    }
}
