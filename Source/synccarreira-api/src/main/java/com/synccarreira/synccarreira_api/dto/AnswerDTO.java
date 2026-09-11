package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Answer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {

    @Getter
    private Long id;

    @NotBlank(message = "Campo obrigatório")
    @Getter
    private String content;

    @NotNull(message = "O estudante associado é obrigatório")
    @Getter
    private StudentDTO student;

    @NotNull(message = "A opção escolhida é obrigatória")
    @Getter
    private QuestionOptionDTO questionOptionDTO;

    public AnswerDTO(Answer entity) {
        this.id = entity.getId();
        this.content = entity.getContent();

        if (entity.getStudent() != null) {
            this.student = new StudentDTO(entity.getStudent());
        }

        if (entity.getQuestionOption() != null) {
            this.questionOptionDTO = new QuestionOptionDTO(entity.getQuestionOption());
        }
    }
}