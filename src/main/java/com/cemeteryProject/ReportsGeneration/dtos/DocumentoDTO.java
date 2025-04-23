package com.cemeteryProject.ReportsGeneration.dtos;

import com.cemeteryProject.ReportsGeneration.models.DocumentoModel.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Solo incluye los campos no nulos en la respuesta JSON
public class DocumentoDTO {

    private String id; // El ID es generado automáticamente en el modelo
    private String nombre; // Nombre del documento
    private TipoDocumento tipo; // El tipo de documento (REPORTE, DIGITALIZACION)
    private LocalDateTime fechaGeneracion; // Fecha de generación del documento
    private String usuarioId; // ID del usuario que generó el documento
}

