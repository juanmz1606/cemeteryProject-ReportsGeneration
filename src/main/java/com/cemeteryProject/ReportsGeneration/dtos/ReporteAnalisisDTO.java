package com.cemeteryProject.ReportsGeneration.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class ReporteAnalisisDTO {
    private LocalDate fechaGeneracion;
    private String usuario;
    private int totalCuerpos;
    private int totalNichos;
    private double porcentajeOcupacion;

    private Map<String, Double> promedioMensualPorTipo; // tipo -> promedio
    private double promedioMensualGeneral;
}
