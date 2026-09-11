package com.synccarreira.synccarreira_api.dto.psicologa;

import com.synccarreira.synccarreira_api.entities.OrientationSession;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** DTOs de agendamento de sessões (RF-08, RF-16). */
public final class SessionDTOs {

    private SessionDTOs() {
    }

    public record Participant(Long studentId, String studentName) {}

    public record View(
            Long id,
            String type,               // INDIVIDUAL | GRUPO
            String title,
            String description,
            LocalDateTime start,
            LocalDateTime end,
            String location,
            String status,             // AGENDADA | REALIZADA | CANCELADA
            String psychologistName,
            List<Participant> participants
    ) {
        public View(OrientationSession s) {
            this(
                    s.getId(),
                    s.getType().name(),
                    s.getTitle(),
                    s.getDescription(),
                    s.getStart(),
                    s.getEnd(),
                    s.getLocation(),
                    s.getStatus().name(),
                    s.getPsychologist() != null ? s.getPsychologist().getName() : null,
                    s.getParticipants().stream()
                            .map(p -> new Participant(p.getStudent().getId(), p.getStudent().getName()))
                            .toList()
            );
        }
    }

    public record Insert(
            @NotBlank(message = "Informe INDIVIDUAL ou GRUPO")
            @Pattern(regexp = "INDIVIDUAL|GRUPO") String type,
            @NotBlank(message = "Campo obrigatório") String title,
            String description,
            @NotNull(message = "Campo obrigatório") LocalDateTime start,
            @NotNull(message = "Campo obrigatório") LocalDateTime end,
            String location,
            @NotEmpty(message = "Selecione ao menos um aluno") List<Long> studentIds
    ) {
    }

    public record Update(
            @NotBlank(message = "Campo obrigatório") String title,
            String description,
            @NotNull(message = "Campo obrigatório") LocalDateTime start,
            @NotNull(message = "Campo obrigatório") LocalDateTime end,
            String location,
            @NotBlank @Pattern(regexp = "AGENDADA|REALIZADA|CANCELADA") String status,
            List<Long> studentIds
    ) {
    }
}
