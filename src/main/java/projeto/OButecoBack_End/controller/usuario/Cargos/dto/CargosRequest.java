package projeto.OButecoBack_End.controller.usuario.Cargos.dto;


import jakarta.validation.constraints.*;
import projeto.OButecoBack_End.model.entity.usuario.CargosEntity;

public record CargosRequest(
        Long id,
        @NotBlank(message = "Cargo é obrigatório")
        String cargo
) {
    public static CargosRequest de(CargosEntity cargosEntity) {
        return new CargosRequest(
                cargosEntity.getId(),
                cargosEntity.getCargo()
        );
    }
}
