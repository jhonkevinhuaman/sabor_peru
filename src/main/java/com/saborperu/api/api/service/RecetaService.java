package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.CrearRecetaRequest;
import com.saborperu.api.api.dto.RecetaDTO;
import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.domain.entity.*;
import com.saborperu.api.domain.repository.*;
import com.saborperu.api.exception.ResourceNotFoundException;
import com.saborperu.api.exception.UnauthorizedException;
import com.saborperu.api.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecetaService {

        private static final Set<String> NIVELES_DIFICULTAD_VALIDOS = Set.of("FACIL", "MEDIA", "DIFICIL");
        private static final Set<String> ESTADOS_VALIDACION_VALIDOS = Set.of("APROBADA", "RECHAZADA");

    private final RecetaRepository recetaRepository;
    private final UserRepository userRepository;
    private final NotificacionService notificacionService;

    public RecetaDTO crearReceta(Long usuarioId, CrearRecetaRequest request) {
        log.info("Creando receta para usuario: {}", usuarioId);
        
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));

        String nivelNormalizado = normalizarNivelDificultad(request.getNivelDificultad());

        Receta receta = Receta.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tiempoPreparacion(request.getTiempoPreparacion())
                .porciones(request.getPorciones())
                .nivelDificultad(nivelNormalizado)
                .estado("PENDIENTE")
                .usuarioCreador(usuario)
                .build();

        if (request.getIngredientes() != null) {
            receta.setIngredientes(request.getIngredientes().stream()
                    .map(ing -> Ingrediente.builder()
                            .nombre(ing.getNombre())
                            .cantidad(ing.getCantidad())
                            .unidad(ing.getUnidad())
                            .instruccionPreparacion(ing.getInstruccionPreparacion())
                            .receta(receta)
                            .build())
                    .collect(Collectors.toList()));
        }

        if (request.getPasos() != null) {
            receta.setPasos(request.getPasos().stream()
                    .map(paso -> Paso.builder()
                            .numeroPaso(paso.getNumeroPaso())
                            .descripcion(paso.getDescripcion())
                            .tiempoEstimado(paso.getTiempoEstimado())
                            .receta(receta)
                            .build())
                    .collect(Collectors.toList()));
        }

        Receta guardada = recetaRepository.save(receta);
        log.info("Receta creada exitosamente: {}", guardada.getId());

        // Notificar a admins
        userRepository.findAll().stream()
                .filter(User::getIsAdmin)
                .forEach(admin -> notificacionService.crearNotificacion(
                        "SISTEMA",
                        "Nueva receta pendiente: " + guardada.getTitulo(),
                        admin,
                        guardada
                ));

        return mapToDTO(guardada);
    }

    public Page<RecetaDTO> listarRecetasUsuario(Long usuarioId, Pageable pageable) {
        log.info("Listando recetas del usuario: {}", usuarioId);
        return recetaRepository.findByUsuarioCreadorId(usuarioId, pageable)
                .map(this::mapToDTO);
    }

    public Page<RecetaDTO> listarRecetasPendientes(Pageable pageable) {
        log.info("Listando recetas pendientes");
        return recetaRepository.findByEstado("PENDIENTE", pageable)
                .map(this::mapToDTO);
    }

        public Page<RecetaDTO> listarRecetasAprobadas(Pageable pageable) {
                log.info("Listando recetas aprobadas");
                return recetaRepository.findByEstado("APROBADA", pageable)
                                .map(this::mapToDTO);
        }

    public RecetaDTO validarReceta(Long recetaId, Long adminId, String estado, String motivo) {
        log.info("Validando receta {} con estado {}", recetaId, estado);

                String estadoNormalizado = normalizarEstadoValidacion(estado);

        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", recetaId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        if (!admin.getIsAdmin()) {
            throw new UnauthorizedException("El usuario no tiene permisos de administrador");
        }

                receta.setEstado(estadoNormalizado);
        receta.setValidadoPor(admin);
        
                if ("RECHAZADA".equals(estadoNormalizado)) {
            receta.setMotivoRechazo(motivo);
        }

        Receta guardada = recetaRepository.save(receta);

        // Notificar al creador
        String tipo = "APROBADA".equals(estadoNormalizado) ? "RECETA_APROBADA" : "RECETA_RECHAZADA";
        String contenido = "APROBADA".equals(estadoNormalizado) 
                ? "Tu receta '" + receta.getTitulo() + "' fue aprobada ✅"
                : "Tu receta fue rechazada. Motivo: " + motivo;

        notificacionService.crearNotificacion(tipo, contenido, receta.getUsuarioCreador(), guardada);

                log.info("Receta validada: {}", estadoNormalizado);
        return mapToDTO(guardada);
    }

    public RecetaDTO obtenerReceta(Long id) {
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", id));
        return mapToDTO(receta);
    }

        public RecetaDTO obtenerReceta(Long id, Long usuarioIdSolicitante, boolean esAdmin) {
                Receta receta = recetaRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Receta", id));

                boolean esAprobada = "APROBADA".equals(receta.getEstado());
                boolean esCreador = receta.getUsuarioCreador() != null
                                && receta.getUsuarioCreador().getId().equals(usuarioIdSolicitante);

                if (!esAprobada && !esAdmin && !esCreador) {
                        throw new UnauthorizedException("No tiene permisos para ver esta receta");
                }

                return mapToDTO(receta);
        }

        private String normalizarNivelDificultad(String nivelDificultad) {
                String normalizado = nivelDificultad == null ? "" : nivelDificultad.trim().toUpperCase(Locale.ROOT);
                if (!NIVELES_DIFICULTAD_VALIDOS.contains(normalizado)) {
                        throw new ValidationException("Nivel de dificultad inválido. Use FACIL, MEDIA o DIFICIL")
                                        .addError("nivelDificultad", "Valor inválido: " + nivelDificultad);
                }
                return normalizado;
        }

        private String normalizarEstadoValidacion(String estado) {
                String normalizado = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
                if (!ESTADOS_VALIDACION_VALIDOS.contains(normalizado)) {
                        throw new ValidationException("Estado de validación inválido. Use APROBADA o RECHAZADA")
                                        .addError("estado", "Valor inválido: " + estado);
                }
                return normalizado;
        }

    private RecetaDTO mapToDTO(Receta receta) {
        return RecetaDTO.builder()
                .id(receta.getId())
                .titulo(receta.getTitulo())
                .descripcion(receta.getDescripcion())
                .tiempoPreparacion(receta.getTiempoPreparacion())
                .porciones(receta.getPorciones())
                .nivelDificultad(receta.getNivelDificultad())
                .estado(receta.getEstado())
                .usuarioCreador(mapUserToDTO(receta.getUsuarioCreador()))
                .validadoPor(receta.getValidadoPor() != null ? mapUserToDTO(receta.getValidadoPor()) : null)
                .motivoRechazo(receta.getMotivoRechazo())
                .cantidadFavoritos(receta.getCantidadFavoritos())
                .build();
    }

    private UserDTO mapUserToDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .correo(user.getEmail())
                .nombre(user.getFirstName())
                .apellido(user.getLastName())
                .pais(user.getCountry())
                .esAdmin(user.getIsAdmin())
                .build();
    }
}
