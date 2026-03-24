package com.saborperu.api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad Paso.
 * Representa un paso de preparación en una receta.
 */
@Entity
@Table(name = "pasos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Paso extends BaseEntity {

    @Column(name = "numero_paso", nullable = false)
    private Integer numeroPaso;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "tiempo_estimado")
    private Integer tiempoEstimado; // en minutos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    private Receta receta;
}
