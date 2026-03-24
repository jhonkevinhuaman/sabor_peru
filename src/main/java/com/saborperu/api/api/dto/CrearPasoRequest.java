package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPasoRequest {
    @NotNull(message = "El número de paso es obligatorio")
    @Min(value = 1, message = "El número de paso debe ser mayor a 0")
    private Integer numeroPaso;

    @NotBlank(message = "La descripción del paso es obligatoria")
    @Size(min = 5, max = 2000, message = "La descripción del paso debe tener entre 5 y 2000 caracteres")
    private String descripcion;

    @Min(value = 0, message = "El tiempo estimado no puede ser negativo")
    @Max(value = 1440, message = "El tiempo estimado no puede exceder 1440 minutos")
    private Integer tiempoEstimado;
}
