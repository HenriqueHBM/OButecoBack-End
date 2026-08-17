package projeto.OButecoBack_End.Controller.Login;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.Controller.Login.dto.LoginRequest;
import projeto.OButecoBack_End.Controller.Login.dto.LoginResponse;
import projeto.OButecoBack_End.Entity.UsuariosEntity;
import projeto.OButecoBack_End.Service.LoginService;

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
