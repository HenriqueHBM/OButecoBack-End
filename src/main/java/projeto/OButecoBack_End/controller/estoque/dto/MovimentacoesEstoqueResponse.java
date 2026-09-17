package projeto.OButecoBack_End.controller.estoque.dto;

import projeto.OButecoBack_End.model.entity.estoque.MovimentacoesEstoqueEntity;

import java.time.Instant;

public record MovimentacoesEstoqueResponse(
        Long id,
        Long fk_id_estoque,
        String tipo,
        double qtde,
        double valorUnitario,
        double valorTotal,
        Long fk_id_usuario,
        Long fk_id_conversao,
        Long fk_id_produto,
        String observacao,
        Instant dataMovimentacao
) {
    public static MovimentacoesEstoqueResponse de(MovimentacoesEstoqueEntity entity) {
        return new MovimentacoesEstoqueResponse(
                entity.getId(),
                entity.getEstoqueEntity().getId(),
                entity.getTipo(),
                entity.getQtde(),
                entity.getValorUnitario(),
                entity.getValorTotal(),
                entity.getUsuarioEntity().getId(),
                entity.getConversoesEntity().getId(),
                entity.getProdutosEntity() != null ? entity.getProdutosEntity().getId() : null,
                entity.getObservacao(),
                entity.getDataMovimentacao()
        );
    }
}
