package projeto.OButecoBack_End.controller.produto.dto;

import jakarta.validation.constraints.NotBlank;
import projeto.OButecoBack_End.model.Enum.CategoriaEnum;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.Enum.GrupoEnum;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record ProdutoResponse(
        Long id,
        String nome,
        EStatus status,
        CategoriaEnum categoriaEnum,
        GrupoEnum grupoEnum,
        BigDecimal precoVenda,
        String observacao,
        Timestamp created_at,
        Timestamp updated_at,
        Timestamp deleted_at
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
