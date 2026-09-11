package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.psicologa.SessionDTOs;
import com.synccarreira.synccarreira_api.entities.OrientationSession;
import com.synccarreira.synccarreira_api.entities.SessionParticipant;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.entities.enums.SessionStatus;
import com.synccarreira.synccarreira_api.entities.enums.SessionType;
import com.synccarreira.synccarreira_api.repositories.OrientationSessionRepository;
import com.synccarreira.synccarreira_api.repositories.UserRepository;
import com.synccarreira.synccarreira_api.security.CurrentUser;
import com.synccarreira.synccarreira_api.security.PsychologistScope;
import com.synccarreira.synccarreira_api.services.exceptions.BusinessException;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Agendamento de sessões de orientação (RF-08); área do aluno (RF-16). */
@Service
public class SessionService {

    @Autowired private OrientationSessionRepository sessionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrentUser currentUser;
    @Autowired private PsychologistScope scope;
    @Autowired private NotificationService notificationService;

    // ---------------------------------------------------------------- psicóloga

    @Transactional(readOnly = true)
    public List<SessionDTOs.View> listMine() {
        return sessionRepository.findByPsychologistIdOrderByStartDesc(currentUser.userId()).stream()
                .map(SessionDTOs.View::new)
                .toList();
    }

    @Transactional
    public SessionDTOs.View create(SessionDTOs.Insert dto) {
        validateInterval(dto.start(), dto.end());
        OrientationSession session = new OrientationSession();
        session.setPsychologist(currentUser.user());
        session.setType(SessionType.valueOf(dto.type()));
        session.setTitle(dto.title().trim());
        session.setDescription(nullable(dto.description()));
        session.setStart(dto.start());
        session.setEnd(dto.end());
        session.setLocation(nullable(dto.location()));
        session.setStatus(SessionStatus.AGENDADA);

        List<User> students = resolveStudents(dto.studentIds());
        if (session.getType() == SessionType.INDIVIDUAL && students.size() != 1) {
            throw new BusinessException("Sessão individual deve ter exatamente um aluno.");
        }
        for (User student : students) {
            SessionParticipant p = new SessionParticipant();
            p.setSession(session);
            p.setStudent(student);
            session.getParticipants().add(p);
        }

        session = sessionRepository.save(session);
        notificationService.notifySessionScheduled(session, students);
        return new SessionDTOs.View(session);
    }

    @Transactional
    public SessionDTOs.View update(Long id, SessionDTOs.Update dto) {
        OrientationSession session = owned(id);
        validateInterval(dto.start(), dto.end());
        session.setTitle(dto.title().trim());
        session.setDescription(nullable(dto.description()));
        session.setStart(dto.start());
        session.setEnd(dto.end());
        session.setLocation(nullable(dto.location()));
        SessionStatus newStatus = SessionStatus.valueOf(dto.status());
        boolean cancelling = newStatus == SessionStatus.CANCELADA && session.getStatus() != SessionStatus.CANCELADA;
        session.setStatus(newStatus);

        if (dto.studentIds() != null) {
            session.getParticipants().clear();
            for (User student : resolveStudents(dto.studentIds())) {
                SessionParticipant p = new SessionParticipant();
                p.setSession(session);
                p.setStudent(student);
                session.getParticipants().add(p);
            }
        }

        session = sessionRepository.save(session);
        List<User> students = session.getParticipants().stream().map(SessionParticipant::getStudent).toList();
        if (cancelling) {
            notificationService.notifySessionCancelled(session, students);
        } else {
            notificationService.notifySessionScheduled(session, students);
        }
        return new SessionDTOs.View(session);
    }

    @Transactional
    public void delete(Long id) {
        OrientationSession session = owned(id);
        sessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public String ics(Long id) {
        OrientationSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada. ID: " + id));
        boolean isOwner = session.getPsychologist().getId().equals(currentUser.userId());
        boolean isParticipant = session.getParticipants().stream()
                .anyMatch(p -> p.getStudent().getId().equals(currentUser.userId()));
        if (!isOwner && !isParticipant && !currentUser.isAdmin()) {
            throw new ForbiddenOperationException("Você não participa desta sessão.");
        }
        return notificationService.buildIcs(session);
    }

    // ---------------------------------------------------------------- aluno (RF-16)

    @Transactional(readOnly = true)
    public List<SessionDTOs.View> listForCurrentStudent() {
        return sessionRepository.findForStudent(currentUser.userId()).stream()
                .map(SessionDTOs.View::new)
                .toList();
    }

    // ---------------------------------------------------------------- helpers

    private OrientationSession owned(Long id) {
        OrientationSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada. ID: " + id));
        if (!currentUser.isAdmin() && !session.getPsychologist().getId().equals(currentUser.userId())) {
            throw new ForbiddenOperationException("Esta sessão foi criada por outra psicóloga.");
        }
        return session;
    }

    private List<User> resolveStudents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("Selecione ao menos um aluno.");
        }
        List<User> students = userRepository.findAllById(ids);
        if (students.size() != ids.size()) {
            throw new ResourceNotFoundException("Um ou mais alunos não foram encontrados.");
        }
        for (User s : students) {
            if (!s.hasRole("ROLE_USER")) {
                throw new BusinessException("Apenas alunos podem participar de sessões.");
            }
            scope.assertStudentInScope(s.getId());
        }
        return students;
    }

    private void validateInterval(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessException("O horário de término deve ser depois do início.");
        }
    }

    private String nullable(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
