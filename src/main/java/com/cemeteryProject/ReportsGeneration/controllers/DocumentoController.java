package com.cemeteryProject.ReportsGeneration.controllers;

import com.cemeteryProject.ReportsGeneration.dtos.DocumentoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel.TipoDocumento;
import com.cemeteryProject.ReportsGeneration.services.DocumentoService;
import com.cemeteryProject.ReportsGeneration.services.PdfGeneratorService;
import com.cemeteryProject.ReportsGeneration.services.ReporteAnalisisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;
    private final PdfGeneratorService pdfGeneratorService;
    private final ReporteAnalisisService reporteAnalisisService;

    @GetMapping("/descargar")
    public ResponseEntity<byte[]> generarPdf(@RequestParam(defaultValue = "user-demo") String usuarioId) {
        // Construir datos analíticos
        ReporteAnalisisDTO analisis = reporteAnalisisService.generarAnalisis(usuarioId);

        // Generar PDF con análisis
        byte[] pdfBytes = pdfGeneratorService.generarReportePDFConAnalisis(analisis);

        DocumentoDTO docDTO = new DocumentoDTO();
        docDTO.setNombre("Reporte_Analitico_" + LocalDateTime.now());
        docDTO.setFechaGeneracion(LocalDateTime.now());
        docDTO.setTipo(TipoDocumento.REPORTE);
        docDTO.setUsuarioId(usuarioId);
        documentoService.crearDocumento(docDTO);


        // Devolver PDF al cliente
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("reporte", "reporte-analitico-cementerio.pdf");

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
