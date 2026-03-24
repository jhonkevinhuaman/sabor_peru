package com.saborperu.api.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecetaDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Integer tiempoPreparacion;
    private Integer porciones;
    private String nivelDificultad;
    private String estado;
    private UserDTO usuarioCreador;
    private UserDTO validadoPor;
    private String motivoRechazo;
    private Integer cantidadFavoritos;
    private List<IngredienteDTO> ingredientes;
    private List<PasoDTO> pasos;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class IngredienteDTO {
    private Long id;
    private String nombre;
    private Double cantidad;
    private String unidad;
    private String instruccionPreparacion;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PasoDTO {
    private Long id;
    private Integer numeroPaso;
    private String descripcion;
    private Integer tiempoEstimado;
}
