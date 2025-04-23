package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.DocumentoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoCuerpoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteDTO;
import com.cemeteryProject.ReportsGeneration.models.DocumentoModel;
import com.cemeteryProject.ReportsGeneration.repositories.IDocumentoRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final IDocumentoRepository documentoRepository;
    private final ExternalDataService externalDataService; // <<--- Agregado 

    public ReporteDTO generarDatosReporte() {
        List<NichoDTO> nichos = externalDataService.getAllNichos();
        List<CuerpoInhumadoDTO> cuerpos = externalDataService.getAllCuerpos();
        List<NichoCuerpoDTO> relaciones = externalDataService.getAllNichoCuerpo();

        return new ReporteDTO(nichos.size(), cuerpos.size(), relaciones.size());
    }

    public DocumentoDTO toDTO(DocumentoModel model) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setId(model.getId());
        dto.setNombre(model.getNombre());
        dto.setFechaGeneracion(model.getFechaGeneracion());
        dto.setTipo(model.getTipo());
        dto.setUsuarioId(model.getUsuarioId());
        return dto;
    }

    public DocumentoModel toModel(DocumentoDTO dto) {
        DocumentoModel model = new DocumentoModel();
        model.setNombre(dto.getNombre());
        model.setFechaGeneracion(dto.getFechaGeneracion());
        model.setTipo(dto.getTipo());
        model.setUsuarioId(dto.getUsuarioId());
        return model;
    }

    public List<DocumentoDTO> obtenerTodos() {
        return documentoRepository.findAll().stream().map(this::toDTO).toList();
    }
    
    public DocumentoDTO obtenerPorId(String id) {
        return documentoRepository.findById(id).map(this::toDTO).orElse(null);
    }
    
    public DocumentoDTO crearDocumento(DocumentoDTO dto) {
        DocumentoModel model = toModel(dto);
        return toDTO(documentoRepository.save(model));
    }
    
    public DocumentoDTO actualizarDocumento(String id, DocumentoDTO dto) {
        if (!documentoRepository.existsById(id)) return null;
        DocumentoModel actualizado = toModel(dto);
        actualizado.setId(id); // mantener el ID original
        return toDTO(documentoRepository.save(actualizado));
    }
    
    public boolean eliminarPorId(String id) {
        if (!documentoRepository.existsById(id)) return false;
        documentoRepository.deleteById(id);
        return true;
    }

}

