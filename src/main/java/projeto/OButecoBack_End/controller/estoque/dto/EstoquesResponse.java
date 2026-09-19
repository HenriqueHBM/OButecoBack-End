package projeto.OButecoBack_End.controller.estoque.dto;

import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record EstoquesResponse(
        Long id,
        String produto,
        Long fk_id_produto,
        BigDecimal qtdeEstoque,
        Long fk_id_conversao,
        String local,
        Instant dataCriacao,
        Instant dataAtualizado
) {
    public static EstoquesResponse de(EstoquesEntity estoquesEntity) {
        return new EstoquesResponse(
                estoquesEntity.getId(),
                estoquesEntity.getProdutosEntity().getNome(),
                estoquesEntity.getProdutosEntity().getId(),
                estoquesEntity.getQntdEstoque(),
                estoquesEntity.getConversoesEntity().getId(),
                estoquesEntity.getLocal(),
                estoquesEntity.getDataCriacao(),
                estoquesEntity.getDataAtualizado()
        );
    }
}
