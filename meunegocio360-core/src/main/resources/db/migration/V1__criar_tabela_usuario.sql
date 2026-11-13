CREATE TABLE usuario (
    id BINARY(16) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(160) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    tentativas_falhas INT NOT NULL DEFAULT 0,
    bloqueado_ate DATETIME(6) NULL,
    ultimo_login_em DATETIME(6) NULL,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);
