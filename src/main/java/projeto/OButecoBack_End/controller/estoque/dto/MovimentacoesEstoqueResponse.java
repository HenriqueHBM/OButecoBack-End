package projeto.OButecoBack_End.controller.estoque.dto;

import projeto.OButecoBack_End.model.entity.estoque.MovimentacoesEstoqueEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacoesEstoqueResponse(
        Long id,
        Long fk_id_estoque,
        String tipo,
        BigDecimal qtde,
        BigDecimal valorUnitario,
        BigDecimal valorTotal,
        Long fk_id_usuario,
        String usuario,
        String conversao,
        String produto,
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
                entity.getQuantidade(),
                entity.getValorUnitario(),
                entity.getValorTotal(),
                entity.getUsuarioEntity().getId(),
                entity.getUsuarioEntity().getNome(),
                entity.getConversoesEntity().getNomenclatura(),
                entity.getProdutosEntity().getNome(),
                entity.getConversoesEntity().getId(),
                entity.getProdutosEntity() != null ? entity.getProdutosEntity().getId() : null,
                entity.getObservacao(),
                entity.getDataMovimentacao()
        );
    }
}
