package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.RecetaDTO;
import com.saborperu.api.domain.entity.Receta;
import com.saborperu.api.domain.repository.RecetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Servicio de búsqueda de recetas.
 * Proporciona funcionalidad de búsqueda por texto con validación y logging.
 * 
 * P-007: Search Integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecetaSearchService {

    private final RecetaRepository recetaRepository;

    /**
     * Busca recetas por texto en título o descripción.
     * Solo retorna recetas APROBADAS.
     * 
     * @param query Texto a buscar (se busca con LIKE %query%)
     * @param pageable Información de paginación
     * @return Página de RecetaDTO ordenadas por favoritos y fecha
     */
    public Page<RecetaDTO> buscarRecetas(String query, Pageable pageable) {
        // Validar entrada
        if (query == null || query.trim().isEmpty()) {
            log.warn("Búsqueda vacía o nula recibida");
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String queryTrimmed = query.trim();
        
        // Validar longitud mínima
        if (queryTrimmed.length() < 2) {
            log.info("Búsqueda muy corta (<2 caracteres): {}", queryTrimmed);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.info("Buscando recetas con query: '{}', página: {}, tamaño: {}", 
                 queryTrimmed, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Receta> resultados = recetaRepository.buscarPorTexto(queryTrimmed, pageable);
            
            log.debug("Búsqueda completada: {} resultados encontrados (página {} de {})", 
                      resultados.getNumberOfElements(), 
                      resultados.getNumber() + 1, 
                      resultados.getTotalPages());

            return resultados.map(this::mapToDTO);
        } catch (Exception e) {
            log.error("Error durante búsqueda de recetas: '{}'", queryTrimmed, e);
            throw new RuntimeException("Error al buscar recetas: " + e.getMessage());
        }
    }

    /**
     * Busca recetas por título exacto (case-insensitive).
     * 
     * @param titulo Título a buscar
     * @param pageable Información de paginación
     * @return Página de recetas que contienen el título
     */
    public Page<RecetaDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.info("Buscando recetas por título: '{}'", titulo);
        
        Page<Receta> resultados = recetaRepository.findByTituloContainingIgnoreCaseAndEstado(titulo, "APROBADA", pageable);
        return resultados.map(this::mapToDTO);
    }

    /**
     * Busca recetas por descripción.
     * 
     * @param descripcion Texto de descripción a buscar
     * @param pageable Información de paginación
     * @return Página de recetas que contienen la descripción
     */
    public Page<RecetaDTO> buscarPorDescripcion(String descripcion, Pageable pageable) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        log.info("Buscando recetas por descripción: '{}'", descripcion);
        
        Page<Receta> resultados = recetaRepository.findByDescripcionContainingIgnoreCaseAndEstado(descripcion, "APROBADA", pageable);
        return resultados.map(this::mapToDTO);
    }

    /**
     * Convierte Receta entity a RecetaDTO.
     */
    private RecetaDTO mapToDTO(Receta receta) {
        return RecetaDTO.builder()
                .id(receta.getId())
                .titulo(receta.getTitulo())
                .descripcion(receta.getDescripcion())
                .tiempoPreparacion(receta.getTiempoPreparacion())
                .porciones(receta.getPorciones())
                .nivelDificultad(receta.getNivelDificultad())
                .estado(receta.getEstado())
                .cantidadFavoritos(receta.getCantidadFavoritos())
                .build();
    }
}
