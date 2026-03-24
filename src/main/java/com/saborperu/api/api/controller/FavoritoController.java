package com.saborperu.api.api.controller;

import com.saborperu.api.api.dto.FavoritoDTO;
import com.saborperu.api.api.service.FavoritoService;
import com.saborperu.api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de Favoritos.
 * Proporciona endpoints para agregar, remover y listar recetas favoritas de usuarios.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/favoritos")
@RequiredArgsConstructor
@Tag(name = "Favoritos", description = "Operaciones de gestión de recetas favoritas")
public class FavoritoController {

    private final FavoritoService favoritoService;

    /**
     * Agrega una receta a los favoritos del usuario actual.
     * POST /api/v1/favoritos/{recetaId}
     */
    @PostMapping("/{recetaId}")
    @Operation(summary = "Agregar receta a favoritos",
               description = "Marca una receta como favorita para el usuario autenticado")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Favorito agregado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Receta ya está en favoritos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<ApiResponse<FavoritoDTO>> agregarFavorito(
            Authentication authentication,
            @PathVariable Long recetaId) {
        
        Long usuarioId = extractUserId(authentication);
        log.info("Solicitud de agregar receta {} a favoritos del usuario {}", recetaId, usuarioId);
        
        FavoritoDTO favorito = favoritoService.agregarFavorito(usuarioId, recetaId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(favorito, "Receta agregada a favoritos"));
    }

    /**
     * Remueve una receta de los favoritos del usuario actual.
     * DELETE /api/v1/favoritos/{recetaId}
     */
    @DeleteMapping("/{recetaId}")
    @Operation(summary = "Remover receta de favoritos",
               description = "Desmarca una receta como favorita para el usuario autenticado")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Favorito removido exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Favorito no encontrado")
    })
    public ResponseEntity<Void> removeFavorito(
            Authentication authentication,
            @PathVariable Long recetaId) {
        
        Long usuarioId = extractUserId(authentication);
        log.info("Solicitud de remover receta {} de favoritos del usuario {}", recetaId, usuarioId);
        
        favoritoService.removeFavorito(usuarioId, recetaId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos los favoritos del usuario actual.
     * GET /api/v1/favoritos
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar mis favoritos",
               description = "Obtiene la lista de recetas marcadas como favoritas por el usuario autenticado")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de favoritos"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<ApiResponse<List<FavoritoDTO>>> obtenerMisFavoritos(
            Authentication authentication) {
        
        Long usuarioId = extractUserId(authentication);
        log.info("Solicitud de listar favoritos del usuario {}", usuarioId);
        
        List<FavoritoDTO> favoritos = favoritoService.obtenerFavoritos(usuarioId);
        
        return ResponseEntity.ok(
                ApiResponse.success(favoritos, "Favoritos listados correctamente"));
    }

    /**
     * Verifica si una receta es favorita del usuario actual.
     * GET /api/v1/favoritos/{recetaId}/verificar
     */
    @GetMapping("/{recetaId}/verificar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verificar si una receta es favorita",
               description = "Comprueba si una receta está marcada como favorita por el usuario autenticado")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verificación completada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<ApiResponse<Boolean>> verificarFavorito(
            Authentication authentication,
            @PathVariable Long recetaId) {
        
        Long usuarioId = extractUserId(authentication);
        log.info("Verificando si receta {} es favorita del usuario {}", recetaId, usuarioId);
        
        boolean esFavorito = favoritoService.esFavorito(usuarioId, recetaId);
        
        return ResponseEntity.ok(
                ApiResponse.success(esFavorito, "Verificación realizada"));
    }

    /**
     * Extrae el ID del usuario desde el token de autenticación.
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        
        // El usuario está autenticado, su nombre de principal es el userId
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
    }
}
