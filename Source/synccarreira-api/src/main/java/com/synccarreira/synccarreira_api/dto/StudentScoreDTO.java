package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Student;

public record StudentScoreDTO(
        Long studentId,
        Double humanitiesScore,
        Double exactSciencesScore,
        Double biologicalSciencesScore,
        Double artsScore
) {
    public StudentScoreDTO(Student student) {
        this(
                student.getId(),
                student.getHumanitiesScore(),
                student.getExactSciencesScore(),
                student.getBiologicalSciencesScore(),
                student.getArtsScore()
        );
    }
}