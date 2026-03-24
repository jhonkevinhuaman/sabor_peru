package com.saborperu.api.api.service;

import com.saborperu.api.api.dto.CrearRecetaRequest;
import com.saborperu.api.api.dto.RecetaDTO;
import com.saborperu.api.domain.entity.Receta;
import com.saborperu.api.domain.entity.User;
import com.saborperu.api.domain.repository.RecetaRepository;
import com.saborperu.api.domain.repository.UserRepository;
import com.saborperu.api.exception.ResourceNotFoundException;
import com.saborperu.api.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RecetaService.
 */
@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private RecetaService recetaService;

    private User testUser;
    private User testAdmin;
    private Receta testReceta;
    private CrearRecetaRequest crearRecetaRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .email("usuario@test.com")
                .firstName("Juan")
                .lastName("Pérez")
                .passwordHash("hashed_password")
                .isAdmin(false)
                .status("ACTIVO")
                .build();
        testUser.setId(1L);

        // Setup test admin
        testAdmin = User.builder()
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .passwordHash("hashed_password")
                .isAdmin(true)
                .status("ACTIVO")
                .build();
        testAdmin.setId(2L);

        // Setup test receta
        testReceta = Receta.builder()
                .titulo("Ceviche Peruano")
                .descripcion("Plato tradicional peruano")
                .tiempoPreparacion(30)
                .porciones(4)
                .nivelDificultad("MEDIA")
                .estado("PENDIENTE")
                .usuarioCreador(testUser)
                .cantidadFavoritos(0)
                .build();
        testReceta.setId(1L);

        // Setup crear receta request
        crearRecetaRequest = CrearRecetaRequest.builder()
                .titulo("Ceviche Peruano")
                .descripcion("Plato tradicional peruano")
                .tiempoPreparacion(30)
                .porciones(4)
                .nivelDificultad("MEDIA")
                .build();
    }

    /**
     * Test: Crear una receta exitosamente
     */
    @Test
    void testCrearReceta_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recetaRepository.save(any(Receta.class))).thenReturn(testReceta);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testAdmin));

        // Act
        RecetaDTO resultado = recetaService.crearReceta(1L, crearRecetaRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals("Ceviche Peruano", resultado.getTitulo());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("Juan", resultado.getUsuarioCreador().getNombre());
        
        // Verify interactions
        verify(userRepository, times(1)).findById(1L);
        verify(recetaRepository, times(1)).save(any(Receta.class));
        verify(notificacionService, times(1)).crearNotificacion(
            eq("SISTEMA"),
                anyString(),
                eq(testAdmin),
                eq(testReceta)
        );
    }

    /**
     * Test: Intentar crear receta con usuario inexistente
     */
    @Test
    void testCrearReceta_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            recetaService.crearReceta(999L, crearRecetaRequest);
        });
        
        verify(userRepository, times(1)).findById(999L);
        verify(recetaRepository, never()).save(any());
    }

    /**
     * Test: Validar receta como APROBADA
     */
    @Test
    void testValidarReceta_Approve_Success() {
        // Arrange
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));
        when(recetaRepository.save(any(Receta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RecetaDTO resultado = recetaService.validarReceta(1L, 2L, "APROBADA", null);

        // Assert
        assertNotNull(resultado);
        assertEquals("APROBADA", resultado.getEstado());
        verify(recetaRepository, times(1)).findById(1L);
        verify(notificacionService, times(1)).crearNotificacion(
                eq("RECETA_APROBADA"),
                anyString(),
                eq(testUser),
                any()
        );
    }

    /**
     * Test: Validar receta como RECHAZADA
     */
    @Test
    void testValidarReceta_Reject_Success() {
        // Arrange
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));
        when(recetaRepository.save(any(Receta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String motivo = "Ingredientes no claros";
        RecetaDTO resultado = recetaService.validarReceta(1L, 2L, "RECHAZADA", motivo);

        // Assert
        assertNotNull(resultado);
        assertEquals("RECHAZADA", resultado.getEstado());
        assertEquals(motivo, resultado.getMotivoRechazo());
        verify(notificacionService, times(1)).crearNotificacion(
                eq("RECETA_RECHAZADA"),
                anyString(),
                eq(testUser),
                any()
        );
    }

    /**
     * Test: Intentar validar receta sin ser admin
     */
    @Test
    void testValidarReceta_NotAdmin() {
        // Arrange
        User noAdmin = User.builder()
                .email("nonadmin@test.com")
                .isAdmin(false)
                .build();
        noAdmin.setId(3L);

        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(userRepository.findById(3L)).thenReturn(Optional.of(noAdmin));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            recetaService.validarReceta(1L, 3L, "APROBADA", null);
        });
    }

    /**
     * Test: Obtener receta por ID
     */
    @Test
    void testObtenerReceta_Success() {
        // Arrange
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));

        // Act
        RecetaDTO resultado = recetaService.obtenerReceta(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Ceviche Peruano", resultado.getTitulo());
    }

    /**
     * Test: Obtener receta inexistente
     */
    @Test
    void testObtenerReceta_NotFound() {
        // Arrange
        when(recetaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            recetaService.obtenerReceta(999L);
        });
    }

    /**
     * Test: Listar recetas del usuario
     */
    @Test
    void testListarRecetasUsuario_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Receta> recetasPage = new PageImpl<>(Arrays.asList(testReceta));
        when(recetaRepository.findByUsuarioCreadorId(1L, pageable)).thenReturn(recetasPage);

        // Act
        Page<RecetaDTO> resultado = recetaService.listarRecetasUsuario(1L, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Ceviche Peruano", resultado.getContent().get(0).getTitulo());
        verify(recetaRepository, times(1)).findByUsuarioCreadorId(1L, pageable);
    }

    /**
     * Test: Listar recetas pendientes
     */
    @Test
    void testListarRecetasPendientes_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Receta> recetasPage = new PageImpl<>(Arrays.asList(testReceta));
        when(recetaRepository.findByEstado("PENDIENTE", pageable)).thenReturn(recetasPage);

        // Act
        Page<RecetaDTO> resultado = recetaService.listarRecetasPendientes(pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("PENDIENTE", resultado.getContent().get(0).getEstado());
        verify(recetaRepository, times(1)).findByEstado("PENDIENTE", pageable);
    }
}
