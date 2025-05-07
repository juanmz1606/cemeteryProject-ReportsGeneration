package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.DocumentoDTO;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel.TipoDocumento;
import com.cemeteryProject.ReportsGeneration.repositories.IDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentoServiceTest {

    @Mock
    private IDocumentoRepository documentoRepository;

    @InjectMocks
    private DocumentoService documentoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
    }

    @Test
    void toDTO_DeberiaConvertirModeloEnDTOCorrectamente() {
        // Arrange
        DocumentoModel model = new DocumentoModel();
        model.setId("123");
        model.setNombre("Reporte Prueba");
        model.setFechaGeneracion(LocalDateTime.now());
        model.setTipo(TipoDocumento.REPORTE);
        model.setUsuarioId("usuario123");

        // Act
        DocumentoDTO dto = documentoService.toDTO(model);

        // Assert
        assertNotNull(dto);
        assertEquals("123", dto.getId());
        assertEquals("Reporte Prueba", dto.getNombre());
        assertEquals(model.getFechaGeneracion(), dto.getFechaGeneracion());
        assertEquals(TipoDocumento.REPORTE, dto.getTipo());
        assertEquals("usuario123", dto.getUsuarioId());
    }

    @Test
    void toModel_DeberiaConvertirDTOenModeloCorrectamente() {
        // Arrange
        DocumentoDTO dto = new DocumentoDTO();
        dto.setNombre("Documento DTO");
        dto.setFechaGeneracion(LocalDateTime.of(2025, 5, 6, 10, 30));
        dto.setTipo(TipoDocumento.REPORTE);
        dto.setUsuarioId("user456");

        // Act
        DocumentoModel model = documentoService.toModel(dto);

        // Assert
        assertNotNull(model);
        assertNull(model.getId()); // No se setea desde el DTO
        assertEquals("Documento DTO", model.getNombre());
        assertEquals(LocalDateTime.of(2025, 5, 6, 10, 30), model.getFechaGeneracion());
        assertEquals(TipoDocumento.REPORTE, model.getTipo());
        assertEquals("user456", model.getUsuarioId());
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaDeDTOs() {
        // Arrange
        DocumentoModel model1 = new DocumentoModel();
        model1.setId("1");
        model1.setNombre("Reporte 1");
        model1.setFechaGeneracion(LocalDateTime.now());
        model1.setTipo(TipoDocumento.REPORTE);
        model1.setUsuarioId("user1");

        DocumentoModel model2 = new DocumentoModel();
        model2.setId("2");
        model2.setNombre("Reporte 2");
        model2.setFechaGeneracion(LocalDateTime.now());
        model2.setTipo(TipoDocumento.REPORTE);
        model2.setUsuarioId("user2");

        List<DocumentoModel> modelos = Arrays.asList(model1, model2);
        when(documentoRepository.findAll()).thenReturn(modelos);

        // Act
        List<DocumentoDTO> dtos = documentoService.obtenerTodos();

        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Reporte 1", dtos.get(0).getNombre());
        assertEquals("Reporte 2", dtos.get(1).getNombre());
        verify(documentoRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_DocumentoExistente_DeberiaRetornarDTO() {
        // Arrange
        String id = "123";
        DocumentoModel model = new DocumentoModel();
        model.setId(id);
        model.setNombre("Reporte Prueba");
        model.setFechaGeneracion(LocalDateTime.now());
        model.setTipo(TipoDocumento.REPORTE);
        model.setUsuarioId("user123");

        when(documentoRepository.findById(id)).thenReturn(Optional.of(model));

        // Act
        DocumentoDTO dto = documentoService.obtenerPorId(id);

        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("Reporte Prueba", dto.getNombre());
        assertEquals(TipoDocumento.REPORTE, dto.getTipo());
        verify(documentoRepository, times(1)).findById(id);
    }

    @Test
    void obtenerPorId_DocumentoNoExistente_DeberiaRetornarNull() {
        // Arrange
        String id = "999";
        when(documentoRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        DocumentoDTO dto = documentoService.obtenerPorId(id);

        // Assert
        assertNull(dto);
        verify(documentoRepository, times(1)).findById(id);
    }

    @Test
    void crearDocumento_DTOValido_DeberiaGuardarYRetornarDTO() {
        // Arrange
        DocumentoDTO dto = new DocumentoDTO();
        dto.setNombre("Nuevo Documento");
        dto.setFechaGeneracion(LocalDateTime.of(2025, 5, 6, 10, 30));
        dto.setTipo(TipoDocumento.REPORTE);
        dto.setUsuarioId("user456");

        DocumentoModel model = new DocumentoModel();
        model.setNombre(dto.getNombre());
        model.setFechaGeneracion(dto.getFechaGeneracion());
        model.setTipo(dto.getTipo());
        model.setUsuarioId(dto.getUsuarioId());

        DocumentoModel savedModel = new DocumentoModel();
        savedModel.setId("123");
        savedModel.setNombre(dto.getNombre());
        savedModel.setFechaGeneracion(dto.getFechaGeneracion());
        savedModel.setTipo(dto.getTipo());
        savedModel.setUsuarioId(dto.getUsuarioId());

        when(documentoRepository.save(any(DocumentoModel.class))).thenReturn(savedModel);

        // Act
        DocumentoDTO result = documentoService.crearDocumento(dto);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("Nuevo Documento", result.getNombre());
        assertEquals(TipoDocumento.REPORTE, result.getTipo());
        verify(documentoRepository, times(1)).save(any(DocumentoModel.class));
    }

    @Test
    void actualizarDocumento_DocumentoExistente_DeberiaActualizarYRetornarDTO() {
        // Arrange
        String id = "123";
        DocumentoDTO dto = new DocumentoDTO();
        dto.setNombre("Documento Actualizado");
        dto.setFechaGeneracion(LocalDateTime.of(2025, 5, 6, 10, 30));
        dto.setTipo(TipoDocumento.REPORTE);
        dto.setUsuarioId("user456");

        DocumentoModel updatedModel = new DocumentoModel();
        updatedModel.setId(id);
        updatedModel.setNombre(dto.getNombre());
        updatedModel.setFechaGeneracion(dto.getFechaGeneracion());
        updatedModel.setTipo(dto.getTipo());
        updatedModel.setUsuarioId(dto.getUsuarioId());

        when(documentoRepository.existsById(id)).thenReturn(true);
        when(documentoRepository.save(any(DocumentoModel.class))).thenReturn(updatedModel);

        // Act
        DocumentoDTO result = documentoService.actualizarDocumento(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Documento Actualizado", result.getNombre());
        assertEquals(TipoDocumento.REPORTE, result.getTipo());
        verify(documentoRepository, times(1)).existsById(id);
        verify(documentoRepository, times(1)).save(any(DocumentoModel.class));
    }

    @Test
    void actualizarDocumento_DocumentoNoExistente_DeberiaRetornarNull() {
        // Arrange
        String id = "999";
        DocumentoDTO dto = new DocumentoDTO();
        dto.setNombre("Documento Actualizado");
        dto.setFechaGeneracion(LocalDateTime.of(2025, 5, 6, 10, 30));
        dto.setTipo(TipoDocumento.REPORTE);
        dto.setUsuarioId("user456");

        when(documentoRepository.existsById(id)).thenReturn(false);

        // Act
        DocumentoDTO result = documentoService.actualizarDocumento(id, dto);

        // Assert
        assertNull(result);
        verify(documentoRepository, times(1)).existsById(id);
        verify(documentoRepository, never()).save(any(DocumentoModel.class));
    }

    @Test
    void eliminarPorId_DocumentoExistente_DeberiaRetornarTrueYEliminar() {
        // Arrange
        String id = "123";
        when(documentoRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = documentoService.eliminarPorId(id);

        // Assert
        assertTrue(result);
        verify(documentoRepository, times(1)).existsById(id);
        verify(documentoRepository, times(1)).deleteById(id);
    }

    @Test
    void eliminarPorId_DocumentoNoExistente_DeberiaRetornarFalse() {
        // Arrange
        String id = "999";
        when(documentoRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = documentoService.eliminarPorId(id);

        // Assert
        assertFalse(result);
        verify(documentoRepository, times(1)).existsById(id);
        verify(documentoRepository, never()).deleteById(id);
    }
}