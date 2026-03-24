package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String correo;
    private String nombre;
    private String apellido;
    private String pais;
    private Boolean esAdmin;
    private String estado;
}
