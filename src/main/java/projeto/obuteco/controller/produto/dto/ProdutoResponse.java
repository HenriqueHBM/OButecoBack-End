package projeto.obuteco.controller.produto.dto;

import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;
import projeto.obuteco.model.entity.produto.ProdutosEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record ProdutoResponse(
        Long id,
        String name,
        EStatus status,
        CategoriaEnum categoryEnum,
        GrupoEnum groupEnum,
        BigDecimal salePrice,
        String observation,
        Timestamp creationDate,
        Timestamp updateDate,
        Timestamp deletedDate
) {
    public static ProdutoResponse de(ProdutosEntity produtosEntity){
        return new ProdutoResponse(
                produtosEntity.getId(),
                produtosEntity.getNome(),
                produtosEntity.getStatus(),
                produtosEntity.getCategoriaEnum(),
                produtosEntity.getGrupo(),
                produtosEntity.getPrecoVenda(),
                produtosEntity.getObservacao(),
                produtosEntity.getDataCriacao(),
                produtosEntity.getDataAtualizacao(),
                produtosEntity.getDeletedAt()
        );
    }
}
