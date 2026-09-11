package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Alerta automático de necessidade de orientação (RF-04).
 * Criado quando o aluno conclui a jornada mas segue "em dúvida" (RN-04),
 * com prioridade para sessão em grupo.
 */
@Entity
@Table(name = "tb_alerta_orientacao")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrientationAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_aluno", nullable = false)
    @Getter
    @Setter
    private User student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_jornada", nullable = false)
    @Getter
    @Setter
    private Journey journey;

    @Column(name = "motivo", nullable = false, length = 30)
    @Getter
    @Setter
    private String reason = "EM_DUVIDA";

    /** GRUPO | INDIVIDUAL */
    @Column(name = "prioridade", nullable = false, length = 20)
    @Getter
    @Setter
    private String priority = "GRUPO";

    @Column(name = "resolvido", nullable = false)
    @Getter
    @Setter
    private Boolean resolved = false;

    @ManyToOne
    @JoinColumn(name = "resolvido_por")
    @Getter
    @Setter
    private User resolvedBy;

    @Column(name = "resolvido_em")
    @Getter
    @Setter
    private Instant resolvedAt;

    @Column(name = "criado_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (resolved == null) resolved = false;
    }
}
