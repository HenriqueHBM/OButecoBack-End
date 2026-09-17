package projeto.OButecoBack_End.controller.produto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import projeto.OButecoBack_End.model.Enum.CategoriaEnum;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.Enum.GrupoEnum;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record ProdutoRequest(
        Long id,

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        EStatus status,

        @NotNull(message = "Categoria é obrigatório")
        CategoriaEnum categoriaEnum,

        @NotNull(message = "Grupo é obrigatório")
        GrupoEnum grupoEnum,

        BigDecimal precoVenda,

        String observacao
) {
    public static ProdutoRequest de(ProdutosEntity produtosEntity){
        return new ProdutoRequest(
                produtosEntity.getId(),
                produtosEntity.getNome(),
                produtosEntity.getStatus(),
                produtosEntity.getCategoriaEnum(),
                produtosEntity.getGrupo(),
                produtosEntity.getPrecoVenda(),
                produtosEntity.getObservacao()
        );
    }
}
