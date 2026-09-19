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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/login")
@CrossOrigin("*")
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping()
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Solicitação de login. Usuário: {}", loginRequest.usuario());

        UsuariosEntity usuario = loginService.autenticaoDoLogin(loginRequest);

        log.info("Login realizado com sucesso. Usuário: {}, ID: {}",
                usuario.getUsuario(),
                usuario.getId());

        return new ResponseEntity<>(LoginResponse.de(usuario), HttpStatus.OK);
    }
}
