package projeto.OButecoBack_End.Controller.Usuarios.Cargos.dto;


import jakarta.validation.constraints.*;
import projeto.OButecoBack_End.Entity.CargosEntity;

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
