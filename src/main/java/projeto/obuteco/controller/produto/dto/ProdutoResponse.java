package projeto.obuteco.controller.produto.dto;

import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;
import projeto.obuteco.model.entity.produto.ProdutosEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

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
        Timestamp deletedDate,
        List<InsumosResponse> insumos
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
                produtosEntity.getDeletedAt(),
                // ficha técnica: insumos usados pelo produto (vazio para os outros tipos)
                produtosEntity.getInsumos() == null
                        ? List.of()
                        : produtosEntity.getInsumos().stream().map(InsumosResponse::de).toList()
        );
    }
}
