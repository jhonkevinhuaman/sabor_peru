package com.saborperu.api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad AuthToken.
 * Almacena tokens JWT para validación y revocación.
 */
@Entity
@Table(name = "auth_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AuthToken extends BaseEntity {

    @Column(name = "token_type")
    private String tokenType; // ACCESS or REFRESH

    @Column(columnDefinition = "TEXT")
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @Column(name = "expira_en")
    private LocalDateTime expiresAt;

    @Column(name = "revocado")
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revocado_en")
    private LocalDateTime revokedAt;
}
