package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_agendamento")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agendamento")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "data_horario")
    @Getter
    @Setter
    private LocalDateTime dateTime;

    @Column(name = "tipo_agendamento")
    @Getter
    @Setter
    private String scheduleType;

    @Column(name = "status_agendamento")
    @Getter
    @Setter
    private String scheduleStatus;

    @ManyToOne
    @JoinColumn(name = "fk_aluno")
    @Getter
    @Setter
    private Student student;

    @ManyToOne
    @JoinColumn(name = "fk_psicologa")
    @Getter
    @Setter
    private Psychologist psychologist;

}
