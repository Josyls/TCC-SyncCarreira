package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.psicologa.PanelDTOs;
import com.synccarreira.synccarreira_api.entities.*;
import com.synccarreira.synccarreira_api.entities.enums.JourneyTrailStatus;
import com.synccarreira.synccarreira_api.repositories.*;
import com.synccarreira.synccarreira_api.security.CurrentUser;
import com.synccarreira.synccarreira_api.security.PsychologistScope;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Painel da psicóloga (RF-03, RF-04, RF-05).
 *
 * <p>RN-03: o status por aluno traz apenas progresso e sinalizações — nunca o
 * conteúdo das respostas individuais.</p>
 */
@Service
public class PsychologistPanelService {

    @Autowired private PsychologistScope scope;
    @Autowired private CurrentUser currentUser;
    @Autowired private JourneyRepository journeyRepository;
    @Autowired private JourneyTrailRepository journeyTrailRepository;
    @Autowired private OrientationAlertRepository alertRepository;
    @Autowired private TrailRepository trailRepository;
    @Autowired private AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public PanelDTOs.PanelSummary panel(Boolean onlyInDoubt) {
        long totalTrails = trailRepository.count();
        List<User> students = scope.studentsInScope();

        Map<Long, Boolean> openAlertByStudent = new HashMap<>();
        for (OrientationAlert a : openAlerts()) {
            openAlertByStudent.put(a.getStudent().getId(), true);
        }

        List<PanelDTOs.StudentStatus> statuses = new ArrayList<>();
        int completed = 0, inDoubtCount = 0;

        for (User s : students) {
            Account acc = accountRepository.findById(s.getId()).orElse(null);
            Journey j = journeyRepository.findFirstByStudentIdOrderByCycleDesc(s.getId()).orElse(null);

            long concluded = 0;
            String jStatus = "SEM_JORNADA";
            Integer cycle = null;
            Boolean inDoubt = null;
            if (j != null) {
                cycle = j.getCycle();
                jStatus = j.getStatus().name();
                inDoubt = j.getInDoubt();
                concluded = journeyTrailRepository.findByJourneyId(j.getId()).stream()
                        .filter(jt -> jt.getStatus() == JourneyTrailStatus.CONCLUIDA)
                        .count();
                if ("CONCLUIDA".equals(jStatus)) completed++;
                if (Boolean.TRUE.equals(inDoubt)) inDoubtCount++;
            }

            boolean needsGuidance = openAlertByStudent.getOrDefault(s.getId(), false);

            PanelDTOs.StudentStatus st = new PanelDTOs.StudentStatus(
                    s.getId(), s.getName(),
                    acc != null && acc.getInstitution() != null ? acc.getInstitution().getLegalName() : null,
                    acc != null && acc.getSchoolClass() != null ? acc.getSchoolClass().getName() : null,
                    cycle, jStatus, concluded, totalTrails, inDoubt, needsGuidance);

            // RF-05: "sinalizado com dúvida" = jornada atual em dúvida OU alerta aberto.
            if (Boolean.TRUE.equals(onlyInDoubt) && !Boolean.TRUE.equals(inDoubt) && !needsGuidance) continue;
            statuses.add(st);
        }

        statuses.sort(Comparator
                .comparing((PanelDTOs.StudentStatus s) -> !s.needsGuidance())          // alertas primeiro
                .thenComparing(PanelDTOs.StudentStatus::studentName, String.CASE_INSENSITIVE_ORDER));

        List<PanelDTOs.AlertView> alertViews = openAlerts().stream()
                .map(a -> {
                    Account acc = accountRepository.findById(a.getStudent().getId()).orElse(null);
                    return new PanelDTOs.AlertView(
                            a.getId(), a.getStudent().getId(), a.getStudent().getName(),
                            acc != null && acc.getSchoolClass() != null ? acc.getSchoolClass().getName() : null,
                            a.getReason(), a.getPriority(), a.getJourney().getId(),
                            Boolean.TRUE.equals(a.getResolved()), a.getCreatedAt());
                })
                .toList();

        return new PanelDTOs.PanelSummary(
                students.size(), completed, inDoubtCount, alertViews.size(), statuses, alertViews);
    }

    @Transactional
    public void resolveAlert(Long alertId) {
        OrientationAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado. ID: " + alertId));
        scope.assertStudentInScope(alert.getStudent().getId());
        alert.setResolved(true);
        alert.setResolvedBy(currentUser.user());
        alert.setResolvedAt(Instant.now());
        alertRepository.save(alert);
    }

    private List<OrientationAlert> openAlerts() {
        if (currentUser.isAdmin()) {
            return alertRepository.findByResolvedFalse();
        }
        Long instId = scope.institutionId();
        if (instId == null) return List.of();
        return alertRepository.findOpenByInstitution(instId);
    }
}
