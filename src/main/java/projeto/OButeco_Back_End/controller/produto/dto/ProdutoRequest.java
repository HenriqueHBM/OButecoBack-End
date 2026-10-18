package projeto.OButeco_Back_End.controller.produto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import projeto.OButeco_Back_End.model.Enum.CategoriaEnum;
import projeto.OButeco_Back_End.model.Enum.EStatus;
import projeto.OButeco_Back_End.model.Enum.GrupoEnum;

import java.math.BigDecimal;
import java.util.List;

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
        String observacao,

        @Valid
        List<InsumosRequest> insumos
) {
    /*
    public static ProdutoRequest de(ProdutosEntity produtosEntity){
        return new ProdutoRequest(
                produtosEntity.getId(),
                produtosEntity.getNome(),
                produtosEntity.getStatus(),
                produtosEntity.getCategoriaEnum(),
                produtosEntity.getGrupo(),
                produtosEntity.getPrecoVenda(),
                produtosEntity.getObservacao(),
                produtosEntity.getInsumos()
        );
    }
    */
    public record InsumosRequest(
            @NotNull(message = "Insumo é obrigatório")
            Long insumoId,

            @NotNull @Positive
            BigDecimal qtde
    ) {}

}
