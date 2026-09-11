package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Feedback textual da psicóloga para o aluno (RF-09). Texto cifrado (RNF-02). */
@Entity
@Table(name = "tb_feedback")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feedback")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_psicologa", nullable = false)
    @Getter
    @Setter
    private User psychologist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_aluno", nullable = false)
    @Getter
    @Setter
    private User student;

    @ManyToOne
    @JoinColumn(name = "fk_jornada")
    @Getter
    @Setter
    private Journey journey;

    @ManyToOne
    @JoinColumn(name = "fk_sessao")
    @Getter
    @Setter
    private OrientationSession session;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "texto", nullable = false, length = 4000)
    @Getter
    @Setter
    private String text;

    @Column(name = "criado_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
