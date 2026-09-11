package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

/** Aluno participante de uma sessão de orientação (RF-08). */
@Entity
@Table(name = "tb_sessao_participante")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participacao")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_sessao", nullable = false)
    @Getter
    @Setter
    private OrientationSession session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_aluno", nullable = false)
    @Getter
    @Setter
    private User student;
}
