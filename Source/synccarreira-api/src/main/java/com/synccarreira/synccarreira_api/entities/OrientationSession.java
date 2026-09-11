package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.entities.enums.SessionStatus;
import com.synccarreira.synccarreira_api.entities.enums.SessionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Sessão de orientação individual ou em grupo (RF-08). */
@Entity
@Table(name = "tb_sessao")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrientationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessao")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_psicologa", nullable = false)
    @Getter
    @Setter
    private User psychologist;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sessao", nullable = false, length = 15)
    @Getter
    @Setter
    private SessionType type;

    @Column(name = "titulo", nullable = false, length = 200)
    @Getter
    @Setter
    private String title;

    @Column(name = "descricao", length = 1000)
    @Getter
    @Setter
    private String description;

    @Column(name = "inicio", nullable = false)
    @Getter
    @Setter
    private LocalDateTime start;

    @Column(name = "fim", nullable = false)
    @Getter
    @Setter
    private LocalDateTime end;

    @Column(name = "local_sessao", length = 300)
    @Getter
    @Setter
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_sessao", nullable = false, length = 15)
    @Getter
    @Setter
    private SessionStatus status = SessionStatus.AGENDADA;

    @Column(name = "criado_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @Column(name = "atualizado_em")
    @Getter
    @Setter
    private Instant updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private List<SessionParticipant> participants = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = SessionStatus.AGENDADA;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
