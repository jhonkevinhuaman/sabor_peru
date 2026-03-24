package com.saborperu.api.config;

import com.saborperu.api.api.service.RecetaService;
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
public class RecetaSeedConfig {

    private final RecetaService recetaService;
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedRecetas() {
        log.info("Iniciando carga de recetas de ejemplo...");

        // Obtener usuario normal para crear recetas
        var usuarioNormal = userRepository.findByEmail("usuario@saborperu.com")
                .orElse(null);

        if (usuarioNormal == null) {
            log.warn("Usuario 'usuario@saborperu.com' no encontrado, saltando seed de recetas");
            return;
        }

        // Verificar si ya existen recetas
        var recetasExistentes = recetaService.listarRecetasUsuario(usuarioNormal.getId(), 
                org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
        if (!recetasExistentes.isEmpty()) {
            log.info("Recetas de ejemplo ya existen, saltando seed");
            return;
        }

        // Crear receta de ejemplo 1: Ceviche
        crearRecetaEjemplo(
                usuarioNormal.getId(),
                "Ceviche Peruano",
                "Delicioso ceviche tradicional peruano con pescado fresco",
                45,
                4,
            "MEDIA"
        );

        // Crear receta de ejemplo 2: Lomo Saltado
        crearRecetaEjemplo(
                usuarioNormal.getId(),
                "Lomo Saltado",
                "Plato icónico peruana con lomo de res y papas",
                30,
                3,
            "MEDIA"
        );

        // Crear receta de ejemplo 3: Causa Limeña
        crearRecetaEjemplo(
                usuarioNormal.getId(),
                "Causa Limeña",
                "Causa de papas con pollo, pimienta amarilla y limón",
                20,
                2,
                "FACIL"
        );

        log.info("Carga de recetas de ejemplo completada");
    }

    private void crearRecetaEjemplo(Long usuarioId, String titulo, String descripcion, 
                                    Integer tiempoPreparacion, Integer porciones, 
                                    String nivelDificultad) {
        try {
            com.saborperu.api.api.dto.CrearRecetaRequest request = 
                    com.saborperu.api.api.dto.CrearRecetaRequest.builder()
                    .titulo(titulo)
                    .descripcion(descripcion)
                    .tiempoPreparacion(tiempoPreparacion)
                    .porciones(porciones)
                    .nivelDificultad(nivelDificultad)
                    .ingredientes(java.util.List.of(
                        com.saborperu.api.api.dto.CrearIngredienteRequest.builder()
                            .nombre("Sal")
                            .cantidad(1.0)
                            .unidad("cda")
                            .build()
                    ))
                    .pasos(java.util.List.of(
                        com.saborperu.api.api.dto.CrearPasoRequest.builder()
                            .numeroPaso(1)
                            .descripcion("Preparar y mezclar ingredientes")
                            .tiempoEstimado(10)
                            .build()
                    ))
                    .build();
            
            recetaService.crearReceta(usuarioId, request);
            log.info("Receta de ejemplo creada: {}", titulo);
        } catch (Exception e) {
            log.warn("Error al crear receta de ejemplo: {}", titulo, e);
        }
    }
}
