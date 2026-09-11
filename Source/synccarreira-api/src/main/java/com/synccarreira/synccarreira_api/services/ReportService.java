package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.entities.*;
import com.synccarreira.synccarreira_api.entities.enums.JourneyTrailStatus;
import com.synccarreira.synccarreira_api.repositories.*;
import com.synccarreira.synccarreira_api.security.PsychologistScope;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Relatórios de turma e individuais (RF-07). Saída em CSV (UTF-8 com BOM para o
 * Excel). O relatório de turma traz apenas progresso e status — não expõe as
 * respostas individuais (RN-03). O relatório individual inclui as sínteses do
 * próprio aluno, conforme o RF-07.
 */
@Service
public class ReportService {

    private static final String BOM = "﻿";
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private PsychologistScope scope;
    @Autowired private JourneyRepository journeyRepository;
    @Autowired private JourneyTrailRepository journeyTrailRepository;
    @Autowired private TrailSynthesisRepository trailSynthesisRepository;
    @Autowired private OrientationAlertRepository alertRepository;
    @Autowired private TrailRepository trailRepository;
    @Autowired private AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public String classReportCsv(Long classId) {
        long totalTrails = trailRepository.count();
        List<User> students = scope.studentsInScope().stream()
                .filter(s -> classId == null || inClass(s.getId(), classId))
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        StringBuilder sb = new StringBuilder(BOM);
        sb.append("Aluno;Instituicao;Turma;Ciclo;Status da jornada;Trilhas concluidas;Total de trilhas;Em duvida;Alerta aberto\n");
        for (User s : students) {
            Account acc = accountRepository.findById(s.getId()).orElse(null);
            Journey j = journeyRepository.findFirstByStudentIdOrderByCycleDesc(s.getId()).orElse(null);

            long concluded = 0;
            String status = "Sem jornada";
            String cycle = "-";
            String inDoubt = "-";
            if (j != null) {
                cycle = String.valueOf(j.getCycle());
                status = j.getStatus().name().equals("CONCLUIDA") ? "Concluida" : "Em andamento";
                inDoubt = j.getInDoubt() == null ? "-" : (j.getInDoubt() ? "Sim" : "Nao");
                concluded = journeyTrailRepository.findByJourneyId(j.getId()).stream()
                        .filter(jt -> jt.getStatus() == JourneyTrailStatus.CONCLUIDA).count();
            }
            boolean alert = j != null && alertRepository.findByJourneyId(j.getId())
                    .map(a -> !Boolean.TRUE.equals(a.getResolved())).orElse(false);

            sb.append(csv(s.getName())).append(';')
              .append(csv(acc != null && acc.getInstitution() != null ? acc.getInstitution().getLegalName() : "")).append(';')
              .append(csv(acc != null && acc.getSchoolClass() != null ? acc.getSchoolClass().getName() : "")).append(';')
              .append(cycle).append(';')
              .append(status).append(';')
              .append(concluded).append(';')
              .append(totalTrails).append(';')
              .append(inDoubt).append(';')
              .append(alert ? "Sim" : "Nao").append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String individualReportCsv(Long studentId) {
        scope.assertStudentInScope(studentId);
        User student = accountRepository.findById(studentId).map(Account::getUser)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado. ID: " + studentId));

        StringBuilder sb = new StringBuilder(BOM);
        sb.append("Relatorio individual;").append(csv(student.getName())).append('\n');

        List<Journey> journeys = journeyRepository.findByStudentIdOrderByCycleDesc(studentId);
        if (journeys.isEmpty()) {
            sb.append("\nO aluno ainda nao iniciou nenhuma jornada.\n");
            return sb.toString();
        }

        for (Journey j : journeys) {
            long concluded = journeyTrailRepository.findByJourneyId(j.getId()).stream()
                    .filter(jt -> jt.getStatus() == JourneyTrailStatus.CONCLUIDA).count();
            sb.append('\n')
              .append("Ciclo;").append(j.getCycle()).append('\n')
              .append("Status;").append(j.getStatus().name().equals("CONCLUIDA") ? "Concluida" : "Em andamento").append('\n')
              .append("Trilhas concluidas;").append(concluded).append('\n')
              .append("Inicio;").append(j.getStartedAt() != null ? j.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(D) : "-").append('\n')
              .append("Conclusao;").append(j.getFinishedAt() != null ? j.getFinishedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(D) : "-").append('\n')
              .append("Sinalizou duvida;").append(j.getInDoubt() == null ? "-" : (j.getInDoubt() ? "Sim" : "Nao")).append('\n')
              .append("\nSinteses por trilha:\n");

            trailSynthesisRepository.findByJourneyId(j.getId()).stream()
                    .sorted(Comparator.comparingInt(ts -> ts.getTrail().getSequentialOrder()))
                    .forEach(ts -> sb.append(csv(ts.getTrail().getName().name())).append(';')
                            .append(csv(ts.getText())).append('\n'));

            if (j.getFinalSynthesis() != null) {
                sb.append("SINTESE FINAL;").append(csv(j.getFinalSynthesis())).append('\n');
            }
        }
        return sb.toString();
    }

    private boolean inClass(Long studentId, Long classId) {
        return accountRepository.findById(studentId)
                .map(a -> a.getSchoolClass() != null && a.getSchoolClass().getId().equals(classId))
                .orElse(false);
    }

    private String csv(String v) {
        if (v == null) return "";
        String cleaned = v.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"");
        return '"' + cleaned + '"';
    }
}
