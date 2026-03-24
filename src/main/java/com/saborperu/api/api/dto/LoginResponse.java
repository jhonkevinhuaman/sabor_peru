package com.saborperu.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long accessTokenExpiresAt;
    private String tipo;
    private Long usuarioId;
    private String correo;
    private String nombre;
    private Boolean esAdmin;
}
