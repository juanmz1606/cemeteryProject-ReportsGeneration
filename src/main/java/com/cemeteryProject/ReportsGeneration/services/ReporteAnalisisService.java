package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.DocumentoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoCuerpoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteAnalisisService {

    private final ExternalDataService externalDataService;
    private final DocumentoService documentoService;

    public ReporteAnalisisDTO generarAnalisis(String usuario) {
        List<CuerpoInhumadoDTO> cuerpos = externalDataService.getAllCuerpos();
        List<NichoDTO> nichos = externalDataService.getAllNichos();
        List<DocumentoDTO> documents = documentoService.obtenerTodos();

        ReporteAnalisisDTO dto = new ReporteAnalisisDTO();
        dto.setFechaGeneracion(LocalDate.now());
        dto.setUsuario(usuario);
        dto.setTotalCuerpos(cuerpos.size());
        dto.setTotalNichos(nichos.size());

        // Cuerpos asignados
        List<NichoCuerpoDTO> nichosCuerpos = externalDataService.getAllNichoCuerpo();
        dto.setCuerposAsignados(nichosCuerpos.size());

        // Ocupación
        dto.setPorcentajeOcupacion(nichos.size() == 0 ? 0 :
            (double) nichosCuerpos.size() / nichos.size() * 100);

        // Nichos disponibles
        long nichosDisponibles = nichos.stream()
            .filter(n -> n.getEstado().toString().equals("DISPONIBLE"))
            .count();
        dto.setNichosDisponibles((int) nichosDisponibles);

        // Cuerpos recientes (último mes)
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        long cuerposRecientes = cuerpos.stream()
            .filter(c -> c.getFechaInhumacion() != null && 
                         c.getFechaInhumacion().isAfter(oneMonthAgo))
            .count();
        dto.setCuerposRecientes((int) cuerposRecientes);

        // Promedios mensuales
        Map<Month, Long> conteoMensual = cuerpos.stream()
            .collect(Collectors.groupingBy(
                c -> c.getFechaIngreso().toLocalDate().getMonth(),
                Collectors.counting()
            ));

        double promedioGeneral = conteoMensual.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
        dto.setPromedioMensualGeneral(promedioGeneral);

        Map<String, Double> promedioPorEstado = cuerpos.stream()
            .collect(Collectors.groupingBy(
                c -> c.getEstado().toString(),
                Collectors.averagingDouble(c -> 1.0)
            ));
        dto.setPromedioMensualPorTipo(promedioPorEstado);

        // Estado de nichos
        Map<String, Long> estadoNichos = nichos.stream()
            .collect(Collectors.groupingBy(
                nicho -> nicho.getEstado().toString(),
                Collectors.counting()
            ));
        dto.setEstadoNichos(estadoNichos);

        // Cuerpos por tipo (estado)
        Map<Object, Long> cuerposPorTipo = cuerpos.stream()
            .collect(Collectors.groupingBy(
                CuerpoInhumadoDTO::getEstado,
                Collectors.counting()
            ));
        dto.setCuerposPorTipo(cuerposPorTipo);

        // Tendencia de inhumaciones (12 semanas)
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsAgo = now.minusMonths(3);
        Map<String, Long> weeklyCounts = new HashMap<>();
        List<ReporteAnalisisDTO.WeeklyData> weeks = new ArrayList<>();

        // Generar las últimas 12 semanas (de más antiguo a más reciente)
        for (int i = 11; i >= 0; i--) {
            LocalDate weekStart = now.minusDays(i * 7L);
            String weekKey = String.format("%d-%s", weekStart.getDayOfMonth(), weekStart.getMonth().toString().substring(0, 3));
            weeklyCounts.put(weekKey, 0L);
            ReporteAnalisisDTO.WeeklyData weekData = new ReporteAnalisisDTO.WeeklyData();
            weekData.setWeek(weekKey);
            weeks.add(weekData);
        }

        // Contar inhumaciones por semana
        cuerpos.forEach(cuerpo -> {
            LocalDate fechaInhumacion = cuerpo.getFechaInhumacion();
            if (fechaInhumacion != null && fechaInhumacion.isAfter(threeMonthsAgo)) {
                for (int i = 0; i < weeks.size(); i++) {
                    LocalDate weekStart = now.minusDays((11 - i) * 7L);
                    LocalDate nextWeekStart = i < weeks.size() - 1 ? now.minusDays((11 - (i + 1)) * 7L) : now;
                    if (fechaInhumacion.isAfter(weekStart) && fechaInhumacion.isBefore(nextWeekStart.plusDays(1))) {
                        String weekKey = weeks.get(i).getWeek();
                        weeklyCounts.put(weekKey, weeklyCounts.getOrDefault(weekKey, 0L) + 1);
                        break;
                    }
                }
            }
        });

        weeks.forEach(week -> week.setCount(weeklyCounts.get(week.getWeek())));
        dto.setWeeklyInhumations(weeks);

        // Distribución de cuerpos (Asignados, No Asignados)
        Map<String, Long> cuerposAsignadosDistribucion = new HashMap<>();
        cuerposAsignadosDistribucion.put("Asignados", (long) nichosCuerpos.size());
        cuerposAsignadosDistribucion.put("No Asignados", (long) (cuerpos.size() - nichosCuerpos.size()));
        dto.setCuerposAsignadosDistribucion(cuerposAsignadosDistribucion);

        // Documentos por tipo
        Map<String, Long> documentTypes = documents.stream()
            .collect(Collectors.groupingBy(
                doc -> doc.getTipo().toString(),
                Collectors.counting()
            ));
        dto.setDocumentTypes(documentTypes);

        // Tendencia de generación de documentos (12 semanas)
        LocalDateTime nowDoc = LocalDateTime.now();
        LocalDateTime threeMonthsAgoDoc = nowDoc.minusMonths(3);
        Map<String, Long> weeklyDocCounts = new HashMap<>();
        List<ReporteAnalisisDTO.WeeklyData> docWeeks = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            LocalDateTime weekStart = nowDoc.minusDays(i * 7L);
            String weekKey = String.format("%d-%s", weekStart.getDayOfMonth(), weekStart.getMonth().toString().substring(0, 3));
            weeklyDocCounts.put(weekKey, 0L);
            ReporteAnalisisDTO.WeeklyData weekData = new ReporteAnalisisDTO.WeeklyData();
            weekData.setWeek(weekKey);
            docWeeks.add(weekData);
        }

        documents.forEach(doc -> {
            LocalDateTime fechaGeneracion = doc.getFechaGeneracion();
            if (fechaGeneracion != null && fechaGeneracion.isAfter(threeMonthsAgoDoc)) {
                for (int i = 0; i < docWeeks.size(); i++) {
                    LocalDateTime weekStart = nowDoc.minusDays((11 - i) * 7L);
                    LocalDateTime nextWeekStart = i < docWeeks.size() - 1 ? nowDoc.minusDays((11 - (i + 1)) * 7L) : nowDoc;
                    if (fechaGeneracion.isAfter(weekStart) && fechaGeneracion.isBefore(nextWeekStart)) {
                        String weekKey = docWeeks.get(i).getWeek();
                        weeklyDocCounts.put(weekKey, weeklyDocCounts.getOrDefault(weekKey, 0L) + 1);
                        break;
                    }
                }
            }
        });

        docWeeks.forEach(week -> week.setCount(weeklyDocCounts.get(week.getWeek())));
        dto.setWeeklyDocuments(docWeeks);

        // Top 3 usuarios con más documentos
        Map<String, Long> userDocCounts = documents.stream()
            .collect(Collectors.groupingBy(
                DocumentoDTO::getUsuarioId,
                Collectors.counting()
            ));

        List<ReporteAnalisisDTO.TopUser> topUsers = userDocCounts.entrySet().stream()
            .map(entry -> {
                ReporteAnalisisDTO.TopUser topUser = new ReporteAnalisisDTO.TopUser();
                topUser.setUsuarioId(entry.getKey());
                topUser.setCount(entry.getValue());
                return topUser;
            })
            .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
            .limit(3)
            .collect(Collectors.toList());
        dto.setTopUsers(topUsers);

        return dto;
    }
}