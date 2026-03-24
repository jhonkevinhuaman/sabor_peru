package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearRecetaRequest {
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 255, message = "El título debe tener entre 3 y 255 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 3000, message = "La descripción debe tener entre 10 y 3000 caracteres")
    private String descripcion;

    @Min(value = 1, message = "El tiempo de preparación debe ser mayor a 0")
    @Max(value = 1440, message = "El tiempo de preparación no puede exceder 1440 minutos")
    private Integer tiempoPreparacion;

    @Min(value = 1, message = "Las porciones deben ser mayores a 0")
    @Max(value = 100, message = "Las porciones no pueden exceder 100")
    private Integer porciones;

    @NotBlank(message = "El nivel de dificultad es obligatorio")
    private String nivelDificultad;

    @NotEmpty(message = "La receta debe incluir al menos un ingrediente")
    @Valid
    private List<CrearIngredienteRequest> ingredientes;

    @NotEmpty(message = "La receta debe incluir al menos un paso")
    @Valid
    private List<CrearPasoRequest> pasos;
}
