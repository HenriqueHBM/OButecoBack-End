package projeto.OButecoBack_End.model.service.usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.login.dto.LoginRequest;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.repository.usuario.UsuariosRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
    private final UsuariosRepository usuariosRepository;

    public UsuariosEntity autenticaoDoLogin(LoginRequest loginRequest) {
        log.info("Autenticando usuário: {}", loginRequest.usuario());

        //procura se existe o usuarios vindo do request
        UsuariosEntity usuario = usuariosRepository.findByUsuarioAndDeletedAtIsNull(loginRequest.usuario())
                .orElseThrow(() -> {
                    log.warn("Tentativa de login com usuário inexistente: {}", loginRequest.usuario());

                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");});

        //verifica a senha se bate
        if (!usuario.getSenha().equals(loginRequest.senha())) {
            log.warn("Tentativa de login com senha inválida. Usuário: {}", loginRequest.usuario());

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }

        log.info("Autenticação realizada com sucesso. Usuário: {}, ID: {}",
                usuario.getUsuario(),
                usuario.getId());

        return usuario;
    }
}
