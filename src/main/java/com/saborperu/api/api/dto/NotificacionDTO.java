package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDTO {
    private Long id;
    private String tipo;
    private String contenido;
    private Boolean leida;
    private Long usuarioId;
    private Long recetaId;
    private String fechaCreacion;
}
