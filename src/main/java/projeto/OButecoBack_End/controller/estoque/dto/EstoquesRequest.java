package projeto.OButecoBack_End.controller.estoque.dto;

import jakarta.validation.constraints.*;

public record EstoquesRequest(
        @NotNull(message = "Produto é obrigatório")
        Long fk_id_produto,

        @NotBlank(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        double qtdeEstoque,

        @NotNull(message = "Conversão é obrigatória")
        Long fk_id_conversao,

        String local
) {
}
