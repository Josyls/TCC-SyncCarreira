-- =====================================================================
-- V2__fundacao.sql  (Bloco 1 — Fundação)
-- Executar manualmente (ddl-auto=none). Idempotente: pode rodar de novo.
-- NÃO altera create.sql nem import.sql.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Perfil de psicóloga (RF-02). ROLE_USER=1 (aluno), ROLE_ADMIN=2 já existem.
-- ---------------------------------------------------------------------
INSERT INTO tb_role (autoridade_role)
SELECT 'ROLE_PSICOLOGA'
WHERE NOT EXISTS (SELECT 1 FROM tb_role WHERE autoridade_role = 'ROLE_PSICOLOGA');

-- ---------------------------------------------------------------------
-- C2 — ordem das trilhas conforme o Documento de Requisitos:
-- Autoconhecimento(1) > Influencias(2) > Informacao(3) > Plano de Futuro(4).
-- (o import.sql tinha Plano de Futuro=3 e Informacao=4)
-- ---------------------------------------------------------------------
UPDATE tb_trilha SET ordem_sequencial_trilha = 3 WHERE nome_trilha = 'INFORMACAO';
UPDATE tb_trilha SET ordem_sequencial_trilha = 4 WHERE nome_trilha = 'PLANO_DE_FUTURO';

-- ---------------------------------------------------------------------
-- RF-01 — Instituições parceiras (escola / ONG). CNPJ obrigatório e único.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_instituicao (
    id_instituicao   BIGINT NOT NULL AUTO_INCREMENT,
    razao_social     VARCHAR(255) NOT NULL,
    nome_fantasia    VARCHAR(255),
    cnpj             VARCHAR(14)  NOT NULL,
    tipo_instituicao VARCHAR(20)  NOT NULL,
    ativo            BIT          NOT NULL DEFAULT 1,
    criado_em        DATETIME(6)  NOT NULL,
    PRIMARY KEY (id_instituicao),
    CONSTRAINT uk_instituicao_cnpj UNIQUE (cnpj)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- RF-01 — Turmas vinculadas a uma instituição, com psicóloga responsável.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_turma (
    id_turma       BIGINT NOT NULL AUTO_INCREMENT,
    nome_turma     VARCHAR(255) NOT NULL,
    ano_letivo     INT          NOT NULL,
    fk_instituicao BIGINT       NOT NULL,
    fk_psicologa   BIGINT,
    ativo          BIT          NOT NULL DEFAULT 1,
    criado_em      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id_turma),
    CONSTRAINT fk_turma_instituicao FOREIGN KEY (fk_instituicao) REFERENCES tb_instituicao (id_instituicao),
    CONSTRAINT fk_turma_psicologa   FOREIGN KEY (fk_psicologa)   REFERENCES tb_usuario (id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- RF-02 / RNF-08 — Conta administrativa (tabela lateral de tb_usuario):
-- CPF único, flag de ativo/desativado, vínculo com instituição e turma.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_conta (
    id_usuario         BIGINT NOT NULL,
    cpf                VARCHAR(11),
    ativo              BIT          NOT NULL DEFAULT 1,
    motivo_desativacao VARCHAR(255),
    fk_instituicao     BIGINT,
    fk_turma           BIGINT,
    criado_em          DATETIME(6)  NOT NULL,
    atualizado_em      DATETIME(6),
    PRIMARY KEY (id_usuario),
    CONSTRAINT uk_conta_cpf         UNIQUE (cpf),
    CONSTRAINT fk_conta_usuario     FOREIGN KEY (id_usuario)     REFERENCES tb_usuario (id_usuario),
    CONSTRAINT fk_conta_instituicao FOREIGN KEY (fk_instituicao) REFERENCES tb_instituicao (id_instituicao),
    CONSTRAINT fk_conta_turma       FOREIGN KEY (fk_turma)       REFERENCES tb_turma (id_turma)
) ENGINE=InnoDB;

-- Conta para todos os usuários já existentes (seed) que ainda não têm.
INSERT INTO tb_conta (id_usuario, ativo, criado_em)
SELECT u.id_usuario, 1, NOW(6)
FROM tb_usuario u
WHERE NOT EXISTS (SELECT 1 FROM tb_conta c WHERE c.id_usuario = u.id_usuario);

-- ---------------------------------------------------------------------
-- Bootstrap do administrador. Senha: 12345678 (mesmo hash bcrypt do seed).
-- ---------------------------------------------------------------------
INSERT INTO tb_usuario (nome_usuario, email_usuario, senha_usuario)
SELECT 'Administrador SyncCarreira', 'admin@synccarreira.com',
       '$2a$10$HibjoKCNHoOlUpl1UFYkuee58qncnGO00giJ1zhh3advi3Sct/FD2'
WHERE NOT EXISTS (SELECT 1 FROM tb_usuario WHERE email_usuario = 'admin@synccarreira.com');

INSERT INTO tb_usuario_role (id_usuario, id_role)
SELECT u.id_usuario, r.id_role
FROM tb_usuario u
JOIN tb_role r ON r.autoridade_role = 'ROLE_ADMIN'
WHERE u.email_usuario = 'admin@synccarreira.com'
  AND NOT EXISTS (
      SELECT 1 FROM tb_usuario_role ur
      WHERE ur.id_usuario = u.id_usuario AND ur.id_role = r.id_role
  );

INSERT INTO tb_conta (id_usuario, ativo, criado_em)
SELECT u.id_usuario, 1, NOW(6)
FROM tb_usuario u
WHERE u.email_usuario = 'admin@synccarreira.com'
  AND NOT EXISTS (SELECT 1 FROM tb_conta c WHERE c.id_usuario = u.id_usuario);
