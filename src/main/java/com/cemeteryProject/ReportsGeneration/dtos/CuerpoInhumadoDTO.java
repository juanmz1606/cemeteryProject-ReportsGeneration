package com.cemeteryProject.ReportsGeneration.dtos;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.cemeteryProject.ReportsGeneration.models.CuerpoInhumadoModel.EstadoCuerpo;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CuerpoInhumadoDTO {
    private String idCadaver;
    private String nombre;
    private String apellido;
    private String documentoIdentidad;
    private String numeroProtocoloNecropsia;
    private String causaMuerte;
    private LocalDate fechaNacimiento;
    private LocalDate fechaDefuncion;
    private LocalDateTime fechaIngreso;
    private LocalDate fechaInhumacion;
    private LocalDate fechaExhumacion;
    private String funcionarioReceptor;
    private String cargoFuncionario;
    private String autoridadRemitente;
    private String cargoAutoridadRemitente;
    private String autoridadExhumacion;
    private String cargoAutoridadExhumacion;
    private EstadoCuerpo estado;
    private String observaciones;
}