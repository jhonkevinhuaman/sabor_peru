package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaExternaDTO {
    private String proveedor;
    private String idExterno;
    private String titulo;
    private String categoria;
    private String origen;
    private String imagenUrl;
    private String resumen;
}
