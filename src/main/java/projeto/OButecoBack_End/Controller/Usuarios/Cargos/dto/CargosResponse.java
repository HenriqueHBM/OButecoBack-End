package projeto.OButecoBack_End.Controller.Usuarios.Cargos.dto;

import projeto.OButecoBack_End.Entity.CargosEntity;

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
