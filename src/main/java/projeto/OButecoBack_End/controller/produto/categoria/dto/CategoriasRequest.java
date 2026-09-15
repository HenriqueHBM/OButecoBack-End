package projeto.OButecoBack_End.controller.produto.categoria.dto;

import projeto.OButecoBack_End.model.entity.produto.CategoriasEntity;

public record CategoriasRequest(
        Long id,
        String categoria
) {
    public CategoriasRequest de(CategoriasEntity categoriasEntity) {
        return new CategoriasRequest(
                categoriasEntity.getId(),
                categoriasEntity.getCategoria());
    }
}
