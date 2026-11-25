package projeto.obuteco.controller.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import projeto.obuteco.model.enums.CargoEnum;

public record UsuariosAtualizacaoRequest(
        @NotBlank
        String nome,
        @NotBlank
        String usuario,
        String senha,
        @NotNull
        CargoEnum cargoEnum
) {

}
