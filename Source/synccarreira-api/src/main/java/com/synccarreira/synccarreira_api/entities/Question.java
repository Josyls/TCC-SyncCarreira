package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.entities.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_pergunta")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pergunta")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "enunciado_pergunta", nullable = false, length = 500)
    @Getter
    @Setter
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pergunta", nullable = false)
    @Getter
    @Setter
    private QuestionType questionType;

    @ManyToOne
    @JoinColumn(name = "fk_trilha", nullable = false)
    @Getter
    @Setter
    private Trail trail;

    @ManyToOne
    @JoinColumn(name = "fk_psicologa")
    @Getter
    @Setter
    private Psychologist psychologist;

    // Opções só existem para perguntas que não são do tipo ABERTA
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<QuestionOption> options = new ArrayList<>();

    public boolean acceptsOptions() {
        return questionType != null && questionType != QuestionType.ABERTA;
    }
}
