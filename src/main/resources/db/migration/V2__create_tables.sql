create table produtos (
      id bigserial primary key not null,
      nome varchar(255) not null,
      status varchar(20) default 'ATIVO' not null,
      preco_venda decimal(10,2),
      categoria varchar(30) not null,
      grupo varchar(30) not null,
      observacao text default null,
      created_at timestamp default now(),
      updated_at timestamp default now(),
      deleted_at timestamp default null
);


create table insumos_produtos(
     id bigserial primary key not null,
     fk_id_produto bigint not null,
     fk_insumos_produto bigint not null,
     qtde decimal(10,2) not null,
     foreign key (fk_id_produto) references produtos(id),
     foreign key (fk_insumos_produto) references produtos(id),
     unique (fk_id_produto, fk_insumos_produto)
);


create table conversoes(
   id bigserial primary key not null,
   conversao varchar(150) not null,
   nomenclatura varchar(20)
);

create table estoques(
     id bigserial primary key not null,
     fk_id_produto bigint not null,
     qtde_estoque decimal(10,2),
     status varchar(20) default 'ATIVO' not null,
     fk_id_conversao bigint not null,
     local varchar(255),
     created_at timestamp default now(),
     updated_at timestamp default now(),
     deleted_at timestamp default null,
     foreign key (fk_id_conversao) references conversoes(id)
);



create table usuarios (
      id bigserial primary key not null,
      nome varchar(255) not null,
      usuario varchar(150) not null,
      senha varchar(255) not null,
      status varchar(20) default 'ATIVO' not null,
      cargo int not null,
      created_at timestamp default now(),
      updated_at timestamp default now(),
      deleted_at timestamp default null
);

create table movimentacoes_estoques
(
    id                bigserial primary key not null,
    fk_id_estoque     bigint                not null,
    tipo              varchar(30)        not null,
    qtde              decimal(10, 2)     not null,
    qtde_conversao decimal(10,2),
    valor_unitario    decimal(10, 2),
    valor_total       decimal(10, 2),
    fk_id_usuario     bigint                not null,
    fk_id_conversao   bigint                not null,
    fk_id_produto     bigint,
    data_movimentacao timestamp default now(),
    observacao        text,
    foreign key (fk_id_usuario) references usuarios (id),
    foreign key (fk_id_conversao) references conversoes (id),
    foreign key (fk_id_produto) references produtos (id)
);