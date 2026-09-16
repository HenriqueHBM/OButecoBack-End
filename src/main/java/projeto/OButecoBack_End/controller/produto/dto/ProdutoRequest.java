package projeto.OButecoBack_End.controller.produto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;

import java.sql.Timestamp;

public record ProdutoRequest(
        Long id,

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        EStatus status,

        @NotNull(message = "Categoria é obrigatório")
        Long categoriaId,

        @NotNull(message = "Grupo é obrigatório")
        Long grupoId,

        Double precoVenda,

        String observacao
) {
    public static ProdutoRequest de(ProdutosEntity produtosEntity){
        return new ProdutoRequest(
                produtosEntity.getId(),
                produtosEntity.getNome(),
                produtosEntity.getStatus(),
                produtosEntity.getCategoria().getId(),
                produtosEntity.getGrupo().getId(),
                produtosEntity.getPrecoVenda(),
                produtosEntity.getObservacao()
        );
    }
}
