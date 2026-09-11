package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.journey.*;
import com.synccarreira.synccarreira_api.entities.*;
import com.synccarreira.synccarreira_api.entities.enums.JourneyStatus;
import com.synccarreira.synccarreira_api.entities.enums.JourneyTrailStatus;
import com.synccarreira.synccarreira_api.repositories.*;
import com.synccarreira.synccarreira_api.security.CurrentUser;
import com.synccarreira.synccarreira_api.services.exceptions.BusinessException;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Jornada do aluno pelas trilhas (RF-10, RF-11, RF-13, RF-15).
 *
 * Regras aplicadas no servidor:
 *  - RN-01: uma trilha só libera quando a anterior está CONCLUIDA.
 *  - RN-02: para concluir uma trilha é obrigatório responder todas as perguntas
 *           E registrar a síntese textual.
 *  - RN-08: o aluno tem acesso ao panorama completo do histórico.
 *  - A etapa "Síntese" (C3-B) é derivada: só abre com as 4 trilhas concluídas.
 */
@Service
public class JourneyService {

    @Autowired private CurrentUser currentUser;
    @Autowired private JourneyRepository journeyRepository;
    @Autowired private JourneyTrailRepository journeyTrailRepository;
    @Autowired private JourneyAnswerRepository journeyAnswerRepository;
    @Autowired private TrailSynthesisRepository trailSynthesisRepository;
    @Autowired private TrailRepository trailRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private QuestionOptionRepository questionOptionRepository;
    @Autowired private CareerSuggestionService careerSuggestionService;
    @Autowired private OrientationAlertService orientationAlertService;

    // ---------------------------------------------------------------- jornada

    @Transactional
    public JourneyViewDTO currentOrStart() {
        Long studentId = currentUser.userId();
        Journey journey = journeyRepository
                .findFirstByStudentIdAndStatusOrderByCycleDesc(studentId, JourneyStatus.EM_ANDAMENTO)
                .orElseGet(() -> startNew(currentUser.user()));
        return view(journey);
    }

    private Journey startNew(User student) {
        int cycle = journeyRepository.maxCycleForStudent(student.getId()) + 1;
        Journey journey = new Journey();
        journey.setStudent(student);
        journey.setCycle(cycle);
        journey.setStatus(JourneyStatus.EM_ANDAMENTO);
        journey.setStartedAt(Instant.now());
        journey = journeyRepository.save(journey);

        for (Trail trail : orderedTrails()) {
            JourneyTrail jt = new JourneyTrail();
            jt.setJourney(journey);
            jt.setTrail(trail);
            jt.setStatus(trail.getSequentialOrder() == 1
                    ? JourneyTrailStatus.LIBERADA : JourneyTrailStatus.BLOQUEADA);
            journeyTrailRepository.save(jt);
        }
        return journeyRepository.findById(journey.getId()).orElseThrow();
    }

    @Transactional(readOnly = true)
    public JourneyViewDTO view(Long journeyId) {
        return view(ownedJourney(journeyId));
    }

    private JourneyViewDTO view(Journey journey) {
        List<JourneyTrail> trails = journeyTrailRepository.findByJourneyId(journey.getId());
        trails.sort(Comparator.comparingInt(jt -> jt.getTrail().getSequentialOrder()));

        List<JourneyViewDTO.TrailProgressDTO> progress = new ArrayList<>();
        boolean allConcluded = !trails.isEmpty();
        for (JourneyTrail jt : trails) {
            long total = questionRepository.countByTrailId(jt.getTrail().getId());
            long answered = journeyAnswerRepository.countByJourneyAndTrail(journey.getId(), jt.getTrail().getId());
            boolean hasSynthesis = trailSynthesisRepository
                    .findByJourneyIdAndTrailId(journey.getId(), jt.getTrail().getId()).isPresent();
            if (!jt.isConcluded()) allConcluded = false;
            progress.add(new JourneyViewDTO.TrailProgressDTO(
                    jt.getTrail().getId(),
                    jt.getTrail().getName().name(),
                    jt.getTrail().getSequentialOrder(),
                    jt.getStatus().name(),
                    answered, total, hasSynthesis));
        }

        return new JourneyViewDTO(
                journey.getId(), journey.getCycle(), journey.getStatus(),
                journey.getInDoubt(), allConcluded, progress);
    }

