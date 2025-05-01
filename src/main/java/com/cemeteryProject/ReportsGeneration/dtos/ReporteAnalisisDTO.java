package com.cemeteryProject.ReportsGeneration.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ReporteAnalisisDTO {
    private LocalDate fechaGeneracion;
    private String usuario;
    private int totalCuerpos;
    private int totalNichos;
    private double porcentajeOcupacion;
    private int cuerposAsignados;
    private int nichosDisponibles;
    private int cuerposRecientes;

    private Map<String, Double> promedioMensualPorTipo; // tipo -> promedio
    private double promedioMensualGeneral;
    private Map<String, Long> estadoNichos;
    private Map<Object, Long> cuerposPorTipo;
    private List<WeeklyData> weeklyInhumations; // Para la tendencia de inhumaciones
    private Map<String, Long> cuerposAsignadosDistribucion; // Para la distribución de cuerpos (Asignados, No Asignados)
    private Map<String, Long> documentTypes; // Para la distribución de documentos por tipo
    private List<WeeklyData> weeklyDocuments; // Para la tendencia de generación de documentos
    private List<TopUser> topUsers; // Para el top 3 usuarios con más documentos

    // Clase interna para datos semanales (inhumaciones y documentos)
    @Data
    public static class WeeklyData {
        private String week;
        private long count;
    }

    // Clase interna para el top 3 usuarios
    @Data
    public static class TopUser {
        private String usuarioId; // Cambiado de username a usuarioId
        private long count;
    }
}