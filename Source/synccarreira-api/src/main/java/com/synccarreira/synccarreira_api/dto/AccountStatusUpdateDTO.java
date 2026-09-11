package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.NotNull;

/** Ativar/desativar uma conta (RF-02). */
public record AccountStatusUpdateDTO(
        @NotNull(message = "Campo obrigatório")
        Boolean active,

        /** Justificativa — recomendada ao desativar. */
        String reason
) {
}
