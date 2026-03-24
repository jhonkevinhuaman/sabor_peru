package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearIngredienteRequest {
    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre del ingrediente debe tener entre 2 y 255 caracteres")
    private String nombre;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    @NotBlank(message = "La unidad es obligatoria")
    @Size(min = 1, max = 50, message = "La unidad debe tener entre 1 y 50 caracteres")
    private String unidad;

    @Size(max = 500, message = "La instrucción de preparación no puede exceder 500 caracteres")
    private String instruccionPreparacion;
}
