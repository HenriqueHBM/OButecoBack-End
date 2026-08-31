package projeto.OButecoBack_End.controller.login;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.login.dto.LoginRequest;
import projeto.OButecoBack_End.controller.login.dto.LoginResponse;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.service.usuario.LoginService;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping()
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsuariosEntity usuario = loginService.autenticaoDoLogin(loginRequest);
        return new ResponseEntity<>(LoginResponse.de(usuario), HttpStatus.OK);
    }
}
