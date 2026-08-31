package projeto.OButecoBack_End.controller.produto.categoria.dto;

import jakarta.persistence.Id;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosResponse;
import projeto.OButecoBack_End.model.entity.produto.CategoriasEntity;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;

public record CategoriasResponse(
        Long id,
        String categoria
) {
    public static CategoriasResponse de(CategoriasEntity categoriasEntity) {
        return new CategoriasResponse(
                categoriasEntity.getId(),
                categoriasEntity.getCategoria()
        );
    }
}
