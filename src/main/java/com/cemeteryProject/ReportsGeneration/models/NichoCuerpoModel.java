package com.cemeteryProject.ReportsGeneration.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "nichocuerpo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NichoCuerpoModel {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cadaver", nullable = false)
    private CuerpoInhumadoModel cuerpoInhumado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_nicho", nullable = false)
    private NichoModel nicho;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
