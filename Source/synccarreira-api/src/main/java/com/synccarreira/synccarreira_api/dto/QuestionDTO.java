package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Question;
import com.synccarreira.synccarreira_api.entities.enums.QuestionType;

import java.util.List;

public record QuestionDTO(
        Long id,
        String content,
        QuestionType questionType,
        Long trailId,
        Long psychologistId,
        List<QuestionOptionDTO> options
) {
    public QuestionDTO(Question question) {
        this(
                question.getId(),
                question.getContent(),
                question.getQuestionType(),
                question.getTrail() != null ? question.getTrail().getId() : null,
                question.getPsychologist() != null ? question.getPsychologist().getId() : null,
                question.getOptions() != null ?
                        question.getOptions().stream().map(QuestionOptionDTO::new).toList() : List.of()
        );
    }
}