package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Dados para o administrador criar uma conta de aluno ou psicóloga (RF-02).
 * CPF obrigatório e único.
 */
public record ManagedUserInsertDTO(
        @NotBlank(message = "Campo obrigatório")
        String name,

        @NotBlank(message = "Campo obrigatório")
        @Email(message = "Favor entrar com email válido")
        String email,

        @NotBlank(message = "Campo obrigatório")
        @Size(min = 8, message = "Deve ter no mínimo 8 caracteres")
        String password,

        @NotBlank(message = "Campo obrigatório")
        String cpf,

        @NotBlank(message = "Informe ALUNO ou PSICOLOGA")
        @Pattern(regexp = "ALUNO|PSICOLOGA", message = "Perfil deve ser ALUNO ou PSICOLOGA")
        String perfil,

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
