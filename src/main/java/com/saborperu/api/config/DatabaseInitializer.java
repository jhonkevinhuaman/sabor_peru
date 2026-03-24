package com.saborperu.api.config;

import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        log.info("Inicializando datos de la base de datos...");
        
        createAdminUser();
        createNormalUser();
        
        log.info("Inicialización de base de datos completada");
    }

    private void createAdminUser() {
        String adminEmail = "admin@saborperu.com";
        
        User admin = userRepository.findByEmail(adminEmail)
                .orElse(new User());
        
        if (admin.getId() != null) {
            log.debug("Usuario admin ya existe: {}", adminEmail);
        }
        
        admin.setEmail(adminEmail);
        admin.setPassword("Admin123!");
        admin.setFirstName("Admin");
        admin.setLastName("SaborPerú");
        admin.setCountry("Perú");
        admin.setStatus("ACTIVO");
        admin.setIsAdmin(true);
        admin.setBio("Administrador del sistema SaborPerú");
        
        User guardado = userRepository.save(admin);
        log.info("Usuario admin creado/actualizado: ID={}, Email={}", guardado.getId(), guardado.getEmail());
    }

    private void createNormalUser() {
        String userEmail = "usuario@saborperu.com";
        
        User usuario = userRepository.findByEmail(userEmail)
                .orElse(new User());
        
        if (usuario.getId() != null) {
            log.debug("Usuario normal ya existe: {}", userEmail);
        }
        
        usuario.setEmail(userEmail);
        usuario.setPassword("Usuario123!");
        usuario.setFirstName("Usuario");
        usuario.setLastName("Normal");
        usuario.setCountry("Perú");
        usuario.setStatus("ACTIVO");
        usuario.setIsAdmin(false);
        usuario.setBio("Usuario normal del sistema");
        
        User guardado = userRepository.save(usuario);
        log.info("Usuario normal creado/actualizado: ID={}, Email={}", guardado.getId(), guardado.getEmail());
    }
}
