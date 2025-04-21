package com.cemeteryProject.ReportsGeneration.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data   
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NichoCuerpoDTO {
    private String id;
    private String idCadaver;
    private String codigoNicho;
    
}