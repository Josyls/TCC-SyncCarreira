package com.synccarreira.synccarreira_api.dto.psicologa;

import java.time.Instant;
import java.util.List;

/**
 * DTOs do painel da psicóloga (RF-03, RF-04, RF-05).
 * Importante: o status por aluno NÃO expõe respostas individuais (RN-03).
 */
public final class PanelDTOs {

    private PanelDTOs() {
    }

    /** RF-03 — status de um aluno: só progresso e flags, sem conteúdo de respostas. */
    public record StudentStatus(
            Long studentId,
            String studentName,
            String institutionName,
            String className,
            Integer cycle,
            String journeyStatus,          // SEM_JORNADA | EM_ANDAMENTO | CONCLUIDA
            long concludedTrails,
            long totalTrails,
            Boolean inDoubt,               // sinalização do aluno na conclusão
            boolean needsGuidance          // RF-04 — há alerta aberto
    ) {
    }

    /** RF-04 — alerta automático de necessidade de orientação. */
    public record AlertView(
            Long alertId,
            Long studentId,
            String studentName,
            String className,
            String reason,
            String priority,               // GRUPO | INDIVIDUAL
            Long journeyId,
            boolean resolved,
            Instant createdAt
    ) {
    }

    public record PanelSummary(
            int totalStudents,
            int completedJourneys,
            int inDoubtStudents,
            int openAlerts,
            List<StudentStatus> students,
            List<AlertView> alerts
    ) {
    }
}
