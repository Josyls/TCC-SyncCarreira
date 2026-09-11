package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Item do catálogo fixo área → carreira/curso (RF-12, RN-09).
 * Cada item pode ser validado pela psicóloga.
 */
@Entity
@Table(name = "tb_area_carreira")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CareerArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area_carreira")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    /** HUMANAS | EXATAS | BIOLOGICAS | ARTES */
    @Column(name = "area", nullable = false, length = 20)
    @Getter
    @Setter
    private String area;

    @Column(name = "titulo", nullable = false, length = 150)
    @Getter
    @Setter
    private String title;

    /** CARREIRA | CURSO */
    @Column(name = "tipo", nullable = false, length = 20)
    @Getter
    @Setter
    private String type;

    @Column(name = "descricao", length = 400)
    @Getter
    @Setter
    private String description;

    @Column(name = "validado", nullable = false)
    @Getter
    @Setter
    private Boolean validated = false;

    @ManyToOne
    @JoinColumn(name = "fk_validado_por")
    @Getter
    @Setter
    private User validatedBy;

    @Column(name = "validado_em")
    @Getter
    @Setter
    private Instant validatedAt;

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
        if (validated == null) validated = false;
        if (active == null) active = true;
    }
}
