package projeto.OButecoBack_End.controller.usuario.dto;

import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;

import java.sql.Timestamp;

public record UsuariosResponse(
        Long id,
        String nome,
        String usuario,
        String senha,
        String cargo,
        Timestamp created_at,
        Timestamp updated_at,
        Timestamp deleted_at
) {
    public static UsuariosResponse de(UsuariosEntity usuariosEntity) {
        return new UsuariosResponse(
                usuariosEntity.getId(),
                usuariosEntity.getNome(),
                usuariosEntity.getUsuario(),
                usuariosEntity.getSenha(),
                usuariosEntity.getCargosEntity().getCargo(),
                usuariosEntity.getCreated_at(),
                usuariosEntity.getUpdated_at(),
                usuariosEntity.getDeletedAt()
        );
    }
}
