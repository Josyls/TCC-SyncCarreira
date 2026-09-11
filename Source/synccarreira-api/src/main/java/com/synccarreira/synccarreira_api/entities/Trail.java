package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.entities.enums.TrailName;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_trilha")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Trail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trilha")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nome_trilha", nullable = false)
    @Getter
    @Setter
    private TrailName name;

    @Column(name = "ordem_sequencial_trilha", nullable = false)
    @Getter
    @Setter
    private Integer sequentialOrder;

    @OneToMany(mappedBy = "trail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<Question> questions = new ArrayList<>();

    public boolean isConcluded(List<Long> answeredIds) {
        return questions.stream()
                .allMatch(question -> answeredIds.contains(question.getId()));
    }
}
