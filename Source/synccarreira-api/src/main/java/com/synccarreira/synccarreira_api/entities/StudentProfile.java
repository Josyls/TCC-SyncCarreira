package com.synccarreira.synccarreira_api.entities;

import com.synccarreira.synccarreira_api.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Perfil do aluno — informações e interesses para contextualizar a jornada (RF-14).
 * A bio é armazenada cifrada (RNF-02).
 */
@Entity
@Table(name = "tb_perfil_aluno")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StudentProfile {

    @Id
    @Column(name = "id_usuario")
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    @Getter
    @Setter
    private User user;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bio", length = 2000)
    @Getter
    @Setter
    private String bio;

    @Column(name = "telefone", length = 20)
    @Getter
    @Setter
    private String phone;

    @Column(name = "cidade", length = 120)
    @Getter
    @Setter
    private String city;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tb_perfil_aluno_interesse",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_interesse"))
    @Getter
    private Set<Interest> interests = new HashSet<>();

    @Column(name = "criado_em", nullable = false)
    @Getter
    @Setter
    private Instant createdAt;

    @Column(name = "atualizado_em")
    @Getter
    @Setter
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
