package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.FavoritoDTO;
import com.saborperu.api.domain.entity.Favorito;
import com.saborperu.api.domain.entity.Receta;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.FavoritoRepository;
import com.saborperu.api.domain.repository.RecetaRepository;
import com.saborperu.api.domain.repository.UserRepository;
import com.saborperu.api.exception.BusinessException;
import com.saborperu.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Favoritos.
 * Maneja la lógica de negocio para operaciones de favoritos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UserRepository userRepository;
    private final RecetaRepository recetaRepository;

    /**
     * Agrega una receta a los favoritos de un usuario.
     */
    public FavoritoDTO agregarFavorito(Long usuarioId, Long recetaId) {
        log.info("Agregando receta {} a favoritos de usuario {}", recetaId, usuarioId);
        
        User usuario = userRepository.findById(usuarioId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        
        Receta receta = recetaRepository.findById(recetaId)
                                .orElseThrow(() -> new ResourceNotFoundException("Receta", recetaId));
        
        // Verificar si ya existe
        if (favoritoRepository.existsByUsuarioIdAndRecetaId(usuarioId, recetaId)) {
                        throw new BusinessException("La receta ya está en favoritos", "FAVORITO_DUPLICADO");
        }
        
        Favorito favorito = Favorito.builder()
                .usuario(usuario)
                .receta(receta)
                .build();
        
        favoritoRepository.save(favorito);
        
        // Incrementar contador de favoritos en la receta
        receta.setCantidadFavoritos((receta.getCantidadFavoritos() != null ? 
                                    receta.getCantidadFavoritos() : 0) + 1);
        recetaRepository.save(receta);
        
        log.info("Favorito agregado correctamente");
        return toDTO(favorito);
    }

    /**
     * Remueve una receta de los favoritos de un usuario.
     */
    public void removeFavorito(Long usuarioId, Long recetaId) {
        log.info("Removiendo receta {} de favoritos de usuario {}", recetaId, usuarioId);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", recetaId));
        
        Favorito favorito = favoritoRepository.findByUsuarioAndReceta(usuario, receta)
                .orElseThrow(() -> new ResourceNotFoundException("Favorito", "usuario=" + usuarioId + ",receta=" + recetaId));
        
        favoritoRepository.delete(favorito);
        
        // Decrementar contador de favoritos en la receta
        if (receta.getCantidadFavoritos() != null && receta.getCantidadFavoritos() > 0) {
            receta.setCantidadFavoritos(receta.getCantidadFavoritos() - 1);
            recetaRepository.save(receta);
        }
        
        log.info("Favorito removido correctamente");
    }

    /**
     * Lista todos los favoritos de un usuario.
     */
    @Transactional(readOnly = true)
    public List<FavoritoDTO> obtenerFavoritos(Long usuarioId) {
        log.info("Obteniendo favoritos del usuario {}", usuarioId);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        
        return favoritoRepository.findByUsuario(usuario)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si una receta es favorita de un usuario.
     */
    @Transactional(readOnly = true)
    public boolean esFavorito(Long usuarioId, Long recetaId) {
        return favoritoRepository.existsByUsuarioIdAndRecetaId(usuarioId, recetaId);
    }

    /**
     * Obtiene el contador de favoritos de una receta.
     */
    @Transactional(readOnly = true)
    public long obtenerContadorFavoritos(Long recetaId) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", recetaId));
        
        return favoritoRepository.countByReceta(receta);
    }

    /**
     * Convierte una entidad Favorito a DTO.
     */
    private FavoritoDTO toDTO(Favorito favorito) {
        return FavoritoDTO.builder()
                .id(favorito.getId())
                .usuarioId(favorito.getUsuario().getId())
                .recetaId(favorito.getReceta().getId())
                .recetaTitulo(favorito.getReceta().getTitulo())
                .fechaCreacion(favorito.getFechaCreacion())
                .build();
    }
}
