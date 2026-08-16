package projeto.OButecoBack_End.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.Controller.Login.dto.LoginRequest;
import projeto.OButecoBack_End.Entity.UsuariosEntity;
import projeto.OButecoBack_End.Repository.UsuariosRepository;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UsuariosRepository usuariosRepository;

    public UsuariosEntity autenticaoDoLogin(LoginRequest loginRequest) {
        UsuariosEntity usuario = usuariosRepository.findByUsuarioAndDeletedAtIsNull(loginRequest.usuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos"));

        if (!usuario.getSenha().equals(loginRequest.senha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }

        return usuario;
    }
}
