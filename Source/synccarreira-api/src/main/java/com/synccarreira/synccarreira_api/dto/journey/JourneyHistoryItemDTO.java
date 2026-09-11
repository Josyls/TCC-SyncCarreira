package com.synccarreira.synccarreira_api.dto.journey;

import java.time.Instant;

/** Resumo de uma jornada anterior (RF-15). */
public record JourneyHistoryItemDTO(
        Long journeyId,
        int cycle,
        String status,
        Boolean inDoubt,
        long concludedTrails,
        long totalTrails,
        Instant startedAt,
        Instant finishedAt
) {
}
