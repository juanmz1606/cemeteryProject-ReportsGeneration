package com.cemeteryProject.ReportsGeneration.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "nicho")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NichoModel {

    @Id
    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo; // Código será un String generado automáticamente

    @NotNull
    private String ubicacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoNicho estado;

    @PrePersist
    public void generateCodigo() {
        // Generar el código automáticamente como un String
        if (this.codigo == null) {
            this.codigo = UUID.randomUUID().toString(); // Generar UUID y convertirlo a String
        }
    }

    public enum EstadoNicho {
        DISPONIBLE,
        OCUPADO,
        MANTENIMIENTO
    }
}
