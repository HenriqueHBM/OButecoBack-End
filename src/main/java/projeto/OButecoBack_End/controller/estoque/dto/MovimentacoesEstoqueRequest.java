package projeto.OButecoBack_End.controller.estoque.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record MovimentacoesEstoqueRequest(
        @JsonProperty("fk_id_estoque")
        Long fk_id_estoque,

        String tipo,

        @PositiveOrZero(message = "Quantidade deve ser maior que zero")
        Double qtde,

        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        Double valorUnitario,

        @JsonProperty("fk_id_usuario")
        Long fk_id_usuario,

        @JsonProperty("fk_id_conversao")
        Long fk_id_conversao,

        @PositiveOrZero(message = "Taxa de conversão não pode ser negativa")
        Double taxaConversao,

        String observacao,

        @JsonProperty("fk_id_produto")
        Long fk_id_produto,

        String local
) {

}
