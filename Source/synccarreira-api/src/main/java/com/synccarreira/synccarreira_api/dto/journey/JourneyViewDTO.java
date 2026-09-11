package com.synccarreira.synccarreira_api.dto.journey;

import com.synccarreira.synccarreira_api.entities.enums.JourneyStatus;

import java.util.List;

/** Panorama de progresso de uma jornada (RF-10). */
public record JourneyViewDTO(
        Long journeyId,
        int cycle,
        JourneyStatus status,
        Boolean inDoubt,
        boolean finalPhaseAvailable,
        List<TrailProgressDTO> trails
) {
    public record TrailProgressDTO(
            Long trailId,
            String name,
            int order,
            String status,          // BLOQUEADA | LIBERADA | CONCLUIDA
            long answered,
            long total,
            boolean hasSynthesis
    ) {
    }
}
