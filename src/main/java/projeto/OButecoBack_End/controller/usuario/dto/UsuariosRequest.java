package projeto.OButecoBack_End.controller.usuario.dto;

import jakarta.validation.constraints.*;
import projeto.OButecoBack_End.model.Enum.CargoEnum;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;

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
        CargoEnum cargoEnum,
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
                usuariosEntity.getCargo(),
                usuariosEntity.getCreated_at(),
                usuariosEntity.getUpdated_at(),
                usuariosEntity.getDeletedAt()
        );
    }
}
