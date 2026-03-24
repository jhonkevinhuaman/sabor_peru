package com.saborperu.api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Entidad de Usuario.
 * Representa un usuario del sistema con roles de ADMIN o USER.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(name = "correo", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "contraseña_hash", nullable = false)
    private String passwordHash;

    @Column(name = "nombre", nullable = false, length = 100)
    private String firstName;

    @Column(name = "apellido", length = 100)
    private String lastName;

    @Column(name = "pais", length = 100)
    private String country;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVO";

    @Column(name = "es_admin", nullable = false)
    @Builder.Default
    private Boolean isAdmin = false;

    /**
     * Establece la contraseña del usuario hasheándola con BCrypt.
     * 
     * @param rawPassword contraseña en texto plano
     */
    public void setPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.passwordHash = encoder.encode(rawPassword);
    }

    /**
     * Verifica si la contraseña proporcionada coincide con el hash almacenado.
     * 
     * @param rawPassword contraseña en texto plano a verificar
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public boolean checkPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, this.passwordHash);
    }
}
