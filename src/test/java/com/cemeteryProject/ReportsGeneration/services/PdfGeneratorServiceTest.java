package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import com.cemeteryProject.ReportsGeneration.models.CuerpoInhumadoModel.EstadoCuerpo;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PdfGeneratorServiceTest {

    @InjectMocks
    private PdfGeneratorService pdfGeneratorService;

    @Mock
    private Document document;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test PGS-01: DTO válido con todos los campos debería retornar byte[] no nulo con contenido PDF válido.
     */
    @Test
    void generarReportePDFConAnalisis_DtoValidoConTodosLosCampos_ShouldReturnValidPdfBytes() {
        // Preparar DTO válido
        ReporteAnalisisDTO analisis = new ReporteAnalisisDTO();
        analisis.setUsuario("user123");
        analisis.setFechaGeneracion(LocalDate.now());
        analisis.setTotalNichos(10);
        analisis.setTotalCuerpos(8);
        analisis.setCuerposAsignados(6);
        analisis.setPorcentajeOcupacion(60.0);
        analisis.setNichosDisponibles(4);
        analisis.setCuerposRecientes(2);
        analisis.setPromedioMensualGeneral(5.0);
        analisis.setPromedioMensualPorTipo(Map.of("INHUMADO", 3.0, "EXHUMADO", 2.0));
        analisis.setEstadoNichos(Map.of("OCUPADO", 6L, "DISPONIBLE", 4L));
        analisis.setCuerposPorTipo(Map.of(EstadoCuerpo.INHUMADO, 5L, EstadoCuerpo.EXHUMADO, 3L));
        analisis.setCuerposAsignadosDistribucion(Map.of("Asignados", 6L, "No Asignados", 2L));
        analisis.setDocumentTypes(Map.of("REPORTE", 10L, "CERTIFICADO", 5L));
        List<ReporteAnalisisDTO.WeeklyData> weeklyInhumations = new ArrayList<>();
        ReporteAnalisisDTO.WeeklyData weeklyData = new ReporteAnalisisDTO.WeeklyData();
        weeklyData.setWeek("1-JAN");
        weeklyData.setCount(2L);
        weeklyInhumations.add(weeklyData);
        analisis.setWeeklyInhumations(weeklyInhumations);
        analisis.setWeeklyDocuments(weeklyInhumations);
        List<ReporteAnalisisDTO.TopUser> topUsers = new ArrayList<>();
        ReporteAnalisisDTO.TopUser topUser = new ReporteAnalisisDTO.TopUser();
        topUser.setUsuarioId("user1");
        topUser.setCount(10L);
        topUsers.add(topUser);
        analisis.setTopUsers(topUsers);

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

        // Validar
        assertNotNull(pdfBytes, "El PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(isValidPdf(pdfBytes), "El PDF debe tener una cabecera válida");
        String pdfContent = extractPdfText(pdfBytes);
        assertTrue(pdfContent.contains("Reporte del Cementerio"), "El PDF debe contener el título");
        assertTrue(pdfContent.contains("Generado por: user123"), "El PDF debe contener el usuario");
        assertTrue(pdfContent.contains("Total de nichos: 10"), "El PDF debe contener el total de nichos");
        assertTrue(pdfContent.contains("1° ID: user1: 10 documentos"), "El PDF debe contener el top usuario");
    }

    /**
     * Test PGS-02: DTO sin datos (listas vacías) debería retornar byte[] válido con texto pero sin gráficos dinámicos.
     */
    @Test
    void generarReportePDFConAnalisis_DtoSinDatos_ShouldReturnValidPdfWithTextOnly() {
        // Preparar DTO con listas vacías
        ReporteAnalisisDTO analisis = new ReporteAnalisisDTO();
        analisis.setUsuario("user123");
        analisis.setFechaGeneracion(LocalDate.now());
        analisis.setTotalNichos(0);
        analisis.setTotalCuerpos(0);
        analisis.setCuerposAsignados(0);
        analisis.setPorcentajeOcupacion(0.0);
        analisis.setNichosDisponibles(0);
        analisis.setCuerposRecientes(0);
        analisis.setPromedioMensualGeneral(0.0);
        analisis.setPromedioMensualPorTipo(new HashMap<>());
        analisis.setEstadoNichos(new HashMap<>());
        analisis.setCuerposPorTipo(new HashMap<>());
        analisis.setCuerposAsignadosDistribucion(new HashMap<>());
        analisis.setDocumentTypes(new HashMap<>());
        analisis.setWeeklyInhumations(new ArrayList<>());
        analisis.setWeeklyDocuments(new ArrayList<>());
        analisis.setTopUsers(new ArrayList<>());

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

        // Validar
        assertNotNull(pdfBytes, "El PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(isValidPdf(pdfBytes), "El PDF debe tener una cabecera válida");
        String pdfContent = extractPdfText(pdfBytes);
        assertTrue(pdfContent.contains("Reporte del Cementerio"), "El PDF debe contener el título");
        assertTrue(pdfContent.contains("Generado por: user123"), "El PDF debe contener el usuario");
        assertTrue(pdfContent.contains("Total de nichos: 0"), "El PDF debe contener el total de nichos");
        assertTrue(pdfContent.contains("No hay datos de usuarios disponibles"), "El PDF debe indicar que no hay datos de usuarios");
    }

    /**
     * Test PGS-03: DTO con valores nulos en campos no obligatorios debería generar PDF correctamente.
     */
    @Test
    void generarReportePDFConAnalisis_DtoConCamposNoObligatoriosNulos_ShouldGeneratePdf() {
        // Preparar DTO con campos no obligatorios nulos
        ReporteAnalisisDTO analisis = new ReporteAnalisisDTO();
        analisis.setUsuario("user123");
        analisis.setFechaGeneracion(LocalDate.now());
        analisis.setTotalNichos(10);
        analisis.setTotalCuerpos(8);
        // Campos como promedios, mapas y listas se dejan vacíos o nulos
        analisis.setPromedioMensualPorTipo(new HashMap<>());
        analisis.setEstadoNichos(new HashMap<>());
        analisis.setCuerposPorTipo(new HashMap<>());
        analisis.setCuerposAsignadosDistribucion(new HashMap<>());
        analisis.setDocumentTypes(new HashMap<>());
        analisis.setWeeklyInhumations(new ArrayList<>());
        analisis.setWeeklyDocuments(new ArrayList<>());
        analisis.setTopUsers(new ArrayList<>());

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

        // Validar
        assertNotNull(pdfBytes, "El PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(isValidPdf(pdfBytes), "El PDF debe tener una cabecera válida");
        String pdfContent = extractPdfText(pdfBytes);
        assertTrue(pdfContent.contains("Reporte del Cementerio"), "El PDF debe contener el título");
        assertTrue(pdfContent.contains("Generado por: user123"), "El PDF debe contener el usuario");
        assertTrue(pdfContent.contains("Total de nichos: 10"), "El PDF debe contener el total de nichos");
    }

    /**
     * Test PGS-04: DTO con campos críticos nulos debería generar PDF con valores por defecto o vacíos.
     */
    @Test
    void generarReportePDFConAnalisis_DtoConCamposCriticosNulos_ShouldReturnNull() {
        // Preparar DTO con campos críticos nulos
        ReporteAnalisisDTO analisis = new ReporteAnalisisDTO();
        analisis.setUsuario(null); // Campo crítico
        analisis.setFechaGeneracion(null); // Campo crítico
        analisis.setTotalNichos(0);
        analisis.setTotalCuerpos(0);
        analisis.setCuerposAsignados(0);
        analisis.setPorcentajeOcupacion(0.0);
        analisis.setNichosDisponibles(0);
        analisis.setCuerposRecientes(0);
        analisis.setPromedioMensualGeneral(0.0);
        analisis.setPromedioMensualPorTipo(new HashMap<>());
        analisis.setEstadoNichos(new HashMap<>());
        analisis.setCuerposPorTipo(new HashMap<>());
        analisis.setCuerposAsignadosDistribucion(new HashMap<>());
        analisis.setDocumentTypes(new HashMap<>());
        analisis.setWeeklyInhumations(new ArrayList<>());
        analisis.setWeeklyDocuments(new ArrayList<>());
        analisis.setTopUsers(new ArrayList<>());

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

        // Validar
        assertNull(pdfBytes, "El PDF debe ser nulo cuando los campos críticos son nulos");
    }

    /**
     * Test PGS-05: Excepción interna debería retornar null.
     */
    @Test
    void generarReportePDFConAnalisis_ExcepcionInterna_ShouldReturnNull() {
        // Preparar DTO válido
        ReporteAnalisisDTO analisis = new ReporteAnalisisDTO();
        analisis.setUsuario("user123");
        analisis.setFechaGeneracion(LocalDate.now());

        // Simular excepción interna
        try (MockedStatic<PdfWriter> mockedPdfWriter = mockStatic(PdfWriter.class)) {
            mockedPdfWriter.when(() -> PdfWriter.getInstance(any(Document.class), any(ByteArrayOutputStream.class)))
                .thenThrow(new DocumentException("Error de IO simulado"));

            // Ejecutar
            byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

            // Validar
            assertNull(pdfBytes, "El PDF debe ser nulo en caso de excepción interna");
        }
    }

    /**
     * Test PGS-06: Lista válida con varios cuerpos debería retornar byte[] con PDF bien formado.
     */
    @Test
    void generarReportePDFCuerpos_ListaValidaConVariosCuerpos_ShouldReturnValidPdf() {
        // Preparar lista de cuerpos
        List<CuerpoInhumadoDTO> cuerpoList = new ArrayList<>();
        CuerpoInhumadoDTO cuerpo1 = createFullCuerpoDTO("1");
        CuerpoInhumadoDTO cuerpo2 = createFullCuerpoDTO("2");
        cuerpoList.add(cuerpo1);
        cuerpoList.add(cuerpo2);

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFCuerpos(cuerpoList);

        // Validar
        assertNotNull(pdfBytes, "El PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(isValidPdf(pdfBytes), "El PDF debe tener una cabecera válida");
        String pdfContent = extractPdfText(pdfBytes);
        assertTrue(pdfContent.contains("Lista de Cuerpos Registrados"), "El PDF debe contener el título");
        assertTrue(pdfContent.contains("Cuerpo #1 - ID: 1"), "El PDF debe contener el primer cuerpo");
        assertTrue(pdfContent.contains("Cuerpo #2 - ID: 2"), "El PDF debe contener el segundo cuerpo");
        assertTrue(pdfContent.contains("Nombre: John Doe"), "El PDF debe contener el nombre del cuerpo");
    }

    /**
     * Test PGS-07: Lista vacía debería lanzar RuntimeException.
     */
    @Test
    void generarReportePDFCuerpos_ListaVacia_ShouldThrowRuntimeException() {
        // Preparar lista vacía
        List<CuerpoInhumadoDTO> cuerpoList = new ArrayList<>();

        // Validar excepción
        assertThrows(RuntimeException.class, () -> {
            pdfGeneratorService.generarReportePDFCuerpos(cuerpoList);
        }, "Debe lanzar RuntimeException para lista vacía");
    }

    /**
     * Test PGS-08: Lista con elementos con campos nulos debería mostrar “N/A” en el PDF.
     */
    @Test
    void generarReportePDFCuerpos_ListaConCamposNulos_ShouldShowNA() {
        // Preparar cuerpo con campos nulos
        CuerpoInhumadoDTO cuerpo = new CuerpoInhumadoDTO();
        cuerpo.setIdCadaver("1");
        // Dejar todos los demás campos como null

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFCuerpos(List.of(cuerpo));

        // Validar
        assertNotNull(pdfBytes, "El PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(isValidPdf(pdfBytes), "El PDF debe tener una cabecera válida");
        String pdfContent = extractPdfText(pdfBytes);
        assertTrue(pdfContent.contains("Lista de Cuerpos Registrados"), "El PDF debe contener el título");
        assertTrue(pdfContent.contains("Cuerpo #1 - ID: 1"), "El PDF debe contener el ID del cuerpo");
        assertTrue(pdfContent.contains("Nombre: N/A"), "El PDF debe mostrar N/A para nombre nulo");
        assertTrue(pdfContent.contains("Documento: N/A"), "El PDF debe mostrar N/A para documento nulo");
        assertTrue(pdfContent.contains("Estado: N/A"), "El PDF debe mostrar N/A para estado nulo");
    }

    /**
     * Test PGS-09: Cuerpo con todos los campos debería generar todas las filas de atributos correctamente.
     */
    @Test
    void generarReportePDFCuerpos_CuerpoConTodosLosCampos_ShouldGenerateAllAttributes() {
        // Preparar cuerpo con todos los campos
        CuerpoInhumadoDTO cuerpo = createFullCuerpoDTO("1");

        // Ejecutar
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFCuerpos(List.of(cuerpo));

        // Validar
        assertNotNull(pdfBytes, "El PDF no debe ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(isValidPdf(pdfBytes), "El PDF debe tener una cabecera válida");
        String pdfContent = extractPdfText(pdfBytes);
        assertTrue(pdfContent.contains("Lista de Cuerpos Registrados"), "El PDF debe contener el título");
        assertTrue(pdfContent.contains("Cuerpo #1 - ID: 1"), "El PDF debe contener el ID del cuerpo");
        assertTrue(pdfContent.contains("Nombre: John Doe"), "El PDF debe contener el nombre");
        assertTrue(pdfContent.contains("Documento: 123456"), "El PDF debe contener el documento");
        assertTrue(pdfContent.contains("Estado: INHUMADO"), "El PDF debe contener el estado");
        assertTrue(pdfContent.contains("Observaciones: Sin observaciones"), "El PDF debe contener las observaciones");
    }

    /**
     * Test PGS-10: Error interno debería lanzar RuntimeException.
     */
    @Test
    void generarReportePDFCuerpos_ErrorInterno_ShouldThrowRuntimeException() {
        // Preparar lista válida
        List<CuerpoInhumadoDTO> cuerpoList = List.of(createFullCuerpoDTO("1"));

        // Simular excepción interna
        try (MockedStatic<PdfWriter> mockedPdfWriter = mockStatic(PdfWriter.class)) {
            mockedPdfWriter.when(() -> PdfWriter.getInstance(any(Document.class), any(ByteArrayOutputStream.class)))
                .thenThrow(new DocumentException("Error de IO simulado"));

            // Validar excepción
            assertThrows(RuntimeException.class, () -> {
                pdfGeneratorService.generarReportePDFCuerpos(cuerpoList);
            }, "Debe lanzar RuntimeException para error interno");
        }
    }

    // Métodos auxiliares
    private boolean isValidPdf(byte[] pdfBytes) {
        try (PdfReader reader = new PdfReader(pdfBytes)) {
            return new String(pdfBytes, 0, 5).startsWith("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }

    private String extractPdfText(byte[] pdfBytes) {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            return "";
        }
    }

    private CuerpoInhumadoDTO createFullCuerpoDTO(String id) {
        CuerpoInhumadoDTO cuerpo = new CuerpoInhumadoDTO();
        cuerpo.setIdCadaver(id);
        cuerpo.setNombre("John");
        cuerpo.setApellido("Doe");
        cuerpo.setDocumentoIdentidad("123456");
        cuerpo.setNumeroProtocoloNecropsia("NECRO123");
        cuerpo.setCausaMuerte("Natural");
        cuerpo.setFechaNacimiento(LocalDate.of(1950, 1, 1));
        cuerpo.setFechaDefuncion(LocalDate.of(2020, 1, 1));
        cuerpo.setFechaIngreso(LocalDateTime.of(2020, 1, 1, 10, 0));
        cuerpo.setFechaInhumacion(LocalDate.of(2020, 1, 2));
        cuerpo.setFechaExhumacion(LocalDate.of(2020, 1, 3));
        cuerpo.setFuncionarioReceptor("Jane Smith");
        cuerpo.setCargoFuncionario("Recepcionista");
        cuerpo.setAutoridadRemitente("Dr. Brown");
        cuerpo.setCargoAutoridadRemitente("Médico");
        cuerpo.setAutoridadExhumacion("Dr. Green");
        cuerpo.setCargoAutoridadExhumacion("Forense");
        cuerpo.setEstado(EstadoCuerpo.INHUMADO);
        cuerpo.setObservaciones("Sin observaciones");
        return cuerpo;
    }
}