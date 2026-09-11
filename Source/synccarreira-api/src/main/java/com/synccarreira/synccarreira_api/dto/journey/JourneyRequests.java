package com.synccarreira.synccarreira_api.dto.journey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payloads de escrita da jornada. */
public final class JourneyRequests {

    private JourneyRequests() {
    }

    /** Salvar/atualizar a resposta de uma pergunta. */
    public record SaveAnswer(
            @NotNull(message = "Campo obrigatório") Long questionId,
            Long optionId,
            String content
    ) {
    }

    /** Registrar/atualizar a síntese de uma trilha (RF-11, RN-02). */
    public record SaveSynthesis(
            @NotBlank(message = "A síntese é obrigatória") String text
    ) {
    }

    /** Concluir a jornada na etapa final de Síntese. */
    public record CompleteJourney(
            @NotBlank(message = "A síntese final é obrigatória") String finalSynthesis,
            @NotNull(message = "Informe se você ainda está em dúvida") Boolean inDoubt
    ) {
    }
}
