package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.enums.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Dados para cadastrar uma instituição (RF-01). CNPJ obrigatório. */
public record InstitutionInsertDTO(
        @NotBlank(message = "Campo obrigatório")
        String legalName,

        String tradeName,

        @NotBlank(message = "Campo obrigatório")
        String cnpj,

        @NotNull(message = "Campo obrigatório")
        InstitutionType type
) {
}
