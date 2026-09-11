-- =====================================================================
-- V3__jornada.sql  (Bloco 4 — Jornada do aluno)
-- Executar manualmente (ddl-auto=none). Idempotente.
-- RF-10, RF-11, RF-13, RF-14, RF-15, RN-01, RN-02, RN-08, RN-09.
-- =====================================================================

-- ---------------------------------------------------------------------
-- RF-14 — Perfil do aluno + interesses.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_perfil_aluno (
    id_usuario     BIGINT NOT NULL,
    bio            VARCHAR(2000),
    telefone       VARCHAR(20),
    cidade         VARCHAR(120),
    criado_em      DATETIME(6) NOT NULL,
    atualizado_em  DATETIME(6),
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_perfil_aluno_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tb_interesse (
    id_interesse   BIGINT NOT NULL AUTO_INCREMENT,
    nome_interesse VARCHAR(80) NOT NULL,
    PRIMARY KEY (id_interesse),
    CONSTRAINT uk_interesse_nome UNIQUE (nome_interesse)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tb_perfil_aluno_interesse (
    id_usuario   BIGINT NOT NULL,
    id_interesse BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_interesse),
    CONSTRAINT fk_pai_perfil    FOREIGN KEY (id_usuario)   REFERENCES tb_perfil_aluno (id_usuario),
    CONSTRAINT fk_pai_interesse FOREIGN KEY (id_interesse) REFERENCES tb_interesse (id_interesse)
) ENGINE=InnoDB;

INSERT INTO tb_interesse (nome_interesse)
SELECT * FROM (
    SELECT 'Tecnologia' UNION ALL SELECT 'Saúde' UNION ALL SELECT 'Educação' UNION ALL
    SELECT 'Artes e Design' UNION ALL SELECT 'Negócios e Empreendedorismo' UNION ALL
    SELECT 'Direito e Justiça' UNION ALL SELECT 'Ciências Exatas' UNION ALL
    SELECT 'Ciências Biológicas' UNION ALL SELECT 'Ciências Humanas' UNION ALL
    SELECT 'Comunicação e Mídia' UNION ALL SELECT 'Esportes' UNION ALL
    SELECT 'Meio Ambiente e Sustentabilidade' UNION ALL SELECT 'Gastronomia' UNION ALL
    SELECT 'Idiomas' UNION ALL SELECT 'Música' UNION ALL
    SELECT 'Psicologia e Comportamento' UNION ALL SELECT 'Engenharia' UNION ALL
    SELECT 'Agronomia' UNION ALL SELECT 'Turismo' UNION ALL SELECT 'Serviço Social'
) AS seed(nome)
WHERE NOT EXISTS (SELECT 1 FROM tb_interesse i WHERE i.nome_interesse = seed.nome);

-- ---------------------------------------------------------------------
-- RF-10 / RN-01 — Jornada (um ciclo do aluno pelas trilhas).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_jornada (
    id_jornada     BIGINT NOT NULL AUTO_INCREMENT,
    fk_aluno       BIGINT NOT NULL,
    ciclo          INT NOT NULL,
    status_jornada VARCHAR(20) NOT NULL,
    em_duvida      BIT,
    sintese_final  VARCHAR(4000),
    iniciada_em    DATETIME(6) NOT NULL,
    concluida_em   DATETIME(6),
    PRIMARY KEY (id_jornada),
    CONSTRAINT fk_jornada_aluno FOREIGN KEY (fk_aluno) REFERENCES tb_usuario (id_usuario),
    CONSTRAINT uk_jornada_aluno_ciclo UNIQUE (fk_aluno, ciclo)
) ENGINE=InnoDB;

-- Progresso/gating por trilha dentro da jornada.
CREATE TABLE IF NOT EXISTS tb_jornada_trilha (
    id_jornada_trilha BIGINT NOT NULL AUTO_INCREMENT,
    fk_jornada        BIGINT NOT NULL,
    fk_trilha         BIGINT NOT NULL,
    status_trilha     VARCHAR(20) NOT NULL,
    concluida_em      DATETIME(6),
    PRIMARY KEY (id_jornada_trilha),
    CONSTRAINT fk_jt_jornada FOREIGN KEY (fk_jornada) REFERENCES tb_jornada (id_jornada),
    CONSTRAINT fk_jt_trilha  FOREIGN KEY (fk_trilha)  REFERENCES tb_trilha (id_trilha),
    CONSTRAINT uk_jt_jornada_trilha UNIQUE (fk_jornada, fk_trilha)
) ENGINE=InnoDB;

-- Respostas da nova jornada (cifradas na aplicação — RNF-02).
CREATE TABLE IF NOT EXISTS tb_resposta_jornada (
    id_resposta_jornada BIGINT NOT NULL AUTO_INCREMENT,
    fk_jornada          BIGINT NOT NULL,
    fk_pergunta         BIGINT NOT NULL,
    fk_opcao_pergunta   BIGINT,
    conteudo            VARCHAR(1000),
    criada_em           DATETIME(6) NOT NULL,
    atualizada_em       DATETIME(6),
    PRIMARY KEY (id_resposta_jornada),
    CONSTRAINT fk_rj_jornada  FOREIGN KEY (fk_jornada)        REFERENCES tb_jornada (id_jornada),
    CONSTRAINT fk_rj_pergunta FOREIGN KEY (fk_pergunta)       REFERENCES tb_pergunta (id_pergunta),
    CONSTRAINT fk_rj_opcao    FOREIGN KEY (fk_opcao_pergunta) REFERENCES tb_opcao_pergunta (id_opcao_pergunta),
    CONSTRAINT uk_rj_jornada_pergunta UNIQUE (fk_jornada, fk_pergunta)
) ENGINE=InnoDB;

-- RF-11 / RN-02 — Síntese textual obrigatória ao fim de cada trilha (cifrada).
CREATE TABLE IF NOT EXISTS tb_sintese_trilha (
    id_sintese_trilha BIGINT NOT NULL AUTO_INCREMENT,
    fk_jornada        BIGINT NOT NULL,
    fk_trilha         BIGINT NOT NULL,
    texto_sintese     VARCHAR(4000) NOT NULL,
    criada_em         DATETIME(6) NOT NULL,
    atualizada_em     DATETIME(6),
    PRIMARY KEY (id_sintese_trilha),
    CONSTRAINT fk_st_jornada FOREIGN KEY (fk_jornada) REFERENCES tb_jornada (id_jornada),
    CONSTRAINT fk_st_trilha  FOREIGN KEY (fk_trilha)  REFERENCES tb_trilha (id_trilha),
    CONSTRAINT uk_st_jornada_trilha UNIQUE (fk_jornada, fk_trilha)
) ENGINE=InnoDB;
