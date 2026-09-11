package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.entities.OrientationSession;
import com.synccarreira.synccarreira_api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Notificação de sessões (RF-08).
 *
 * <p>Decisão C5: em vez de integrar a Google Calendar API (que exigiria
 * credenciais e chamadas externas), cada sessão gera um convite no padrão
 * iCalendar (.ics) — que abre no Google Calendar, Outlook, etc. — e a
 * notificação ao aluno é registrada em log (mock de e-mail). A interface abaixo
 * isola essa escolha: trocar por um provedor real é adicionar uma implementação.</p>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter ICS = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    /** Monta o conteúdo .ics (VCALENDAR/VEVENT) da sessão. */
    public String buildIcs(OrientationSession s) {
        String uid = "sessao-" + s.getId() + "@synccarreira";
        String dtstamp = LocalDateTime.now(ZoneOffset.UTC).format(STAMP);
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n")
          .append("VERSION:2.0\r\n")
          .append("PRODID:-//SyncCarreira//Orientacao Vocacional//PT-BR\r\n")
          .append("CALSCALE:GREGORIAN\r\n")
          .append("METHOD:PUBLISH\r\n")
          .append("BEGIN:VEVENT\r\n")
          .append("UID:").append(uid).append("\r\n")
          .append("DTSTAMP:").append(dtstamp).append("\r\n")
          .append("DTSTART:").append(s.getStart().format(ICS)).append("\r\n")
          .append("DTEND:").append(s.getEnd().format(ICS)).append("\r\n")
          .append("SUMMARY:").append(escape(s.getTitle())).append("\r\n");
        if (s.getDescription() != null && !s.getDescription().isBlank()) {
            sb.append("DESCRIPTION:").append(escape(s.getDescription())).append("\r\n");
        }
        if (s.getLocation() != null && !s.getLocation().isBlank()) {
            sb.append("LOCATION:").append(escape(s.getLocation())).append("\r\n");
        }
        sb.append("STATUS:").append(s.getStatus().name().equals("CANCELADA") ? "CANCELLED" : "CONFIRMED").append("\r\n")
          .append("END:VEVENT\r\n")
          .append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    /** Registra a notificação de agendamento para os alunos (mock de e-mail). */
    public void notifySessionScheduled(OrientationSession s, List<User> students) {
        for (User student : students) {
            log.info("[NOTIFICACAO] Sessao '{}' ({} {}-{}) agendada para {} <{}>. Convite .ics disponivel em /appointments/{}/ics",
                    s.getTitle(), s.getStart().toLocalDate(), s.getStart().toLocalTime(), s.getEnd().toLocalTime(),
                    student.getName(), student.getEmail(), s.getId());
        }
    }

    public void notifySessionCancelled(OrientationSession s, List<User> students) {
        for (User student : students) {
            log.info("[NOTIFICACAO] Sessao '{}' CANCELADA — avisar {} <{}>", s.getTitle(), student.getName(), student.getEmail());
        }
    }

    private String escape(String v) {
        return v.replace("\\", "\\\\").replace("\n", "\\n").replace(",", "\\,").replace(";", "\\;");
    }
}