    // ---------------------------------------------------------------- trilha

    @Transactional(readOnly = true)
    public TrailDetailDTO trailDetail(Long journeyId, Long trailId) {
        Journey journey = ownedJourney(journeyId);
        JourneyTrail jt = journeyTrail(journeyId, trailId);
        if (!jt.isUnlocked()) {
            throw new ForbiddenOperationException(
                    "Trilha bloqueada. Conclua a trilha anterior para liberar esta (RN-01).");
        }

        List<Question> questions = questionRepository.findByTrailId(trailId);
        questions.sort(Comparator.comparing(Question::getId));

        Map<Long, TrailDetailDTO.AnswerDTO> answers = new HashMap<>();
        for (JourneyAnswer a : journeyAnswerRepository.findByJourneyAndTrail(journeyId, trailId)) {
            answers.put(a.getQuestion().getId(), new TrailDetailDTO.AnswerDTO(
                    a.getQuestionOption() != null ? a.getQuestionOption().getId() : null,
                    a.getContent()));
        }

        List<TrailDetailDTO.QuestionDTO> qDtos = questions.stream().map(q -> {
            List<TrailDetailDTO.OptionDTO> opts = q.getOptions().stream()
                    .sorted(Comparator.comparing(QuestionOption::getId))
                    .map(o -> new TrailDetailDTO.OptionDTO(o.getId(), o.getOptionText()))
                    .toList();
            return new TrailDetailDTO.QuestionDTO(q.getId(), q.getContent(), q.getQuestionType().name(), opts);
        }).toList();

        String synthesis = trailSynthesisRepository.findByJourneyIdAndTrailId(journeyId, trailId)
                .map(TrailSynthesis::getText).orElse(null);

        // Trilha sem perguntas (ex.: Informação — RN-05, só links curados) pode ser
        // concluída apenas com a síntese; trilha com perguntas exige todas respondidas.
        boolean canConclude = questions.isEmpty() || answers.size() >= questions.size();

        return new TrailDetailDTO(
                journeyId, trailId, jt.getTrail().getName().name(), jt.getTrail().getSequentialOrder(),
                jt.getStatus().name(), canConclude, synthesis, qDtos, answers);
    }

