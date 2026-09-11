package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Link de fonte externa confiável, curado pela psicóloga, exibido na trilha de
 * Informação (RF-06, RN-05).
 */
@Entity
@Table(name = "tb_link_curado")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CuratedLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_link")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_psicologa", nullable = false)
    @Getter
    @Setter
    private User psychologist;

    /** Nula = disponível para todas as instituições. */
    @ManyToOne
    @JoinColumn(name = "fk_instituicao")
    @Getter
    @Setter
    private Institution institution;

    @Column(name = "titulo", nullable = false, length = 200)
    @Getter
    @Setter
    private String title;

    @Column(name = "url", nullable = false, length = 500)
    @Getter
    @Setter
    private String url;

    @Column(name = "descricao", length = 400)
    @Getter
    @Setter
    private String description;

    /** ENEM | PROUNI | SISU | COTAS | CURSOS | OUTRO */
    @Column(name = "categoria", nullable = false, length = 20)
    @Getter
    @Setter
    private String category;

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
        if (createdAt == null) createdAt = Instant.now();
        if (active == null) active = true;
    }
}
