package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.SchoolClass;

import java.time.Instant;

/** Representação de leitura de uma turma (RF-01). */
public record SchoolClassDTO(
        Long id,
        String name,
        Integer schoolYear,
        Long institutionId,
        String institutionName,
        Long psychologistId,
        String psychologistName,
        boolean active,
        Instant createdAt
) {
    public SchoolClassDTO(SchoolClass e) {
        this(
                e.getId(),
                e.getName(),
                e.getSchoolYear(),
                e.getInstitution() != null ? e.getInstitution().getId() : null,
                e.getInstitution() != null ? e.getInstitution().getLegalName() : null,
                e.getPsychologist() != null ? e.getPsychologist().getId() : null,
                e.getPsychologist() != null ? e.getPsychologist().getName() : null,
                Boolean.TRUE.equals(e.getActive()),
                e.getCreatedAt()
        );
    }
}
