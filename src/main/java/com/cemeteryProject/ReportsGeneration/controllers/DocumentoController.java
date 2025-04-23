package com.cemeteryProject.ReportsGeneration.controllers;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.DocumentoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoCuerpoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoDTO;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel.TipoDocumento;
import com.cemeteryProject.ReportsGeneration.services.DocumentoService;
import com.cemeteryProject.ReportsGeneration.services.ExternalDataService;
import com.cemeteryProject.ReportsGeneration.services.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final ExternalDataService externalDataService;
    private final DocumentoService documentoService;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/descargar")
    public ResponseEntity<byte[]> generarPdf(@RequestParam(defaultValue = "user-demo") String usuarioId) {
        // Obtener datos para el reporte
        List<NichoDTO> nichos = externalDataService.getAllNichos();
        List<CuerpoInhumadoDTO> cuerpos = externalDataService.getAllCuerpos();
        List<NichoCuerpoDTO> relaciones = externalDataService.getAllNichoCuerpo();

        // Generar PDF
        byte[] pdfBytes = pdfGeneratorService.generarReportePDF(
                nichos.size(),
                cuerpos.size(),
                relaciones.size(),
                usuarioId
        );

        // Guardar el documento generado
        DocumentoDTO doc = new DocumentoDTO();
        doc.setNombre("Reporte_" + LocalDateTime.now());
        doc.setFechaGeneracion(LocalDateTime.now());
        doc.setTipo(TipoDocumento.REPORTE);
        doc.setUsuarioId(usuarioId);
        documentoService.crearDocumento(doc); // <-- ahora usando el DTO

        // Configurar los headers para la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("reporte", "reporte-cementerio.pdf");

        // Devolver el PDF al cliente
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
