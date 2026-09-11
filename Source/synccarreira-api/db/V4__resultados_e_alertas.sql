-- =====================================================================
-- V4__resultados_e_alertas.sql  (Blocos 4/5 — leque de carreiras e alerta)
-- Executar manualmente (ddl-auto=none). Idempotente.
-- RF-04, RF-12, RN-04, RN-07, RN-09.
-- =====================================================================

-- ---------------------------------------------------------------------
-- RF-12 / RN-09 — Catálogo fixo de áreas → carreiras/cursos.
-- Cada item marcado como pendente de validação pela psicóloga.
-- area: HUMANAS | EXATAS | BIOLOGICAS | ARTES
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_area_carreira (
    id_area_carreira BIGINT NOT NULL AUTO_INCREMENT,
    area             VARCHAR(20) NOT NULL,
    titulo           VARCHAR(150) NOT NULL,
    tipo             VARCHAR(20) NOT NULL,          -- CARREIRA | CURSO
    descricao        VARCHAR(400),
    validado         BIT NOT NULL DEFAULT 0,
    fk_validado_por  BIGINT,
    validado_em      DATETIME(6),
    ativo            BIT NOT NULL DEFAULT 1,
    criado_em        DATETIME(6) NOT NULL,
    PRIMARY KEY (id_area_carreira),
    CONSTRAINT fk_area_carreira_psicologa FOREIGN KEY (fk_validado_por) REFERENCES tb_usuario (id_usuario)
) ENGINE=InnoDB;

INSERT INTO tb_area_carreira (area, titulo, tipo, descricao, validado, ativo, criado_em)
SELECT * FROM (
    SELECT 'EXATAS' AS area, 'Engenharia de Software' AS titulo, 'CARREIRA' AS tipo, 'Projeto e construção de sistemas e aplicações.' AS descricao, 0 AS validado, 1 AS ativo, NOW(6) AS criado_em UNION ALL
    SELECT 'EXATAS', 'Ciência de Dados', 'CARREIRA', 'Análise de dados para apoiar decisões.', 0, 1, NOW(6) UNION ALL
    SELECT 'EXATAS', 'Engenharia Civil', 'CARREIRA', 'Planejamento e execução de obras e infraestrutura.', 0, 1, NOW(6) UNION ALL
    SELECT 'EXATAS', 'Análise e Desenvolvimento de Sistemas', 'CURSO', 'Curso tecnológico voltado a desenvolvimento.', 0, 1, NOW(6) UNION ALL
    SELECT 'EXATAS', 'Matemática / Estatística', 'CURSO', 'Base para pesquisa, ensino e análise quantitativa.', 0, 1, NOW(6) UNION ALL
    SELECT 'HUMANAS', 'Direito', 'CARREIRA', 'Assessoria jurídica, advocacia, carreiras públicas.', 0, 1, NOW(6) UNION ALL
    SELECT 'HUMANAS', 'Psicologia', 'CARREIRA', 'Escuta, avaliação e acompanhamento de pessoas.', 0, 1, NOW(6) UNION ALL
    SELECT 'HUMANAS', 'Pedagogia / Licenciaturas', 'CARREIRA', 'Ensino e gestão educacional.', 0, 1, NOW(6) UNION ALL
    SELECT 'HUMANAS', 'Serviço Social', 'CARREIRA', 'Atuação com políticas e direitos sociais.', 0, 1, NOW(6) UNION ALL
    SELECT 'HUMANAS', 'Comunicação Social', 'CURSO', 'Jornalismo, publicidade, produção de conteúdo.', 0, 1, NOW(6) UNION ALL
    SELECT 'BIOLOGICAS', 'Enfermagem', 'CARREIRA', 'Cuidado e assistência à saúde.', 0, 1, NOW(6) UNION ALL
    SELECT 'BIOLOGICAS', 'Medicina', 'CARREIRA', 'Diagnóstico e tratamento em saúde.', 0, 1, NOW(6) UNION ALL
    SELECT 'BIOLOGICAS', 'Educação Física', 'CARREIRA', 'Saúde, esporte e movimento humano.', 0, 1, NOW(6) UNION ALL
    SELECT 'BIOLOGICAS', 'Biologia / Ciências Ambientais', 'CURSO', 'Pesquisa, meio ambiente e ensino.', 0, 1, NOW(6) UNION ALL
    SELECT 'BIOLOGICAS', 'Nutrição', 'CARREIRA', 'Alimentação e saúde.', 0, 1, NOW(6) UNION ALL
    SELECT 'ARTES', 'Design Gráfico / Digital', 'CARREIRA', 'Criação visual para produtos e comunicação.', 0, 1, NOW(6) UNION ALL
    SELECT 'ARTES', 'Arquitetura e Urbanismo', 'CARREIRA', 'Projeto de espaços e cidades.', 0, 1, NOW(6) UNION ALL
    SELECT 'ARTES', 'Artes Visuais / Cênicas', 'CURSO', 'Produção artística e cultural.', 0, 1, NOW(6) UNION ALL
    SELECT 'ARTES', 'Música', 'CURSO', 'Performance, composição e ensino musical.', 0, 1, NOW(6) UNION ALL
    SELECT 'ARTES', 'Produção Audiovisual', 'CURSO', 'Cinema, vídeo e novas mídias.', 0, 1, NOW(6)
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM tb_area_carreira);

-- ---------------------------------------------------------------------
-- RF-04 / RN-04 — Alerta automático de necessidade de orientação.
-- Gerado quando o aluno conclui a jornada mas segue "em dúvida".
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_alerta_orientacao (
    id_alerta      BIGINT NOT NULL AUTO_INCREMENT,
    fk_aluno       BIGINT NOT NULL,
    fk_jornada     BIGINT NOT NULL,
    motivo         VARCHAR(30) NOT NULL,          -- EM_DUVIDA
    prioridade     VARCHAR(20) NOT NULL,          -- GRUPO | INDIVIDUAL
    resolvido      BIT NOT NULL DEFAULT 0,
    resolvido_por  BIGINT,
    resolvido_em   DATETIME(6),
    criado_em      DATETIME(6) NOT NULL,
    PRIMARY KEY (id_alerta),
    CONSTRAINT fk_alerta_aluno    FOREIGN KEY (fk_aluno)      REFERENCES tb_usuario (id_usuario),
    CONSTRAINT fk_alerta_jornada  FOREIGN KEY (fk_jornada)    REFERENCES tb_jornada (id_jornada),
    CONSTRAINT fk_alerta_resolv   FOREIGN KEY (resolvido_por) REFERENCES tb_usuario (id_usuario),
    CONSTRAINT uk_alerta_jornada  UNIQUE (fk_jornada)
) ENGINE=InnoDB;
