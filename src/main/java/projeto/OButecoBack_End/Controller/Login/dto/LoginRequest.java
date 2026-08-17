package projeto.OButecoBack_End.Controller.Login.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank(message = "Usuario e obrigatorio")
        String usuario,

        @NotBlank(message = "Senha e obrigatorio")
        String senha
){
}
