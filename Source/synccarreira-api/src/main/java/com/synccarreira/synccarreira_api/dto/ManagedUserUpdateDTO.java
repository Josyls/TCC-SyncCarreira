package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/** Dados para o administrador editar uma conta gerenciada (RF-02). Sem troca de perfil. */
public record ManagedUserUpdateDTO(
        @NotBlank(message = "Campo obrigatório")
        String name,

        @NotBlank(message = "Campo obrigatório")
        @Email(message = "Favor entrar com email válido")
        String email,

        @NotBlank(message = "Campo obrigatório")
        String cpf,

        Long institutionId,
        Long schoolClassId,

        // Aluno
        String schoolYear,
        String schoolType,

        // Psicóloga
        String crp,
        LocalDate contractExpirationDate
) {
}