    @Transactional
    public JourneyViewDTO saveAnswer(Long journeyId, JourneyRequests.SaveAnswer req) {
        Journey journey = ownedJourney(journeyId);
        requireOngoing(journey);

        Question question = questionRepository.findById(req.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta não encontrada. ID: " + req.questionId()));
        JourneyTrail jt = journeyTrail(journeyId, question.getTrail().getId());
        if (!jt.isUnlocked()) {
            throw new ForbiddenOperationException("Trilha bloqueada (RN-01).");
        }

        JourneyAnswer answer = journeyAnswerRepository
                .findByJourneyIdAndQuestionId(journeyId, question.getId())
                .orElseGet(() -> {
                    JourneyAnswer a = new JourneyAnswer();
                    a.setJourney(journey);
                    a.setQuestion(question);
                    a.setCreatedAt(Instant.now());
                    return a;
                });

        if (req.optionId() != null) {
            QuestionOption option = questionOptionRepository.findById(req.optionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Opção não encontrada. ID: " + req.optionId()));
            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new BusinessException("A opção informada não pertence a esta pergunta.");
            }
            answer.setQuestionOption(option);
        } else {
            answer.setQuestionOption(null);
        }
        answer.setContent(trimOrNull(req.content()));

        if (answer.getQuestionOption() == null && answer.getContent() == null) {
            throw new BusinessException("Informe uma opção ou um texto para a resposta.");
        }
        journeyAnswerRepository.save(answer);
        return view(journey);
    }

    @Transactional
    public JourneyViewDTO saveTrailSynthesis(Long journeyId, Long trailId, JourneyRequests.SaveSynthesis req) {
        Journey journey = ownedJourney(journeyId);
        requireOngoing(journey);
        JourneyTrail jt = journeyTrail(journeyId, trailId);
        if (!jt.isUnlocked()) {
            throw new ForbiddenOperationException("Trilha bloqueada (RN-01).");
        }
        String text = trimOrNull(req.text());
        if (text == null) {
            throw new BusinessException("A síntese é obrigatória para concluir a trilha (RN-02).");
        }

        long total = questionRepository.countByTrailId(trailId);
        long answered = journeyAnswerRepository.countByJourneyAndTrail(journeyId, trailId);
        if (total > 0 && answered < total) {
            throw new BusinessException(
                    "Responda todas as perguntas da trilha antes de registrar a síntese (RN-02).");
        }

        TrailSynthesis synthesis = trailSynthesisRepository.findByJourneyIdAndTrailId(journeyId, trailId)
                .orElseGet(() -> {
                    TrailSynthesis s = new TrailSynthesis();
                    s.setJourney(journey);
                    s.setTrail(jt.getTrail());
                    s.setCreatedAt(Instant.now());
                    return s;
                });
        synthesis.setText(text);
        trailSynthesisRepository.save(synthesis);

        if (jt.getStatus() != JourneyTrailStatus.CONCLUIDA) {
            jt.setStatus(JourneyTrailStatus.CONCLUIDA);
            jt.setConcludedAt(Instant.now());
            journeyTrailRepository.save(jt);
            unlockNext(journeyId, jt.getTrail().getSequentialOrder() + 1);
        }
        return view(journey);
    }

    private void unlockNext(Long journeyId, int nextOrder) {
        journeyTrailRepository.findByJourneyId(journeyId).stream()
                .filter(x -> x.getTrail().getSequentialOrder() == nextOrder
                        && x.getStatus() == JourneyTrailStatus.BLOQUEADA)
                .findFirst()
                .ifPresent(x -> {
                    x.setStatus(JourneyTrailStatus.LIBERADA);
                    journeyTrailRepository.save(x);
                });
    }

    // ---------------------------------------------------------------- etapa final

    @Transactional(readOnly = true)
    public FinalPhaseDTO finalPhase(Long journeyId) {
        return buildFinalPhase(ownedJourney(journeyId));
    }

    @Transactional
    public FinalPhaseDTO completeJourney(Long journeyId, JourneyRequests.CompleteJourney req) {
        Journey journey = ownedJourney(journeyId);
        if (journey.getStatus() == JourneyStatus.CONCLUIDA) {
            throw new BusinessException("Esta jornada já foi concluída.");
        }
        if (!allTrailsConcluded(journeyId)) {
            throw new ForbiddenOperationException(
                    "Conclua todas as trilhas antes da etapa de Síntese (RN-01).");
        }
        String text = trimOrNull(req.finalSynthesis());
        if (text == null) {
            throw new BusinessException("A síntese final é obrigatória (RN-02).");
        }
        journey.setFinalSynthesis(text);
        journey.setInDoubt(req.inDoubt());
        journey.setStatus(JourneyStatus.CONCLUIDA);
        journey.setFinishedAt(Instant.now());
        journeyRepository.save(journey);

        // RF-04 / RN-04 — aluno concluiu mas segue em dúvida → alerta automático + priorização.
        orientationAlertService.onJourneyCompleted(journey);

        return buildFinalPhase(journey);
    }

    private FinalPhaseDTO buildFinalPhase(Journey journey) {
        if (!allTrailsConcluded(journey.getId())) {
            throw new ForbiddenOperationException(
                    "A etapa de Síntese abre apenas após concluir todas as trilhas (RN-01).");
        }

        Map<Long, Trail> trailById = new HashMap<>();
        orderedTrails().forEach(t -> trailById.put(t.getId(), t));

        List<FinalPhaseDTO.TrailSynthesisDTO> syntheses = trailSynthesisRepository.findByJourneyId(journey.getId())
                .stream()
                .sorted(Comparator.comparingInt(s -> s.getTrail().getSequentialOrder()))
                .map(s -> new FinalPhaseDTO.TrailSynthesisDTO(
                        s.getTrail().getSequentialOrder(), s.getTrail().getName().name(), s.getText()))
                .toList();

        List<FinalPhaseDTO.CareerSuggestionDTO> suggestions =
                careerSuggestionService.suggestForJourney(journey);

        return new FinalPhaseDTO(
                journey.getId(),
                journey.getStatus() == JourneyStatus.CONCLUIDA,
                journey.getFinalSynthesis(),
                journey.getInDoubt(),
                syntheses,
                suggestions,
                "Este leque é um ponto de partida para a sua reflexão, não um diagnóstico "
                        + "fechado de carreira. Converse com a sua orientadora (RN-07).");
    }

    // ---------------------------------------------------------------- panorama / histórico

    @Transactional(readOnly = true)
    public PanoramaDTO panorama(Long journeyId) {
        Journey journey = ownedJourney(journeyId);

        List<JourneyTrail> jts = journeyTrailRepository.findByJourneyId(journeyId);
        jts.sort(Comparator.comparingInt(jt -> jt.getTrail().getSequentialOrder()));

        List<PanoramaDTO.TrailBlockDTO> blocks = new ArrayList<>();
        for (JourneyTrail jt : jts) {
            Long trailId = jt.getTrail().getId();
            List<Question> questions = questionRepository.findByTrailId(trailId);
            questions.sort(Comparator.comparing(Question::getId));

            Map<Long, JourneyAnswer> answerByQ = new HashMap<>();
            journeyAnswerRepository.findByJourneyAndTrail(journeyId, trailId)
                    .forEach(a -> answerByQ.put(a.getQuestion().getId(), a));

            List<PanoramaDTO.ItemDTO> items = questions.stream().map(q -> {
                JourneyAnswer a = answerByQ.get(q.getId());
                return new PanoramaDTO.ItemDTO(
                        q.getContent(),
                        a != null && a.getQuestionOption() != null ? a.getQuestionOption().getOptionText() : null,
                        a != null ? a.getContent() : null);
            }).toList();

            String synthesis = trailSynthesisRepository.findByJourneyIdAndTrailId(journeyId, trailId)
                    .map(TrailSynthesis::getText).orElse(null);

            blocks.add(new PanoramaDTO.TrailBlockDTO(
                    jt.getTrail().getSequentialOrder(), jt.getTrail().getName().name(), synthesis, items));
        }

        return new PanoramaDTO(
                journey.getId(), journey.getCycle(), journey.getStatus().name(), journey.getInDoubt(),
                journey.getStartedAt(), journey.getFinishedAt(), journey.getFinalSynthesis(), blocks);
    }

    @Transactional(readOnly = true)
    public List<JourneyHistoryItemDTO> myHistory() {
        Long studentId = currentUser.userId();
        return journeyRepository.findByStudentIdOrderByCycleDesc(studentId).stream()
                .map(j -> {
                    List<JourneyTrail> jts = journeyTrailRepository.findByJourneyId(j.getId());
                    long concluded = jts.stream().filter(JourneyTrail::isConcluded).count();
                    return new JourneyHistoryItemDTO(
                            j.getId(), j.getCycle(), j.getStatus().name(), j.getInDoubt(),
                            concluded, jts.size(), j.getStartedAt(), j.getFinishedAt());
                })
                .toList();
    }

    // ---------------------------------------------------------------- helpers

    private List<Trail> orderedTrails() {
        return trailRepository.findAll(Sort.by(Sort.Direction.ASC, "sequentialOrder"));
    }

    private Journey ownedJourney(Long journeyId) {
        Journey journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new ResourceNotFoundException("Jornada não encontrada. ID: " + journeyId));
        if (!journey.getStudent().getId().equals(currentUser.userId())) {
            throw new ForbiddenOperationException("Esta jornada pertence a outro aluno (RNF-01).");
        }
        return journey;
    }

    private JourneyTrail journeyTrail(Long journeyId, Long trailId) {
        return journeyTrailRepository.findByJourneyIdAndTrailId(journeyId, trailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trilha não faz parte desta jornada. Trilha ID: " + trailId));
    }

    private boolean allTrailsConcluded(Long journeyId) {
        List<JourneyTrail> jts = journeyTrailRepository.findByJourneyId(journeyId);
        return !jts.isEmpty() && jts.stream().allMatch(JourneyTrail::isConcluded);
    }

    private void requireOngoing(Journey journey) {
        if (journey.getStatus() == JourneyStatus.CONCLUIDA) {
            throw new BusinessException("Jornada concluída — não é possível alterar respostas ou sínteses.");
        }
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
