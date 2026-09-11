package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.journey.FinalPhaseDTO;
import com.synccarreira.synccarreira_api.entities.CareerArea;
import com.synccarreira.synccarreira_api.entities.Journey;
import com.synccarreira.synccarreira_api.entities.JourneyAnswer;
import com.synccarreira.synccarreira_api.entities.QuestionOption;
import com.synccarreira.synccarreira_api.repositories.CareerAreaRepository;
import com.synccarreira.synccarreira_api.repositories.JourneyAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Monta o "leque" de áreas e carreiras sugeridas ao final da jornada (RF-12).
 *
 * <p>Soma os pesos por área das opções escolhidas pelo aluno, ordena as áreas e
 * devolve itens do catálogo das áreas de maior afinidade. É sempre um leque
 * multi-área — nunca um diagnóstico fechado (RN-07).</p>
 */
@Service
public class CareerSuggestionService {

    /** Quantas áreas entram no leque. */
    private static final int TOP_AREAS = 3;
    /** Itens por área no leque. */
    private static final int ITEMS_PER_AREA = 3;

    @Autowired private JourneyAnswerRepository journeyAnswerRepository;
    @Autowired private CareerAreaRepository careerAreaRepository;

    @Transactional(readOnly = true)
    public List<FinalPhaseDTO.CareerSuggestionDTO> suggestForJourney(Journey journey) {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("HUMANAS", 0.0);
        scores.put("EXATAS", 0.0);
        scores.put("BIOLOGICAS", 0.0);
        scores.put("ARTES", 0.0);

        for (JourneyAnswer answer : journeyAnswerRepository.findByJourneyId(journey.getId())) {
            QuestionOption o = answer.getQuestionOption();
            if (o == null) continue;
            scores.merge("HUMANAS", nz(o.getHumanitiesWeight()), Double::sum);
            scores.merge("EXATAS", nz(o.getExactSciencesWeight()), Double::sum);
            scores.merge("BIOLOGICAS", nz(o.getBiologicalSciencesWeight()), Double::sum);
            scores.merge("ARTES", nz(o.getArtsWeight()), Double::sum);
        }

        List<String> orderedAreas = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        boolean noSignal = scores.values().stream().allMatch(v -> v == 0.0);
        List<String> chosen = noSignal
                ? new ArrayList<>(scores.keySet())          // sem sinal: mostra todas as áreas
                : orderedAreas.subList(0, Math.min(TOP_AREAS, orderedAreas.size()));

        List<FinalPhaseDTO.CareerSuggestionDTO> leque = new ArrayList<>();
        for (String area : chosen) {
            careerAreaRepository.findByAreaAndActiveTrue(area).stream()
                    .sorted(Comparator.comparing((CareerArea c) -> !Boolean.TRUE.equals(c.getValidated()))
                            .thenComparing(CareerArea::getTitle, String.CASE_INSENSITIVE_ORDER))
                    .limit(ITEMS_PER_AREA)
                    .forEach(c -> leque.add(new FinalPhaseDTO.CareerSuggestionDTO(
                            c.getArea(), c.getTitle(), c.getType(), c.getDescription())));
        }
        return leque;
    }

    private double nz(Double d) {
        return d == null ? 0.0 : d;
    }
}
