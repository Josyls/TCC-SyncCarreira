package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.services.validation.UserUpdateValid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@UserUpdateValid
public class UserUpdateDTO {

    @NotBlank(message = "Campo obrigatório")
    @Getter
    private String name;

    @Email(message = "Favor entrar com email válido")
    @Getter
    private String email;

    @Getter
    private Long roleId;
}
