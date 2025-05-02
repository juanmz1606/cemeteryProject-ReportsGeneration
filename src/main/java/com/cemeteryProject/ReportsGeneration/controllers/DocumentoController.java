package com.cemeteryProject.ReportsGeneration.controllers;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.DocumentoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel.TipoDocumento;
import com.cemeteryProject.ReportsGeneration.services.DocumentoService;
import com.cemeteryProject.ReportsGeneration.services.ExternalDataService;
import com.cemeteryProject.ReportsGeneration.services.PdfGeneratorService;
import com.cemeteryProject.ReportsGeneration.services.ReporteAnalisisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;
    private final PdfGeneratorService pdfGeneratorService;
    private final ReporteAnalisisService reporteAnalisisService;
    private final ExternalDataService externalDataService;

    @GetMapping("/descargar")
    public ResponseEntity<byte[]> generarPdf(@RequestParam(defaultValue = "user-demo") String usuarioId) {
        // Construir datos analíticos
        ReporteAnalisisDTO analisis = reporteAnalisisService.generarAnalisis(usuarioId);

        // Generar PDF con análisis
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

        // Crear registro del documento con un nombre más bonito
        DocumentoDTO docDTO = new DocumentoDTO();
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        docDTO.setNombre("Reporte del Cementerio | " + formattedDate);
        docDTO.setFechaGeneracion(LocalDateTime.now());
        docDTO.setTipo(TipoDocumento.REPORTE);
        docDTO.setUsuarioId(usuarioId);
        documentoService.crearDocumento(docDTO);

        // Devolver PDF al cliente
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("reporte", "reporte-cementerio.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/descargar-cuerpos")
    public ResponseEntity<byte[]> descargarListaCuerpos(@RequestParam(defaultValue = "user-demo") String usuarioId) {
        // Obtener la lista de cuerpos desde ExternalDataService
        List<CuerpoInhumadoDTO> cuerpoList = externalDataService.getAllCuerpos();

        // Generar PDF con la lista de cuerpos
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFCuerpos(cuerpoList);

        // Crear registro del documento con un nombre más bonito
        DocumentoDTO docDTO = new DocumentoDTO();
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        docDTO.setNombre("Lista de Cuerpos | " + formattedDate);
        docDTO.setFechaGeneracion(LocalDateTime.now());
        docDTO.setTipo(TipoDocumento.REPORTE);
        docDTO.setUsuarioId(usuarioId);
        documentoService.crearDocumento(docDTO);

        // Devolver PDF al cliente
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("reporte", "lista-cuerpos.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping
    public List<DocumentoDTO> listarTodos() {
        return documentoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDTO> obtenerPorId(@PathVariable String id) {
        DocumentoDTO dto = documentoService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<DocumentoDTO> crearDocumento(@RequestBody DocumentoDTO dto) {
        DocumentoDTO creado = documentoService.crearDocumento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoDTO> actualizarDocumento(@PathVariable String id, @RequestBody DocumentoDTO dto) {
        DocumentoDTO actualizado = documentoService.actualizarDocumento(id, dto);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDocumento(@PathVariable String id) {
        boolean eliminado = documentoService.eliminarPorId(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}