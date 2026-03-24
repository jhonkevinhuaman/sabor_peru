package com.saborperu.api.domain.repository;

import com.saborperu.api.domain.entity.Receta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Page<Receta> findByUsuarioCreadorIdAndEstado(Long usuarioId, String estado, Pageable pageable);
    Page<Receta> findByEstado(String estado, Pageable pageable);
    Page<Receta> findByUsuarioCreadorId(Long usuarioId, Pageable pageable);
    
    // Métodos de búsqueda (P-007)
    Page<Receta> findByTituloContainingIgnoreCaseAndEstado(String titulo, String estado, Pageable pageable);
    
    Page<Receta> findByDescripcionContainingIgnoreCaseAndEstado(String descripcion, String estado, Pageable pageable);
    
    /**
     * Búsqueda con @Query para buscar por título o descripción
     * Filtra solo recetas APROBADAS
     */
    @Query("SELECT r FROM Receta r WHERE " +
            "r.estado = 'APROBADA' AND " +
            "(LOWER(r.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY r.cantidadFavoritos DESC, r.fechaCreacion DESC")
    Page<Receta> buscarPorTexto(@Param("query") String query, Pageable pageable);
}
