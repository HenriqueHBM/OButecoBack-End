package projeto.obuteco.controller.estoque.dto;

import projeto.obuteco.model.entity.estoque.MovimentacoesEstoqueEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacoesEstoqueResponse(
        Long id,
        Long fk_id_stock,
        String type,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        Long fk_id_user,
        String user,
        String conversion,
        String product,
        Long fk_id_conversion,
        Long fk_id_product,
        String observation,
        Instant movDate
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
