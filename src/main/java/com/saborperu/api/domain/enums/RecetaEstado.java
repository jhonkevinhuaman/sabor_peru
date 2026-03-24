package com.saborperu.api.domain.enums;

/**
 * Estados posibles de una receta en el sistema.
 * Reemplaza los hardcoded strings ("PENDIENTE", "APROBADA", "RECHAZADA")
 */
public enum RecetaEstado {
    PENDIENTE("Pendiente de aprobación"),
    APROBADA("Receta aprobada"),
    RECHAZADA("Receta rechazada");

    private final String descripcion;

    RecetaEstado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Convierte un string a enum, ignorando mayúsculas/minúsculas
     */
    public static RecetaEstado fromString(String estado) {
        if (estado == null || estado.isEmpty()) {
            return PENDIENTE;
        }
        try {
            return RecetaEstado.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDIENTE; // valor por defecto
        }
    }
}
