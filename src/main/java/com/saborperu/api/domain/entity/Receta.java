package com.saborperu.api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entidad Receta.
 * Representa una receta peruana en el sistema.
 * Estados: PENDIENTE, APROBADA, RECHAZADA
 */
@Entity
@Table(name = "recetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Receta extends BaseEntity {

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion; // en minutos

    @Column(name = "porciones")
    private Integer porciones;

    @Column(name = "nivel_dificultad")
    private String nivelDificultad; // FACIL, MEDIA, DIFICIL

    @Column(name = "estado", length = 30)
    @Builder.Default
    private String estado = "PENDIENTE"; // PENDIENTE, APROBADA, RECHAZADA

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_creador_id", nullable = false)
    private User usuarioCreador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validado_por_id")
    private User validadoPor;

    @Column(name = "cantidad_favoritos")
    @Builder.Default
    private Integer cantidadFavoritos = 0;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    private List<Ingrediente> ingredientes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    private List<Paso> pasos;
}
