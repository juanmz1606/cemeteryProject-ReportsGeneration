package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoCuerpoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoDTO;
import com.cemeteryProject.ReportsGeneration.models.CuerpoInhumadoModel.EstadoCuerpo;
import com.cemeteryProject.ReportsGeneration.models.NichoModel.EstadoNicho;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalDataServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExternalDataService externalDataService;

    private final String backendBaseUrl = "http://mock-backend";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Establecer backendBaseUrl usando ReflectionTestUtils
        ReflectionTestUtils.setField(externalDataService, "backendBaseUrl", backendBaseUrl);
    }

    @Test
    void getAllNichos_ValidResponse_ShouldReturnListOfNichoDTOs() {
        // Arrange
        NichoDTO nicho1 = new NichoDTO();
        nicho1.setCodigo(UUID.randomUUID().toString());
        nicho1.setUbicacion("Sector A");
        nicho1.setEstado(EstadoNicho.OCUPADO);

        NichoDTO nicho2 = new NichoDTO();
        nicho2.setCodigo(UUID.randomUUID().toString());
        nicho2.setUbicacion("Sector B");
        nicho2.setEstado(EstadoNicho.DISPONIBLE);

        NichoDTO[] nichosArray = {nicho1, nicho2};
        when(restTemplate.getForObject(backendBaseUrl + "/nichos", NichoDTO[].class))
                .thenReturn(nichosArray);

        // Act
        List<NichoDTO> result = externalDataService.getAllNichos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Sector A", result.get(0).getUbicacion());
        assertEquals("Sector B", result.get(1).getUbicacion());
        assertTrue(isValidUUID(result.get(0).getCodigo()));
        assertTrue(isValidUUID(result.get(1).getCodigo()));
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/nichos", NichoDTO[].class);
    }

    @Test
    void getAllNichos_EmptyResponse_ShouldReturnEmptyList() {
        // Arrange
        NichoDTO[] emptyArray = {};
        when(restTemplate.getForObject(backendBaseUrl + "/nichos", NichoDTO[].class))
                .thenReturn(emptyArray);

        // Act
        List<NichoDTO> result = externalDataService.getAllNichos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/nichos", NichoDTO[].class);
    }

    @Test
    void getAllNichos_BackendError_ShouldThrowRuntimeException() {
        // Arrange
        when(restTemplate.getForObject(backendBaseUrl + "/nichos", NichoDTO[].class))
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            externalDataService.getAllNichos();
        });
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/nichos", NichoDTO[].class);
    }

    @Test
    void getAllCuerpos_ValidResponse_ShouldReturnListOfCuerpoInhumadoDTOs() {
        // Arrange
        CuerpoInhumadoDTO cuerpo1 = new CuerpoInhumadoDTO();
        cuerpo1.setIdCadaver(UUID.randomUUID().toString());
        cuerpo1.setNombre("Juan");
        cuerpo1.setApellido("Pérez");
        cuerpo1.setFechaDefuncion(LocalDate.of(2025, 1, 1));
        cuerpo1.setEstado(EstadoCuerpo.INHUMADO);

        CuerpoInhumadoDTO cuerpo2 = new CuerpoInhumadoDTO();
        cuerpo2.setIdCadaver(UUID.randomUUID().toString());
        cuerpo2.setNombre("María");
        cuerpo2.setApellido("Gómez");
        cuerpo2.setFechaDefuncion(LocalDate.of(2025, 2, 1));
        cuerpo2.setEstado(EstadoCuerpo.INHUMADO);

        CuerpoInhumadoDTO[] cuerposArray = {cuerpo1, cuerpo2};
        when(restTemplate.getForObject(backendBaseUrl + "/cuerposinhumados", CuerpoInhumadoDTO[].class))
                .thenReturn(cuerposArray);

        // Act
        List<CuerpoInhumadoDTO> result = externalDataService.getAllCuerpos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getNombre());
        assertEquals("María", result.get(1).getNombre());
        assertTrue(isValidUUID(result.get(0).getIdCadaver()));
        assertTrue(isValidUUID(result.get(1).getIdCadaver()));
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/cuerposinhumados", CuerpoInhumadoDTO[].class);
    }

    @Test
    void getAllCuerpos_EmptyResponse_ShouldReturnEmptyList() {
        // Arrange
        CuerpoInhumadoDTO[] emptyArray = {};
        when(restTemplate.getForObject(backendBaseUrl + "/cuerposinhumados", CuerpoInhumadoDTO[].class))
                .thenReturn(emptyArray);

        // Act
        List<CuerpoInhumadoDTO> result = externalDataService.getAllCuerpos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/cuerposinhumados", CuerpoInhumadoDTO[].class);
    }

    @Test
    void getAllNichoCuerpo_ValidResponse_ShouldReturnListOfNichoCuerpoDTOs() {
        // Arrange
        NichoCuerpoDTO relacion1 = new NichoCuerpoDTO();
        relacion1.setId(UUID.randomUUID().toString());
        relacion1.setIdCadaver(UUID.randomUUID().toString());
        relacion1.setCodigoNicho(UUID.randomUUID().toString());

        NichoCuerpoDTO relacion2 = new NichoCuerpoDTO();
        relacion2.setId(UUID.randomUUID().toString());
        relacion2.setIdCadaver(UUID.randomUUID().toString());
        relacion2.setCodigoNicho(UUID.randomUUID().toString());

        NichoCuerpoDTO[] relacionesArray = {relacion1, relacion2};
        when(restTemplate.getForObject(backendBaseUrl + "/nichoscuerpos", NichoCuerpoDTO[].class))
                .thenReturn(relacionesArray);

        // Act
        List<NichoCuerpoDTO> result = externalDataService.getAllNichoCuerpo();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(isValidUUID(result.get(0).getId()));
        assertTrue(isValidUUID(result.get(0).getIdCadaver()));
        assertTrue(isValidUUID(result.get(0).getCodigoNicho()));
        assertTrue(isValidUUID(result.get(1).getId()));
        assertTrue(isValidUUID(result.get(1).getIdCadaver()));
        assertTrue(isValidUUID(result.get(1).getCodigoNicho()));
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/nichoscuerpos", NichoCuerpoDTO[].class);
    }

    @Test
    void getAllNichoCuerpo_NullOrErrorResponse_ShouldThrowRuntimeException() {
        // Arrange
        when(restTemplate.getForObject(backendBaseUrl + "/nichoscuerpos", NichoCuerpoDTO[].class))
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            externalDataService.getAllNichoCuerpo();
        });
        verify(restTemplate, times(1)).getForObject(backendBaseUrl + "/nichoscuerpos", NichoCuerpoDTO[].class);
    }

    // Método auxiliar para validar UUID
    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}