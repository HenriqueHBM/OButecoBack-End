ALTER TABLE usuarios ALTER COLUMN cargo TYPE varchar(20)
USING CASE cargo
    WHEN 0 THEN 'ESTAGIARIO'
    WHEN 1 THEN 'AUXILIAR'
    WHEN 2 THEN 'GERENTE'
    WHEN 3 THEN 'ADMIN'
END;

CREATE UNIQUE INDEX uk_usuarios_usuario_ativo
    ON usuarios (usuario) WHERE deleted_at IS NULL;