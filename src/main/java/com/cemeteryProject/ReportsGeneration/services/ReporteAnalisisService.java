package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteAnalisisService {

    private final ExternalDataService externalDataService;

    public ReporteAnalisisDTO generarAnalisis(String usuario) {
        List<CuerpoInhumadoDTO> cuerpos = externalDataService.getAllCuerpos();
        List<NichoDTO> nichos = externalDataService.getAllNichos();

        ReporteAnalisisDTO dto = new ReporteAnalisisDTO();
        dto.setFechaGeneracion(LocalDate.now());
        dto.setUsuario(usuario);
        dto.setTotalCuerpos(cuerpos.size());
        dto.setTotalNichos(nichos.size());

        // Ocupación
        dto.setPorcentajeOcupacion(nichos.size() == 0 ? 0 :
            (double) cuerpos.size() / nichos.size() * 100);

        // Promedios
        Map<Month, Long> conteoMensual = cuerpos.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getFechaIngreso().toLocalDate().getMonth(),
                    Collectors.counting()
                ));

        double promedioGeneral = conteoMensual.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        Map<String, Double> promedioPorEstado = cuerpos.stream()
        .collect(Collectors.groupingBy(
            c -> c.getEstado().toString(),
            Collectors.averagingInt(c -> {
                LocalDate fecha = c.getFechaIngreso().toLocalDate();
                return fecha.getYear() * 12 + fecha.getMonthValue();
            })
        ));
        
        

        dto.setPromedioMensualGeneral(promedioGeneral);
        dto.setPromedioMensualPorTipo(promedioPorEstado);

        return dto;
    }
}
