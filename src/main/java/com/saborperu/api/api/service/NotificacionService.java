package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.NotificacionDTO;
import com.saborperu.api.domain.entity.Notificacion;
import com.saborperu.api.domain.entity.Receta;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.NotificacionRepository;
import com.saborperu.api.exception.ResourceNotFoundException;
import com.saborperu.api.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificacionService {

    private static final String TIPO_SISTEMA = "SISTEMA";
    private static final String PREFIJO_RECETA_PENDIENTE = "Nueva receta pendiente:";

    private final NotificacionRepository notificacionRepository;

    public void crearNotificacion(String tipo, String contenido, User usuario, Receta receta) {
        log.info("Creando notificación tipo {} para usuario {}", tipo, usuario.getId());

        Notificacion notificacion = Notificacion.builder()
                .tipo(tipo)
                .contenido(contenido)
                .usuario(usuario)
                .receta(receta)
                .leida(false)
                .build();

        notificacionRepository.save(notificacion);
        log.info("Notificación creada exitosamente");
    }

    public Page<NotificacionDTO> listarNotificacionesUsuario(Long usuarioId, Pageable pageable) {
        log.info("Listando notificaciones del usuario: {}", usuarioId);
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable)
                .map(this::mapToDTO);
    }

    public Long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    public Page<NotificacionDTO> listarPendientesRecetasAdmin(Long adminId, Pageable pageable) {
        return notificacionRepository
                .findByUsuarioIdAndTipoAndLeidaFalseAndContenidoStartingWithOrderByFechaCreacionDesc(
                        adminId,
                        TIPO_SISTEMA,
                        PREFIJO_RECETA_PENDIENTE,
                        pageable)
                .map(this::mapToDTO);
    }

    public Long contarPendientesRecetasAdmin(Long adminId) {
        return notificacionRepository.countByUsuarioIdAndTipoAndLeidaFalseAndContenidoStartingWith(
                adminId,
                TIPO_SISTEMA,
                PREFIJO_RECETA_PENDIENTE);
    }

    public NotificacionDTO marcarComoLeida(Long notificacionId, Long usuarioId) {
        log.info("Marcando notificación como leída: {}", notificacionId);

        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", notificacionId));

        if (notificacion.getUsuario() == null || !notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene permisos para modificar esta notificación");
        }

        notificacion.setLeida(true);
        Notificacion guardada = notificacionRepository.save(notificacion);

        return mapToDTO(guardada);
    }

    private NotificacionDTO mapToDTO(Notificacion notificacion) {
        return NotificacionDTO.builder()
                .id(notificacion.getId())
                .tipo(notificacion.getTipo())
                .contenido(notificacion.getContenido())
                .leida(notificacion.getLeida())
                .usuarioId(notificacion.getUsuario() != null ? notificacion.getUsuario().getId() : null)
                .recetaId(notificacion.getReceta() != null ? notificacion.getReceta().getId() : null)
                .fechaCreacion(notificacion.getFechaCreacion() != null ? notificacion.getFechaCreacion().toString() : null)
                .build();
    }
}
