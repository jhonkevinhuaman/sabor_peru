package com.saborperu.api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad Ingrediente.
 * Representa un ingrediente de una receta.
 */
@Entity
@Table(name = "ingredientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Ingrediente extends BaseEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false)
    private String unidad;

    @Column(columnDefinition = "TEXT")
    private String instruccionPreparacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    private Receta receta;
}
