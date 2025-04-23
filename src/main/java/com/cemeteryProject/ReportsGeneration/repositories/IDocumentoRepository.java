package com.cemeteryProject.ReportsGeneration.repositories;

import com.cemeteryProject.ReportsGeneration.models.DocumentoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDocumentoRepository extends JpaRepository<DocumentoModel, String> {
}
