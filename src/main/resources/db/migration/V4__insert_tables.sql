-- =========================================================
-- CONVERSÕES
-- =========================================================
INSERT INTO conversoes (conversao,nomenclatura,fator_base) VALUES
      ('Quilograma', 'Kg', 1000),
      ('Grama', 'g', 1);


-- =========================================================
-- USUÁRIO
-- =========================================================

INSERT INTO usuarios (nome,usuario,senha,status,cargo,created_at,updated_at,deleted_at) VALUES (
             'Henrique Braz Madeira',
             'henrique.madeira',
             '23132',
             'ATIVO',
             0,
             now(),
             now(),
             NULL
         );

-- =========================================================
-- PRODUTOS
-- =========================================================

INSERT INTO produtos (nome,status,preco_venda,categoria,grupo,observacao,created_at,updated_at,deleted_at) VALUES
      ('Pizza','ATIVO',NULL,'PRODUTO_INSUMOS','COMIDA',NULL,now(),now(),NULL),
      ('Pizza','ATIVO',10.00,'PRODUTO_INSUMOS','COMIDA',NULL,now(),now(),NULL),
      ('Pizza','ATIVO',NULL,'PRODUTO_INSUMOS','COMIDA',NULL,now(),now(),NULL),
      ('Luciana Teza','INATIVO',20.00,'NORMAL','BEBIDA',NULL,now(),now(),now()),
      ('queijo','ATIVO',15.00,'INSUMO','COMIDA',NULL,now(),now(),NULL),
      ('batata','INATIVO',5.00,'NORMAL','COMIDA',NULL,now(),now(),NULL);


-- =========================================================
-- RELAÇÃO PRODUTO -> INSUMO
-- Pizza ID 3 utiliza queijo ID 5
-- Quantidade: 150 g
-- Conversão do insumo: grama (ID 2)
-- =========================================================

INSERT INTO insumos_produtos (fk_id_produto,fk_insumos_produto,qtde,fk_id_conversao) VALUES (
             3,
             5,
             150.00,
             2);

-- =========================================================
-- ESTOQUE
-- Queijo ID 5 armazenado em Kg
-- Quantidade inicial: 1 Kg
-- =========================================================

INSERT INTO estoques (fk_id_produto,qtde_estoque,status,fk_id_conversao,local,created_at,updated_at,deleted_at) VALUES (
             5,
             1.00,
             'ATIVO',
             1,
             NULL,
             now(),
             now(),
             NULL);

-- =========================================================
-- MOVIMENTAÇÃO INICIAL DO ESTOQUE
-- Entrada de 1 Kg de queijo
-- =========================================================

INSERT INTO movimentacoes_estoques (fk_id_estoque,tipo,qtde,qtde_conversao,valor_unitario,valor_total,fk_id_usuario,fk_id_conversao,fk_id_produto,data_movimentacao,observacao) VALUES
             (1,'ENTRADA',1.00,1.00,15.00,15.00,1,1,5,now(),'Entrada inicial de queijo para teste');