package projeto.obuteco.controller.produto.dto;

import projeto.obuteco.model.entity.produto.InsumosProdutoEntity;

import java.math.BigDecimal;

public record InsumosResponse(Long insumoId, String nomeInsumo, BigDecimal qtde) {
    public static InsumosResponse de(InsumosProdutoEntity e) {
        return new InsumosResponse(
                e.getInsumos().getId(), e.getInsumos().getNome(), e.getQtde());
    }
}
