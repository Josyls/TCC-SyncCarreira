package com.synccarreira.synccarreira_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Dados administrativos de uma conta de usuário, mantidos em tabela lateral para
 * não alterar {@code tb_usuario} (regra: só arquivos novos).
 *
 * <ul>
 *   <li>CPF obrigatório e único (validações da seção 7 — cobrado na camada de serviço).</li>
 *   <li>{@code active} — habilita/desabilita a conta (RF-02, "desativar contas").</li>
 *   <li>Vínculo com instituição e turma — base do isolamento por instituição (RNF-08).</li>
 * </ul>
 */
@Entity
@Table(name = "tb_conta")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Account {

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

    @Column(name = "cpf", unique = true, length = 11)
    @Getter
    @Setter
    private String cpf;

    @Column(name = "ativo", nullable = false)
    @Getter
    @Setter
    private Boolean active = true;

    @Column(name = "motivo_desativacao")
    @Getter
    @Setter
    private String deactivationReason;

    @ManyToOne
    @JoinColumn(name = "fk_instituicao")
    @Getter
    @Setter
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "fk_turma")
    @Getter
    @Setter
    private SchoolClass schoolClass;

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
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}
