package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Turma operacional vinculada a uma instituição (RF-01).
 * Pode ter uma psicóloga responsável.
 */
@Entity
@Table(name = "tb_turma")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turma")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "nome_turma", nullable = false)
    @Getter
    @Setter
    private String name;

    @Column(name = "ano_letivo", nullable = false)
    @Getter
    @Setter
    private Integer schoolYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_instituicao", nullable = false)
    @Getter
    @Setter
    private Institution institution;

    /** Psicóloga responsável pela turma (usuário com ROLE_PSICOLOGA). Opcional. */
    @ManyToOne
    @JoinColumn(name = "fk_psicologa")
    @Getter
    @Setter
    private User psychologist;

    @Column(name = "ativo", nullable = false)
    @Getter
    @Setter
    private Boolean active = true;

    @Column(name = "criado_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (active == null) {
            active = true;
        }
    }
}
