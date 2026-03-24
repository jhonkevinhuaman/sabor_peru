package com.saborperu.api.domain.repository;

import com.saborperu.api.domain.entity.Favorito;
import com.saborperu.api.domain.entity.Receta;
import com.saborperu.api.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Favorito.
 * Maneja operaciones CRUD y búsquedas relacionadas con favoritos.
 */
@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    /**
     * Busca un favorito específico de un usuario para una receta.
     */
    Optional<Favorito> findByUsuarioAndReceta(User usuario, Receta receta);

    /**
     * Lista todos los favoritos de un usuario.
     */
    List<Favorito> findByUsuario(User usuario);

    /**
     * Cuenta cuántos usuarios han marcado una receta como favorita.
     */
    long countByReceta(Receta receta);

    /**
     * Verifica si un usuario tiene una receta marcada como favorita.
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Favorito f WHERE f.usuario.id = :usuarioId AND f.receta.id = :recetaId")
    boolean existsByUsuarioIdAndRecetaId(@Param("usuarioId") Long usuarioId, 
                                         @Param("recetaId") Long recetaId);

    /**
     * Elimina un favorito específico.
     */
    void deleteByUsuarioAndReceta(User usuario, Receta receta);
}
