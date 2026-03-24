package com.saborperu.api.config;

import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedAdminUsers() {
        log.info("Verificando usuarios administrativos...");

        // Usuario admin principal
        crearUsuarioAdminSiNoExiste(
                "admin@saborperu.com",
                "Admin123!",
                "Administrador",
                "Principal"
        );

        // Usuario admin secundario (para testing)
        crearUsuarioAdminSiNoExiste(
                "moderador@saborperu.com",
                "Moderador123!",
                "Moderador",
                "SaborPerú"
        );

        log.info("Verificación de usuarios administrativos completada");
    }

    private void crearUsuarioAdminSiNoExiste(String email, String contraseña, 
                                             String nombre, String apellido) {
        try {
            if (userRepository.findByEmail(email).isEmpty()) {
                User adminUser = User.builder()
                        .email(email)
                        .firstName(nombre)
                        .lastName(apellido)
                        .isAdmin(true)
                        .status("ACTIVO")
                        .build();
                
                adminUser.setPassword(contraseña);
                userRepository.save(adminUser);
                log.info("Usuario administrador creado: {}", email);
            } else {
                log.info("Usuario administrador ya existe: {}", email);
            }
        } catch (Exception e) {
            log.warn("Error al crear usuario administrador: {}", email, e);
        }
    }
}
