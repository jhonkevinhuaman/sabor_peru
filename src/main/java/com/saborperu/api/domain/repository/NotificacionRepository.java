package com.saborperu.api.domain.repository;

import com.saborperu.api.domain.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    Page<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);
    Long countByUsuarioIdAndLeidaFalse(Long usuarioId);
    Page<Notificacion> findByUsuarioIdAndTipoAndLeidaFalseAndContenidoStartingWithOrderByFechaCreacionDesc(
            Long usuarioId,
            String tipo,
            String contenidoPrefix,
            Pageable pageable);
    Long countByUsuarioIdAndTipoAndLeidaFalseAndContenidoStartingWith(Long usuarioId, String tipo, String contenidoPrefix);
}
