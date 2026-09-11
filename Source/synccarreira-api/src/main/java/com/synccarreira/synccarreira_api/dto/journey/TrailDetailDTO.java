package com.synccarreira.synccarreira_api.dto.journey;

import java.util.List;
import java.util.Map;

/** Detalhe de uma trilha para o aluno responder (RF-10, RF-11). */
public record TrailDetailDTO(
        Long journeyId,
        Long trailId,
        String name,
        int order,
        String status,
        boolean canConclude,          // todas as perguntas respondidas
        String synthesisText,         // síntese já registrada (ou null)
        List<QuestionDTO> questions,
        Map<Long, AnswerDTO> answers  // questionId -> resposta atual
) {
    public record QuestionDTO(Long id, String content, String type, List<OptionDTO> options) {}
    public record OptionDTO(Long id, String text) {}
    public record AnswerDTO(Long optionId, String content) {}
}
