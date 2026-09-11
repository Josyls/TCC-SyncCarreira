package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.enums.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Dados para editar uma instituição (RF-01). O CNPJ pode ser corrigido. */
public record InstitutionUpdateDTO(
        @NotBlank(message = "Campo obrigatório")
        String legalName,

        String tradeName,

        @NotBlank(message = "Campo obrigatório")
        String cnpj,

        @NotNull(message = "Campo obrigatório")
        InstitutionType type,

        @NotNull(message = "Campo obrigatório")
        Boolean active
) {
}
