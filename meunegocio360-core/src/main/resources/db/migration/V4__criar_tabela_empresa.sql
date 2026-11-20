CREATE TABLE empresa (
    id BINARY(16) NOT NULL,
    razao_social VARCHAR(150) NOT NULL,
    nome_fantasia VARCHAR(150) NULL,
    cnpj CHAR(14) NOT NULL,
    email VARCHAR(160) NULL,
    telefone VARCHAR(11) NULL,
    fuso_horario VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
    moeda CHAR(3) NOT NULL DEFAULT 'BRL',
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    ativa_unica TINYINT GENERATED ALWAYS AS (CASE WHEN ativa THEN 1 ELSE NULL END) STORED,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_empresa PRIMARY KEY (id),
    CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj),
    CONSTRAINT uk_empresa_ativa_unica UNIQUE (ativa_unica)
);
