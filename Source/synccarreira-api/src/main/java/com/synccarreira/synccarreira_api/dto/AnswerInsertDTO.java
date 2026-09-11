package com.synccarreira.synccarreira_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AnswerInsertDTO {

    @NotBlank(message = "Campo obrigatório")
    @Getter
    private String content;

    @NotNull(message = "O estudante associado é obrigatório")
    @Getter
    private Long studentId;

    @NotNull(message = "A opção escolhida é obrigatória")
    @Getter
    private Long questionOptionId;
}