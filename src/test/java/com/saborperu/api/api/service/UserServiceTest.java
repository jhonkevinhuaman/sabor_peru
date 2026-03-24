package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.UserDTO;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.UserRepository;
import com.saborperu.api.exception.ResourceNotFoundException;
import com.saborperu.api.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Tests unitarios para UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("usuario@test.com")
                .firstName("Juan")
                .lastName("Pérez")
                .passwordHash("$2a$10$hashed_password")
                .isAdmin(false)
                .status("ACTIVO")
                .build();
        testUser.setId(1L);

        testAdmin = User.builder()
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .passwordHash("$2a$10$hashed_password")
                .isAdmin(true)
                .status("ACTIVO")
                .build();
        testAdmin.setId(2L);
    }

    /**
     * Test: Obtener usuario por ID
     */
    @Test
    void testObtenerUsuario_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO resultado = userService.obtenerUsuario(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("usuario@test.com", resultado.getCorreo());
        assertFalse(resultado.getEsAdmin());
    }

    /**
     * Test: Obtener usuario inexistente
     */
    @Test
    void testObtenerUsuario_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.obtenerUsuario(999L);
        });
    }

    /**
     * Test: Obtener usuario por email
     */
    @Test
    void testObtenerPorEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDTO resultado = userService.obtenerPorEmail("usuario@test.com");

        // Assert
        assertNotNull(resultado);
        assertEquals("usuario@test.com", resultado.getCorreo());
        assertEquals("Juan", resultado.getNombre());
    }

    /**
     * Test: Obtener usuario por email inexistente
     */
    @Test
    void testObtenerPorEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.obtenerPorEmail("noexiste@test.com");
        });
    }

    /**
     * Test: Listar todos los usuarios
     */
    @Test
    void testListarUsuarios_Success() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testAdmin));

        // Act
        List<UserDTO> resultado = userService.listarUsuarios();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(userRepository, times(1)).findAll();
    }

    /**
     * Test: Listar admins
     */
    @Test
    void testListarAdmins_Success() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testAdmin));

        // Act
        List<UserDTO> resultado = userService.listarAdmins();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getEsAdmin());
        assertEquals("admin@test.com", resultado.get(0).getCorreo());
    }

    /**
     * Test: Actualizar perfil de usuario
     */
    @Test
    void testActualizarPerfil_Success() {
        // Arrange
        User updatedUser = User.builder()
                .email("usuario@test.com")
                .firstName("Juanito")
                .lastName("García")
            .country("Perú")
                .passwordHash("$2a$10$hashed_password")
                .isAdmin(false)
                .status("ACTIVO")
                .build();
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserDTO resultado = userService.actualizarPerfil(1L, "Juanito", "García", "Perú");

        // Assert
        assertNotNull(resultado);
        assertEquals("Juanito", resultado.getNombre());
        assertEquals("García", resultado.getApellido());
        assertEquals("Perú", resultado.getPais());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test: Actualizar perfil solo nombre
     */
    @Test
    void testActualizarPerfil_OnlyFirstName() {
        // Arrange
        User updatedUser = User.builder()
                .email("usuario@test.com")
                .firstName("Juanito")
                .lastName("Pérez")
            .country("Perú")
                .passwordHash("$2a$10$hashed_password")
                .isAdmin(false)
                .status("ACTIVO")
                .build();
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserDTO resultado = userService.actualizarPerfil(1L, "Juanito", null, null);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juanito", resultado.getNombre());
        assertEquals("Pérez", resultado.getApellido()); // Unchanged
    }

    /**
     * Test: Cambiar contraseña exitosamente
     */
    @Test
    void testCambiarContrasena_Success() {
        // Arrange
        User spyUser = spy(testUser);
        when(spyUser.checkPassword("password123")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(spyUser));
        when(userRepository.save(any(User.class))).thenReturn(spyUser);

        // Act
        userService.cambiarContrasena(1L, "password123", "nuevacontraseña123");

        // Assert
        verify(spyUser, times(1)).checkPassword("password123");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test: Cambiar contraseña con contraseña actual incorrecta
     */
    @Test
    void testCambiarContrasena_CurrentPasswordIncorrect() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            userService.cambiarContrasena(1L, "contraseñaIncorrecta", "nuevacontraseña");
        });
        
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    /**
     * Test: Cambiar contraseña de usuario inexistente
     */
    @Test
    void testCambiarContrasena_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.cambiarContrasena(999L, "password123", "nuevacontraseña");
        });
    }
}
