package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para Favorito.
 * Representa un favorito en las respuestas de la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoDTO {
    
    private Long id;
    private Long usuarioId;
    private Long recetaId;
    private String recetaTitulo;
    private LocalDateTime fechaCreacion;
}
