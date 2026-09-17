package projeto.OButecoBack_End.controller.estoque.dto;

import jakarta.validation.constraints.*;

public record MovimentacoesEstoqueRequest(
        @NotNull(message = "ID do estoque é obrigatório")
        Long fk_id_estoque,

        @NotBlank(message = "Tipo de movimentação é obrigatório")
        String tipo,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        double qtde,

        @NotNull(message = "Valor unitário é obrigatório")
        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        double valorUnitario,

        @NotNull(message = "ID do usuário é obrigatório")
        Long fk_id_usuario,

        @NotNull(message = "ID da conversão é obrigatório")
        Long fk_id_conversao,

        @PositiveOrZero(message = "Taxa de conversão não pode ser negativa")
        Double taxaConversao,

        String observacao,

        Long fk_id_produto,

        String local
) {

}
