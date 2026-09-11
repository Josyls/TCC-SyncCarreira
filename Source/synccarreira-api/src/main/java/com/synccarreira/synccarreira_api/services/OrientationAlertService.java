package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.entities.Journey;
import com.synccarreira.synccarreira_api.entities.OrientationAlert;
import com.synccarreira.synccarreira_api.repositories.OrientationAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Alerta automático de necessidade de orientação (RF-04, RN-04).
 *
 * <p>Quando o aluno conclui a jornada mas sinaliza que ainda está "em dúvida",
 * é criado (ou reaberto) um alerta com prioridade para sessão em grupo — que
 * aparece no painel da psicóloga (Bloco 6).</p>
 */
@Service
public class OrientationAlertService {

    @Autowired
    private OrientationAlertRepository alertRepository;

    @Transactional
    public void onJourneyCompleted(Journey journey) {
        boolean emDuvida = Boolean.TRUE.equals(journey.getInDoubt());

        OrientationAlert alert = alertRepository.findByJourneyId(journey.getId()).orElse(null);

        if (!emDuvida) {
            // Aluno concluiu decidido: se havia alerta aberto para esta jornada, resolve.
            if (alert != null && !Boolean.TRUE.equals(alert.getResolved())) {
                alert.setResolved(true);
                alert.setResolvedAt(Instant.now());
                alertRepository.save(alert);
            }
            return;
        }

        if (alert == null) {
            alert = new OrientationAlert();
            alert.setStudent(journey.getStudent());
            alert.setJourney(journey);
            alert.setCreatedAt(Instant.now());
        }
        alert.setReason("EM_DUVIDA");
        alert.setPriority("GRUPO");
        alert.setResolved(false);
        alert.setResolvedBy(null);
        alert.setResolvedAt(null);
        alertRepository.save(alert);
    }
}
