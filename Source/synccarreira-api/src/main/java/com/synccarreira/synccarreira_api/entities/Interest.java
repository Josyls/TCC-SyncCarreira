package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

/** Área de interesse do catálogo (RF-14). */
@Entity
@Table(name = "tb_interesse")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interesse")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "nome_interesse", nullable = false, unique = true, length = 80)
    @Getter
    @Setter
    private String name;
}
