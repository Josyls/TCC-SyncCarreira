package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.entities.enums.JourneyTrailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Progresso de uma trilha dentro de uma jornada (RN-01). */
@Entity
@Table(name = "tb_jornada_trilha")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JourneyTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jornada_trilha")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status_trilha", nullable = false, length = 20)
    @Getter
    @Setter
    private JourneyTrailStatus status = JourneyTrailStatus.BLOQUEADA;

    @Column(name = "concluida_em")
    @Getter
    @Setter
    private Instant concludedAt;

    public boolean isUnlocked() {
        return status == JourneyTrailStatus.LIBERADA || status == JourneyTrailStatus.CONCLUIDA;
    }

    public boolean isConcluded() {
        return status == JourneyTrailStatus.CONCLUIDA;
    }
}
