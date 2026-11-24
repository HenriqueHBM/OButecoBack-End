package projeto.obuteco.controller.usuario.dto;

import projeto.obuteco.model.enums.CargoEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;

import java.sql.Timestamp;

public record UsuariosResponse(
        Long id,
        String name,
        String user,
        EStatus status,
        CargoEnum role,
        Timestamp creationDate,
        Timestamp updateDate,
        Timestamp deleteDate
) {
    public static UsuariosResponse de(UsuariosEntity usuariosEntity) {
        return new UsuariosResponse(
                usuariosEntity.getId(),
                usuariosEntity.getNome(),
                usuariosEntity.getUsuario(),
                usuariosEntity.getStatus(),
                usuariosEntity.getCargo(),
                usuariosEntity.getCreatedAt(),
                usuariosEntity.getUpdatedAt(),
                usuariosEntity.getDeletedAt()
        );
    }
}
