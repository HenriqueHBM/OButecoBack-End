package projeto.OButeco_Back_End.controller.login.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank(message = "Usuario e obrigatorio")
        String usuario,

        @NotBlank(message = "Senha e obrigatorio")
        String senha
){
}
