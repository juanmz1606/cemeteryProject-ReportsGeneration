package com.cemeteryProject.ReportsGeneration.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cuerpoinhumado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuerpoInhumadoModel {

    @Id
    @Column(name = "id_cadaver", unique = true, nullable = false)
    private String idCadaver;

    @NotNull
    private String nombre;

    @NotNull
    private String apellido;

    @NotNull
    private String documentoIdentidad;

    @NotNull
    private String numeroProtocoloNecropsia;

    @NotNull
    private String causaMuerte;

    @NotNull
    private LocalDate fechaNacimiento;

    @NotNull
    private LocalDate fechaDefuncion;

    @NotNull
    private LocalDateTime fechaIngreso;

    @NotNull
    private LocalDate fechaInhumacion;

    @NotNull
    private LocalDate fechaExhumacion;

    @NotNull
    private String funcionarioReceptor;

    @NotNull
    private String cargoFuncionario;

    @NotNull
    private String autoridadRemitente;

    @NotNull
    private String cargoAutoridadRemitente;

    @NotNull
    private String autoridadExhumacion;

    @NotNull
    private String cargoAutoridadExhumacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoCuerpo estado;

    @NotNull
    private String observaciones;

    @PrePersist
    public void generateIdCadaver() {
        if (this.idCadaver == null) {
            this.idCadaver = UUID.randomUUID().toString();
        }
    }

    public enum EstadoCuerpo {
        INHUMADO,
        EXHUMADO
    }
}