package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.services.validation.UserUpdateValid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
public class PsychologistUpdateDTO {

    @NotBlank(message = "Campo obrigatório")
    @Getter
    private String name;

    @Email(message = "Favor entrar com email válido")
    @Getter
    private String email;

    @Getter
    private Long roleId;

    @NotBlank(message = "Campo obrigatório")
    @Getter
    private String crp;

    @NotNull(message = "Campo obrigatório")
    @Getter
    private LocalDate contractExpirationDate;
}