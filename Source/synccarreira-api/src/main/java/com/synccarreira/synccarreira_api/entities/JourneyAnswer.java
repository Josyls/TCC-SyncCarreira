package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Resposta do aluno a uma pergunta dentro de uma jornada.
 * Substitui {@code tb_respostas_aluno} para o fluxo novo; conteúdo cifrado (RNF-02).
 */
@Entity
@Table(name = "tb_resposta_jornada")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JourneyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resposta_jornada")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_jornada", nullable = false)
    @Getter
    @Setter
    private Journey journey;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_pergunta", nullable = false)
    @Getter
    @Setter
    private Question question;

    /** Opção escolhida (para LIKERT/CHECKBOX/MÚLTIPLA). Nulo em pergunta ABERTA. */
    @ManyToOne
    @JoinColumn(name = "fk_opcao_pergunta")
    @Getter
    @Setter
    private QuestionOption questionOption;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "conteudo", length = 1000)
    @Getter
    @Setter
    private String content;

    @Column(name = "criada_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @Column(name = "atualizada_em")
    @Getter
    @Setter
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
