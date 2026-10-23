package projeto.obuteco.controller.produto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;

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

    public record InsumosRequest(
            @NotNull(message = "Insumo é obrigatório")
            Long insumoId,

            @NotNull @Positive
            BigDecimal qtde
    ) {}

}
