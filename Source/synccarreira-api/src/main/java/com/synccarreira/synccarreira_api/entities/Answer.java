package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_respostas_aluno")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resposta")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "conteudo")
    @Getter
    @Setter
    private String content;

    @ManyToOne
    @JoinColumn(name = "fk_usuario")
    @Getter
    @Setter
    private Student student;

    @ManyToOne
    @JoinColumn(name = "fk_opcao_pergunta")
    @Getter
    @Setter
    private QuestionOption questionOption;

}
