package com.saborperu.api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad Notificacion.
 * Almacena notificaciones del sistema para usuarios.
 */
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Notificacion extends BaseEntity {

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "leida", nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receta_id")
    private Receta receta;
}
