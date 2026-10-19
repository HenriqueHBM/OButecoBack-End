package projeto.obuteco.controller.login;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.controller.login.dto.LoginResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.service.usuario.LoginService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/login")
@CrossOrigin("http://localhost:4200")
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
