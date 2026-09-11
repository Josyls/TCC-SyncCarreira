package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Dados para editar uma turma (RF-01). */
public record SchoolClassUpdateDTO(
        @NotBlank(message = "Campo obrigatório")
        String name,

        @NotNull(message = "Campo obrigatório")
        @Positive(message = "Ano letivo inválido")
        Integer schoolYear,

        Long psychologistId,

        @NotNull(message = "Campo obrigatório")
        Boolean active
) {
}
