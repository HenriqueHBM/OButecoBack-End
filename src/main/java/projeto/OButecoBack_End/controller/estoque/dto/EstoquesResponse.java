package projeto.OButecoBack_End.controller.estoque.dto;

import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;

import java.time.Instant;

public record EstoquesResponse(
        Long id,
        Long fk_id_produto,
        double qtdeEstoque,
        Long fk_id_conversao,
        String local,
        Instant dataCriacao,
        Instant dataAtualizado
) {
    public static EstoquesResponse de(EstoquesEntity estoquesEntity) {
        return new EstoquesResponse(
                estoquesEntity.getId(),
                estoquesEntity.getProdutosEntity().getId(),
                estoquesEntity.getQtdeEstoque(),
                estoquesEntity.getConversoesEntity().getId(),
                estoquesEntity.getLocal(),
                estoquesEntity.getDataCriacao(),
                estoquesEntity.getDataAtualizado()
        );
    }
}
