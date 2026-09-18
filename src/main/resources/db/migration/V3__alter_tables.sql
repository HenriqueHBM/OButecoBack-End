ALTER TABLE insumos_produtos
    ADD COLUMN fk_id_conversao BIGINT;

ALTER TABLE insumos_produtos
    ADD CONSTRAINT fk_insumos_produtos_conversao
        FOREIGN KEY (fk_id_conversao)
            REFERENCES conversoes(id);

ALTER TABLE conversoes
    ADD COLUMN fator_base DECIMAL(10,4);