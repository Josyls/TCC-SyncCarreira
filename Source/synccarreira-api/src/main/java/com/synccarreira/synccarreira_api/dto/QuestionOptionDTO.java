package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.QuestionOption;

public record QuestionOptionDTO(
        Long id,
        String optionText,
        Double humanitiesWeight,
        Double biologicalSciencesWeight,
        Double exactSciencesWeight,
        Double artsWeight
) {
    public QuestionOptionDTO(QuestionOption option) {
        this(
                option.getId(),
                option.getOptionText(),
                option.getHumanitiesWeight(),
                option.getBiologicalSciencesWeight(),
                option.getExactSciencesWeight(),
                option.getArtsWeight()
        );
    }
}