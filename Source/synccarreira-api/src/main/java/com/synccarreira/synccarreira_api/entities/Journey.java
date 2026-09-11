package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.crypto.EncryptedStringConverter;
import com.synccarreira.synccarreira_api.entities.enums.JourneyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Um ciclo do aluno pela jornada de trilhas (RF-10, RF-15).
 * {@code finalSynthesis} é a síntese consolidada da etapa final (C3-B), cifrada (RNF-02).
 */
@Entity
@Table(name = "tb_jornada")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jornada")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_aluno", nullable = false)
    @Getter
    @Setter
    private User student;

    @Column(name = "ciclo", nullable = false)
    @Getter
    @Setter
    private Integer cycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_jornada", nullable = false, length = 20)
    @Getter
    @Setter
    private JourneyStatus status = JourneyStatus.EM_ANDAMENTO;

    /** Preenchido na conclusão: o aluno sinaliza se ainda está em dúvida (RF-04, RN-04). */
    @Column(name = "em_duvida")
    @Getter
    @Setter
    private Boolean inDoubt;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "sintese_final", length = 4000)
    @Getter
    @Setter
    private String finalSynthesis;

    @Column(name = "iniciada_em", nullable = false)
    @Getter
    @Setter
    private Instant startedAt;

    @Column(name = "concluida_em")
    @Getter
    @Setter
    private Instant finishedAt;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<JourneyTrail> journeyTrails = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (startedAt == null) startedAt = Instant.now();
        if (status == null) status = JourneyStatus.EM_ANDAMENTO;
    }
}
