-- =====================================================================
-- V5__agendamento_links_feedback.sql  (Bloco 6 — painel da psicóloga)
-- Executar manualmente (ddl-auto=none). Idempotente.
-- RF-06, RF-08, RF-09, RF-16, RN-05.
-- =====================================================================

-- ---------------------------------------------------------------------
-- RF-06 / RN-05 — Links curados pela psicóloga (trilha de Informação).
-- fk_instituicao nulo = link disponível para todas as instituições.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_link_curado (
    id_link        BIGINT NOT NULL AUTO_INCREMENT,
    fk_psicologa   BIGINT NOT NULL,
    fk_instituicao BIGINT,
    titulo         VARCHAR(200) NOT NULL,
    url            VARCHAR(500) NOT NULL,
    descricao      VARCHAR(400),
    categoria      VARCHAR(20) NOT NULL,       -- ENEM | PROUNI | SISU | COTAS | CURSOS | OUTRO
    ativo          BIT NOT NULL DEFAULT 1,
    criado_em      DATETIME(6) NOT NULL,
    PRIMARY KEY (id_link),
    CONSTRAINT fk_link_psicologa   FOREIGN KEY (fk_psicologa)   REFERENCES tb_usuario (id_usuario),
    CONSTRAINT fk_link_instituicao FOREIGN KEY (fk_instituicao) REFERENCES tb_instituicao (id_instituicao)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- RF-08 — Sessões de orientação (individuais ou em grupo).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_sessao (
    id_sessao        BIGINT NOT NULL AUTO_INCREMENT,
    fk_psicologa     BIGINT NOT NULL,
    tipo_sessao      VARCHAR(15) NOT NULL,     -- INDIVIDUAL | GRUPO
    titulo           VARCHAR(200) NOT NULL,
    descricao        VARCHAR(1000),
    inicio           DATETIME(6) NOT NULL,
    fim              DATETIME(6) NOT NULL,
    local_sessao     VARCHAR(300),
    status_sessao    VARCHAR(15) NOT NULL,     -- AGENDADA | REALIZADA | CANCELADA
    criado_em        DATETIME(6) NOT NULL,
    atualizado_em    DATETIME(6),
    PRIMARY KEY (id_sessao),
    CONSTRAINT fk_sessao_psicologa FOREIGN KEY (fk_psicologa) REFERENCES tb_usuario (id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tb_sessao_participante (
    id_participacao BIGINT NOT NULL AUTO_INCREMENT,
    fk_sessao       BIGINT NOT NULL,
    fk_aluno        BIGINT NOT NULL,
    PRIMARY KEY (id_participacao),
    CONSTRAINT fk_sp_sessao FOREIGN KEY (fk_sessao) REFERENCES tb_sessao (id_sessao),
    CONSTRAINT fk_sp_aluno  FOREIGN KEY (fk_aluno)  REFERENCES tb_usuario (id_usuario),
    CONSTRAINT uk_sp_sessao_aluno UNIQUE (fk_sessao, fk_aluno)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- RF-09 — Feedback textual da psicóloga para o aluno (cifrado — RNF-02).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_feedback (
    id_feedback   BIGINT NOT NULL AUTO_INCREMENT,
    fk_psicologa  BIGINT NOT NULL,
    fk_aluno      BIGINT NOT NULL,
    fk_jornada    BIGINT,
    fk_sessao     BIGINT,
    texto         VARCHAR(4000) NOT NULL,
    criado_em     DATETIME(6) NOT NULL,
    PRIMARY KEY (id_feedback),
    CONSTRAINT fk_fb_psicologa FOREIGN KEY (fk_psicologa) REFERENCES tb_usuario (id_usuario),
    CONSTRAINT fk_fb_aluno     FOREIGN KEY (fk_aluno)     REFERENCES tb_usuario (id_usuario),
    CONSTRAINT fk_fb_jornada   FOREIGN KEY (fk_jornada)   REFERENCES tb_jornada (id_jornada),
    CONSTRAINT fk_fb_sessao    FOREIGN KEY (fk_sessao)    REFERENCES tb_sessao (id_sessao)
) ENGINE=InnoDB;
