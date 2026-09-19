package projeto.OButecoBack_End.controller.produto.dto;

import projeto.OButecoBack_End.model.entity.produto.InsumosProdutoEntity;

import java.math.BigDecimal;

public record InsumosResponse(Long insumoId, String nomeInsumo, BigDecimal qtde) {
    public static InsumosResponse de(InsumosProdutoEntity e) {
        return new InsumosResponse(
                e.getInsumos().getId(), e.getInsumos().getNome(), e.getQtde());
    }
}
