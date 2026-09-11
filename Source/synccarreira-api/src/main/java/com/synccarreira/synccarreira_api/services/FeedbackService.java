package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.psicologa.FeedbackDTOs;
import com.synccarreira.synccarreira_api.entities.Feedback;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.repositories.FeedbackRepository;
import com.synccarreira.synccarreira_api.repositories.JourneyRepository;
import com.synccarreira.synccarreira_api.repositories.OrientationSessionRepository;
import com.synccarreira.synccarreira_api.repositories.UserRepository;
import com.synccarreira.synccarreira_api.security.CurrentUser;
import com.synccarreira.synccarreira_api.security.PsychologistScope;
import com.synccarreira.synccarreira_api.services.exceptions.BusinessException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Feedback textual da psicóloga para o aluno (RF-09); leitura pelo aluno (RF-16). */
@Service
public class FeedbackService {

    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JourneyRepository journeyRepository;
    @Autowired private OrientationSessionRepository sessionRepository;
    @Autowired private CurrentUser currentUser;
    @Autowired private PsychologistScope scope;

    @Transactional
    public FeedbackDTOs.View create(FeedbackDTOs.Insert dto) {
        User student = userRepository.findById(dto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado. ID: " + dto.studentId()));
        if (!student.hasRole("ROLE_USER")) {
            throw new BusinessException("O feedback só pode ser enviado a um aluno.");
        }
        scope.assertStudentInScope(student.getId());

        Feedback fb = new Feedback();
        fb.setPsychologist(currentUser.user());
        fb.setStudent(student);
        fb.setText(dto.text().trim());
        fb.setCreatedAt(Instant.now());
        if (dto.journeyId() != null) {
            fb.setJourney(journeyRepository.findById(dto.journeyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Jornada não encontrada.")));
        }
        if (dto.sessionId() != null) {
            fb.setSession(sessionRepository.findById(dto.sessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada.")));
        }
        return new FeedbackDTOs.View(feedbackRepository.save(fb));
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTOs.View> listMineAsStudent() {
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(currentUser.userId()).stream()
                .map(FeedbackDTOs.View::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTOs.View> listSentByPsychologist() {
        return feedbackRepository.findByPsychologistIdOrderByCreatedAtDesc(currentUser.userId()).stream()
                .map(FeedbackDTOs.View::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTOs.View> listForStudent(Long studentId) {
        scope.assertStudentInScope(studentId);
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(FeedbackDTOs.View::new)
                .toList();
    }
}
