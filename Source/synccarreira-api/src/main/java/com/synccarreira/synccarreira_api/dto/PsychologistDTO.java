package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Psychologist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public record PsychologistDTO(
        Long id,

        @NotBlank(message = "Campo obrigatório")
        String name,

        @Email(message = "Favor entrar com email válido")
        String email,

        @NotBlank(message = "Campo obrigatório")
        String crp,

        @NotNull(message = "Campo obrigatório")
        LocalDate contractExpirationDate,

        boolean isContractValid,

        Set<RoleDTO> roles
) {

    public PsychologistDTO(Psychologist entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCrp(),
                entity.getContractExpirationDate(),
                entity.isContractValid(),
                new HashSet<>()
        );

        entity.getRoles().forEach(role -> this.roles.add(new RoleDTO(role)));
    }
}