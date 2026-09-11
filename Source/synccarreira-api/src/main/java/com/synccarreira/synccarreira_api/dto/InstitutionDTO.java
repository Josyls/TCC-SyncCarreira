package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Institution;
import com.synccarreira.synccarreira_api.entities.enums.InstitutionType;

import java.time.Instant;

/** Representação de leitura de uma instituição parceira (RF-01). */
public record InstitutionDTO(
        Long id,
        String legalName,
        String tradeName,
        String cnpj,
        InstitutionType type,
        boolean active,
        int classCount,
        Instant createdAt
) {
    public InstitutionDTO(Institution e) {
        this(
                e.getId(),
                e.getLegalName(),
                e.getTradeName(),
                e.getCnpj(),
                e.getType(),
                Boolean.TRUE.equals(e.getActive()),
                e.getClasses() != null ? e.getClasses().size() : 0,
                e.getCreatedAt()
        );
    }
}
