ALTER TABLE eventos
    ADD COLUMN proponente_nome VARCHAR(150) NOT NULL,
    ADD COLUMN proponente_cpf VARCHAR(14) NOT NULL,
    ADD COLUMN proponente_email VARCHAR(150) NOT NULL,
    ADD COLUMN proponente_telefone VARCHAR(20) NOT NULL,
    ADD COLUMN proponente_mini_curriculo VARCHAR(2000) NOT NULL,

    ADD COLUMN evento_entidade_estudantil BOOLEAN NOT NULL DEFAULT FALSE,

    ADD COLUMN caracterizacao_atividade VARCHAR(4000) NOT NULL,
    ADD COLUMN apresentacao VARCHAR(1500) NOT NULL,
    ADD COLUMN publicar_no_campus BOOLEAN NOT NULL DEFAULT FALSE,

    ADD COLUMN objetivo_geral VARCHAR(2000) NOT NULL,
    ADD COLUMN objetivos_especificos VARCHAR(3000) NOT NULL,
    ADD COLUMN publico_alvo VARCHAR(1000) NOT NULL,

    ADD COLUMN tipo_local VARCHAR(30) NOT NULL,
    ADD COLUMN espaco_realizacao VARCHAR(30) NOT NULL,
    ADD COLUMN especificacao_espaco VARCHAR(1000) NOT NULL,

    ADD COLUMN faixa_vagas VARCHAR(30) NOT NULL,

    ADD COLUMN hora_inicio TIME NOT NULL,
    ADD COLUMN hora_fim TIME NOT NULL,

    ADD COLUMN dias_horarios VARCHAR(1000) NOT NULL,
    ADD COLUMN programacao VARCHAR(4000) NOT NULL,

    ADD COLUMN emite_certificado BOOLEAN NOT NULL DEFAULT FALSE,

    ADD COLUMN tipo_investimento VARCHAR(20) NOT NULL DEFAULT 'GRATUITO',

    ADD COLUMN autorizacao_lgpd BOOLEAN NOT NULL DEFAULT FALSE;
