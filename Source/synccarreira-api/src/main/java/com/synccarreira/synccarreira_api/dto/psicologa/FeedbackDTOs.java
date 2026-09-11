package com.synccarreira.synccarreira_api.dto.psicologa;

import com.synccarreira.synccarreira_api.entities.Feedback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** DTOs de feedback da psicóloga para o aluno (RF-09, RF-16). */
public final class FeedbackDTOs {

    private FeedbackDTOs() {
    }

    public record View(
            Long id,
            String psychologistName,
            String studentName,
            Long studentId,
            Integer cycle,
            String sessionTitle,
            String text,
            Instant createdAt
    ) {
        public View(Feedback f) {
            this(
                    f.getId(),
                    f.getPsychologist() != null ? f.getPsychologist().getName() : null,
                    f.getStudent() != null ? f.getStudent().getName() : null,
                    f.getStudent() != null ? f.getStudent().getId() : null,
                    f.getJourney() != null ? f.getJourney().getCycle() : null,
                    f.getSession() != null ? f.getSession().getTitle() : null,
                    f.getText(),
                    f.getCreatedAt()
            );
        }
    }

    public record Insert(
            @NotNull(message = "Campo obrigatório") Long studentId,
            Long journeyId,
            Long sessionId,
            @NotBlank(message = "O texto do feedback é obrigatório") String text
    ) {
    }
}
