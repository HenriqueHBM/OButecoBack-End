package projeto.OButecoBack_End.Controller.Usuarios.dto;

import projeto.OButecoBack_End.Entity.CargosEntity;
import projeto.OButecoBack_End.Entity.UsuariosEntity;

import java.sql.Timestamp;

public record UsuariosResponse(
        Long id,
        String nome,
        String usuario,
        String senha,
        Boolean status,
        Long cargo,
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
                usuariosEntity.getStatus(),
                usuariosEntity.getCargosEntity().getId(),
                usuariosEntity.getCreated_at(),
                usuariosEntity.getUpdated_at(),
                usuariosEntity.getDeletedAt()
        );
    }
}
