package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.entities.enums.InstitutionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Instituição parceira — escola ou ONG (RF-01).
 * CNPJ obrigatório e único (validações da seção 7 do documento de requisitos).
 */
@Entity
@Table(name = "tb_instituicao")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instituicao")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Column(name = "razao_social", nullable = false)
    @Getter
    @Setter
    private String legalName;

    @Column(name = "nome_fantasia")
    @Getter
    @Setter
    private String tradeName;

    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    @Getter
    @Setter
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_instituicao", nullable = false, length = 20)
    @Getter
    @Setter
    private InstitutionType type;

    @Column(name = "ativo", nullable = false)
    @Getter
    @Setter
    private Boolean active = true;

    @Column(name = "criado_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @OneToMany(mappedBy = "institution")
    @Getter
    private List<SchoolClass> classes = new ArrayList<>();

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
