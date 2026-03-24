package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.UserRepository;
import com.saborperu.api.exception.ResourceNotFoundException;
import com.saborperu.api.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Obtener usuario por ID
     */
    public UserDTO obtenerUsuario(Long usuarioId) {
        log.info("Obteniendo usuario con ID: {}", usuarioId);
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        return mapUserToDTO(user);
    }

    /**
     * Obtener usuario por email
     */
    public UserDTO obtenerPorEmail(String email) {
        log.info("Obteniendo usuario con email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return mapUserToDTO(user);
    }

    /**
     * Listar todos los usuarios (solo para admins)
     */
    public List<UserDTO> listarUsuarios() {
        log.info("Listando todos los usuarios");
        return userRepository.findAll().stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar únicamente admins
     */
    public List<UserDTO> listarAdmins() {
        log.info("Listando todos los administradores");
        return userRepository.findAll().stream()
                .filter(User::getIsAdmin)
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar perfil de usuario
     */
    public UserDTO actualizarPerfil(Long usuarioId, String firstName, String lastName, String pais) {
        log.info("Actualizando perfil del usuario: {}", usuarioId);
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        
        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }
        if (pais != null && !pais.isBlank()) {
            user.setCountry(pais.trim());
        }
        
        User updated = userRepository.save(user);
        return mapUserToDTO(updated);
    }

    /**
     * Cambiar contraseña
     */
    public void cambiarContrasena(Long usuarioId, String contraseñaActual, String contraseñaNueva) {
        log.info("Cambiando contraseña del usuario: {}", usuarioId);
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        
        // Validar contraseña actual
        if (!user.checkPassword(contraseñaActual)) {
            throw new UnauthorizedException("Contraseña actual incorrecta");
        }
        
        // Establecer nueva contraseña
        user.setPassword(contraseñaNueva);
        userRepository.save(user);
        log.info("Contraseña cambiada exitosamente");
    }

    /**
     * Habilitar/deshabilitar usuario
     */
    public UserDTO cambiarEstado(Long usuarioId, String estado) {
        log.info("Cambiando estado del usuario: {} a {}", usuarioId, estado);
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        user.setStatus(estado);
        User updated = userRepository.save(user);
        return mapUserToDTO(updated);
    }

    /**
     * Promover a admin
     */
    public UserDTO promoverAdmin(Long usuarioId) {
        log.info("Promoviendo usuario a admin: {}", usuarioId);
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        user.setIsAdmin(true);
        User updated = userRepository.save(user);
        return mapUserToDTO(updated);
    }

    /**
     * Remover permisos de admin
     */
    public UserDTO removerAdmin(Long usuarioId) {
        log.info("Removiendo permisos de admin del usuario: {}", usuarioId);
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("User", usuarioId));
        user.setIsAdmin(false);
        User updated = userRepository.save(user);
        return mapUserToDTO(updated);
    }

    /**
     * Contar usuarios activos
     */
    public long contarUsuariosActivos() {
        log.info("Contando usuarios activos");
        return userRepository.findAll().stream()
                .filter(u -> "ACTIVO".equals(u.getStatus()))
                .count();
    }

    /**
     * Mapear User a UserDTO
     */
    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .correo(user.getEmail())
                .nombre(user.getFirstName())
                .apellido(user.getLastName())
            .pais(user.getCountry())
                .esAdmin(user.getIsAdmin())
                .estado(user.getStatus())
                .build();
    }
}
