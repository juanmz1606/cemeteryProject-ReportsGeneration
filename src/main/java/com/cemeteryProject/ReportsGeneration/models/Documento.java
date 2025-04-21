package com.cemeteryProject.ReportsGeneration.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @Column(name = "id_documento", unique = true, nullable = false)
    private String id;

    @Column(name = "nombre_documento", length = 150, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipo;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "usuario_id", length = 36, nullable = false)
    private String usuarioId;

    @PrePersist
    public void generateId() {
        // Generar el código automáticamente como un String
        if (this.id == null) {
            this.id = UUID.randomUUID().toString(); // Generar UUID y convertirlo a String
        }
    }

    public enum TipoDocumento {
        REPORTE,
        DIGITALIZACION
    }
}

