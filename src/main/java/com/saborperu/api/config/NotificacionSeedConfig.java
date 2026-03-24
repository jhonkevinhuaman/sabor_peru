package com.saborperu.api.config;

import com.saborperu.api.api.service.NotificacionService;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.NotificacionRepository;
import com.saborperu.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionSeedConfig {

    private final NotificacionService notificacionService;
    private final UserRepository userRepository;
    private final NotificacionRepository notificacionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedNotificaciones() {
        log.info("Iniciando seed de notificaciones de bienvenida...");

        try {
            // Obtener usuario normal
            User usuarioNormal = userRepository.findByEmail("usuario@saborperu.com")
                    .orElse(null);

            if (usuarioNormal == null) {
                log.warn("Usuario 'usuario@saborperu.com' no encontrado. Nota: DatabaseInitializer debe ejecutarse antes");
                return;
            }

            log.debug("Usuario encontrado: ID={}, Email={}", usuarioNormal.getId(), usuarioNormal.getEmail());

            // Verificar si ya tiene notificaciones
            long totalNotificaciones = notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(
                    usuarioNormal.getId(), 
                    PageRequest.of(0, 1)
            ).getTotalElements();

            if (totalNotificaciones > 0) {
                log.info("Usuario {} ya tiene {} notificaciones, saltando seed", 
                         usuarioNormal.getId(), totalNotificaciones);
                return;
            }

            // Crear notificación de bienvenida
            notificacionService.crearNotificacion(
                    "BIENVENIDA",
                    "¡Bienvenido a SaborPerú! Aquí puedes compartir tus mejores recetas.",
                    usuarioNormal,
                    null
            );
            log.info("✅ Notificación de bienvenida creada para usuario {}", usuarioNormal.getId());

            // Crear notificaciones adicionales
            notificacionService.crearNotificacion(
                    "INFO",
                    "Recuerda validar tu perfil para que otros usuarios te encuentren.",
                    usuarioNormal,
                    null
            );
            log.info("✅ Notificación de info creada para usuario {}", usuarioNormal.getId());

        } catch (Exception e) {
            log.error("❌ Error durante seed de notificaciones", e);
            // No lanzar excepción para no romper startup de la aplicación
        }

        log.info("Seed de notificaciones completado");
    }
}
