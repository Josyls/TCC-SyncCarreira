package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Síntese textual livre registrada pelo aluno ao concluir uma trilha (RF-11, RN-02).
 * Obrigatória para concluir a trilha. Texto cifrado (RNF-02).
 */
@Entity
@Table(name = "tb_sintese_trilha")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TrailSynthesis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sintese_trilha")
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
    @JoinColumn(name = "fk_trilha", nullable = false)
    @Getter
    @Setter
    private Trail trail;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "texto_sintese", nullable = false, length = 4000)
    @Getter
    @Setter
    private String text;

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
