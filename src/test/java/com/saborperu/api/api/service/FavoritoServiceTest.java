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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FavoritoService.
 */
@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecetaRepository recetaRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private User testUser;
    private Receta testReceta;
    private Favorito testFavorito;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("usuario@test.com")
                .firstName("Juan")
                .lastName("Pérez")
                .passwordHash("hashed_password")
                .isAdmin(false)
                .status("ACTIVO")
                .build();
        testUser.setId(1L);

        testReceta = Receta.builder()
                .titulo("Ceviche Peruano")
                .descripcion("Plato tradicional peruano")
                .estado("APROBADA")
                .cantidadFavoritos(1)
                .build();
        testReceta.setId(1L);

        testFavorito = Favorito.builder()
                .usuario(testUser)
                .receta(testReceta)
                .build();
        testFavorito.setId(1L);
    }

    /**
     * Test: Agregar favorito exitosamente
     */
    @Test
    void testAgregarFavorito_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(favoritoRepository.existsByUsuarioIdAndRecetaId(1L, 1L)).thenReturn(false);
        
        // Mock save para asignar ID al Favorito guardado
        when(favoritoRepository.save(any(Favorito.class))).thenAnswer(invocation -> {
            Favorito favorito = invocation.getArgument(0);
            favorito.setId(1L);
            return favorito;
        });

        // Act
        FavoritoDTO resultado = favoritoService.agregarFavorito(1L, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(1L, resultado.getRecetaId());
        assertEquals("Ceviche Peruano", resultado.getRecetaTitulo());

        verify(userRepository, times(1)).findById(1L);
        verify(recetaRepository, times(1)).findById(1L);  // Solo se llama una vez en agregarFavorito
        verify(favoritoRepository, times(1)).save(any(Favorito.class));
        verify(recetaRepository, times(1)).save(any(Receta.class));
    }

    /**
     * Test: Intentar agregar favorito duplicado
     */
    @Test
    void testAgregarFavorito_AlreadyExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(favoritoRepository.existsByUsuarioIdAndRecetaId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            favoritoService.agregarFavorito(1L, 1L);
        });

        verify(favoritoRepository, never()).save(any());
    }

    /**
     * Test: Agregar favorito con usuario inexistente
     */
    @Test
    void testAgregarFavorito_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            favoritoService.agregarFavorito(999L, 1L);
        });

        verify(favoritoRepository, never()).save(any());
    }

    /**
     * Test: Agregar favorito con receta inexistente
     */
    @Test
    void testAgregarFavorito_RecetaNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recetaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            favoritoService.agregarFavorito(1L, 999L);
        });

        verify(favoritoRepository, never()).save(any());
    }

    /**
     * Test: Remover favorito exitosamente
     */
    @Test
    void testRemoveFavorito_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(favoritoRepository.findByUsuarioAndReceta(testUser, testReceta))
                .thenReturn(Optional.of(testFavorito));

        // Act
        favoritoService.removeFavorito(1L, 1L);

        // Assert
        verify(favoritoRepository, times(1)).delete(testFavorito);
        verify(recetaRepository, times(1)).save(any(Receta.class));
    }

    /**
     * Test: Remover favorito que no existe
     */
    @Test
    void testRemoveFavorito_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(favoritoRepository.findByUsuarioAndReceta(testUser, testReceta))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            favoritoService.removeFavorito(1L, 1L);
        });

        verify(favoritoRepository, never()).delete(any());
    }

    /**
     * Test: Obtener favoritos del usuario
     */
    @Test
    void testObtenerFavoritos_Success() {
        // Arrange
        Receta receta2 = Receta.builder()
                .titulo("Lomo Saltado")
                .build();
        receta2.setId(2L);
        
        Favorito favorito2 = Favorito.builder()
                .usuario(testUser)
                .receta(receta2)
                .build();
        favorito2.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoritoRepository.findByUsuario(testUser))
                .thenReturn(Arrays.asList(testFavorito, favorito2));

        // Act
        List<FavoritoDTO> resultado = favoritoService.obtenerFavoritos(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Ceviche Peruano", resultado.get(0).getRecetaTitulo());
        assertEquals("Lomo Saltado", resultado.get(1).getRecetaTitulo());
        verify(favoritoRepository, times(1)).findByUsuario(testUser);
    }

    /**
     * Test: Obtener favoritos de usuario sin favoritos
     */
    @Test
    void testObtenerFavoritos_Empty() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoritoRepository.findByUsuario(testUser)).thenReturn(Arrays.asList());

        // Act
        List<FavoritoDTO> resultado = favoritoService.obtenerFavoritos(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    /**
     * Test: Verificar si una receta es favorita
     */
    @Test
    void testEsFavorito_True() {
        // Arrange
        when(favoritoRepository.existsByUsuarioIdAndRecetaId(1L, 1L)).thenReturn(true);

        // Act
        boolean resultado = favoritoService.esFavorito(1L, 1L);

        // Assert
        assertTrue(resultado);
    }

    /**
     * Test: Verificar si una receta NO es favorita
     */
    @Test
    void testEsFavorito_False() {
        // Arrange
        when(favoritoRepository.existsByUsuarioIdAndRecetaId(1L, 2L)).thenReturn(false);

        // Act
        boolean resultado = favoritoService.esFavorito(1L, 2L);

        // Assert
        assertFalse(resultado);
    }

    /**
     * Test: Obtener contador de favoritos de una receta
     */
    @Test
    void testObtenerContadorFavoritos_Success() {
        // Arrange
        testReceta.setCantidadFavoritos(5);
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(favoritoRepository.countByReceta(testReceta)).thenReturn(5L);

        // Act
        long resultado = favoritoService.obtenerContadorFavoritos(1L);

        // Assert
        assertEquals(5L, resultado);
        verify(favoritoRepository, times(1)).countByReceta(testReceta);
    }

    /**
     * Test: Obtener contador de receta con 0 favoritos
     */
    @Test
    void testObtenerContadorFavoritos_Zero() {
        // Arrange
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(testReceta));
        when(favoritoRepository.countByReceta(testReceta)).thenReturn(0L);

        // Act
        long resultado = favoritoService.obtenerContadorFavoritos(1L);

        // Assert
        assertEquals(0L, resultado);
    }
}
