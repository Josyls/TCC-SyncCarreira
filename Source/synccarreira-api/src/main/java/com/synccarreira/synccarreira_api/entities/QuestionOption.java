package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_opcao_pergunta")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcao_pergunta")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "texto_opcao", nullable = false)
    @Getter
    @Setter
    private String optionText;

    @Column(name = "peso_humanas", nullable = false)
    @Getter
    @Setter
    private Double humanitiesWeight;

    @Column(name = "peso_biologicas", nullable = false)
    @Getter
    @Setter
    private Double biologicalSciencesWeight;

    @Column(name = "peso_exatas", nullable = false)
    @Getter
    @Setter
    private Double exactSciencesWeight;

    @Column(name = "peso_arte", nullable = false)
    @Getter
    @Setter
    private Double artsWeight;

    @ManyToOne
    @JoinColumn(name = "fk_pergunta", nullable = false)
    @Getter
    @Setter
    private Question question;
}
