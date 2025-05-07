package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.*;
import com.cemeteryProject.ReportsGeneration.models.CuerpoInhumadoModel.EstadoCuerpo;
import com.cemeteryProject.ReportsGeneration.models.NichoModel.EstadoNicho;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteAnalisisServiceTest {

    @Mock
    private ExternalDataService externalDataService;

    @Mock
    private DocumentoService documentoService;

    @InjectMocks
    private ReporteAnalisisService reporteAnalisisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generarAnalisis_BasicValidData_ShouldReturnCorrectReporteAnalisisDTO() {
        String usuario = "user123";
        CuerpoInhumadoDTO cuerpo = new CuerpoInhumadoDTO();
        cuerpo.setIdCadaver(UUID.randomUUID().toString());
        cuerpo.setEstado(EstadoCuerpo.INHUMADO);
        cuerpo.setFechaInhumacion(LocalDate.now().minusDays(10));
        cuerpo.setFechaIngreso(LocalDateTime.now().minusDays(10));

        NichoDTO nicho = new NichoDTO();
        nicho.setCodigo(UUID.randomUUID().toString());
        nicho.setEstado(EstadoNicho.OCUPADO);

        NichoCuerpoDTO nichoCuerpo = new NichoCuerpoDTO();
        nichoCuerpo.setId(UUID.randomUUID().toString());
        nichoCuerpo.setIdCadaver(cuerpo.getIdCadaver());
        nichoCuerpo.setCodigoNicho(nicho.getCodigo());

        DocumentoDTO documento = new DocumentoDTO();
        documento.setId(UUID.randomUUID().toString());
        documento.setTipo(DocumentoModel.TipoDocumento.REPORTE);
        documento.setUsuarioId(usuario);
        documento.setFechaGeneracion(LocalDateTime.now());

        when(externalDataService.getAllCuerpos()).thenReturn(List.of(cuerpo));
        when(externalDataService.getAllNichos()).thenReturn(List.of(nicho));
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of(nichoCuerpo));
        when(documentoService.obtenerTodos()).thenReturn(List.of(documento));

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis(usuario);

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getFechaGeneracion());
        assertEquals(usuario, result.getUsuario());
        assertEquals(1, result.getTotalCuerpos());
        assertEquals(1, result.getTotalNichos());
        assertEquals(1, result.getCuerposAsignados());
        assertEquals(100.0, result.getPorcentajeOcupacion(), 0.01);
        assertEquals(0, result.getNichosDisponibles());
        assertEquals(1, result.getCuerposRecientes());
        assertEquals(1, result.getEstadoNichos().get("OCUPADO"));
        assertEquals(1, result.getCuerposPorTipo().get(EstadoCuerpo.INHUMADO));
        assertEquals(1, result.getDocumentTypes().get("REPORTE"));
        assertEquals(1, result.getTopUsers().size());
        assertEquals(usuario, result.getTopUsers().get(0).getUsuarioId());
        assertEquals(1, result.getTopUsers().get(0).getCount());
    }

    @Test
    void generarAnalisis_NullOrEmptyUsuario_ShouldReflectInDTO() {
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());
        when(documentoService.obtenerTodos()).thenReturn(List.of());

        ReporteAnalisisDTO resultNull = reporteAnalisisService.generarAnalisis(null);
        ReporteAnalisisDTO resultEmpty = reporteAnalisisService.generarAnalisis("");

        assertNotNull(resultNull);
        assertNull(resultNull.getUsuario());
        assertNotNull(resultEmpty);
        assertEquals("", resultEmpty.getUsuario());
    }

    @Test
    void generarAnalisis_EmptyData_ShouldReturnZeroCountsAndEmptyLists() {
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());
        when(documentoService.obtenerTodos()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        assertNotNull(result);
        assertEquals(0, result.getTotalCuerpos());
        assertEquals(0, result.getTotalNichos());
        assertEquals(0, result.getCuerposAsignados());
        assertEquals(0.0, result.getPorcentajeOcupacion(), 0.01);
        assertEquals(0, result.getNichosDisponibles());
        assertEquals(0, result.getCuerposRecientes());
        assertEquals(0.0, result.getPromedioMensualGeneral(), 0.01);
        assertTrue(result.getEstadoNichos().isEmpty());
        assertTrue(result.getCuerposPorTipo().isEmpty());
        assertTrue(result.getDocumentTypes().isEmpty());
        assertTrue(result.getTopUsers().isEmpty());
        assertEquals(12, result.getWeeklyInhumations().size());
        assertEquals(12, result.getWeeklyDocuments().size());
        result.getWeeklyInhumations().forEach(week -> assertEquals(0, week.getCount()));
        result.getWeeklyDocuments().forEach(week -> assertEquals(0, week.getCount()));
    }

    @Test
    void generarAnalisis_CuerposWithNullFechaInhumacion_ShouldNotCountInRecientes() {
        CuerpoInhumadoDTO cuerpo1 = new CuerpoInhumadoDTO();
        cuerpo1.setIdCadaver(UUID.randomUUID().toString());
        cuerpo1.setFechaInhumacion(null);
        cuerpo1.setFechaIngreso(LocalDateTime.now());
        cuerpo1.setEstado(EstadoCuerpo.INHUMADO);

        CuerpoInhumadoDTO cuerpo2 = new CuerpoInhumadoDTO();
        cuerpo2.setIdCadaver(UUID.randomUUID().toString());
        cuerpo2.setFechaInhumacion(LocalDate.now().minusDays(10));
        cuerpo2.setFechaIngreso(LocalDateTime.now());
        cuerpo2.setEstado(EstadoCuerpo.INHUMADO);

        when(externalDataService.getAllCuerpos()).thenReturn(List.of(cuerpo1, cuerpo2));
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());
        when(documentoService.obtenerTodos()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        assertEquals(2, result.getTotalCuerpos());
        assertEquals(1, result.getCuerposRecientes());
    }

    @Test
    void generarAnalisis_NichosWithDifferentEstados_ShouldCountCorrectly() {
        NichoDTO nicho1 = new NichoDTO();
        nicho1.setCodigo(UUID.randomUUID().toString());
        nicho1.setEstado(EstadoNicho.OCUPADO);

        NichoDTO nicho2 = new NichoDTO();
        nicho2.setCodigo(UUID.randomUUID().toString());
        nicho2.setEstado(EstadoNicho.DISPONIBLE);

        when(externalDataService.getAllNichos()).thenReturn(List.of(nicho1, nicho2));
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());
        when(documentoService.obtenerTodos()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        assertEquals(2, result.getTotalNichos());
        assertEquals(1, result.getEstadoNichos().get("OCUPADO"));
        assertEquals(1, result.getEstadoNichos().get("DISPONIBLE"));
        assertEquals(1, result.getNichosDisponibles());
    }

    @Test
    void generarAnalisis_CuerposWithNullEstado_ShouldThrowNullPointerException() {
        CuerpoInhumadoDTO cuerpo1 = new CuerpoInhumadoDTO();
        cuerpo1.setIdCadaver(UUID.randomUUID().toString());
        cuerpo1.setEstado(null);
        cuerpo1.setFechaIngreso(LocalDateTime.now());

        CuerpoInhumadoDTO cuerpo2 = new CuerpoInhumadoDTO();
        cuerpo2.setIdCadaver(UUID.randomUUID().toString());
        cuerpo2.setEstado(EstadoCuerpo.INHUMADO);
        cuerpo2.setFechaIngreso(LocalDateTime.now());

        when(externalDataService.getAllCuerpos()).thenReturn(List.of(cuerpo1, cuerpo2));
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());
        when(documentoService.obtenerTodos()).thenReturn(List.of());

        assertThrows(NullPointerException.class, () -> {
            reporteAnalisisService.generarAnalisis("user123");
        });
    }

    @Test
    void generarAnalisis_TendenciaInhumaciones_ShouldCountWeeklyCorrectly() {
        CuerpoInhumadoDTO cuerpo1 = new CuerpoInhumadoDTO();
        cuerpo1.setIdCadaver(UUID.randomUUID().toString());
        cuerpo1.setFechaInhumacion(LocalDate.now().minusDays(5));
        cuerpo1.setFechaIngreso(LocalDateTime.now());
        cuerpo1.setEstado(EstadoCuerpo.INHUMADO);

        CuerpoInhumadoDTO cuerpo2 = new CuerpoInhumadoDTO();
        cuerpo2.setIdCadaver(UUID.randomUUID().toString());
        cuerpo2.setFechaInhumacion(LocalDate.now().minusDays(15));
        cuerpo2.setFechaIngreso(LocalDateTime.now());
        cuerpo2.setEstado(EstadoCuerpo.INHUMADO);

        when(externalDataService.getAllCuerpos()).thenReturn(List.of(cuerpo1, cuerpo2));
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());
        when(documentoService.obtenerTodos()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        List<ReporteAnalisisDTO.WeeklyData> weeks = result.getWeeklyInhumations();
        assertEquals(12, weeks.size());

        LocalDate now = LocalDate.now();
        // Calcular las semanas según la lógica del servicio
        Map<String, Integer> expectedCounts = new HashMap<>();
        List<String> weekKeys = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate weekStart = now.minusDays(i * 7L);
            String weekKey = String.format("%d-%s", weekStart.getDayOfMonth(), weekStart.getMonth().toString().substring(0, 3));
            weekKeys.add(weekKey);
            expectedCounts.put(weekKey, 0);
        }

        // Asignar inhumaciones a las semanas correctas
        for (CuerpoInhumadoDTO cuerpo : List.of(cuerpo1, cuerpo2)) {
            LocalDate fechaInhumacion = cuerpo.getFechaInhumacion();
            for (int i = 0; i < weekKeys.size(); i++) {
                LocalDate weekStart = now.minusDays((11 - i) * 7L);
                LocalDate nextWeekStart = i < weekKeys.size() - 1 ? now.minusDays((11 - (i + 1)) * 7L) : now;
                if (fechaInhumacion.isAfter(weekStart) && fechaInhumacion.isBefore(nextWeekStart.plusDays(1))) {
                    String weekKey = weekKeys.get(i);
                    expectedCounts.put(weekKey, expectedCounts.get(weekKey) + 1);
                    break;
                }
            }
        }

        weeks.forEach(week -> {
            int expectedCount = expectedCounts.getOrDefault(week.getWeek(), 0);
            assertEquals(expectedCount, week.getCount(), "Week " + week.getWeek() + " should have " + expectedCount + " inhumations");
        });
    }

    @Test
    void generarAnalisis_DocumentosWeeklyTrend_ShouldReflectCorrectly() {
        DocumentoDTO doc1 = new DocumentoDTO();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setFechaGeneracion(LocalDateTime.now().minusDays(5));
        doc1.setTipo(DocumentoModel.TipoDocumento.REPORTE);
        doc1.setUsuarioId("user1");

        DocumentoDTO doc2 = new DocumentoDTO();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setFechaGeneracion(LocalDateTime.now().minusDays(15));
        doc2.setTipo(DocumentoModel.TipoDocumento.REPORTE);
        doc2.setUsuarioId("user2");

        when(documentoService.obtenerTodos()).thenReturn(List.of(doc1, doc2));
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        List<ReporteAnalisisDTO.WeeklyData> weeks = result.getWeeklyDocuments();
        assertEquals(12, weeks.size());

        LocalDateTime now = LocalDateTime.now();
        // Calcular las semanas según la lógica del servicio
        Map<String, Integer> expectedCounts = new HashMap<>();
        List<String> weekKeys = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime weekStart = now.minusDays(i * 7L);
            String weekKey = String.format("%d-%s", weekStart.getDayOfMonth(), weekStart.getMonth().toString().substring(0, 3));
            weekKeys.add(weekKey);
            expectedCounts.put(weekKey, 0);
        }

        // Asignar documentos a las semanas correctas
        for (DocumentoDTO doc : List.of(doc1, doc2)) {
            LocalDateTime fechaGeneracion = doc.getFechaGeneracion();
            if (fechaGeneracion != null) {
                for (int i = 0; i < weekKeys.size(); i++) {
                    LocalDateTime weekStart = now.minusDays((11 - i) * 7L);
                    LocalDateTime nextWeekStart = i < weekKeys.size() - 1 ? now.minusDays((11 - (i + 1)) * 7L) : now;
                    if (fechaGeneracion.isAfter(weekStart) && fechaGeneracion.isBefore(nextWeekStart)) {
                        String weekKey = weekKeys.get(i);
                        expectedCounts.put(weekKey, expectedCounts.get(weekKey) + 1);
                        break;
                    }
                }
            }
        }

        weeks.forEach(week -> {
            int expectedCount = expectedCounts.getOrDefault(week.getWeek(), 0);
            assertEquals(expectedCount, week.getCount(), "Week " + week.getWeek() + " should have " + expectedCount + " documents");
        });
    }

    @Test
    void generarAnalisis_DocumentoSinFechaGeneracion_ShouldBeIgnoredInTrend() {
        DocumentoDTO doc1 = new DocumentoDTO();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setFechaGeneracion(null);
        doc1.setTipo(DocumentoModel.TipoDocumento.REPORTE);
        doc1.setUsuarioId("user1");

        DocumentoDTO doc2 = new DocumentoDTO();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setFechaGeneracion(LocalDateTime.now().minusDays(5));
        doc2.setTipo(DocumentoModel.TipoDocumento.REPORTE);
        doc2.setUsuarioId("user2");

        when(documentoService.obtenerTodos()).thenReturn(List.of(doc1, doc2));
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        List<ReporteAnalisisDTO.WeeklyData> weeks = result.getWeeklyDocuments();
        assertEquals(12, weeks.size());

        LocalDateTime now = LocalDateTime.now();
        // Calcular las semanas según la lógica del servicio
        Map<String, Integer> expectedCounts = new HashMap<>();
        List<String> weekKeys = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime weekStart = now.minusDays(i * 7L);
            String weekKey = String.format("%d-%s", weekStart.getDayOfMonth(), weekStart.getMonth().toString().substring(0, 3));
            weekKeys.add(weekKey);
            expectedCounts.put(weekKey, 0);
        }

        // Asignar documentos a las semanas correctas
        for (DocumentoDTO doc : List.of(doc1, doc2)) {
            LocalDateTime fechaGeneracion = doc.getFechaGeneracion();
            if (fechaGeneracion != null) {
                for (int i = 0; i < weekKeys.size(); i++) {
                    LocalDateTime weekStart = now.minusDays((11 - i) * 7L);
                    LocalDateTime nextWeekStart = i < weekKeys.size() - 1 ? now.minusDays((11 - (i + 1)) * 7L) : now;
                    if (fechaGeneracion.isAfter(weekStart) && fechaGeneracion.isBefore(nextWeekStart)) {
                        String weekKey = weekKeys.get(i);
                        expectedCounts.put(weekKey, expectedCounts.get(weekKey) + 1);
                        break;
                    }
                }
            }
        }

        weeks.forEach(week -> {
            int expectedCount = expectedCounts.getOrDefault(week.getWeek(), 0);
            assertEquals(expectedCount, week.getCount(), "Week " + week.getWeek() + " should have " + expectedCount + " documents");
        });
    }

    @Test
    void generarAnalisis_Top3Usuarios_ShouldReturnMaxThreeOrdered() {
        DocumentoDTO doc1 = new DocumentoDTO();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setUsuarioId("user1");
        doc1.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        DocumentoDTO doc2 = new DocumentoDTO();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setUsuarioId("user1");
        doc2.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        DocumentoDTO doc3 = new DocumentoDTO();
        doc3.setId(UUID.randomUUID().toString());
        doc3.setUsuarioId("user2");
        doc3.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        DocumentoDTO doc4 = new DocumentoDTO();
        doc4.setId(UUID.randomUUID().toString());
        doc4.setUsuarioId("user3");
        doc4.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        DocumentoDTO doc5 = new DocumentoDTO();
        doc5.setId(UUID.randomUUID().toString());
        doc5.setUsuarioId("user4");
        doc5.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        when(documentoService.obtenerTodos()).thenReturn(List.of(doc1, doc2, doc3, doc4, doc5));
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());

        ReporteAnalisisDTO result = reporteAnalisisService.generarAnalisis("user123");

        List<ReporteAnalisisDTO.TopUser> topUsers = result.getTopUsers();
        assertEquals(3, topUsers.size());
        assertEquals("user1", topUsers.get(0).getUsuarioId());
        assertEquals(2, topUsers.get(0).getCount());
        assertEquals("user2", topUsers.get(1).getUsuarioId());
        assertEquals(1, topUsers.get(1).getCount());
        assertEquals("user3", topUsers.get(2).getUsuarioId());
        assertEquals(1, topUsers.get(2).getCount());
    }

    @Test
    void generarAnalisis_DocumentosSinUsuarioId_ShouldThrowNullPointerException() {
        DocumentoDTO doc1 = new DocumentoDTO();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setUsuarioId(null);
        doc1.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        DocumentoDTO doc2 = new DocumentoDTO();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setUsuarioId("user1");
        doc2.setTipo(DocumentoModel.TipoDocumento.REPORTE);

        when(documentoService.obtenerTodos()).thenReturn(List.of(doc1, doc2));
        when(externalDataService.getAllCuerpos()).thenReturn(List.of());
        when(externalDataService.getAllNichos()).thenReturn(List.of());
        when(externalDataService.getAllNichoCuerpo()).thenReturn(List.of());

        assertThrows(NullPointerException.class, () -> {
            reporteAnalisisService.generarAnalisis("user123");
        });
    }
}