package projeto.OButecoBack_End.Controller.Usuarios.dto;

import jakarta.validation.constraints.*;
import projeto.OButecoBack_End.Entity.CargosEntity;
import projeto.OButecoBack_End.Entity.UsuariosEntity;

import java.sql.Timestamp;

public record UsuariosRequest(
        Long id,
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        @NotBlank(message = "Usuário é obrigatório")
        String usuario,
        @NotBlank(message = "Senha é obrigatória")
        String senha,
        @NotNull(message = "Cargo é obrigatório")
        Long cargoId,
        Timestamp created_at,
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        Timestamp updated_at,
        Timestamp deleted_at
) {
    public static UsuariosRequest de(UsuariosEntity usuariosEntity) {
        return new UsuariosRequest(
                usuariosEntity.getId(),
                usuariosEntity.getNome(),
                usuariosEntity.getUsuario(),
                usuariosEntity.getSenha(),
                usuariosEntity.getCargosEntity().getId(),
                usuariosEntity.getCreated_at(),
                usuariosEntity.getUpdated_at(),
                usuariosEntity.getDeletedAt()
        );
    }
}
