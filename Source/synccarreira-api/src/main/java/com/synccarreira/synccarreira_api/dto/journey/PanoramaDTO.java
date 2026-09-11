package com.synccarreira.synccarreira_api.dto.journey;

import java.time.Instant;
import java.util.List;

/** Panorama completo de uma jornada — histórico de respostas e sínteses (RF-13, RN-08). */
public record PanoramaDTO(
        Long journeyId,
        int cycle,
        String status,
        Boolean inDoubt,
        Instant startedAt,
        Instant finishedAt,
        String finalSynthesis,
        List<TrailBlockDTO> trails
) {
    public record TrailBlockDTO(int order, String trailName, String synthesisText, List<ItemDTO> items) {}

    public record ItemDTO(String question, String chosenOption, String content) {}
}
