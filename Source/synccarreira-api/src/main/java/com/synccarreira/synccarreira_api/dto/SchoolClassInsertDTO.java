package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Dados para criar uma turma vinculada a uma instituição (RF-01). */
public record SchoolClassInsertDTO(
        @NotBlank(message = "Campo obrigatório")
        String name,

        @NotNull(message = "Campo obrigatório")
        @Positive(message = "Ano letivo inválido")
        Integer schoolYear,

        @NotNull(message = "Campo obrigatório")
        Long institutionId,

        /** Opcional — psicóloga responsável pela turma. */
        Long psychologistId
) {
}
