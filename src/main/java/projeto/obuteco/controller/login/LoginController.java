package projeto.obuteco.controller.login;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.controller.login.dto.LoginResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.service.usuario.LoginService;

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

        return new ResponseEntity<>(loginService.autenticaoDoLogin(loginRequest), HttpStatus.OK);
    }
}
