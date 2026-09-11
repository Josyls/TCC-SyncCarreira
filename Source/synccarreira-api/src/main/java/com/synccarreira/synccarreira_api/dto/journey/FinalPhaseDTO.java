package com.synccarreira.synccarreira_api.dto.journey;

import java.util.List;

/**
 * Etapa final de Síntese (C3-B): consolida as sínteses das trilhas e apresenta
 * o leque de carreiras (RF-12 — preenchido no Bloco 5).
 */
public record FinalPhaseDTO(
        Long journeyId,
        boolean completed,
        String finalSynthesis,
        Boolean inDoubt,
        List<TrailSynthesisDTO> trailSyntheses,
        List<CareerSuggestionDTO> careerSuggestions,
        String disclaimer
) {
    public record TrailSynthesisDTO(int order, String trailName, String text) {}

    public record CareerSuggestionDTO(String area, String title, String type, String description) {}
}
