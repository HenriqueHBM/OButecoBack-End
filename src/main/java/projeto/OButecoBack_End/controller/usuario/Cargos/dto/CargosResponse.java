package projeto.OButecoBack_End.controller.usuario.Cargos.dto;

import projeto.OButecoBack_End.model.entity.usuario.CargosEntity;

public record CargosResponse(
        long id,
        String cargo
) {
    public static CargosResponse de(CargosEntity cargosEntity) {
        return new CargosResponse(
                cargosEntity.getId(),
                cargosEntity.getCargo()
        );
    }
}
