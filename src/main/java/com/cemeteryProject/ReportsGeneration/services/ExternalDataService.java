package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoCuerpoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.NichoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalDataService {

    private final RestTemplate restTemplate;

    @Value("${backend.management.base-url}")
    private String backendBaseUrl;

    public List<NichoDTO> getAllNichos() {
        String url = backendBaseUrl + "/nichos";
        NichoDTO[] nichos = restTemplate.getForObject(url, NichoDTO[].class);
        return Arrays.asList(nichos);
    }

    public List<CuerpoInhumadoDTO> getAllCuerpos() {
        String url = backendBaseUrl + "/cuerposinhumados";
        CuerpoInhumadoDTO[] cuerpos = restTemplate.getForObject(url, CuerpoInhumadoDTO[].class);
        return Arrays.asList(cuerpos);
    }

    public List<NichoCuerpoDTO> getAllNichoCuerpo() {
        String url = backendBaseUrl + "/nichoscuerpos";
        NichoCuerpoDTO[] relaciones = restTemplate.getForObject(url, NichoCuerpoDTO[].class);
        return Arrays.asList(relaciones);
    }
}