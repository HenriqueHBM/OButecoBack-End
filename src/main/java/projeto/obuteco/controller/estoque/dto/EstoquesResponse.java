package projeto.obuteco.controller.estoque.dto;

import projeto.obuteco.model.entity.estoque.EstoquesEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record EstoquesResponse(
        Long id,
        String product,
        Long fk_id_product,
        BigDecimal stockQuantity,
        Long fk_id_conversion,
        String local,
        Instant creationDate,
        Instant updateDate
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
