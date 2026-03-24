package com.saborperu.api.dto.mapper;

import com.saborperu.api.api.dto.RegistroRequest;
import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.domain.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre User entity y UserDTO
 */
@Component
public class UserMapper {
    
    /**
     * Convierte User entity a UserDTO
     */
    public UserDTO userToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
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
    
    /**
     * Convierte RegistroRequest a User entity
     */
    public User registroRequestToUser(RegistroRequest request) {
        if (request == null) {
            return null;
        }
        
        User user = new User();
        user.setEmail(request.getCorreo());
        user.setFirstName(request.getNombre());
        user.setLastName(request.getApellido());
        user.setCountry(request.getPais());
        user.setStatus("ACTIVO");
        user.setIsAdmin(false);
        // Password se setea en AuthService
        
        return user;
    }
}
