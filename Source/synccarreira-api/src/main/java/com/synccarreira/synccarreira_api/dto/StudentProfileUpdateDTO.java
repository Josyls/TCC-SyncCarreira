package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/** Atualização do perfil do aluno (RF-14). */
public record StudentProfileUpdateDTO(
        @Size(max = 2000, message = "Máximo de 2000 caracteres")
        String bio,

        @Size(max = 20, message = "Telefone inválido")
        String phone,

        @Size(max = 120, message = "Máximo de 120 caracteres")
        String city,

        List<Long> interestIds
) {
}
